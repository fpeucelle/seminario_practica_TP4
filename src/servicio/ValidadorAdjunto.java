package servicio;

import excepciones.ValidacionException;
import java.io.File;

public class ValidadorAdjunto {
    private static final long TAMANO_MAXIMO_BYTES = 10L * 1024L * 1024L;
    private static final String[] EXTENSIONES_PERMITIDAS = {"pdf", "doc", "docx", "xls", "xlsx"};

    private ValidadorAdjunto() {
    }

    public static String validarRutaOpcional(String ruta) throws ValidacionException {
        if (ruta == null || ruta.trim().isEmpty()) {
            return "Sin adjunto: permitido en el prototipo operacional.";
        }

        File archivo = new File(ruta.trim());

        if (!archivo.exists() || !archivo.isFile()) {
            throw new ValidacionException("El adjunto indicado no existe o no es un archivo valido.");
        }

        if (archivo.length() > TAMANO_MAXIMO_BYTES) {
            throw new ValidacionException("El adjunto supera el limite de 10 MB definido para el prototipo.");
        }

        String extension = obtenerExtension(archivo.getName());
        if (!esExtensionPermitida(extension)) {
            throw new ValidacionException("Extension de adjunto no permitida: " + extension + ".");
        }

        return archivo.getName() + " (" + archivo.length() + " bytes)";
    }

    private static boolean esExtensionPermitida(String extension) {
        for (String permitida : EXTENSIONES_PERMITIDAS) {
            if (permitida.equalsIgnoreCase(extension)) {
                return true;
            }
        }

        return false;
    }

    private static String obtenerExtension(String nombreArchivo) {
        int posicionPunto = nombreArchivo.lastIndexOf('.');

        if (posicionPunto < 0 || posicionPunto == nombreArchivo.length() - 1) {
            return "sin extension";
        }

        return nombreArchivo.substring(posicionPunto + 1).toLowerCase();
    }
}
