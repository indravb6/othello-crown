# Game Service

[![pipeline status](https://gitlab.com/AdvProg-C-10-OthelloCrown/game-service/badges/master/pipeline.svg)](https://gitlab.com/AdvProg-C-10-OthelloCrown/game-service/commits/master)
[![coverage report](https://gitlab.com/AdvProg-C-10-OthelloCrown/game-service/badges/master/coverage.svg)](https://gitlab.com/AdvProg-C-10-OthelloCrown/game-service/commits/master)

### Liver Server:
https://othello-crown-game-service.herokuapp.com/

### Requirement :

- Java 8
- IntelliJ (recommendation)

### Preparing local working directory :

1.  Clone this repository
2.  Go inside the repository directory (`game-service`)
3.  Copy the example config:  
    `cp src/main/resources/application.properties.example src/main/resources/application.properties`  
    `cp src/main/resources/application-test.properties.example src/main/resources/application-test.properties`

5.  Run:  
    `gradlew test`  
    Make sure the tests pass before moving on to the next sections.

#### Running the server locally : `gradlew bootRun`
