package com.trackstore.service;

import com.trackstore.dao.HibernateUtil;
import com.trackstore.dao.ProdutoDAO;
import com.trackstore.model.Produto;
import com.trackstore.util.Mensagens;
import com.trackstore.util.ValidacaoException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/** Regras de negócio de produto: CRUD completo com validação. */
public class ProdutoService {

    private final ProdutoDAO produtoDAO = new ProdutoDAO();

    // ===================== CREATE =====================

    public Produto cadastrar(int codigo, String nome, int quantidade, BigDecimal preco)
            throws ValidacaoException {

        validar(nome, quantidade, preco);

        return HibernateUtil.emTransacao(session -> {
            if (produtoDAO.buscarPorCodigo(session, codigo) != null) {
                throw new ValidacaoException(Mensagens.CODIGO_DUPLICADO);
            }
            if (produtoDAO.buscarPorNomeExato(session, nome) != null) {
                throw new ValidacaoException(Mensagens.NOME_PRODUTO_DUPLICADO);
            }

            Produto produto = new Produto(codigo, nome.trim(), quantidade,
                    preco.setScale(2, RoundingMode.HALF_UP));
            produtoDAO.salvar(session, produto);
            return produto;
        });
    }

    // ===================== UPDATE =====================

    public Produto atualizar(int id, int codigo, String nome, int quantidade, BigDecimal preco)
            throws ValidacaoException {

        validar(nome, quantidade, preco);

        return HibernateUtil.emTransacao(session -> {
            Produto produto = produtoDAO.buscarPorId(session, id);
            if (produto == null) {
                throw new ValidacaoException(Mensagens.PRODUTO_NAO_ENCONTRADO);
            }

            // duplicidade só é problema se o registro encontrado for OUTRO produto
            Produto porCodigo = produtoDAO.buscarPorCodigo(session, codigo);
            if (porCodigo != null && porCodigo.getId() != id) {
                throw new ValidacaoException(Mensagens.CODIGO_DUPLICADO);
            }
            Produto porNome = produtoDAO.buscarPorNomeExato(session, nome);
            if (porNome != null && porNome.getId() != id) {
                throw new ValidacaoException(Mensagens.NOME_PRODUTO_DUPLICADO);
            }

            produto.setCodigo(codigo);
            produto.setNome(nome.trim());
            produto.setQuantidade(quantidade);
            produto.setPreco(preco.setScale(2, RoundingMode.HALF_UP));
            return produto;
        });
    }

    // ===================== DELETE =====================

    /**
     * Exclui o produto.
     *
     * Se ele já foi vendido ou reposto, a exclusão é LÓGICA (vira INATIVO): apagar
     * de verdade violaria a chave estrangeira de itens_pedido/reposicoes e destruiria
     * o histórico de vendas. Só um produto sem nenhum movimento é apagado de fato.
     *
     * @return true se apagou fisicamente, false se apenas inativou
     */
    public boolean excluir(int id) throws ValidacaoException {
        return HibernateUtil.emTransacao(session -> {
            Produto produto = produtoDAO.buscarPorId(session, id);
            if (produto == null) {
                throw new ValidacaoException(Mensagens.PRODUTO_NAO_ENCONTRADO);
            }

            boolean temHistorico = produtoDAO.contarVendas(session, id) > 0
                    || produtoDAO.contarReposicoes(session, id) > 0;

            if (temHistorico) {
                produto.setSituacao(Produto.Situacao.INATIVO);
                return false;
            }

            produtoDAO.remover(session, produto);
            return true;
        });
    }

    public void reativar(int id) throws ValidacaoException {
        HibernateUtil.emTransacao(session -> {
            Produto produto = produtoDAO.buscarPorId(session, id);
            if (produto == null) {
                throw new ValidacaoException(Mensagens.PRODUTO_NAO_ENCONTRADO);
            }
            produto.setSituacao(Produto.Situacao.ATIVO);
        });
    }

    // ===================== READ =====================

    public List<Produto> listarTodos() {
        return HibernateUtil.consultando(produtoDAO::listarTodos);
    }

    /** Usado pela loja: só produtos ativos e com saldo. */
    public List<Produto> listarDisponiveis(String trechoNome) {
        return HibernateUtil.consultando(s -> produtoDAO.listarDisponiveis(s, trechoNome));
    }

    public List<Produto> filtrar(String filtroCodigo, String filtroNome, boolean somenteAtivos) {
        return HibernateUtil.consultando(s -> produtoDAO.filtrar(s, filtroCodigo, filtroNome, somenteAtivos));
    }

    public Produto buscarPorId(int id) {
        return HibernateUtil.consultando(s -> produtoDAO.buscarPorId(s, id));
    }

    public Produto buscarPorCodigo(int codigo) {
        return HibernateUtil.consultando(s -> produtoDAO.buscarPorCodigo(s, codigo));
    }

    // ===================== validação =====================

    private void validar(String nome, int quantidade, BigDecimal preco) throws ValidacaoException {
        if (nome == null || nome.isBlank()) {
            throw new ValidacaoException(Mensagens.CAMPOS_OBRIGATORIOS);
        }
        // quantidade zero é válida (produto esgotado); negativa não é
        if (quantidade < 0) {
            throw new ValidacaoException(Mensagens.QUANTIDADE_POSITIVA);
        }
        if (preco == null || preco.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidacaoException(Mensagens.PRECO_POSITIVO);
        }
    }
}
