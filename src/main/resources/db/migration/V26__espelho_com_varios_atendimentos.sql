-- O espelho junta vários atendimentos: a lista fica em Espelho_Item.Atendimento_ID.
-- Espelho.Atendimento_ID continua com o primeiro atendimento e deixa de ser único.
ALTER TABLE Espelho DROP CONSTRAINT IF EXISTS uq_espelho_atendimento;
