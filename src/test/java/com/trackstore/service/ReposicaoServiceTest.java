package com.trackstore.service;

import com.trackstore.BaseTesteBanco;
import com.trackstore.model.Fornecedor;
import com.trackstore.model.Produto;
import com.trackstore.util.Mensagens;
import com.trackstore.util.ValidacaoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ReposicaoService - reposição de estoque e histórico")
class ReposicaoServiceTest extends BaseTesteBanco {

    private final ReposicaoService reposicaoService = new ReposicaoService();
    private final ProdutoService produtoService = new ProdutoService();
    private final FornecedorService fornecedorService = new FornecedorService();

    private Produto produto;
    private Fornecedor fornecedor;

    @BeforeEach
    void prepararDados() throws ValidacaoException {
        produto = produtoService.cadastrar(3001, "Caneta", 20, new BigDecimal("3.00"));
        fornecedor = fornecedorService.cadastrar("Fornecedor Central");
    }

    @Test
    @DisplayName("reposição soma ao saldo e registra no histórico")
    void reporSomaERegistra() throws ValidacaoException {
        reposicaoService.repor(produto.getId(), fornecedor.getId(), 50);

        assertEquals(70, produtoService.buscarPorId(produto.getId()).getQuantidade());

        var historico = reposicaoService.listarPorProduto(produto.getId());
        assertEquals(1, historico.size());
        assertEquals(50, historico.get(0).getQuantidade());
        assertEquals("Fornecedor Central", historico.get(0).getFornecedor().getNome());
        assertNotNull(historico.get(0).getDataReposicao());
    }

    @Test
    @DisplayName("reposição com quantidade negativa é recusada")
    void recusaQuantidadeNegativa() {
        // Na versão antiga isto SUBTRAÍA 50 do estoque e ainda gravava a operação
        // no histórico como se fosse uma entrada de mercadoria.
        ValidacaoException erro = assertThrows(ValidacaoException.class,
                () -> reposicaoService.repor(produto.getId(), fornecedor.getId(), -50));

        assertEquals(Mensagens.QUANTIDADE_POSITIVA, erro.getMessage());
        assertEquals(20, produtoService.buscarPorId(produto.getId()).getQuantidade());
        assertTrue(reposicaoService.listarTodas().isEmpty());
    }

    @Test
    @DisplayName("reposição com quantidade zero é recusada")
    void recusaQuantidadeZero() {
        assertThrows(ValidacaoException.class,
                () -> reposicaoService.repor(produto.getId(), fornecedor.getId(), 0));
        assertEquals(20, produtoService.buscarPorId(produto.getId()).getQuantidade());
    }

    @Test
    @DisplayName("recusa produto ou fornecedor inexistente sem alterar nada")
    void recusaReferenciasInvalidas() {
        assertThrows(ValidacaoException.class,
                () -> reposicaoService.repor(99999, fornecedor.getId(), 10));
        assertThrows(ValidacaoException.class,
                () -> reposicaoService.repor(produto.getId(), 99999, 10));

        assertEquals(20, produtoService.buscarPorId(produto.getId()).getQuantidade());
        assertTrue(reposicaoService.listarTodas().isEmpty());
    }

    @Test
    @DisplayName("fornecedor com reposições não pode ser excluído")
    void naoExcluiFornecedorComHistorico() throws ValidacaoException {
        reposicaoService.repor(produto.getId(), fornecedor.getId(), 10);

        ValidacaoException erro = assertThrows(ValidacaoException.class,
                () -> fornecedorService.excluir(fornecedor.getId()));

        assertEquals(Mensagens.FORNECEDOR_COM_REPOSICOES, erro.getMessage());
    }

    @Test
    @DisplayName("fornecedor sem histórico pode ser excluído")
    void excluiFornecedorSemHistorico() throws ValidacaoException {
        Fornecedor novo = fornecedorService.cadastrar("Fornecedor Sem Uso");

        fornecedorService.excluir(novo.getId());

        assertEquals(1, fornecedorService.listarTodos().size());
    }

    @Test
    @DisplayName("recusa fornecedor com nome duplicado")
    void recusaFornecedorDuplicado() {
        assertThrows(ValidacaoException.class,
                () -> fornecedorService.cadastrar("Fornecedor Central"));
    }
}
