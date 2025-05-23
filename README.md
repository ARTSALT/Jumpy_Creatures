# Simulação de Criaturas Saltitantes

Um projeto de simulação desktop desenvolvido em Java utilizando o framework libGDX, que modela o comportamento de criaturas que interagem entre si em um ambiente virtual.

## Descrição do Projeto

Esta simulação consiste em um sistema onde múltiplas criaturas interagem em um espaço unidimensional (linha do horizonte), competindo por moedas de ouro.  
Nossas criaturas são zumbis, e a cada iteração do ciclo de simulação, eles saltam proporcionalmente à sua quantidade de moedas atual e roubam metade das moedas do outro zumbi mais próximo.  
As principais características são:

- N zumbis, definido pelo usuário
- Cada zumbi possui:
    - Uma quantidade de moedas de ouro (gi) inicialmente definida como 1.000.000
    - Uma posição no horizonte (xi) representada como um número de ponto flutuante de dupla precisão

## Requisitos do Sistema

- Java JDK 11 ou superior
- Gradle (para construção do projeto)
- libGDX (já incluído como dependência)
- IntelliJ IDEA (cobre os requisitos anteriores)

## Como Executar

1. Clone o repositório:
   ```bash
   git clone https://github.com/ARTSALT/Jumpy_Creatures.git
   ```

2. No IntelliJ IDEA, rode o projeto em `Gradle -> Jumpy_Creatures -> Tasks -> application -> run` ou execute o seguinte comando no terminal (pressione `Ctrl` duas vezes para abrir o terminal):
   ```bash
   gradle lwjgl3:run
   ```

## Estrutura do Projeto

```
Jumpy_Creatures
├── assets/                             # Recursos do jogo (imagens, sons, etc.)
├── core/                               # Código principal do jogo (lógica da simulação)
│   ├── .../com.badlogic/drop/          # Pacote principal com as classes Main (gerencia interface) e Simulation (gerencia a simulação)
│   ├── .../com.badlogic/drop/entity/   # Pacote da entidade Zombie
│   ├── .../tests/                      # Pacote de testes (a classe de foco dos testes é a classe Simulation)
├── lwjgl3/                             # Configuração do LWJGL (Lightweight Java Game Library)
├── build.gradle                        # Configuração do Gradle
└── ...
```

## Parâmetros da simulação

Você pode ajustar os seguintes parâmetros ao rodar a simulação:

- Número de criaturas, defina o número de criaturas na caixa de texto e pressione o botão de play ou pressione `Enter` para iniciar a simulação.
- É possível selecionar um zumbi clicando nele, o que fará com que ele fique em destaque e mostre sua quantidade de moedas.
- A simulação não tem um critério de parada, então você pode parar a simulação iniciando uma nova simulação ou pressionando `Esc`.

# Licença

Este projeto é licenciado sob a Licença MIT. Veja o arquivo [LICENSE](LICENSE) para mais detalhes.
