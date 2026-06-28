package dao;

import excepciones.PersistenciaException;
import java.util.List;
import modelo.BienCatalogo;
import modelo.SolicitudCompra;
import modelo.SolicitudResumen;
import modelo.UsuarioResumen;

public interface ISolicitudDAO {
    boolean registrarSolicitud(SolicitudCompra solicitud) throws PersistenciaException;

    List<SolicitudResumen> listarSolicitudesResumen() throws PersistenciaException;

    boolean actualizarEstado(int idSolicitud, String nuevoEstado) throws PersistenciaException;

    BienCatalogo buscarBienPorId(int idBien) throws PersistenciaException;

    boolean existeUsuario(int idUsuario) throws PersistenciaException;

    List<BienCatalogo> listarBienesCatalogo() throws PersistenciaException;

    List<UsuarioResumen> listarUsuariosSolicitantes() throws PersistenciaException;
}
