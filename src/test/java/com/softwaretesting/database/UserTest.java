package com.softwaretesting.database;

import com.softwaretesting.core.domain.model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Testes para a classe User")
public class UserTest {

    @DisplayName("Testa criação de usuário com ID, nome de usuário, senha, URL do avatar e pontuação válidos")
    @Test
    public void testUserCreationWithValidParameters() {
        User user = new User(1L, "testuser", "password123", "images/avatar.png", 100);

        assertThat(user.getId()).isEqualTo(1L);
        assertThat(user.getUsername()).isEqualTo("testuser");
        assertThat(user.getPassword()).isEqualTo("password123");
        assertThat(user.getAvatarUrl()).isEqualTo("images/default_avatar.png");
        assertThat(user.getScore()).isEqualTo(100);
    }

    @DisplayName("Testa criação de usuário com nome de usuário e senha válidos")
    @Test
    public void testUserCreationWithUsernameAndPassword() {
        User user = new User("testuser", "password123");

        assertThat(user.getId()).isEqualTo(null);
        assertThat(user.getUsername()).isEqualTo("testuser");
        assertThat(user.getPassword()).isEqualTo("password123");
        assertThat(user.getAvatarUrl()).isEqualTo(User.DEFAULT_AVATAR_URL);
        assertThat(user.getScore()).isEqualTo(0);
    }
}
