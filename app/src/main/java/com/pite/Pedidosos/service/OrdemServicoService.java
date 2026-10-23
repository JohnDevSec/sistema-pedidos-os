package com.pite.Pedidosos.service;

import com.pite.Pedidosos.model.*;
import com.pite.Pedidosos.repository.HistoricoAlteracaoRepository;
import com.pite.Pedidosos.repository.OrdemServicoRepository;
import com.pite.Pedidosos.repository.UsuarioRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class OrdemServicoService {

    private final OrdemServicoRepository ordens;
    private final UsuarioRepository usuarios;
    private final HistoricoAlteracaoRepository historico;

    public OrdemServicoService(OrdemServicoRepository ordens, UsuarioRepository usuarios,
                               HistoricoAlteracaoRepository historico) {
        this.ordens = ordens;
        this.usuarios = usuarios;
        this.historico = historico;
    }

    @Transactional(readOnly = true)
    public Page<OrdemServico> listar(StatusOS status, String emailVendedor, Pageable pageable) {
        if (emailVendedor == null) {
            return status == null ? ordens.findAll(pageable) : ordens.findByStatus(status, pageable);
        }
        return status == null
                ? ordens.findByPedidoVendedorEmailIgnoreCase(emailVendedor, pageable)
                : ordens.findByStatusAndPedidoVendedorEmailIgnoreCase(status, emailVendedor, pageable);
    }

    @Transactional
    public OrdemServico avancar(Long id, String emailAnalista) {
        Usuario analista = usuarios.findByEmailIgnoreCase(emailAnalista)
                .orElseThrow(() -> new RegraDeNegocioException("Usuário não encontrado."));
        if (!analista.isAtivo() || analista.getPerfil() != Perfil.ANALISTA) {
            throw new RegraDeNegocioException("Usuário sem permissão para esta ação.");
        }
        OrdemServico os = ordens.findById(id)
                .orElseThrow(() -> new RegraDeNegocioException("Ordem de serviço não encontrada."));

        StatusOS anterior = os.getStatus();
        switch (anterior) {
            case ABERTA -> os.setStatus(StatusOS.EM_ANDAMENTO);
            case EM_ANDAMENTO -> {
                os.setStatus(StatusOS.CONCLUIDA);
                os.setDataConclusao(LocalDateTime.now());
            }
            case CONCLUIDA -> throw new RegraDeNegocioException("Esta ordem de serviço já foi concluída.");
        }

        HistoricoAlteracao registro = new HistoricoAlteracao();
        registro.setPedido(os.getPedido());
        registro.setUsuario(analista);
        registro.setCampo("status_os");
        registro.setValorAnterior(anterior.name());
        registro.setValorNovo(os.getStatus().name());
        historico.save(registro);
        return os;
    }
}
