package com.pite.Pedidosos.service;

import com.pite.Pedidosos.model.*;
import com.pite.Pedidosos.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

@Service
public class PedidoService {

    private final PedidoRepository pedidos;
    private final ClienteRepository clientes;
    private final UsuarioRepository usuarios;
    private final OrdemServicoRepository ordens;
    private final HistoricoAlteracaoRepository historico;

    public PedidoService(PedidoRepository pedidos, ClienteRepository clientes,
                         UsuarioRepository usuarios, OrdemServicoRepository ordens,
                         HistoricoAlteracaoRepository historico) {
        this.pedidos = pedidos;
        this.clientes = clientes;
        this.usuarios = usuarios;
        this.ordens = ordens;
        this.historico = historico;
    }

    @Transactional
    public Pedido criar(Long clienteId, Long vendedorId, String observacao, List<ItemPedido> itens) {
        exigirItens(itens);
        Cliente cliente = clientes.findById(clienteId)
                .orElseThrow(() -> new RegraDeNegocioException("Cliente não encontrado."));
        Usuario vendedor = usuarioComPerfil(vendedorId, Perfil.VENDEDOR);

        Pedido pedido = new Pedido();
        pedido.setCliente(cliente);
        pedido.setVendedor(vendedor);
        pedido.setObservacao(observacao);
        itens.forEach(pedido::adicionarItem);
        pedidos.save(pedido);

        registrar(pedido, vendedor, "status", null, pedido.getStatus());
        return pedido;
    }

    @Transactional(readOnly = true)
    public Pedido buscar(Long id) {
        return pedidos.findWithItensById(id)
                .orElseThrow(() -> new RegraDeNegocioException("Pedido não encontrado."));
    }

    @Transactional
    public Pedido editar(Long pedidoId, Long analistaId, String observacao, List<ItemPedido> novosItens) {
        exigirItens(novosItens);
        Usuario analista = usuarioComPerfil(analistaId, Perfil.ANALISTA);
        Pedido pedido = buscar(pedidoId);
        exigirEditavel(pedido);

        if (pedido.getStatus() == StatusPedido.CRIADO) {
            mudarStatus(pedido, StatusPedido.EM_REVISAO, analista);
        }
        if (!Objects.equals(pedido.getObservacao(), observacao)) {
            registrar(pedido, analista, "observacao", pedido.getObservacao(), observacao);
            pedido.setObservacao(observacao);
        }

        BigDecimal totalAnterior = pedido.getValorTotal();
        pedido.getItens().clear();
        novosItens.forEach(pedido::adicionarItem);
        if (totalAnterior.compareTo(pedido.getValorTotal()) != 0) {
            registrar(pedido, analista, "valor_total", totalAnterior, pedido.getValorTotal());
        }
        return pedido;
    }

    @Transactional
    public OrdemServico aprovar(Long pedidoId, Long analistaId) {
        Usuario analista = usuarioComPerfil(analistaId, Perfil.ANALISTA);
        Pedido pedido = buscar(pedidoId);
        if (pedido.getStatus() != StatusPedido.EM_REVISAO) {
            throw new RegraDeNegocioException("Só é possível aprovar pedidos em revisão.");
        }
        if (ordens.existsByPedidoId(pedidoId)) {
            throw new RegraDeNegocioException("Este pedido já gerou uma ordem de serviço.");
        }
        mudarStatus(pedido, StatusPedido.APROVADO, analista);

        OrdemServico os = new OrdemServico();
        os.setPedido(pedido);
        os.setAnalista(analista);
        return ordens.save(os);
    }

    @Transactional
    public Pedido cancelar(Long pedidoId, Long usuarioId) {
        Usuario usuario = usuarios.findById(usuarioId)
                .orElseThrow(() -> new RegraDeNegocioException("Usuário não encontrado."));
        Pedido pedido = buscar(pedidoId);
        exigirEditavel(pedido);
        boolean vendedorDeOutroPedido = usuario.getPerfil() == Perfil.VENDEDOR
                && !pedido.getVendedor().getId().equals(usuarioId);
        if (!usuario.isAtivo() || vendedorDeOutroPedido) {
            throw new RegraDeNegocioException("Usuário sem permissão para cancelar este pedido.");
        }
        mudarStatus(pedido, StatusPedido.CANCELADO, usuario);
        return pedido;
    }

    private void exigirItens(List<ItemPedido> itens) {
        if (itens == null || itens.isEmpty()) {
            throw new RegraDeNegocioException("O pedido precisa ter pelo menos um item.");
        }
    }

    private void exigirEditavel(Pedido pedido) {
        if (pedido.getStatus() == StatusPedido.APROVADO || pedido.getStatus() == StatusPedido.CANCELADO) {
            throw new RegraDeNegocioException("Pedidos aprovados ou cancelados não podem ser alterados.");
        }
    }

    private Usuario usuarioComPerfil(Long id, Perfil perfil) {
        Usuario usuario = usuarios.findById(id)
                .orElseThrow(() -> new RegraDeNegocioException("Usuário não encontrado."));
        if (!usuario.isAtivo() || usuario.getPerfil() != perfil) {
            throw new RegraDeNegocioException("Usuário sem permissão para esta ação.");
        }
        return usuario;
    }

    private void mudarStatus(Pedido pedido, StatusPedido novo, Usuario usuario) {
        registrar(pedido, usuario, "status", pedido.getStatus(), novo);
        pedido.setStatus(novo);
    }

    private void registrar(Pedido pedido, Usuario usuario, String campo, Object anterior, Object novo) {
        HistoricoAlteracao h = new HistoricoAlteracao();
        h.setPedido(pedido);
        h.setUsuario(usuario);
        h.setCampo(campo);
        h.setValorAnterior(anterior == null ? null : anterior.toString());
        h.setValorNovo(novo == null ? null : novo.toString());
        historico.save(h);
    }
}
