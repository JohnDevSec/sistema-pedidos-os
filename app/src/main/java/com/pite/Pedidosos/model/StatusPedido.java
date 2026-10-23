package com.pite.Pedidosos.model;

public enum StatusPedido {
    CRIADO("Criado"),
    EM_REVISAO("Em revisão"),
    APROVADO("Aprovado"),
    CANCELADO("Cancelado");

    private final String rotulo;

    StatusPedido(String rotulo) {
        this.rotulo = rotulo;
    }

    public String getRotulo() {
        return rotulo;
    }
}
