-- Nulos: guias emitidas antes desta migration não têm documento.
ALTER TABLE Guia
    ADD COLUMN IF NOT EXISTS Documento   BYTEA,
    ADD COLUMN IF NOT EXISTS Hash_Pacote VARCHAR(64);
