package config;

/**
 * Configuracao centralizada do banco (Aula 06 - Tarefa 2).
 *
 * O codigo-fonte nao guarda mais URL/usuario/senha: esses valores vem do
 * AMBIENTE. Assim o mesmo codigo roda na sua maquina, na do professor e no
 * servidor, mudando so a configuracao.
 *
 * Como definir (escolha UMA das formas):
 *
 *  1) Variaveis de ambiente do Windows (permanente):
 *       setx DB_URL "jdbc:mysql://localhost:3306/banco_digital"
 *       setx DB_USER "root"
 *       setx DB_PASSWORD "sua_senha"
 *     Depois FECHE e reabra o NetBeans (variaveis sao lidas ao iniciar o processo).
 *
 *  2) NetBeans -> Properties -> Run -> VM Options (vale so para este projeto):
 *       -DDB_URL=jdbc:mysql://localhost:3306/banco_digital -DDB_USER=root -DDB_PASSWORD=sua_senha
 */
public class DatabaseConfig {

    public static final String URL = getRequiredEnvironmentVariable("DB_URL");
    public static final String USER = getRequiredEnvironmentVariable("DB_USER");
    public static final String PASSWORD = getRequiredEnvironmentVariable("DB_PASSWORD");

    private static String getRequiredEnvironmentVariable(String variableName) {

        String value = System.getenv(variableName);

        // EXTRA (nao pedido na aula): aceita tambem -Dvariavel=valor nas VM Options,
        // porque o NetBeans nao tem campo para variavel de ambiente em projeto Ant.
        if (value == null || value.isBlank()) {
            value = System.getProperty(variableName);
        }

        if (value == null || value.isBlank()) {
            throw new IllegalStateException(
                    "A variável de ambiente " + variableName + " não foi configurada.");
        }

        return value;
    }
}
