-- Tabla para almacenar metadatos de archivos de guiones en Supabase Storage
CREATE TABLE IF NOT EXISTS guion_archivo (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_campania BIGINT NOT NULL,
    id_agente BIGINT NULL COMMENT 'NULL para guiones generales de la campaña',
    nombre_archivo VARCHAR(255) NOT NULL,
    ruta_supabase VARCHAR(500) NOT NULL COMMENT 'Ruta completa en Supabase Storage',
    tipo_archivo VARCHAR(10) NOT NULL DEFAULT 'md',
    tamanio_bytes BIGINT NOT NULL,
    fecha_subida DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    subido_por BIGINT NULL,
    
    INDEX idx_campania (id_campania),
    INDEX idx_campania_agente (id_campania, id_agente),
    INDEX idx_campania_general (id_campania, id_agente) COMMENT 'Para buscar guiones generales (id_agente IS NULL)'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Metadatos de archivos de guiones almacenados en Supabase Storage';
