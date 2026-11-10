-- US007: liga cada procedimento da pré-guia ao atendimento (Guia_Proc) criado na emissão.
-- Fica NULL até a guia ser emitida.
ALTER TABLE Pre_Guia_Proc
    ADD COLUMN Guia_Proc_ID INT,
    ADD CONSTRAINT uq_pgp_guia_proc UNIQUE (Guia_Proc_ID),
    ADD CONSTRAINT fk_pgp_guia_proc FOREIGN KEY (Guia_Proc_ID)
        REFERENCES Guia_Proc (ID)
        ON UPDATE CASCADE ON DELETE SET NULL;