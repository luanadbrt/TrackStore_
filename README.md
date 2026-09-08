# Track Store — Sistema de estoque central do avivamento

Projeto integrador (faculdade): sistema desktop em **Java 17 + Swing**, com
persistência via **Hibernate 6 (ORM) + MySQL** e testes automatizados em **JUnit 5**.

---

## Como rodar

### 1. Pré-requisitos

- **JDK 17 ou superior** — confira com `java -version` e `mvn -v`
- **Maven 3.8+**
- **MySQL** rodando localmente

> Se `mvn -v` mostrar um JDK anterior ao 17, a compilação falha com
> `invalid target release: 17`. Ajuste a variável `JAVA_HOME` para apontar para o
> JDK 17 — o problema é do ambiente, não do `pom.xml`.

### 2. Criar o banco

O script cria o schema inteiro (a aplicação **não** cria tabelas sozinha):

```bash
mysql -u root -p < database/script.sql
```

Ele também insere 3 fornecedores e 5 produtos de exemplo.

### 3. Configurar a conexão

As credenciais **não ficam no código**. Copie o modelo e preencha:

```bash
cp db.properties.example db.properties
```

```properties
db.url=jdbc:mysql://localhost:3306/trackstore?useSSL=false&serverTimezone=UTC
db.user=trackstore
db.password=sua-senha
```

O `db.properties` está no `.gitignore` e nunca é versionado. Alternativamente,
defina as variáveis de ambiente `TRACKSTORE_DB_URL`, `TRACKSTORE_DB_USER` e
`TRACKSTORE_DB_PASS` (têm prioridade sobre o arquivo).

O fim do `script.sql` traz os comandos para criar um usuário MySQL dedicado, em vez
de usar `root` na aplicação.

### 4. Rodar

Pela IDE: importe como projeto Maven e execute `com.trackstore.Main`.

Pelo terminal:

```bash
mvn clean package
java -jar target/trackstore.jar
```

### 5. Primeiro acesso

Cadastre-se pela tela de login. **O primeiro usuário do sistema vira ADMIN
automaticamente**; os seguintes viram CLIENTE. Depois disso, o admin muda o tipo de
qualquer usuário pela tela "Gestão de Usuários" — não é mais preciso rodar
`UPDATE usuarios SET tipo='ADMIN'` no banco.

### 6. Testes

```bash
mvn test
```

Os testes usam um banco **H2 em memória**, então rodam sem MySQL ligado e sem tocar
nos seus dados.

---

## Telas

| Tela | Acesso | O que faz |
|---|---|---|
| Login | todos | Autentica por e-mail/senha |
| Menu Principal | todos | Cliente vê só a loja; admin vê tudo |
| Cadastro de Usuário | todos | Autocadastro; admin também escolhe o tipo |
| Gestão de Usuários | admin | Listar, criar, editar e excluir usuários |
| Cadastro de Produtos | admin | Inclusão de produto |
| Gestão de Produtos | admin | Listar, editar, excluir e reativar |
| Loja de Produtos | todos | Busca, carrinho, compra e baixa de estoque |
| Estoque com Reposição | admin | Lista com filtro; botão "+" repõe o saldo |
| Histórico de Reposições | admin | Consulta por produto e período |
| Fornecedores | admin | CRUD de fornecedores |

---

## Estrutura

```
src/main/java/com/trackstore/
├── Main.java
├── model/     -> entidades (Usuario, Produto, Fornecedor, Pedido, ItemPedido, Reposicao)
├── dao/       -> HibernateUtil + DAOs: SÓ persistência, sem regra de negócio
├── service/   -> regras de negócio e controle de transação + Carrinho
├── util/      -> SenhaUtil, CpfUtil, Mensagens, Formato, ValidacaoException
└── view/      -> telas Swing + UI (diálogos e carregamento em segundo plano)

src/main/resources/hibernate.cfg.xml   -> mapeamento e pool (sem credenciais)
src/test/                              -> testes JUnit 5 sobre banco H2
database/script.sql                    -> DDL completo + dados iniciais
db.properties.example                  -> modelo das credenciais
```

Fluxo: **view → service → dao → model**. A view só coleta dados e mostra mensagens;
toda regra vive no `service`; o `dao` só executa consultas.

---

## Decisões de projeto

### Estoque nunca fica negativo

A baixa acontece dentro da mesma transação da gravação do pedido, e o produto é lido
com `PESSIMISTIC_WRITE` (`SELECT ... FOR UPDATE`). Se o saldo for insuficiente, a
transação inteira é desfeita e o usuário recebe *"Item sem a quantidade desejada no
estoque"*. O carrinho também valida antes, relendo o saldo do banco e somando o que
já foi adicionado — mas quem garante é a transação.

### Exclusão de produto é lógica

Produto com vendas ou reposições registradas não é apagado: vira `INATIVO`. Apagar
violaria a chave estrangeira de `itens_pedido`/`reposicoes` e destruiria o histórico.
Produto sem nenhum movimento é apagado de fato.

### Schema criado por script, não pelo ORM

`hibernate.hbm2ddl.auto=validate`. O `script.sql` é a fonte da verdade; o Hibernate
apenas confere na inicialização e falha imediatamente se houver divergência.
`update` (usado na versão anterior) nunca altera tipo de coluna nem corrige
constraint — ele esconde a divergência até virar dado corrompido.

### Dinheiro em `BigDecimal`, quantidade em `int`

`double` não representa `0,10` exatamente: somar três itens de R$ 0,10 daria
`0.30000000000000004`. E não se vende meia unidade de um produto.

### Senha com PBKDF2

PBKDF2-HMAC-SHA256, salt por usuário, 210.000 iterações — tudo do próprio JDK, sem
dependência externa. SHA-256 puro (versão anterior) é rápido demais: velocidade é
justamente o que favorece o ataque de força bruta.

### CPF com 11 dígitos e validação real

Os dígitos verificadores são conferidos (`CpfUtil`), então `12345678901` é rejeitado
mesmo tendo 11 caracteres. Armazenado como texto, para não perder zeros à esquerda.

> **Atenção:** a especificação original do trabalho pedia **9 dígitos**. Se o
> enunciado realmente exigir 9, altere `CpfUtil.DIGITOS` para `9`, remova a
> verificação dos dígitos verificadores e ajuste `usuarios.cpf` para `VARCHAR(9)`
> no `script.sql` e em `Usuario.java`.

### Confirmação de compra em um diálogo só

A versão anterior abria duas confirmações seguidas, o que só treina o usuário a
clicar "Sim" duas vezes sem ler. Agora é **um** diálogo com o resumo completo
(itens, quantidades, total e aviso de irreversibilidade).

> Se o enunciado exigir literalmente "dupla verificação", basta reintroduzir o
> segundo `JOptionPane` em `TelaLojaProdutos.confirmarCompra()`.

### Desempenho

- **HikariCP** como pool de conexões (antes, cada operação abria uma conexão nova).
- Consultas rodam em **`SwingWorker`**, fora da thread da interface — a janela não
  congela esperando o banco.
- Filtros aplicados **no banco**, não em memória.

---

## Segurança

- Credenciais fora do código e fora do controle de versão.
- Senha nunca em texto puro; comparação de hash em tempo constante
  (`MessageDigest.isEqual`).
- Consultas sempre parametrizadas (sem concatenação de valores em HQL).
- Uso de `root` desencorajado: o `script.sql` cria um usuário com permissão mínima.

> A tela de login informa separadamente "usuário não existe" e "senha incorreta"
> porque a especificação pede as duas mensagens. Em um sistema de produção usa-se
> uma mensagem única ("E-mail ou senha inválidos"), já que a distinção permite
> descobrir quais e-mails estão cadastrados.
