CREATE PROCEDURE sp_LoginValidate
    @username NVARCHAR(50),
    @password NVARCHAR(100)
AS
BEGIN
    SET NOCOUNT ON;

    DECLARE 
        @storedHash VARBINARY(64),
        @storedSalt NVARCHAR(32),
        @intentos INT,
        @bloqueado BIT,
        @calculatedHash VARBINARY(64);

    -- Verifica si el usuario existe
    IF NOT EXISTS (SELECT 1 FROM Usuario WHERE username = @username)
    BEGIN
        SELECT 'Este usuario no está registrado en el sistema.' AS Mensaje;
        RETURN;
    END

    -- Obtiene los datos del usuario
    SELECT 
        @storedHash = password_hash,
        @storedSalt = salt,
        @intentos = intentos_fallidos,
        @bloqueado = bloqueado
    FROM Usuario
    WHERE username = @username;

    -- Si el usuario ya está bloqueado
    IF @bloqueado = 1
    BEGIN
        SELECT 'Tu cuenta está bloqueada por múltiples intentos fallidos. Por favor, contacta con soporte.' AS Mensaje;
        RETURN;
    END

    -- Calcula el hash con la contraseña ingresada y el salt guardado
    SET @calculatedHash = HASHBYTES('SHA2_256', CONVERT(VARBINARY(MAX), @password + @storedSalt));

    -- Compara los hashes
    IF @calculatedHash = @storedHash
    BEGIN
        -- Restablece intentos si la contraseña es correcta
        UPDATE Usuario
        SET intentos_fallidos = 0
        WHERE username = @username;

        SELECT '¡Bienvenido! Has iniciado sesión correctamente.' AS Mensaje;
    END
    ELSE
    BEGIN
        -- Aumenta el contador de intentos
        SET @intentos = @intentos + 1;

        UPDATE Usuario
        SET intentos_fallidos = @intentos,
            bloqueado = CASE WHEN @intentos >= 3 THEN 1 ELSE 0 END
        WHERE username = @username;

        IF @intentos >= 3
            SELECT 'Has excedido el número de intentos permitidos. Tu cuenta ha sido bloqueada.' AS Mensaje;
        ELSE
            SELECT 'La contraseña ingresada es incorrecta. Inténtalo nuevamente.' AS Mensaje;
    END
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
            cuenta_destino,
            monto,
            fecha,
            tipo
        )
        VALUES (
            @cuentaOrigen,
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
