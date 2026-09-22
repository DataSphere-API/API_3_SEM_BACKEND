-- 1. TABELAS BASE (Sem dependências)
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

-- 2. BENEFICIÁRIOS (Depende de usuario)
INSERT INTO beneficiario (preccp, dependente, grupo, data_nasc, nome, ug, cotista, usuario_id) VALUES
                                                                                                   (358495831, true, 'Exército Brasileiro - FUSEX', '2018-05-12', 'MARCOS LORENZO GOMES DE MORAES', 'Cmdo Bda Inf Amv', true, 358495830),
                                                                                                   (358495832, false, 'Exército Brasileiro - FUSEX', '1985-03-20', 'MARCOS DOUGLAS DOS SANTOS MORAES', 'Cmdo Bda Inf Amv', true, 358495831),
                                                                                                   (358495833, true, 'Exército Brasileiro - FUSEX', '2015-08-10', 'ANA LUCIA GOMES DE MORAES', 'Cmdo Bda Inf Amv', true, 358495832),
                                                                                                   (358495834, false, 'Exército Brasileiro - FUSEX', '1990-11-04', 'CARLOS EDUARDO SILVA', 'Cmdo Bda Inf Amv', false, 358495833),
                                                                                                   (358495835, true, 'Exército Brasileiro - FUSEX', '2020-01-15', 'BEATRIZ SILVA', 'Cmdo Bda Inf Amv', false, 358495834),
                                                                                                   (358495836, false, 'Exército Brasileiro - FUSEX', '1982-07-25', 'ROBERTO ALMEIDA FERREIRA', 'Cmdo Bda Inf Amv', true, 358495835),
                                                                                                   (358495837, true, 'Exército Brasileiro - FUSEX', '2012-09-30', 'LUCAS FERREIRA', 'Cmdo Bda Inf Amv', true, 358495836),
                                                                                                   (358495838, false, 'Exército Brasileiro - FUSEX', '1995-12-18', 'FERNANDA OLIVEIRA SANTOS', 'Cmdo Bda Inf Amv', false, 358495837),
                                                                                                   (358495839, false, 'Exército Brasileiro - FUSEX', '1988-02-14', 'GUSTAVO HENRIQUE LIMA', 'Cmdo Bda Inf Amv', true, 358495838),
                                                                                                   (358495840, true, 'Exército Brasileiro - FUSEX', '2019-06-08', 'SOPHIA LIMA', 'Cmdo Bda Inf Amv', true, 358495839)
    ON CONFLICT (preccp) DO NOTHING;

-- 3. PAPÉIS DE USUÁRIOS (Dependem de ocs, usuario ou beneficiario)
INSERT INTO funocs (id, ocs_id, usuario_id) VALUES
                                                (101, '12345678000101', 358495830),
                                                (102, '12345678000102', 358495831),
                                                (103, '12345678000103', 358495832)
    ON CONFLICT (id) DO NOTHING;

INSERT INTO medico (id, CRM, benef_id) VALUES
                                           (272865, '12345', 358495831),
                                           (265664, '54321', 358495832),
                                           (198432, '98765', 358495833),
                                           (312001, '45678', 358495834),
                                           (154890, '11223', 358495835),
                                           (389112, '33221', 358495836)
    ON CONFLICT (id) DO NOTHING;

INSERT INTO analista (id, benef_id) VALUES
                                        (101, 358495831),
                                        (102, 358495832),
                                        (103, 358495833)
    ON CONFLICT (id) DO NOTHING;

INSERT INTO chefe_fusex (id, benef_id) VALUES
                                           (1, 358495831),
                                           (2, 358495832)
    ON CONFLICT (id) DO NOTHING;

INSERT INTO auditor (id, benef_id) VALUES
                                       (1, 358495831),
                                       (2, 358495832),
                                       (3, 358495833)
    ON CONFLICT (id) DO NOTHING;

-- 4. CONTRATOS E ESPELHOS
INSERT INTO contrato (id, proc_id, ocs_id, valor) VALUES
                                                      (1, 5001, '12345678000101', 15000.00),
                                                      (2, 5002, '12345678000102', 28500.50),
                                                      (3, 5003, '12345678000103', 42000.00),
                                                      (4, 5004, '12345678000104', 9800.75),
                                                      (5, 5005, '12345678000105', 63400.00)
    ON CONFLICT (id) DO NOTHING;

INSERT INTO espelho (id, data_inicio, data_fim, ocs_id, funocs_id, auditor_id) VALUES
                                                                                   (1, '2026-01-10', '2026-01-20', '12345678000101', 101, 1),
                                                                                   (2, '2026-02-01', '2026-02-15', '12345678000102', 102, 2),
                                                                                   (3, '2026-03-05', '2026-03-25', '12345678000103', 101, 1),
                                                                                   (4, '2026-04-10', '2026-04-30', '12345678000104', 103, 3),
                                                                                   (5, '2026-05-01', '2026-05-18', '12345678000105', 102, 2)
    ON CONFLICT (id) DO NOTHING;

-- 5. GUIAS, GUIA_PROC E FATURA (Cadeia final)
INSERT INTO guia (id, benf_id, med_id, emissor_id, chefe_id, pdf, status, data) VALUES
                                                                                    (1, '358495831', 272865, 101, 1, 'guia_001.pdf', 'EMITIDA', '2026-01-10'),
                                                                                    (2, '358495832', 265664, 102, 1, 'guia_002.pdf', 'EMITIDA', '2026-01-12'),
                                                                                    (3, '358495833', 198432, 103, 1, 'guia_003.pdf', 'PENDENTE', '2026-01-15'),
                                                                                    (4, '358495834', 312001, 101, 1, 'guia_004.pdf', 'EMITIDA', '2026-01-18'),
                                                                                    (5, '358495835', 154890, 102, 1, 'guia_005.pdf', 'FINALIZADA', '2026-01-20')
    ON CONFLICT (id) DO NOTHING;

INSERT INTO guia_proc (id, cont_id, guia_id, qr, status) VALUES
                                                             (1001, 1, 1, 'QR1001', 'AUTORIZADO'),
                                                             (1002, 2, 2, 'QR1002', 'AUTORIZADO'),
                                                             (1003, 3, 3, 'QR1003', 'PENDENTE'),
                                                             (1004, 4, 4, 'QR1004', 'AUTORIZADO'),
                                                             (1005, 5, 5, 'QR1005', 'FINALIZADO')
    ON CONFLICT (id) DO NOTHING;

INSERT INTO fatura (id, guia_proc_id, esp_fat) VALUES
                                                   (1, 1001, 1),
                                                   (2, 1002, 2),
                                                   (3, 1003, 3),
                                                   (4, 1004, 4),
                                                   (5, 1005, 5)
    ON CONFLICT (id) DO NOTHING;