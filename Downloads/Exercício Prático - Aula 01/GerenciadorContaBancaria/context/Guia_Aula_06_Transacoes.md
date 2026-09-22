# Guia de prova — Transações JDBC e Configuração (Aula 06)

Continuação do `Guia_Aula_05_JDBC.md`. Aqui entram **transações (ACID)**,
**PreparedStatement x SQL Injection** e **configuração fora do código-fonte**.

---

## 1. Transação em uma frase

> Várias operações que precisam dar certo **juntas**. Ou todas gravam (`commit`),
> ou nenhuma grava (`rollback`).

O exemplo clássico é a transferência: se o débito funciona e o crédito falha, o dinheiro
some. A transação impede isso.

### As 3 linhas que fazem tudo
```java
conn.setAutoCommit(false);  // 1. abre a transação (desliga o commit automático)
conn.commit();              // 2. confirma TUDO
conn.rollback();            // 3. desfaz TUDO
```

> Por padrão o JDBC está em **autocommit**: cada `executeUpdate()` já grava sozinho.
> Por isso o `setAutoCommit(false)` é obrigatório para agrupar comandos.

### O molde completo
```java
Connection conn = null;
try {
    conn = Conexao.getConnection();
    conn.setAutoCommit(false);

    // ... comando 1 ...
    // ... comando 2 ...

    conn.commit();
} catch (Exception e) {
    if (conn != null) conn.rollback();
    throw e;
} finally {
    if (conn != null) { conn.setAutoCommit(true); conn.close(); }
}
```

**Atenção:** aqui **não** dá para usar try-with-resources na `Connection` como nos outros
métodos, porque você precisa da conexão dentro do `catch` para chamar `rollback()`.
Por isso o `finally` fecha na mão.

### ACID (pode cair na teoria)
| Letra | Nome | Significado |
|---|---|---|
| **A** | Atomicidade | tudo ou nada |
| **C** | Consistência | o banco sai de um estado válido para outro válido |
| **I** | Isolamento | uma transação não enxerga o meio da outra |
| **D** | Durabilidade | depois do commit, persiste mesmo se faltar energia |

---

## 2. O que foi implementado

### `src/config/DatabaseConfig.java` (novo) — Tarefa 2
URL, usuário e senha saíram do código e vieram do **ambiente**:
```java
public static final String URL      = getRequiredEnvironmentVariable("DB_URL");
public static final String USER     = getRequiredEnvironmentVariable("DB_USER");
public static final String PASSWORD = getRequiredEnvironmentVariable("DB_PASSWORD");

private static String getRequiredEnvironmentVariable(String variableName) {
    String value = System.getenv(variableName);
    if (value == null || value.isBlank()) {
        value = System.getProperty(variableName);   // EXTRA: aceita também -Dvar=valor
    }
    if (value == null || value.isBlank()) {
        throw new IllegalStateException("A variável de ambiente " + variableName + " não foi configurada.");
    }
    return value;
}
```
> A linha do `System.getProperty` **não** está na aula. Coloquei porque projeto Ant do
> NetBeans não tem campo de variável de ambiente, mas tem *VM Options*. Se a professora
> quiser exatamente o código dela, é só apagar essas 3 linhas.

### `src/dao/Conexao.java` — agora sem segredo nenhum
```java
public static Connection getConnection() throws SQLException {
    return DriverManager.getConnection(
            DatabaseConfig.URL, DatabaseConfig.USER, DatabaseConfig.PASSWORD);
}
```

### `src/dao/ContaDAO.transferir(...)` — Tarefa 4
```java
String sqlDebito  = "UPDATE contas SET saldo = saldo - ? WHERE numero = ? AND saldo >= ?";
String sqlCredito = "UPDATE contas SET saldo = saldo + ? WHERE numero = ?";
```
Dois detalhes que valem ponto:
- **`AND saldo >= ?`** no débito: o próprio banco impede saldo negativo. Se voltar
  `0 linhas afetadas`, ou a conta não existe ou faltou saldo → exceção → `rollback`.
- **`saldo = saldo - ?`** em vez de ler o saldo, calcular no Java e gravar: evita que duas
  transferências simultâneas sobrescrevam uma à outra.

### `src/view/ContaGUI.java` — Tarefa 5
Botão **Transferir**: usa a conta selecionada como origem, pergunta destino e valor,
chama `contaDAO.transferir(...)` e recarrega a tabela do banco (duas contas mudaram).
Cada erro tem seu `catch`: valor inválido, mesma conta, saldo insuficiente e erro de banco.

### `src/app/MainJDBC.java` — testes
Ganhou a seção 5 com três cenários: transferência válida (commit), saldo insuficiente
(rollback) e destino inexistente (**rollback de verdade**, porque o débito já tinha sido feito).

---

## 3. Como configurar as variáveis (obrigatório para rodar)

Sem isso o programa avisa que a configuração está ausente.

### Opção A — VM Options do NetBeans (mais rápido, vale só para o projeto)
1. Botão direito no projeto → **Properties** → categoria **Run**.
2. No campo **VM Options**, cole tudo em uma linha:
```
-DDB_URL=jdbc:mysql://localhost:3306/banco_digital -DDB_USER=root -DDB_PASSWORD=sua_senha
```
3. **OK**. Funciona no *Run Project* e no *Run File*.

### Opção B — Variáveis de ambiente do Windows (o que a aula pede de verdade)
No PowerShell:
```powershell
setx DB_URL "jdbc:mysql://localhost:3306/banco_digital"
setx DB_USER "root"
setx DB_PASSWORD "sua_senha"
```
Depois **feche e reabra o NetBeans** — variáveis de ambiente são lidas quando o processo
inicia, então o NetBeans aberto não enxerga as novas.

Conferir se pegou: `echo $env:DB_URL` num PowerShell **novo**.

---

## 4. PreparedStatement x SQL Injection (teoria que cai na prova)

**Errado — concatenando:**
```java
String sql = "SELECT * FROM usuarios WHERE nome = '" + entrada + "'";
```
Se `entrada` for `admin' OR '1'='1`, o SQL vira
`... WHERE nome = 'admin' OR '1'='1'` → retorna **todos** os usuários.

**Certo — parametrizado:**
```java
String sql = "SELECT * FROM usuarios WHERE nome = ?";
PreparedStatement stmt = conn.prepareStatement(sql);
stmt.setString(1, entrada);
```
O banco recebe a estrutura da consulta **antes** dos dados. O que entra pelo `?` é sempre
tratado como **valor**, nunca como comando — procura literalmente um usuário chamado
`admin' OR '1'='1` e não acha nada. Ataque neutralizado.

Três vantagens: **segurança** (dado ≠ código), **sintaxe validada** antes e
**performance** (SQL pré-compilado, reaproveitado a cada execução).

---

## 5. Checklist da atividade → onde está

- [x] Banco `banco_digital` + tabela `contas` → `sql/banco_digital.sql`
- [x] `Conexao` com parâmetros vindos de classe centralizada → `dao/Conexao.java` + `config/DatabaseConfig.java`
- [x] CRUD `inserir` / `buscarPorNumero` / `atualizarSaldo` / `remover` → `dao/ContaDAO.java`
- [x] `transferir(origem, destino, valor)` com atomicidade → `dao/ContaDAO.java`
- [x] GUI: inserir, listar, atualizar saldo, excluir e **transferir** → `view/ContaGUI.java`

---

## 6. Erros comuns com transações

| Sintoma | Causa | Solução |
|---|---|---|
| Gravou só metade da operação | esqueceu `setAutoCommit(false)` | cada comando virou uma transação isolada |
| "Nada é gravado" | esqueceu o `conn.commit()` | ao fechar a conexão sem commit, o MySQL descarta |
| `rollback()` não desfaz nada | tabela em **MyISAM** | MyISAM não suporta transação; use **InnoDB** (padrão no MySQL 8) |
| `Can't call rollback when autocommit=true` | chamou rollback sem abrir transação | `setAutoCommit(false)` primeiro |
| Conexão "suja" no resto do programa | não voltou o autocommit | `conn.setAutoCommit(true)` no `finally` |
| `IllegalStateException: variável ... não foi configurada` | faltam DB_URL/DB_USER/DB_PASSWORD | veja a seção 3 |
| Saldo ficou negativo | validou só no Java | ponha a regra no SQL: `AND saldo >= ?` |

Conferir a engine da tabela no Workbench:
```sql
SELECT TABLE_NAME, ENGINE FROM information_schema.TABLES
WHERE TABLE_SCHEMA = 'banco_digital';   -- tem que aparecer InnoDB
```

---

## 7. Receita rápida: "implemente transferência com transação"

1. Dois `UPDATE` com aritmética no SQL (`saldo = saldo - ?` / `saldo = saldo + ?`).
2. `conn.setAutoCommit(false)` logo após abrir a conexão.
3. Checar `executeUpdate() == 0` em cada um → se der 0, `throw`.
4. `conn.commit()` no fim do `try`.
5. `conn.rollback()` no `catch`, e `throw e` de novo para a tela saber.
6. `setAutoCommit(true)` + `close()` no `finally`.
7. Na GUI: `try/catch` mostrando `JOptionPane` para cada tipo de erro.
