-- #15: referências de preço para testar a lisura em desenvolvimento.
-- Tabela de parâmetros UG-FUSEx, códigos 41001109 e 41001117.
INSERT INTO Ocs (Cnpj, Nome)
VALUES ('00000000000015', 'OCS de teste da lisura')
ON CONFLICT (Cnpj) DO NOTHING;

INSERT INTO Procedimento (Nome)
SELECT 'TC Pelve ou Bacia'
WHERE NOT EXISTS (SELECT 1 FROM Procedimento WHERE Nome = 'TC Pelve ou Bacia');

INSERT INTO Contrato (Proc_ID, Ocs_ID, Valor)
SELECT procedimento.id, '00000000000015', referencia.valor
FROM (VALUES ('TC Abdome Superior', 440.10), ('TC Pelve ou Bacia', 413.10)) AS referencia(nome, valor)
CROSS JOIN LATERAL (
    SELECT id
    FROM Procedimento
    WHERE nome = referencia.nome
    ORDER BY id
    LIMIT 1
) AS procedimento
WHERE NOT EXISTS (
    SELECT 1
    FROM Contrato
    WHERE Proc_ID = procedimento.id AND Ocs_ID = '00000000000015'
);
