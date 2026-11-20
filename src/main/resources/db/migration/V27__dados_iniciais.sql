-- Dados iniciais para usar o sistema em qualquer banco, sem perfil: OCS, procedimentos, contratos
-- e um usuário de cada perfil (chefe, médico, emissor, auditor, OCS e beneficiário).
-- Login = CPF, senha de todos = fusex123 (BCrypt). Troque as senhas antes de usar com dados reais.
-- Os IDs são os mesmos do seed de db/dev. ON CONFLICT DO NOTHING deixa a migration rodar também
-- depois do seed de db/dev (perfil dev), sem duplicar nada.

INSERT INTO Ocs (CNPJ, Nome) VALUES
    ('12345678000199', 'Clínica X'),
    ('98765432000111', 'Hospital Y'),
    ('11222333000144', 'Laboratório Z')
ON CONFLICT DO NOTHING;

INSERT INTO Procedimento (ID, Nome) VALUES
    (1, 'Sessão de psicoterapia'),
    (2, 'TC Abdome Superior'),
    (3, 'Consulta cardiológica'),
    (4, 'Hemograma completo'),
    (5, 'Sessão de fisioterapia'),
    (6, 'Ressonância magnética de joelho')
ON CONFLICT DO NOTHING;

INSERT INTO Contrato (ID, Proc_ID, Ocs_ID, Valor) VALUES
    (1, 1, '12345678000199', 150.00),
    (2, 1, '98765432000111', 160.00),
    (3, 2, '98765432000111', 720.49),
    (4, 3, '98765432000111', 250.00),
    (5, 4, '11222333000144', 35.00),
    (6, 5, '12345678000199', 80.00)
ON CONFLICT DO NOTHING;

-- Beltrano (111111111-00) fica sem login para o autocadastro.
INSERT INTO Beneficiario (PrecCP, Dependente, Grupo, Data_Nasc, Nome, UG, Cotista) VALUES
    ('123456789-00', FALSE, 'Titular', '1986-03-10', 'Fulano de Tal',     'UG Caçapava', TRUE),
    ('111111111-00', FALSE, 'Titular', '1979-07-22', 'Beltrano da Silva', 'UG Caçapava', TRUE),
    ('222222222-00', FALSE, 'Titular', '1975-01-15', 'Carlos Medeiros',   'UG Caçapava', TRUE),
    ('333333333-00', FALSE, 'Titular', '1982-09-30', 'Ana Souza',         'UG Caçapava', TRUE),
    ('444444444-00', FALSE, 'Titular', '1970-05-05', 'Roberto Lima',      'UG Caçapava', TRUE),
    ('555555555-00', FALSE, 'Titular', '1990-11-18', 'Mariana Costa',     'UG Caçapava', TRUE),
    ('666666666-00', FALSE, 'Titular', '1984-02-27', 'Paulo Andrade',     'UG Caçapava', TRUE)
ON CONFLICT DO NOTHING;

INSERT INTO Medico (ID, CRM, Benef_ID) VALUES
    (1, 'SP123456', '222222222-00'),
    (2, 'SP654321', '333333333-00')
ON CONFLICT DO NOTHING;

INSERT INTO Chefe_Fusex (ID, Benef_ID) VALUES (1, '444444444-00') ON CONFLICT DO NOTHING;
INSERT INTO Analista (ID, Benef_ID) VALUES (1, '555555555-00') ON CONFLICT DO NOTHING;
INSERT INTO Auditor (ID, Benef_ID) VALUES (1, '666666666-00') ON CONFLICT DO NOTHING;

INSERT INTO Usuario (Login, Senha) VALUES
    ('52601815906', '$2a$10$nB/cKLQjqlQqZVheNQrkOO66i9qAzvN0nmY5.yYBFyt3t.aK/u/8a'), -- Roberto Lima: CHEFE
    ('08301661305', '$2a$10$nB/cKLQjqlQqZVheNQrkOO66i9qAzvN0nmY5.yYBFyt3t.aK/u/8a'), -- Carlos Medeiros: MEDICO
    ('73140628544', '$2a$10$nB/cKLQjqlQqZVheNQrkOO66i9qAzvN0nmY5.yYBFyt3t.aK/u/8a'), -- Ana Souza: MEDICO
    ('18609139034', '$2a$10$nB/cKLQjqlQqZVheNQrkOO66i9qAzvN0nmY5.yYBFyt3t.aK/u/8a'), -- Mariana Costa: EMISSOR
    ('99603082430', '$2a$10$nB/cKLQjqlQqZVheNQrkOO66i9qAzvN0nmY5.yYBFyt3t.aK/u/8a'), -- Paulo Andrade: AUDITOR
    ('99351819019', '$2a$10$nB/cKLQjqlQqZVheNQrkOO66i9qAzvN0nmY5.yYBFyt3t.aK/u/8a'), -- Fulano de Tal: BENEFICIARIO
    ('62819482112', '$2a$10$nB/cKLQjqlQqZVheNQrkOO66i9qAzvN0nmY5.yYBFyt3t.aK/u/8a'), -- OCS Clínica X
    ('45091836233', '$2a$10$nB/cKLQjqlQqZVheNQrkOO66i9qAzvN0nmY5.yYBFyt3t.aK/u/8a'), -- OCS Hospital Y
    ('28675310471', '$2a$10$nB/cKLQjqlQqZVheNQrkOO66i9qAzvN0nmY5.yYBFyt3t.aK/u/8a')  -- OCS Laboratório Z
ON CONFLICT DO NOTHING;

UPDATE Beneficiario b SET Usuario_ID = u.ID
FROM (VALUES
    ('444444444-00', '52601815906'),
    ('222222222-00', '08301661305'),
    ('333333333-00', '73140628544'),
    ('555555555-00', '18609139034'),
    ('666666666-00', '99603082430'),
    ('123456789-00', '99351819019')
) AS vinculo(prec_cp, login)
JOIN Usuario u ON u.Login = vinculo.login
WHERE b.PrecCP = vinculo.prec_cp
  AND b.Usuario_ID IS NULL
  AND NOT EXISTS (SELECT 1 FROM Beneficiario outro WHERE outro.Usuario_ID = u.ID);

INSERT INTO FunOcs (Cargo, Ocs_ID, Usuario_ID)
SELECT 'Recepção', vinculo.cnpj, u.ID
FROM (VALUES
    ('12345678000199', '62819482112'),
    ('98765432000111', '45091836233'),
    ('11222333000144', '28675310471')
) AS vinculo(cnpj, login)
JOIN Usuario u ON u.Login = vinculo.login
ON CONFLICT DO NOTHING;

-- Os próximos cadastros feitos pelas telas continuam a numeração depois dos IDs fixos.
SELECT setval(pg_get_serial_sequence('procedimento', 'id'), (SELECT MAX(id) FROM procedimento));
SELECT setval(pg_get_serial_sequence('contrato', 'id'), (SELECT MAX(id) FROM contrato));
SELECT setval(pg_get_serial_sequence('medico', 'id'), (SELECT MAX(id) FROM medico));
SELECT setval(pg_get_serial_sequence('chefe_fusex', 'id'), (SELECT MAX(id) FROM chefe_fusex));
SELECT setval(pg_get_serial_sequence('analista', 'id'), (SELECT MAX(id) FROM analista));
SELECT setval(pg_get_serial_sequence('auditor', 'id'), (SELECT MAX(id) FROM auditor));
