package modelo;

public class SolicitudResumen {
    private final int idSolicitud;
    private final String dependenciaSolicitante;
    private final String solicitante;
    private final String fechaSolicitud;
    private final String estado;
    private final int cantidadItems;
    private final double montoTotal;

    public SolicitudResumen(
            int idSolicitud,
            String dependenciaSolicitante,
            String solicitante,
            String fechaSolicitud,
            String estado,
            int cantidadItems,
            double montoTotal) {
        this.idSolicitud = idSolicitud;
        this.dependenciaSolicitante = dependenciaSolicitante;
        this.solicitante = solicitante;
        this.fechaSolicitud = fechaSolicitud;
        this.estado = estado;
        this.cantidadItems = cantidadItems;
        this.montoTotal = montoTotal;
    }

    public int getIdSolicitud() {
        return idSolicitud;
    }

    public String getDependenciaSolicitante() {
        return dependenciaSolicitante;
    }

    public String getSolicitante() {
        return solicitante;
    }

    public String getFechaSolicitud() {
        return fechaSolicitud;
    }

    public String getEstado() {
        return estado;
    }

    public int getCantidadItems() {
        return cantidadItems;
    }

    public double getMontoTotal() {
        return montoTotal;
    }

    public String getResumen() {
        return "Solicitud #" + idSolicitud
                + " | " + fechaSolicitud
                + " | " + dependenciaSolicitante
                + " | " + solicitante
                + " | Estado: " + estado
                + " | Items: " + cantidadItems
                + " | Total: $" + montoTotal;
    }
}
