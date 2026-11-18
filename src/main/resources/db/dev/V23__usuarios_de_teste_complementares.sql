-- Logins que faltavam para testar todos os perfis sem mock. Senha de todos: fusex123 (BCrypt).
-- 111111111-00 (Beltrano) continua sem login para testar o autocadastro.
INSERT INTO Usuario (Login, Senha) VALUES
                                       ('73140628544', '$2a$10$nB/cKLQjqlQqZVheNQrkOO66i9qAzvN0nmY5.yYBFyt3t.aK/u/8a'), -- MEDICO 2 (Ana Souza) + BENEFICIARIO
                                       ('45091836233', '$2a$10$nB/cKLQjqlQqZVheNQrkOO66i9qAzvN0nmY5.yYBFyt3t.aK/u/8a'), -- OCS Hospital Y
                                       ('28675310471', '$2a$10$nB/cKLQjqlQqZVheNQrkOO66i9qAzvN0nmY5.yYBFyt3t.aK/u/8a'); -- OCS Laboratório Z

UPDATE Beneficiario SET Usuario_ID = (SELECT ID FROM Usuario WHERE Login = '73140628544') WHERE PrecCP = '333333333-00';

INSERT INTO FunOcs (Cargo, Ocs_ID, Usuario_ID) VALUES
                                                   ('Recepção', '98765432000111', (SELECT ID FROM Usuario WHERE Login = '45091836233')),
                                                   ('Recepção', '11222333000144', (SELECT ID FROM Usuario WHERE Login = '28675310471'));
