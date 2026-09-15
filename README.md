# Catálogo de Produtos — Product Catalog API

API REST para gerenciar produtos de uma empresa de vendas on-line. Projeto acadêmico do desafio final de Arquitetura de Software, implementado como um monólito em camadas com Spring Boot e MVC.

A aplicação destina-se a estudo. Não representa uma solução pronta para produção.

## Funcionalidades

- Criar produtos e consultar o recurso criado pelo cabeçalho `Location`.
- Listar todos os produtos e consultar um produto por ID.
- Buscar por parte do nome, sem diferenciar maiúsculas e minúsculas.
- Contar, atualizar e excluir produtos.
- Validar entradas e retornar erros padronizados em JSON.
- Consultar a especificação OpenAPI e o console H2 durante a execução.

A listagem e a contagem incluem produtos ativos e inativos. A exclusão remove o registro; não é uma desativação. Não há paginação nem ordem de listagem garantida pelo código.

## Tecnologias e versões

| Tecnologia | Versão/configuração | Uso |
|---|---|---|
| Java | 21 | Linguagem e nível de compilação |
| Spring Boot | 3.5.16 | Inicialização e configuração da aplicação |
| Maven | 3.9.9 pelo Wrapper | Compilação, testes e empacotamento |
| Maven Wrapper | 3.3.4, `only-script` | Execução sem instalar Maven separadamente |
| Springdoc OpenAPI | 2.8.17 | Especificação OpenAPI e Swagger UI |
| Spring Web / Spring MVC | Gerenciada pelo Spring Boot | Contrato HTTP e JSON |
| Spring Data JPA / Hibernate | Gerenciada pelo Spring Boot | Persistência e validação do schema |
| Bean Validation | Gerenciada pelo Spring Boot | Validação de entrada |
| H2 / Flyway | Gerenciada pelo Spring Boot | Banco em memória e migration |
| JUnit 5, Mockito e MockMvc | Via Spring Boot Starter Test | Testes unitários, MVC e integração |

As dependências estão declaradas no [pom.xml](pom.xml). Identificação Maven: `br.com.rauel:product-catalog-api:0.0.1-SNAPSHOT`. A versão informada no OpenAPI é `1.0.0`.

## Pré-requisitos

- **JDK 21**, com `JAVA_HOME` apontando para a instalação correta e o executável Java disponível no terminal.
- PowerShell no Windows; Git Bash, Bash no Linux ou no macOS para os comandos Unix.
- Acesso à internet no primeiro uso para obter a distribuição Maven e as dependências.
- Porta 8080 disponível para a execução padrão.

Execute os comandos a partir da raiz do repositório. Confira a instalação com `java -version` e a versão usada pelo Wrapper com `.\mvnw.cmd -version` no PowerShell ou `./mvnw -version` no Bash. Se houver múltiplos JDKs, confira também `JAVA_HOME`: o Wrapper pode usar uma instalação diferente daquela encontrada no PATH.

Não é necessário IntelliJ, Docker ou servidor de banco de dados. O Wrapper oficial usa scripts e não exige um JAR próprio do Wrapper no repositório.

## Compilar e verificar

O comando abaixo compila, executa os testes e empacota a aplicação.

### Windows — PowerShell

```powershell
.\mvnw.cmd clean verify
```

### Git Bash, Linux e macOS

```bash
./mvnw clean verify
```

Se o script não tiver permissão de execução no Linux/macOS, conceda-a com `chmod +x mvnw`.

## Executar

### Windows — PowerShell

```powershell
.\mvnw.cmd spring-boot:run
```

### Git Bash, Linux e macOS

```bash
./mvnw spring-boot:run
```

A API fica em `http://localhost:8080/api/v1/products`. Use `Ctrl+C` no terminal para encerrar. Os dados do banco em memória são perdidos ao encerrar a aplicação.

## Executar os testes

### Windows — PowerShell

```powershell
.\mvnw.cmd clean test
```

### Git Bash, Linux e macOS

```bash
./mvnw clean test
```

O código contém 45 casos, considerando as entradas dos testes parametrizados:

| Classe | Casos | Abrangência |
|---|---:|---|
| `TesteServicoProduto` | 11 | Casos de uso, mapeamento e produto inexistente, com Mockito |
| `TesteControladorProduto` | 31 | HTTP e validação com MockMvc e service simulado |
| `TesteAplicacaoCatalogoProdutos` | 3 | Contexto, Flyway, CRUD, busca e datas com H2 real |

Os testes de integração usam `jdbc:h2:mem:producttestdb` e rollback transacional. A contagem descreve a suíte existente; não é uma declaração de nova execução. Não há testes específicos para os erros 405, 406, 415 e 500, nem testes de carga ou medição de cobertura configurada.

## Estrutura de diretórios

```text
.
├── .mvn/
│   ├── maven.config
│   └── wrapper/maven-wrapper.properties
├── docs/
│   └── arquitetura.md
├── src/
│   ├── main/
│   │   ├── java/br/com/rauel/productcatalog/
│   │   │   ├── AplicacaoCatalogoProdutos.java
│   │   │   ├── controller/ControladorProduto.java
│   │   │   ├── dto/
│   │   │   ├── exception/
│   │   │   ├── model/Produto.java
│   │   │   ├── repository/RepositorioProduto.java
│   │   │   └── service/ServicoProduto.java
│   │   └── resources/
│   │       ├── application.yml
│   │       └── db/migration/V1__create_product_table.sql
│   └── test/java/br/com/rauel/productcatalog/
│       ├── TesteAplicacaoCatalogoProdutos.java
│       ├── controller/TesteControladorProduto.java
│       └── service/TesteServicoProduto.java
├── .gitignore
├── mvnw
├── mvnw.cmd
├── pom.xml
└── README.md
```

### Camadas

- **Controller:** `ControladorProduto` recebe requisições e define as respostas HTTP.
- **Service:** `ServicoProduto` concentra os casos de uso, as transações e o mapeamento manual.
- **Repository:** `RepositorioProduto` abstrai a persistência com Spring Data JPA.
- **Model:** `Produto` representa a entidade e controla suas datas.
- **DTO:** records representam entradas, saídas e erros sem expor a entidade diretamente.
- **Exception:** `ExcecaoProdutoNaoEncontrado` e `TratadorGlobalExcecoes` tratam falhas da aplicação e do contrato HTTP.

No MVC desta API, a View corresponde às representações JSON. Não existe uma interface HTML de gerenciamento de produtos.

## Endpoints

As rotas permanecem em inglês; os campos JSON são em português.

| Método | Caminho | Resultado principal |
|---|---|---|
| POST | `/api/v1/products` | 201, produto criado e `Location`; 400 se entrada inválida |
| GET | `/api/v1/products` | 200 e lista, inclusive `[]` |
| GET | `/api/v1/products/{id}` | 200; 404 se inexistente; 400 se ID inválido |
| GET | `/api/v1/products/search?name={name}` | 200 e lista; 400 se parâmetro ausente, branco ou maior que 150 caracteres |
| GET | `/api/v1/products/count` | 200 e objeto com `quantidade` |
| PUT | `/api/v1/products/{id}` | 200; 400 se entrada/ID inválido; 404 se inexistente |
| DELETE | `/api/v1/products/{id}` | 204 sem corpo; 400 se ID inválido; 404 se inexistente |

O PUT recebe todos os campos obrigatórios editáveis. Preserva `id` e `criadoEm` e atualiza `atualizadoEm`. A busca recebe o parâmetro **`name`**, mesmo que o atributo interno seja `nome`.

## Contrato e exemplos

Os exemplos são ilustrativos, derivados dos DTOs e do controller. IDs e datas são gerados na execução.

### Criar produto

```http
POST /api/v1/products HTTP/1.1
Host: localhost:8080
Content-Type: application/json
Accept: application/json

{
  "nome": "Teclado",
  "descricao": "USB",
  "preco": 99.90,
  "quantidadeEstoque": 10,
  "ativo": true
}
```

Resposta de exemplo:

```http
HTTP/1.1 201 Created
Location: http://localhost:8080/api/v1/products/1
Content-Type: application/json

{
  "id": 1,
  "nome": "Teclado",
  "descricao": "USB",
  "preco": 99.90,
  "quantidadeEstoque": 10,
  "ativo": true,
  "criadoEm": "2026-01-01T10:00:00.123456",
  "atualizadoEm": "2026-01-01T10:00:00.123456"
}
```

### Atualizar produto

```http
PUT /api/v1/products/1 HTTP/1.1
Host: localhost:8080
Content-Type: application/json

{
  "nome": "Mouse",
  "descricao": null,
  "preco": 25.50,
  "quantidadeEstoque": 0,
  "ativo": false
}
```

Retorna 200 com os mesmos campos de `RespostaProduto`, os valores editados, a data de criação preservada e a data de atualização renovada. A descrição é opcional; `null` a remove.

### Consultar, buscar e contar

```http
GET /api/v1/products/1 HTTP/1.1
Host: localhost:8080
```

A consulta retorna um objeto `RespostaProduto`. Listagem e busca retornam arrays desses objetos. Uma busca sem correspondências retorna `[]`.

```http
GET /api/v1/products/search?name=TEC HTTP/1.1
Host: localhost:8080
```

```http
GET /api/v1/products/count HTTP/1.1
Host: localhost:8080
```

Resposta ilustrativa da contagem:

```json
{"quantidade": 1}
```

### Validações e erros

| Campo | Regra |
|---|---|
| `nome` | Obrigatório, não branco, 3 a 150 caracteres após remoção de espaços externos |
| `descricao` | Opcional, até 500 caracteres |
| `preco` | Obrigatório, maior que zero, até 13 dígitos inteiros e 2 decimais |
| `quantidadeEstoque` | Inteiro obrigatório e não negativo |
| `ativo` | Boolean obrigatório; `false` é válido |

Não envie `id`, `criadoEm` ou `atualizadoEm` no POST/PUT. Campos desconhecidos são rejeitados. Números fracionários não são convertidos em inteiros para o estoque.

Exemplo de erro para um produto inexistente:

```json
{
  "dataHora": "2026-01-01T10:00:00",
  "status": 404,
  "erro": "Not Found",
  "mensagem": "Produto com ID 9 não encontrado",
  "caminho": "/api/v1/products/9",
  "campos": []
}
```

Exemplo de erro de validação quando apenas o preço é zero:

```json
{
  "dataHora": "2026-01-01T10:00:00",
  "status": 400,
  "erro": "Bad Request",
  "mensagem": "Dados de entrada inválidos",
  "caminho": "/api/v1/products",
  "campos": [
    {"campo": "preco", "mensagem": "O preço deve ser maior que zero"}
  ]
}
```

O atributo `erro` usa a descrição HTTP em inglês. As mensagens próprias são em português. Não há stack trace na resposta. Os nomes JSON antigos em inglês não são aliases dos campos atuais.

## Swagger e OpenAPI

Com a aplicação em execução:

- [Swagger UI](http://localhost:8080/swagger-ui/index.html)
- [Entrada alternativa do Swagger](http://localhost:8080/swagger-ui.html)
- [OpenAPI JSON](http://localhost:8080/v3/api-docs)

O título da especificação é **Catálogo de Produtos**, com versão **1.0.0**.

## Banco e console H2

Acesse o [console H2](http://localhost:8080/h2-console/) com:

```text
JDBC URL: jdbc:h2:mem:productdb
Usuário: sa
Senha: vazia
```

Deixe o campo de senha em branco. O Flyway cria a tabela `products` pela migration V1; Hibernate usa `ddl-auto=validate` e apenas valida a compatibilidade do schema. SQL visível e formatado está habilitado.

**Os dados existem somente durante a execução.** Ao encerrar a aplicação, o catálogo é perdido. Na próxima inicialização, o banco em memória e sua estrutura são recriados, sem produtos iniciais cadastrados pela migration.

## Arquitetura

Consulte o [documento arquitetural](docs/arquitetura.md), com requisitos, decisões, limitações e diagramas de contexto, contêineres, componentes e sequência.

## Limitações e possíveis evoluções

A implementação acadêmica exige Java 21 e não possui autenticação, autorização, paginação, banco persistente, containerização ou testes de carga. Não implementa integrações externas, mensageria, mecanismos de resiliência ou garantias de escalabilidade. Não há garantia de ordem de listagem nem política de concorrência com versionamento otimista.

Possíveis evoluções, **não implementadas**, incluem banco persistente, controle de acesso, paginação/ordenação, testes dos caminhos de erro restantes e de carga, análise de cobertura, controle de concorrência e containerização. Cada evolução exige nova avaliação do escopo e validação.
