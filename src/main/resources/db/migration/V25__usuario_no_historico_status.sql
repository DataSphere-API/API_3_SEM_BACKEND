-- Cópia histórica: eventos antigos e automáticos permanecem sem usuário.
-- Não há FK para preservar a identificação mesmo se o usuário for removido.
ALTER TABLE historico_status
    ADD COLUMN usuario_id INTEGER,
    ADD COLUMN usuario_nome VARCHAR(255);
