package com.trackstore.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Pedido gravado no banco.
 *
 * Esta entidade NÃO é mais usada como carrinho de compras da tela: o carrinho é
 * a classe Carrinho, no pacote service. O Pedido só é construído dentro da
 * transação de finalização, já com entidades gerenciadas pelo Hibernate.
 */
@Entity
@Table(name = "pedidos")
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal total = BigDecimal.ZERO;

    @Column(name = "data_pedido", nullable = false)
    private LocalDateTime dataPedido = LocalDateTime.now();

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemPedido> itens = new ArrayList<>();

    public Pedido() {
    }

    public Pedido(Usuario usuario) {
        this.usuario = usuario;
    }

    public void adicionarItem(Produto produto, int quantidade) {
        ItemPedido item = new ItemPedido(produto, quantidade);
        item.setPedido(this);
        itens.add(item);
        recalcularTotal();
    }

    private void recalcularTotal() {
        BigDecimal soma = BigDecimal.ZERO;
        for (ItemPedido item : itens) {
            soma = soma.add(item.getTotal());
        }
        this.total = soma.setScale(2, RoundingMode.HALF_UP);
    }

    public int getId() { return id; }
    public Usuario getUsuario() { return usuario; }
    public BigDecimal getTotal() { return total; }
    public LocalDateTime getDataPedido() { return dataPedido; }

    public List<ItemPedido> getItens() {
        return Collections.unmodifiableList(itens);
    }
}
