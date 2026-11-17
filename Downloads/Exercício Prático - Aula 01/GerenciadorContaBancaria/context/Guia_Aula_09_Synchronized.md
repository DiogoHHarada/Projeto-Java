# Guia - Aula 09: Controle de Concorrência (synchronized)

## O que foi implementado

| Arquivo | Papel |
|---|---|
| `src/model/ContaConjunta.java` | Conta com **dois titulares** (herda de `Conta`). Tem o saque **sem** e **com** `synchronized`. |
| `src/concorrencia/SaqueTitular.java` | `Runnable` de um titular tentando sacar da conta conjunta. |
| `src/app/MainContaConjunta.java` | Roda o cenário dos slides 2 vezes (sem e com sincronização) e imprime a comparação. |

Checklist da atividade:

1. Adaptar o gerenciador -> `ContaConjunta extends Conta` (reaproveita número, titular, saldo, depositar...).
2. Conta conjunta com dois titulares -> `titular` + `segundoTitular`, `isTitular(nome)`.
3. Duas threads na mesma conta -> `Titular-1` (Ana) e `Titular-2` (Pedro).
4. Rodar sem sincronização -> `sacarSemSincronizacao()`.
5. Proteger com `synchronized` -> `sacarSincronizado()` (+ `sacar`, `depositar`, `getSaldo` sincronizados).
6. Comparar -> tabela "COMPARAÇÃO" no final da execução.

## Como executar no NetBeans

Clean and Build -> botão direito em `MainContaConjunta.java` -> **Run File** (Shift+F6).

Cenário: saldo R$ 100,00, cada titular tenta sacar R$ 80,00. O correto é 1 saque aprovado e
saldo R$ 20,00.

## Resultados obtidos (antes x depois)

| | Saques aprovados | Saldo final | Consistente? |
|---|---|---|---|
| Sem sincronização | 2 | **R$ -60,00** ou **R$ 20,00** | NÃO |
| Com `synchronized` | 1 | R$ 20,00 | SIM |

Sem sincronização apareceram **dois tipos** de inconsistência:

1. **Saldo negativo (-60):** as duas threads passaram pelo `if (valor <= saldo)` enquanto o saldo
   ainda era 100. Depois as duas descontaram: 100 - 80 - 80 = -60. É a tabela do slide
   "Como surge uma inconsistência?".
2. **Atualização perdida (20 com 2 saques):** `saldo -= valor` não é atômico (é
   *ler -> subtrair -> gravar*). As duas threads leram 100 e as duas gravaram 20. O banco
   entregou R$ 160,00 e o saldo caiu só R$ 80,00.

O resultado **varia entre execuções**. É exatamente a característica de uma condição de corrida.

Com `synchronized`, a thread que entra primeiro pega o **monitor** da conta. A outra espera.
Quando a segunda entra, já vê saldo 20 e o saque é negado.

> O `Thread.sleep(100)` entre verificar e atualizar só serve para tornar o defeito visível em
> toda execução. Sem ele o erro continua existindo, mas aparece raramente (o que é pior:
> passa nos testes e falha em produção).

## Receita para a prova

```java
public class Conta {
    private double saldo;

    // método sincronizado: monitor = this (a própria conta)
    public synchronized boolean sacar(double valor) {
        if (valor <= saldo) {      // verificação
            saldo -= valor;        // atualização  -> as duas na MESMA região protegida
            return true;
        }
        return false;
    }

    public synchronized double getSaldo() {
        return saldo;
    }
}
```

Variação: **bloco sincronizado** (protege só o trecho crítico):

```java
public boolean sacar(double valor) {
    // código não crítico (log, validação de parâmetros...)
    synchronized (this) {
        if (valor <= saldo) {
            saldo -= valor;
            return true;
        }
        return false;
    }
}
```

Testando com duas threads:

```java
Conta conta = new Conta(100);
Thread a = new Thread(() -> conta.sacar(80), "A");
Thread b = new Thread(() -> conta.sacar(80), "B");
a.start(); b.start();
a.join();  b.join();
System.out.println(conta.getSaldo());   // com synchronized: sempre 20
```

## Conceitos que caem

| Item | Resumo |
|---|---|
| Condição de corrida | resultado depende da ordem/momento das threads sobre um estado compartilhado |
| Seção crítica | trecho que lê e altera o estado compartilhado (verificar + atualizar o saldo) |
| Monitor | "cadeado" de cada objeto. `synchronized` precisa adquiri-lo para entrar |
| Método `synchronized` | monitor = `this` (ou a classe, se o método for `static`) |
| Bloco `synchronized (obj)` | monitor = `obj`; delimita só o trecho necessário |
| Garante | exclusão mútua, coordenação e visibilidade das alterações |
| NÃO garante | ordem FIFO, ausência de deadlock, proteger quem acessa sem `synchronized` |
| Deadlock | A tem o recurso 1 e espera o 2; B tem o 2 e espera o 1 -> ninguém avança |
| Outras opções | `Lock`/`ReentrantLock` (lock/unlock explícitos), `Semaphore` (limita N threads) |

## Erros comuns

- Sincronizar só o `sacar` e deixar `depositar`/`getSaldo` sem: quem não usa o **mesmo
  monitor** não está protegido.
- Sincronizar em objetos diferentes (`synchronized (new Object())`): cada thread pega um
  cadeado diferente, então não há exclusão.
- Separar a verificação e a atualização em dois métodos sincronizados diferentes: entre um e
  outro a outra thread ainda pode entrar.
- Achar que o teste passou porque "rodou uma vez certo": condição de corrida é intermitente.
