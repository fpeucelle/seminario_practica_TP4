package excepciones;

public class OperacionCanceladaException extends RuntimeException {
    public OperacionCanceladaException() {
        super("Operacion cancelada por el usuario.");
    }
}
