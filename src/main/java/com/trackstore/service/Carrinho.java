package com.trackstore.service;

import com.trackstore.model.Produto;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Carrinho de compras da tela da loja. Não é entidade: vive só em memória,
 * enquanto o usuário monta a compra.
 *
 * Antes a própria entidade Pedido fazia esse papel, o que misturava estado de
 * tela com estado de banco e obrigava a carregar objetos destacados da sessão.
 */
public class Carrinho {

    /** Uma linha do carrinho. Guarda um retrato do produto no momento da adição. */
    public static class Item {
        private final int produtoId;
        private final int codigo;
        private final String nome;
        private final BigDecimal precoUnitario;
        private int quantidade;

        Item(Produto produto, int quantidade) {
            this.produtoId = produto.getId();
            this.codigo = produto.getCodigo();
            this.nome = produto.getNome();
            this.precoUnitario = produto.getPreco();
            this.quantidade = quantidade;
        }

        public int getProdutoId() { return produtoId; }
        public int getCodigo() { return codigo; }
        public String getNome() { return nome; }
        public BigDecimal getPrecoUnitario() { return precoUnitario; }
        public int getQuantidade() { return quantidade; }

        public BigDecimal getTotal() {
            return precoUnitario.multiply(BigDecimal.valueOf(quantidade));
        }
    }

    private final List<Item> itens = new ArrayList<>();

    /**
     * Adiciona ao carrinho. Se o produto já estiver lá, SOMA na linha existente.
     *
     * Sem isso, adicionar o mesmo produto duas vezes criava duas linhas que não se
     * enxergavam: com estoque 10 dava para inserir 6 + 6 = 12 e as duas validações
     * passavam isoladamente.
     */
    public void adicionar(Produto produto, int quantidade) {
        for (Item item : itens) {
            if (item.produtoId == produto.getId()) {
                item.quantidade += quantidade;
                return;
            }
        }
        itens.add(new Item(produto, quantidade));
    }

    public void remover(int indice) {
        if (indice >= 0 && indice < itens.size()) {
            itens.remove(indice);
        }
    }

    public void limpar() {
        itens.clear();
    }

    /** Quantas unidades deste produto já estão no carrinho. */
    public int quantidadeDe(int produtoId) {
        int soma = 0;
        for (Item item : itens) {
            if (item.produtoId == produtoId) {
                soma += item.quantidade;
            }
        }
        return soma;
    }

    public BigDecimal getTotal() {
        BigDecimal soma = BigDecimal.ZERO;
        for (Item item : itens) {
            soma = soma.add(item.getTotal());
        }
        return soma.setScale(2, RoundingMode.HALF_UP);
    }

    public List<Item> getItens() {
        return Collections.unmodifiableList(itens);
    }

    public boolean isVazio() {
        return itens.isEmpty();
    }

    public int getQuantidadeItens() {
        return itens.size();
    }
}
