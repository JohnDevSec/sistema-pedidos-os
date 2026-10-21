package com.pite.Pedidosos.repository;

import com.pite.Pedidosos.model.OrdemServico;
import com.pite.Pedidosos.model.StatusOS;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrdemServicoRepository extends JpaRepository<OrdemServico, Long> {

    boolean existsByPedidoId(Long pedidoId);

    @EntityGraph(attributePaths = {"pedido", "analista"})
    Page<OrdemServico> findByStatus(StatusOS status, Pageable pageable);

    @EntityGraph(attributePaths = {"pedido", "analista"})
    Page<OrdemServico> findAll(Pageable pageable);
}
