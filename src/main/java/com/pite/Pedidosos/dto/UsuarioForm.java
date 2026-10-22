package com.pite.Pedidosos.dto;

import com.pite.Pedidosos.model.Perfil;
import com.pite.Pedidosos.model.Usuario;
import jakarta.validation.constraints.*;

public class UsuarioForm {

    @NotBlank @Size(max = 120)
    private String nome;

    @NotBlank @Email @Size(max = 160)
    private String email;

    @Size(max = 72)
    private String senha;

    @NotNull
    private Perfil perfil;

    private boolean ativo = true;

    public static UsuarioForm de(Usuario usuario) {
        UsuarioForm form = new UsuarioForm();
        form.nome = usuario.getNome();
        form.email = usuario.getEmail();
        form.perfil = usuario.getPerfil();
        form.ativo = usuario.isAtivo();
        return form;
    }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }
    public Perfil getPerfil() { return perfil; }
    public void setPerfil(Perfil perfil) { this.perfil = perfil; }
    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }
}
