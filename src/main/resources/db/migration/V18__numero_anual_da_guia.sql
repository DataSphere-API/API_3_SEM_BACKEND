-- US003-6: numeração anual para guias existentes e novas, sem depender do ID global.
ALTER TABLE guia ADD COLUMN numero VARCHAR(40);

CREATE TABLE guia_numeracao_anual (
    ano INTEGER PRIMARY KEY,
    ultimo_numero BIGINT NOT NULL CHECK (ultimo_numero > 0)
);

WITH numeradas AS (
    SELECT id, EXTRACT(YEAR FROM data_emissao)::INTEGER AS ano,
           ROW_NUMBER() OVER (PARTITION BY EXTRACT(YEAR FROM data_emissao) ORDER BY data_emissao, id) AS sequencia
    FROM guia
)
UPDATE guia g
SET numero = n.ano::TEXT || '-' || LPAD(n.sequencia::TEXT, GREATEST(6, LENGTH(n.sequencia::TEXT)), '0')
FROM numeradas n WHERE n.id = g.id;

INSERT INTO guia_numeracao_anual (ano, ultimo_numero)
SELECT EXTRACT(YEAR FROM data_emissao)::INTEGER, COUNT(*) FROM guia GROUP BY 1;

ALTER TABLE guia ALTER COLUMN numero SET NOT NULL;
ALTER TABLE guia ADD CONSTRAINT uq_guia_numero UNIQUE (numero);

CREATE FUNCTION atribuir_numero_anual_guia() RETURNS TRIGGER AS $$
DECLARE
    ano_emissao INTEGER;
    sequencia BIGINT;
BEGIN
    ano_emissao := EXTRACT(YEAR FROM NEW.data_emissao)::INTEGER;
    INSERT INTO guia_numeracao_anual (ano, ultimo_numero) VALUES (ano_emissao, 1)
    ON CONFLICT (ano) DO UPDATE
        SET ultimo_numero = guia_numeracao_anual.ultimo_numero + 1
    RETURNING ultimo_numero INTO sequencia;

    NEW.numero := ano_emissao::TEXT || '-' || LPAD(sequencia::TEXT, GREATEST(6, LENGTH(sequencia::TEXT)), '0');
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_numero_anual_guia BEFORE INSERT ON guia
FOR EACH ROW EXECUTE FUNCTION atribuir_numero_anual_guia();
