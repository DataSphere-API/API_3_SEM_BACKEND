-- US003-6: vínculos fictícios para testar chefe, emissor e médico.
INSERT INTO usuario (login, senha) VALUES
    ('00000000353', 'US0036-MOCK-SEM-LOGIN'),
    ('00000000354', 'US0036-MOCK-SEM-LOGIN'),
    ('00000000355', 'US0036-MOCK-SEM-LOGIN');

UPDATE beneficiario SET usuario_id = (SELECT id FROM usuario WHERE login = '00000000353')
WHERE preccp = '444444444-00';
UPDATE beneficiario SET usuario_id = (SELECT id FROM usuario WHERE login = '00000000354')
WHERE preccp = '555555555-00';
UPDATE beneficiario SET usuario_id = (SELECT id FROM usuario WHERE login = '00000000355')
WHERE preccp = '222222222-00';
