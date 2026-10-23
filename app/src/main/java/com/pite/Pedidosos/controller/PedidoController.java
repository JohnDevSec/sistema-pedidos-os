package com.pite.Pedidosos.controller;

import com.pite.Pedidosos.dto.PedidoForm;
import com.pite.Pedidosos.model.ItemPedido;
import com.pite.Pedidosos.model.OrdemServico;
import com.pite.Pedidosos.model.Pedido;
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
        String filtroVendedor = tem(auth, "VENDEDOR") ? auth.getName() : null;
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
                pedidos.criarComoVendedor(principal.getName(), form.getClienteId(),
                        form.getObservacao(), paraItens(form));
                redirect.addFlashAttribute("sucesso", "Pedido criado.");
                return "redirect:/pedidos";
            } catch (RegraDeNegocioException e) {
                result.reject("regra", e.getMessage());
            }
        }
        carregarClientes(model);
        return "pedidos/form";
    }

    @GetMapping("/{id}")
    public String detalhe(@PathVariable Long id, Authentication auth, Model model) {
        Pedido pedido = pedidos.buscarVisivel(id, auth.getName(), tem(auth, "VENDEDOR"));
        model.addAttribute("pedidoForm", paraForm(pedido));
        preencherDetalhe(model, pedido, auth);
        return "pedidos/detalhe";
    }

    @PostMapping("/{id}/editar")
    public String editar(@PathVariable Long id, @Valid @ModelAttribute("pedidoForm") PedidoForm form,
                         BindingResult result, Authentication auth,
                         RedirectAttributes redirect, Model model) {
        if (!result.hasErrors()) {
            try {
                pedidos.editarComoAnalista(auth.getName(), id, form.getObservacao(), paraItens(form));
                redirect.addFlashAttribute("sucesso", "Pedido atualizado.");
                return "redirect:/pedidos/" + id;
            } catch (RegraDeNegocioException e) {
                result.reject("regra", e.getMessage());
            }
        }
        preencherDetalhe(model, pedidos.buscarVisivel(id, auth.getName(), tem(auth, "VENDEDOR")), auth);
        return "pedidos/detalhe";
    }

    @PostMapping("/{id}/aprovar")
    public String aprovar(@PathVariable Long id, Authentication auth, RedirectAttributes redirect) {
        try {
            OrdemServico os = pedidos.aprovarComoAnalista(auth.getName(), id);
            redirect.addFlashAttribute("sucesso", "Pedido aprovado. Ordem de serviço #" + os.getId() + " gerada.");
            return "redirect:/pedidos";
        } catch (RegraDeNegocioException e) {
            redirect.addFlashAttribute("erro", e.getMessage());
            return "redirect:/pedidos/" + id;
        }
    }

    @PostMapping("/{id}/cancelar")
    public String cancelar(@PathVariable Long id, Authentication auth, RedirectAttributes redirect) {
        try {
            pedidos.cancelarComo(auth.getName(), id);
            redirect.addFlashAttribute("sucesso", "Pedido cancelado.");
            return "redirect:/pedidos";
        } catch (RegraDeNegocioException e) {
            redirect.addFlashAttribute("erro", e.getMessage());
            return "redirect:/pedidos/" + id;
        }
    }

    private void preencherDetalhe(Model model, Pedido pedido, Authentication auth) {
        boolean editavel = pedido.getStatus() == StatusPedido.CRIADO
                || pedido.getStatus() == StatusPedido.EM_REVISAO;
        boolean donoVendedor = tem(auth, "VENDEDOR")
                && pedido.getVendedor().getEmail().equalsIgnoreCase(auth.getName());
        model.addAttribute("pedidoAtual", pedido);
        model.addAttribute("historico", pedidos.historicoDe(pedido.getId()));
        model.addAttribute("podeEditar", editavel && tem(auth, "ANALISTA"));
        model.addAttribute("podeCancelar",
                editavel && (tem(auth, "ANALISTA") || tem(auth, "ADMIN") || donoVendedor));
        model.addAttribute("podeAprovar", pedido.getStatus() == StatusPedido.EM_REVISAO && tem(auth, "ANALISTA"));
    }

    private boolean tem(Authentication auth, String perfil) {
        return auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_" + perfil));
    }

    private PedidoForm paraForm(Pedido pedido) {
        PedidoForm form = new PedidoForm();
        form.setClienteId(pedido.getCliente().getId());
        form.setObservacao(pedido.getObservacao());
        pedido.getItens().forEach(item -> {
            PedidoForm.ItemForm itemForm = new PedidoForm.ItemForm();
            itemForm.setDescricao(item.getDescricao());
            itemForm.setQuantidade(item.getQuantidade());
            itemForm.setValorUnitario(item.getValorUnitario());
            form.getItens().add(itemForm);
        });
        return form;
    }

    private List<ItemPedido> paraItens(PedidoForm form) {
        return form.getItens().stream().map(f -> {
            ItemPedido item = new ItemPedido();
            item.setDescricao(f.getDescricao().trim());
            item.setQuantidade(f.getQuantidade());
            item.setValorUnitario(f.getValorUnitario());
            return item;
        }).toList();
    }

    private void carregarClientes(Model model) {
        model.addAttribute("clientes",
                clientes.listar(null, PageRequest.of(0, 500, Sort.by("nome"))).getContent());
    }
}
