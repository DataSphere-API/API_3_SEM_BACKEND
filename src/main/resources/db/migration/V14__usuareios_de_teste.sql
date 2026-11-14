INSERT INTO Usuario (ID, Login, Senha) VALUES
                                           (1, '52601815906', '$2a$10$nB/cKLQjqlQqZVheNQrkOO66i9qAzvN0nmY5.yYBFyt3t.aK/u/8a'), -- CHEFE + BENEFICIARIO
                                           (2, '08301661305', '$2a$10$nB/cKLQjqlQqZVheNQrkOO66i9qAzvN0nmY5.yYBFyt3t.aK/u/8a'), -- MEDICO + BENEFICIARIO
                                           (3, '18609139034', '$2a$10$nB/cKLQjqlQqZVheNQrkOO66i9qAzvN0nmY5.yYBFyt3t.aK/u/8a'), -- EMISSOR + BENEFICIARIO
                                           (4, '99603082430', '$2a$10$nB/cKLQjqlQqZVheNQrkOO66i9qAzvN0nmY5.yYBFyt3t.aK/u/8a'), -- AUDITOR + BENEFICIARIO
                                           (5, '62819482112', '$2a$10$nB/cKLQjqlQqZVheNQrkOO66i9qAzvN0nmY5.yYBFyt3t.aK/u/8a'), -- OCS
                                           (6, '99351819019', '$2a$10$nB/cKLQjqlQqZVheNQrkOO66i9qAzvN0nmY5.yYBFyt3t.aK/u/8a'); -- BENEFICIARIO


UPDATE Beneficiario SET Usuario_ID = 1 WHERE PrecCP = '444444444-00'; -- Roberto Lima  (Chefe_Fusex 1)
UPDATE Beneficiario SET Usuario_ID = 2 WHERE PrecCP = '222222222-00'; -- Carlos Medeiros (Medico 1)
UPDATE Beneficiario SET Usuario_ID = 3 WHERE PrecCP = '555555555-00'; -- Mariana Costa (Analista 1)
UPDATE Beneficiario SET Usuario_ID = 4 WHERE PrecCP = '666666666-00'; -- Paulo Andrade (Auditor 1)
UPDATE Beneficiario SET Usuario_ID = 6 WHERE PrecCP = '123456789-00'; -- Fulano de Tal (dono da guia 1)

INSERT INTO FunOcs (ID, Cargo, Ocs_ID, Usuario_ID) VALUES (1, 'Recepção', '12345678000199', 5);


SELECT setval(pg_get_serial_sequence('usuario', 'id'), (SELECT MAX(ID) FROM Usuario));
SELECT setval(pg_get_serial_sequence('funocs', 'id'), (SELECT MAX(ID) FROM FunOcs));