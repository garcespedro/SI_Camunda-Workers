📌 Descrição

O projeto SI_Camunda-Workers consiste na implementação de workers em Java para execução de tarefas externas associadas a um processo BPMN modelado na plataforma Camunda.

O sistema permite gerir operações relacionadas com stock de alimentos, geração de etiquetas e produção de relatórios de desperdício, integrando-se com um motor de workflow através do padrão External Task Worker.

🛠 Tecnologias Utilizadas

Java

Maven

Camunda BPM (BPMN 2.0)

JSON para armazenamento de dados

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

⚙️ Funcionalidades
✔ Gestão de Stock

Leitura e atualização de dados de stock

Verificação de disponibilidade de produtos

Integração com ficheiro JSON de persistência

✔ Geração de Etiquetas

Criação automática de etiquetas para produtos processados

Integração com variáveis do processo BPMN

✔ Relatório de Desperdício

Cálculo e geração de relatórios relacionados com desperdício alimentar

Atualização de dados no fluxo do processo

🚀 Como Executar
1️⃣ Pré-requisitos

Java 17 ou superior

Maven instalado

Motor Camunda em execução (local ou remoto)

Verificar versões instaladas:

java -version
mvn -version

2️⃣ Compilar o Projeto

Na pasta CamundaWorker, executar:

mvn clean install

3️⃣ Executar os Workers
mvn exec:java


Ou executar diretamente a classe principal:

org.example.Main

🔄 Funcionamento

O processo BPMN é iniciado na Camunda.

Quando uma Service Task é atingida, o worker correspondente é acionado.

O worker:

Obtém as variáveis do processo

Executa a lógica associada

Atualiza variáveis

Conclui a tarefa no motor de workflow

🧪 Testes

Os testes encontram-se na pasta:

src/test/java/


Para executar:

mvn test

📎 Processo BPMN

O ficheiro do processo encontra-se em:

src/main/resources/BPMN_LEI_SI2526_N2.bpmn


Pode ser aberto e editado através do Camunda Modeler ou outra ferramenta compatível com BPMN 2.0.

👨‍💻 Contexto Académico

Projeto desenvolvido no âmbito da unidade curricular de Sistemas de Informação, com foco na integração entre modelação de processos de negócio (BPMN) e implementação de workers externos em Java.
