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
    cuenta_id INT PRIMARY KEY IDENTITY(1,1),
    cliente_id INT FOREIGN KEY REFERENCES Cliente(cliente_id),
    tipo_cuenta_id INT FOREIGN KEY REFERENCES TipoCuenta(tipo_cuenta_id),
    numero_cuenta NVARCHAR(20) UNIQUE NOT NULL DEFAULT 'PENDIENTE',
    saldo DECIMAL(18,2) DEFAULT 0,
    activa BIT DEFAULT 1
);
GO

-- Trigger para generar automaticamente el numero de cuenta
CREATE TRIGGER trg_GenerarNumeroCuenta
ON Cuenta
AFTER INSERT
AS
BEGIN
    SET NOCOUNT ON;

    UPDATE c
    SET numero_cuenta = 
        RIGHT('00000' + CAST(10000 + i.cliente_id AS NVARCHAR), 5) +
        RIGHT('0' + CAST(i.tipo_cuenta_id AS NVARCHAR), 2)
    FROM Cuenta c
    INNER JOIN inserted i ON c.cuenta_id = i.cuenta_id;
END;
GO

-- Indice filtrado para asegurar la insercion de varias cuentas sin problema
CREATE UNIQUE INDEX IX_Cuenta_numero_cuenta ON Cuenta(numero_cuenta)
WHERE numero_cuenta <> 'PENDIENTE';

CREATE TABLE Transaccion (
    transaccion_id INT IDENTITY(1,1) PRIMARY KEY,
    cuenta_origen_id INT NULL FOREIGN KEY REFERENCES Cuenta(cuenta_id),
    cuenta_destino_id INT NULL FOREIGN KEY REFERENCES Cuenta(cuenta_id),
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

-- =============================================
-- INSERCIONES DE PRUEBA
-- =============================================

--DBCC CHECKIDENT ('Cuenta', RESEED, 0);
-- Se inserta a Juan
INSERT INTO Cuenta (cliente_id, tipo_cuenta_id, saldo) VALUES
(1, 1, 1000.00);
-- Se inserta a Maria
INSERT INTO Cuenta (cliente_id, tipo_cuenta_id, saldo) VALUES
(2, 1, 100.00);
SELECT * FROM Cuenta

-- Se inserta a Ana
INSERT INTO Cuenta (cliente_id, tipo_cuenta_id, saldo) VALUES
(4, 1, 50.00);
-- Se inserta a Luis
INSERT INTO Cuenta (cliente_id, tipo_cuenta_id, saldo) VALUES
(5, 1, 0.00);
SELECT * FROM Cuenta

-- Transferencia de Juan a Maria
EXEC sp_Transferencia 
    @cuenta_origen = 1,
    @cuenta_destino = 2,
    @monto = 100.00,
    @descripcion = 'Pago de servicios';

-- Transferencia Ana a Luis
EXEC sp_Transferencia 
    @cuenta_origen = 3,
    @cuenta_destino = 4,
    @monto = 25.00,
    @descripcion = 'Pago de servicios Y S.A';

SELECT * FROM Transaccion;
SELECT * FROM Movimiento;

-- =============================================
-- PROCEDIMIENTOS ALMACENADOS
-- =============================================

CREATE PROCEDURE sp_Transferencia
    @cuenta_origen INT,
    @cuenta_destino INT,
    @monto DECIMAL(18,2),
    @descripcion NVARCHAR(255)
AS
BEGIN
    SET NOCOUNT ON;

    DECLARE @trans_id INT;

    -- 1. Registrar la transacción
    INSERT INTO Transaccion (tipo, monto, descripcion, cuenta_origen_id, cuenta_destino_id)
    VALUES ('Transferencia', @monto, @descripcion, @cuenta_origen, @cuenta_destino);

    SET @trans_id = SCOPE_IDENTITY();

    -- 2. Insertar los movimientos
    INSERT INTO Movimiento (cuenta_id, monto_debito, monto_credito, transaccion_id)
    VALUES 
    (@cuenta_origen, @monto, 0.00, @trans_id),
    (@cuenta_destino, 0.00, @monto, @trans_id);

    -- 3. Actualizar saldos en Cuenta
    UPDATE Cuenta SET saldo = saldo - @monto WHERE cuenta_id = @cuenta_origen;
    UPDATE Cuenta SET saldo = saldo + @monto WHERE cuenta_id = @cuenta_destino;
END;


CREATE PROCEDURE sp_HistorialMovimientosPorCliente
    @cliente_id INT
AS
BEGIN
    SELECT 
        cl.cliente_id,
        cl.nombre AS nombre_cliente,
        cu.cuenta_id AS cuenta_origen,
        cu.saldo,
        t.transaccion_id,
        t.fecha,
        t.tipo,
        t.monto,
        t.descripcion,
        t.cuenta_destino_id,
        cu_dest.numero_cuenta AS cuenta_destino_numero,
        m.monto_debito,
        m.monto_credito
    FROM Movimiento m
    JOIN Cuenta cu ON m.cuenta_id = cu.cuenta_id
    JOIN Cliente cl ON cu.cliente_id = cl.cliente_id
    JOIN Transaccion t ON m.transaccion_id = t.transaccion_id
    LEFT JOIN Cuenta cu_dest ON t.cuenta_destino_id = cu_dest.cuenta_id
    WHERE cl.cliente_id = @cliente_id
    ORDER BY t.fecha DESC;
END;

EXEC sp_HistorialMovimientosPorCliente @cliente_id = 4;

CREATE or ALTER VIEW vw_MovimientosDelDia AS
SELECT 
    m.movimiento_id,
    c.cliente_id,
    cl.nombre AS nombre_cliente,
    c.numero_cuenta,
    t.tipo AS tipo_transaccion,
    t.descripcion,
    t.monto AS monto_total_transaccion,
    m.monto_debito,
    m.monto_credito,
    m.fecha AS fecha_movimiento
FROM Movimiento m
JOIN Cuenta c ON m.cuenta_id = c.cuenta_id
JOIN Cliente cl ON c.cliente_id = cl.cliente_id
JOIN Transaccion t ON m.transaccion_id = t.transaccion_id
WHERE 
    CONVERT(DATE, m.fecha) = CONVERT(DATE, GETDATE())

SELECT * FROM vw_MovimientosDelDia;
