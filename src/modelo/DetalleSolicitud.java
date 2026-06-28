package modelo;

import excepciones.ValidacionException;

public class DetalleSolicitud {
    private final BienCatalogo bien;
    private final int cantidad;

    public DetalleSolicitud(BienCatalogo bien, int cantidad) throws ValidacionException {
        if (bien == null) {
            throw new ValidacionException("El bien del catalogo no puede ser nulo.");
        }

        if (cantidad <= 0) {
            throw new ValidacionException("La cantidad debe ser mayor a 0.");
        }

        this.bien = bien;
        this.cantidad = cantidad;
    }

    public BienCatalogo getBien() {
        return bien;
    }

    public int getCantidad() {
        return cantidad;
    }

    public double calcularSubtotal() {
        return bien.getPrecioReferencia() * cantidad;
    }

    public String getResumen() {
        return cantidad + "x " + bien.getDescripcion() + " - Subtotal: $" + calcularSubtotal();
    }
}
