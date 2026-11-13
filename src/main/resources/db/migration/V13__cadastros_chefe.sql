-- US000-17: a aplicação valida antes de gravar; as constraints também protegem acessos concorrentes.
CREATE UNIQUE INDEX uq_procedimento_nome ON procedimento (LOWER(TRIM(nome)));
ALTER TABLE contrato ADD CONSTRAINT uq_contrato_procedimento_ocs UNIQUE (proc_id, ocs_id);
ALTER TABLE contrato ADD CONSTRAINT ck_contrato_valor_positivo CHECK (valor > 0);

CREATE UNIQUE INDEX uq_chefe_beneficiario ON chefe_fusex (benef_id);
CREATE UNIQUE INDEX uq_medico_beneficiario ON medico (benef_id);
CREATE UNIQUE INDEX uq_analista_beneficiario ON analista (benef_id);
CREATE UNIQUE INDEX uq_auditor_beneficiario ON auditor (benef_id);

-- Compatível com cargas anteriores que inseriram IDs explícitos.
SELECT setval(pg_get_serial_sequence('medico', 'id'),
    GREATEST(COALESCE((SELECT MAX(id) FROM medico), 1), (SELECT last_value FROM medico_id_seq)));
SELECT setval(pg_get_serial_sequence('chefe_fusex', 'id'),
    GREATEST(COALESCE((SELECT MAX(id) FROM chefe_fusex), 1), (SELECT last_value FROM chefe_fusex_id_seq)));
SELECT setval(pg_get_serial_sequence('analista', 'id'),
    GREATEST(COALESCE((SELECT MAX(id) FROM analista), 1), (SELECT last_value FROM analista_id_seq)));
SELECT setval(pg_get_serial_sequence('auditor', 'id'),
    GREATEST(COALESCE((SELECT MAX(id) FROM auditor), 1), (SELECT last_value FROM auditor_id_seq)));
