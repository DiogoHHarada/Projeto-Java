# Guia - Aula 11: Serviços de Rede Modernos (HTTP, REST e JSON)

## Biblioteca adicionada: Gson

- `lib/gson-2.11.0.jar` (baixado do Maven Central, SHA-1 conferido).
- Registrado em `nbproject/project.properties` (`file.reference.gson-2.11.0.jar` + `javac.classpath`).
  No NetBeans aparece em **Libraries**. Se não aparecer: botão direito em Libraries ->
  *Add JAR/Folder* -> `lib/gson-2.11.0.jar`.
- O Java **não** converte JSON em objetos sozinho. É para isso que serve o Gson:
  `gson.fromJson(texto, Classe.class)` e `gson.toJson(objeto)`.

## Etapa 1 - Consumo de API externa (cotação de moedas)

| Arquivo | Papel |
|---|---|
| `src/api/Cotacao.java` | Objeto que recebe o JSON (`@SerializedName("bid") compra` ...). |
| `src/api/CotacaoService.java` | `HttpClient` + `HttpRequest` GET, verifica o status, converte o JSON com Gson. |
| `src/api/CotacaoException.java` | Erro de comunicação, HTTP ou JSON inválido. |
| `src/app/MainCotacao.java` | Console: digite USD, EUR, BTC... e veja a cotação. ENTER sai. |

API usada: **AwesomeAPI** (grátis, sem chave). Ex.: `https://economia.awesomeapi.com.br/json/last/USD-BRL`

```json
{"USDBRL":{"code":"USD","codein":"BRL","name":"Dólar Americano/Real Brasileiro",
 "high":"5.8091","low":"5.7594","pctChange":"-0.025932","bid":"5.7825","ask":"5.7833",
 "create_date":"2026-11-17 17:04:17"}}
```

Requisitos atendidos:

1. `HttpClient` + GET -> `cliente.send(requisicao, BodyHandlers.ofString())`.
2. API externa de cotação -> AwesomeAPI.
3. Verifica o código HTTP -> `404` = moeda não encontrada, `!= 200` = erro.
4. Interpreta o JSON -> `JsonParser` pega o objeto `"USDBRL"` e o Gson converte para `Cotacao`.
5. Exibe organizado -> `Cotacao.toString()` (compra, venda, máxima, mínima, variação, data).
6. Trata erros -> timeout, falha de conexão, status != 200, JSON inesperado, código inválido.

Saída real do teste:

```
Dólar Americano/Real Brasileiro
  1 USD = R$ 5,7825 (compra) | R$ 5,7833 (venda)
  Máxima: R$ 5,8091 | Mínima: R$ 5,7594 | Variação: -0,03%
  Atualizado em: 2026-11-17 17:04:17
Não foi possível consultar: Moeda não encontrada: XYZ
```

**NetBeans:** Run File em `MainCotacao.java` e digite a moeda no Output (precisa de internet).

## Etapa 2 - API HTTP do banco (HttpServer)

Organização (igual ao slide "Responsabilidades da aplicação"):

```
ServidorHTTP        recebe as requisições (porta 8080)
   |
ControladorContas   interpreta: caminho + método + JSON -> status + JSON
   |
GerenciadorContas   executa as operações (Map em memória) - o mesmo da Aula 10, ampliado
   |
ContaCorrente       dados da conta + regra do saque
```

| Método | Endpoint | Corpo (JSON) | Sucesso |
|---|---|---|---|
| GET | `/contas` | - | 200 lista |
| GET | `/contas/{numero}` | - | 200 conta |
| POST | `/contas` | `{"numero":1007,"titular":"Lucas","saldo":250.0}` | **201** + header `Location` |
| PUT | `/contas/{numero}` | `{"titular":"Novo nome"}` | 200 conta |
| DELETE | `/contas/{numero}` | - | 200 `{"mensagem":...}` |
| POST | `/contas/{numero}/depositos` | `{"valor":100}` | 200 conta com novo saldo |
| POST | `/contas/{numero}/saques` | `{"valor":50}` | 200 conta com novo saldo |

Erros (sempre `{"erro": "mensagem"}`):

| Status | Quando |
|---|---|
| 400 | JSON inválido, campo faltando, valor <= 0, número não numérico, **saldo insuficiente**, tentar mudar saldo pelo PUT |
| 404 | conta não encontrada / endpoint inexistente |
| 405 | método não permitido no endpoint (ex.: `GET /contas/1/saques`) - com header `Allow` |
| 409 | POST de conta com número que já existe |
| 500 | erro inesperado |

Decisões (para explicar se perguntarem):

- **PUT só altera o titular.** O saldo só muda por depósito/saque, para preservar as regras do banco.
- Contas em **memória**: começam com o `contas.txt`. Ao parar o servidor, as alterações somem.
- `ContaRequest` usa `Integer`/`Double` (não `int`/`double`) para detectar campo ausente (`null`).

**NetBeans:** Run File em `api/ServidorHTTP.java`. Teste no navegador
`http://localhost:8080/contas` (GET) ou no **Postman/Insomnia** (todos). No Postman:
Body -> raw -> JSON. Para parar: botão Stop no Output.

Teste com curl (Prompt de Comando):

```bash
curl http://localhost:8080/contas/1001
curl -X POST http://localhost:8080/contas -H "Content-Type: application/json" -d "{\"numero\":1007,\"titular\":\"Lucas\",\"saldo\":250}"
curl -X POST http://localhost:8080/contas/1007/saques -H "Content-Type: application/json" -d "{\"valor\":1000}"
```

## Receita para a prova

**Cliente HTTP (consumir):**

```java
HttpClient cliente = HttpClient.newHttpClient();
HttpRequest req = HttpRequest.newBuilder()
        .uri(URI.create("https://exemplo.com/api/1"))
        .GET()
        .build();
HttpResponse<String> resp = cliente.send(req, HttpResponse.BodyHandlers.ofString());

if (resp.statusCode() == 200) {
    Filme f = new Gson().fromJson(resp.body(), Filme.class);   // JSON -> objeto
} else {
    System.out.println("Erro HTTP: " + resp.statusCode());
}
// POST com JSON:
HttpRequest post = HttpRequest.newBuilder()
        .uri(URI.create("http://localhost:8080/filmes"))
        .header("Content-Type", "application/json")
        .POST(HttpRequest.BodyPublishers.ofString(json))
        .build();
```

**Servidor HTTP (disponibilizar):**

```java
HttpServer servidor = HttpServer.create(new InetSocketAddress(8080), 0);
servidor.createContext("/filmes", troca -> {
    if (troca.getRequestMethod().equals("GET")) {
        byte[] resp = new Gson().toJson(lista).getBytes(StandardCharsets.UTF_8);
        troca.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        troca.sendResponseHeaders(200, resp.length);
        troca.getResponseBody().write(resp);
    } else if (troca.getRequestMethod().equals("POST")) {
        String corpo = new String(troca.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Filme novo = new Gson().fromJson(corpo, Filme.class);
        // validar -> cadastrar -> sendResponseHeaders(201, ...)
    } else {
        troca.sendResponseHeaders(405, -1);   // -1 = sem corpo
    }
    troca.close();
});
servidor.start();
```

Pegar o `{id}` do caminho: `troca.getRequestURI().getPath()` -> `"/filmes/101"` -> `split("/")`.

## Conceitos que caem

| Item | Resumo |
|---|---|
| HTTP | protocolo de aplicação sobre TCP: requisição (método, recurso, cabeçalhos, corpo) e resposta (status, cabeçalhos, corpo) |
| HTTPS | HTTP + criptografia |
| REST | recursos identificados por URL + métodos HTTP + JSON + **stateless** |
| GET / POST / PUT / PATCH / DELETE | consultar / criar / substituir / alterar parte / remover |
| 200 / 201 / 400 / 401 / 403 / 404 / 405 / 500 | ok / criado / inválida / não autenticado / proibido / não encontrado / método não permitido / erro do servidor |
| JSON | `{}` objeto, `"chave": valor`, string entre aspas, número sem aspas, `[]` lista |
| Endpoint | endereço de uma funcionalidade (`POST /contas/1001/saques`) |
| `HttpClient` / `HttpRequest` / `HttpResponse` / `URI` | pacote `java.net.http` (Java 11+) |
| `HttpServer` / `createContext` / `HttpHandler` / `HttpExchange` | pacote `com.sun.net.httpserver` (módulo `jdk.httpserver`) |

## Erros comuns

- Esquecer `sendResponseHeaders()` antes de escrever o corpo (ou chamar duas vezes).
- Tamanho errado no `sendResponseHeaders`: use `bytes.length`, não `texto.length()` (acentos ocupam 2 bytes).
- Não chamar `troca.close()`: o cliente fica esperando.
- Esquecer o `Content-Type: application/json` no POST do Postman/curl.
- Comparar método com `==` em vez de `.equals("GET")`.
- Gson sem o jar no classpath: `NoClassDefFoundError: com/google/gson/Gson`.
- Porta 8080 ocupada (servidor anterior ainda rodando): `BindException: Address already in use`.
