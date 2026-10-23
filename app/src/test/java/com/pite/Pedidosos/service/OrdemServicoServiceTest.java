package com.pite.Pedidosos.service;

import com.pite.Pedidosos.model.*;
import com.pite.Pedidosos.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrdemServicoServiceTest {

    private static final String EMAIL = "analista@teste.com";

    @Mock OrdemServicoRepository ordens;
    @Mock UsuarioRepository usuarios;
    @Mock HistoricoAlteracaoRepository historico;
    @InjectMocks OrdemServicoService service;

    private Usuario usuario(Perfil perfil) {
        Usuario u = new Usuario();
        u.setPerfil(perfil);
        u.setEmail(EMAIL);
        ReflectionTestUtils.setField(u, "id", 5L);
        return u;
    }

    private OrdemServico ordem(StatusOS status) {
        OrdemServico os = new OrdemServico();
        os.setStatus(status);
        os.setPedido(new Pedido());
        return os;
    }

    @Test
    void iniciaOrdemAberta() {
        OrdemServico os = ordem(StatusOS.ABERTA);
        when(usuarios.findByEmailIgnoreCase(EMAIL)).thenReturn(Optional.of(usuario(Perfil.ANALISTA)));
        when(ordens.findById(1L)).thenReturn(Optional.of(os));

        service.avancar(1L, EMAIL);

        assertEquals(StatusOS.EM_ANDAMENTO, os.getStatus());
        assertNull(os.getDataConclusao());
        verify(historico).save(any(HistoricoAlteracao.class));
    }

    @Test
    void concluiOrdemEmAndamentoEPreencheData() {
        OrdemServico os = ordem(StatusOS.EM_ANDAMENTO);
        when(usuarios.findByEmailIgnoreCase(EMAIL)).thenReturn(Optional.of(usuario(Perfil.ANALISTA)));
        when(ordens.findById(1L)).thenReturn(Optional.of(os));

        service.avancar(1L, EMAIL);

        assertEquals(StatusOS.CONCLUIDA, os.getStatus());
        assertNotNull(os.getDataConclusao());
        verify(historico).save(any(HistoricoAlteracao.class));
    }

    @Test
    void ordemConcluidaNaoAvanca() {
        OrdemServico os = ordem(StatusOS.CONCLUIDA);
        when(usuarios.findByEmailIgnoreCase(EMAIL)).thenReturn(Optional.of(usuario(Perfil.ANALISTA)));
        when(ordens.findById(1L)).thenReturn(Optional.of(os));

        assertThrows(RegraDeNegocioException.class, () -> service.avancar(1L, EMAIL));
        verify(historico, never()).save(any());
    }

    @Test
    void usuarioQueNaoEAnalistaNaoAvanca() {
        when(usuarios.findByEmailIgnoreCase(EMAIL)).thenReturn(Optional.of(usuario(Perfil.VENDEDOR)));

        assertThrows(RegraDeNegocioException.class, () -> service.avancar(1L, EMAIL));
        verifyNoInteractions(ordens);
    }
}
