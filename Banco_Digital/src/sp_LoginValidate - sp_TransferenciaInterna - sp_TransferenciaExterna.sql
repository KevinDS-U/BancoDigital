CREATE PROCEDURE sp_LoginValidate
    @username VARCHAR(50),
    @passwordHash VARCHAR(64)
AS
BEGIN
    SET NOCOUNT ON;

    IF EXISTS (
        SELECT 1
        FROM Cuentas
        WHERE username = @username AND password_hash = @passwordHash
    )
        SELECT 'Login exitoso' AS Resultado;
    ELSE
        SELECT 'Usuario o contraseña incorrectos' AS Resultado;
END;
GO

CREATE PROCEDURE sp_TransferenciaInterna
    @cuentaOrigen INT,
    @cuentaDestino INT,
    @monto DECIMAL(18,2)
AS
BEGIN
    SET NOCOUNT ON;
    BEGIN TRANSACTION;

    BEGIN TRY
        -- Verificar fondos
        IF (SELECT saldo FROM Cuentas WHERE id_cuenta = @cuentaOrigen) < @monto
        BEGIN
            RAISERROR('Fondos insuficientes.', 16, 1);
        END

        -- Descontar de origen
        UPDATE Cuentas
        SET saldo = saldo - @monto
        WHERE id_cuenta = @cuentaOrigen;

        -- Agregar al destino
        UPDATE Cuentas
        SET saldo = saldo + @monto
        WHERE id_cuenta = @cuentaDestino;

        -- Registrar transferencia
        INSERT INTO HistorialTransferencias (cuenta_origen, cuenta_destino, monto, fecha, tipo)
        VALUES (@cuentaOrigen, @cuentaDestino, @monto, GETDATE(), 'Interna');

        COMMIT;
    END TRY
    BEGIN CATCH
        ROLLBACK;
        SELECT ERROR_MESSAGE() AS Error;
    END CATCH
END;
GO

CREATE PROCEDURE sp_TransferenciaExterna
    @cuentaOrigen INT,
    @bancoDestino VARCHAR(100),
    @cuentaDestinoExterna VARCHAR(50),
    @monto DECIMAL(18,2)
AS
BEGIN
    SET NOCOUNT ON;
    BEGIN TRANSACTION;

    BEGIN TRY
        -- Verificar fondos
        IF (SELECT saldo FROM Cuentas WHERE id_cuenta = @cuentaOrigen) < @monto
        BEGIN
            RAISERROR('Fondos insuficientes.', 16, 1);
        END

        -- Descontar de cuenta origen
        UPDATE Cuentas
        SET saldo = saldo - @monto
        WHERE id_cuenta = @cuentaOrigen;

        -- Registrar transferencia externa
        INSERT INTO HistorialTransferencias (
            cuenta_origen,
            banco_destino,
            cuenta_destino,
            monto,
            fecha,
            tipo
        )
        VALUES (
            @cuentaOrigen,
            @bancoDestino,
            @cuentaDestinoExterna,
            @monto,
            GETDATE(),
            'Externa'
        );

        COMMIT;
    END TRY
    BEGIN CATCH
        ROLLBACK;
        SELECT ERROR_MESSAGE() AS Error;
    END CATCH
END;
