package com.trackstore.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "reposicoes")
public class Reposicao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "produto_id", nullable = false)
    private Produto produto;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "fornecedor_id", nullable = false)
    private Fornecedor fornecedor;

    @Column(nullable = false)
    private int quantidade;

    @Column(name = "data_reposicao", nullable = false)
    private LocalDateTime dataReposicao = LocalDateTime.now();

    public Reposicao() {
    }

    public Reposicao(Produto produto, Fornecedor fornecedor, int quantidade) {
        this.produto = produto;
        this.fornecedor = fornecedor;
        this.quantidade = quantidade;
    }

    public int getId() { return id; }
    public Produto getProduto() { return produto; }
    public Fornecedor getFornecedor() { return fornecedor; }
    public int getQuantidade() { return quantidade; }
    public LocalDateTime getDataReposicao() { return dataReposicao; }
}
