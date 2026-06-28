CREATE DATABASE IF NOT EXISTS sic_stj_db;
USE sic_stj_db;

CREATE TABLE areas (
    id_area INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    ubicacion VARCHAR(100)
);

CREATE TABLE usuarios (
    id_usuario INT AUTO_INCREMENT PRIMARY KEY,
    id_area INT NOT NULL,
    nombre_completo VARCHAR(150) NOT NULL,
    legajo VARCHAR(20) UNIQUE NOT NULL,
    rol VARCHAR(50) NOT NULL,
    CONSTRAINT fk_usuario_area FOREIGN KEY (id_area) REFERENCES areas(id_area)
);

CREATE TABLE bienes_catalogo (
    id_bien INT AUTO_INCREMENT PRIMARY KEY,
    descripcion VARCHAR(200) NOT NULL,
    precio_referencia DECIMAL(10, 2) NOT NULL
);

CREATE TABLE solicitudes_compra (
    id_solicitud INT AUTO_INCREMENT PRIMARY KEY,
    id_usuario INT NOT NULL,
    fecha_solicitud DATE NOT NULL,
    estado VARCHAR(30) NOT NULL DEFAULT 'Borrador',
    justificacion TEXT NOT NULL,
    CONSTRAINT fk_solicitud_usuario FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario)
);

CREATE TABLE detalles_solicitud (
    id_detalle INT AUTO_INCREMENT PRIMARY KEY,
    id_solicitud INT NOT NULL,
    id_bien INT NOT NULL,
    cantidad INT NOT NULL,
    precio_estimado DECIMAL(10, 2) NOT NULL,
    CONSTRAINT fk_detalle_solicitud
        FOREIGN KEY (id_solicitud)
        REFERENCES solicitudes_compra(id_solicitud)
        ON DELETE CASCADE,
    CONSTRAINT fk_detalle_bien
        FOREIGN KEY (id_bien)
        REFERENCES bienes_catalogo(id_bien)
);
