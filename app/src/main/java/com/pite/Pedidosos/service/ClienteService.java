package com.pite.Pedidosos.service;

import com.pite.Pedidosos.model.Cliente;
import com.pite.Pedidosos.repository.ClienteRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClienteService {

    private final ClienteRepository clientes;

    public ClienteService(ClienteRepository clientes) {
        this.clientes = clientes;
    }

    @Transactional(readOnly = true)
    public Page<Cliente> listar(String busca, Pageable pageable) {
        if (busca == null || busca.isBlank()) {
            return clientes.findAll(pageable);
        }
        return clientes.findByNomeContainingIgnoreCase(busca.trim(), pageable);
    }

    @Transactional(readOnly = true)
    public Cliente buscar(Long id) {
        return clientes.findById(id)
                .orElseThrow(() -> new RegraDeNegocioException("Cliente não encontrado."));
    }

    @Transactional
    public Cliente criar(Cliente cliente) {
        if (clientes.existsByDocumento(cliente.getDocumento())) {
            throw new RegraDeNegocioException("Já existe um cliente com este documento.");
        }
        return clientes.save(cliente);
    }

    @Transactional
    public Cliente atualizar(Long id, Cliente dados) {
        Cliente cliente = buscar(id);
        if (clientes.existsByDocumentoAndIdNot(dados.getDocumento(), id)) {
            throw new RegraDeNegocioException("Já existe um cliente com este documento.");
        }
        cliente.setNome(dados.getNome());
        cliente.setDocumento(dados.getDocumento());
        cliente.setEmail(dados.getEmail());
        cliente.setTelefone(dados.getTelefone());
        return cliente;
    }
}
