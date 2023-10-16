# An ETL / Data Collector for test and other data

## Usage

This project is a model template and not ready for production. Expect things not to run as you would normally expect!

This project uses [Docker](https://www.docker.com/) and [sbt / Scala 3](https://www.scala-lang.org/download/). Please 
refer to the documentation for each to get the environment setup.

## Quickstart

To get started, clone the repository and go to the root folder.

```
git clone https://github.com/professor-greebie/test-compile
cd test-compile
```

Run the docker compose file to include the associated Kafka brokers and a copy of PostgreSQL. (Exclude the -d flag 
if you wish to see the associated logs in real time).

```
docker compose up -d
```

Then you can run the project:

```
sbt compile
sbt run
```




