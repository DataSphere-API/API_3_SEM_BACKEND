-- Nulos: guias emitidas antes desta migration não têm documento.
ALTER TABLE Guia
    ADD COLUMN Documento   BYTEA,
    ADD COLUMN Hash_Pacote VARCHAR(64);
