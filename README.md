SI Camunda Workers

Projeto desenvolvido no âmbito da unidade curricular de Sistemas de Informação.

Português
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

CamundaWorker/

pom.xml

src/main/java/org/example/

Main.java

GerarEtiquetas.java

RelatorioDesperdicio.java

StockManager.java

src/main/resources/

BPMN_LEI_SI2526_N2.bpmn

stock_alimentos.json

src/test/

⚙ Funcionalidades
Gestão de Stock

Leitura de dados de stock

Atualização de quantidades

Verificação de disponibilidade

Geração de Etiquetas

Criação automática de etiquetas

Integração com variáveis do processo

Relatórios de Desperdício

Cálculo de desperdício alimentar

Atualização de variáveis no workflow

🛠 Tecnologias Utilizadas

Java

Maven

Camunda BPM (BPMN 2.0)

JSON

🚀 Execução
Requisitos

Java 17 ou superior

Maven

Camunda Engine em execução

Compilar
mvn clean install

Executar
mvn exec:java


Ou executar:

org.example.Main

English
📌 Introduction

The SI Camunda Workers project implements External Task Workers in Java, integrated with a BPMN 2.0 process using Camunda.

The system supports:

Food stock management

Automatic label generation

Waste report production

Workflow integration using the External Task pattern

This repository contains:

Java worker implementation

BPMN process file

JSON stock data file

📂 Folder Structure

CamundaWorker/

pom.xml

src/main/java/org/example/

src/main/resources/

src/test/

⚙ Features
Stock Management

Read and update stock data

Check product availability

Label Generation

Automatically generate product labels

Waste Reports

Calculate food waste

Update workflow variables

🛠 Technologies

Java

Maven

Camunda BPM
