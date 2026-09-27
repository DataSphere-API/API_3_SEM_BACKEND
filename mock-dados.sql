INSERT INTO usuario (id, login, senha) VALUES
    (358495830, 'usuario358495830', 'senha_hash_placeholder'),
    (358495831, 'usuario358495831', 'senha_hash_placeholder'),
    (358495832, 'usuario358495832', 'senha_hash_placeholder'),
    (358495833, 'usuario358495833', 'senha_hash_placeholder'),
    (358495834, 'usuario358495834', 'senha_hash_placeholder'),
    (358495835, 'usuario358495835', 'senha_hash_placeholder'),
    (358495836, 'usuario358495836', 'senha_hash_placeholder'),
    (358495837, 'usuario358495837', 'senha_hash_placeholder'),
    (358495838, 'usuario358495838', 'senha_hash_placeholder'),
    (358495839, 'usuario358495839', 'senha_hash_placeholder')
ON CONFLICT (id) DO NOTHING;

-- 1. OCS / PROCEDIMENTO
INSERT INTO ocs (cnpj, nome) VALUES
    ('12345678000101', 'Hospital Central OCS'),
    ('12345678000102', 'Clínica Médica Integrada'),
    ('12345678000103', 'Centro Diagnóstico FUSEX'),
    ('12345678000104', 'Laboratório Especializado'),
    ('12345678000105', 'Hospital Universitário')
ON CONFLICT (cnpj) DO NOTHING;

INSERT INTO procedimento (id, nome) VALUES
    (5001, 'Consulta Médica Especializada'),
    (5002, 'Exame Laboratorial Complexo'),
    (5003, 'Procedimento Cirúrgico Simples'),
    (5004, 'Sessão de Fisioterapia'),
    (5005, 'Exame de Imagem')
ON CONFLICT (id) DO NOTHING;

-- 2. BENEFICIARIOS
INSERT INTO beneficiario (preccp, dependente, grupo, data_nasc, nome, ug, cotista, usuario_id) VALUES
    ('358495831', true, 'Exército Brasileiro - FUSEX', '2018-05-12', 'MARCOS LORENZO GOMES DE MORAES', 'Cmdo Bda Inf Amv', true, 358495830),
    ('358495832', false, 'Exército Brasileiro - FUSEX', '1985-03-20', 'MARCOS DOUGLAS DOS SANTOS MORAES', 'Cmdo Bda Inf Amv', true, 358495831),
    ('358495833', true, 'Exército Brasileiro - FUSEX', '2015-08-10', 'ANA LUCIA GOMES DE MORAES', 'Cmdo Bda Inf Amv', true, 358495832),
    ('358495834', false, 'Exército Brasileiro - FUSEX', '1990-11-04', 'CARLOS EDUARDO SILVA', 'Cmdo Bda Inf Amv', false, 358495833),
    ('358495835', true, 'Exército Brasileiro - FUSEX', '2020-01-15', 'BEATRIZ SILVA', 'Cmdo Bda Inf Amv', false, 358495834),
    ('358495836', false, 'Exército Brasileiro - FUSEX', '1982-07-25', 'ROBERTO ALMEIDA FERREIRA', 'Cmdo Bda Inf Amv', true, 358495835),
    ('358495837', true, 'Exército Brasileiro - FUSEX', '2012-09-30', 'LUCAS FERREIRA', 'Cmdo Bda Inf Amv', true, 358495836),
    ('358495838', false, 'Exército Brasileiro - FUSEX', '1995-12-18', 'FERNANDA OLIVEIRA SANTOS', 'Cmdo Bda Inf Amv', false, 358495837),
    ('358495839', false, 'Exército Brasileiro - FUSEX', '1988-02-14', 'GUSTAVO HENRIQUE LIMA', 'Cmdo Bda Inf Amv', true, 358495838),
    ('358495840', true, 'Exército Brasileiro - FUSEX', '2019-06-08', 'SOPHIA LIMA', 'Cmdo Bda Inf Amv', true, 358495839)
ON CONFLICT (preccp) DO NOTHING;

-- 3. PAPEIS DE USUARIOS
INSERT INTO funocs (id, ocs_id, usuario_id) VALUES
    (101, '12345678000101', 358495830),
    (102, '12345678000102', 358495831),
    (103, '12345678000103', 358495832)
ON CONFLICT (id) DO NOTHING;

INSERT INTO medico (id, crm, benef_id) VALUES
    (272865, '12345', '358495831'),
    (265664, '54321', '358495832'),
    (198432, '98765', '358495833'),
    (312001, '45678', '358495834'),
    (154890, '11223', '358495835'),
    (389112, '33221', '358495836')
ON CONFLICT (id) DO NOTHING;

INSERT INTO analista (id, benef_id) VALUES
    (101, '358495831'),
    (102, '358495832'),
    (103, '358495833')
ON CONFLICT (id) DO NOTHING;

INSERT INTO chefe_fusex (id, benef_id) VALUES
    (1, '358495831'),
    (2, '358495832')
ON CONFLICT (id) DO NOTHING;

INSERT INTO auditor (id, benef_id) VALUES
    (1, '358495831'),
    (2, '358495832'),
    (3, '358495833')
ON CONFLICT (id) DO NOTHING;

-- 4. CONTRATOS
INSERT INTO contrato (id, proc_id, ocs_id, valor) VALUES
    (1, 5001, '12345678000101', 15000.00),
    (2, 5002, '12345678000102', 28500.50),
    (3, 5003, '12345678000103', 42000.00),
    (4, 5004, '12345678000104', 9800.75),
    (5, 5005, '12345678000105', 63400.00)
ON CONFLICT (id) DO NOTHING;

-- 5. GUIAS + GUIA_PROC EXISTENTES
INSERT INTO guia (id, benf_id, med_id, emissor_id, chefe_id, pdf, status, data) VALUES
    (1, '358495831', 272865, 101, 1, 'guia_001.pdf', 'EMITIDA', '2026-01-10'),
    (2, '358495832', 265664, 102, 1, 'guia_002.pdf', 'EMITIDA', '2026-01-12'),
    (3, '358495833', 198432, 103, 1, 'guia_003.pdf', 'PENDENTE', '2026-01-15'),
    (4, '358495834', 312001, 101, 1, 'guia_004.pdf', 'EMITIDA', '2026-01-18'),
    (5, '358495835', 154890, 102, 1, 'guia_005.pdf', 'FINALIZADA', '2026-01-20')
ON CONFLICT (id) DO NOTHING;

INSERT INTO guia_proc (id, cont_id, guia_id, qr, status) VALUES
    (1001, 1, 1, 'QR1001', 'Realizada'),
    (1002, 2, 2, 'QR1002', 'Realizada'),
    (1003, 3, 3, 'QR1003', 'Pendente'),
    (1004, 4, 4, 'QR1004', 'Realizada'),
    (1005, 5, 5, 'QR1005', 'Realizada')
ON CONFLICT (id) DO NOTHING;

-- 6. NOVOS ATENDIMENTOS REALIZADOS DISPONIVEIS PARA CRIAR ESPELHO
-- Estes atendimentos nao possuem espelho e podem ser usados pelo frontend.
INSERT INTO guia (id, benf_id, med_id, emissor_id, chefe_id, pdf, status, data) VALUES
    (6, '358495836', 389112, 103, 2, 'guia_006.pdf', 'EMITIDA', '2026-06-01'),
    (7, '358495837', 272865, 101, 1, 'guia_007.pdf', 'EMITIDA', '2026-06-05'),
    (8, '358495838', 265664, 102, 1, 'guia_008.pdf', 'EMITIDA', '2026-06-10'),
    (9, '358495839', 198432, 103, 2, 'guia_009.pdf', 'EMITIDA', '2026-06-15'),
    (10, '358495840', 312001, 101, 1, 'guia_010.pdf', 'EMITIDA', '2026-06-20')
ON CONFLICT (id) DO NOTHING;

INSERT INTO guia_proc (id, cont_id, guia_id, qr, status) VALUES
    (1006, 1, 6, 'QR1006', 'Realizada'),
    (1007, 2, 7, 'QR1007', 'Realizada'),
    (1008, 3, 8, 'QR1008', 'Realizada'),
    (1009, 4, 9, 'QR1009', 'Realizada'),
    (1010, 5, 10, 'QR1010', 'Realizada')
ON CONFLICT (id) DO NOTHING;

-- 7. ESPELHOS JA EXISTENTES
INSERT INTO espelho (id, data_inicio, data_fim, ocs_id, funocs_id, auditor_id, atendimento_id) VALUES
    (1, '2026-01-10', '2026-01-20', '12345678000101', 101, 1, 1001),
    (2, '2026-02-01', '2026-02-15', '12345678000102', 102, 2, 1002),
    (3, '2026-03-05', '2026-03-25', '12345678000103', 101, 1, 1003),
    (4, '2026-04-10', '2026-04-30', '12345678000104', 103, 3, 1004),
    (5, '2026-05-01', '2026-05-18', '12345678000105', 102, 2, 1005)
ON CONFLICT (id) DO NOTHING;

-- 8. ITENS DOS ESPELHOS
INSERT INTO espelho_item (id, espelho_id, atendimento_id, beneficiario_id, descricao, valor) VALUES
    (1, 1, 1001, '358495831', 'Consulta Médica Especializada', 16200.00),
    (2, 2, 1002, '358495832', 'Exame Laboratorial Complexo', 27000.00),
    (3, 3, 1003, '358495833', 'Procedimento Cirúrgico Simples', 42000.00),
    (4, 4, 1004, '358495834', 'Sessão de Fisioterapia', 10500.00),
    (5, 5, 1005, '358495835', 'Exame de Imagem', 60000.00)
ON CONFLICT (id) DO NOTHING;

-- 9. FATURAS EXISTENTES
-- A Fatura 1 fica pronta para teste de regeracao:
-- data_geracao = ontem e data_atualizacao do Espelho 1 = agora.
INSERT INTO fatura (id, guia_proc_id, esp_fat, status, data_geracao) VALUES
    (1, 1001, 1, 'ATIVA', NOW() - INTERVAL '1 day'),
    (2, 1002, 2, 'ATIVA', NOW()),
    (4, 1004, 4, 'ATIVA', NOW()),
    (5, 1005, 5, 'ATIVA', NOW())
ON CONFLICT (id) DO NOTHING;

-- 10. ITENS DAS FATURAS EXISTENTES
-- Mantem o GET /faturas com itens no mock e permite conferir divergencias.
INSERT INTO item_fatura (
    id,
    fatura_id,
    espelho_item_id,
    valor_apresentado,
    valor_contratado,
    divergencia_valor
) VALUES
    (1, 1, 1, 16200.00, 15000.00, 1200.00),
    (2, 2, 2, 27000.00, 28500.50, -1500.50),
    (4, 4, 4, 10500.00, 9800.75, 699.25),
    (5, 5, 5, 60000.00, 63400.00, -3400.00)
ON CONFLICT (id) DO NOTHING;

-- 11. COLOCA OS ESPELHOS FATURADOS NO ESTADO CORRETO
-- O Espelho 1 tambem recebe data_atualizacao posterior a data_geracao da Fatura 1,
-- deixando o cenario pronto para POST /faturas/1/regerar.
UPDATE espelho
SET status = 'FATURADO'
WHERE id IN (1, 2, 4, 5);

UPDATE espelho
SET status = 'EM_ABERTO',
    data_atualizacao = NULL
WHERE id IN (3);

UPDATE espelho
SET status = 'FATURADO',
    data_atualizacao = NOW()
WHERE id = 1;

-- Garante que a Fatura 1 continue anterior a data de atualizacao do Espelho 1.
UPDATE fatura
SET status = 'ATIVA',
    data_geracao = NOW() - INTERVAL '1 day',
    fatura_origem_id = NULL
WHERE id = 1;

-- A Fatura 2, 4 e 5 permanecem ativas, mas sem data_atualizacao posterior no espelho,
-- portanto nao estao prontas para regeracao.
UPDATE fatura
SET status = 'ATIVA',
    fatura_origem_id = NULL
WHERE id IN (2, 4, 5);

-- =========================================================
-- 12. AJUSTA AS SEQUENCES (IDENTITY)
-- =========================================================
SELECT setval(pg_get_serial_sequence('usuario', 'id'), (SELECT MAX(id) FROM usuario));
SELECT setval(pg_get_serial_sequence('procedimento', 'id'), (SELECT MAX(id) FROM procedimento));
SELECT setval(pg_get_serial_sequence('funocs', 'id'), (SELECT MAX(id) FROM funocs));
SELECT setval(pg_get_serial_sequence('medico', 'id'), (SELECT MAX(id) FROM medico));
SELECT setval(pg_get_serial_sequence('analista', 'id'), (SELECT MAX(id) FROM analista));
SELECT setval(pg_get_serial_sequence('chefe_fusex', 'id'), (SELECT MAX(id) FROM chefe_fusex));
SELECT setval(pg_get_serial_sequence('auditor', 'id'), (SELECT MAX(id) FROM auditor));
SELECT setval(pg_get_serial_sequence('contrato', 'id'), (SELECT MAX(id) FROM contrato));
SELECT setval(pg_get_serial_sequence('guia', 'id'), (SELECT MAX(id) FROM guia));
SELECT setval(pg_get_serial_sequence('guia_proc', 'id'), (SELECT MAX(id) FROM guia_proc));
SELECT setval(pg_get_serial_sequence('espelho', 'id'), (SELECT MAX(id) FROM espelho));
SELECT setval(pg_get_serial_sequence('espelho_item', 'id'), (SELECT MAX(id) FROM espelho_item));
SELECT setval(pg_get_serial_sequence('fatura', 'id'), (SELECT MAX(id) FROM fatura));
SELECT setval(pg_get_serial_sequence('item_fatura', 'id'), (SELECT MAX(id) FROM item_fatura));

