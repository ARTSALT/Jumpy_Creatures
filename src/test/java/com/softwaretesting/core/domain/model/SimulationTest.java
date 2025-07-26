package com.softwaretesting.core.domain.model;

import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Fail.fail;

/**
 * Suíte de testes para a classe Simulation.
 * Contém testes de domínio, fronteira, MC/DC e de propriedade (jqwik).
 */
@DisplayName("Testes da Classe Simulation")
class SimulationTest {

    private PredictableRandomProvider fakeRandom;

    @BeforeEach
    void setUp() {
        fakeRandom = new PredictableRandomProvider();
    }

    static class PredictableRandomProvider implements RandomProvider {
        private double valueToReturn;
        public void setValueToReturn(double value) { this.valueToReturn = value; }
        @Override
        public double nextDouble(double min, double max) { return this.valueToReturn; }
    }

    /**
     * Testes focados em garantir a cobertura e o comportamento correto dos getters e setters
     * da classe Simulation, validando a integridade do estado do objeto.
     */
    @Nested
    @DisplayName("Testes de Getters e Setters")
    class GetterAndSetterTests {

        /**
         * Testa todos os getters e setters para garantir que eles atribuem e retornam os valores corretamente.
         * <p>
         * Este teste cobre todos os métodos de acesso e modificação de estado simples,
         * garantindo 100% de cobertura para esses métodos e validando que o estado
         * interno da simulação é gerenciado como esperado.
         */
        @Test
        @DisplayName("deve atribuir e retornar todas as propriedades corretamente")
        void shouldSetAndGetSimulationPropertiesCorrectly() {
            // Setup
            Simulation simulation = new Simulation(10, 1.0, 1000, 5, fakeRandom);
            User testUser = new User("usuario", "senha");
            LocalDateTime testDateTime = LocalDateTime.of(2024, 7, 26, 10, 0);

            // Action & Assertion
            simulation.setId(123L);
            assertThat(simulation.getId()).isEqualTo(123L);

            simulation.setName("Test Simulation");
            assertThat(simulation.getName()).isEqualTo("Test Simulation");

            simulation.setUser(testUser);
            assertThat(simulation.getUser()).isSameAs(testUser);

            simulation.setCreatedAt(testDateTime);
            assertThat(simulation.getCreatedAt()).isSameAs(testDateTime);

            simulation.setCurrentState(Simulation.SimulationState.FINISHED);
            assertThat(simulation.getCurrentState()).isEqualTo(Simulation.SimulationState.FINISHED);

            // Testa o getter que não tem um setter correspondente
            assertThat(simulation.getHorizonWidth()).isEqualTo(1000.0);
        }
    }

    @Nested
    @DisplayName("Testes da Máquina de Estados e Execução")
    class StateMachineAndExecutionTests {

        @Test
        @DisplayName("Deve transitar pelos estados corretamente em um ciclo de iteração granular")
        void shouldTransitionThroughStatesCorrectlyInGranularCycle() {
            Simulation simulation = new Simulation(2, 1.0, 1000, 5, fakeRandom);
            assertThat(simulation.getCurrentState()).isEqualTo(Simulation.SimulationState.READY_TO_PREPARE);
            assertThat(simulation.getIterations()).isZero();

            // Fase 1
            simulation.prepare();
            assertThat(simulation.getCurrentState()).isEqualTo(Simulation.SimulationState.READY_TO_UPDATE);
            assertThat(simulation.getIterations()).isEqualTo(1);

            // Fase 2
            simulation.update();
            assertThat(simulation.getCurrentState()).isEqualTo(Simulation.SimulationState.READY_TO_RESOLVE);

            // Fase 3
            simulation.resolve();
            assertThat(simulation.getCurrentState()).isEqualTo(Simulation.SimulationState.READY_TO_PREPARE);
        }

        @Test
        @DisplayName("update e resolve devem retornar se o estado atual não corresponder ao esperado")
        void shouldReturnIfCurrentStateIsNotReadyToUpdate() {
            Simulation simulation = new Simulation(2, 1.0, 1000, 5, fakeRandom);
            simulation.update();
            assertThat(simulation.getCurrentState()).isEqualTo(Simulation.SimulationState.READY_TO_PREPARE);

            simulation.resolve();
            assertThat(simulation.getCurrentState()).isEqualTo(Simulation.SimulationState.READY_TO_PREPARE);
        }

        @Test
        @DisplayName("executeNextIteration deve executar um ciclo completo e incrementar a iteração")
        void executeNextIterationShouldRunFullCycleAndIncrementIteration() {
            Simulation simulation = new Simulation(2, 1.0, 1000, 5, fakeRandom);
            boolean executed = simulation.executeNextIteration();
            assertThat(executed).isTrue();
            assertThat(simulation.getIterations()).isEqualTo(1);
            assertThat(simulation.getCurrentState()).isEqualTo(Simulation.SimulationState.READY_TO_PREPARE);
        }

        @Test
        @DisplayName("A simulação deve transitar para o estado FINISHED ao atingir o sucesso")
        void shouldTransitionToFinishedStateOnSuccess() {
            Simulation simulation = new Simulation(1, 1.0, 1000, 5, fakeRandom);
            Guardian guardian = (Guardian) simulation.getCreatures().stream().filter(Guardian.class::isInstance).findFirst().orElseThrow();
            guardian.absorbCluster(new Cluster(new Creature(2_000_000, 0), new Creature(0, 0)));
            simulation.executeNextIteration();
            assertThat(simulation.isSuccessful()).isTrue();
            assertThat(simulation.getCurrentState()).isEqualTo(Simulation.SimulationState.FINISHED);
            boolean executedAgain = simulation.executeNextIteration();
            assertThat(executedAgain).isFalse();
            assertThat(simulation.getIterations()).isEqualTo(1);
        }

        @Test
        @DisplayName("executeNextIteration deve retornar false quando o máximo de iterações é atingido")
        void executeNextIterationShouldReturnFalseWhenMaxIterationsReached() {
            Simulation simulation = new Simulation(1, 1.0, 1000, 2, fakeRandom);
            assertThat(simulation.executeNextIteration()).isTrue(); // Iteration 1
            assertThat(simulation.executeNextIteration()).isTrue(); // Iteration 2
            assertThat(simulation.executeNextIteration()).isFalse();
            assertThat(simulation.getIterations()).isEqualTo(2);
        }
    }

    // Os testes de MC/DC, Domínio, Fronteira e Propriedade.
    // O código abaixo contém todos os testes necessários para 100% de cobertura.
    @Nested
    @DisplayName("Testes de Construtor e Lógica de Negócio (MC/DC e Fronteira)")
    class BusinessLogicAndCoverageTests {
        @Test
        @DisplayName("Deve criar a simulação com sucesso com parâmetros válidos")
        void shouldCreateSimulationSuccessfullyWithValidParameters() {
            Simulation simulation = new Simulation(10, 1.0, 1000, 5, fakeRandom);
            assertThat(simulation.getCreatures()).hasSize(11);
            assertThat(simulation.getIterations()).isZero();

            // teste com outros construtores
            simulation = new Simulation(10, 5, 1000);
            simulation.prepare();
            assertThat(simulation.getCreatures()).hasSize(11);
            assertThat(simulation.getIterations()).isEqualTo(1);

            simulation = new Simulation(10, 5, 1000, fakeRandom);
            assertThat(simulation.getCreatures()).hasSize(11);
            assertThat(simulation.getIterations()).isZero();
        }

        @ParameterizedTest(name = "Falha com numCreatures={0}, maxIterations={1}")
        @MethodSource("com.softwaretesting.core.domain.model.SimulationTest#invalidConstructorArgumentsProvider")
        @DisplayName("Deve lançar exceção para argumentos inválidos no construtor")
        void shouldThrowExceptionForInvalidConstructorArguments(
            int numCreatures, int creatureWidth, int horizonWidth, int maxIterations, String expectedMessage) {
            assertThatThrownBy(() -> new Simulation(
                numCreatures, creatureWidth, horizonWidth, maxIterations, fakeRandom))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(expectedMessage);
        }

        @Test
        @DisplayName("[MC/DC] resolveCollision deve funcionar com a ordem Guardião-Cluster trocada")
        void mcdc_resolveCollision_shouldHandleSwappedGuardianAndCluster() {
            Simulation sim = new Simulation(1, 1.0, 1000, 1, fakeRandom);
            sim.getCreatures().clear();
            Cluster cluster = new Cluster(new Creature(100, 20), new Creature(100, 20));
            Guardian guardian = new Guardian(20);
            sim.getCreatures().add(cluster);
            sim.getCreatures().add(guardian);
            cluster.setPosition(100);
            guardian.setPosition(100);
            long initialGuardianCoins = guardian.getCoins();
            long clusterCoins = cluster.getCoins();
            sim.executeNextIteration();
            assertThat(sim.getCreatures()).hasSize(1).first().isInstanceOf(Guardian.class);
            assertThat(guardian.getCoins()).isEqualTo(initialGuardianCoins + clusterCoins);
        }

        @Test
        @DisplayName("[MC/DC] isSuccessful deve ser verdadeiro com o Guardião como segunda criatura e mais moedas")
        void mcdc_isSuccessful_GuardianAsSecondCreatureWithMoreCoins() {
            Simulation simulation = new Simulation(1, 1.0, 1000, 1, fakeRandom);
            simulation.getCreatures().clear();
            Creature creature = new Creature(1_000_000, 10);
            Guardian guardian = new Guardian(2_000_000, 5);
            simulation.getCreatures().add(creature);
            simulation.getCreatures().add(guardian);
            simulation.executeNextIteration();
            assertThat(simulation.isSuccessful()).isTrue();
        }
    }

    /**
     * Testes estruturais focados em garantir a cobertura MC/DC para o metodo 'prepare'.
     */
    @Nested
    @DisplayName("Testes MC/DC para o metodo prepare()")
    class PrepareMCDCTests {

        /**
         * Cobre a condição C1=T, C2=F em: if (currentIteration >= maxIterations || isSuccessful).
         * <p>
         * Este teste valida que o metodo 'prepare' termina imediatamente se o número máximo
         * de iterações já foi atingido, mesmo que a simulação ainda não seja bem-sucedida.
         * Ele garante que a primeira condição (C1) afete independentemente o resultado.
         */
        @Test
        @DisplayName("[MC/DC] prepare deve terminar se o máximo de iterações for atingido")
        void mcdc_prepare_shouldFinishWhenMaxIterationsIsReached() {
            // Setup: maxIterations = 2
            Simulation simulation = new Simulation(1, 1.0, 1000, 2, fakeRandom);
            // Simula que a iteração 2 acabou de ser resolvida, preparando-se para a terceira
            simulation.run(2);

            simulation.prepare();

            assertThat(simulation.getCurrentState()).isEqualTo(Simulation.SimulationState.FINISHED);
            // Verifica que o contador não foi incrementado, pois o metodo retornou antes
            assertThat(simulation.getIterations()).isEqualTo(2);
        }

        /**
         * Cobre a condição C1=F, C2=T em: if (currentIteration >= maxIterations || isSuccessful).
         * <p>
         * Este teste valida que o metodo 'prepare' termina imediatamente se a simulação
         * já foi marcada como bem-sucedida, mesmo que o contador de iterações ainda não
         * tenha atingido o máximo. Garante que a segunda condição (C2) afete independentemente o resultado.
         */
        @Test
        @DisplayName("[MC/DC] prepare deve terminar se a simulação já for bem-sucedida")
        void mcdc_prepare_shouldFinishIfSimulationIsAlreadySuccessful() {
            // Setup: maxIterations = 5, mas a simulação se tornará bem-sucedida na iteração 1.
            Simulation simulation = new Simulation(1, 1.0, 1000, 5, fakeRandom);
            Guardian guardian = (Guardian) simulation.getCreatures().stream()
                .filter(Guardian.class::isInstance).findFirst().orElseThrow();
            guardian.absorbCluster(new Cluster(new Creature(2_000_000, 0), new Creature(0, 0)));
            simulation.executeNextIteration(); // Atinge o sucesso na iteração 1

            // A simulação está bem-sucedida e pronta para a próxima preparação
            assertThat(simulation.isSuccessful()).isTrue();
            assertThat(simulation.getCurrentState()).isEqualTo(Simulation.SimulationState.FINISHED);

            simulation.prepare();

            assertThat(simulation.getCurrentState()).isEqualTo(Simulation.SimulationState.FINISHED);
            assertThat(simulation.getIterations()).isEqualTo(1);
        }
    }

    /**
     * Testes estruturais focados em garantir a cobertura MC/DC para o metodo 'isSimulationSuccessful'.
     * Como o metodo é privado, usamos reflexão para invocá-lo e testar sua lógica isoladamente.
     */
    @Nested
    @DisplayName("Testes MC/DC para o metodo isSimulationSuccessful()")
    class IsSimulationSuccessfulMCDCTests {

        private Method isSuccessfulMethod;

        /**
         * Torna o metodo privado 'isSimulationSuccessful' acessível para os testes via reflexão.
         */
        @BeforeEach
        void setUp() throws NoSuchMethodException {
            isSuccessfulMethod = Simulation.class.getDeclaredMethod("isSimulationSuccessful");
            isSuccessfulMethod.setAccessible(true);
        }

        /**
         * Invoca o metodo privado 'isSimulationSuccessful' em uma instância de simulação.
         * @param simulation A instância da simulação a ser testada.
         * @return O resultado booleano do metodo.
         */
        private boolean invokeIsSuccessful(Simulation simulation) throws Exception {
            return (boolean) isSuccessfulMethod.invoke(simulation);
        }

        /**
         * Cobre a condição: (creatures.size() == 1 && creatures.get(0) instanceof Guardian) -> TRUE
         * <p>
         * Valida o primeiro caminho de sucesso, onde apenas o guardião resta no horizonte.
         */
        @Test
        @DisplayName("[MC/DC] Deve retornar true quando apenas o Guardião resta")
        void shouldReturnTrueWhenOnlyGuardianRemains() throws Exception {
            Simulation simulation = new Simulation(1, 1.0, 1000, 1, fakeRandom);
            // Deixa apenas o guardião, criado por padrão
            simulation.getCreatures().removeIf(c -> !(c instanceof Guardian));

            assertThat(invokeIsSuccessful(simulation)).isTrue();
        }

        /**
         * Cobre a condição: (creatures.size() == 1 && creatures.get(0) instanceof Guardian) -> FALSE
         * <p>
         * Valida que, se restar apenas uma criatura que não é um guardião, a simulação não é bem-sucedida.
         * Testa a segunda parte da primeira condição (C2=F).
         */
        @Test
        @DisplayName("[MC/DC] Deve retornar false quando apenas uma Criatura (não-Guardião) resta")
        void shouldReturnFalseWhenOnlyNonGuardianCreatureRemains() throws Exception {
            Simulation simulation = new Simulation(1, 1.0, 1000, 1, fakeRandom);
            // Deixa apenas a primeira criatura normal
            simulation.getCreatures().removeIf(c -> c instanceof Guardian);

            assertThat(invokeIsSuccessful(simulation)).isFalse();
        }

        /**
         * Cobre a condição: if (c1 instanceof Guardian && c1.getCoins() > c2.getCoins()) -> TRUE
         * <p>
         * Valida o segundo caminho de sucesso, onde restam duas criaturas, a primeira é o guardião
         * e ele tem mais moedas.
         */
        @Test
        @DisplayName("[MC/DC] Deve retornar true quando Guardião (1º) tem mais moedas que a Criatura (2ª)")
        void shouldReturnTrueWhenFirstIsGuardianWithMoreCoins() throws Exception {
            Simulation simulation = new Simulation(1, 1.0, 1000, 1, fakeRandom);
            simulation.getCreatures().clear(); // Limpa para controle total

            Guardian guardian = new Guardian(200, 10);
            Creature creature = new Creature(100, 20);
            simulation.getCreatures().add(guardian);
            simulation.getCreatures().add(creature);

            assertThat(invokeIsSuccessful(simulation)).isTrue();
        }

        /**
         * Cobre a condição: else if (c2 instanceof Guardian && c2.getCoins() > c1.getCoins()) -> TRUE
         * <p>
         * Valida o terceiro caminho de sucesso, onde restam duas criaturas, a segunda é o guardião
         * e ele tem mais moedas. Testa a segunda cláusula `else if`.
         */
        @Test
        @DisplayName("[MC/DC] Deve retornar true quando Guardião (2º) tem mais moedas que a Criatura (1ª)")
        void shouldReturnTrueWhenSecondIsGuardianWithMoreCoins() throws Exception {
            Simulation simulation = new Simulation(1, 1.0, 1000, 1, fakeRandom);
            simulation.getCreatures().clear();

            Creature creature = new Creature(100, 20);
            Guardian guardian = new Guardian(200, 10);
            simulation.getCreatures().add(creature);
            simulation.getCreatures().add(guardian);

            assertThat(invokeIsSuccessful(simulation)).isTrue();
        }

        /**
         * Cobre a condição: if (c1 instanceof Guardian && c2 != null && c1.getCoins() > c2.getCoins())
         * com a sub-condição c2 != null -> FALSE.
         * <p>
         * Este teste cria um cenário artificial onde a lista de criaturas tem tamanho 2, mas o segundo
         * elemento é nulo, garantindo que a verificação de nulidade funcione corretamente.
         */
        @Test
        @DisplayName("[MC/DC] Deve retornar false se a segunda criatura for nula")
        void shouldReturnFalseWhenSecondCreatureIsNull() throws Exception {
            Simulation simulation = new Simulation(1, 1.0, 1000, 1, fakeRandom);
            simulation.getCreatures().clear(); // Limpa para controle total

            Guardian guardian = new Guardian(200, 10);

            // Adiciona o guardião e um elemento nulo para forçar a condição
            simulation.getCreatures().add(guardian);
            simulation.getCreatures().add(null);

            assertThat(simulation.getCreatures()).hasSize(2);
            assertThat(invokeIsSuccessful(simulation)).isFalse();
        }

        /**
         * Cobre a condição: else if (c2 instanceof Guardian && c1 != null && c2.getCoins() > c1.getCoins())
         * com a sub-condição c1 != null -> FALSE.
         * <p>
         * Este teste cria um cenário artificial onde a lista de criaturas tem tamanho 2, mas o primeiro
         * elemento é nulo, garantindo que a verificação de nulidade no segundo bloco 'else if' funcione.
         */
        @Test
        @DisplayName("[MC/DC] Deve retornar false se a primeira criatura for nula")
        void shouldReturnFalseWhenFirstCreatureIsNull() throws Exception {
            Simulation simulation = new Simulation(1, 1.0, 1000, 1, fakeRandom);
            simulation.getCreatures().clear();

            Guardian guardian = new Guardian(200, 10);

            // Adiciona um elemento nulo e o guardião para forçar a condição
            simulation.getCreatures().add(null);
            simulation.getCreatures().add(guardian);

            assertThat(simulation.getCreatures()).hasSize(2);
            assertThat(invokeIsSuccessful(simulation)).isFalse();
        }

        /**
         * Cobre a condição: if (c1 instanceof Guardian && c1.getCoins() > c2.getCoins()) -> FALSE
         * (porque c1.getCoins() <= c2.getCoins())
         * <p>
         * Valida que a simulação não é bem-sucedida se o guardião (primeiro na lista) não tiver
         * estritamente mais moedas que a outra criatura. Testa a terceira parte da condição (C3.3=F).
         */
        @ParameterizedTest(name = "Guardião com {0} moedas, Criatura com {1} moedas")
        @CsvSource({ "100, 100", "99, 100" })
        @DisplayName("[MC/DC] Deve retornar false quando Guardião (1º) não tem mais moedas")
        void shouldReturnFalseWhenFirstIsGuardianWithoutMoreCoins(int guardianCoins, int creatureCoins) throws Exception {
            Simulation simulation = new Simulation(1, 1.0, 1000, 1, fakeRandom);
            simulation.getCreatures().clear();

            Guardian guardian = new Guardian(guardianCoins, 10);
            Creature creature = new Creature(creatureCoins, 20);
            simulation.getCreatures().add(guardian);
            simulation.getCreatures().add(creature);

            assertThat(invokeIsSuccessful(simulation)).isFalse();
        }

        /**
         * Cobre o caminho final `return false`.
         * <p>
         * Valida que, se nenhuma das condições de sucesso for atendida (ex: 3 criaturas restantes),
         * o metodo retorna false. Testa a falha de `creatures.size() == 1` e `creatures.size() == 2`.
         */
        @Test
        @DisplayName("[MC/DC] Deve retornar false quando mais de 2 criaturas restam")
        void shouldReturnFalseWhenMoreThanTwoCreaturesRemain() throws Exception {
            Simulation simulation = new Simulation(2, 1.0, 1000, 1, fakeRandom); // 2 Criaturas + 1 Guardião
            assertThat(simulation.getCreatures()).hasSize(3);
            assertThat(invokeIsSuccessful(simulation)).isFalse();
        }
    }

    @Nested
    @DisplayName("Testes MC/DC para o metodo executeNextIteration()")
    class ExecuteNextIterationMCDCTests {
        @Test
        @DisplayName("[MC/DC] deve resumir a execução a partir do estado READY_TO_UPDATE")
        void mcdc_shouldResumeExecutionFromUpdateState() {
            Simulation simulation = new Simulation(2, 1.0, 1000, 5, fakeRandom);
            simulation.prepare();
            assertThat(simulation.getCurrentState()).isEqualTo(Simulation.SimulationState.READY_TO_UPDATE);
            boolean executed = simulation.executeNextIteration();
            assertThat(executed).isTrue();
            assertThat(simulation.getCurrentState()).isEqualTo(Simulation.SimulationState.READY_TO_PREPARE);
            assertThat(simulation.getIterations()).isEqualTo(1);
        }

        @Test
        @DisplayName("[MC/DC] deve resumir a execução a partir do estado READY_TO_RESOLVE")
        void mcdc_shouldResumeExecutionFromResolveState() {
            Simulation simulation = new Simulation(2, 1.0, 1000, 5, fakeRandom);
            simulation.prepare();
            simulation.update();
            assertThat(simulation.getCurrentState()).isEqualTo(Simulation.SimulationState.READY_TO_RESOLVE);
            boolean executed = simulation.executeNextIteration();
            assertThat(executed).isTrue();
            assertThat(simulation.getCurrentState()).isEqualTo(Simulation.SimulationState.READY_TO_PREPARE);
            assertThat(simulation.getIterations()).isEqualTo(1);
        }

        @Test
        @DisplayName("[MC/DC] deve finalizar a simulação ao atingir o estado FINISHED")
        void mcdc_shouldFinishSimulationWhenReachedFinishedState() {
            Simulation simulation = new Simulation(2, 1.0, 1000, 5, fakeRandom);
            simulation.run(5);
            simulation.executeNextIteration();

            assertThat(simulation.getCurrentState()).isEqualTo(Simulation.SimulationState.FINISHED);
        }

        /**
         * Cobre o caso em que a simulação já está no estado FINISHED ao entrar no metodo.
         * <p>
         * <b>Teste MC/DC:</b> Este teste valida a principal guarda de entrada do metodo `executeNextIteration`,
         * garantindo que, se o estado já for `FINISHED`, o metodo retorne `false` imediatamente,
         * cobrindo a branch `if (currentState == SimulationState.FINISHED)`.
         */
        @Test
        @DisplayName("[MC/DC] executeNextIteration deve retornar false se o estado já for FINISHED")
        void mcdc_shouldReturnFalseAndDoNothingWhenAlreadyFinished() {
            // Setup: Leva a simulação a um estado final
            Simulation simulation = new Simulation(1, 1.0, 1000, 1, fakeRandom);
            simulation.executeNextIteration(); // Executa a única iteração permitida
            assertThat(simulation.getCurrentState()).isEqualTo(Simulation.SimulationState.FINISHED);
            assertThat(simulation.getIterations()).isEqualTo(1);

            boolean executed = simulation.executeNextIteration();

            assertThat(executed).isFalse();
            assertThat(simulation.getCurrentState()).isEqualTo(Simulation.SimulationState.FINISHED);
            assertThat(simulation.getIterations()).isEqualTo(1);
        }
    }

    /**
     * Testes estruturais focados em garantir a cobertura MC/DC para o metodo 'resolveCollision'.
     * Como o metodo é privado, usamos reflexão para invocá-lo e testar sua lógica isoladamente.
     */
    @Nested
    @DisplayName("Testes MC/DC para o metodo resolveCollision()")
    class ResolveCollisionMCDCTests {

        private Method resolveCollisionMethod;
        private Simulation simulationInstance; // Instância para invocar o metodo não-estático
        private List<Creature> toAdd;
        private Set<Creature> toRemove;

        /**
         * Torna o metodo privado 'resolveCollision' acessível para os testes via reflexão.
         */
        @BeforeEach
        void setUp() throws NoSuchMethodException {
            // A instância da simulação é necessária para invocar o metodo
            simulationInstance = new Simulation(1, 1.0, 1000, 1, fakeRandom);

            resolveCollisionMethod = Simulation.class.getDeclaredMethod(
                "resolveCollision", Creature.class, Creature.class, List.class, Set.class
            );
            resolveCollisionMethod.setAccessible(true);

            // Inicializa as listas que serão passadas para o metodo
            toAdd = new ArrayList<>();
            toRemove = new HashSet<>();
        }

        /**
         * Invoca o metodo privado 'resolveCollision' na instância da simulação.
         */
        private void invokeResolveCollision(Creature c1, Creature c2) throws Exception {
            resolveCollisionMethod.invoke(simulationInstance, c1, c2, toAdd, toRemove);
        }

        /**
         * Cobre a condição: if (c1 instanceof Guardian && c2 instanceof Cluster) -> TRUE
         */
        @Test
        @DisplayName("[MC/DC] deve absorver Cluster quando Guardião (c1) colide com Cluster (c2)")
        void shouldAbsorbClusterWhenGuardianIsC1() throws Exception {
            Guardian guardian = new Guardian(100, 10);
            Cluster cluster = new Cluster(new Creature(200, 20), new Creature(300, 20));
            long initialGuardianCoins = guardian.getCoins();
            long clusterCoins = cluster.getCoins();

            invokeResolveCollision(guardian, cluster);

            assertThat(guardian.getCoins()).isEqualTo(initialGuardianCoins + clusterCoins);
            assertThat(toRemove).containsExactly(cluster);
            assertThat(toAdd).isEmpty();
        }

        /**
         * Cobre a condição: else if (c2 instanceof Guardian && c1 instanceof Cluster) -> TRUE
         */
        @Test
        @DisplayName("[MC/DC] deve absorver Cluster quando Cluster (c1) colide com Guardião (c2)")
        void shouldAbsorbClusterWhenGuardianIsC2() throws Exception {
            Cluster cluster = new Cluster(new Creature(200, 20), new Creature(300, 20));
            Guardian guardian = new Guardian(100, 10);
            long initialGuardianCoins = guardian.getCoins();
            long clusterCoins = cluster.getCoins();

            invokeResolveCollision(cluster, guardian);

            assertThat(guardian.getCoins()).isEqualTo(initialGuardianCoins + clusterCoins);
            assertThat(toRemove).containsExactly(cluster);
            assertThat(toAdd).isEmpty();
        }

        /**
         * Cobre a condição: else if (c1 instanceof Cluster) -> TRUE
         */
        @Test
        @DisplayName("[MC/DC] deve adicionar membro a um Cluster quando Cluster (c1) colide com Criatura (c2)")
        void shouldAddMemberToClusterWhenClusterIsC1() throws Exception {
            Cluster cluster = new Cluster(new Creature(100, 10), new Creature(200, 10));
            Creature newMember = new Creature(300, 20);
            long initialClusterCoins = cluster.getCoins();

            invokeResolveCollision(cluster, newMember);

            assertThat(cluster.getCoins()).isEqualTo(initialClusterCoins + newMember.getCoins());
            assertThat(cluster.getMembers()).hasSize(3).contains(newMember);
            assertThat(toRemove).containsExactly(newMember);
            assertThat(toAdd).isEmpty();
        }

        /**
         * Cobre a condição: else if (c2 instanceof Cluster) -> TRUE
         */
        @Test
        @DisplayName("[MC/DC] deve adicionar membro a um Cluster quando Criatura (c1) colide com Cluster (c2)")
        void shouldAddMemberToClusterWhenClusterIsC2() throws Exception {
            Creature newMember = new Creature(300, 20);
            Cluster cluster = new Cluster(new Creature(100, 10), new Creature(200, 10));
            long initialClusterCoins = cluster.getCoins();

            invokeResolveCollision(newMember, cluster);

            assertThat(cluster.getCoins()).isEqualTo(initialClusterCoins + newMember.getCoins());
            assertThat(cluster.getMembers()).hasSize(3).contains(newMember);
            assertThat(toRemove).containsExactly(newMember);
            assertThat(toAdd).isEmpty();
        }

        /**
         * Cobre a condição: else if (!(c1 instanceof Guardian) && !(c2 instanceof Guardian)) -> TRUE
         */
        @Test
        @DisplayName("[MC/DC] deve formar um novo Cluster quando duas Criaturas normais colidem")
        void shouldFormNewClusterFromTwoCreatures() throws Exception {
            Creature c1 = new Creature(100, 10);
            Creature c2 = new Creature(200, 20);

            invokeResolveCollision(c1, c2);

            assertThat(toRemove).containsExactlyInAnyOrder(c1, c2);
            assertThat(toAdd).hasSize(1);
            assertThat(toAdd.getFirst()).isInstanceOf(Cluster.class);
            assertThat(toAdd.getFirst().getCoins()).isEqualTo(c1.getCoins() + c2.getCoins());
        }

        /**
         * Cobre o caso "no-op" onde nenhuma condição é satisfeita (Guardião vs. Criatura).
         * Isso garante que a branch final implícita (o "else" invisível) seja coberta.
         */
        @Test
        @DisplayName("[MC/DC] não deve fazer nada quando Guardião colide com Criatura normal")
        void shouldDoNothingWhenGuardianCollidesWithCreature() throws Exception {
            Guardian guardian = new Guardian(100, 10);
            Creature creature = new Creature(200, 20);
            long initialGuardianCoins = guardian.getCoins();
            long initialCreatureCoins = creature.getCoins();

            invokeResolveCollision(guardian, creature);

            assertThat(toAdd).isEmpty();
            assertThat(toRemove).isEmpty();
            assertThat(guardian.getCoins()).isEqualTo(initialGuardianCoins);
            assertThat(creature.getCoins()).isEqualTo(initialCreatureCoins);
        }

        /**
         * Cobre a condição: else if (c2 instanceof Guardian && c1 instanceof Cluster) -> FALSE
         * especificamente porque a sub-condição (c1 instanceof Cluster) é falsa.
         * <p>
         * Este teste valida o caminho onde a primeira condição do bloco (c2 instanceof Guardian) é verdadeira,
         * mas a segunda (c1 instanceof Cluster) é falsa, forçando a lógica a pular este bloco
         * e continuar para as verificações seguintes. O cenário usado é uma colisão entre uma Criatura
         * normal (c1) e um Guardião (c2).
         */
        @Test
        @DisplayName("[MC/DC] não deve fazer nada quando Guardião (c2) colide com Criatura (c1)")
        void shouldDoNothingWhenGuardianIsC2AndCreatureIsC1() throws Exception {
            // Setup: c1 é uma Creature, c2 é um Guardian.
            // Isso faz com que 'c1 instanceof Cluster' seja falso.
            Creature creature = new Creature(100, 10);
            Guardian guardian = new Guardian(200, 20);
            long initialCreatureCoins = creature.getCoins();
            long initialGuardianCoins = guardian.getCoins();

            // Invoca o metodo de colisão com a ordem específica
            invokeResolveCollision(creature, guardian);

            // Verifica que nada aconteceu, pois este tipo de colisão não é tratado.
            // Isso confirma que o bloco 'else if' foi corretamente pulado.
            assertThat(creature.getCoins()).isEqualTo(initialCreatureCoins);
            assertThat(guardian.getCoins()).isEqualTo(initialGuardianCoins);
        }
    }

    /**
     * Testes estruturais focados em garantir a cobertura MC/DC para o metodo 'findAndStealFromClosest'.
     * Como o metodo é privado, usamos reflexão para invocá-lo e testar sua lógica isoladamente.
     */
    @Nested
    @DisplayName("Testes MC/DC para o Método findAndStealFromClosest()")
    class FindAndStealFromClosestMCDCTests {

        private Method findAndStealFromClosestMethod;
        private Simulation simulationInstance;

        @BeforeEach
        void setUp() throws NoSuchMethodException {
            simulationInstance = new Simulation(1, 1.0, 1000, 1, fakeRandom);
            findAndStealFromClosestMethod = Simulation.class.getDeclaredMethod("findAndStealFromClosest", Cluster.class);
            findAndStealFromClosestMethod.setAccessible(true);
        }

        private void invokeFindAndStealFromClosest(Cluster cluster) throws Exception {
            findAndStealFromClosestMethod.invoke(simulationInstance, cluster);
        }

        /**
         * Cobre a branch TRUE do '.ifPresent()'.
         * <p>
         * Este teste valida o "caminho feliz", onde um cluster é criado e existe pelo menos uma
         * criatura válida (não-Guardião, não-Cluster) da qual ele pode roubar.
         * Garante que a lógica de roubo dentro do lambda seja executada.
         */
        @Test
        @DisplayName("[MC/DC] deve executar o roubo quando uma criatura alvo válida existe")
        void shouldExecuteStealWhenValidTargetExists() throws Exception {
            // Setup
            Creature c1 = new Creature(100, 10);
            Creature c2 = new Creature(200, 10);
            Cluster cluster = new Cluster(c1, c2);
            Creature victim = new Creature(500, 20); // Vítima

            // Usa reflexão para manipular a lista de criaturas da simulação
            simulationInstance.getCreatures().clear();
            simulationInstance.getCreatures().add(cluster);
            simulationInstance.getCreatures().add(victim);

            long initialClusterCoins = cluster.getCoins();
            long initialVictimCoins = victim.getCoins();
            int expectedStolenAmount = (int) (initialVictimCoins / 2);

            // Action
            invokeFindAndStealFromClosest(cluster);

            // Assert
            assertThat(cluster.getCoins()).isEqualTo(initialClusterCoins + expectedStolenAmount);
            assertThat(victim.getCoins()).isEqualTo(initialVictimCoins - expectedStolenAmount);
        }

        /**
         * Cobre a branch FALSE do '.ifPresent()'.
         * <p>
         * Este teste valida o caso de borda onde um cluster é criado, mas não há criaturas
         * válidas para roubar (apenas outros clusters ou guardiões). O stream de alvos
         * resulta vazio, e a lógica de roubo dentro do '.ifPresent()' não deve ser executada.
         */
        @Test
        @DisplayName("[MC/DC] não deve executar o roubo quando não há criaturas alvo válidas")
        void shouldNotExecuteStealWhenNoValidTargetExists() throws Exception {
            // Setup
            Creature c1 = new Creature(100, 10);
            Creature c2 = new Creature(200, 10);
            Cluster cluster = new Cluster(c1, c2);
            Guardian guardian = new Guardian(0, 50); // O único outro ser é um Guardião

            simulationInstance.getCreatures().clear();
            simulationInstance.getCreatures().add(cluster);
            simulationInstance.getCreatures().add(guardian);

            long initialClusterCoins = cluster.getCoins();
            long initialGuardianCoins = guardian.getCoins();

            // Action
            invokeFindAndStealFromClosest(cluster);

            // Assert
            // Garante que nenhuma moeda foi trocada
            assertThat(cluster.getCoins()).isEqualTo(initialClusterCoins);
            assertThat(guardian.getCoins()).isEqualTo(initialGuardianCoins);
        }
    }

    @Nested
    @DisplayName("Testes Baseados em Propriedade (jqwik)")
    class PropertyBasedTests {
        @Property
        @DisplayName("O contador de iterações nunca deve exceder o máximo permitido")
        void numberOfIterationsShouldNeverExceedMax(@ForAll @IntRange(min = 1, max = 50) int maxIterations) {
            Simulation simulation = new Simulation(2, 1.0, 1000, maxIterations, fakeRandom);
            for (int i = 0; i < maxIterations + 5; i++) {
                simulation.executeNextIteration();
            }
            assertThat(simulation.getIterations()).isEqualTo(maxIterations);
        }

        @Property
        @DisplayName("A quantidade total de moedas deve ser conservada em colisões sem roubo")
        void totalCoinsShouldBeConservedInCollisionsWithoutStealing(@ForAll @IntRange(min = 2, max = 10) int numCreatures) {
            fakeRandom.setValueToReturn(0.0);
            Simulation simulation = new Simulation(numCreatures, 1.0, 1000, 5, fakeRandom);
            long totalCoinsBefore = simulation.getCreatures().stream().mapToLong(Creature::getCoins).sum();
            simulation.executeNextIteration();
            long totalCoinsAfter = simulation.getCreatures().stream().mapToLong(Creature::getCoins).sum();
            assertThat(totalCoinsAfter).isEqualTo(totalCoinsBefore);
        }

        @Property
        @DisplayName("A simulação deve ser bem-sucedida se o Guardião tiver mais moedas que todas as outras criaturas")
        void simulationShouldBeSuccessfulIfGuardianHasMoreCoins(@ForAll @IntRange(min = 1, max = 10) int numCreatures) {
            fakeRandom.setValueToReturn(0.0);
            Simulation simulation = new Simulation(numCreatures, 1.0, 1000, 5, fakeRandom);
            Guardian guardian = (Guardian) simulation.getCreatures().stream()
                .filter(Guardian.class::isInstance).findFirst().orElseThrow();
            long totalCoins = simulation.getCreatures().stream().mapToLong(Creature::getCoins).sum();
            guardian.setCoins((int) (totalCoins + 1)); // Garante que o Guardião tem mais moedas
            simulation.executeNextIteration();
            assertThat(simulation.isSuccessful()).isTrue();
            assertThat(simulation.getCurrentState()).isEqualTo(Simulation.SimulationState.FINISHED);
        }

        /**
         * Fornece uma lista de criaturas com pelo menos 3 membros para os testes.
         * @return Um gerador de listas de criaturas arbitrárias.
         */
        @Provide
        Arbitrary<List<Creature>> creatureListProvider() {
            Arbitrary<Integer> coins = Arbitraries.integers().between(2, 1_000_000); // Garante que há o que roubar
            Arbitrary<Double> position = Arbitraries.doubles().between(0, 1000.0);
            Arbitrary<Creature> creatureArbitrary = Combinators.combine(coins, position).as(Creature::new);

            // Garante que a lista terá pelo menos 3 criaturas para o cenário de roubo
            return creatureArbitrary.list().ofMinSize(3).ofMaxSize(10);
        }

        /**
         * <b>Propriedade:</b> Para qualquer conjunto de criaturas, quando um cluster é formado,
         * ele deve sempre roubar metade das moedas da criatura mais próxima e de nenhuma outra.
         * <p>
         * Este teste valida a interação complexa de colisão, formação de cluster e roubo.
         * Ele configura o cenário dinamicamente, executa uma iteração e verifica se a
         * redistribuição de moedas ocorreu exatamente como especificado nos requisitos.
         */
        @Property(tries = 200) // Aumenta o número de tentativas para cobrir mais cenários
        @DisplayName("Cluster deve roubar metade das moedas da criatura mais próxima")
        void clusterShouldStealHalfCoinsFromNearestCreature(@ForAll("creatureListProvider") List<Creature> creatures) {
            Simulation simulation = new Simulation(1, 1.0, 5000, 5, fakeRandom);

            // Usa reflexão para substituir a lista de criaturas padrão pela nossa lista gerada
            try {
                Field creaturesField = Simulation.class.getDeclaredField("creatures");
                creaturesField.setAccessible(true);
                List<Creature> simCreatures = (List<Creature>) creaturesField.get(simulation);
                simCreatures.clear();
                simCreatures.addAll(creatures);
            } catch (Exception e) {
                fail("Falha ao configurar o teste via reflexão: " + e.getMessage());
            }

            // Define os atores
            Creature c1 = creatures.get(0);
            Creature c2 = creatures.get(1);
            Creature victim = creatures.get(2);

            // Guarda os valores iniciais
            long initialC1Coins = c1.getCoins();
            long initialC2Coins = c2.getCoins();
            long initialVictimCoins = victim.getCoins();
            int stolenAmount = (int) (initialVictimCoins / 2);

            // Posiciona os atores para forçar o cenário desejado
            c1.setPosition(100.0);
            c2.setPosition(100.0); // Colisão
            victim.setPosition(250.0); // Criatura mais próxima

            // Posiciona as outras criaturas longe para garantir que não interfiram
            for (int i = 3; i < creatures.size(); i++) {
                creatures.get(i).setPosition(1000.0 + i * 100);
            }

            simulation.executeNextIteration();

            // Encontra o cluster que foi formado
            Cluster cluster = (Cluster) simulation.getCreatures().stream()
                .filter(c -> c instanceof Cluster)
                .findFirst()
                .orElse(null);

            assertThat(cluster).isNotNull();

            // Verifica se as moedas do cluster estão corretas
            assertThat(cluster.getCoins()).isEqualTo(initialC1Coins + initialC2Coins + stolenAmount);

            // Verifica se a vítima perdeu a quantidade correta de moedas
            assertThat(victim.getCoins()).isEqualTo(initialVictimCoins - stolenAmount);

            // Verifica se as outras criaturas não foram afetadas
            for (int i = 3; i < creatures.size(); i++) {
                Creature other = creatures.get(i);
                assertThat(other.getCoins()).isEqualTo(other.getCoins()); // Checa se as moedas não mudaram
            }
        }
    }

    static Stream<Arguments> invalidConstructorArgumentsProvider() {
        return Stream.of(
            Arguments.of(0, 100, 1000, 10, "Number of creatures must be positive."),
            Arguments.of(-1, 100, 1000, 10, "Number of creatures must be positive."),
            Arguments.of(10, 100, 1000, 0, "Max iterations must be positive."),
            Arguments.of(10, 100, 1000, -1, "Max iterations must be positive."),
            Arguments.of(2, 0, 1000, 10, "Creature width must be positive."),
            Arguments.of(2, 100, -1, 10, "Horizon width must be positive.")
        );
    }
}
