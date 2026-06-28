package modelo;

public class SolicitudUrgente extends SolicitudCompra {
    @Override
    public int calcularPlazoSLA() {
        return 3;
    }
}
