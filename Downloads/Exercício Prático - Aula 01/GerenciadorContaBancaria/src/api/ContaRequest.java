package api;

/**
 * Aula 11 - corpo JSON recebido em POST /contas e PUT /contas/{numero}.
 *
 *   POST /contas            {"numero": 1007, "titular": "Lucas Alves", "saldo": 250.00}
 *   PUT  /contas/1007       {"titular": "Lucas A. Alves"}
 *
 * Usa Integer/Double (e nao int/double) para saber quando o campo NAO veio:
 * nesse caso o Gson deixa null, e o controlador responde 400.
 */
public class ContaRequest {

    private Integer numero;
    private String titular;
    private Double saldo;

    public Integer getNumero() {
        return numero;
    }

    public String getTitular() {
        return titular;
    }

    public Double getSaldo() {
        return saldo;
    }
}
