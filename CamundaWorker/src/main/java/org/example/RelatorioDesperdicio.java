package org.example;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Classe responsável pela criação de relatórios de desperdício alimentar.
 *
 * <p>
 * A classe {@code RelatorioDesperdicio} recebe um mapa flexível de variáveis
 * (tipicamente provenientes de um processo BPMN) e gera um relatório de texto
 * com os detalhes dos alimentos desperdiçados, motivos, responsáveis e
 * informações de contexto. O relatório é gravado em ficheiro de texto e é
 * devolvido um conjunto de variáveis com o resultado da operação.
 * </p>
 */
public class RelatorioDesperdicio {

    /**
     * Cria um relatório de desperdício alimentar completo a partir de um mapa de variáveis.
     *
     * <p>
     * Este método é flexível e aceita qualquer {@link Map} de variáveis.
     * As chaves mais relevantes esperadas são:
     * <ul>
     *     <li>{@code alimentos} – lista de alimentos, separados por ponto (por exemplo, {@code "arroz.feijão"});</li>
     *     <li>{@code quantidades} – lista de quantidades, também separadas por ponto (por exemplo, {@code "2.3"});</li>
     *     <li>{@code lote_produto} – lote associado ao desperdício;</li>
     *     <li>{@code responsavel_cozedura} – responsável pela cozedura;</li>
     *     <li>{@code nome_funcionario} ou {@code operador} – funcionário que registou o desperdício;</li>
     *     <li>{@code motivo} – motivo do desperdício (ex.: {@code "qualidade_insuficiente"});</li>
     *     <li>{@code descricao_preparacao} – descrição da preparação (opcional);</li>
     *     <li>{@code descricao_procedimento} – descrição do procedimento realizado (opcional);</li>
     *     <li>{@code equipamentos} – equipamentos utilizados (opcional).</li>
     * </ul>
     * Caso algumas variáveis não sejam fornecidas, são usados valores por omissão.
     * </p>
     *
     * <p>
     * O método gera o conteúdo textual do relatório, grava-o em ficheiro e devolve
     * um mapa de resultado que inclui, entre outros:
     * <ul>
     *     <li>{@code sucesso} – {@code true} se o relatório foi criado sem erros;</li>
     *     <li>{@code caminhoFicheiro} – caminho do ficheiro de relatório gerado;</li>
     *     <li>{@code lote}, {@code responsavelCozedura}, {@code funcionario};</li>
     *     <li>{@code timestamp} – data/hora em que o relatório foi gerado;</li>
     *     <li>em caso de erro, {@code erro} – mensagem de erro.</li>
     * </ul>
     * </p>
     *
     * @param variaveis mapa com as variáveis de entrada utilizadas para construir o relatório.
     * @return mapa com o resultado da operação de criação do relatório.
     */
    public static Map<String, Object> criarRelatorioCompleto(Map<String, Object> variaveis) {
        Map<String, Object> resultado = new HashMap<>();

        try {
            String alimentos = (String) variaveis.getOrDefault("alimentos", "");
            String quantidades = (String) variaveis.getOrDefault("quantidades", "");
            String lote = (String) variaveis.getOrDefault("lote_produto", "LOTE-NÃO-INFORMADO");
            String responsavelCozedura = (String) variaveis.getOrDefault("responsavel_cozedura", "Não informado");
            String funcionario = (String) variaveis.getOrDefault("nome_funcionario",
                    variaveis.getOrDefault("operador", "Operador não identificado"));
            String motivo = (String) variaveis.getOrDefault("motivo", "qualidade_insuficiente");

            String descPreparacao = (String) variaveis.getOrDefault("descricao_preparacao", "N/A");
            String descProcedimento = (String) variaveis.getOrDefault("descricao_procedimento", "N/A");
            String equipamentos = (String) variaveis.getOrDefault("equipamentos", "N/A");

            String conteudo = gerarConteudoRelatorio(
                    alimentos, quantidades, lote, responsavelCozedura,
                    funcionario, motivo, descPreparacao, descProcedimento, equipamentos
            );

            String caminhoFicheiro = salvarFicheiro(conteudo, lote);

            resultado.put("sucesso", true);
            resultado.put("caminhoFicheiro", caminhoFicheiro);
            resultado.put("lote", lote);
            resultado.put("responsavelCozedura", responsavelCozedura);
            resultado.put("funcionario", funcionario);
            resultado.put("timestamp", new Date().toString());

        } catch (Exception e) {
            resultado.put("sucesso", false);
            resultado.put("erro", e.getMessage());
        }

        return resultado;
    }

    /**
     * Gera o conteúdo textual formatado do relatório de desperdício alimentar.
     *
     * <p>
     * O relatório inclui:
     * <ul>
     *     <li>Cabeçalho com título;</li>
     *     <li>Informações básicas (data, lote, responsáveis, motivo);</li>
     *     <li>Lista de alimentos desperdiçados e respetivas quantidades (quando fornecidos);</li>
     *     <li>Informações adicionais sobre o processo (preparação, procedimento, equipamentos), caso existam;</li>
     *     <li>Secção de assinaturas para validação.</li>
     * </ul>
     * As listas de alimentos e quantidades são construídas a partir de strings
     * separadas por ponto ({@code "."}).
     * </p>
     *
     * @param alimentos        string com os alimentos separados por ponto.
     * @param quantidades      string com as quantidades separadas por ponto.
     * @param lote             lote associado ao desperdício.
     * @param responsavelCozedura nome do responsável pela cozedura.
     * @param funcionario      nome do funcionário/operador que registou o desperdício.
     * @param motivo           motivo do desperdício.
     * @param descPreparacao   descrição da preparação (ou {@code "N/A"} se não aplicável).
     * @param descProcedimento descrição do procedimento realizado (ou {@code "N/A"} se não aplicável).
     * @param equipamentos     equipamentos utilizados (ou {@code "N/A"} se não aplicável).
     * @return string com o texto completo do relatório formatado.
     */
    private static String gerarConteudoRelatorio(String alimentos, String quantidades,
                                                 String lote, String responsavelCozedura,
                                                 String funcionario, String motivo,
                                                 String descPreparacao, String descProcedimento,
                                                 String equipamentos) {

        StringBuilder sb = new StringBuilder();

        // CABEÇALHO
        sb.append("╔══════════════════════════════════════════════╗\n");
        sb.append("║      RELATÓRIO DE DESPERDÍCIO ALIMENTAR      ║\n");
        sb.append("╚══════════════════════════════════════════════╝\n\n");

        // INFORMAÇÕES BÁSICAS
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        sb.append("DATA: ").append(sdf.format(new Date())).append("\n");
        sb.append("LOTE: ").append(lote).append("\n");
        sb.append("RESPONSÁVEL COZEDURA: ").append(responsavelCozedura).append("\n");
        sb.append("FUNCIONÁRIO: ").append(funcionario).append("\n");
        sb.append("MOTIVO: ").append(motivo).append("\n\n");

        // ALIMENTOS DESPERDIÇADOS
        sb.append("────────────────────────────────────────────\n");
        sb.append("            ALIMENTOS DESPERDIÇADOS           \n");
        sb.append("────────────────────────────────────────────\n\n");

        if (!alimentos.isEmpty() && !quantidades.isEmpty()) {
            String[] alimentosArr = alimentos.split("\\.");
            String[] quantidadesArr = quantidades.split("\\.");

            for (int i = 0; i < Math.min(alimentosArr.length, quantidadesArr.length); i++) {
                sb.append(String.format("  • %-15s : %4s unidades%n",
                        alimentosArr[i].trim(),
                        quantidadesArr[i].trim()));
            }
            sb.append("\n");
            sb.append("  TOTAL ITENS: ").append(alimentosArr.length).append("\n\n");
        } else {
            sb.append("  Não foram especificados alimentos\n\n");
        }

        // INFORMAÇÕES DO PROCESSO (APENAS SE NÃO FOR "N/A")
        if (!descPreparacao.equals("N/A") || !descProcedimento.equals("N/A") || !equipamentos.equals("N/A")) {
            sb.append("────────────────────────────────────────────\n");
            sb.append("           INFORMAÇÕES DO PROCESSO          \n");
            sb.append("────────────────────────────────────────────\n\n");

            if (!descPreparacao.equals("N/A")) {
                sb.append("DESCRIÇÃO DA PREPARAÇÃO:\n");
                sb.append("  ").append(descPreparacao).append("\n\n");
            }

            if (!descProcedimento.equals("N/A")) {
                sb.append("PROCEDIMENTO REALIZADO:\n");
                sb.append("  ").append(descProcedimento).append("\n\n");
            }

            if (!equipamentos.equals("N/A")) {
                sb.append("EQUIPAMENTOS UTILIZADOS:\n");
                sb.append("  ").append(equipamentos).append("\n\n");
            }
        }

        // ASSINATURAS
        sb.append("\n────────────────────────────────────────────\n");
        sb.append("ASSINATURAS\n\n");
        sb.append("Responsável Cozedura: ____________________\n");
        sb.append("Data: ______/______/______\n");
        sb.append("\n══════════════════════════════════════════════\n");
        sb.append("         RELATÓRIO GERADO AUTOMATICAMENTE      \n");
        sb.append("══════════════════════════════════════════════\n");

        return sb.toString();
    }

    /**
     * Guarda o conteúdo do relatório num ficheiro de texto.
     *
     * <p>
     * O ficheiro é criado na pasta {@code relatorios}. Caso a pasta não exista,
     * é criada automaticamente. O nome do ficheiro é gerado no formato
     * {@code DESP_<lote>_<timestamp>.txt}, onde o lote é sanitizado para conter
     * apenas caracteres alfanuméricos (os restantes são substituídos por {@code "_"}).
     * O {@code timestamp} segue o formato {@code yyyyMMdd_HHmmss}.
     * </p>
     *
     * @param conteudo texto completo do relatório a gravar.
     * @param lote     lote associado ao relatório (usado no nome do ficheiro).
     * @return caminho relativo do ficheiro de relatório criado.
     * @throws Exception se ocorrer algum erro ao criar a pasta ou escrever o ficheiro.
     */
    private static String salvarFicheiro(String conteudo, String lote) throws Exception {
        // Criar pasta se não existir
        File pasta = new File("relatorios");
        if (!pasta.exists()) {
            pasta.mkdirs();
        }

        // Gerar nome do ficheiro
        SimpleDateFormat sdfFile = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String timestamp = sdfFile.format(new Date());
        String nomeFicheiro = String.format("DESP_%s_%s.txt",
                lote.replaceAll("[^a-zA-Z0-9]", "_"),
                timestamp);

        String caminho = "relatorios/" + nomeFicheiro;

        // Escrever ficheiro
        Files.write(Paths.get(caminho), conteudo.getBytes());

        return caminho;
    }
}
