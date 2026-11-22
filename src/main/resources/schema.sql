CREATE TABLE IF NOT EXISTS analises (
    id VARCHAR(36) PRIMARY KEY,
    aluno_id VARCHAR(20) NOT NULL,
    hash_documento VARCHAR(64) NOT NULL,
    status VARCHAR(30) NOT NULL,
    criado_em VARCHAR(40) NOT NULL,
    resposta CLOB NOT NULL
);
