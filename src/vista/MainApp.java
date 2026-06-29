package vista;

import controlador.SolicitudController;
import dao.ISolicitudDAO;
import dao.SolicitudDAOMySQL;
import excepciones.OperacionCanceladaException;
import excepciones.PersistenciaException;
import excepciones.ValidacionException;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;
import modelo.BienCatalogo;
import modelo.DetalleSolicitud;
import modelo.SolicitudCompra;
import modelo.SolicitudOrdinaria;
import modelo.SolicitudResumen;
import modelo.SolicitudUrgente;
import modelo.UsuarioResumen;

public class MainApp {
    private static final String COMANDO_CANCELAR = "X";

    public static void main(String[] args) {
        System.out.println("=== SIC-STJ: Sistema Integral de Compras ===");
        System.out.println("--- Prototipo Operacional TP4 - MVC, MySQL, archivos y colecciones ---");
        System.out.println("Modo de persistencia: MySQL");

        ISolicitudDAO dao = new SolicitudDAOMySQL();
        SolicitudController controlador = new SolicitudController(dao);

        try (Scanner scanner = new Scanner(System.in)) {
            mostrarMenu(controlador, scanner);
        } catch (RuntimeException e) {
            System.err.println("Error Critico del Sistema: " + e.getMessage());
        } finally {
            System.out.println("--- Fin de la ejecucion del Prototipo ---");
        }
    }

    private static void mostrarMenu(SolicitudController controlador, Scanner scanner) {
        int opcion;

        do {
            System.out.println();
            System.out.println("Menu de seleccion");
            System.out.println("1. Registrar solicitud de compra");
            System.out.println("2. Actualizar estado de solicitud");
            System.out.println("3. Listar solicitudes");
            System.out.println("4. Listar bienes del catalogo");
            System.out.println("5. Listar usuarios solicitantes");
            System.out.println("6. Ver bitacora de auditoria");
            System.out.println("0. Salir");
            System.out.println("Ayuda: Siempre tiene la opcion de ingresar X para cancelar una operacion");
            System.out.print("Seleccione una opcion: ");

            opcion = leerOpcionMenu(scanner);
            if (opcion == -1) {
                continue;
            }

            try {
                switch (opcion) {
                    case 1:
                        ejecutarRegistro(controlador, scanner);
                        break;
                    case 2:
                        actualizarEstado(controlador, scanner);
                        break;
                    case 3:
                        listarSolicitudes(controlador);
                        break;
                    case 4:
                        listarBienes(controlador);
                        break;
                    case 5:
                        listarUsuarios(controlador);
                        break;
                    case 6:
                        mostrarBitacora(controlador);
                        break;
                    case 0:
                        System.out.println("Saliendo del prototipo.");
                        break;
                    default:
                        System.out.println("Opcion invalida. Intente nuevamente.");
                        break;
                }
            } catch (ValidacionException e) {
                System.err.println("Error de Validacion (Regla de Negocio): " + e.getMessage());
                controlador.registrarErrorValidacion(e.getMessage());
            } catch (PersistenciaException e) {
                System.err.println("Error de Persistencia/MySQL: " + e.getMessage());
                controlador.registrarErrorPersistencia(e.getMessage());
            } catch (IOException e) {
                System.err.println("Error de Archivos/Bitacora: " + e.getMessage());
            } catch (OperacionCanceladaException e) {
                System.out.println("Operacion cancelada. Volviendo al menu principal.");
                controlador.registrarCancelacion("Operacion cancelada desde la vista.");
            }
        } while (opcion != 0);
    }

    private static void ejecutarRegistro(
            SolicitudController controlador,
            Scanner scanner) throws ValidacionException, PersistenciaException {
        SolicitudCompra nuevaSolicitud = seleccionarTipoSolicitud(scanner);
        nuevaSolicitud.setIdUsuario(leerIdUsuario(controlador, scanner));
        nuevaSolicitud.setJustificacion(leerTextoObligatorio(
                scanner,
                "Ingrese justificacion de la solicitud: "));

        System.out.println("Carga manual de items de la solicitud...");
        cargarItemsManual(controlador, scanner, nuevaSolicitud);

        String rutaAdjunto = leerRutaAdjunto(controlador, scanner);
        imprimirResumen(nuevaSolicitud);

        System.out.println();
        boolean confirmar = leerConfirmacionObligatoria(scanner, "Confirmar registro de la solicitud? (S/N): ");
        if (!confirmar) {
            System.out.println("Registro cancelado por el usuario.");
            return;
        }

        controlador.procesarGuardado(nuevaSolicitud, rutaAdjunto);
        System.out.println("Estado final: " + nuevaSolicitud.getEstado());
    }

    private static SolicitudCompra seleccionarTipoSolicitud(Scanner scanner) {
        while (true) {
            System.out.println();
            System.out.println("Tipo de solicitud:");
            System.out.println("1. Ordinaria");
            System.out.println("2. Urgente");
            int opcion = leerEnteroObligatorio(scanner, "Seleccione tipo de solicitud: ");

            switch (opcion) {
                case 1:
                    return new SolicitudOrdinaria();
                case 2:
                    return new SolicitudUrgente();
                default:
                    System.out.println("Tipo de solicitud invalido. Intente nuevamente.");
                    break;
            }
        }
    }

    private static int leerIdUsuario(SolicitudController controlador, Scanner scanner)
            throws PersistenciaException {
        while (true) {
            int idUsuario = leerEnteroObligatorio(
                    scanner,
                    "Ingrese ID de usuario solicitante (0 para ver usuarios): ");

            if (idUsuario == 0) {
                listarUsuarios(controlador);
                continue;
            }

            try {
                controlador.validarUsuarioSolicitante(idUsuario);
                return idUsuario;
            } catch (ValidacionException e) {
                System.out.println("Error de Validacion (Regla de Negocio): " + e.getMessage());
                controlador.registrarErrorValidacion(e.getMessage());
            }
        }
    }

    private static void cargarItemsManual(
            SolicitudController controlador,
            Scanner scanner,
            SolicitudCompra solicitud) throws ValidacionException, PersistenciaException {
        boolean continuar = false;

        do {
            DetalleSolicitud detalle = leerDetalleSolicitud(controlador, scanner);
            solicitud.agregarDetalle(detalle);
            BienCatalogo bien = detalle.getBien();
            int cantidad = detalle.getCantidad();
            System.out.println("Item agregado: " + cantidad + "x " + bien.getDescripcion());

            continuar = leerConfirmacionObligatoria(scanner, "Agregar otro item? (S/N): ");
        } while (continuar);
    }

    private static DetalleSolicitud leerDetalleSolicitud(SolicitudController controlador, Scanner scanner)
            throws PersistenciaException {
        while (true) {
            BienCatalogo bien = leerBienHomologado(controlador, scanner);
            int cantidad = leerEnteroObligatorio(scanner, "Ingrese cantidad: ");

            try {
                return new DetalleSolicitud(bien, cantidad);
            } catch (ValidacionException e) {
                System.out.println("Error de Validacion (Regla de Negocio): " + e.getMessage());
                controlador.registrarErrorValidacion(e.getMessage());
            }
        }
    }

    private static BienCatalogo leerBienHomologado(SolicitudController controlador, Scanner scanner)
            throws PersistenciaException {
        while (true) {
            int idBien = leerEnteroObligatorio(
                    scanner,
                    "Ingrese ID de bien homologado (0 para ver catalogo): ");
            if (idBien == 0) {
                listarBienes(controlador);
                continue;
            }

            try {
                return controlador.buscarBienPorId(idBien);
            } catch (ValidacionException e) {
                System.out.println("Error de Validacion (Regla de Negocio): " + e.getMessage());
                controlador.registrarErrorValidacion(e.getMessage());
            }
        }
    }

    private static void listarSolicitudes(SolicitudController controlador) throws PersistenciaException {
        List<SolicitudResumen> solicitudes = controlador.listarSolicitudes();

        if (solicitudes.isEmpty()) {
            System.out.println("No hay solicitudes registradas para mostrar.");
            return;
        }

        System.out.println();
        System.out.println("Reporte integral de solicitudes:");
        for (SolicitudResumen solicitud : solicitudes) {
            System.out.println("- " + solicitud.getResumen());
        }
    }

    private static void listarBienes(SolicitudController controlador) throws PersistenciaException {
        List<BienCatalogo> bienes = controlador.listarBienesCatalogo();

        if (bienes.isEmpty()) {
            System.out.println("No hay bienes homologados para mostrar.");
            return;
        }

        System.out.println();
        System.out.println("Catalogo de bienes homologados:");
        for (BienCatalogo bien : bienes) {
            System.out.println("- ID " + bien.getIdBien()
                    + " | " + bien.getDescripcion()
                    + " | Precio ref.: $" + bien.getPrecioReferencia());
        }
    }

    private static void listarUsuarios(SolicitudController controlador) throws PersistenciaException {
        List<UsuarioResumen> usuarios = controlador.listarUsuariosSolicitantes();

        if (usuarios.isEmpty()) {
            System.out.println("No hay usuarios solicitantes para mostrar.");
            return;
        }

        System.out.println();
        System.out.println("Usuarios solicitantes registrados:");
        for (UsuarioResumen usuario : usuarios) {
            System.out.println("- " + usuario.getResumen());
        }
    }

    private static void actualizarEstado(SolicitudController controlador, Scanner scanner)
            throws ValidacionException, PersistenciaException {
        String[] estados = controlador.obtenerEstadosPermitidos();

        int idSolicitud = leerIdSolicitudParaActualizar(controlador, scanner);
        int opcionEstado = leerOpcionEstado(scanner, estados);
        String nuevoEstado = estados[opcionEstado - 1];
        boolean confirmar = leerConfirmacionObligatoria(
                scanner,
                "Confirmar cambio de estado a '" + nuevoEstado + "'? (S/N): ");
        if (!confirmar) {
            System.out.println("Actualizacion cancelada por el usuario.");
            return;
        }

        boolean actualizado = controlador.actualizarEstado(idSolicitud, nuevoEstado);
        if (actualizado) {
            System.out.println(">> EXITO: Estado actualizado correctamente.");
        }
    }

    private static int leerIdSolicitudParaActualizar(SolicitudController controlador, Scanner scanner)
            throws PersistenciaException {
        while (true) {
            int idSolicitud = leerEnteroObligatorio(
                    scanner,
                    "Ingrese ID de solicitud (0 para ver solicitudes): ");
            if (idSolicitud == 0) {
                listarSolicitudes(controlador);
                continue;
            }

            try {
                controlador.validarSolicitudExistente(idSolicitud);
                return idSolicitud;
            } catch (ValidacionException e) {
                System.out.println("Error de Validacion (Regla de Negocio): " + e.getMessage());
                controlador.registrarErrorValidacion(e.getMessage());
            }
        }
    }

    private static void mostrarBitacora(SolicitudController controlador) throws IOException {
        List<String> eventos = controlador.leerBitacora();

        System.out.println();
        System.out.println("Bitacora local: " + controlador.obtenerRutaBitacora());

        if (eventos.isEmpty()) {
            System.out.println("No hay eventos registrados.");
            return;
        }

        for (String evento : eventos) {
            System.out.println("- " + evento);
        }
    }

    private static int leerOpcionMenu(Scanner scanner) {
        String entrada = scanner.nextLine().trim();

        if (entrada.isEmpty()) {
            System.out.println("Opcion invalida. Debe ingresar un numero del menu.");
            return -1;
        }

        try {
            return Integer.parseInt(entrada);
        } catch (NumberFormatException e) {
            System.out.println("Opcion invalida. Debe ingresar un numero del menu.");
            return -1;
        }
    }

    private static int leerEnteroObligatorio(Scanner scanner, String mensaje) {
        while (true) {
            System.out.print(mensaje);
            String entrada = scanner.nextLine().trim();
            cancelarSiCorresponde(entrada);

            if (entrada.isEmpty()) {
                System.out.println("Valor obligatorio. Ingrese un numero.");
                continue;
            }

            try {
                return Integer.parseInt(entrada);
            } catch (NumberFormatException e) {
                System.out.println("Valor invalido. Ingrese un numero.");
            }
        }
    }

    private static String leerTexto(Scanner scanner, String mensaje) {
        System.out.print(mensaje);
        return scanner.nextLine();
    }

    private static String leerRutaAdjunto(SolicitudController controlador, Scanner scanner) {
        while (true) {
            String rutaAdjunto = leerTexto(scanner, "Ruta de adjunto opcional [sin adjunto]: ");
            cancelarSiCorresponde(rutaAdjunto);

            try {
                controlador.validarAdjuntoOpcional(rutaAdjunto);
                return rutaAdjunto;
            } catch (ValidacionException e) {
                System.out.println("Error de Validacion (Regla de Negocio): " + e.getMessage());
                controlador.registrarErrorValidacion(e.getMessage());
            }
        }
    }

    private static int leerOpcionEstado(Scanner scanner, String[] estados) {
        while (true) {
            int opcion = leerEnteroObligatorio(
                    scanner,
                    "Seleccione nuevo estado (0 para ver estados): ");

            if (opcion == 0) {
                mostrarEstadosPermitidos(estados);
                continue;
            }

            if (opcion >= 1 && opcion <= estados.length) {
                return opcion;
            }

            System.out.println("Opcion de estado invalida. Intente nuevamente.");
        }
    }

    private static void mostrarEstadosPermitidos(String[] estados) {
        System.out.println();
        System.out.println("Estados permitidos:");
        for (int i = 0; i < estados.length; i++) {
            System.out.println((i + 1) + ". " + estados[i]);
        }
    }

    private static String leerTextoObligatorio(Scanner scanner, String mensaje) {
        while (true) {
            System.out.print(mensaje);
            String entrada = scanner.nextLine().trim();
            cancelarSiCorresponde(entrada);

            if (!entrada.isEmpty()) {
                return entrada;
            }

            System.out.println("Valor obligatorio. Ingrese un texto.");
        }
    }

    private static boolean esRespuestaAfirmativa(String respuesta) {
        return "s".equalsIgnoreCase(respuesta)
                || "si".equalsIgnoreCase(respuesta)
                || "y".equalsIgnoreCase(respuesta)
                || "yes".equalsIgnoreCase(respuesta);
    }

    private static boolean leerConfirmacionObligatoria(Scanner scanner, String mensaje) {
        while (true) {
            String respuesta = leerTexto(scanner, mensaje).trim();
            cancelarSiCorresponde(respuesta);

            if (esRespuestaAfirmativa(respuesta)) {
                return true;
            }

            if ("n".equalsIgnoreCase(respuesta) || "no".equalsIgnoreCase(respuesta)) {
                return false;
            }

            System.out.println("Respuesta invalida. Ingrese S o N.");
        }
    }

    private static void imprimirResumen(SolicitudCompra solicitud) {
        System.out.println();
        System.out.println("Resumen de Items:");

        for (DetalleSolicitud detalle : solicitud.getDetalles()) {
            System.out.println("- " + detalle.getResumen());
        }

        System.out.println();
        System.out.println("Monto Total Estimado: $" + solicitud.calcularMontoTotal());
        System.out.println("SLA de Resolucion: " + solicitud.calcularPlazoSLA() + " dias.");
    }

    private static void cancelarSiCorresponde(String entrada) {
        if (COMANDO_CANCELAR.equalsIgnoreCase(entrada.trim())) {
            throw new OperacionCanceladaException();
        }
    }
}
