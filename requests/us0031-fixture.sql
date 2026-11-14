-- Opcional: executar em um banco de desenvolvimento com o seed dev carregado.
-- Cria somente registros novos para testar a geração de UUID, sem alterar os QR do seed.
-- Copie o id retornado para atendimentoId em us0031.http ou para a URL no Postman.
WITH nova_guia AS (
    INSERT INTO guia (benf_id, med_id, emissor_id, chefe_id, status, data_emissao)
    VALUES ('123456789-00', 1, 1, 1, 'EMITIDA', CURRENT_DATE)
    RETURNING id
)
INSERT INTO guia_proc (cont_id, guia_id, status, qtd_autorizada, qtd_realizada, data_validade)
SELECT 1, id, 'PENDENTE', 1, 0, CURRENT_DATE + 60
FROM nova_guia
RETURNING id AS atendimento_id;
