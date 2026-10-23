package com.pite.Pedidosos.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class PedidoForm {

    @NotNull(message = "Selecione um cliente.")
    private Long clienteId;

    @Size(max = 2000)
    private String observacao;

    @Valid
    @NotEmpty(message = "O pedido precisa ter pelo menos um item.")
    private List<ItemForm> itens = new ArrayList<>();

    public Long getClienteId() { return clienteId; }
    public void setClienteId(Long clienteId) { this.clienteId = clienteId; }
    public String getObservacao() { return observacao; }
    public void setObservacao(String observacao) { this.observacao = observacao; }
    public List<ItemForm> getItens() { return itens; }
    public void setItens(List<ItemForm> itens) { this.itens = itens; }

    public static class ItemForm {

        @NotBlank @Size(max = 200)
        private String descricao;

        @Min(1)
        private int quantidade = 1;

        @NotNull @DecimalMin("0.00")
        private BigDecimal valorUnitario;

        public String getDescricao() { return descricao; }
        public void setDescricao(String descricao) { this.descricao = descricao; }
        public int getQuantidade() { return quantidade; }
        public void setQuantidade(int quantidade) { this.quantidade = quantidade; }
        public BigDecimal getValorUnitario() { return valorUnitario; }
        public void setValorUnitario(BigDecimal valorUnitario) { this.valorUnitario = valorUnitario; }
    }
}
