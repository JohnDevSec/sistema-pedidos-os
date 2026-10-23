package com.pite.Pedidosos.controller;

import com.pite.Pedidosos.dto.PedidoForm;
import com.pite.Pedidosos.model.ItemPedido;
import com.pite.Pedidosos.model.StatusPedido;
import com.pite.Pedidosos.service.ClienteService;
import com.pite.Pedidosos.service.PedidoService;
import com.pite.Pedidosos.service.RegraDeNegocioException;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/pedidos")
public class PedidoController {

    private final PedidoService pedidos;
    private final ClienteService clientes;

    public PedidoController(PedidoService pedidos, ClienteService clientes) {
        this.pedidos = pedidos;
        this.clientes = clientes;
    }

    @GetMapping
    public String listar(@RequestParam(required = false) StatusPedido status,
                         @RequestParam(defaultValue = "0") int pagina,
                         Authentication auth, Model model) {
        boolean vendedor = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_VENDEDOR"));
        String filtroVendedor = vendedor ? auth.getName() : null;
        model.addAttribute("pagina", pedidos.listar(status, filtroVendedor,
                PageRequest.of(Math.max(pagina, 0), 10, Sort.by(Sort.Direction.DESC, "dataCriacao"))));
        model.addAttribute("status", status);
        model.addAttribute("listaStatus", StatusPedido.values());
        return "pedidos/lista";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        PedidoForm form = new PedidoForm();
        form.getItens().add(new PedidoForm.ItemForm());
        model.addAttribute("pedido", form);
        carregarClientes(model);
        return "pedidos/form";
    }

    @PostMapping
    public String criar(@Valid @ModelAttribute("pedido") PedidoForm form, BindingResult result,
                        Principal principal, RedirectAttributes redirect, Model model) {
        if (!result.hasErrors()) {
            try {
                List<ItemPedido> itens = form.getItens().stream().map(this::paraItem).toList();
                pedidos.criarComoVendedor(principal.getName(), form.getClienteId(), form.getObservacao(), itens);
                redirect.addFlashAttribute("sucesso", "Pedido criado.");
                return "redirect:/pedidos";
            } catch (RegraDeNegocioException e) {
                result.reject("regra", e.getMessage());
            }
        }
        carregarClientes(model);
        return "pedidos/form";
    }

    private ItemPedido paraItem(PedidoForm.ItemForm form) {
        ItemPedido item = new ItemPedido();
        item.setDescricao(form.getDescricao().trim());
        item.setQuantidade(form.getQuantidade());
        item.setValorUnitario(form.getValorUnitario());
        return item;
    }

    private void carregarClientes(Model model) {
        model.addAttribute("clientes",
                clientes.listar(null, PageRequest.of(0, 500, Sort.by("nome"))).getContent());
    }
}
