# Guia - Aula 08: Threads e Concorrência

## O que foi implementado

| Arquivo | Papel |
|---|---|
| `src/concorrencia/AtendimentoBancario.java` | **Runnable**: representa um atendimento (conta + operação + valor + tempo). |
| `src/app/MainAtendimentos.java` | Cria 4 atendimentos, uma **Thread** para cada, `start()`, `join()` e mensagem final. |

Operações reaproveitadas da conta (requisito 3): consulta de saldo, depósito, saque e
consulta dos dados. As contas vêm do `contas.txt` (o mesmo das aulas anteriores), via
`ContaService.carregarContas()`.

Checklist da atividade:

1. Classe de atendimento implementando `Runnable` -> `AtendimentoBancario`
2. Recebe as informações da operação -> construtor `(conta, operacao, valor, tempo)`
3. Usa operações já existentes -> `getSaldo()`, `depositar()`, `sacar()`, `getTitular()`
4. `Thread.sleep()` para simular o tempo -> dentro de `run()`
5. Console mostra thread, conta, operação, início e término -> `printf` no `run()`
6. Pelo menos 3 atendimentos -> 4 threads no `main`
7. `start()` -> `t1.start()` ...
8. `join()` -> `t1.join()` ...
9. Mensagem final -> "Todos os atendimentos foram concluídos."

## Como executar no NetBeans

1. **Clean and Build** (Shift+F11).
2. Botão direito em `MainAtendimentos.java` -> **Run File** (Shift+F6).
   (O `main.class` do projeto continua `app.Main`, por isso use Run File.)
3. Rode 2 ou 3 vezes e compare a ordem das mensagens `TÉRMINO`.

Exemplo de saída (a ordem muda de uma execução para outra):

```
[main] Abrindo os atendimentos...
[Atendimento-1] INÍCIO  do atendimento | conta 1001 | Consulta de saldo
[Atendimento-2] INÍCIO  do atendimento | conta 1002 | Depósito
[Atendimento-3] INÍCIO  do atendimento | conta 1003 | Saque
[Atendimento-4] INÍCIO  do atendimento | conta 1004 | Consulta dos dados da conta
[Atendimento-2] TÉRMINO do atendimento | conta 1002 | Depósito | depositado R$ 500,00 -> novo saldo: R$ 3700,50
[Atendimento-1] TÉRMINO do atendimento | conta 1001 | Consulta de saldo | saldo atual: R$ 1500,00
...
[main] Todos os atendimentos foram concluídos.
```

**Resposta da observação do slide:** não. A ordem de criação/`start()` NÃO determina a
ordem de execução. Quem decide quando cada thread roda é o **escalonador** do sistema.

## Receita para a prova

```java
// 1) a TAREFA
public class MinhaTarefa implements Runnable {
    @Override
    public void run() {
        String nome = Thread.currentThread().getName();
        System.out.println(nome + " começou");
        try {
            Thread.sleep(1000);              // milissegundos
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        System.out.println(nome + " terminou");
    }
}

// 2) no main: o FLUXO que executa a tarefa
Thread t1 = new Thread(new MinhaTarefa(), "T1");
Thread t2 = new Thread(new MinhaTarefa(), "T2");
t1.start();
t2.start();
t1.join();   // main espera (main precisa de "throws InterruptedException" ou try/catch)
t2.join();
System.out.println("Fim");
```

Variações:

- **Estendendo Thread** (menos flexível, a classe não pode herdar de outra):
  `class MinhaThread extends Thread { public void run() {...} }` -> `new MinhaThread().start();`
- **Lambda** (Runnable é interface funcional):
  `Thread t = new Thread(() -> System.out.println("oi"), "T1");`

Conceitos que caem:

| Item | Resumo |
|---|---|
| Processo | programa em execução |
| Thread | fluxo de execução dentro de um processo |
| `Runnable` | define o **trabalho** (método `run()`) |
| `Thread` | fornece o **fluxo** que executa o trabalho |
| `start()` | cria a nova thread, que chama `run()` |
| `run()` | só o código da tarefa; chamar direto **não** cria thread |
| `sleep(ms)` | pausa a thread **atual** |
| `join()` | a thread atual espera a outra terminar |
| `currentThread().getName()` | identifica quem está executando |

## Erros comuns

- Chamar `t.run()` em vez de `t.start()`: roda tudo na thread `main`, em sequência.
- Esquecer o `try/catch (InterruptedException)` em `sleep()` e `join()` (não compila).
- Esperar que a ordem dos `start()` seja a ordem de execução.
- Chamar `start()` duas vezes na mesma thread (`IllegalThreadStateException`).
- Não usar `join()`: a mensagem "concluídos" pode aparecer antes de os atendimentos terminarem.

Nesta aula **não** há sincronização: cada thread mexe em uma conta diferente. Quando duas
threads alteram a **mesma** conta, aparece a condição de corrida (Aula 09).
