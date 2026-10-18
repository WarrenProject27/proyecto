CREATE DATABASE IF NOT EXISTS programas_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE programas_db;

CREATE TABLE IF NOT EXISTS programas (
    id INT NOT NULL AUTO_INCREMENT,
    asignatura VARCHAR(100) NOT NULL,
    grado VARCHAR(20) NOT NULL,
    nivel VARCHAR(40) NOT NULL,
    docente VARCHAR(120) NOT NULL,
    pdf_nombre VARCHAR(255) NULL,
    pdf_datos MEDIUMBLOB NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS usuarios (
    id INT NOT NULL AUTO_INCREMENT,
    nombre_usuario VARCHAR(40) NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL,
    rol ENUM('Administrador', 'Docente', 'Estudiante') NOT NULL,
    contrasena_hash VARCHAR(200) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_usuarios_nombre_usuario (nombre_usuario),
    UNIQUE KEY uq_usuarios_email (email)
) ENGINE=InnoDB
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

ALTER TABLE programas
    MODIFY id INT NOT NULL AUTO_INCREMENT,
    MODIFY asignatura VARCHAR(100) NOT NULL,
    MODIFY grado VARCHAR(20) NOT NULL,
    MODIFY nivel VARCHAR(40) NOT NULL,
    MODIFY docente VARCHAR(120) NOT NULL,
    MODIFY pdf_datos MEDIUMBLOB NULL;

UPDATE usuarios
SET contrasena_hash =
    'pbkdf2-sha256$120000$YyVmIU1tdpTyuVxocqub1A==$QXnBHxq3TvvxVaJK2arbadbZCM+EOlEWxFUdzqZPpeY='
WHERE nombre_usuario = 'admin'
  AND contrasena_hash =
    'pbkdf2-sha256$120000$cHJpc21hLWRlbW8tc2FsdA==$IDXYrybm1mTpisnmzos3MgR88TZK6XS2gkcfeone/8c=';

UPDATE usuarios
SET nombre = 'Admin'
WHERE nombre_usuario = 'admin';

UPDATE usuarios
SET nombre = 'Walter'
WHERE nombre_usuario = 'docente';

UPDATE usuarios
SET nombre = 'George'
WHERE nombre_usuario = 'estudiante';

INSERT INTO usuarios
    (nombre_usuario, nombre, email, rol, contrasena_hash)
SELECT
    'admin',
    'Admin',
    'admin@prisma.local',
    'Administrador',
    'pbkdf2-sha256$120000$YyVmIU1tdpTyuVxocqub1A==$QXnBHxq3TvvxVaJK2arbadbZCM+EOlEWxFUdzqZPpeY='
WHERE NOT EXISTS (
    SELECT 1
    FROM usuarios
    WHERE nombre_usuario = 'admin'
);

INSERT INTO usuarios
    (nombre_usuario, nombre, email, rol, contrasena_hash)
SELECT
    'docente',
    'Walter',
    'docente@prisma.local',
    'Docente',
    'pbkdf2-sha256$120000$SWE6sH+OkPRaHc/oArbWaA==$JWte18T+3CVb2VkqCffBC3nQw4cIQG5kK40xe+LqQY8='
WHERE NOT EXISTS (
    SELECT 1
    FROM usuarios
    WHERE nombre_usuario = 'docente'
);

INSERT INTO usuarios
    (nombre_usuario, nombre, email, rol, contrasena_hash)
SELECT
    'estudiante',
    'George',
    'estudiante@prisma.local',
    'Estudiante',
    'pbkdf2-sha256$120000$j6/mFAPRGblBsCRnP6srUQ==$JIKpQBBB8hwQhnKzHaN/PLBa8QtipdUJo2aKtydocG0='
WHERE NOT EXISTS (
    SELECT 1
    FROM usuarios
    WHERE nombre_usuario = 'estudiante'
);

DELETE FROM programas
WHERE asignatura = 'Informática'
  AND grado = '1°'
  AND nivel = 'Bachillerato'
  AND docente = 'Docente de prueba';

INSERT INTO programas (asignatura, grado, nivel, docente)
SELECT 'Informática', '1°', 'Bachillerato', 'Walter'
WHERE NOT EXISTS (
    SELECT 1
    FROM programas
    WHERE asignatura = 'Informática'
      AND grado = '1°'
      AND nivel = 'Bachillerato'
      AND docente = 'Walter'
);
