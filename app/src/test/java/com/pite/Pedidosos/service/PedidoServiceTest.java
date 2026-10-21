package com.pite.Pedidosos.service;

import com.pite.Pedidosos.model.*;
import com.pite.Pedidosos.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PedidoServiceTest {

    @Mock PedidoRepository pedidos;
    @Mock ClienteRepository clientes;
    @Mock UsuarioRepository usuarios;
    @Mock OrdemServicoRepository ordens;
    @Mock HistoricoAlteracaoRepository historico;
    @InjectMocks PedidoService service;

    private Usuario usuario(Long id, Perfil perfil) {
        Usuario u = new Usuario();
        u.setPerfil(perfil);
        ReflectionTestUtils.setField(u, "id", id);
        return u;
    }

    private ItemPedido item(String descricao, int quantidade, String valor) {
        ItemPedido i = new ItemPedido();
        i.setDescricao(descricao);
        i.setQuantidade(quantidade);
        i.setValorUnitario(new BigDecimal(valor));
        return i;
    }

    private Pedido pedido(StatusPedido status, Usuario vendedor) {
        Pedido p = new Pedido();
        p.setVendedor(vendedor);
        p.setStatus(status);
        p.adicionarItem(item("Instalação", 1, "100.00"));
        return p;
    }

    @Test
    void criarSemItensLancaExcecao() {
        assertThrows(RegraDeNegocioException.class,
                () -> service.criar(1L, 2L, null, List.of()));
        verifyNoInteractions(pedidos);
    }

    @Test
    void criarCalculaTotalEGravaHistorico() {
        when(clientes.findById(1L)).thenReturn(Optional.of(new Cliente()));
        when(usuarios.findById(2L)).thenReturn(Optional.of(usuario(2L, Perfil.VENDEDOR)));
        when(pedidos.save(any(Pedido.class))).thenAnswer(i -> i.getArgument(0));

        Pedido criado = service.criar(1L, 2L, "urgente",
                List.of(item("Cabo", 2, "10.50"), item("Conector", 1, "5.00")));

        assertEquals(0, new BigDecimal("26.00").compareTo(criado.getValorTotal()));
        assertEquals(StatusPedido.CRIADO, criado.getStatus());
        verify(historico).save(any(HistoricoAlteracao.class));
    }

    @Test
    void criarComUsuarioQueNaoEVendedorLancaExcecao() {
        when(clientes.findById(1L)).thenReturn(Optional.of(new Cliente()));
        when(usuarios.findById(3L)).thenReturn(Optional.of(usuario(3L, Perfil.ANALISTA)));

        assertThrows(RegraDeNegocioException.class,
                () -> service.criar(1L, 3L, null, List.of(item("Cabo", 1, "10.00"))));
        verify(pedidos, never()).save(any());
    }

    @Test
    void aprovarGeraOrdemDeServicoEMudaStatus() {
        Pedido pedido = pedido(StatusPedido.EM_REVISAO, usuario(2L, Perfil.VENDEDOR));
        when(usuarios.findById(5L)).thenReturn(Optional.of(usuario(5L, Perfil.ANALISTA)));
        when(pedidos.findWithItensById(1L)).thenReturn(Optional.of(pedido));
        when(ordens.existsByPedidoId(1L)).thenReturn(false);
        when(ordens.save(any(OrdemServico.class))).thenAnswer(i -> i.getArgument(0));

        OrdemServico os = service.aprovar(1L, 5L);

        assertEquals(StatusPedido.APROVADO, pedido.getStatus());
        assertEquals(StatusOS.ABERTA, os.getStatus());
        assertSame(pedido, os.getPedido());
    }

    @Test
    void aprovarPedidoForaDeRevisaoLancaExcecao() {
        Pedido pedido = pedido(StatusPedido.CRIADO, usuario(2L, Perfil.VENDEDOR));
        when(usuarios.findById(5L)).thenReturn(Optional.of(usuario(5L, Perfil.ANALISTA)));
        when(pedidos.findWithItensById(1L)).thenReturn(Optional.of(pedido));

        assertThrows(RegraDeNegocioException.class, () -> service.aprovar(1L, 5L));
        verify(ordens, never()).save(any());
    }

    @Test
    void aprovarPedidoQueJaTemOrdemLancaExcecao() {
        Pedido pedido = pedido(StatusPedido.EM_REVISAO, usuario(2L, Perfil.VENDEDOR));
        when(usuarios.findById(5L)).thenReturn(Optional.of(usuario(5L, Perfil.ANALISTA)));
        when(pedidos.findWithItensById(1L)).thenReturn(Optional.of(pedido));
        when(ordens.existsByPedidoId(1L)).thenReturn(true);

        assertThrows(RegraDeNegocioException.class, () -> service.aprovar(1L, 5L));
        verify(ordens, never()).save(any());
    }

    @Test
    void editarPedidoAprovadoLancaExcecao() {
        Pedido pedido = pedido(StatusPedido.APROVADO, usuario(2L, Perfil.VENDEDOR));
        when(usuarios.findById(5L)).thenReturn(Optional.of(usuario(5L, Perfil.ANALISTA)));
        when(pedidos.findWithItensById(1L)).thenReturn(Optional.of(pedido));

        assertThrows(RegraDeNegocioException.class,
                () -> service.editar(1L, 5L, "obs", List.of(item("Cabo", 1, "10.00"))));
    }

    @Test
    void vendedorNaoCancelaPedidoDeOutroVendedor() {
        Pedido pedido = pedido(StatusPedido.CRIADO, usuario(2L, Perfil.VENDEDOR));
        when(usuarios.findById(3L)).thenReturn(Optional.of(usuario(3L, Perfil.VENDEDOR)));
        when(pedidos.findWithItensById(1L)).thenReturn(Optional.of(pedido));

        assertThrows(RegraDeNegocioException.class, () -> service.cancelar(1L, 3L));
        assertEquals(StatusPedido.CRIADO, pedido.getStatus());
    }

    @Test
    void vendedorCancelaProprioPedido() {
        Usuario dono = usuario(2L, Perfil.VENDEDOR);
        Pedido pedido = pedido(StatusPedido.CRIADO, dono);
        when(usuarios.findById(2L)).thenReturn(Optional.of(dono));
        when(pedidos.findWithItensById(1L)).thenReturn(Optional.of(pedido));

        service.cancelar(1L, 2L);

        assertEquals(StatusPedido.CANCELADO, pedido.getStatus());
    }
}
