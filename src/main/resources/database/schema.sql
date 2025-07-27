// cria tabela de usuários caso não exista
CREATE TABLE IF NOT EXISTS users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    avatar_url VARCHAR(255) DEFAULT 'images/default_avatar.png',
    score INT DEFAULT 0
);

// insere usuário admin padrão se não existir
INSERT INTO users (username, password)
SELECT 'admin', '$2a$10$9OGJDg8B8zKVKaAsWUhJZu0.aBi1rxcfeKyJC/38gxT8rI44jozoq'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'admin');

// cria a tabela de simulações
CREATE TABLE IF NOT EXISTS simulations (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    num_creatures INT NOT NULL,
    iterations INT NOT NULL,
    is_successful BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);