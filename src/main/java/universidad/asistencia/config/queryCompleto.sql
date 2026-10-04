/* ============================================================
   PROGRAMACIÓN II
   SISTEMA UNIVERSITARIO DE CONTROL DE ASISTENCIA
   Base de datos: UniversidadAsistenciaDB
   Motor: Microsoft SQL Server
   ============================================================ */


/* ============================================================
   OPCIONAL - SOLO SI QUIERES BORRAR Y RECREAR TODO
   ============================================================ */

/*
USE master;
GO

IF DB_ID('UniversidadAsistenciaDB') IS NOT NULL
BEGIN
    ALTER DATABASE UniversidadAsistenciaDB
    SET SINGLE_USER WITH ROLLBACK IMMEDIATE;

    DROP DATABASE UniversidadAsistenciaDB;
END;
GO
*/


/* ============================================================
   1. CREAR BASE DE DATOS
   ============================================================ */

IF DB_ID('UniversidadAsistenciaDB') IS NULL
BEGIN
    CREATE DATABASE UniversidadAsistenciaDB;
END;
GO

USE UniversidadAsistenciaDB;
GO


/* ============================================================
   2. ESTUDIANTE
   ============================================================ */

CREATE TABLE Estudiante
(
    id_estudiante INT IDENTITY(1,1) NOT NULL,

    carnet VARCHAR(20) NOT NULL,
    nombres NVARCHAR(100) NOT NULL,
    apellidos NVARCHAR(100) NOT NULL,
    correo VARCHAR(150) NOT NULL,

    activo BIT NOT NULL
        CONSTRAINT DF_Estudiante_Activo DEFAULT 1,

    CONSTRAINT PK_Estudiante
        PRIMARY KEY (id_estudiante),

    CONSTRAINT UQ_Estudiante_Carnet
        UNIQUE (carnet),

    CONSTRAINT UQ_Estudiante_Correo
        UNIQUE (correo)
);
GO


/* ============================================================
   3. DOCENTE
   ============================================================ */

CREATE TABLE Docente
(
    id_docente INT IDENTITY(1,1) NOT NULL,

    codigo_empleado VARCHAR(20) NOT NULL,
    nombres NVARCHAR(100) NOT NULL,
    apellidos NVARCHAR(100) NOT NULL,
    correo VARCHAR(150) NOT NULL,

    activo BIT NOT NULL
        CONSTRAINT DF_Docente_Activo DEFAULT 1,

    CONSTRAINT PK_Docente
        PRIMARY KEY (id_docente),

    CONSTRAINT UQ_Docente_CodigoEmpleado
        UNIQUE (codigo_empleado),

    CONSTRAINT UQ_Docente_Correo
        UNIQUE (correo)
);
GO


/* ============================================================
   4. CURSO
   ============================================================ */

CREATE TABLE Curso
(
    id_curso INT IDENTITY(1,1) NOT NULL,

    codigo VARCHAR(20) NOT NULL,
    nombre NVARCHAR(150) NOT NULL,
    descripcion NVARCHAR(500) NULL,
    creditos TINYINT NOT NULL,

    activo BIT NOT NULL
        CONSTRAINT DF_Curso_Activo DEFAULT 1,

    CONSTRAINT PK_Curso
        PRIMARY KEY (id_curso),

    CONSTRAINT UQ_Curso_Codigo
        UNIQUE (codigo),

    CONSTRAINT CK_Curso_Creditos
        CHECK (creditos > 0)
);
GO


/* ============================================================
   5. PERIODO ACADÉMICO
   ============================================================ */

CREATE TABLE PeriodoAcademico
(
    id_periodo INT IDENTITY(1,1) NOT NULL,

    nombre NVARCHAR(100) NOT NULL,
    fecha_inicio DATE NOT NULL,
    fecha_fin DATE NOT NULL,

    activo BIT NOT NULL
        CONSTRAINT DF_PeriodoAcademico_Activo DEFAULT 1,

    CONSTRAINT PK_PeriodoAcademico
        PRIMARY KEY (id_periodo),

    CONSTRAINT UQ_PeriodoAcademico_Nombre
        UNIQUE (nombre),

    CONSTRAINT CK_PeriodoAcademico_Fechas
        CHECK (fecha_fin >= fecha_inicio)
);
GO


/* ============================================================
   6. SECCIÓN
   ============================================================ */

CREATE TABLE Seccion
(
    id_seccion INT IDENTITY(1,1) NOT NULL,

    codigo VARCHAR(20) NOT NULL,

    id_curso INT NOT NULL,
    id_periodo INT NOT NULL,
    id_docente INT NOT NULL,

    aula_asignada NVARCHAR(50) NOT NULL,

    activo BIT NOT NULL
        CONSTRAINT DF_Seccion_Activo DEFAULT 1,

    CONSTRAINT PK_Seccion
        PRIMARY KEY (id_seccion),

    CONSTRAINT FK_Seccion_Curso
        FOREIGN KEY (id_curso)
        REFERENCES Curso(id_curso),

    CONSTRAINT FK_Seccion_PeriodoAcademico
        FOREIGN KEY (id_periodo)
        REFERENCES PeriodoAcademico(id_periodo),

    CONSTRAINT FK_Seccion_Docente
        FOREIGN KEY (id_docente)
        REFERENCES Docente(id_docente),

    CONSTRAINT UQ_Seccion
        UNIQUE
        (
            id_curso,
            id_periodo,
            codigo
        )
);
GO


/* ============================================================
   7. HORARIO SEMANAL
   ------------------------------------------------------------
   dia_semana:
   1 = Lunes
   2 = Martes
   3 = Miércoles
   4 = Jueves
   5 = Viernes
   6 = Sábado
   7 = Domingo
   ============================================================ */

CREATE TABLE HorarioSemanal
(
    id_horario INT IDENTITY(1,1) NOT NULL,

    id_seccion INT NOT NULL,

    dia_semana TINYINT NOT NULL,

    hora_inicio TIME(0) NOT NULL,
    hora_fin TIME(0) NOT NULL,

    CONSTRAINT PK_HorarioSemanal
        PRIMARY KEY (id_horario),

    CONSTRAINT FK_HorarioSemanal_Seccion
        FOREIGN KEY (id_seccion)
        REFERENCES Seccion(id_seccion),

    CONSTRAINT CK_HorarioSemanal_Dia
        CHECK (dia_semana BETWEEN 1 AND 7),

    CONSTRAINT CK_HorarioSemanal_Horas
        CHECK (hora_fin > hora_inicio),

    CONSTRAINT UQ_HorarioSemanal
        UNIQUE
        (
            id_seccion,
            dia_semana,
            hora_inicio
        )
);
GO


/* ============================================================
   8. INSCRIPCIÓN
   ------------------------------------------------------------
   Resuelve:
       Estudiante N:M Seccion
   ============================================================ */

CREATE TABLE Inscripcion
(
    id_inscripcion INT IDENTITY(1,1) NOT NULL,

    id_estudiante INT NOT NULL,
    id_seccion INT NOT NULL,

    fecha_inscripcion DATE NOT NULL
        CONSTRAINT DF_Inscripcion_Fecha
        DEFAULT CAST(GETDATE() AS DATE),

    fecha_retiro DATE NULL,

    activa BIT NOT NULL
        CONSTRAINT DF_Inscripcion_Activa DEFAULT 1,

    CONSTRAINT PK_Inscripcion
        PRIMARY KEY (id_inscripcion),

    CONSTRAINT FK_Inscripcion_Estudiante
        FOREIGN KEY (id_estudiante)
        REFERENCES Estudiante(id_estudiante),

    CONSTRAINT FK_Inscripcion_Seccion
        FOREIGN KEY (id_seccion)
        REFERENCES Seccion(id_seccion),

    CONSTRAINT UQ_Inscripcion
        UNIQUE
        (
            id_estudiante,
            id_seccion
        ),

    CONSTRAINT CK_Inscripcion_FechaRetiro
        CHECK
        (
            fecha_retiro IS NULL
            OR fecha_retiro >= fecha_inscripcion
        )
);
GO


/* ============================================================
   9. SESIÓN DE CLASE
   ------------------------------------------------------------
   Estado:
       PROGRAMADA
       IMPARTIDA
       SUSPENDIDA
       CANCELADA
   ============================================================ */

CREATE TABLE SesionClase
(
    id_sesion INT IDENTITY(1,1) NOT NULL,

    id_seccion INT NOT NULL,

    fecha DATE NOT NULL,

    hora_inicio_programada TIME(0) NOT NULL,
    hora_fin_programada TIME(0) NOT NULL,

    aula NVARCHAR(50) NOT NULL,

    estado VARCHAR(15) NOT NULL
        CONSTRAINT DF_SesionClase_Estado
        DEFAULT 'PROGRAMADA',

    CONSTRAINT PK_SesionClase
        PRIMARY KEY (id_sesion),

    CONSTRAINT FK_SesionClase_Seccion
        FOREIGN KEY (id_seccion)
        REFERENCES Seccion(id_seccion),

    CONSTRAINT CK_SesionClase_Horas
        CHECK
        (
            hora_fin_programada >
            hora_inicio_programada
        ),

    CONSTRAINT CK_SesionClase_Estado
        CHECK
        (
            estado IN
            (
                'PROGRAMADA',
                'IMPARTIDA',
                'SUSPENDIDA',
                'CANCELADA'
            )
        ),

    CONSTRAINT UQ_SesionClase
        UNIQUE
        (
            id_seccion,
            fecha,
            hora_inicio_programada
        )
);
GO


/* ============================================================
   10. DISPOSITIVO
   ------------------------------------------------------------
   Tipo:
       KIOSCO
       APP_MOVIL
       LECTOR_AULA
   ============================================================ */

CREATE TABLE Dispositivo
(
    id_dispositivo INT IDENTITY(1,1) NOT NULL,

    codigo VARCHAR(50) NOT NULL,
    nombre NVARCHAR(100) NOT NULL,

    tipo VARCHAR(20) NOT NULL,

    ubicacion NVARCHAR(200) NULL,

    activo BIT NOT NULL
        CONSTRAINT DF_Dispositivo_Activo DEFAULT 1,

    CONSTRAINT PK_Dispositivo
        PRIMARY KEY (id_dispositivo),

    CONSTRAINT UQ_Dispositivo_Codigo
        UNIQUE (codigo),

    CONSTRAINT CK_Dispositivo_Tipo
        CHECK
        (
            tipo IN
            (
                'KIOSCO',
                'APP_MOVIL',
                'LECTOR_AULA'
            )
        )
);
GO


/* ============================================================
   11. MARCAJE
   ------------------------------------------------------------
   tipo:
       ENTRADA
       SALIDA

   medio:
       KIOSCO
       APP_MOVIL
       LECTOR_AULA
   ============================================================ */

CREATE TABLE Marcaje
(
    id_marcaje BIGINT IDENTITY(1,1) NOT NULL,

    id_estudiante INT NOT NULL,
    id_sesion INT NOT NULL,
    id_dispositivo INT NOT NULL,

    fecha_hora DATETIME2(3) NOT NULL
        CONSTRAINT DF_Marcaje_FechaHora
        DEFAULT SYSDATETIME(),

    tipo VARCHAR(10) NOT NULL,

    medio VARCHAR(20) NOT NULL,

    CONSTRAINT PK_Marcaje
        PRIMARY KEY (id_marcaje),

    CONSTRAINT FK_Marcaje_Estudiante
        FOREIGN KEY (id_estudiante)
        REFERENCES Estudiante(id_estudiante),

    CONSTRAINT FK_Marcaje_SesionClase
        FOREIGN KEY (id_sesion)
        REFERENCES SesionClase(id_sesion),

    CONSTRAINT FK_Marcaje_Dispositivo
        FOREIGN KEY (id_dispositivo)
        REFERENCES Dispositivo(id_dispositivo),

    CONSTRAINT CK_Marcaje_Tipo
        CHECK
        (
            tipo IN
            (
                'ENTRADA',
                'SALIDA'
            )
        ),

    CONSTRAINT CK_Marcaje_Medio
        CHECK
        (
            medio IN
            (
                'KIOSCO',
                'APP_MOVIL',
                'LECTOR_AULA'
            )
        )
);
GO


/* ============================================================
   12. JUSTIFICACIÓN
   ------------------------------------------------------------
   Estado:
       PENDIENTE
       APROBADA
       RECHAZADA
   ============================================================ */

CREATE TABLE Justificacion
(
    id_justificacion INT IDENTITY(1,1) NOT NULL,

    id_estudiante INT NOT NULL,
    id_sesion INT NOT NULL,

    /* Docente que registra la justificación */
    id_docente_registra INT NOT NULL,

    motivo NVARCHAR(250) NOT NULL,

    fecha DATETIME2(0) NOT NULL
        CONSTRAINT DF_Justificacion_Fecha
        DEFAULT SYSDATETIME(),

    observacion NVARCHAR(1000) NULL,

    /* Evidencia opcional */
    evidencia_nombre NVARCHAR(255) NULL,
    evidencia_tipo VARCHAR(100) NULL,
    evidencia VARBINARY(MAX) NULL,

    estado VARCHAR(15) NOT NULL
        CONSTRAINT DF_Justificacion_Estado
        DEFAULT 'PENDIENTE',

    /* Se llena al aprobar/rechazar */
    id_docente_resuelve INT NULL,
    fecha_resolucion DATETIME2(0) NULL,

    CONSTRAINT PK_Justificacion
        PRIMARY KEY (id_justificacion),

    CONSTRAINT FK_Justificacion_Estudiante
        FOREIGN KEY (id_estudiante)
        REFERENCES Estudiante(id_estudiante),

    CONSTRAINT FK_Justificacion_SesionClase
        FOREIGN KEY (id_sesion)
        REFERENCES SesionClase(id_sesion),

    CONSTRAINT FK_Justificacion_DocenteRegistra
        FOREIGN KEY (id_docente_registra)
        REFERENCES Docente(id_docente),

    CONSTRAINT FK_Justificacion_DocenteResuelve
        FOREIGN KEY (id_docente_resuelve)
        REFERENCES Docente(id_docente),

    CONSTRAINT CK_Justificacion_Estado
        CHECK
        (
            estado IN
            (
                'PENDIENTE',
                'APROBADA',
                'RECHAZADA'
            )
        ),

    CONSTRAINT UQ_Justificacion_EstudianteSesion
        UNIQUE
        (
            id_estudiante,
            id_sesion
        ),

    CONSTRAINT CK_Justificacion_Resolucion
        CHECK
        (
            (
                estado = 'PENDIENTE'
                AND id_docente_resuelve IS NULL
                AND fecha_resolucion IS NULL
            )
            OR
            (
                estado IN ('APROBADA', 'RECHAZADA')
                AND id_docente_resuelve IS NOT NULL
                AND fecha_resolucion IS NOT NULL
            )
        )
);
GO


/* ============================================================
   13. USUARIO DEL SISTEMA
   ------------------------------------------------------------
   Esta tabla es un supuesto técnico para la aplicación Java.

   Roles iniciales:
       ADMIN
       DOCENTE
   ============================================================ */

CREATE TABLE Usuario
(
    id_usuario INT IDENTITY(1,1) NOT NULL,

    usuario VARCHAR(50) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,

    rol VARCHAR(10) NOT NULL,

    /*
       Si el usuario es DOCENTE debe quedar relacionado
       con un docente.
       ADMIN no necesita id_docente.
    */
    id_docente INT NULL,

    activo BIT NOT NULL
        CONSTRAINT DF_Usuario_Activo DEFAULT 1,

    fecha_creacion DATETIME2(0) NOT NULL
        CONSTRAINT DF_Usuario_FechaCreacion
        DEFAULT SYSDATETIME(),

    CONSTRAINT PK_Usuario
        PRIMARY KEY (id_usuario),

    CONSTRAINT UQ_Usuario_Usuario
        UNIQUE (usuario),

    CONSTRAINT FK_Usuario_Docente
        FOREIGN KEY (id_docente)
        REFERENCES Docente(id_docente),

    CONSTRAINT CK_Usuario_Rol
        CHECK
        (
            rol IN
            (
                'ADMIN',
                'DOCENTE'
            )
        ),

    CONSTRAINT CK_Usuario_Docente
        CHECK
        (
            (
                rol = 'ADMIN'
                AND id_docente IS NULL
            )
            OR
            (
                rol = 'DOCENTE'
                AND id_docente IS NOT NULL
            )
        )
);
GO


/* ============================================================
   ÍNDICES
   ============================================================ */

/* Buscar las inscripciones de un estudiante */
CREATE INDEX IX_Inscripcion_Estudiante
ON Inscripcion
(
    id_estudiante,
    activa
);
GO


/* Buscar estudiantes de una sección */
CREATE INDEX IX_Inscripcion_Seccion
ON Inscripcion
(
    id_seccion,
    activa
);
GO


/* Buscar sesiones de una sección por fecha */
CREATE INDEX IX_SesionClase_Seccion_Fecha
ON SesionClase
(
    id_seccion,
    fecha
);
GO


/* Buscar sesiones según su estado */
CREATE INDEX IX_SesionClase_Estado
ON SesionClase
(
    estado
);
GO


/* Historial y último marcaje */
CREATE INDEX IX_Marcaje_Estudiante_Sesion_Fecha
ON Marcaje
(
    id_estudiante,
    id_sesion,
    fecha_hora
);
GO


/* Marcajes de una sesión */
CREATE INDEX IX_Marcaje_Sesion
ON Marcaje
(
    id_sesion,
    fecha_hora
);
GO


/* Auditoría de dispositivo */
CREATE INDEX IX_Marcaje_Dispositivo
ON Marcaje
(
    id_dispositivo,
    fecha_hora
);
GO


/* Consultar justificaciones pendientes */
CREATE INDEX IX_Justificacion_Estado
ON Justificacion
(
    estado
);
GO


/* ============================================================
   COMPROBACIÓN
   ============================================================ */

SELECT
    TABLE_NAME
FROM INFORMATION_SCHEMA.TABLES
WHERE TABLE_TYPE = 'BASE TABLE'
ORDER BY TABLE_NAME;
GO