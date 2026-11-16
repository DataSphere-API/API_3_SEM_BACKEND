-- US000-16: um usuário de teste para cada perfil. Login = CPF, senha de todos = fusex123 (BCrypt).
-- Versão exclusiva: V15 já pertence ao QR por procedimento.
-- Executar antes dos seeds opcionais de consulta QR (V17).
-- Fica em db/dev (só carrega com o perfil dev). Sem IDs fixos: o seed do perfil cadastros-mock também insere usuários.
INSERT INTO Usuario (Login, Senha) VALUES
                                       ('52601815906', '$2a$10$nB/cKLQjqlQqZVheNQrkOO66i9qAzvN0nmY5.yYBFyt3t.aK/u/8a'), -- CHEFE + BENEFICIARIO
                                       ('08301661305', '$2a$10$nB/cKLQjqlQqZVheNQrkOO66i9qAzvN0nmY5.yYBFyt3t.aK/u/8a'), -- MEDICO + BENEFICIARIO
                                       ('18609139034', '$2a$10$nB/cKLQjqlQqZVheNQrkOO66i9qAzvN0nmY5.yYBFyt3t.aK/u/8a'), -- EMISSOR + BENEFICIARIO
                                       ('99603082430', '$2a$10$nB/cKLQjqlQqZVheNQrkOO66i9qAzvN0nmY5.yYBFyt3t.aK/u/8a'), -- AUDITOR + BENEFICIARIO
                                       ('62819482112', '$2a$10$nB/cKLQjqlQqZVheNQrkOO66i9qAzvN0nmY5.yYBFyt3t.aK/u/8a'), -- OCS
                                       ('99351819019', '$2a$10$nB/cKLQjqlQqZVheNQrkOO66i9qAzvN0nmY5.yYBFyt3t.aK/u/8a'); -- BENEFICIARIO

UPDATE Beneficiario SET Usuario_ID = (SELECT ID FROM Usuario WHERE Login = '52601815906') WHERE PrecCP = '444444444-00';
UPDATE Beneficiario SET Usuario_ID = (SELECT ID FROM Usuario WHERE Login = '08301661305') WHERE PrecCP = '222222222-00';
UPDATE Beneficiario SET Usuario_ID = (SELECT ID FROM Usuario WHERE Login = '18609139034') WHERE PrecCP = '555555555-00';
UPDATE Beneficiario SET Usuario_ID = (SELECT ID FROM Usuario WHERE Login = '99603082430') WHERE PrecCP = '666666666-00';
UPDATE Beneficiario SET Usuario_ID = (SELECT ID FROM Usuario WHERE Login = '99351819019') WHERE PrecCP = '123456789-00';

INSERT INTO FunOcs (Cargo, Ocs_ID, Usuario_ID)
VALUES ('Recepção', '12345678000199', (SELECT ID FROM Usuario WHERE Login = '62819482112'));

-- 111111111-00 (Beltrano, 1979-07-22) e 333333333-00 (Ana Souza) ficam sem login para testar o autocadastro.
