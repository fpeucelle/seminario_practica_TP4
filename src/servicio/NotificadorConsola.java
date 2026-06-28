package servicio;

import java.io.IOException;

public class NotificadorConsola implements INotificador {
    private final BitacoraArchivo bitacora;

    public NotificadorConsola(BitacoraArchivo bitacora) {
        this.bitacora = bitacora;
    }

    @Override
    public void notificar(String destinatario, String mensaje) throws IOException {
        System.out.println(">> NOTIFICACION SIMULADA a " + destinatario + ": " + mensaje);
        bitacora.registrarEvento("NOTIFICACION", destinatario + " - " + mensaje);
    }
}
