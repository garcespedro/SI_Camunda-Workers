# SI Camunda Workers

## English

### 📌 Introduction
This project was developed as part of the Information Systems course. It consists of implementing **External Task Workers in Java**, integrated with a **BPMN 2.0 process** using Camunda.

The system manages food stock operations, automatic label generation, and waste report production.  
This repository includes the Java implementation of the workers, the BPMN process file, and the JSON file containing stock data.

---

### 📁 Folder Structure

- **CamundaWorker**: main project folder  
- **pom.xml**: Maven configuration file  
- **Main.java**: application entry point  
- **GerarEtiquetas.java**: worker responsible for label generation  
- **RelatorioDesperdicio.java**: worker responsible for waste reports  
- **StockManager.java**: stock management logic  
- **BPMN_LEI_SI2526_N2.bpmn**: BPMN process model  
- **stock_alimentos.json**: JSON file with stock data  
- **test**: unit tests  

---

### 🧰 Tools
- **Java**: programming language used to implement the workers  
- **Maven**: project build and dependency management tool  
- **Camunda BPM**: workflow engine used to execute the BPMN process  
- **JSON**: used for stock data storage  

---

## Português

### 📌 Introdução
Este projeto foi desenvolvido no âmbito da unidade curricular de Sistemas de Informação. Consiste na implementação de **External Task Workers em Java**, integrados com um processo modelado em **BPMN 2.0** na plataforma Camunda.

O sistema permite gerir operações de stock de alimentos, gerar etiquetas automaticamente e produzir relatórios de desperdício.  
Neste repositório encontra-se a implementação dos workers em Java, o ficheiro do processo BPMN e o ficheiro JSON com os dados de stock.

---

### 📁 Estrutura de Pastas

- **CamundaWorker**: pasta principal do projeto  
- **pom.xml**: ficheiro de configuração Maven  
- **Main.java**: ponto de entrada da aplicação  
- **GerarEtiquetas.java**: worker responsável pela geração de etiquetas  
- **RelatorioDesperdicio.java**: worker responsável pelos relatórios de desperdício  
- **StockManager.java**: lógica de gestão de stock  
- **BPMN_LEI_SI2526_N2.bpmn**: modelo do processo BPMN  
- **stock_alimentos.json**: ficheiro JSON com dados de stock  
- **test**: testes unitários  

---

### 🧰 Ferramentas
- **Java**: linguagem utilizada para implementar os workers  
- **Maven**: ferramenta de gestão de dependências e build  
- **Camunda BPM**: motor de workflow para execução do processo  
- **JSON**: utilizado para armazenamento de dados  

