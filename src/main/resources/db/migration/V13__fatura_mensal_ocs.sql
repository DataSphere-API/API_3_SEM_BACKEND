ALTER TABLE Fatura ADD COLUMN IF NOT EXISTS ocs_id VARCHAR(14);
ALTER TABLE Fatura ADD COLUMN IF NOT EXISTS competencia VARCHAR(7); -- Formato: 'YYYY-MM'

ALTER TABLE Fatura ALTER COLUMN esp_fat DROP NOT NULL;
ALTER TABLE Fatura ALTER COLUMN guia_proc_id DROP NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uq_fatura_ocs_competencia_ativa
    ON Fatura (ocs_id, competencia)
    WHERE status = 'ATIVA';