package api;

/** Aula 11 - erro ao consultar a cotacao (comunicacao, HTTP ou JSON invalido). */
public class CotacaoException extends Exception {

    public CotacaoException(String mensagem) {
        super(mensagem);
    }

    public CotacaoException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
