package com.softwaretesting.database;

import java.io.File;

/**
 * Classe que representa um usuário no sistema.
 * Contém informações como ID, nome de usuário, senha, URL do avatar e pontuação.
 */
public class User {
    public static String DEFAULT_AVATAR_URL = "images/default_avatar.png";

    private final Long id;
    private String username;
    private String password;
    private String avatarUrl;
    private int score;

    public User(Long id, String username, String password, String avatarUrl, int score) {
        if (id == null || id < 0) {
            throw new IllegalArgumentException("ID cannot be null or negative");
        }
        this.id = id;

        setUsername(username);
        setPassword(password);
        setAvatarUrl(avatarUrl);
        setScore(score);
    }

    public User(String username, String password) {
        this.id = 0L;
        setUsername(username);
        setPassword(password);
        setAvatarUrl(DEFAULT_AVATAR_URL);
        setScore(0);
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username cannot be null or blank");
        }

        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password cannot be null or blank");
        }

        this.password = password;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    /**
     * Define a URL do avatar do usuário.
     * Se a URL for nula, em branco ou caso o arquivo não exista, define uma URL padrão.
     *
     * @param avatarUrl a URL do avatar
     */
    public void setAvatarUrl(String avatarUrl) {
        if (avatarUrl == null || avatarUrl.isBlank()) {
            this.avatarUrl = DEFAULT_AVATAR_URL;
        } else {
            File file = new File(avatarUrl);
            if (file.isDirectory() || !file.exists()) {
                this.avatarUrl = DEFAULT_AVATAR_URL;
            } else {
                this.avatarUrl = avatarUrl;
            }
        }
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        if (score < 0) {
            throw new IllegalArgumentException("Score cannot be negative");
        }

        this.score = score;
    }

    @Override
    public String toString() {
        return "User{" +
            "username='" + username + '\'' +
            ", password='" + password + '\'' +
            ", avatarUrl='" + avatarUrl + '\'' +
            ", score=" + score +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User user)) return false;
        return score == user.score
            && username.equals(user.username)
            && password.equals(user.password)
            && avatarUrl.equals(user.avatarUrl);
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + username.hashCode();
        result = 31 * result + password.hashCode();
        result = 31 * result + avatarUrl.hashCode();
        result = 31 * result + score;
        return result;
    }
}
