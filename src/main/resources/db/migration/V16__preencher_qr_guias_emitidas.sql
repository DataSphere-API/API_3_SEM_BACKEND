-- US003-5: completar somente códigos ausentes em guias que já passaram pela emissão.
-- gen_random_uuid() é nativo do PostgreSQL 16 utilizado pelo projeto.
UPDATE guia_proc p
SET qr = gen_random_uuid()::text
FROM guia g
WHERE g.id = p.guia_id
  AND g.data_emissao IS NOT NULL
  AND g.status IN ('EMITIDA', 'PARCIALMENTE_REALIZADA', 'FINALIZADA', 'EXPIRADA')
  AND (p.qr IS NULL OR TRIM(p.qr) = '');
