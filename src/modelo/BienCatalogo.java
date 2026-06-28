package modelo;

public class BienCatalogo {
    private final int idBien;
    private final String descripcion;
    private final double precioReferencia;

    public BienCatalogo(int idBien, String descripcion, double precioReferencia) {
        this.idBien = idBien;
        this.descripcion = descripcion;
        this.precioReferencia = precioReferencia;
    }

    public int getIdBien() {
        return idBien;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public double getPrecioReferencia() {
        return precioReferencia;
    }
}
