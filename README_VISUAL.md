# Track Store — atualização visual

Esta versão mantém a lógica do sistema e concentra a melhoria na camada Swing (`com.trackstore.view`).

## O que foi melhorado

- Paleta visual em roxo, lavanda, branco e detalhes dourados.
- Botões arredondados, com hover e melhor espaçamento.
- Campos de formulário mais confortáveis para leitura e digitação.
- Tabelas com cabeçalho destacado, linhas alternadas e seleção mais clara.
- Cards do painel principal com bordas arredondadas.
- Tela de login com identidade visual mais forte e imagem de igreja.
- Tela principal com banner visual relacionado à igreja.
- Menu lateral com hover visual.
- Imagem externa do Unsplash usada quando houver internet; `church-fallback.png` continua como fallback offline.

## Imagem

A foto principal é uma fotografia de interior de igreja publicada no Unsplash por Matt Palmer:
https://unsplash.com/photos/church-interior-6tJbgwdeh8Q

O código não depende exclusivamente da internet: se a imagem externa não carregar, o sistema usa `src/main/resources/church-fallback.png`.

## Arquivos principais alterados

- `src/main/java/com/trackstore/view/UI.java`
- `src/main/java/com/trackstore/view/TelaLogin.java`
- `src/main/java/com/trackstore/view/TelaPrincipal.java`

A lógica de banco, serviços e modelos não foi alterada nesta atualização visual.
