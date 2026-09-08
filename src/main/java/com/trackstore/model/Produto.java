package com.trackstore.model;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;

@Entity
@Table(name = "produtos")
public class Produto {

    /**
     * Exclusão de produto é LÓGICA, não física: um produto já vendido está
     * referenciado por itens_pedido e reposicoes, e apagá-lo de verdade violaria a
     * chave estrangeira além de destruir o histórico de vendas.
     * INATIVO some da loja e das listagens padrão, mas os pedidos antigos continuam
     * íntegros.
     */
    public enum Situacao { ATIVO, INATIVO }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false, unique = true)
    private int codigo;

    @Column(nullable = false, unique = true, length = 150)
    private String nome;

    /** Unidades em estoque. int, não double: não se vende meia unidade. */
    @Column(nullable = false)
    private int quantidade;

    /**
     * BigDecimal, não double: double não representa 0,10 exatamente, e somar três
     * itens de R$ 0,10 daria 0.30000000000000004. Dinheiro nunca em ponto flutuante.
     */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal preco;

    /**
     * @JdbcTypeCode(VARCHAR): sem isso o Hibernate 6 mapeia enum textual no MySQL
     * para uma coluna ENUM('ATIVO','INATIVO'), tipo não padrão e trabalhoso de
     * alterar depois. Com VARCHAR o script.sql vale para MySQL e para o H2 dos testes.
     */
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 10)
    private Situacao situacao = Situacao.ATIVO;

    public Produto() {
    }

    public Produto(int codigo, String nome, int quantidade, BigDecimal preco) {
        this.codigo = codigo;
        this.nome = nome;
        this.quantidade = quantidade;
        this.preco = preco;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getCodigo() { return codigo; }
    public void setCodigo(int codigo) { this.codigo = codigo; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public int getQuantidade() { return quantidade; }
    public void setQuantidade(int quantidade) { this.quantidade = quantidade; }

    public BigDecimal getPreco() { return preco; }
    public void setPreco(BigDecimal preco) { this.preco = preco; }

    public Situacao getSituacao() { return situacao; }
    public void setSituacao(Situacao situacao) { this.situacao = situacao; }

    public boolean isAtivo() {
        return situacao == Situacao.ATIVO;
    }

    @Override
    public String toString() {
        return codigo + " - " + nome;
    }
}
