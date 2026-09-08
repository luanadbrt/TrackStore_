package com.trackstore.service;

import com.trackstore.BaseTesteBanco;
import com.trackstore.model.Produto;
import com.trackstore.model.Usuario;
import com.trackstore.util.Mensagens;
import com.trackstore.util.ValidacaoException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ProdutoService - CRUD completo de produto")
class ProdutoServiceTest extends BaseTesteBanco {

    private final ProdutoService produtoService = new ProdutoService();

    @Test
    @DisplayName("cadastra e recupera pelo código")
    void cadastra() throws ValidacaoException {
        produtoService.cadastrar(2001, "Caderno", 30, new BigDecimal("12.50"));

        Produto salvo = produtoService.buscarPorCodigo(2001);
        assertNotNull(salvo);
        assertEquals("Caderno", salvo.getNome());
        assertEquals(30, salvo.getQuantidade());
        assertTrue(salvo.isAtivo());
    }

    @Test
    @DisplayName("recusa código e nome duplicados")
    void recusaDuplicados() throws ValidacaoException {
        produtoService.cadastrar(2001, "Caderno", 30, new BigDecimal("12.50"));

        ValidacaoException porCodigo = assertThrows(ValidacaoException.class,
                () -> produtoService.cadastrar(2001, "Outro Nome", 5, new BigDecimal("1.00")));
        assertEquals(Mensagens.CODIGO_DUPLICADO, porCodigo.getMessage());

        ValidacaoException porNome = assertThrows(ValidacaoException.class,
                () -> produtoService.cadastrar(2002, "Caderno", 5, new BigDecimal("1.00")));
        assertEquals(Mensagens.NOME_PRODUTO_DUPLICADO, porNome.getMessage());
    }

    @Test
    @DisplayName("recusa preço zero ou negativo e quantidade negativa")
    void recusaValoresInvalidos() {
        assertThrows(ValidacaoException.class,
                () -> produtoService.cadastrar(2003, "Grátis", 10, BigDecimal.ZERO));
        assertThrows(ValidacaoException.class,
                () -> produtoService.cadastrar(2004, "Negativo", 10, new BigDecimal("-5.00")));
        assertThrows(ValidacaoException.class,
                () -> produtoService.cadastrar(2005, "Qtd ruim", -1, new BigDecimal("10.00")));
    }

    @Test
    @DisplayName("atualiza os dados do produto")
    void atualiza() throws ValidacaoException {
        Produto produto = produtoService.cadastrar(2001, "Caderno", 30, new BigDecimal("12.50"));

        produtoService.atualizar(produto.getId(), 2001, "Caderno Grande", 45,
                new BigDecimal("15.00"));

        Produto atualizado = produtoService.buscarPorId(produto.getId());
        assertEquals("Caderno Grande", atualizado.getNome());
        assertEquals(45, atualizado.getQuantidade());
        assertEquals(0, new BigDecimal("15.00").compareTo(atualizado.getPreco()));
    }

    @Test
    @DisplayName("ao editar, manter o próprio código não conta como duplicidade")
    void editarMantendoProprioCodigo() throws ValidacaoException {
        Produto produto = produtoService.cadastrar(2001, "Caderno", 30, new BigDecimal("12.50"));

        assertDoesNotThrow(() -> produtoService.atualizar(produto.getId(), 2001, "Caderno", 31,
                new BigDecimal("12.50")));
    }

    @Test
    @DisplayName("produto sem movimento é apagado de verdade")
    void excluiFisicamenteSemHistorico() throws ValidacaoException {
        Produto produto = produtoService.cadastrar(2001, "Caderno", 30, new BigDecimal("12.50"));

        assertTrue(produtoService.excluir(produto.getId()));
        assertNull(produtoService.buscarPorId(produto.getId()));
    }

    @Test
    @DisplayName("produto já vendido é apenas inativado, preservando o histórico")
    void inativaQuandoTemHistorico() throws ValidacaoException {
        UsuarioService usuarioService = new UsuarioService();
        Usuario cliente = usuarioService.cadastrar("Cliente", "c@teste.com",
                "52998224725", "senha123");
        Produto produto = produtoService.cadastrar(2001, "Caderno", 30, new BigDecimal("12.50"));

        Carrinho carrinho = new Carrinho();
        carrinho.adicionar(produto, 1);
        new PedidoService().finalizar(cliente, carrinho);

        assertFalse(produtoService.excluir(produto.getId()), "não deveria apagar fisicamente");

        Produto depois = produtoService.buscarPorId(produto.getId());
        assertNotNull(depois, "o registro precisa continuar existindo");
        assertEquals(Produto.Situacao.INATIVO, depois.getSituacao());
    }

    @Test
    @DisplayName("produto inativo pode ser reativado")
    void reativa() throws ValidacaoException {
        Produto produto = produtoService.cadastrar(2001, "Caderno", 30, new BigDecimal("12.50"));
        produtoService.atualizar(produto.getId(), 2001, "Caderno", 30, new BigDecimal("12.50"));

        produtoService.excluir(produto.getId());
        // já foi apagado fisicamente (sem histórico), então recadastra para o teste
        Produto novo = produtoService.cadastrar(2002, "Caderno B", 5, new BigDecimal("9.90"));
        produtoService.reativar(novo.getId());

        assertTrue(produtoService.buscarPorId(novo.getId()).isAtivo());
    }

    @Test
    @DisplayName("a loja só oferece produtos ativos e com saldo")
    void listaApenasDisponiveis() throws ValidacaoException {
        produtoService.cadastrar(2001, "Com saldo", 10, new BigDecimal("5.00"));
        produtoService.cadastrar(2002, "Esgotado", 0, new BigDecimal("5.00"));

        var disponiveis = produtoService.listarDisponiveis(null);

        assertEquals(1, disponiveis.size());
        assertEquals("Com saldo", disponiveis.get(0).getNome());
    }

    @Test
    @DisplayName("o filtro por código é aplicado no banco")
    void filtraPorCodigo() throws ValidacaoException {
        produtoService.cadastrar(1001, "Produto A", 10, new BigDecimal("5.00"));
        produtoService.cadastrar(1002, "Produto B", 10, new BigDecimal("5.00"));
        produtoService.cadastrar(2001, "Produto C", 10, new BigDecimal("5.00"));

        assertEquals(2, produtoService.filtrar("100", null, true).size());
        assertEquals(1, produtoService.filtrar(null, "Produto C", true).size());
    }
}
