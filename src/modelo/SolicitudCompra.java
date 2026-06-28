package modelo;

import excepciones.ValidacionException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class SolicitudCompra {
    protected int idSolicitud;
    protected int idUsuario;
    protected String estado;
    protected String justificacion;
    protected final ArrayList<DetalleSolicitud> detalles;

    public SolicitudCompra() {
        this.estado = "Borrador";
        this.detalles = new ArrayList<>();
        this.justificacion = "";
    }

    public void agregarDetalle(DetalleSolicitud detalle) throws ValidacionException {
        if (detalle == null) {
            throw new ValidacionException("El item no puede ser nulo.");
        }

        detalles.add(detalle);
    }

    public double calcularMontoTotal() {
        double total = 0;

        for (DetalleSolicitud detalle : detalles) {
            total += detalle.calcularSubtotal();
        }

        return total;
    }

    public abstract int calcularPlazoSLA();

    public int getIdSolicitud() {
        return idSolicitud;
    }

    public void setIdSolicitud(int idSolicitud) {
        this.idSolicitud = idSolicitud;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getJustificacion() {
        return justificacion;
    }

    public void setJustificacion(String justificacion) {
        this.justificacion = justificacion;
    }

    public List<DetalleSolicitud> getDetalles() {
        return Collections.unmodifiableList(detalles);
    }
}
