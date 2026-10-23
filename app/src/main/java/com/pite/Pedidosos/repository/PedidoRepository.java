package com.pite.Pedidosos.repository;

import com.pite.Pedidosos.model.Pedido;
import com.pite.Pedidosos.model.StatusPedido;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    @EntityGraph(attributePaths = {"cliente", "vendedor"})
    Page<Pedido> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"cliente", "vendedor"})
    Page<Pedido> findByStatus(StatusPedido status, Pageable pageable);

    @EntityGraph(attributePaths = {"cliente", "vendedor"})
    Page<Pedido> findByVendedorEmailIgnoreCase(String email, Pageable pageable);

    @EntityGraph(attributePaths = {"cliente", "vendedor"})
    Page<Pedido> findByStatusAndVendedorEmailIgnoreCase(StatusPedido status, String email, Pageable pageable);

    @EntityGraph(attributePaths = {"cliente", "vendedor", "itens"})
    Optional<Pedido> findWithItensById(Long id);
}
