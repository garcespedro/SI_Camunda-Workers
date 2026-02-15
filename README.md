Projeto desenvolvido no âmbito da unidade curricular de Sistemas de Informação.

🇵🇹 Português
📌 Descrição

O projeto SI Camunda Workers consiste na implementação de External Task Workers em Java, integrados com um processo modelado em BPMN 2.0 na plataforma Camunda.

O sistema permite:

Gestão de stock de alimentos

Geração automática de etiquetas

Produção de relatórios de desperdício

Integração com motor de workflow através do padrão External Task Worker

Este repositório inclui:

Implementação dos workers em Java

Ficheiro do processo BPMN

Ficheiro JSON com dados de stock

📂 Estrutura do Projeto
SI_Camunda-Workers/
└── CamundaWorker/
    ├── pom.xml
    └── src/
        ├── main/
        │   ├── java/org/example/
        │   │   ├── Main.java
        │   │   ├── GerarEtiquetas.java
        │   │   ├── RelatorioDesperdicio.java
        │   │   └── StockManager.java
        │   └── resources/
        │       ├── BPMN_LEI_SI2526_N2.bpmn
        │       └── stock_alimentos.json
        └── test/

⚙ Funcionalidades
✔ Gestão de Stock

Leitura e atualização de dados

Verificação de disponibilidade

Persistência em ficheiro JSON

✔ Geração de Etiquetas

Criação automática de etiquetas

Integração com variáveis do processo BPMN

✔ Relatórios de Desperdício

Cálculo de desperdício alimentar

Atualização de variáveis no workflow

🚀 Como Executar
Pré-requisitos

Java 17+

Maven

Camunda Engine em execução

Verificar versões:

java -version
mvn -version

Compilar
mvn clean install

Executar
mvn exec:java


Ou executar a classe:

org.example.Main

🇬🇧 English
📌 Description

The SI Camunda Workers project consists of the implementation of External Task Workers in Java, integrated with a BPMN 2.0 process using Camunda.

The system provides:

Food stock management

Automatic label generation

Waste report production

Workflow integration using the External Task pattern

This repository includes:

Java worker implementation

BPMN process model

JSON stock data file

⚙ Features
✔ Stock Management

Read and update stock data

Check product availability

JSON-based persistence

✔ Label Generation

Automatically generate product labels

BPMN variable integration

✔ Waste Reports

Calculate food waste

Update workflow process variables

🛠 Technologies

Java

Maven

Camunda BPM (BPMN 2.0)

JSON

🔄 Workflow Integration

The BPMN process is deployed in Camunda.

When a Service Task is reached, the corresponding worker subscribes to the topic.

The worker:

Retrieves process variables

Executes business logic

Updates variables

Completes the task

📎 BPMN Model
src/main/resources/BPMN_LEI_SI2526_N2.bpmn


Can be opened using Camunda Modeler.

👨‍💻 Academic Context

Developed as part of an academic assignment in the Information Systems course, focusing on the integration between business process modeling and Java-based worker implementation.
