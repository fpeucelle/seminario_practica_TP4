package modelo;

public class SolicitudOrdinaria extends SolicitudCompra {
    @Override
    public int calcularPlazoSLA() {
        return 15;
    }
}
