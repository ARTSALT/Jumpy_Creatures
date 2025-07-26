package com.softwaretesting.core.domain.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Testes para a classe User")
public class UserTest {

    /**
     * Testes estruturais focados em garantir a cobertura MC/DC para o metodo 'setAvatarUrl'.
     * Estes testes validam todos os caminhos lógicos para a definição da URL do avatar,
     * incluindo entradas nulas, em branco, caminhos de diretórios e arquivos inexistentes.
     */
    @Nested
    @DisplayName("Testes Estruturais (MC/DC) para o metodo setAvatarUrl")
    class SetAvatarUrlMCDCTests {

        @TempDir
        Path tempDir;

        /**
         * Cobre a condição: if (avatarUrl == null || avatarUrl.isBlank()) -> TRUE
         * com a sub-condição (avatarUrl == null) -> TRUE.
         */
        @Test
        @DisplayName("[MC/DC] deve definir a URL padrão quando a URL fornecida é nula")
        void shouldSetDefaultUrlWhenUrlIsNull() {
            User user = new User("testuser", "password");
            user.setAvatarUrl(null);
            assertThat(user.getAvatarUrl()).isEqualTo(User.DEFAULT_AVATAR_URL);
        }

        /**
         * Cobre a condição: if (avatarUrl == null || avatarUrl.isBlank()) -> TRUE
         * com a sub-condição (avatarUrl.isBlank()) -> TRUE.
         * @param blankUrl Uma URL em branco ou vazia a ser testada.
         */
        @ParameterizedTest
        @ValueSource(strings = {"", " ", "  \t  \n  "})
        @DisplayName("[MC/DC] deve definir a URL padrão quando a URL fornecida está em branco")
        void shouldSetDefaultUrlWhenUrlIsBlank(String blankUrl) {
            User user = new User("testuser", "password");
            user.setAvatarUrl(blankUrl);
            assertThat(user.getAvatarUrl()).isEqualTo(User.DEFAULT_AVATAR_URL);
        }

        /**
         * Cobre a condição: if (file.isDirectory() || !file.exists()) -> TRUE
         * com a sub-condição (file.isDirectory()) -> TRUE.
         */
        @Test
        @DisplayName("[MC/DC] deve definir a URL padrão quando a URL aponta para um diretório")
        void shouldSetDefaultUrlWhenUrlIsADirectory() {
            User user = new User("testuser", "password");
            // Usa o caminho absoluto do diretório temporário fornecido pelo JUnit
            String directoryPath = tempDir.toFile().getAbsolutePath();
            user.setAvatarUrl(directoryPath);
            assertThat(user.getAvatarUrl()).isEqualTo(User.DEFAULT_AVATAR_URL);
        }

        /**
         * Cobre a condição: if (file.isDirectory() || !file.exists()) -> TRUE
         * com a sub-condição (!file.exists()) -> TRUE.
         */
        @Test
        @DisplayName("[MC/DC] deve definir a URL padrão quando o arquivo da URL não existe")
        void shouldSetDefaultUrlWhenFileDoesNotExist() {
            User user = new User("testuser", "password");
            // Cria um caminho para um arquivo que garantidamente não existe
            String nonExistentFilePath = tempDir.resolve("nonexistent_avatar.png").toString();
            user.setAvatarUrl(nonExistentFilePath);
            assertThat(user.getAvatarUrl()).isEqualTo(User.DEFAULT_AVATAR_URL);
        }

        /**
         * Cobre a condição: if (file.isDirectory() || !file.exists()) -> FALSE.
         * Este é o "caminho feliz" onde a URL é válida e o arquivo existe.
         */
        @Test
        @DisplayName("[MC/DC] deve definir a URL fornecida quando o arquivo é válido e existe")
        void shouldSetProvidedUrlWhenFileIsValidAndExists() throws IOException {
            User user = new User("testuser", "password");
            // Cria um arquivo real no diretório temporário
            Path validFile = Files.createFile(tempDir.resolve("valid_avatar.png"));
            String validFilePath = validFile.toFile().getAbsolutePath();

            user.setAvatarUrl(validFilePath);

            assertThat(user.getAvatarUrl()).isEqualTo(validFilePath);
        }
    }


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

        assertThat(user.getId()).isNull();
        assertThat(user.getUsername()).isEqualTo("testuser");
        assertThat(user.getPassword()).isEqualTo("password123");
        assertThat(user.getAvatarUrl()).isEqualTo(User.DEFAULT_AVATAR_URL);
        assertThat(user.getScore()).isEqualTo(0);
    }

    @DisplayName("Testa criação de usuário com ID inválido")
    @Test
    public void testUserCreationWithInvalidId() {
        assertThatThrownBy(() -> new User(-1L, "testuser", "password123", "images/avatar.png", 100))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("ID cannot be null or negative");
        assertThatThrownBy(() -> new User(null, "testuser", "password123", "images/avatar.png", 100))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("ID cannot be null or negative");
    }

    @DisplayName("Testa criação de usuário com nome de usuário inválido")
    @Test
    public void testUserCreationWithInvalidUsername() {
        assertThatThrownBy(() -> new User(null, "password123"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Username cannot be null or blank");
        assertThatThrownBy(() -> new User("", "password123"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Username cannot be null or blank");
    }

    @DisplayName("Testa criação de usuário com senha inválida")
    @Test
    public void testUserCreationWithInvalidPassword() {
        assertThatThrownBy(() -> new User("testuser", null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Password cannot be null or blank");
        assertThatThrownBy(() -> new User("testuser", ""))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Password cannot be null or blank");
    }

    @DisplayName("Testa atualização de nome de usuário com valor válido")
    @Test
    public void testUpdateUsernameWithValidValue() {
        User user = new User("testuser", "password123");
        user.setUsername("newusername");

        assertThat(user.getUsername()).isEqualTo("newusername");
    }

    @Nested
    @DisplayName("Outros Testes de Domínio e Fronteira")
    class DomainAndBoundaryTests {

        @Test
        @DisplayName("Construtor deve lançar exceção para nome de usuário nulo")
        void constructorShouldThrowExceptionForNullUsername() {
            assertThatThrownBy(() -> new User(null, "password"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Username cannot be null or blank");
        }

        @Test
        @DisplayName("setScore deve lançar exceção para pontuação negativa")
        void setScoreShouldThrowExceptionForNegativeScore() {
            User user = new User("testuser", "password");
            assertThatThrownBy(() -> user.setScore(-1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Score cannot be negative");
        }
    }

    /**
     * Testes para os metodos de contrato do objeto (equals, hashCode, toString)
     * e para a funcionalidade de administrador.
     */
    @Nested
    @DisplayName("Testes de Contrato de Objeto e Admin")
    class ObjectContractAndAdminTests {

        /**
         * Testa a funcionalidade de getter e setter do status de administrador.
         * Garante que a propriedade 'admin' pode ser definida e recuperada corretamente.
         */
        @Test
        @DisplayName("setAdmin e isAdmin devem definir e retornar o status de admin")
        void shouldSetAndGetAdminStatus() {
            User user = new User("testuser", "password");

            // Por padrão, um usuário não deve ser admin
            assertThat(user.isAdmin()).isFalse();

            // Testa a definição para true
            user.setAdmin(true);
            assertThat(user.isAdmin()).isTrue();

            // Testa a definição de volta para false
            user.setAdmin(false);
            assertThat(user.isAdmin()).isFalse();
        }

        /**
         * Testa o formato e o conteúdo do metodo toString.
         * Garante que a representação em string do usuário contenha todas as informações
         * relevantes, excluindo dados sensíveis como o ID.
         */
        @Test
        @DisplayName("toString deve retornar a representação em string formatada")
        void toStringShouldReturnFormattedString() {
            User user = new User("testuser", "password123");
            user.setScore(150);

            String result = user.toString();

            assertThat(result).contains("username='testuser'");
            assertThat(result).contains("password='password123'");
            assertThat(result).contains("avatarUrl='" + User.DEFAULT_AVATAR_URL + "'");
            assertThat(result).contains("score=150");
        }

        /**
         * Valida todos os caminhos lógicos do metodo equals para garantir 100% de cobertura MC/DC.
         */
        @Nested
        @DisplayName("metodo equals()")
        class EqualsMCDCTests {

            private User user1;

            @BeforeEach
            void setUp() {
                // Cria um usuário base com um ID para os testes
                user1 = new User(1L, "user1", "pass1", User.DEFAULT_AVATAR_URL, 100);
            }

            @Test
            @DisplayName("[MC/DC] deve retornar true para a mesma instância (this == o)")
            void shouldReturnTrueForSameInstance() {
                assertThat(user1.equals(user1)).isTrue();
            }

            @Test
            @DisplayName("[MC/DC] deve retornar false para nulo ou uma classe diferente (!(o instanceof User))")
            void shouldReturnFalseForNullOrDifferentClass() {
                assertThat(user1.equals(null)).isFalse();
                assertThat(user1.equals(new Object())).isFalse();
            }

            @Test
            @DisplayName("[MC/DC] deve retornar true quando todos os campos são iguais")
            void shouldReturnTrueWhenAllFieldsAreEqual() {
                User user2 = new User(2L, "user1", "pass1", User.DEFAULT_AVATAR_URL, 100);
                assertThat(user1.equals(user2)).isTrue();
            }

            @Test
            @DisplayName("[MC/DC] deve retornar false se o nome de usuário for diferente")
            void shouldReturnFalseIfUsernameIsDifferent() {
                User user2 = new User(2L, "userX", "pass1", User.DEFAULT_AVATAR_URL, 100);
                assertThat(user1.equals(user2)).isFalse();
            }

            @Test
            @DisplayName("[MC/DC] deve retornar false se a senha for diferente")
            void shouldReturnFalseIfPasswordIsDifferent() {
                User user2 = new User(2L, "user1", "passX", User.DEFAULT_AVATAR_URL, 100);
                assertThat(user1.equals(user2)).isFalse();
            }
        }

        /**
         * Valida o contrato entre equals e hashCode.
         */
        @Nested
        @DisplayName("metodo hashCode()")
        class HashCodeTests {

            /**
             * Contrato: Se dois objetos são 'equals', eles devem ter o mesmo hashCode.
             */
            @Test
            @DisplayName("deve retornar o mesmo hashCode para objetos iguais")
            void shouldReturnSameHashCodeForEqualObjects() {
                User user1 = new User(1L, "user1", "pass1", User.DEFAULT_AVATAR_URL, 100);
                User user2 = new User(2L, "user1", "pass1", User.DEFAULT_AVATAR_URL, 100);

                assertThat(user1.equals(user2)).isTrue();
                assertThat(user1.hashCode()).isEqualTo(user2.hashCode());
            }

            /**
             * Cobre a branch 'id != null -> false' no cálculo do hashCode.
             */
            @Test
            @DisplayName("[MC/DC] deve calcular o hashCode corretamente quando o ID é nulo")
            void shouldCalculateHashCodeWhenIdIsNull() {
                // O construtor que não define um ID é usado para este teste
                User user1 = new User("user1", "pass1");
                User user2 = new User("user1", "pass1");

                // O metodo hashCode não deve lançar NullPointerException
                assertThatCode(() -> user1.hashCode()).doesNotThrowAnyException();
                // Verifica se o contrato ainda é mantido
                assertThat(user1.hashCode()).isEqualTo(user2.hashCode());
            }
        }
    }
}
