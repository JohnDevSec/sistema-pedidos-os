package com.pite.Pedidosos.controller;

import com.pite.Pedidosos.service.RegraDeNegocioException;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RegraDeNegocioException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String regraDeNegocio(RegraDeNegocioException e, Model model) {
        model.addAttribute("mensagem", e.getMessage());
        return "erro";
    }
}
