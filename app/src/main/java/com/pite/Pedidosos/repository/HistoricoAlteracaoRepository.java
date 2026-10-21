package com.pite.Pedidosos.repository;

import com.pite.Pedidosos.model.HistoricoAlteracao;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface HistoricoAlteracaoRepository extends JpaRepository<HistoricoAlteracao, Long> {

    @EntityGraph(attributePaths = {"usuario"})
    List<HistoricoAlteracao> findByPedidoIdOrderByDataHoraDesc(Long pedidoId);
}
