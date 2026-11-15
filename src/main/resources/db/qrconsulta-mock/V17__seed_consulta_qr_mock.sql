-- Identidades fictícias; não há login por senha neste mock.
INSERT INTO usuario (login, senha) VALUES
    ('00000000351', 'US0035-MOCK-SEM-LOGIN'),
    ('00000000352', 'US0035-MOCK-SEM-LOGIN');

UPDATE beneficiario SET usuario_id = (SELECT id FROM usuario WHERE login = '00000000351')
WHERE preccp = '123456789-00';
UPDATE beneficiario SET usuario_id = (SELECT id FROM usuario WHERE login = '00000000352')
WHERE preccp = '111111111-00';

-- Três códigos: pronto para baixa, sessões incompletas e já realizado.
WITH nova_guia AS (
    INSERT INTO guia (benf_id, med_id, emissor_id, chefe_id, status, data_emissao)
    VALUES ('123456789-00', 1, 1, 1, 'EMITIDA', CURRENT_DATE)
    RETURNING id
)
INSERT INTO guia_proc (cont_id, guia_id, qr, status, qtd_autorizada, qtd_realizada, data_validade)
SELECT 1, g.id, gen_random_uuid()::text, dados.status, dados.autorizada, dados.realizada, CURRENT_DATE + 60
FROM nova_guia g
CROSS JOIN (VALUES ('EM_ANDAMENTO', 1, 1), ('EM_ANDAMENTO', 2, 1), ('REALIZADA', 1, 1))
    AS dados(status, autorizada, realizada);
