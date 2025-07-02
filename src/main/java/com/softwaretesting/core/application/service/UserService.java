package com.softwaretesting.core.application.service;

import com.softwaretesting.core.domain.model.User;
import com.softwaretesting.core.domain.port.driven.UserRepository;
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

    public boolean register(User user) throws SQLException {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null.");
        }
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new IllegalArgumentException("Username already exists.");
        }

        // hasheia a senha antes de salvar
        String hashedPassword = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt(10));
        user.setPassword(hashedPassword);
        return userRepository.save(user);
    }

    public void update(User user) throws SQLException {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null.");
        }
        if (!userRepository.existsByUsername(user.getUsername())) {
            throw new IllegalArgumentException("User does not exist.");
        }

        userRepository.update(user);
    }

    public Optional<User> login(User user) throws SQLException {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null.");
        }

        Optional<User> userOptional = userRepository.findByUsername(user.getUsername());
        if (userOptional.isPresent()) {
            User userOpt = userOptional.get();
            // compara a senha fornecida com o hash armazenado
            if (BCrypt.checkpw(user.getPassword(), userOpt.getPassword())) {
                return userOptional;
            }
        }
        return Optional.empty();
    }

    public boolean userExists(User user) throws SQLException {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null.");
        }

        return userRepository.existsByUsername(user.getUsername());
    }

    public boolean deleteUser(User user) throws SQLException {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null.");
        }
        if (!userRepository.existsByUsername(user.getUsername())) {
            throw new IllegalArgumentException("User does not exists.");
        }

        return userRepository.deleteByUsername(user.getUsername());
    }

    public List<User> getAllUsers() throws SQLException {
        return userRepository.findAll();
    }
}
