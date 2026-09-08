package com.trackstore.service;

import com.trackstore.BaseTesteBanco;
import com.trackstore.model.Produto;
import com.trackstore.model.Usuario;
import com.trackstore.util.Mensagens;
import com.trackstore.util.ValidacaoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes da regra mais crítica do sistema: a venda nunca pode deixar o estoque
 * negativo, e uma venda recusada não pode alterar nada.
 */
@DisplayName("PedidoService - finalização de compra e baixa de estoque")
class PedidoServiceTest extends BaseTesteBanco {

    private final PedidoService pedidoService = new PedidoService();
    private final ProdutoService produtoService = new ProdutoService();
    private final UsuarioService usuarioService = new UsuarioService();

    private Usuario cliente;
    private Produto produto;

    @BeforeEach
    void prepararDados() throws ValidacaoException {
        cliente = usuarioService.cadastrar("Cliente Teste", "cliente@teste.com",
                "52998224725", "senha123");
        produto = produtoService.cadastrar(1001, "Bíblia de Estudo", 10, new BigDecimal("89.90"));
    }

    @Test
    @DisplayName("venda válida dá baixa exata no estoque")
    void vendaValidaBaixaEstoque() throws ValidacaoException {
        Carrinho carrinho = new Carrinho();
        carrinho.adicionar(produto, 3);

        pedidoService.finalizar(cliente, carrinho);

        assertEquals(7, produtoService.buscarPorId(produto.getId()).getQuantidade());
    }

    @Test
    @DisplayName("venda válida grava o total correto, sem erro de arredondamento")
    void totalDoPedidoEstaCorreto() throws ValidacaoException {
        Carrinho carrinho = new Carrinho();
        carrinho.adicionar(produto, 3);

        var pedido = pedidoService.finalizar(cliente, carrinho);

        // 3 x 89,90 = 269,70 exatos (com double daria 269.70000000000005)
        assertEquals(0, new BigDecimal("269.70").compareTo(pedido.getTotal()));
    }

    @Test
    @DisplayName("comprar mais do que existe é recusado com a mensagem esperada")
    void naoPermiteVendaAcimaDoEstoque() {
        Carrinho carrinho = new Carrinho();
        carrinho.adicionar(produto, 11); // estoque é 10

        ValidacaoException erro = assertThrows(ValidacaoException.class,
                () -> pedidoService.finalizar(cliente, carrinho));

        assertTrue(erro.getMessage().contains(Mensagens.ESTOQUE_INSUFICIENTE));
    }

    @Test
    @DisplayName("venda recusada NÃO altera o estoque (rollback)")
    void vendaRecusadaNaoAlteraEstoque() {
        Carrinho carrinho = new Carrinho();
        carrinho.adicionar(produto, 500);

        assertThrows(ValidacaoException.class, () -> pedidoService.finalizar(cliente, carrinho));

        // a versão antiga gravava -490 aqui
        assertEquals(10, produtoService.buscarPorId(produto.getId()).getQuantidade());
    }

    @Test
    @DisplayName("um item inválido cancela o pedido inteiro, sem baixa parcial")
    void falhaEmUmItemDesfazOsAnteriores() throws ValidacaoException {
        Produto outro = produtoService.cadastrar(1002, "Caneca", 2, new BigDecimal("29.90"));

        Carrinho carrinho = new Carrinho();
        carrinho.adicionar(produto, 5);  // cabe no estoque
        carrinho.adicionar(outro, 99);   // não cabe

        assertThrows(ValidacaoException.class, () -> pedidoService.finalizar(cliente, carrinho));

        assertEquals(10, produtoService.buscarPorId(produto.getId()).getQuantidade());
        assertEquals(2, produtoService.buscarPorId(outro.getId()).getQuantidade());
        assertTrue(pedidoService.listarPorUsuario(cliente.getId()).isEmpty());
    }

    @Test
    @DisplayName("vender exatamente o saldo é permitido e zera o estoque")
    void permiteVenderTodoOSaldo() throws ValidacaoException {
        Carrinho carrinho = new Carrinho();
        carrinho.adicionar(produto, 10);

        pedidoService.finalizar(cliente, carrinho);

        assertEquals(0, produtoService.buscarPorId(produto.getId()).getQuantidade());
    }

    @Test
    @DisplayName("o carrinho soma o mesmo produto adicionado duas vezes")
    void carrinhoAgrupaProdutoRepetido() {
        Carrinho carrinho = new Carrinho();
        carrinho.adicionar(produto, 6);
        carrinho.adicionar(produto, 6);

        // uma linha só, com 12 unidades: é o que impede burlar a validação
        // adicionando 6 + 6 de um produto com 10 em estoque
        assertEquals(1, carrinho.getQuantidadeItens());
        assertEquals(12, carrinho.quantidadeDe(produto.getId()));

        assertThrows(ValidacaoException.class, () -> pedidoService.finalizar(cliente, carrinho));
    }

    @Test
    @DisplayName("carrinho vazio é recusado")
    void recusaCarrinhoVazio() {
        ValidacaoException erro = assertThrows(ValidacaoException.class,
                () -> pedidoService.finalizar(cliente, new Carrinho()));

        assertEquals(Mensagens.CARRINHO_VAZIO, erro.getMessage());
    }

    @Test
    @DisplayName("produto inativado não pode ser vendido")
    void naoVendeProdutoInativo() throws ValidacaoException {
        Carrinho carrinho = new Carrinho();
        carrinho.adicionar(produto, 1);

        produtoService.excluir(produto.getId()); // sem histórico ainda -> apaga de vez
        assertThrows(ValidacaoException.class, () -> pedidoService.finalizar(cliente, carrinho));
    }
}
