# Guia - Aula 10: Sockets e Comunicação em Rede (TCP)

## O que foi implementado (pacote `rede`)

| Componente do slide | Arquivo | Responsabilidade |
|---|---|---|
| Cliente | `ClienteBanco.java` | Menu "BANCO JAVA", monta a solicitação, envia e exibe a resposta |
| Servidor | `ServidorBanco.java` | `ServerSocket` na porta 5000, recebe e **interpreta** as solicitações |
| GerenciadorContas | `GerenciadorContas.java` | Contas **em memória** (`Map`), localiza contas e executa as operações |
| ContaBancaria | `model/ContaCorrente.java` (já existia) | Representa a conta e a regra do saque |

As contas iniciais vêm do `contas.txt`. Requisitos atendidos:

1. Contas em memória no servidor -> `GerenciadorContas` (`HashMap<Integer, ContaCorrente>`).
2. Cliente envia por socket -> `new Socket("localhost", 5000)` + `PrintWriter.println`.
3. Servidor interpreta e executa -> `ServidorBanco.processar()` + `GerenciadorContas`.
4. Uma resposta por solicitação -> `saida.println(resposta)` dentro do `while (readLine)`.
5. Cliente exibe o resultado -> mostra a linha recebida e uma tradução amigável.

**Regras de negócio só no servidor:** o cliente não verifica saldo, conta ou valor. Ele só manda
o texto.

## Protocolo de aplicação (uma linha por mensagem, campos separados por `;`)

| Operação | Solicitação | Resposta |
|---|---|---|
| Consultar saldo | `CONSULTAR;1001` | `SALDO;1500.00` |
| Depositar | `DEPOSITAR;1001;200.00` | `OK;DEPOSITO_REALIZADO;1700.00` |
| Sacar | `SACAR;1001;50.00` | `OK;SAQUE_REALIZADO;1650.00` |
| Erros | | `ERRO;SALDO_INSUFICIENTE`, `ERRO;CONTA_INEXISTENTE`, `ERRO;VALOR_INVALIDO`, `ERRO;COMANDO_INVALIDO` |

O 3º campo do `OK` (novo saldo) é um extra em relação à tabela do slide, inspirado no exemplo
`DEPOSITO_REALIZADO;SALDO:1200.00`. Valores vão sempre com ponto (`Locale.US`).

## Como executar no NetBeans

1. Clean and Build.
2. Botão direito em `rede/ServidorBanco.java` -> **Run File**. Aparece "Servidor aguardando conexão...".
3. Botão direito em `rede/ClienteBanco.java` -> **Run File**. O NetBeans abre **outra aba** de Output.
4. Digite as opções **na aba do cliente** (clique dentro do Output para digitar).
5. Para parar o servidor: botão vermelho (Stop) na aba dele. Se esquecer, a próxima execução dá
   "Address already in use" (porta ocupada).

Teste feito:

```
CONSULTAR;1001         -> SALDO;1500.00
DEPOSITAR;1001;200.00  -> OK;DEPOSITO_REALIZADO;1700.00
SACAR;1001;50          -> OK;SAQUE_REALIZADO;1650.00
SACAR;1003;5000        -> ERRO;SALDO_INSUFICIENTE
CONSULTAR;9999         -> ERRO;CONTA_INEXISTENTE
DEPOSITAR;1002;abc     -> ERRO;VALOR_INVALIDO
(novo cliente) CONSULTAR;1001 -> SALDO;1650.00   (memória do servidor preservada)
```

## Receita para a prova

**Servidor:**

```java
try (ServerSocket servidor = new ServerSocket(5000)) {
    System.out.println("Servidor aguardando conexão...");
    try (Socket cliente = servidor.accept();                        // BLOQUEIA até conectar
         BufferedReader entrada = new BufferedReader(
                 new InputStreamReader(cliente.getInputStream()));
         PrintWriter saida = new PrintWriter(cliente.getOutputStream(), true)) { // true = autoflush

        String linha;
        while ((linha = entrada.readLine()) != null) {               // null = cliente fechou
            String[] p = linha.split(";");
            // ... interpreta p[0], p[1], p[2] e executa ...
            saida.println("OK;...");                                 // SEMPRE responder
        }
    }
} catch (IOException e) {
    System.out.println("Erro: " + e.getMessage());
}
```

**Cliente:**

```java
try (Socket socket = new Socket("localhost", 5000);
     BufferedReader teclado = new BufferedReader(new InputStreamReader(System.in));
     PrintWriter saida = new PrintWriter(socket.getOutputStream(), true);
     BufferedReader entrada = new BufferedReader(
             new InputStreamReader(socket.getInputStream()))) {

    System.out.print("Digite uma mensagem: ");
    saida.println(teclado.readLine());          // envia UMA linha
    System.out.println("Resposta: " + entrada.readLine());  // espera UMA linha
} catch (IOException e) {
    System.out.println("Erro: " + e.getMessage());
}
```

Variação: atender **vários clientes ao mesmo tempo** = uma thread por conexão (Aula 08).
No `while (true)`, faça `Socket s = servidor.accept(); new Thread(() -> atender(s)).start();`.
Os métodos do `GerenciadorContas` já são `synchronized` (Aula 09) para isso funcionar.

## Conceitos que caem

| Item | Resumo |
|---|---|
| IP | identifica a máquina (interface de rede). `localhost` = 127.0.0.1 = o próprio PC |
| Porta | identifica o serviço dentro da máquina (ex.: 5000) |
| TCP | orientado a conexão, confiável, ordenado, bidirecional. **Não** separa mensagens |
| Protocolo de aplicação | o "combinado" do formato. Aqui: 1 linha = 1 mensagem, campos com `;` |
| `ServerSocket` | servidor: abre a porta e espera conexões |
| `accept()` | bloqueia até um cliente conectar e devolve um `Socket` |
| `Socket` | uma conexão. No cliente: `new Socket(host, porta)` (quem inicia é o cliente) |
| `getInputStream()` / `getOutputStream()` | bytes recebidos / enviados |
| `InputStreamReader` | bytes -> caracteres. `BufferedReader.readLine()` lê uma linha |
| `PrintWriter(out, true)` | escreve texto. `true` = descarrega a cada `println` |

## Erros comuns

- **Connection refused**: rodou o cliente antes do servidor (ou porta/host errados).
- **Address already in use**: o servidor anterior ainda está rodando na porta 5000.
- **Programa "travado"**: `readLine()` esperando uma linha que nunca chega. Esqueceu o
  `println` (usou `print`) ou o `true` (autoflush) do `PrintWriter`.
- **`null` no `readLine()`**: o outro lado fechou a conexão.
- Colocar regra de negócio no cliente: na prova, o servidor é quem decide.
