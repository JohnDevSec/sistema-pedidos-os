package com.pite.Pedidosos.controller;

import com.pite.Pedidosos.model.Cliente;
import com.pite.Pedidosos.service.ClienteService;
import com.pite.Pedidosos.service.RegraDeNegocioException;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/clientes")
public class ClienteController {

    private final ClienteService service;

    public ClienteController(ClienteService service) {
        this.service = service;
    }

    @GetMapping
    public String listar(@RequestParam(required = false) String busca,
                         @RequestParam(defaultValue = "0") int pagina, Model model) {
        model.addAttribute("pagina",
                service.listar(busca, PageRequest.of(Math.max(pagina, 0), 10, Sort.by("nome"))));
        model.addAttribute("busca", busca);
        return "clientes/lista";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("cliente", new Cliente());
        preencherForm(model, "Novo cliente", "/clientes");
        return "clientes/form";
    }

    @PostMapping
    public String criar(@Valid @ModelAttribute("cliente") Cliente cliente, BindingResult result,
                        RedirectAttributes redirect, Model model) {
        preencherForm(model, "Novo cliente", "/clientes");
        if (!result.hasErrors()) {
            try {
                service.criar(cliente);
                redirect.addFlashAttribute("sucesso", "Cliente cadastrado.");
                return "redirect:/clientes";
            } catch (RegraDeNegocioException e) {
                result.rejectValue("documento", "duplicado", e.getMessage());
            }
        }
        return "clientes/form";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("cliente", service.buscar(id));
        preencherForm(model, "Editar cliente", "/clientes/" + id);
        return "clientes/form";
    }

    @PostMapping("/{id}")
    public String atualizar(@PathVariable Long id, @Valid @ModelAttribute("cliente") Cliente cliente,
                            BindingResult result, RedirectAttributes redirect, Model model) {
        preencherForm(model, "Editar cliente", "/clientes/" + id);
        if (!result.hasErrors()) {
            try {
                service.atualizar(id, cliente);
                redirect.addFlashAttribute("sucesso", "Cliente atualizado.");
                return "redirect:/clientes";
            } catch (RegraDeNegocioException e) {
                result.rejectValue("documento", "duplicado", e.getMessage());
            }
        }
        return "clientes/form";
    }

    private void preencherForm(Model model, String titulo, String acao) {
        model.addAttribute("titulo", titulo);
        model.addAttribute("acao", acao);
    }
}
