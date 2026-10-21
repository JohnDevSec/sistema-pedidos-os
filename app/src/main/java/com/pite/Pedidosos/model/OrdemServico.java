package com.pite.Pedidosos.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ordem_servico")
public class OrdemServico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pedido_id", unique = true)
    private Pedido pedido;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "analista_id")
    private Usuario analista;

    @Column(name = "data_geracao", nullable = false, updatable = false)
    private LocalDateTime dataGeracao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusOS status = StatusOS.ABERTA;

    @Column(name = "data_conclusao")
    private LocalDateTime dataConclusao;

    @PrePersist
    void aoCriar() {
        this.dataGeracao = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public Pedido getPedido() { return pedido; }
    public void setPedido(Pedido pedido) { this.pedido = pedido; }
    public Usuario getAnalista() { return analista; }
    public void setAnalista(Usuario analista) { this.analista = analista; }
    public LocalDateTime getDataGeracao() { return dataGeracao; }
    public StatusOS getStatus() { return status; }
    public void setStatus(StatusOS status) { this.status = status; }
    public LocalDateTime getDataConclusao() { return dataConclusao; }
    public void setDataConclusao(LocalDateTime dataConclusao) { this.dataConclusao = dataConclusao; }
}
