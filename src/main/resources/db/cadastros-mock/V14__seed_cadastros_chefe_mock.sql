-- Dados fictícios exclusivos dos testes locais da US000-17.
-- Não existe endpoint de login nesta task; os logins usam X-Mock-Login.
-- A V12 pode ter sido carregada após a V13 e insere IDs explícitos de médicos e chefes.
SELECT setval(pg_get_serial_sequence('medico', 'id'),
    GREATEST(COALESCE((SELECT MAX(id) FROM medico), 1), (SELECT last_value FROM medico_id_seq)));
SELECT setval(pg_get_serial_sequence('chefe_fusex', 'id'),
    GREATEST(COALESCE((SELECT MAX(id) FROM chefe_fusex), 1), (SELECT last_value FROM chefe_fusex_id_seq)));

INSERT INTO usuario (login, senha) VALUES
    ('00000000001', 'US00017-MOCK-SEM-LOGIN'),
    ('00000000002', 'US00017-MOCK-SEM-LOGIN'),
    ('00000000003', 'US00017-MOCK-SEM-LOGIN');

INSERT INTO beneficiario (preccp, nome, data_nasc, dependente, ug, cotista, usuario_id) VALUES
    ('US17-CHEFE', 'Chefe de teste US000-17', '1980-01-01', FALSE, 'UG Teste', FALSE,
        (SELECT id FROM usuario WHERE login = '00000000001')),
    ('US17-MILITAR', 'Militar com autocadastro simulado', '1986-05-10', FALSE, '2ª Bda', FALSE,
        (SELECT id FROM usuario WHERE login = '00000000002')),
    ('US17-MEDICO', 'Maria de teste US000-17', '1985-01-01', FALSE, 'UG Teste', FALSE,
        (SELECT id FROM usuario WHERE login = '00000000003')),
    ('US17-PENDENTE', 'Militar sem primeiro acesso', '1990-01-01', FALSE, 'UG Teste', FALSE, NULL);

INSERT INTO chefe_fusex (benef_id) VALUES ('US17-CHEFE');
INSERT INTO medico (benef_id, crm) VALUES ('US17-MEDICO', 'MOCK-US17');
