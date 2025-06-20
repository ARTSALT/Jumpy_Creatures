package com.softwaretesting.database;

import org.mindrot.jbcrypt.BCrypt;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Serviço para gerenciar usuários, incluindo registro, login e verificação de existência.
 * Utiliza UserRepository para interações com o banco de dados.
 */
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<User> getUserByUsername(String username) throws SQLException {
        if (username == null || username.isEmpty()) {
            throw new IllegalArgumentException("Nome de usuário não pode ser nulo ou vazio.");
        }

        return userRepository.findByUsername(username);
    }

    public boolean registerUser(String username, String plainTextPassword) throws SQLException {
        if (username == null || plainTextPassword == null || username.isEmpty() || plainTextPassword.isEmpty()) {
            throw new IllegalArgumentException("Nome de usuário e senha não podem ser nulos ou vazios.");
        }
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Nome de usuário já existe.");
        }
        if (plainTextPassword.length() < 8) {
            throw new IllegalArgumentException("A senha deve ter pelo menos 8 caracteres.");
        }

        // hasheia a senha antes de salvar
        String hashedPassword = BCrypt.hashpw(plainTextPassword, BCrypt.gensalt(10));
        User newUser = new User(username, hashedPassword);
        return userRepository.save(newUser);
    }

    public Optional<User> login(String username, String plainTextPassword) throws SQLException {
        if (username == null || plainTextPassword == null || username.isEmpty() || plainTextPassword.isEmpty()) {
            throw new IllegalArgumentException("Nome de usuário e senha não podem ser nulos ou vazios.");
        }

        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            // compara a senha fornecida com o hash armazenado
            if (BCrypt.checkpw(plainTextPassword, user.getPassword())) {
                return userOptional;
            }
        }
        return Optional.empty();
    }

    public boolean userExists(String username) throws SQLException {
        if (username == null || username.isEmpty()) {
            throw new IllegalArgumentException("Nome de usuário não pode ser nulo ou vazio.");
        }
        return userRepository.existsByUsername(username);
    }

    public boolean deleteUser(String username) throws SQLException {
        if (username == null || username.isEmpty()) {
            throw new IllegalArgumentException("Nome de usuário não pode ser nulo ou vazio.");
        }
        if (!userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Usuário não encontrado.");
        }

        return userRepository.deleteByUsername(username);
    }

    public List<User> getAllUsers() throws SQLException {
        return userRepository.findAll();
    }
}
