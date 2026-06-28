# SIC-STJ - Prototipo Operacional TP4

Prototipo Java del Sistema Integral de Compras y Contrataciones del STJ Chubut, centrado en el caso de uso **CU-01: Registrar solicitud de compra**.

El proyecto implementa una arquitectura MVC con vista por consola, controlador de solicitudes, modelo de dominio, persistencia MySQL mediante JDBC, validaciones, manejo de excepciones, bitacora local por archivo y uso complementario de arreglos y `ArrayList`.

## Contenido del proyecto

- `src/`: codigo fuente Java.
- `sql/`: scripts para crear y poblar la base de datos MySQL.
- `docs/`: documentacion complementaria del alcance TP4 y cambios implementados.
- `mysql-connector-j.jar`: driver JDBC de MySQL incluido para facilitar la ejecucion.
- `README.md`: instrucciones para compilar, configurar y ejecutar.


## Requisitos

- Java JDK instalado.
- MySQL Server instalado y en ejecucion.
- PowerShell, si se ejecutan los comandos tal como estan escritos.

## Preparar la base de datos

Antes de ejecutar el programa, abrir MySQL Workbench o una consola MySQL y ejecutar los scripts en este orden:

1. `sql/schema.sql`
2. `sql/datos_y_operaciones_dml.sql`

El primer script crea la base `sic_stj_db` y sus tablas. El segundo carga datos de prueba y deja disponibles usuarios, bienes y solicitudes iniciales.

## Configurar conexion MySQL

Por defecto, el proyecto intenta conectarse con estos valores:

URL: jdbc:mysql://127.0.0.1:3306/sic_stj_db
Usuario: root
Password: Root

## Compilar

Desde la raiz del proyecto:


javac -d out (Get-ChildItem -Recurse src -Filter *.java).FullName

Esto genera la carpeta `out/` con los archivos `.class` compilados.

## Ejecutar

Desde la raiz del proyecto:

java -cp "out;mysql-connector-j.jar" vista.MainApp

