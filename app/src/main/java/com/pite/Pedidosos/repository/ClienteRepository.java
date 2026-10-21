package com.pite.Pedidosos.repository;

import com.pite.Pedidosos.model.Cliente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    boolean existsByDocumento(String documento);
    Page<Cliente> findByNomeContainingIgnoreCase(String nome, Pageable pageable);
}
