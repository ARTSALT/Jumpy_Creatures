package com.softwaretesting.core.service;

import com.softwaretesting.core.domain.model.Simulation;
import com.softwaretesting.core.domain.model.User;
import com.softwaretesting.core.domain.port.driven.SimulationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@DisplayName("Testes para SimulationService")
class SimulationServiceTest {

    private SimulationRepository simulationRepository;
    private SimulationService simulationService;
    private User user;

    @BeforeEach
    void setUp() {
        simulationRepository = mock(SimulationRepository.class);
        simulationService = new SimulationService(simulationRepository);
        user = new User(1L, "testUserUnit");
    }

    @Nested
    @DisplayName("Testes para o método register()")
    class RegisterTests {

        @Test
        @DisplayName("C1=T: Deve lançar exceção se a simulação for nula")
        void shouldThrowExceptionWhenSimulationIsNull() {
            // Condição: simulation == null (true)
            assertThatThrownBy(() -> simulationService.register(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Simulation cannot be null.");

            verifyNoInteractions(simulationRepository);
        }

        @Test
        @DisplayName("C1=F, C2=T: Deve lançar exceção se o usuário da simulação for nulo")
        void shouldThrowExceptionWhenUserInSimulationIsNull() {
            // Condições: simulation == null (false), simulation.getUser() == null (true)
            Simulation simulation = new Simulation(2, 1, 1);

            assertThatThrownBy(() -> simulationService.register(simulation))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Simulation must be associated with a user.");

            verifyNoInteractions(simulationRepository);
        }

        @Test
        @DisplayName("C1=F, C2=F: Deve registrar com sucesso se os dados forem válidos")
        void shouldRegisterSuccessfullyWithValidData() throws SQLException {
            // Condições: simulation == null (false), simulation.getUser() == null (false)
            Simulation simulation = new Simulation(2, 1, 1);
            simulation.setUser(user);

            simulationService.register(simulation);

            // Verifica que o metodo save do repositório foi chamado uma vez com o objeto correto
            verify(simulationRepository, times(1)).save(simulation);
        }

        @Test
        @DisplayName("Deve propagar SQLException ao tentar registrar")
        void shouldPropagateSqlExceptionOnRegister() throws SQLException {
            Simulation simulation = new Simulation(2, 1, 1);
            simulation.setUser(user);
            doThrow(new SQLException("Database error")).when(simulationRepository).save(simulation);

            assertThatThrownBy(() -> simulationService.register(simulation))
                .isInstanceOf(SQLException.class)
                .hasMessage("Database error");
        }
    }

    @Nested
    @DisplayName("Testes para o método delete()")
    class DeleteTests {

        @Test
        @DisplayName("C1=T: Deve lançar exceção se o ID for nulo")
        void shouldThrowExceptionWhenIdIsNull() {
            // Condições: id == null (true)
            assertThatThrownBy(() -> simulationService.delete(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("ID cannot be null or negative.");

            verifyNoInteractions(simulationRepository);
        }

        @Test
        @DisplayName("C1=F, C2=T: Deve lançar exceção se o ID for negativo")
        void shouldThrowExceptionWhenIdIsNegative() {
            // Condições: id == null (false), id < 0 (true)
            assertThatThrownBy(() -> simulationService.delete(-1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("ID cannot be null or negative.");

            verifyNoInteractions(simulationRepository);
        }

        @Test
        @DisplayName("C1=F, C2=F: Deve deletar com sucesso se o ID for válido")
        void shouldDeleteSuccessfullyWhenIdIsValid() throws SQLException {
            // Condições: id == null (false), id < 0 (false)
            Long validId = 1L;
            simulationService.delete(validId);

            verify(simulationRepository, times(1)).delete(validId);
        }

        @Test
        @DisplayName("Deve propagar SQLException ao tentar deletar")
        void shouldPropagateSqlExceptionOnDelete() throws SQLException {
            Long validId = 1L;
            doThrow(new SQLException("Database error")).when(simulationRepository).delete(validId);

            assertThatThrownBy(() -> simulationService.delete(validId))
                .isInstanceOf(SQLException.class)
                .hasMessage("Database error");
        }
    }

    @Nested
    @DisplayName("Testes para o método getSimulations()")
    class GetSimulationsTests {

        @Test
        @DisplayName("C1=T: Deve lançar exceção se o usuário for nulo")
        void shouldThrowExceptionWhenUserIsNull() {
            // Condição: user == null (true)
            assertThatThrownBy(() -> simulationService.getSimulations(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("User cannot be null.");

            verifyNoInteractions(simulationRepository);
        }

        @Test
        @DisplayName("C1=F: Deve retornar a lista de simulações para um usuário válido")
        void shouldReturnSimulationsForValidUser() throws SQLException {
            // Condição: user == null (false)
            List<Simulation> expectedSimulations = List.of(
                new Simulation(2, 1, 1),
                new Simulation(2, 1, 1)
            );
            when(simulationRepository.findAllByUser(user)).thenReturn(expectedSimulations);

            List<Simulation> actualSimulations = simulationService.getSimulations(user);

            assertThat(actualSimulations).isEqualTo(expectedSimulations);
            verify(simulationRepository, times(1)).findAllByUser(user);
        }

        @Test
        @DisplayName("Deve propagar SQLException ao buscar simulações")
        void shouldPropagateSqlExceptionOnGetSimulations() throws SQLException {
            when(simulationRepository.findAllByUser(user)).thenThrow(new SQLException("Database error"));

            assertThatThrownBy(() -> simulationService.getSimulations(user))
                .isInstanceOf(SQLException.class)
                .hasMessage("Database error");
        }
    }

    @Nested
    @DisplayName("Testes para o método getAllSimulations()")
    class GetAllSimulationsTests {

        @Test
        @DisplayName("Deve retornar todas as simulações do repositório")
        void shouldReturnAllSimulations() throws SQLException {
            List<Simulation> expectedSimulations = List.of(
                new Simulation(2, 1, 1),
                new Simulation(2, 1, 1)
            );
            when(simulationRepository.findAll()).thenReturn(expectedSimulations);

            List<Simulation> actualSimulations = simulationService.getAllSimulations();

            assertThat(actualSimulations).isEqualTo(expectedSimulations);
            verify(simulationRepository, times(1)).findAll();
        }

        @Test
        @DisplayName("Deve propagar SQLException ao buscar todas as simulações")
        void shouldPropagateSqlExceptionOnGetAllSimulations() throws SQLException {
            when(simulationRepository.findAll()).thenThrow(new SQLException("Database error"));

            assertThatThrownBy(() -> simulationService.getAllSimulations())
                .isInstanceOf(SQLException.class)
                .hasMessage("Database error");
        }
    }

    @Nested
    @DisplayName("Testes para o método getSuccessfulSimulations()")
    class GetSuccessfulSimulationsTests {

        @Test
        @DisplayName("Deve filtrar e retornar apenas simulações bem-sucedidas")
        void shouldReturnOnlySuccessfulSimulations() throws SQLException {
            Simulation successfulSim = new Simulation(2, 1, 1);
            successfulSim.setSuccessful(true);
            Simulation unsuccessfulSim = new Simulation(2, 1, 1);
            unsuccessfulSim.setSuccessful(false);

            List<Simulation> allSimulations = List.of(successfulSim, unsuccessfulSim);
            when(simulationRepository.findAllByUser(user)).thenReturn(allSimulations);

            List<Simulation> result = simulationService.getSuccessfulSimulations(user);

            assertThat(result).hasSize(1);
            assertThat(result.getFirst()).isEqualTo(successfulSim);
        }

        @Test
        @DisplayName("Deve retornar uma lista vazia se não houver simulações bem-sucedidas")
        void shouldReturnEmptyListWhenNoSuccessfulSimulations() throws SQLException {
            Simulation unsuccessfulSim1 = new Simulation(2, 1, 1);
            unsuccessfulSim1.setSuccessful(false);
            Simulation unsuccessfulSim2 = new Simulation(2, 1, 1);
            unsuccessfulSim2.setSuccessful(false);

            List<Simulation> allSimulations = List.of(unsuccessfulSim1, unsuccessfulSim2);
            when(simulationRepository.findAllByUser(user)).thenReturn(allSimulations);

            List<Simulation> result = simulationService.getSuccessfulSimulations(user);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Deve retornar uma lista vazia se a lista inicial for vazia")
        void shouldReturnEmptyListWhenInitialListIsEmpty() throws SQLException {
            when(simulationRepository.findAllByUser(user)).thenReturn(Collections.emptyList());

            List<Simulation> result = simulationService.getSuccessfulSimulations(user);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Deve propagar SQLException vindo do método getSimulations")
        void shouldPropagateSqlException() throws SQLException {
            when(simulationRepository.findAllByUser(user)).thenThrow(new SQLException("Database error"));

            assertThatThrownBy(() -> simulationService.getSuccessfulSimulations(user))
                .isInstanceOf(SQLException.class)
                .hasMessage("Database error");
        }
    }

    @Nested
    @DisplayName("Testes para o método getSimulationUser()")
    class GetSimulationUserTests {

        @Test
        @DisplayName("C1=T: Deve lançar exceção se o ID for nulo")
        void shouldThrowExceptionWhenIdIsNull() {
            // Condições: id == null (true)
            assertThatThrownBy(() -> simulationService.getSimulationUser(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("ID cannot be null or negative.");
        }

        @Test
        @DisplayName("C1=F, C2=T: Deve lançar exceção se o ID for negativo")
        void shouldThrowExceptionWhenIdIsNegative() {
            // Condições: id == null (false), id < 0 (true)
            assertThatThrownBy(() -> simulationService.getSimulationUser(-1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("ID cannot be null or negative.");
        }

        @Test
        @DisplayName("C1=F, C2=F: Deve retornar o usuário para um ID de simulação válido")
        void shouldReturnUserForValidSimulationId() throws SQLException {
            // Condições: id == null (false), id < 0 (false)
            Long validId = 1L;
            when(simulationRepository.findUserBySimulationId(validId)).thenReturn(user);

            User foundUser = simulationService.getSimulationUser(validId);

            assertThat(foundUser).isEqualTo(user);
            verify(simulationRepository, times(1)).findUserBySimulationId(validId);
        }

        @Test
        @DisplayName("Deve propagar SQLException ao buscar o usuário da simulação")
        void shouldPropagateSqlExceptionOnFindUser() throws SQLException {
            Long validId = 1L;
            when(simulationRepository.findUserBySimulationId(validId)).thenThrow(new SQLException("Database error"));

            assertThatThrownBy(() -> simulationService.getSimulationUser(validId))
                .isInstanceOf(SQLException.class)
                .hasMessage("Database error");
        }
    }

    @Nested
    @DisplayName("Testes para o método getUserSimulationCount()")
    class GetUserSimulationCountTests {

        @Test
        @DisplayName("C1=T: Deve lançar exceção se o ID do usuário for nulo")
        void shouldThrowExceptionWhenUserIdIsNull() {
            // Condições: id == null (true)
            assertThatThrownBy(() -> simulationService.getUserSimulationCount(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("ID cannot be null or negative.");
        }

        @Test
        @DisplayName("C1=F, C2=T: Deve lançar exceção se o ID do usuário for negativo")
        void shouldThrowExceptionWhenUserIdIsNegative() {
            // Condições: id == null (false), id < 0 (true)
            assertThatThrownBy(() -> simulationService.getUserSimulationCount(-1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("ID cannot be null or negative.");
        }

        @Test
        @DisplayName("C1=F, C2=F: Deve retornar a contagem de simulações para um ID de usuário válido")
        void shouldReturnCountForValidUserId() throws SQLException {
            // Condições: id == null (false), id < 0 (false)
            Long validUserId = 1L;
            when(simulationRepository.countByUserId(validUserId)).thenReturn(5);

            int count = simulationService.getUserSimulationCount(validUserId);

            assertThat(count).isEqualTo(5);
            verify(simulationRepository, times(1)).countByUserId(validUserId);
        }

        @Test
        @DisplayName("Deve lançar RuntimeException se ocorrer um SQLException")
        void shouldThrowRuntimeExceptionOnSqlException() throws SQLException {
            Long validUserId = 1L;
            when(simulationRepository.countByUserId(validUserId)).thenThrow(new SQLException("Database error"));

            assertThatThrownBy(() -> simulationService.getUserSimulationCount(validUserId))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Error counting simulations for user ID: " + validUserId)
                .hasCauseInstanceOf(SQLException.class);
        }
    }
}
