-- =============================================
-- SCRIPT: CREAR BASE DE DATOS
-- Proyecto Final/BD
-- =============================================

-- Crea esta ruta, con el proposito de estandarizar la creacion y uso de esta BD.
-- Ejemplo: " C:\ProyectoFinal\BD\ "
-- Asegúrate de que la carpeta exista y SQL Server tenga permiso de escritura.

CREATE DATABASE BANCO_DIGITAL
ON PRIMARY
(
    NAME = 'BANCO_DIGITAL_Data',
    FILENAME = 'C:\ProyectoFinal\BD\Data\BANCO_DIGITAL_Data.mdf',
    SIZE = 20MB,
    MAXSIZE = 200MB,
    FILEGROWTH = 5MB
)
LOG ON
(
    NAME = 'BANCO_DIGITAL_Log',
    FILENAME = 'C:\ProyectoFinal\BD\Log\BANCO_DIGITAL_Log.ldf',
    SIZE = 10MB,
    MAXSIZE = 50MB,
    FILEGROWTH = 5MB
);
GO

-- Usar la base de datos recién creada
USE BANCO_DIGITAL;
GO

-- Nombres de arhivos logicos asociados a acada base de datos
SELECT
    db.name AS database_name,
    mf.name AS logical_file_name,
    mf.type_desc AS file_type,
    mf.physical_name
FROM sys.master_files mf
JOIN sys.databases db ON db.database_id = mf.database_id
ORDER BY db.name;

-- =============================================
-- CREAR TABLAS PRINCIPALES
-- =============================================

CREATE TABLE Cliente (
    cliente_id INT IDENTITY(1,1) PRIMARY KEY,
    nombre NVARCHAR(100) NOT NULL,
    correo NVARCHAR(100) NOT NULL,
	direccion NVARCHAR (100),
	pais      NVARCHAR (100) NOT NULL,
	telefono NVARCHAR (20)NOT NULL
);
GO

SELECT * FROM Cliente

CREATE TABLE Usuario (
    usuario_id INT IDENTITY(1,1) PRIMARY KEY,
    cliente_id INT FOREIGN KEY REFERENCES Cliente(cliente_id),
    username NVARCHAR(50) UNIQUE NOT NULL,
    password_hash VARBINARY(64) NOT NULL,
    salt NVARCHAR(32) NOT NULL,
    intentos_fallidos INT DEFAULT 0,
    bloqueado BIT DEFAULT 0
);
GO

CREATE TABLE TipoCuenta (
    tipo_cuenta_id INT IDENTITY(1,1) PRIMARY KEY,
    descripcion NVARCHAR(50) NOT NULL
);
GO

CREATE TABLE Cuenta (
    cuenta_id INT IDENTITY(1,1) PRIMARY KEY,
    cliente_id INT FOREIGN KEY REFERENCES Cliente(cliente_id),
    tipo_cuenta_id INT FOREIGN KEY REFERENCES TipoCuenta(tipo_cuenta_id),
    numero_cuenta NVARCHAR(20) UNIQUE NOT NULL,
    saldo DECIMAL(18,2) NOT NULL,
    activa BIT DEFAULT 1
);
GO

CREATE TABLE Transaccion (
    transaccion_id INT IDENTITY(1,1) PRIMARY KEY,
    fecha DATETIME DEFAULT GETDATE(),
    tipo NVARCHAR(50) NOT NULL,
    monto DECIMAL(18,2) NOT NULL,
    descripcion NVARCHAR(255)
);
GO

CREATE TABLE Movimiento (
    movimiento_id INT IDENTITY(1,1) PRIMARY KEY,
    cuenta_id INT FOREIGN KEY REFERENCES Cuenta(cuenta_id),
    monto_debito DECIMAL(18,2) DEFAULT 0,
    monto_credito DECIMAL(18,2) DEFAULT 0,
    transaccion_id INT FOREIGN KEY REFERENCES Transaccion(transaccion_id),
    fecha DATETIME DEFAULT GETDATE()
);
GO

-- =============================================
-- INSERCION DE DATOS
-- =============================================
INSERT INTO Cliente (nombre, correo, direccion, pais, telefono) VALUES
('Juan Perez', 'juan.perez@email.com', 'Paitilla', 'Panamá', '6626-3120'),
('María Gómez', 'maria.gomez@email.com', 'Albrook', 'Panamá', '6789-1234'),
('Carlos Rodríguez', 'carlos.rod@email.com', 'Bella Vista', 'Panamá', '6123-4567'),
('Ana Torres', 'ana.torres@email.com', 'San Miguelito', 'Panamá', '6900-1122'),
('Luis Moreno', 'luis.moreno@email.com', 'Brisas del Golf', 'Panamá', '6555-6677');
GO
SELECT * FROM Cliente;
PRINT 'Base BANCO_DIGITAL creada con archivos en Proyecto Final/BD.';

INSERT INTO TipoCuenta (descripcion) VALUES
('Ahorro'),
('Corriente'),
('Tarjeta de Crédito'),
('Tarjeta de Débito');
GO
SELECT * FROM TipoCuenta

-- INSERCIONES DE PRUEBA

INSERT INTO Cuenta (cliente_id, tipo_cuenta_id, numero_cuenta, saldo)
VALUES 
(1, 1, '100001', 1000.00),
(2, 2, '100002', 2500.00),
(3, 1, '100003', 500.00),
(4, 3, '100004', 1200.00),
(5, 4, '100005', 750.00);
GO
SELECT * FROM Cuenta

INSERT INTO Transaccion (tipo, monto, descripcion)
VALUES ('Transferencia', 100.00, 'Transferencia entre cuentas'),
('Transferencia', 500.00, 'Transferencia entre cuentas');
SELECT * FROM Transaccion

-- Insertar movimientos
-- Juan debita 100 de cuenta 1 a cuenta 2
INSERT INTO Movimiento (cuenta_id, monto_debito, monto_credito, transaccion_id)
VALUES 
(1, 100.00, 0.00, 1),  -- Débito a cuenta 1
(2, 0.00, 100.00, 1);  -- Crédito a cuenta 2
SELECT * FROM Movimiento

-- Carlos debita 100 de cuenta 1 a cuenta 2
INSERT INTO Movimiento (cuenta_id, monto_debito, monto_credito, transaccion_id)
VALUES 
(3, 400.00, 0.00, 2);  -- Débito a cuenta 3
SELECT * FROM Movimiento

-- =============================================
-- PROCEDIMIENTOS ALMACENADOS
-- =============================================

CREATE PROCEDURE sp_HistorialMovimientosPorCliente
    @cliente_id INT
AS
BEGIN
    SELECT 
        cl.cliente_id,
        cl.nombre AS nombre_cliente,
        cu.cuenta_id,
        cu.saldo,
        t.transaccion_id,
        t.fecha,
        t.tipo,
        t.monto,
        t.descripcion,
        m.monto_debito,
        m.monto_credito
    FROM Movimiento m
    JOIN Cuenta cu ON m.cuenta_id = cu.cuenta_id
    JOIN Cliente cl ON cu.cliente_id = cl.cliente_id
    JOIN Transaccion t ON m.transaccion_id = t.transaccion_id
    WHERE cl.cliente_id = @cliente_id
    ORDER BY t.fecha DESC;
END


EXEC sp_HistorialMovimientosPorCliente @cliente_id = 1;

