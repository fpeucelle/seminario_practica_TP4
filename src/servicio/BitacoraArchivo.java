package servicio;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class BitacoraArchivo {
    private final File archivo;

    public BitacoraArchivo() {
        this.archivo = new File("data", "bitacora_solicitudes.csv");
    }

    public void registrarEvento(String tipo, String detalle) throws IOException {
        File directorio = archivo.getParentFile();
        if (directorio != null && !directorio.exists()) {
            directorio.mkdirs();
        }

        try (FileOutputStream salida = new FileOutputStream(archivo, true);
                PrintStream escritor = new PrintStream(salida, true, StandardCharsets.UTF_8)) {
            escritor.println(LocalDateTime.now() + ";" + limpiar(tipo) + ";" + limpiar(detalle));
        }
    }

    public List<String> leerEventos() throws IOException {
        List<String> eventos = new ArrayList<>();

        if (!archivo.exists()) {
            return eventos;
        }

        try (FileInputStream entrada = new FileInputStream(archivo);
                InputStreamReader lectorBytes = new InputStreamReader(entrada, StandardCharsets.UTF_8);
                BufferedReader lector = new BufferedReader(lectorBytes)) {
            String linea;
            while ((linea = lector.readLine()) != null) {
                eventos.add(linea);
            }
        }

        return eventos;
    }

    public String getRutaArchivo() {
        return archivo.getPath();
    }

    private String limpiar(String texto) {
        if (texto == null) {
            return "";
        }

        return texto.replace(";", ",").replace(System.lineSeparator(), " ");
    }
}
