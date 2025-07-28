package com.softwaretesting.core.domain.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.stream.Stream;

class SimulationTest {

    private Simulation simulation;
    private final int numCreatures = 2;

    @BeforeEach
    void setUp() {
        // Reseta o contador de IDs para garantir consistência entre os testes
        Creature.resetIdCounter();

        // Usa um provedor de números aleatórios previsível para testes consistentes
        RandomProvider predictableRandom = (min, max) -> 0.5 * (max - min) + min;
        simulation = new Simulation(numCreatures, 100, 2000, 10, predictableRandom);
    }

    @Test
    void constructor_ShouldCreateCorrectNumberOfEntities() {
        // Espera N criaturas + 1 guardião.
        assertEquals(numCreatures + 1, simulation.getCreatures().size());
        long guardianCount = simulation.getCreatures().stream().filter(c -> c instanceof Guardian).count();
        assertEquals(1, guardianCount);
    }

    @Test
    void processNextCreatureInTurn_ShouldCycleThroughCreaturesAndThenGuardian() {
        // Primeiro turno deve ser da criatura com ID 0
        Optional<Creature> firstCreatureOpt = simulation.processNextCreatureInTurn();
        assertTrue(firstCreatureOpt.isPresent());
        assertEquals(0, firstCreatureOpt.get().getId(),
            "A primeira criatura processada deve ser a de ID 0.");

        // Segundo turno deve ser da criatura com ID 1
        Optional<Creature> secondCreatureOpt = simulation.processNextCreatureInTurn();
        assertTrue(secondCreatureOpt.isPresent());
        assertEquals(1, secondCreatureOpt.get().getId(),
            "A segunda criatura processada deve ser a de ID 1.");

        // Terceiro turno deve ser do Guardião, pois todas as outras já foram processadas no round.
        Optional<Creature> guardianTurnOpt = simulation.processNextCreatureInTurn();
        assertTrue(guardianTurnOpt.isPresent());
        assertInstanceOf(Guardian.class, guardianTurnOpt.get(),
            "Após todas as criaturas, o próximo deve ser o Guardião.");

        // Quarto turno deve voltar para a criatura com ID 0, iniciando um novo round.
        Optional<Creature> fourthCreatureOpt = simulation.processNextCreatureInTurn();
        assertTrue(fourthCreatureOpt.isPresent());
        assertEquals(0, fourthCreatureOpt.get().getId(),
            "Após o guardião, o ciclo deve recomeçar com a criatura de ID 0.");
    }

    @Test
    void resolveTurnFor_StealAction_ShouldTransferCoins() {
        Creature creature0 = simulation.getCreatures().get(0);
        Creature creature1 = simulation.getCreatures().get(1);
        creature0.setPosition(100);
        creature1.setPosition(150); // Posição mais próxima

        // Força o alvo do roubo.
        creature0.setTargetPosition(100);
        simulation.resolveTurnFor(creature0);

        // Verifica se as moedas foram transferidas corretamente.
        assertEquals(1_500_000, creature0.getCoins());
        assertEquals(500_000, creature1.getCoins());
    }

    @Test
    void handleCollisionsFor_TwoCreatures_ShouldFormClusterAndPreserveTurnOrder() {
        Creature creature0 = simulation.getCreatures().get(0);
        Creature creature1 = simulation.getCreatures().get(1);

        // Força uma colisão durante o turno da Criatura 1
        creature0.setPosition(100);
        creature1.setTargetPosition(101); // Moverá para perto de creature0

        // A Criatura 1 age, colide e forma um cluster.
        simulation.resolveTurnFor(creature1);

        assertEquals(2, simulation.getCreatures().size()); // 1 cluster e 1 guardião
        Creature newEntity = simulation.getCreatures().getFirst(); // O cluster deve substituir a criatura
        assertInstanceOf(Cluster.class, newEntity);
        assertEquals(2_000_000, newEntity.getCoins());
    }

    @Test
    void checkEndCondition_ShouldFinishWhenOnlyGuardianRemains() {
        Creature creature0 = simulation.getCreatures().get(0);
        Creature creature1 = simulation.getCreatures().get(1);
        Guardian guardian = (Guardian) simulation.getCreatures().get(2);

        guardian.absorbCluster(new Cluster(creature0, creature1));
        simulation.getCreatures().remove(creature0);
        simulation.getCreatures().remove(creature1);

        simulation.resolveTurnFor(guardian);

        assertTrue(simulation.isFinished());
        assertTrue(simulation.isSuccessful());
        assertEquals("SUCCESS! Only the guardian remains.", simulation.getFinalMessage());
    }

    @Test
    void checkEndCondition_ShouldFinishWhenGuardianHasMoreCoins() {
        Creature creature0 = simulation.getCreatures().get(0);
        simulation.getCreatures().remove(1);
        Guardian guardian = (Guardian) simulation.getCreatures().get(1);

        guardian.setCoins(1000);
        creature0.setCoins(500);

        simulation.resolveTurnFor(guardian);

        assertTrue(simulation.isFinished());
        assertTrue(simulation.isSuccessful());
    }

    @Test
    void checkEndCondition_ShouldFinishWhenCreatureHasMoreCoinsButNotCluster() {
        Creature creature0 = simulation.getCreatures().get(0);
        simulation.getCreatures().remove(1);
        Guardian guardian = (Guardian) simulation.getCreatures().get(1);

        guardian.setCoins(500);
        creature0.setCoins(1000);

        simulation.resolveTurnFor(guardian);

        assertTrue(simulation.isFinished());
        assertFalse(simulation.isSuccessful());
        assertEquals("DEFEAT! The final creature has more coins than the guardian.",
            simulation.getFinalMessage());
    }

    @Test
    void runToEnd_ShouldFinishWhenIterationLimitIsReached() {
        Simulation longSim = new Simulation(2, 100, 2000,
            5, (min, max) -> 0.1);
        longSim.runToEnd();
        assertTrue(longSim.isFinished());
        assertFalse(longSim.isSuccessful());
        assertEquals("Simulation Over: Iteration limit reached.", longSim.getFinalMessage());
    }

    @Nested
    class ConstructorTests {
        // Usa um provedor de números aleatórios previsível para testes consistentes
        RandomProvider fakeRandom = (min, max) -> 0.5 * (max - min) + min;

        @BeforeEach
        void setUp() {
            // Reseta o contador de IDs para garantir consistência entre os testes
            Creature.resetIdCounter();

            simulation = new Simulation(numCreatures, 100, 2000,
                10, fakeRandom);
        }

        @Test
        @DisplayName("Deve criar a simulação com sucesso com parâmetros válidos")
        void shouldCreateSimulationSuccessfullyWithValidParameters() {
            Simulation simulation = new Simulation(10, 1.0, 1000,
                5, fakeRandom);
            assertThat(simulation.getCreatures()).hasSize(11);
            assertThat(simulation.getIterations()).isZero();

            // teste com outros construtores
            simulation = new Simulation(10, 5, 1000, fakeRandom);
            assertThat(simulation.getCreatures()).hasSize(11);
            assertThat(simulation.getIterations()).isZero();
        }

        @Test
        @DisplayName("Deve criar a simulação com um número específico de criaturas")
        void shouldCreateSimulationWithSpecificNumberOfCreatures() {
            Simulation simulation = new Simulation(5, 100, 2000,
                10, fakeRandom);
            assertThat(simulation.getCreatures()).hasSize(6); // 5 criaturas + 1 guardião
            assertThat(simulation.getCreatures().get(5)).isInstanceOf(Guardian.class);
        }

        @Test
        @DisplayName("creatureWidth deve ser igual a 1.0 se não for especificado")
        void shouldDefaultCreatureWidthToOneIfNotSpecified() {
            Simulation simulation = new Simulation(5, 1000, 10, fakeRandom);
            assertEquals(1.0, simulation.getCreatureWidth(),
                "Creature width should default to 1.0 if not specified.");

            simulation = new Simulation(10, 5, 1);
            assertEquals(1.0, simulation.getCreatureWidth(),
                "Creature width should default to 1.0 if not specified.");
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
    }

    @Nested
    @DisplayName("Testes MC/DC para o método runToEnd()")
    class RunToEndMCDCTests {

        @Test
        @DisplayName("Loop continua: isFinished=false, activeCreature is Present")
        void runToEnd_LoopContinues_WhenNotFinishedAndCreatureIsPresent() {
            // Condição: '!isFinished() = true', 'activeCreatureOpt.isEmpty()' = false
            // Este teste verifica o caminho feliz, onde o loop é executado até o fim
            Simulation sim = new Simulation(2, 100, 1000,
                3, (min, max) -> 0.1); // Simulação curta

            sim.runToEnd();

            // Atesta que a simulação de fato terminou, provando que o loop foi executado
            assertTrue(sim.isFinished(), "A simulação deve terminar após a execução do loop.");
        }

        @Test
        @DisplayName("Loop para: isFinished=true, activeCreature is Present (não avaliado)")
        void runToEnd_LoopStops_WhenSimulationIsAlreadyFinished() {
            // Condição: '!isFinished() = false'. O loop não deve ser iniciado
            simulation.isFinished = true; // Força o estado inicial para 'terminado'
            int initialCoins = simulation.getCreatures().getFirst().getCoins();

            simulation.runToEnd();

            // Atesta que nenhuma iteração ocorreu, pois a condição !isFinished() falhou na primeira verificação
            assertEquals(initialCoins, simulation.getCreatures().getFirst().getCoins(),
                "Nenhuma ação deve ocorrer se a simulação já começou como 'finished'.");
        }

        @Test
        @DisplayName("Loop para: isFinished=false, activeCreature is Empty")
        void runToEnd_LoopStops_WhenNoMoreCreaturesToProcess() {
            // Condição: '!isFinished() = true', mas 'activeCreatureOpt.isEmpty()' se torna true.
            // Configura uma simulação que terminará na primeira chamada de processNextCreatureInTurn
            Simulation simWithZeroIterations = new Simulation(2, 100,
                1000, 1, (min, max) -> 0.1);

            simWithZeroIterations.runToEnd();

            // Atesta que a simulação terminou e a mensagem correta foi definida,
            // provando que o 'break' foi acionado pela condição activeCreatureOpt.isEmpty()
            assertTrue(simWithZeroIterations.isFinished());
            assertEquals("Simulation Over: Iteration limit reached.", simWithZeroIterations.getFinalMessage());
        }
    }

    @Nested
    @DisplayName("Testes MC/DC para o método processNextCreatureInTurn()")
    class ProcessNextCreatureMCDCTests {

        @Test
        @DisplayName("C1: Retorna Optional.empty se a simulação já estiver finalizada")
        void shouldReturnEmptyWhenSimulationIsFinished() {
            // Condição: isFinished = true
            simulation.isFinished = true;
            Optional<Creature> result = simulation.processNextCreatureInTurn();
            assertTrue(result.isEmpty(), "Deve retornar vazio se a simulação já terminou.");
        }

        @Test
        @DisplayName("C2: Inicia um novo round se o guardião já agiu no round anterior")
        void shouldStartNewRoundAfterGuardianHasActed() {
            // Condição: guardianHasActedInRound = true
            simulation.processNextCreatureInTurn(); // Creature 0
            simulation.processNextCreatureInTurn(); // Creature 1
            simulation.processNextCreatureInTurn(); // Guardian (agora guardianHasActedInRound é true)

            assertEquals(0, simulation.getIterations(),
                "A iteração ainda deve ser 0 antes de iniciar o novo round.");

            Optional<Creature> result = simulation.processNextCreatureInTurn(); // Inicia o round 1

            assertEquals(1, simulation.getIterations(),
                "Deve incrementar a iteração ao iniciar um novo round.");
            assertTrue(result.isPresent());
            assertEquals(0, result.get().getId(),
                "Deve retornar a primeira criatura no novo round.");
        }

        @Test
        @DisplayName("C3: Retorna Optional.empty se o limite de iterações for atingido")
        void shouldReturnEmptyWhenMaxIterationsIsReached() {
            // Condição: currentIteration >= maxIterations
            Simulation shortSim = new Simulation(2, 100, 1000,
                1, (min, max) -> 0.1);

            shortSim.processNextCreatureInTurn(); // Round 0, Creature 0
            shortSim.processNextCreatureInTurn(); // Round 0, Creature 1
            shortSim.processNextCreatureInTurn(); // Round 0, Guardian

            // A próxima chamada tentará iniciar o Round 1, que é >= maxIterations (1)
            Optional<Creature> result = shortSim.processNextCreatureInTurn();

            assertTrue(result.isEmpty(), "Deve retornar vazio ao atingir o limite de iterações.");
            assertTrue(shortSim.isFinished());
            assertEquals("Simulation Over: Iteration limit reached.", shortSim.getFinalMessage());
        }

        @Test
        @DisplayName("C4 & C5: Retorna a próxima criatura saltitante se houver uma no round atual")
        void shouldReturnNextJumpingCreatureWhenAvailable() {
            // Condição: loop 'while (searchIndex < creatures.size())' encontra uma criatura
            // que satisfaz '!(nextCreature instanceof Guardian)'

            // Turno 1
            Optional<Creature> result1 = simulation.processNextCreatureInTurn();
            assertTrue(result1.isPresent());
            assertEquals(0, result1.get().getId());

            // Turno 2
            Optional<Creature> result2 = simulation.processNextCreatureInTurn();
            assertTrue(result2.isPresent());
            assertEquals(1, result2.get().getId());
        }

        @Test
        @DisplayName("C6: Retorna o guardião se não houver mais criaturas saltitantes no round")
        void shouldReturnGuardianWhenNoJumpingCreaturesAreLeftInRound() {
            // Condição: O primeiro loop 'while' termina, e o loop 'for' encontra o guardião
            simulation.processNextCreatureInTurn(); // Creature 0
            simulation.processNextCreatureInTurn(); // Creature 1

            // Agora, não há mais criaturas para processar no round 0
            Optional<Creature> result = simulation.processNextCreatureInTurn();

            assertTrue(result.isPresent());
            assertInstanceOf(Guardian.class, result.get());
        }

        @Test
        @DisplayName("C7: Inicia um novo round recursivamente se o guardião estiver ausente")
        void shouldStartNewRoundRecursivelyWhenGuardianIsMissing() {
            // Remove o guardião da simulação para forçar o caminho de falha
            simulation.getCreatures().removeIf(creature -> creature instanceof Guardian);
            assertEquals(numCreatures, simulation.getCreatures().size());

            // Executa os turnos para todas as criaturas normais
            simulation.processNextCreatureInTurn(); // Retorna criatura 0
            simulation.processNextCreatureInTurn(); // Retorna criatura 1

            // Agora, não há mais criaturas saltitantes nem guardião no round 0
            // A próxima chamada deve acionar as linhas de fallback
            Optional<Creature> result = simulation.processNextCreatureInTurn();

            // O fallback deve iniciar um novo round (iteração 1)
            assertEquals(1, simulation.getIterations(),
                "Deve incrementar a iteração para iniciar um novo round.");
            // A chamada recursiva deve então retornar a primeira criatura do novo round
            assertTrue(result.isPresent());
            assertEquals(0, result.get().getId(),
                "Deve retornar a primeira criatura no novo round após a recursão.");
        }
    }

    @Nested
    @DisplayName("Testes MC/DC para resolveTurnFor()")
    class ResolveTurnForMCDCTests {

        @Test
        @DisplayName("Branch 1 (True): Deve retornar imediatamente se a simulação já estiver finalizada")
        void shouldReturnImmediatelyWhenSimulationIsFinished() {
            simulation.isFinished = true;
            Creature creature = simulation.getCreatures().getFirst();
            int initialCoins = creature.getCoins();

            simulation.resolveTurnFor(creature);

            assertEquals(initialCoins, creature.getCoins(),
                "As moedas não deveriam mudar se a simulação já terminou.");
        }

        @Test
        @DisplayName("Branch 2 (True): Deve retornar imediatamente se a criatura for nula")
        void shouldReturnImmediatelyWhenCreatureIsNull() {
            // O metodo deve lidar com a entrada nula sem lançar uma exceção
            assertDoesNotThrow(() -> simulation.resolveTurnFor(null));
        }

        @Test
        @DisplayName("Branch 3 (Loop Break): Deve sair do loop de colisão se não houver colisão")
        void shouldBreakCollisionLoopWhenNoCollisionOccurs() {
            // Cenário: Uma criatura se move, mas não colide com nada
            Creature creature0 = simulation.getCreatures().get(0);
            Creature creature1 = simulation.getCreatures().get(1);
            creature0.setPosition(100);
            creature1.setPosition(500); // Posiciona longe para evitar colisão

            int initialCoins0 = creature0.getCoins();
            int initialCoins1 = creature1.getCoins();

            simulation.resolveTurnFor(creature0);

            // O loop de colisão foi pulado, e a lógica de roubo foi executada
            assertTrue(creature0.getCoins() > initialCoins0,
                "A criatura 0 deveria ter roubado moedas.");
            assertTrue(creature1.getCoins() < initialCoins1,
                "A criatura 1 deveria ter perdido moedas.");
        }

        @Test
        @DisplayName("Branch 4 (True Hit): Deve parar o turno se a criatura for absorvida")
        void shouldStopTurnIfCreatureIsAbsorbed() {
            // Cenário: Um cluster absorve a criatura que está se movendo.
            Creature creatureToMove = simulation.getCreatures().get(0);
            Creature creatureToAbsorbInto = simulation.getCreatures().get(1);
            Cluster cluster = new Cluster(creatureToAbsorbInto, new Creature(1,1));

            // Prepara a simulação com as entidades do teste
            simulation.getCreatures().clear();
            simulation.getCreatures().add(creatureToMove);
            simulation.getCreatures().add(cluster);

            int initialClusterCoins = cluster.getCoins();

            // Move a criatura para colidir com o cluster
            creatureToMove.setTargetPosition(cluster.getPosition());
            simulation.resolveTurnFor(creatureToMove);

            // A criatura foi removida
            assertFalse(simulation.getCreatures().contains(creatureToMove),
                "A criatura que se moveu deveria ter sido absorvida.");
            // O cluster não roubou moedas (a ação de roubo foi pulada porque wasAbsorbed se tornou true)
            // A única mudança nas moedas do cluster deve ser a adição das moedas da criatura absorvida
            assertEquals(initialClusterCoins + creatureToMove.getCoins(), cluster.getCoins(),
                "O cluster deveria ter apenas as moedas somadas, sem roubo adicional.");
        }

        @Test
        @DisplayName("Branch 5 (False Hit): Deve continuar o loop se a entidade for modificada, não substituída")
        void shouldContinueLoopWhenEntityIsModified() {
            // Cenário: Um cluster se move, absorve uma criatura, e o loop continua para verificar novas colisões
            // Isso cobre o "false hit" para if (result.newEntity().isPresent())

            Cluster cluster = new Cluster(simulation.getCreatures().get(0), simulation.getCreatures().get(1));
            Creature creatureToAbsorb = new Creature(1, 101);

            simulation.getCreatures().clear();
            simulation.getCreatures().add(cluster);
            simulation.getCreatures().add(creatureToAbsorb);

            // Move o cluster para colidir com a criatura.
            cluster.setTargetPosition(creatureToAbsorb.getPosition());
            simulation.resolveTurnFor(cluster);

            // A criatura foi absorvida
            assertFalse(simulation.getCreatures().contains(creatureToAbsorb));
            // O cluster, agora maior, roubou moedas (provando que o turno continuou após a absorção)
            // Como agora só há o cluster e o guardião, ele não tem de quem roubar, então as moedas permanecem as mesmas
            // O teste aqui é que o código executou sem erros, provando que o loop lidou com a modificação
            assertEquals(1, simulation.getCreatures().stream().filter(
                c -> c instanceof Cluster).count());
        }

        @Test
        @DisplayName("Branch 6 (False Hit): Não deve roubar moedas se o ator do turno for o guardião")
        void shouldNotStealWhenActorIsGuardian() {
            // Cenário: O guardião se move, não colide, e não deve tentar roubar
            Guardian guardian = (Guardian) simulation.getCreatures().stream().filter(
                g -> g instanceof Guardian).findFirst().orElseThrow();
            int initialGuardianCoins = guardian.getCoins();

            simulation.resolveTurnFor(guardian);

            // As moedas do guardião não mudaram
            assertEquals(initialGuardianCoins, guardian.getCoins(), "O guardião não deveria roubar moedas.");
        }
    }

    @Nested
    @DisplayName("Testes MC/DC para handleCollisionsFor()")
    class HandleCollisionsMCDCTests {

        private Creature creature0, creature1;
        private Guardian guardian;

        @BeforeEach
        void setupEntities() {
            // Setup inicial com entidades comuns para os testes.
            Creature.resetIdCounter();
            simulation = new Simulation(2, 100, 2000,
                10, (min, max) -> 0.5);
            creature0 = simulation.getCreatures().get(0);
            creature1 = simulation.getCreatures().get(1);
            guardian = (Guardian) simulation.getCreatures().get(2);
        }

        @Test
        @DisplayName("Branch 1: Deve executar o roubo, mas não colisões, quando as criaturas estão distantes")
        void shouldExecuteStealButNoCollisionWhenCreaturesAreApart() {
            creature0.setPosition(100);
            creature1.setPosition(500);
            int initialCreatureCount = simulation.getCreatures().size();

            simulation.resolveTurnFor(creature0);

            assertEquals(initialCreatureCount, simulation.getCreatures().size(),
                "O número de criaturas não deve mudar quando não há colisão.");
            assertTrue(creature0.getCoins() > 1_000_000,
                "A criatura 0 deveria ter roubado moedas.");
            assertTrue(creature1.getCoins() < 1_000_000,
                "A criatura 1 deveria ter perdido moedas.");
        }

        @Test
        @DisplayName("Branch 2.1: Guardião (em movimento) deve absorver Cluster")
        void shouldReturnEntityAbsorbedWhenGuardianCollidesWithCluster() {
            Cluster cluster = new Cluster(creature0, creature1);
            simulation.getCreatures().clear();
            simulation.getCreatures().add(guardian);
            simulation.getCreatures().add(cluster);

            guardian.setTargetPosition(cluster.getPosition());
            simulation.resolveTurnFor(guardian);

            assertEquals(1, simulation.getCreatures().size(), "Apenas o guardião deve restar.");
            assertInstanceOf(Guardian.class, simulation.getCreatures().getFirst());
            assertEquals(2_000_000, guardian.getCoins());
        }

        @Test
        @DisplayName("Branch 2.2: Guardião (parado) deve absorver Cluster (em movimento)")
        void shouldReturnEntityAbsorbedWhenClusterCollidesWithGuardian() {
            Cluster cluster = new Cluster(creature0, creature1);
            simulation.getCreatures().clear();
            simulation.getCreatures().add(guardian);
            simulation.getCreatures().add(cluster);

            cluster.setTargetPosition(guardian.getPosition());
            simulation.resolveTurnFor(cluster);

            assertEquals(1, simulation.getCreatures().size(), "Apenas o guardião deve restar.");
            assertInstanceOf(Guardian.class, simulation.getCreatures().getFirst());
            assertEquals(2_000_000, guardian.getCoins());
        }

        @Test
        @DisplayName("Branch 3: Cluster (parado) deve absorver Creature (em movimento)")
        void shouldReturnEntityAbsorbedWhenCreatureCollidesWithCluster() {
            Cluster cluster = new Cluster(creature0, new Creature(1, 1));
            simulation.getCreatures().clear();
            simulation.getCreatures().add(cluster);
            simulation.getCreatures().add(creature1);

            creature1.setTargetPosition(cluster.getPosition());
            simulation.resolveTurnFor(creature1);

            assertEquals(1, simulation.getCreatures().size(), "Apenas o cluster deve restar.");
            assertEquals(3, ((Cluster) simulation.getCreatures().getFirst()).getMembers().size());
        }

        @Test
        @DisplayName("Branch 4: Cluster (em movimento) deve absorver Creature (parada)")
        void shouldAbsorbCreatureWhenMovingClusterCollides() {
            Cluster cluster = new Cluster(creature0, new Creature(1, 1));
            simulation.getCreatures().clear();
            simulation.getCreatures().add(cluster);
            simulation.getCreatures().add(creature1);

            cluster.setTargetPosition(creature1.getPosition());
            simulation.resolveTurnFor(cluster);

            assertEquals(1, simulation.getCreatures().size(), "Apenas o cluster deve restar.");
            assertEquals(3, ((Cluster) simulation.getCreatures().getFirst()).getMembers().size());
        }

        @Test
        @DisplayName("Branch 5: Duas Creatures devem formar um novo Cluster")
        void shouldReturnNewEntityWhenTwoCreaturesCollide() {
            creature0.setPosition(100);
            creature1.setPosition(150);
            guardian.setPosition(5000);

            creature0.setTargetPosition(creature1.getPosition());
            simulation.resolveTurnFor(creature0);

            assertEquals(2, simulation.getCreatures().size(),
                "Deveria restar um cluster e um guardião.");
            Optional<Creature> clusterOpt = simulation.getCreatures().stream().filter(
                c -> c instanceof Cluster).findFirst();
            assertTrue(clusterOpt.isPresent(), "Um novo cluster deveria ter sido criado.");
            assertEquals(2_000_000, clusterOpt.get().getCoins());
        }

        @Test
        @DisplayName("Branch 5 (False Hit on second condition): Deve ignorar colisão entre Creature e Guardian")
        void shouldNotFormClusterWhenCreatureHitsGuardian() {
            // Este teste cobre o "false hit" para '!(otherCreature instanceof Guardian)'
            // Cenário: A primeira parte da condição '!(movedCreature instanceof Guardian)' é verdadeira,
            // mas a segunda é falsa

            // Posiciona a criatura e o guardião perto um do outro
            creature0.setPosition(100);
            guardian.setPosition(150);
            // Coloca a outra criatura bem longe para garantir que ela não interfira
            creature1.setPosition(5000);

            // Move a criatura 0 para a posição exata do guardião, forçando a colisão.
            creature0.setTargetPosition(guardian.getPosition());
            simulation.resolveTurnFor(creature0);

            // O número de criaturas não deve mudar, pois a colisão não tem efeito.
            assertEquals(3, simulation.getCreatures().size(),
                "Nenhuma criatura deveria ter sido removida ou criada.");

            // Um cluster não deve ter sido formado
            long clusterCount = simulation.getCreatures().stream().filter(c -> c instanceof Cluster).count();
            assertEquals(0, clusterCount,
                "A colisão entre uma criatura e um guardião não deve formar um cluster.");
        }

        @Test
        @DisplayName("Branch 5 (False Hit on activeIndex check): " +
            "Deve adicionar cluster ao final se a criatura ativa não for encontrada")
        void shouldAddClusterToEndWhenMovedCreatureIsNotFound() {
            // Este teste cobre os false hits para 'if (activeIndex != -1)' e o true hit para o 'else' block
            // Cenário: Uma criatura que não está mais na lista principal (um caso de borda) causa uma colisão
            creature0.setPosition(100);
            creature1.setPosition(100);

            // Força a condição removendo a criatura que vai se mover ANTES de resolver o turno dela
            simulation.getCreatures().remove(creature0);

            // Resolve o turno para a criatura que já foi removida
            simulation.resolveTurnFor(creature0);

            // A outra criatura (creature1) foi removida
            assertFalse(simulation.getCreatures().contains(creature1),
                "A outra criatura deveria ter sido removida para formar o cluster.");

            // Um novo cluster foi adicionado ao final da lista
            assertInstanceOf(Cluster.class, simulation.getCreatures().getLast(),
                "Um novo cluster deveria ter sido adicionado ao final da lista.");

            // O número total de criaturas deve ser 2 (o guardião + o novo cluster)
            assertEquals(2, simulation.getCreatures().size());
        }

        @Test
        @DisplayName("Branch 6: Deve retornar noCollision para Guardião vs Creature")
        void shouldReturnNoCollisionForGuardianVsCreature() {
            guardian.setTargetPosition(creature0.getPosition());
            simulation.resolveTurnFor(guardian);

            assertEquals(3, simulation.getCreatures().size(),
                "O número de criaturas não deve mudar em uma colisão sem efeito.");
        }
    }

    @Nested
    @DisplayName("Testes MC/DC para peekNextCreatureInTurn()")
    class PeekNextCreatureMCDCTests {

        @BeforeEach
        void setup() {
            // Usa uma simulação com 2 criaturas para a maioria dos testes
            Creature.resetIdCounter();
            simulation = new Simulation(2, 100,
                2000, 10, (min, max) -> 0.5);
        }

        @Test
        @DisplayName("Branch 1 (True): Deve retornar vazio se a simulação já terminou")
        void shouldReturnEmptyIfSimulationIsFinished() {
            // Condição: isFinished = true
            simulation.isFinished = true;
            Optional<Creature> result = simulation.peekNextCreatureInTurn();
            assertTrue(result.isEmpty(), "Não deve espiar o próximo turno se o jogo já acabou.");
        }

        @Test
        @DisplayName("Branch 2 (True): Deve retornar a primeira criatura se o round acabou")
        void shouldReturnFirstCreatureIfRoundIsOver() {
            // Condição: guardianHasActedInRound = true
            // Força o estado para o final de um round
            simulation.processNextCreatureInTurn(); // Turno do C0
            simulation.processNextCreatureInTurn(); // Turno do C1
            simulation.processNextCreatureInTurn(); // Turno do Guardião (agora guardianHasActedInRound é true)

            Optional<Creature> result = simulation.peekNextCreatureInTurn();

            assertTrue(result.isPresent(), "Deveria encontrar a primeira criatura do próximo round.");
            assertEquals(0, result.get().getId(), "Deveria ser a criatura com ID 0.");
        }

        @Test
        @DisplayName("Branch 3 (True in While): Deve retornar a próxima criatura no meio do round")
        void shouldReturnNextCreatureMidRound() {
            // Condição: O loop 'while' encontra uma criatura que não é guardião
            simulation.processNextCreatureInTurn(); // Simula o turno da criatura 0

            Optional<Creature> result = simulation.peekNextCreatureInTurn();

            assertTrue(result.isPresent(), "Deveria encontrar a próxima criatura na fila.");
            assertEquals(1, result.get().getId(), "A próxima criatura a agir deveria ser a de ID 1.");
        }

        @Test
        @DisplayName("Branch 4 (Fall-through): Deve retornar o guardião se for o próximo")
        void shouldReturnGuardianIfItIsNext() {
            // Condição: O loop 'while' termina (não encontra mais criaturas normais),
            // então o stream final deve encontrar o guardião
            simulation.processNextCreatureInTurn(); // Turno do C0
            simulation.processNextCreatureInTurn(); // Turno do C1

            Optional<Creature> result = simulation.peekNextCreatureInTurn();

            assertTrue(result.isPresent(), "Deveria encontrar o guardião como o próximo a agir.");
            assertInstanceOf(Guardian.class, result.get());
        }
    }

    @Nested
    @DisplayName("Testes MC/DC para checkEndCondition()")
    class CheckEndConditionMCDCTests {

        private Creature creature0;
        private Creature creature1;
        private Guardian guardian;

        @BeforeEach
        void setupEntities() {
            Creature.resetIdCounter();
            simulation = new Simulation(2, 100, 1000,
                10, (min, max) -> 0.5);
            creature0 = simulation.getCreatures().get(0);
            creature1 = simulation.getCreatures().get(1);
            guardian = (Guardian) simulation.getCreatures().get(2);
        }

        @Test
        @DisplayName("Branch 1 (True): Deve finalizar com sucesso se apenas o guardião restar")
        void shouldSucceedWhenOnlyGuardianRemains() {
            // Condição: creatures.size() == 1 && creature é Guardião
            simulation.getCreatures().removeIf(c -> !(c instanceof Guardian));
            assertEquals(1, simulation.getCreatures().size());

            simulation.resolveTurnFor(guardian); // Chama checkEndCondition internamente

            assertTrue(simulation.isFinished());
            assertTrue(simulation.isSuccessful());
            assertEquals("SUCCESS! Only the guardian remains.", simulation.getFinalMessage());
        }

        @Test
        @DisplayName("Branch 2 (True): Deve finalizar com sucesso se Guardião tiver mais moedas")
        void shouldSucceedWhenGuardianHasMoreCoins() {
            // Condição: creatures.size() == 2, tem Guardião, tem Creature, e Guardião tem mais moedas.
            simulation.getCreatures().remove(1); // Remove uma criatura para sobrar apenas 2
            guardian.setCoins(1000);
            creature0.setCoins(500);

            simulation.resolveTurnFor(creature0);

            assertTrue(simulation.isFinished());
            assertTrue(simulation.isSuccessful());
            assertEquals("SUCCESS! The guardian has more coins than the final creature.",
                simulation.getFinalMessage());
        }

        @Test
        @DisplayName("Branch 3 (False in last condition): Deve finalizar com derrota se a Criatura tiver mais moedas")
        void shouldFailWhenCreatureHasMoreCoins() {
            // Condição: creatures.size() == 2, tem Guardião, tem Creature, mas Guardião tem menos moedas.
            simulation.getCreatures().remove(1);
            guardian.setCoins(500);
            creature0.setCoins(1000);

            simulation.resolveTurnFor(creature0);

            assertTrue(simulation.isFinished());
            assertFalse(simulation.isSuccessful());
            assertEquals("DEFEAT! The final creature has more coins than the guardian.",
                simulation.getFinalMessage());
        }

        @Test
        @DisplayName("Branch 4 (False in class check): Não deve finalizar se a outra entidade for um Cluster")
        void shouldNotEndIfRemainingEntityIsACluster() {
            // Condição: creatures.size() == 2, mas a outra entidade não é 'Creature.class'

            // Posições controladas para evitar colisão
            Cluster cluster = new Cluster(creature0, simulation.getCreatures().get(1));
            simulation.getCreatures().clear();
            simulation.getCreatures().add(guardian);
            simulation.getCreatures().add(cluster);

            // Coloca o guardião e o cluster bem distante um do outro
            guardian.setPosition(0);
            cluster.setPosition(5000);

            // Move o cluster para um lugar onde ele não colidirá com o guardião.
            cluster.setTargetPosition(6000);
            simulation.resolveTurnFor(cluster);

            assertFalse(simulation.isFinished(),
                "A simulação não deve terminar se a outra entidade for um cluster.");
            assertEquals(2, simulation.getCreatures().size(),
                "O número de criaturas deve permanecer 2.");
        }

        @Test
        @DisplayName("Branch 5 (Fall-through): Não deve finalizar se houver mais de 2 criaturas")
        void shouldNotEndWhenMoreThanTwoCreaturesRemain() {
            // Condição: Nenhuma das condições de finalização é atendida

            // Posições controladas para evitar colisão
            // Coloca todas as 3 criaturas distantes umas das outras
            creature0.setPosition(100);
            creature1.setPosition(1000);
            guardian.setPosition(5000);

            // Move a criatura 0 para um lugar seguro onde ela não colidirá com ninguém
            creature0.setTargetPosition(200);
            simulation.resolveTurnFor(creature0);

            assertFalse(simulation.isFinished(),
                "A simulação não deve terminar quando há 3 ou mais criaturas e nenhuma colisão fatal.");
            assertEquals(3, simulation.getCreatures().size(),
                "O número de criaturas deve permanecer 3.");
        }

        @Test
        @DisplayName("Branch 6 (False in guardian check): " +
            "Não deve finalizar se não houver guardião na lista de 2 criaturas")
        void shouldNotEndWhenGuardianIsMissingWithTwoCreatures() {
            // Este teste cobre o "false hit" para 'if (guardian != null)'
            // Cenário: A lista de criaturas tem tamanho 2, mas nenhuma delas é um Guardião

            // Cria duas criaturas normais e remove o guardião
            simulation.getCreatures().removeIf(c -> c instanceof Guardian);
            assertEquals(2, simulation.getCreatures().size());

            // Executa o turno de uma das criaturas.
            simulation.resolveTurnFor(creature0);

            // A simulação não deve ter sido finalizada, pois a condição 'guardian != null' falhou
            assertFalse(simulation.isFinished(),
                "A simulação não deve terminar se não houver guardião, mesmo com 2 criaturas.");
        }
    }

    @Nested
    @DisplayName("Testes para cobrir getters e setters")
    class GettersAndSettersTests {

        private Simulation simulation;

        @BeforeEach
        void setup() {
            // Cria uma nova instância de simulação para cada teste
            simulation = new Simulation(2, 100, 1000,
                10, (min, max) -> 0.5);
        }

        @Test
        @DisplayName("Deve definir e obter o número de iterações corretamente")
        void shouldSetAndGetIterations() {
            simulation.setIterations(5);
            assertEquals(5, simulation.getIterations(),
                "getIterations should return the value set by setIterations.");
        }

        @Test
        @DisplayName("Deve definir e obter o status de sucesso corretamente")
        void shouldSetAndGetSuccessfulStatus() {
            simulation.setSuccessful(true);
            assertTrue(simulation.isSuccessful(),
                "isSuccessful should return true after being set to true.");

            simulation.setSuccessful(false);
            assertFalse(simulation.isSuccessful(),
                "isSuccessful should return false after being set to false.");
        }

        @Test
        @DisplayName("Deve definir e obter o usuário corretamente")
        void shouldSetAndGetUser() {
            User testUser = new User(99L, "testuser");

            simulation.setUser(testUser);

            assertEquals(testUser, simulation.getUser(),
                "getUser should return the User object that was set.");
            assertEquals("testuser", simulation.getUser().getUsername());
        }

        @Test
        @DisplayName("Deve definir e obter o nome da simulação corretamente")
        void shouldSetAndGetName() {
            String testName = "My Test Simulation";
            simulation.setName(testName);
            assertEquals(testName, simulation.getName(), "getName should return the value set by setName.");
        }

        @Test
        @DisplayName("Deve definir e obter o ID da simulação corretamente")
        void shouldSetAndGetId() {
            Long testId = 123L;
            simulation.setId(testId);
            assertEquals(testId, simulation.getId(), "getId should return the value set by setId.");
        }

        @Test
        @DisplayName("Deve definir e obter a data de criação corretamente")
        void shouldSetAndGetCreatedAt() {
            LocalDateTime testDateTime = LocalDateTime.of(2024, 1, 1, 12, 0);
            simulation.setCreatedAt(testDateTime);
            assertEquals(testDateTime, simulation.getCreatedAt(),
                "getCreatedAt should return the value set by setCreatedAt.");
        }

        @Test
        @DisplayName("Deve obter o número inicial de criaturas corretamente")
        void shouldGetInitialNumCreatures() {
            // O valor é definido no construtor
            int expectedInitialCreatures = 2;
            assertEquals(expectedInitialCreatures, simulation.getInitialNumCreatures(),
                "getInitialNumCreatures should return the value set in the constructor.");
        }
    }

    static Stream<Arguments> invalidConstructorArgumentsProvider() {
        return Stream.of(
            Arguments.of(0, 100, 1000, 10, "Number of creatures must be greater than 1."),
            Arguments.of(1, 100, 1000, 10, "Number of creatures must be greater than 1."),
            Arguments.of(10, 100, 1000, 0, "Max iterations must be positive."),
            Arguments.of(10, 100, 1000, -1, "Max iterations must be positive."),
            Arguments.of(2, 0, 1000, 10, "Creature width must be positive."),
            Arguments.of(2, 100, -1, 10, "Horizon width must be positive.")
        );
    }
}
