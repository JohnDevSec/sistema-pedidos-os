package com.pite.Pedidosos.controller;

import com.pite.Pedidosos.model.OrdemServico;
import com.pite.Pedidosos.model.StatusOS;
import com.pite.Pedidosos.service.OrdemServicoService;
import com.pite.Pedidosos.service.RegraDeNegocioException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/ordens")
public class OrdemServicoController {

    private final OrdemServicoService service;

    public OrdemServicoController(OrdemServicoService service) {
        this.service = service;
    }

    @GetMapping
    public String listar(@RequestParam(required = false) StatusOS status,
                         @RequestParam(defaultValue = "0") int pagina,
                         Authentication auth, Model model) {
        String filtroVendedor = tem(auth, "VENDEDOR") ? auth.getName() : null;
        model.addAttribute("pagina", service.listar(status, filtroVendedor,
                PageRequest.of(Math.max(pagina, 0), 10, Sort.by(Sort.Direction.DESC, "dataGeracao"))));
        model.addAttribute("status", status);
        model.addAttribute("listaStatus", StatusOS.values());
        model.addAttribute("podeAvancar", tem(auth, "ANALISTA"));
        return "ordens/lista";
    }

    @PostMapping("/{id}/avancar")
    public String avancar(@PathVariable Long id, Authentication auth, RedirectAttributes redirect) {
        try {
            OrdemServico os = service.avancar(id, auth.getName());
            redirect.addFlashAttribute("sucesso",
                    "Ordem de serviço #" + id + " agora está: " + os.getStatus().getRotulo() + ".");
        } catch (RegraDeNegocioException e) {
            redirect.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/ordens";
    }

    private boolean tem(Authentication auth, String perfil) {
        return auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_" + perfil));
    }
}
