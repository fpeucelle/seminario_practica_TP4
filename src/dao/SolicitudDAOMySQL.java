package dao;

import excepciones.PersistenciaException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import modelo.BienCatalogo;
import modelo.DetalleSolicitud;
import modelo.SolicitudCompra;
import modelo.SolicitudResumen;
import modelo.UsuarioResumen;

public class SolicitudDAOMySQL implements ISolicitudDAO {
    private static final String URL = "jdbc:mysql://127.0.0.1:3306/sic_stj_db";
    private static final String USER = "root";
    private static final String PASSWORD = "Root";
    private static final String DB_NAME = "sic_stj_db";

    @Override
    public boolean registrarSolicitud(SolicitudCompra solicitud) throws PersistenciaException {
        String queryCabecera = "INSERT INTO solicitudes_compra "
                + "(id_usuario, fecha_solicitud, estado, justificacion) "
                + "VALUES (?, CURDATE(), ?, ?)";
        String queryDetalle = "INSERT INTO detalles_solicitud "
                + "(id_solicitud, id_bien, cantidad, precio_estimado) "
                + "VALUES (?, ?, ?, ?)";

        try (Connection con = obtenerConexion()) {
            con.setAutoCommit(false);

            try (PreparedStatement pstCabecera = con.prepareStatement(queryCabecera, Statement.RETURN_GENERATED_KEYS)) {
                pstCabecera.setInt(1, solicitud.getIdUsuario());
                pstCabecera.setString(2, solicitud.getEstado());
                pstCabecera.setString(3, solicitud.getJustificacion());

                int filasAfectadas = pstCabecera.executeUpdate();
                if (filasAfectadas == 0) {
                    con.rollback();
                    return false;
                }

                int idSolicitud = obtenerIdSolicitudGenerado(pstCabecera);
                solicitud.setIdSolicitud(idSolicitud);

                try (PreparedStatement pstDetalle = con.prepareStatement(queryDetalle)) {
                    for (DetalleSolicitud detalle : solicitud.getDetalles()) {
                        pstDetalle.setInt(1, idSolicitud);
                        pstDetalle.setInt(2, detalle.getBien().getIdBien());
                        pstDetalle.setInt(3, detalle.getCantidad());
                        pstDetalle.setDouble(4, detalle.getBien().getPrecioReferencia());
                        pstDetalle.addBatch();
                    }

                    pstDetalle.executeBatch();
                    imprimirAdvertencias(pstDetalle, "insertar detalles");
                }

                imprimirAdvertencias(pstCabecera, "insertar cabecera");
                imprimirAdvertencias(con, "registrar solicitud");
                con.commit();
                return true;
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new PersistenciaException("No se pudo registrar la solicitud en MySQL.", e);
        }
    }

    @Override
    public List<SolicitudResumen> listarSolicitudesResumen() throws PersistenciaException {
        String query = "SELECT "
                + "s.id_solicitud, "
                + "a.nombre AS dependencia, "
                + "u.nombre_completo AS solicitante, "
                + "s.fecha_solicitud, "
                + "s.estado, "
                + "COUNT(d.id_detalle) AS cantidad_items, "
                + "SUM(d.cantidad * d.precio_estimado) AS monto_total "
                + "FROM solicitudes_compra s "
                + "JOIN usuarios u ON s.id_usuario = u.id_usuario "
                + "JOIN areas a ON u.id_area = a.id_area "
                + "LEFT JOIN detalles_solicitud d ON s.id_solicitud = d.id_solicitud "
                + "GROUP BY s.id_solicitud, a.nombre, u.nombre_completo, s.fecha_solicitud, s.estado "
                + "ORDER BY s.id_solicitud DESC";

        ArrayList<SolicitudResumen> resumenes = new ArrayList<>();

        try (Connection con = obtenerConexion()) {
            con.setReadOnly(true);

            try (PreparedStatement pst = con.prepareStatement(query);
                    ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    resumenes.add(new SolicitudResumen(
                            rs.getInt("id_solicitud"),
                            rs.getString("dependencia"),
                            rs.getString("solicitante"),
                            rs.getDate("fecha_solicitud").toString(),
                            rs.getString("estado"),
                            rs.getInt("cantidad_items"),
                            rs.getDouble("monto_total")));
                }

                imprimirAdvertencias(pst, "listar solicitudes");
            }

            imprimirAdvertencias(con, "listar solicitudes");
            return resumenes;
        } catch (SQLException e) {
            throw new PersistenciaException("No se pudieron consultar las solicitudes en MySQL.", e);
        }
    }

    @Override
    public boolean actualizarEstado(int idSolicitud, String nuevoEstado) throws PersistenciaException {
        String query = "UPDATE solicitudes_compra SET estado = ? WHERE id_solicitud = ?";

        try (Connection con = obtenerConexion();
                PreparedStatement pst = con.prepareStatement(query)) {
            pst.setString(1, nuevoEstado);
            pst.setInt(2, idSolicitud);

            int filasAfectadas = pst.executeUpdate();
            imprimirAdvertencias(pst, "actualizar estado");
            imprimirAdvertencias(con, "actualizar estado");
            return filasAfectadas > 0;
        } catch (SQLException e) {
            throw new PersistenciaException("No se pudo actualizar el estado de la solicitud en MySQL.", e);
        }
    }

    @Override
    public boolean existeSolicitud(int idSolicitud) throws PersistenciaException {
        String query = "SELECT 1 FROM solicitudes_compra WHERE id_solicitud = ?";

        try (Connection con = obtenerConexion()) {
            con.setReadOnly(true);

            try (PreparedStatement pst = con.prepareStatement(query)) {
                pst.setInt(1, idSolicitud);

                try (ResultSet rs = pst.executeQuery()) {
                    return rs.next();
                }
            }
        } catch (SQLException e) {
            throw new PersistenciaException("No se pudo validar la solicitud en MySQL.", e);
        }
    }

    @Override
    public BienCatalogo buscarBienPorId(int idBien) throws PersistenciaException {
        String query = "SELECT id_bien, descripcion, precio_referencia "
                + "FROM bienes_catalogo WHERE id_bien = ?";

        try (Connection con = obtenerConexion()) {
            con.setReadOnly(true);

            try (PreparedStatement pst = con.prepareStatement(query)) {
                pst.setInt(1, idBien);

                try (ResultSet rs = pst.executeQuery()) {
                    if (rs.next()) {
                        return new BienCatalogo(
                                rs.getInt("id_bien"),
                                rs.getString("descripcion"),
                                rs.getDouble("precio_referencia"));
                    }
                }

                imprimirAdvertencias(pst, "buscar bien");
            }

            imprimirAdvertencias(con, "buscar bien");
            return null;
        } catch (SQLException e) {
            throw new PersistenciaException("No se pudo consultar el catalogo de bienes en MySQL.", e);
        }
    }

    @Override
    public boolean existeUsuario(int idUsuario) throws PersistenciaException {
        String query = "SELECT 1 FROM usuarios WHERE id_usuario = ?";

        try (Connection con = obtenerConexion()) {
            con.setReadOnly(true);

            try (PreparedStatement pst = con.prepareStatement(query)) {
                pst.setInt(1, idUsuario);

                try (ResultSet rs = pst.executeQuery()) {
                    return rs.next();
                }
            }
        } catch (SQLException e) {
            throw new PersistenciaException("No se pudo validar el usuario solicitante en MySQL.", e);
        }
    }

    @Override
    public List<BienCatalogo> listarBienesCatalogo() throws PersistenciaException {
        String query = "SELECT id_bien, descripcion, precio_referencia "
                + "FROM bienes_catalogo "
                + "ORDER BY id_bien";

        ArrayList<BienCatalogo> bienes = new ArrayList<>();

        try (Connection con = obtenerConexion()) {
            con.setReadOnly(true);

            try (PreparedStatement pst = con.prepareStatement(query);
                    ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    bienes.add(new BienCatalogo(
                            rs.getInt("id_bien"),
                            rs.getString("descripcion"),
                            rs.getDouble("precio_referencia")));
                }

                imprimirAdvertencias(pst, "listar bienes");
            }

            imprimirAdvertencias(con, "listar bienes");
            return bienes;
        } catch (SQLException e) {
            throw new PersistenciaException("No se pudo consultar el catalogo de bienes en MySQL.", e);
        }
    }

    @Override
    public List<UsuarioResumen> listarUsuariosSolicitantes() throws PersistenciaException {
        String query = "SELECT "
                + "u.id_usuario, "
                + "u.nombre_completo, "
                + "u.legajo, "
                + "u.rol, "
                + "a.nombre AS area "
                + "FROM usuarios u "
                + "JOIN areas a ON u.id_area = a.id_area "
                + "ORDER BY u.id_usuario";

        ArrayList<UsuarioResumen> usuarios = new ArrayList<>();

        try (Connection con = obtenerConexion()) {
            con.setReadOnly(true);

            try (PreparedStatement pst = con.prepareStatement(query);
                    ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    usuarios.add(new UsuarioResumen(
                            rs.getInt("id_usuario"),
                            rs.getString("nombre_completo"),
                            rs.getString("legajo"),
                            rs.getString("rol"),
                            rs.getString("area")));
                }

                imprimirAdvertencias(pst, "listar usuarios");
            }

            imprimirAdvertencias(con, "listar usuarios");
            return usuarios;
        } catch (SQLException e) {
            throw new PersistenciaException("No se pudieron consultar los usuarios solicitantes en MySQL.", e);
        }
    }

    private int obtenerIdSolicitudGenerado(PreparedStatement pst) throws SQLException {
        try (ResultSet rs = pst.getGeneratedKeys()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }

        throw new SQLException("No se pudo obtener el ID generado para la solicitud.");
    }

    private Connection obtenerConexion() throws SQLException {
        Connection con = DriverManager.getConnection(
                obtenerValorConfig("SIC_DB_URL", URL),
                obtenerValorConfig("SIC_DB_USER", USER),
                obtenerValorConfig("SIC_DB_PASSWORD", PASSWORD));

        if (con.isClosed() || !con.isValid(2)) {
            throw new SQLException("La conexion MySQL no se encuentra disponible.");
        }

        con.setCatalog(DB_NAME);
        return con;
    }

    private String obtenerValorConfig(String variable, String valorPorDefecto) {
        String valor = System.getenv(variable);
        if (valor == null || valor.trim().isEmpty()) {
            return valorPorDefecto;
        }

        return valor;
    }

    private void imprimirAdvertencias(Connection con, String contexto) throws SQLException {
        imprimirAdvertencias(con.getWarnings(), contexto);
        con.clearWarnings();
    }

    private void imprimirAdvertencias(Statement statement, String contexto) throws SQLException {
        imprimirAdvertencias(statement.getWarnings(), contexto);
        statement.clearWarnings();
    }

    private void imprimirAdvertencias(SQLWarning advertencia, String contexto) {
        SQLWarning actual = advertencia;
        while (actual != null) {
            System.err.println("Advertencia SQL (" + contexto + "): " + actual.getMessage());
            actual = actual.getNextWarning();
        }
    }
}
