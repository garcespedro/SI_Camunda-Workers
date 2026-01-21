/**
 * Trabalho realizado por:
 * Luís Garcês nº 8230235
 * João Lima nº 8230178
 * Gonçalo Barbosa nº 8230153
 */

package org.example;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.worker.JobWorker;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobHandler;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.client.impl.oauth.OAuthCredentialsProvider;
import io.camunda.zeebe.client.impl.oauth.OAuthCredentialsProviderBuilder;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Classe principal responsável por iniciar o cliente Zeebe e
 * registar todos os workers necessários para o processo de produção alimentar.
 *
 * <p>
 * Esta classe estabelece a ligação ao cluster Zeebe na Camunda Cloud,
 * configura as credenciais OAuth e cria os vários workers que tratam
 * das diferentes tarefas do processo (gerar etiquetas, registar desperdício,
 * verificar stock, emitir ordens de compra, etc.).
 * </p>
 */
public class    Main {

    /**
     * Ponto de entrada da aplicação.
     *
     * <p>
     * Este método:
     * <ol>
     *     <li>Configura as credenciais OAuth para ligação ao cluster Zeebe;</li>
     *     <li>Cria o cliente Zeebe com parâmetros de timeout e execução;</li>
     *     <li>Testa a ligação ao gateway Zeebe;</li>
     *     <li>Regista os workers definidos no método {@link #setupWorkers(ZeebeClient)};</li>
     *     <li>Mantém a thread principal ativa para que os workers continuem a correr.</li>
     * </ol>
     * </p>
     *
     * @param args argumentos da linha de comandos (não utilizados).
     */
    public static void main(String[] args) {

        String clusterId = "dfdb8d36-5bf6-4b20-be42-8205ce0805f0";
        String clientId = "3hNiVfFIHruoK3eGjxUtDLtIG0XL-dpO";
        String clientSecret = "3eNPjVql1lsU0vneNh7Nl3q3dLbpiYPZQ-L3tIYGWsYU8m-ySADIuXO0Ysf_y3QG";
        String region = "bru-2";

        String connectionString = clusterId + "." + region + ".zeebe.camunda.io";

        System.out.println("Iniciando Worker Zeebe...");

        // Configuração mais robusta para evitar timeouts
        OAuthCredentialsProvider credentialsProvider = new OAuthCredentialsProviderBuilder()
                .authorizationServerUrl("https://login.cloud.camunda.io/oauth/token")
                .audience(connectionString)
                .clientId(clientId)
                .clientSecret(clientSecret)
                .readTimeout(Duration.ofSeconds(10)) // Timeout menor
                .build();

        try (ZeebeClient client = ZeebeClient.newClientBuilder()
                .gatewayAddress(connectionString)
                .credentialsProvider(credentialsProvider)
                .numJobWorkerExecutionThreads(3)
                .defaultJobWorkerName("food-production-worker")
                .defaultJobTimeout(Duration.ofMinutes(1))
                .defaultJobWorkerMaxJobsActive(5)
                .build()) {

            System.out.println("Conectado ao Zeebe!");

            // Testar conexão rápida
            try {
                client.newTopologyRequest().send().join();
                System.out.println("Conexão testada com sucesso!");
            } catch (Exception e) {
                System.out.println("Aviso no teste: " + e.getMessage());
            }

            // Registrar workers
            setupWorkers(client);

            // Manter ativo
            Thread.currentThread().join();

        } catch (Exception e) {
            System.err.println("Erro: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Regista e configura todos os workers Zeebe utilizados pelo processo.
     *
     * <p>
     * São criados os seguintes workers (jobTypes):
     * <ul>
     *     <li><b>gerar_etiquetas</b>: gera etiquetas completas para produtos;</li>
     *     <li><b>registar_nao_consumiveis</b>: regista produtos/alimentos não consumíveis
     *         e gera o relatório de desperdício correspondente;</li>
     *     <li><b>verificar_alimentos</b>: verifica se existe stock suficiente no armazém
     *         para um conjunto de alimentos e quantidades;</li>
     *     <li><b>Emitir_Ordem_Compra</b>: emite uma ordem de compra para o fornecedor;</li>
     *     <li><b>Enviar_amostras</b>: simula o envio de amostras pelo fornecedor;</li>
     *     <li><b>Preparar_Encomenda</b>: simula a preparação de uma encomenda;</li>
     *     <li><b>Rejeitar_Proposta</b>: regista a rejeição de uma proposta de fornecedor.</li>
     * </ul>
     * Cada worker trata o respetivo tipo de job, lê as variáveis do processo,
     * executa a lógica de negócio necessária e completa o job com as variáveis de saída.
     * </p>
     *
     * @param client instância do {@link ZeebeClient} já ligada ao cluster,
     *               utilizada para criar e abrir os workers.
     */
    private static void setupWorkers(ZeebeClient client) {

        // 1. Worker: gerar_etiquetas
        JobWorker worker1 = client.newWorker()
                .jobType("gerar_etiquetas")
                .handler(new JobHandler() {
                    /**
                     * Trata o job do tipo {@code gerar_etiquetas}.
                     *
                     * <p>
                     * Lê as variáveis do job, chama {@link GerarEtiquetas#gerarEtiquetaCompleta(Map)}
                     * para gerar a etiqueta e, em seguida, completa o job com o resultado.
                     * Em caso de erro, completa o job com variáveis de erro identificando
                     * que a etiqueta não foi gerada.
                     * </p>
                     *
                     * @param jobClient cliente para envio de comandos relacionados com o job.
                     * @param job       job Zeebe atualmente ativado, contendo as variáveis do processo.
                     */
                    @Override
                    public void handle(JobClient jobClient, ActivatedJob job) {
                        System.out.println("=================[Gerar Etiquetas]==============================");

                        try {
                            Map<String, Object> vars = job.getVariablesAsMap();

                            Map<String, Object> resultado = GerarEtiquetas.gerarEtiquetaCompleta(vars);

                            jobClient.newCompleteCommand(job.getKey())
                                    .variables(resultado)
                                    .send()
                                    .join();

                            System.out.println("Etiqueta gerada: " + resultado.get("produtoId"));

                        } catch (Exception e) {
                            System.err.println(" Erro: " + e.getMessage());

                            Map<String, Object> erro = Map.of(
                                    "etiquetaGerada", false,
                                    "erro", e.getMessage()
                            );

                            jobClient.newCompleteCommand(job.getKey())
                                    .variables(erro)
                                    .send()
                                    .join();
                        }
                    }
                })
                .timeout(Duration.ofSeconds(60))
                .maxJobsActive(3)
                .open();

        // 2. Worker: registar_nao_consumiveis
        JobWorker worker2 = client.newWorker()
                .jobType("registar_nao_consumiveis")
                .handler(new JobHandler() {
                    /**
                     * Trata o job do tipo {@code registar_nao_consumiveis}.
                     *
                     * <p>
                     * Este handler:
                     * <ol>
                     *     <li>Lê e regista as variáveis recebidas (alimentos, quantidades, lote, etc.);</li>
                     *     <li>Prepara um mapa de variáveis para geração do relatório de desperdício;</li>
                     *     <li>Invoca {@link RelatorioDesperdicio#criarRelatorioCompleto(Map)} para
                     *     criar o relatório em ficheiro;</li>
                     *     <li>Completa o job com informação sobre o sucesso, caminho do ficheiro,
                     *     funcionário, lote, responsável e timestamp.</li>
                     * </ol>
                     * Em caso de erro, completa o job com variáveis que indicam falha no registo.
                     * </p>
                     *
                     * @param jobClient cliente para envio de comandos relacionados com o job.
                     * @param job       job Zeebe atualmente ativado, contendo as variáveis do processo.
                     */
                    @Override
                    public void handle(JobClient jobClient, ActivatedJob job) {
                        System.out.println("=================[Registar Nao Consumiveis]==============================");

                        try {
                            Map<String, Object> vars = job.getVariablesAsMap();

                            System.out.println("VARIÁVEIS RECEBIDAS:");
                            vars.forEach((key, value) ->
                                    System.out.println("   • " + key + " = " + value));

                            String alimentos = (String) vars.getOrDefault("alimentos", "");
                            String quantidades = (String) vars.getOrDefault("quantidades", "");
                            String lote = (String) vars.getOrDefault("lote_produto", "LOTE-NÃO-INFORMADO");
                            String responsavelCozedura = (String) vars.getOrDefault("responsavel_cozedura", "Não informado");
                            String funcionario = (String) vars.getOrDefault("nome_funcionario", "Anónimo");
                            String motivo = (String) vars.getOrDefault("motivo", "qualidade_insuficiente");

                            String descPreparacao = (String) vars.getOrDefault("descricao_preparacao", "N/A");
                            String descProcedimento = (String) vars.getOrDefault("descricao_procedimento", "N/A");
                            String equipamentos = (String) vars.getOrDefault("equipamentos", "N/A");

                            Map<String, Object> variaveisParaRelatorio = new HashMap<>();
                            variaveisParaRelatorio.put("alimentos", alimentos);
                            variaveisParaRelatorio.put("quantidades", quantidades);
                            variaveisParaRelatorio.put("lote_produto", lote);
                            variaveisParaRelatorio.put("responsavel_cozedura", responsavelCozedura);
                            variaveisParaRelatorio.put("nome_funcionario", funcionario);
                            variaveisParaRelatorio.put("motivo", motivo);
                            variaveisParaRelatorio.put("descricao_preparacao", descPreparacao);
                            variaveisParaRelatorio.put("descricao_procedimento", descProcedimento);
                            variaveisParaRelatorio.put("equipamentos", equipamentos);

                            Map<String, Object> resultadoRelatorio =
                                    RelatorioDesperdicio.criarRelatorioCompleto(variaveisParaRelatorio);

                            Map<String, Object> resultado = new HashMap<>();

                            resultado.putAll(resultadoRelatorio);

                            resultado.put("registrado", resultadoRelatorio.get("sucesso"));
                            resultado.put("funcionario", funcionario);
                            resultado.put("lote", lote);
                            resultado.put("responsavel_cozedura", responsavelCozedura);
                            resultado.put("timestamp", new Date().toString());

                            // 6. Completar o job
                            jobClient.newCompleteCommand(job.getKey())
                                    .variables(resultado)
                                    .send()
                                    .join();

                            System.out.println("RELATÓRIO GERADO COM SUCESSO!");
                            System.out.println("   Caminho: " + resultado.get("caminhoFicheiro"));
                            System.out.println("=================[COMPLETO]==============================\n");

                        } catch (Exception e) {
                            System.err.println("ERRO: " + e.getMessage());
                            e.printStackTrace();

                            Map<String, Object> erro = new HashMap<>();
                            erro.put("registrado", false);
                            erro.put("erro", e.getMessage());
                            erro.put("jobKey", job.getKey());

                            jobClient.newCompleteCommand(job.getKey())
                                    .variables(erro)
                                    .send()
                                    .join();
                        }
                    }
                })
                .timeout(Duration.ofSeconds(60))
                .maxJobsActive(3)
                .open();

        // 3. Worker: verificar_alimentos
        JobWorker worker3 = client.newWorker()
                .jobType("verificar_alimentos")
                .handler(new JobHandler() {
                    /**
                     * Trata o job do tipo {@code verificar_alimentos}.
                     *
                     * <p>
                     * Este handler obtém listas de alimentos e quantidades a partir de
                     * strings separadas por ponto, converte as quantidades em inteiros
                     * e utiliza {@link StockManager#verificarPedido(String[], int[])} para
                     * verificar se existe stock suficiente no armazém.
                     * No fim, completa o job com a variável {@code AlimentosArmazem}
                     * como {@code "true"} ou {@code "false"} e uma mensagem de texto.
                     * </p>
                     *
                     * @param jobClient cliente para envio de comandos relacionados com o job.
                     * @param job       job Zeebe atualmente ativado, contendo as variáveis do processo.
                     */
                    @Override
                    public void handle(JobClient jobClient, ActivatedJob job) {
                        System.out.println("=================[Verificar Stock de alimentos]==============================");

                        try {
                            Map<String, Object> vars = job.getVariablesAsMap();
                            String alimentosStr = (String) vars.get("alimentos"); // "arroz.feijão.tomate"
                            String quantidadesStr = (String) vars.get("quantidades"); // "5.3.2"

                            // 1. Separar os dados
                            String[] alimentos = alimentosStr.split("\\.");
                            String[] quantidadesArray = quantidadesStr.split("\\.");

                            // 2. Converter quantidades para int[]
                            int[] quantidades = new int[quantidadesArray.length];
                            for (int i = 0; i < quantidadesArray.length; i++) {
                                quantidades[i] = Integer.parseInt(quantidadesArray[i].trim());
                            }

                            // 3. Verificar com StockManager
                            boolean temTodos = StockManager.verificarPedido(alimentos, quantidades);

                            // 4. Log simples
                            System.out.println("Alimentos: " + String.join(", ", alimentos));
                            System.out.println("Quantidades: " + Arrays.toString(quantidades));
                            System.out.println("Resultado: " + (temTodos ? "TEM TODOS" : "FALTA ALGUM"));

                            // 5. Preparar resposta SIM/NÃO para o gateway
                            Map<String, Object> resultado = new HashMap<>();
                            resultado.put("AlimentosArmazem", temTodos ? "true" : "false");
                            resultado.put("mensagem", temTodos ? "Stock suficiente" : "Stock insuficiente");

                            // 6. Completar
                            jobClient.newCompleteCommand(job.getKey())
                                    .variables(resultado)
                                    .send()
                                    .join();

                        } catch (Exception e) {
                            System.err.println("Erro: " + e.getMessage());
                            jobClient.newFailCommand(job.getKey())
                                    .retries(0)
                                    .errorMessage("Erro na verificação: " + e.getMessage())
                                    .send()
                                    .join();
                        }
                    }
                })
                .timeout(Duration.ofSeconds(30))
                .open();

        // 4. Worker: Emitir Ordem de Compra
        JobWorker worker4 = client.newWorker()
                .jobType("Emitir_Ordem_Compra")
                .handler(new JobHandler() {
                    /**
                     * Trata o job do tipo {@code Emitir_Ordem_Compra}.
                     *
                     * <p>
                     * Simula a emissão de uma ordem de compra: lê as variáveis do fornecedor,
                     * ingrediente e quantidade, escreve um log e marca nas variáveis do processo
                     * que a ordem foi emitida ({@code ordemEmitida = true}, {@code estadoOrdem = "emitida"}).
                     * </p>
                     *
                     * @param jobClient cliente para envio de comandos relacionados com o job.
                     * @param job       job Zeebe atualmente ativado, contendo as variáveis do processo.
                     */
                    @Override
                    public void handle(JobClient jobClient, ActivatedJob job) {
                        System.out.println("=================[Emitir Ordem de Compra]==============================");

                        try {
                            Map<String, Object> vars = job.getVariablesAsMap();

                            String fornecedor = (String) vars.getOrDefault("fornecedor", "N/D");
                            String ingrediente = (String) vars.getOrDefault("ingrediente", "N/D");
                            String quantidade = (String) vars.getOrDefault("quantidade", "N/D");

                            System.out.printf("Emitir ordem de compra para %s | ingrediente: %s | quantidade: %s%n",
                                    fornecedor, ingrediente, quantidade);

                            // marca que a ordem foi emitida
                            vars.put("ordemEmitida", true);
                            vars.put("estadoOrdem", "emitida");

                            jobClient.newCompleteCommand(job.getKey())
                                    .variables(vars)
                                    .send()
                                    .join();

                            System.out.println("Ordem de compra emitida com sucesso!");
                        } catch (Exception e) {
                            System.err.println("ERRO em Emitir_Ordem_Compra: " + e.getMessage());
                            jobClient.newFailCommand(job.getKey())
                                    .retries(0)
                                    .errorMessage("Falha ao emitir ordem: " + e.getMessage())
                                    .send()
                                    .join();
                        }
                    }
                })
                .timeout(Duration.ofMinutes(2))
                .maxJobsActive(3)
                .open();

        // 5. Worker: Enviar Amostras (fornecedores)
        JobWorker worker5 = client.newWorker()
                .jobType("Enviar_amostras")
                .handler(new JobHandler() {
                    /**
                     * Trata o job do tipo {@code Enviar_amostras}.
                     *
                     * <p>
                     * Simula o envio de amostras por parte do fornecedor, regista o lote
                     * e o ingrediente em log, e define a variável {@code amostrasEnviadas = true}
                     * para indicar que a ação foi concluída.
                     * </p>
                     *
                     * @param jobClient cliente para envio de comandos relacionados com o job.
                     * @param job       job Zeebe atualmente ativado, contendo as variáveis do processo.
                     */
                    @Override
                    public void handle(JobClient jobClient, ActivatedJob job) {
                        System.out.println("=================[Enviar Amostras]==============================");

                        try {
                            Map<String, Object> vars = job.getVariablesAsMap();

                            String loteAmostra = (String) vars.getOrDefault("lote_amostra", "LOTE-AMOSTRA-ND");
                            String ingrediente = (String) vars.getOrDefault("ingrediente", "N/D");

                            System.out.printf("Fornecedor está a enviar amostras | ingrediente: %s | lote: %s%n",
                                    ingrediente, loteAmostra);

                            vars.put("amostrasEnviadas", true);

                            jobClient.newCompleteCommand(job.getKey())
                                    .variables(vars)
                                    .send()
                                    .join();

                            System.out.println("Amostras enviadas (simulado).");
                        } catch (Exception e) {
                            System.err.println("ERRO em Enviar_Amostras: " + e.getMessage());
                            jobClient.newFailCommand(job.getKey())
                                    .retries(0)
                                    .errorMessage("Falha ao enviar amostras: " + e.getMessage())
                                    .send()
                                    .join();
                        }
                    }
                })
                .timeout(Duration.ofMinutes(1))
                .maxJobsActive(3)
                .open();

        // 6. Worker: Preparar Encomenda
        JobWorker worker6 = client.newWorker()
                .jobType("Preparar_Encomenda")
                .handler(new JobHandler() {
                    /**
                     * Trata o job do tipo {@code Preparar_Encomenda}.
                     *
                     * <p>
                     * Simula a preparação de uma encomenda associada a uma determinada ordem.
                     * Lê a variável {@code ordemId} para log, marca {@code encomendaPreparada = true}
                     * e completa o job.
                     * </p>
                     *
                     * @param jobClient cliente para envio de comandos relacionados com o job.
                     * @param job       job Zeebe atualmente ativado, contendo as variáveis do processo.
                     */
                    @Override
                    public void handle(JobClient jobClient, ActivatedJob job) {
                        System.out.println("=================[Preparar Encomenda]==============================");

                        try {
                            Map<String, Object> vars = job.getVariablesAsMap();

                            String ordemId = (String) vars.getOrDefault("ordemId", "ORDEM-ND");
                            System.out.println("A preparar encomenda para ORDEM: " + ordemId);

                            vars.put("encomendaPreparada", true);

                            jobClient.newCompleteCommand(job.getKey())
                                    .variables(vars)
                                    .send()
                                    .join();

                            System.out.println("Encomenda preparada (simulado).");
                        } catch (Exception e) {
                            System.err.println("ERRO em Preparar_Encomenda: " + e.getMessage());
                            jobClient.newFailCommand(job.getKey())
                                    .retries(0)
                                    .errorMessage("Falha ao preparar encomenda: " + e.getMessage())
                                    .send()
                                    .join();
                        }
                    }
                })
                .timeout(Duration.ofMinutes(2))
                .maxJobsActive(3)
                .open();

        // 7. Worker: Rejeitar Proposta
        JobWorker worker7 = client.newWorker()
                .jobType("Rejeitar_Proposta")
                .handler(new JobHandler() {
                    /**
                     * Trata o job do tipo {@code Rejeitar_Proposta}.
                     *
                     * <p>
                     * Regista a rejeição de uma proposta de fornecedor, indicando o motivo.
                     * Define as variáveis {@code propostaRejeitada = true} e
                     * {@code estadoProposta = "rejeitada"} e completa o job.
                     * </p>
                     *
                     * @param jobClient cliente para envio de comandos relacionados com o job.
                     * @param job       job Zeebe atualmente ativado, contendo as variáveis do processo.
                     */
                    @Override
                    public void handle(JobClient jobClient, ActivatedJob job) {
                        System.out.println("=================[Rejeitar Proposta]==============================");

                        try {
                            Map<String, Object> vars = job.getVariablesAsMap();

                            String fornecedor = (String) vars.getOrDefault("fornecedor", "N/D");
                            String motivo = (String) vars.getOrDefault("motivo_rejeicao",
                                    "Proposta não cumpre os requisitos.");

                            System.out.printf("Proposta do fornecedor %s rejeitada. Motivo: %s%n",
                                    fornecedor, motivo);

                            vars.put("propostaRejeitada", true);
                            vars.put("estadoProposta", "rejeitada");

                            jobClient.newCompleteCommand(job.getKey())
                                    .variables(vars)
                                    .send()
                                    .join();

                            System.out.println("Proposta marcada como REJEITADA.");
                        } catch (Exception e) {
                            System.err.println("ERRO em Rejeitar_Proposta: " + e.getMessage());
                            jobClient.newFailCommand(job.getKey())
                                    .retries(0)
                                    .errorMessage("Falha ao rejeitar proposta: " + e.getMessage())
                                    .send()
                                    .join();
                        }
                    }
                })
                .timeout(Duration.ofMinutes(1))
                .maxJobsActive(2)
                .open();

        // 8. Worker: Enviar Encomenda
        JobWorker worker8 = client.newWorker()
                .jobType("fornecedor_envia_encomenda")
                .handler(new JobHandler() {

                    /**
                     * Trata o job do tipo {@code Enviar_Encomenda}.
                     *
                     * <p>
                     * Simula o envio de uma encomenda para o fornecedor.
                     * Não depende de formulários — apenas lê as variáveis existentes.
                     * Define {@code encomendaEnviada = true} e {@code estadoEncomenda = "enviada"}.
                     * </p>
                     *
                     * @param jobClient cliente para envio de comandos relacionados com o job.
                     * @param job job Zeebe atualmente ativado.
                     */
                    @Override
                    public void handle(JobClient jobClient, ActivatedJob job) {

                        System.out.println("=================[Enviar Encomenda]==============================");

                        try {
                            Map<String, Object> vars = job.getVariablesAsMap();

                            String fornecedor  = (String) vars.getOrDefault("fornecedor", "N/D");
                            String ingrediente = (String) vars.getOrDefault("ingrediente", "N/D");
                            String quantidade  = (String) vars.getOrDefault("quantidade", "N/D");
                            String ordemId     = (String) vars.getOrDefault("ordemId", "ORDEM-ND");

                            System.out.printf(
                                    "A enviar encomenda para o fornecedor %s | ingrediente: %s | quantidade: %s | ordem: %s%n",
                                    fornecedor, ingrediente, quantidade, ordemId
                            );

                            vars.put("encomendaEnviada", true);
                            vars.put("estadoEncomenda", "enviada");

                            jobClient.newCompleteCommand(job.getKey())
                                    .variables(vars)
                                    .send()
                                    .join();

                            System.out.println("Encomenda enviada com sucesso!");

                        } catch (Exception e) {
                            System.err.println("ERRO em Enviar_Encomenda: " + e.getMessage());

                            jobClient.newFailCommand(job.getKey())
                                    .retries(0)
                                    .errorMessage("Falha ao enviar encomenda: " + e.getMessage())
                                    .send()
                                    .join();
                        }
                    }
                })
                .timeout(Duration.ofMinutes(2))
                .maxJobsActive(3)
                .open();
    }
}
