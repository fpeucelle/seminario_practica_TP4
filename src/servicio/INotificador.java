package servicio;

import java.io.IOException;

public interface INotificador {
    void notificar(String destinatario, String mensaje) throws IOException;
}
