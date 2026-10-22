package com.pite.Pedidosos.controller;

import com.pite.Pedidosos.dto.UsuarioForm;
import com.pite.Pedidosos.model.Perfil;
import com.pite.Pedidosos.service.RegraDeNegocioException;
import com.pite.Pedidosos.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioService service;

    public UsuarioController(UsuarioService service) {
        this.service = service;
    }

    @GetMapping
    public String listar(@RequestParam(defaultValue = "0") int pagina, Model model) {
        model.addAttribute("pagina",
                service.listar(PageRequest.of(Math.max(pagina, 0), 10, Sort.by("nome"))));
        return "usuarios/lista";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("usuario", new UsuarioForm());
        preencherForm(model, "Novo usuário", "/usuarios", false);
        return "usuarios/form";
    }

    @PostMapping
    public String criar(@Valid @ModelAttribute("usuario") UsuarioForm form, BindingResult result,
                        RedirectAttributes redirect, Model model) {
        preencherForm(model, "Novo usuário", "/usuarios", false);
        if (!result.hasErrors()) {
            try {
                service.criar(form);
                redirect.addFlashAttribute("sucesso", "Usuário cadastrado.");
                return "redirect:/usuarios";
            } catch (RegraDeNegocioException e) {
                result.reject("regra", e.getMessage());
            }
        }
        return "usuarios/form";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("usuario", UsuarioForm.de(service.buscar(id)));
        preencherForm(model, "Editar usuário", "/usuarios/" + id, true);
        return "usuarios/form";
    }

    @PostMapping("/{id}")
    public String atualizar(@PathVariable Long id, @Valid @ModelAttribute("usuario") UsuarioForm form,
                            BindingResult result, Principal principal,
                            RedirectAttributes redirect, Model model) {
        preencherForm(model, "Editar usuário", "/usuarios/" + id, true);
        if (!result.hasErrors()) {
            try {
                service.atualizar(id, form, principal.getName());
                redirect.addFlashAttribute("sucesso", "Usuário atualizado.");
                return "redirect:/usuarios";
            } catch (RegraDeNegocioException e) {
                result.reject("regra", e.getMessage());
            }
        }
        return "usuarios/form";
    }

    private void preencherForm(Model model, String titulo, String acao, boolean editando) {
        model.addAttribute("titulo", titulo);
        model.addAttribute("acao", acao);
        model.addAttribute("editando", editando);
        model.addAttribute("perfis", Perfil.values());
    }
}
