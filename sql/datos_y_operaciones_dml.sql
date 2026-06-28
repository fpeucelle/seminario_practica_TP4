USE sic_stj_db;

INSERT INTO areas (nombre, ubicacion) VALUES
('Direccion de Administracion', 'Rawson - Edificio Principal'),
('Juzgado Civil y Comercial Nro 1', 'Trelew - Anexo I'),
('Oficina Judicial Penal', 'Comodoro Rivadavia');

INSERT INTO usuarios (id_area, nombre_completo, legajo, rol) VALUES
(1, 'Laura Gomez', 'LEG-4501', 'Administrativo'),
(2, 'Carlos Ruiz', 'LEG-3220', 'Secretario'),
(3, 'Mariana Vega', 'LEG-8911', 'Director');

INSERT INTO bienes_catalogo (descripcion, precio_referencia) VALUES
('Resmas Papel A4 80gr', 6500.00),
('Toner alternativo para impresora Brother', 25000.00),
('Boligrafos trazo fino caja x50', 8000.00),
('Notebook Core i7 16GB RAM', 850000.00),
('Proyector HD para sala de reuniones', 320000.00);

INSERT INTO solicitudes_compra (id_usuario, fecha_solicitud, estado, justificacion) VALUES
(2, '2026-05-10', 'Enviada', 'Renovacion trimestral de insumos de libreria para proveidos diarios.'),
(3, '2026-05-12', 'Borrador', 'Actualizacion de equipamiento informatico para salas de audiencias.');

INSERT INTO detalles_solicitud (id_solicitud, id_bien, cantidad, precio_estimado) VALUES
(1, 1, 50, 6500.00),
(1, 2, 4, 25000.00),
(1, 3, 2, 8000.00);

INSERT INTO detalles_solicitud (id_solicitud, id_bien, cantidad, precio_estimado) VALUES
(2, 4, 2, 850000.00),
(2, 5, 1, 320000.00);

SELECT
    s.id_solicitud AS 'Nro Solicitud',
    a.nombre AS 'Dependencia Solicitante',
    u.nombre_completo AS 'Solicitante',
    s.fecha_solicitud AS 'Fecha',
    s.estado AS 'Estado',
    COUNT(d.id_detalle) AS 'Cant. Items',
    SUM(d.cantidad * d.precio_estimado) AS 'Monto Total Estimado'
FROM solicitudes_compra s
JOIN usuarios u ON s.id_usuario = u.id_usuario
JOIN areas a ON u.id_area = a.id_area
JOIN detalles_solicitud d ON s.id_solicitud = d.id_solicitud
GROUP BY s.id_solicitud, a.nombre, u.nombre_completo, s.fecha_solicitud, s.estado;

UPDATE solicitudes_compra
SET estado = 'Autorizada'
WHERE id_solicitud = 2;

DELETE FROM detalles_solicitud
WHERE id_detalle = 5 AND id_solicitud = 2;
