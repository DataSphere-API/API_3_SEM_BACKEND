-- US000-15 / US005-1: carga de dados de teste (IDs fixos, publicados na Wiki).

INSERT INTO Ocs (CNPJ, Nome) VALUES
                                 ('12345678000199', 'Clínica X'),
                                 ('98765432000111', 'Hospital Y'),
                                 ('11222333000144', 'Laboratório Z');

INSERT INTO Procedimento (ID, Nome) VALUES
                                        (1, 'Sessão de psicoterapia'),
                                        (2, 'TC Abdome Superior'),
                                        (3, 'Consulta cardiológica'),
                                        (4, 'Hemograma completo'),
                                        (5, 'Sessão de fisioterapia'),
                                        (6, 'Ressonância magnética de joelho');

-- Procedimento 6 não tem contrato com nenhuma OCS (serve para testar a pendência PROCEDIMENTO_SEM_CONTRATO).
-- Psicoterapia tem contrato com duas OCS, com valores diferentes.
INSERT INTO Contrato (ID, Proc_ID, Ocs_ID, Valor) VALUES
                                                      (1, 1, '12345678000199', 150.00),
                                                      (2, 1, '98765432000111', 160.00),
                                                      (3, 2, '98765432000111', 720.49),
                                                      (4, 3, '98765432000111', 250.00),
                                                      (5, 4, '11222333000144', 35.00),
                                                      (6, 5, '12345678000199', 80.00);

INSERT INTO Beneficiario (PrecCP, Dependente, Grupo, Data_Nasc, Nome, UG, Cotista) VALUES
                                                                                       ('123456789-00', FALSE, 'Titular', '1986-03-10', 'Fulano de Tal',       'UG Caçapava', TRUE),
                                                                                       ('111111111-00', FALSE, 'Titular', '1979-07-22', 'Beltrano da Silva',   'UG Caçapava', TRUE),
                                                                                       ('222222222-00', FALSE, 'Titular', '1975-01-15', 'Carlos Medeiros',     'UG Caçapava', TRUE),
                                                                                       ('333333333-00', FALSE, 'Titular', '1982-09-30', 'Ana Souza',           'UG Caçapava', TRUE),
                                                                                       ('444444444-00', FALSE, 'Titular', '1970-05-05', 'Roberto Lima',        'UG Caçapava', TRUE),
                                                                                       ('555555555-00', FALSE, 'Titular', '1990-11-18', 'Mariana Costa',       'UG Caçapava', TRUE),
                                                                                       ('666666666-00', FALSE, 'Titular', '1984-02-27', 'Paulo Andrade',       'UG Caçapava', TRUE);

INSERT INTO Medico (ID, CRM, Benef_ID) VALUES
                                           (1, 'SP123456', '222222222-00'),
                                           (2, 'SP654321', '333333333-00');

INSERT INTO Chefe_Fusex (ID, Benef_ID) VALUES (1, '444444444-00');
INSERT INTO Analista (ID, Benef_ID) VALUES (1, '555555555-00');
INSERT INTO Auditor (ID, Benef_ID) VALUES (1, '666666666-00');

-- Pré-guias (US005-1): 4 VALIDADA, 1 COM_PENDENCIAS e 1 PRE_GUIA.
INSERT INTO Pre_Guia (ID, Status, Benf_ID, Med_ID, Emissor_ID, Data_Criacao, Data_Aprovacao_Medico) VALUES
                                                                                                        (1, 'VALIDADA',       '123456789-00', 1, 1, CURRENT_DATE - 3, NULL),
                                                                                                        (2, 'VALIDADA',       '111111111-00', 1, 1, CURRENT_DATE - 3, CURRENT_TIMESTAMP - INTERVAL '1 day'),
                                                                                                        (3, 'VALIDADA',       '123456789-00', 2, 1, CURRENT_DATE - 2, NULL),
                                                                                                        (4, 'VALIDADA',       '111111111-00', 2, 1, CURRENT_DATE - 2, CURRENT_TIMESTAMP - INTERVAL '1 day'),
                                                                                                        (5, 'COM_PENDENCIAS', '123456789-00', 1, 1, CURRENT_DATE - 1, NULL),
                                                                                                        (6, 'PRE_GUIA',       '111111111-00', 1, 1, CURRENT_DATE,     NULL);

INSERT INTO Pre_Guia_Proc (Pre_Guia_ID, Proc_ID, Ocs_ID, Qtd_Autorizada) VALUES
                                                                             (1, 1, '12345678000199', 10),
                                                                             (1, 4, '11222333000144', 1),
                                                                             (2, 2, '98765432000111', 1),
                                                                             (3, 3, '98765432000111', 1),
                                                                             (4, 5, '12345678000199', 5),
                                                                             (5, 6, '98765432000111', 1),
                                                                             (6, 1, '98765432000111', 4);

-- Guias já emitidas (para testar US003, US004 e a expiração da US004-7).
-- Guia 1: emitida há 8 dias, psicoterapia em andamento (3 de 10 sessões) e hemograma pendente.
-- Guia 2: emitida há 70 dias (vencida), para testar o job de expiração.
INSERT INTO Guia (ID, Benf_ID, Med_ID, Emissor_ID, Chefe_ID, Status, Data_Emissao) VALUES
                                                                                       (1, '123456789-00', 1, 1, 1, 'EMITIDA', CURRENT_DATE - 8),
                                                                                       (2, '111111111-00', 1, 1, 1, 'EMITIDA', CURRENT_DATE - 70);

INSERT INTO Guia_Proc (ID, Cont_ID, Guia_ID, QR, Status, Qtd_Autorizada, Qtd_Realizada, Data_Validade) VALUES
                                                                                                           (1, 1, 1, 'QR-TESTE-PSICO-001', 'EM_ANDAMENTO', 10, 3, CURRENT_DATE - 8 + 60),
                                                                                                           (2, 5, 1, 'QR-TESTE-HEMO-001',  'PENDENTE',      1, 0, CURRENT_DATE - 8 + 60),
                                                                                                           (3, 4, 2, 'QR-TESTE-CARDIO-001', 'PENDENTE',     1, 0, CURRENT_DATE - 70 + 60);

INSERT INTO Sessao_Procedimento (Guia_Proc_ID, Numero_Sessao, Data_Realizacao, Ocs_ID) VALUES
                                                                                           (1, 1, CURRENT_TIMESTAMP - INTERVAL '7 days', '12345678000199'),
                                                                                           (1, 2, CURRENT_TIMESTAMP - INTERVAL '5 days', '12345678000199'),
                                                                                           (1, 3, CURRENT_TIMESTAMP - INTERVAL '3 days', '12345678000199');

-- Ajusta os contadores depois de inserir IDs fixos.
SELECT setval(pg_get_serial_sequence('procedimento', 'id'), (SELECT MAX(ID) FROM Procedimento));
SELECT setval(pg_get_serial_sequence('contrato', 'id'), (SELECT MAX(ID) FROM Contrato));
SELECT setval(pg_get_serial_sequence('medico', 'id'), (SELECT MAX(ID) FROM Medico));
SELECT setval(pg_get_serial_sequence('chefe_fusex', 'id'), (SELECT MAX(ID) FROM Chefe_Fusex));
SELECT setval(pg_get_serial_sequence('analista', 'id'), (SELECT MAX(ID) FROM Analista));
SELECT setval(pg_get_serial_sequence('auditor', 'id'), (SELECT MAX(ID) FROM Auditor));
SELECT setval(pg_get_serial_sequence('pre_guia', 'id'), (SELECT MAX(ID) FROM Pre_Guia));
SELECT setval(pg_get_serial_sequence('guia', 'id'), (SELECT MAX(ID) FROM Guia));
SELECT setval(pg_get_serial_sequence('guia_proc', 'id'), (SELECT MAX(ID) FROM Guia_Proc));