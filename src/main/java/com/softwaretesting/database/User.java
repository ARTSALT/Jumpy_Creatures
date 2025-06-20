package com.softwaretesting.database;

/**
 * Classe que representa um usuário no sistema.
 * Contém informações como ID, nome de usuário, senha, URL do avatar e pontuação.
 */
public class User {
    private final Long id;
    private String username;
    private String password;
    private String avatarUrl;
    private int score;

    public User(Long id, String username, String password, String avatarUrl, int score) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.avatarUrl = avatarUrl;
        this.score = score;
    }

    public User(String username, String password) {
        this.id = 0L;
        this.username = username;
        this.password = password;
        this.avatarUrl = "images/default_avatar.png";
        this.score = 0;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    @Override
    public String toString() {
        return "User{" +
                "username='" + username + '\'' +
                ", passwordHash='" + password + '\'' +
                ", avatarUrl='" + avatarUrl + '\'' +
                ", score=" + score +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User user)) return false;
        return score == user.score &&
                username.equals(user.username) &&
                password.equals(user.password) &&
                avatarUrl.equals(user.avatarUrl);
    }

    @Override
    public int hashCode() {
        int result = username.hashCode();
        result = 31 * result + password.hashCode();
        result = 31 * result + avatarUrl.hashCode();
        result = 31 * result + score;
        return result;
    }
}
