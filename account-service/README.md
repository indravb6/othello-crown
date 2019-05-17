# Account Service

[![pipeline status](https://gitlab.com/AdvProg-C-10-OthelloCrown/account-service/badges/master/pipeline.svg)](https://gitlab.com/AdvProg-C-10-OthelloCrown/account-service/commits/master)
[![coverage report](https://gitlab.com/AdvProg-C-10-OthelloCrown/account-service/badges/master/coverage.svg)](https://gitlab.com/AdvProg-C-10-OthelloCrown/account-service/commits/master)

### Requirement :

- Java 8
- PostgreSQL
- IntelliJ (recommendation)

### Preparing local working directory :

1.  Clone this repository
2.  Go inside the repository directory (`account-service`)
3.  Copy the example config:  
    `cp src/main/resources/application.properties.example src/main/resources/application.properties`  
    `cp src/main/resources/application-test.properties.example src/main/resources/application-test.properties`
4.  Preparing local database :

    1.  Create new database called `othellocrown_account` and `othellocrown_account_test`
    2.  Open the config file `src/main/resources/application.properties` and `src/main/resources/application-test.properties`
    3.  modify datasource `url`, `username` and `password` as necessary

5.  Run:  
    `gradlew test`  
    Make sure the tests pass before moving on to the next sections.

#### Running the server locally : `gradlew bootRun`
