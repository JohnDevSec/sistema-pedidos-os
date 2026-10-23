package com.pite.Pedidosos.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.security.Principal;

@ControllerAdvice
public class GlobalModelAdvice {

    @ModelAttribute("emailLogado")
    public String emailLogado(Principal principal) {
        return principal == null ? null : principal.getName();
    }

    @ModelAttribute("ehVendedor")
    public boolean ehVendedor(Authentication authentication) {
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_VENDEDOR"));
    }

    @ModelAttribute("ehAdmin")
    public boolean ehAdmin(Authentication authentication) {
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
}
