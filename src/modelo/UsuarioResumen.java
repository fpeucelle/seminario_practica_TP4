package modelo;

public class UsuarioResumen {
    private final int idUsuario;
    private final String nombreCompleto;
    private final String legajo;
    private final String rol;
    private final String area;

    public UsuarioResumen(int idUsuario, String nombreCompleto, String legajo, String rol, String area) {
        this.idUsuario = idUsuario;
        this.nombreCompleto = nombreCompleto;
        this.legajo = legajo;
        this.rol = rol;
        this.area = area;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public String getLegajo() {
        return legajo;
    }

    public String getRol() {
        return rol;
    }

    public String getArea() {
        return area;
    }

    public String getResumen() {
        return "Usuario #" + idUsuario
                + " | " + nombreCompleto
                + " | Legajo: " + legajo
                + " | Rol: " + rol
                + " | Area: " + area;
    }
}
