package com.pite.Pedidosos.service;

import com.pite.Pedidosos.dto.UsuarioForm;
import com.pite.Pedidosos.model.Perfil;
import com.pite.Pedidosos.model.Usuario;
import com.pite.Pedidosos.repository.UsuarioRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarios;
    private final PasswordEncoder encoder;

    public UsuarioService(UsuarioRepository usuarios, PasswordEncoder encoder) {
        this.usuarios = usuarios;
        this.encoder = encoder;
    }

    @Transactional(readOnly = true)
    public Page<Usuario> listar(Pageable pageable) {
        return usuarios.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Usuario buscar(Long id) {
        return usuarios.findById(id)
                .orElseThrow(() -> new RegraDeNegocioException("Usuário não encontrado."));
    }

    @Transactional
    public Usuario criar(UsuarioForm form) {
        String email = normalizar(form.getEmail());
        exigirSenhaValida(form.getSenha());
        if (usuarios.existsByEmailIgnoreCase(email)) {
            throw new RegraDeNegocioException("Já existe um usuário com este e-mail.");
        }
        Usuario usuario = new Usuario();
        preencher(usuario, form, email);
        usuario.setSenhaHash(encoder.encode(form.getSenha()));
        return usuarios.save(usuario);
    }

    @Transactional
    public Usuario atualizar(Long id, UsuarioForm form, String emailLogado) {
        Usuario usuario = buscar(id);
        String email = normalizar(form.getEmail());
        if (usuarios.existsByEmailIgnoreCaseAndIdNot(email, id)) {
            throw new RegraDeNegocioException("Já existe um usuário com este e-mail.");
        }
        boolean proprio = usuario.getEmail().equalsIgnoreCase(emailLogado);
        if (proprio && (!form.isAtivo() || form.getPerfil() != Perfil.ADMIN)) {
            throw new RegraDeNegocioException("Você não pode desativar nem rebaixar o próprio usuário.");
        }
        if (proprio && !usuario.getEmail().equalsIgnoreCase(email)) {
            throw new RegraDeNegocioException("Você não pode alterar o próprio e-mail por aqui.");
        }
        preencher(usuario, form, email);
        if (form.getSenha() != null && !form.getSenha().isBlank()) {
            exigirSenhaValida(form.getSenha());
            usuario.setSenhaHash(encoder.encode(form.getSenha()));
        }
        return usuario;
    }

    private void preencher(Usuario usuario, UsuarioForm form, String email) {
        usuario.setNome(form.getNome().trim());
        usuario.setEmail(email);
        usuario.setPerfil(form.getPerfil());
        usuario.setAtivo(form.isAtivo());
    }

    private void exigirSenhaValida(String senha) {
        if (senha == null || senha.length() < 8) {
            throw new RegraDeNegocioException("A senha precisa ter pelo menos 8 caracteres.");
        }
    }

    private String normalizar(String email) {
        return email.trim().toLowerCase();
    }
}
