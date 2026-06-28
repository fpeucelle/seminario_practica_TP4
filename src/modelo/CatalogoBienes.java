package modelo;

import java.util.Arrays;

public class CatalogoBienes {
    private static final BienCatalogo[] BIENES = {
        new BienCatalogo(1, "Resmas Papel A4 80gr", 6500.00),
        new BienCatalogo(2, "Toner alternativo para impresora Brother", 25000.00),
        new BienCatalogo(3, "Boligrafos trazo fino caja x50", 8000.00),
        new BienCatalogo(4, "Notebook Core i7 16GB RAM", 850000.00),
        new BienCatalogo(5, "Proyector HD para sala de reuniones", 320000.00)
    };

    private CatalogoBienes() {
    }

    public static BienCatalogo[] obtenerTodos() {
        return Arrays.copyOf(BIENES, BIENES.length);
    }

    public static BienCatalogo buscarPorId(int idBien) {
        int izquierda = 0;
        int derecha = BIENES.length - 1;

        while (izquierda <= derecha) {
            int medio = izquierda + (derecha - izquierda) / 2;
            int idMedio = BIENES[medio].getIdBien();

            if (idMedio == idBien) {
                return BIENES[medio];
            }

            if (idMedio < idBien) {
                izquierda = medio + 1;
            } else {
                derecha = medio - 1;
            }
        }

        return null;
    }
}
