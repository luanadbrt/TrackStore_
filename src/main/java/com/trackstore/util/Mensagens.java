package com.trackstore.util;

/**
 * Todas as mensagens exibidas ao usuário, num lugar só.
 *
 * Antes elas estavam espalhadas e em três estilos diferentes ao mesmo tempo
 * ("não permitido campos vazios", "SELECIONE um produto", "Codigo já cadastrado"),
 * e algumas apareciam duplicadas na tela e no DAO — mudar uma deixava a outra
 * dessincronizada. Padrão adotado: frase normal, acentuada, terminando em ponto.
 */
public final class Mensagens {

    private Mensagens() {
    }

    // ===== validação genérica =====
    public static final String CAMPOS_OBRIGATORIOS = "Preencha todos os campos obrigatórios.";
    public static final String QUANTIDADE_INVALIDA = "Informe uma quantidade numérica válida.";
    public static final String QUANTIDADE_POSITIVA = "A quantidade deve ser maior que zero.";
    public static final String PRECO_INVALIDO = "Informe um preço numérico válido.";
    public static final String PRECO_POSITIVO = "O preço deve ser maior que zero.";
    public static final String CODIGO_INVALIDO = "O código do produto deve ser um número inteiro.";

    // ===== usuário =====
    public static final String EMAIL_INVALIDO = "Informe um e-mail válido.";
    public static final String EMAIL_DUPLICADO = "Já existe um usuário cadastrado com este e-mail.";
    public static final String CPF_INVALIDO = "Informe um CPF válido com 11 dígitos.";
    public static final String CPF_DUPLICADO = "Já existe um usuário cadastrado com este CPF.";
    public static final String SENHA_CURTA = "A senha deve ter no mínimo 6 caracteres.";
    public static final String USUARIO_NAO_ENCONTRADO = "Usuário não encontrado.";
    public static final String CREDENCIAIS_INVALIDAS = "E-mail ou senha inválidos.";
    public static final String USUARIO_SALVO = "Usuário salvo com sucesso.";
    public static final String USUARIO_EXCLUIDO = "Usuário excluído com sucesso.";
    public static final String USUARIO_COM_PEDIDOS =
            "Este usuário possui pedidos registrados e não pode ser excluído.";

    // ===== produto =====
    public static final String PRODUTO_NAO_ENCONTRADO = "Produto não encontrado.";
    public static final String CODIGO_DUPLICADO = "Já existe um produto com este código.";
    public static final String NOME_PRODUTO_DUPLICADO = "Já existe um produto com este nome.";
    public static final String PRODUTO_SALVO = "Produto salvo com sucesso.";
    public static final String PRODUTO_INATIVADO =
            "Produto inativado com sucesso. Ele sai da loja, mas o histórico de vendas é preservado.";
    public static final String PRODUTO_REATIVADO = "Produto reativado com sucesso.";
    public static final String PRODUTO_INATIVO = "Este produto está inativo e não pode ser vendido.";
    public static final String SELECIONE_PRODUTO = "Selecione um produto.";

    // ===== estoque / venda =====
    public static final String ESTOQUE_INSUFICIENTE = "Item sem a quantidade desejada no estoque";
    public static final String CARRINHO_VAZIO = "Adicione ao menos um produto ao carrinho antes de confirmar.";
    public static final String COMPRA_REALIZADA = "Compra realizada com sucesso.";
    public static final String SELECIONE_ITEM_CARRINHO = "Selecione um item do carrinho para remover.";

    // ===== fornecedor / reposição =====
    public static final String FORNECEDOR_NAO_ENCONTRADO = "Fornecedor não encontrado.";
    public static final String NOME_FORNECEDOR_DUPLICADO = "Já existe um fornecedor com este nome.";
    public static final String FORNECEDOR_SALVO = "Fornecedor salvo com sucesso.";
    public static final String FORNECEDOR_EXCLUIDO = "Fornecedor excluído com sucesso.";
    public static final String FORNECEDOR_COM_REPOSICOES =
            "Este fornecedor possui reposições registradas e não pode ser excluído.";
    public static final String SEM_FORNECEDORES =
            "Nenhum fornecedor cadastrado. Cadastre um fornecedor antes de repor o estoque.";
    public static final String SELECIONE_FORNECEDOR = "Selecione um fornecedor.";
    public static final String REPOSICAO_REALIZADA = "Estoque reposto com sucesso.";

    // ===== erros de infraestrutura =====
    public static final String ERRO_BANCO = "Erro ao acessar o banco de dados:\n";

    /** Mensagem de estoque insuficiente com o saldo real, para orientar o usuário. */
    public static String estoqueInsuficiente(String nomeProduto, int disponivel) {
        return String.format("%s%n%nProduto: %s%nDisponível: %d unidade(s).",
                ESTOQUE_INSUFICIENTE, nomeProduto, disponivel);
    }
}
