-- Add metadata columns to guion table
ALTER TABLE guion
ADD COLUMN objetivo TEXT,
ADD COLUMN tipo VARCHAR(50),
ADD COLUMN notas_internas TEXT;

-- Create guion_seccion table
CREATE TABLE guion_seccion (
    id INT AUTO_INCREMENT PRIMARY KEY,
    id_guion INT NOT NULL,
    tipo_seccion VARCHAR(50) NOT NULL,
    contenido TEXT,
    orden INT NOT NULL,
    FOREIGN KEY (id_guion) REFERENCES guion(id) ON DELETE CASCADE,
    INDEX idx_guion_seccion_guion (id_guion),
    INDEX idx_guion_seccion_orden (id_guion, orden)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
