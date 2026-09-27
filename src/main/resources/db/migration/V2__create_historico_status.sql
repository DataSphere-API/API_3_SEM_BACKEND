CREATE TABLE historico_status (
                                  id BIGSERIAL PRIMARY KEY,
                                  entidade_tipo VARCHAR(30) NOT NULL,
                                  entidade_id BIGINT NOT NULL,
                                  status_anterior VARCHAR(50),
                                  status_novo VARCHAR(50) NOT NULL,
                                  data TIMESTAMP NOT NULL,
                                  origem VARCHAR(50) NOT NULL
);

CREATE INDEX idx_hist_entidade ON historico_status (entidade_tipo, entidade_id);
CREATE INDEX idx_hist_data ON historico_status (data);