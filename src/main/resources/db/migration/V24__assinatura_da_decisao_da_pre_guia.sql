-- Assinatura de quem decidiu a pré-guia (aprovou ou reprovou). Nulas nas pré-guias ainda sem decisão.
ALTER TABLE Pre_Guia
    ADD COLUMN IF NOT EXISTS Assinatura_Usuario_ID INT,
    ADD COLUMN IF NOT EXISTS Assinatura_Nome       VARCHAR(150),
    ADD COLUMN IF NOT EXISTS Assinatura_Perfil     VARCHAR(10),
    ADD COLUMN IF NOT EXISTS Assinatura_Data       TIMESTAMP;

ALTER TABLE Pre_Guia
    ADD CONSTRAINT fk_pre_guia_assinatura_usuario FOREIGN KEY (Assinatura_Usuario_ID)
        REFERENCES Usuario (ID)
        ON UPDATE CASCADE ON DELETE RESTRICT;

ALTER TABLE Pre_Guia
    ADD CONSTRAINT ck_pre_guia_assinatura_perfil CHECK (Assinatura_Perfil IN ('MEDICO', 'CHEFE'));
