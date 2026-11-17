package api;

/**
 * Aula 11 - corpo JSON de deposito e saque:
 *
 *   POST /contas/1001/depositos   {"valor": 200.00}
 *   POST /contas/1001/saques      {"valor": 50.00}
 */
public class OperacaoRequest {

    private Double valor;

    public Double getValor() {
        return valor;
    }
}
