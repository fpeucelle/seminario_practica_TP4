package controlador;

import dao.ISolicitudDAO;
import excepciones.PersistenciaException;
import excepciones.ValidacionException;
import java.io.IOException;
import java.util.List;
import modelo.BienCatalogo;
import modelo.SolicitudCompra;
import modelo.SolicitudResumen;
import modelo.UsuarioResumen;
import servicio.BitacoraArchivo;
import servicio.INotificador;
import servicio.NotificadorConsola;
import servicio.ValidadorAdjunto;

public class SolicitudController {
    private final ISolicitudDAO dao;
    private final BitacoraArchivo bitacora;
    private final INotificador notificador;
    private static final String[] ESTADOS_PERMITIDOS = {
        "Borrador",
        "Pendiente de autorizacion",
        "Enviada",
        "Autorizada",
        "Rechazada",
        "Observada"
    };

    public SolicitudController(ISolicitudDAO dao) {
        this(dao, new BitacoraArchivo());
    }

    public SolicitudController(ISolicitudDAO dao, BitacoraArchivo bitacora) {
        this.dao = dao;
        this.bitacora = bitacora;
        this.notificador = new NotificadorConsola(bitacora);
    }

    public void procesarGuardado(SolicitudCompra solicitud) throws ValidacionException, PersistenciaException {
        procesarGuardado(solicitud, "");
    }

    public void procesarGuardado(SolicitudCompra solicitud, String rutaAdjunto)
            throws ValidacionException, PersistenciaException {
        if (solicitud == null) {
            throw new ValidacionException("La solicitud no puede ser nula.");
        }

        if (solicitud.getDetalles().isEmpty()) {
            throw new ValidacionException("No se puede registrar una solicitud vacia. Ingrese items.");
        }

        if (!dao.existeUsuario(solicitud.getIdUsuario())) {
            throw new ValidacionException("El usuario solicitante no existe.");
        }

        if (solicitud.getJustificacion() == null || solicitud.getJustificacion().trim().isEmpty()) {
            throw new ValidacionException("La justificacion de la solicitud es obligatoria.");
        }

        if (solicitud.calcularMontoTotal() <= 0) {
            throw new ValidacionException("El monto de la solicitud debe ser mayor a 0.");
        }

        String adjuntoValidado = ValidadorAdjunto.validarRutaOpcional(rutaAdjunto);
        solicitud.setEstado("Pendiente de autorizacion");

        boolean exito = dao.registrarSolicitud(solicitud);

        if (exito) {
            System.out.println(">> EXITO: Solicitud persistida correctamente.");
            System.out.println(">> ID generado: " + solicitud.getIdSolicitud());
            registrarEventoSeguro(
                    "ALTA",
                    "Solicitud " + solicitud.getIdSolicitud()
                            + " usuario " + solicitud.getIdUsuario()
                            + " total $" + solicitud.calcularMontoTotal()
                            + " adjunto: " + adjuntoValidado);
            notificarSeguro(
                    "Autoridad autorizante",
                    "Solicitud " + solicitud.getIdSolicitud() + " pendiente de autorizacion.");
        } else {
            System.out.println(">> AVISO: No se pudo confirmar el alta de la solicitud.");
            registrarEventoSeguro("ALTA_AVISO", "DAO no confirmo el registro de la solicitud.");
        }
    }

    public List<SolicitudResumen> listarSolicitudes() throws PersistenciaException {
        List<SolicitudResumen> solicitudes = dao.listarSolicitudesResumen();
        registrarEventoSeguro("CONSULTA", "Listado de solicitudes. Registros: " + solicitudes.size());
        return solicitudes;
    }

    public List<BienCatalogo> listarBienesCatalogo() throws PersistenciaException {
        List<BienCatalogo> bienes = dao.listarBienesCatalogo();
        registrarEventoSeguro("CONSULTA", "Listado de bienes. Registros: " + bienes.size());
        return bienes;
    }

    public List<UsuarioResumen> listarUsuariosSolicitantes() throws PersistenciaException {
        List<UsuarioResumen> usuarios = dao.listarUsuariosSolicitantes();
        registrarEventoSeguro("CONSULTA", "Listado de usuarios. Registros: " + usuarios.size());
        return usuarios;
    }

    public boolean actualizarEstado(int idSolicitud, String nuevoEstado)
            throws ValidacionException, PersistenciaException {
        if (idSolicitud <= 0) {
            throw new ValidacionException("El ID de solicitud debe ser mayor a 0.");
        }

        if (!estadoPermitido(nuevoEstado)) {
            throw new ValidacionException("Estado no permitido para el prototipo: " + nuevoEstado + ".");
        }

        boolean actualizado = dao.actualizarEstado(idSolicitud, nuevoEstado);
        if (actualizado) {
            registrarEventoSeguro("ACTUALIZACION", "Solicitud " + idSolicitud + " -> " + nuevoEstado);
            notificarSeguro(
                    "Solicitante interno",
                    "La solicitud " + idSolicitud + " cambio a estado " + nuevoEstado + ".");
        } else {
            registrarEventoSeguro("ACTUALIZACION_AVISO", "Solicitud no encontrada: " + idSolicitud);
        }

        return actualizado;
    }

    public void validarSolicitudExistente(int idSolicitud) throws ValidacionException, PersistenciaException {
        if (idSolicitud <= 0) {
            throw new ValidacionException("El ID de solicitud debe ser mayor a 0.");
        }

        if (!dao.existeSolicitud(idSolicitud)) {
            throw new ValidacionException("La solicitud indicada no existe.");
        }
    }

    public BienCatalogo buscarBienPorId(int idBien) throws ValidacionException, PersistenciaException {
        if (idBien <= 0) {
            throw new ValidacionException("El ID del bien debe ser mayor a 0.");
        }

        BienCatalogo bien = dao.buscarBienPorId(idBien);
        if (bien == null) {
            throw new ValidacionException("El bien solicitado no existe en el catalogo homologado.");
        }

        return bien;
    }

    public void validarUsuarioSolicitante(int idUsuario) throws ValidacionException, PersistenciaException {
        if (idUsuario <= 0) {
            throw new ValidacionException("El ID de usuario debe ser mayor a 0.");
        }

        if (!dao.existeUsuario(idUsuario)) {
            throw new ValidacionException("El usuario solicitante no existe.");
        }
    }

    public String validarAdjuntoOpcional(String rutaAdjunto) throws ValidacionException {
        return ValidadorAdjunto.validarRutaOpcional(rutaAdjunto);
    }

    public List<String> leerBitacora() throws IOException {
        return bitacora.leerEventos();
    }

    public String obtenerRutaBitacora() {
        return bitacora.getRutaArchivo();
    }

    public void registrarErrorValidacion(String mensaje) {
        registrarEventoSeguro("ERROR_VALIDACION", mensaje);
    }

    public void registrarErrorPersistencia(String mensaje) {
        registrarEventoSeguro("ERROR_MYSQL", mensaje);
    }

    public void registrarCancelacion(String mensaje) {
        registrarEventoSeguro("CANCELACION", mensaje);
    }

    public String[] obtenerEstadosPermitidos() {
        String[] copia = new String[ESTADOS_PERMITIDOS.length];
        for (int i = 0; i < ESTADOS_PERMITIDOS.length; i++) {
            copia[i] = ESTADOS_PERMITIDOS[i];
        }

        return copia;
    }

    private boolean estadoPermitido(String estado) {
        if (estado == null || estado.trim().isEmpty()) {
            return false;
        }

        for (String permitido : ESTADOS_PERMITIDOS) {
            if (permitido.equalsIgnoreCase(estado.trim())) {
                return true;
            }
        }

        return false;
    }

    private void registrarEventoSeguro(String tipo, String detalle) {
        try {
            bitacora.registrarEvento(tipo, detalle);
        } catch (IOException e) {
            System.err.println("Aviso: no se pudo escribir la bitacora: " + e.getMessage());
        }
    }

    private void notificarSeguro(String destinatario, String mensaje) {
        try {
            notificador.notificar(destinatario, mensaje);
        } catch (IOException e) {
            System.err.println("Aviso: no se pudo registrar la notificacion: " + e.getMessage());
        }
    }
}
