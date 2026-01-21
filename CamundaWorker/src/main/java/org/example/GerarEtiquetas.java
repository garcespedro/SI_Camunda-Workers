package org.example;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Classe responsável pela geração de etiquetas de produto.
 *
 * <p>
 * A classe {@code GerarEtiquetas} recebe variáveis de contexto (por exemplo, do
 * processo BPMN) e gera uma etiqueta textual com informação sobre o lote,
 * embalamento, datas e responsável. A etiqueta é depois gravada em ficheiro
 * de texto e é devolvido um conjunto de variáveis com o resultado da operação.
 * </p>
 */
public class GerarEtiquetas {

    /**
     * Gera uma etiqueta completa com base nas variáveis fornecidas.
     *
     * <p>
     * As variáveis relevantes esperadas no mapa são, entre outras:
     * <ul>
     *     <li>{@code lote_embalagem} – identificação do lote da embalagem;</li>
     *     <li>{@code embalamento} – descrição do tipo de embalamento;</li>
     *     <li>{@code responsavel_embalamento} – nome do operador responsável;</li>
     *     <li>{@code data_embalamento} – data/hora do embalamento (texto);</li>
     *     <li>{@code validade} – data de validade (texto).</li>
     * </ul>
     * Se algumas destas variáveis não forem fornecidas, são usados valores por omissão.
     * Caso a validade não seja fornecida, é calculada automaticamente como 7 dias após
     * a data de embalamento.
     * </p>
     *
     * <p>
     * O método gera um ID único de produto, constrói o conteúdo da etiqueta,
     * grava-o em ficheiro de texto e devolve um mapa com:
     * <ul>
     *     <li>{@code sucesso} – {@code true} se tudo correu bem;</li>
     *     <li>{@code etiquetaGerada} – {@code true} se a etiqueta foi gerada;</li>
     *     <li>{@code produtoId} – identificador gerado para o produto;</li>
     *     <li>{@code loteEmbalagem}, {@code embalamento}, {@code dataEmbalamento},
     *         {@code dataValidade}, {@code responsavelEmbalamento};</li>
     *     <li>{@code caminhoEtiqueta} – caminho do ficheiro criado;</li>
     *     <li>{@code timestamp} – data/hora da operação;</li>
     *     <li>em caso de erro, {@code erro} – mensagem de erro.</li>
     * </ul>
     * </p>
     *
     * @param variaveis mapa de variáveis de entrada utilizadas para compor a etiqueta.
     * @return mapa de variáveis com o resultado da geração da etiqueta.
     */
    public static Map<String, Object> gerarEtiquetaCompleta(Map<String, Object> variaveis) {
        Map<String, Object> resultado = new HashMap<>();

        try {
            String loteEmbalagem = (String) variaveis.getOrDefault("lote_embalagem",
                    "LOTE-" + System.currentTimeMillis());
            String embalamento = (String) variaveis.getOrDefault("embalamento", "Embalamento padrão");
            String responsavelEmbalamento = (String) variaveis.getOrDefault("responsavel_embalamento",
                    "Operador não identificado");

            String dataEmbalamentoInput = (String) variaveis.get("data_embalamento");
            String dataValidadeInput = (String) variaveis.get("validade");

            SimpleDateFormat sdfDataHora = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            SimpleDateFormat sdfData = new SimpleDateFormat("dd/MM/yyyy");

            String dataEmbalamento;
            String dataValidade;

            if (dataEmbalamentoInput != null && !dataEmbalamentoInput.trim().isEmpty()) {
                dataEmbalamento = dataEmbalamentoInput; // Usa a data fornecida
            } else {
                dataEmbalamento = sdfDataHora.format(new Date()); // Data atual
            }

            if (dataValidadeInput != null && !dataValidadeInput.trim().isEmpty()) {
                dataValidade = dataValidadeInput; // Usa a data fornecida
            } else {
                Calendar cal = Calendar.getInstance();

                if (dataEmbalamentoInput != null && !dataEmbalamentoInput.trim().isEmpty()) {
                    try {
                        Date dataEmbalDate = sdfDataHora.parse(dataEmbalamentoInput);
                        cal.setTime(dataEmbalDate);
                    } catch (Exception e) {
                        cal.setTime(new Date());
                    }
                }

                cal.add(Calendar.DAY_OF_MONTH, 7);
                dataValidade = sdfData.format(cal.getTime());
            }

            String produtoId = "PROD-" + loteEmbalagem + "-" + System.currentTimeMillis();

            String conteudoEtiqueta = gerarConteudoFormatado(
                    produtoId, loteEmbalagem, embalamento,
                    dataEmbalamento, dataValidade, responsavelEmbalamento
            );

            String caminhoEtiqueta = salvarEmFicheiro(conteudoEtiqueta, loteEmbalagem, produtoId);

            resultado.put("sucesso", true);
            resultado.put("etiquetaGerada", true);
            resultado.put("produtoId", produtoId);
            resultado.put("loteEmbalagem", loteEmbalagem);
            resultado.put("embalamento", embalamento);
            resultado.put("dataEmbalamento", dataEmbalamento);
            resultado.put("dataValidade", dataValidade);
            resultado.put("responsavelEmbalamento", responsavelEmbalamento);
            resultado.put("caminhoEtiqueta", caminhoEtiqueta);
            resultado.put("timestamp", new Date().toString());

        } catch (Exception e) {
            resultado.put("sucesso", false);
            resultado.put("etiquetaGerada", false);
            resultado.put("erro", e.getMessage());
        }

        return resultado;
    }

    /**
     * Gera o conteúdo textual formatado da etiqueta.
     *
     * <p>
     * Este método cria uma representação em texto da etiqueta, com molduras
     * e secções para:
     * <ul>
     *     <li>Informações do produto (ID, lote, embalamento);</li>
     *     <li>Datas importantes (data de embalamento e data de validade);</li>
     *     <li>Responsável pelo embalamento;</li>
     *     <li>Código do produto;</li>
     *     <li>Instruções gerais de conservação e consumo.</li>
     * </ul>
     * O formato é adequado para armazenamento em ficheiro de texto ou para
     * impressão em consola.
     * </p>
     *
     * @param produtoId             identificador único do produto.
     * @param loteEmbalagem         identificação do lote da embalagem.
     * @param embalamento           descrição do tipo de embalamento.
     * @param dataEmbalamento       data/hora de embalamento em formato legível.
     * @param dataValidade          data de validade em formato legível.
     * @param responsavelEmbalamento nome do operador responsável pelo embalamento.
     * @return string contendo o texto da etiqueta formatada.
     */
    private static String gerarConteudoFormatado(String produtoId, String loteEmbalagem,
                                                 String embalamento, String dataEmbalamento,
                                                 String dataValidade, String responsavelEmbalamento) {

        StringBuilder sb = new StringBuilder();

        // CABEÇALHO
        sb.append("╔══════════════════════════════════════════════╗\n");
        sb.append("║              ETIQUETA DO PRODUTO             ║\n");
        sb.append("╚══════════════════════════════════════════════╝\n\n");

        // INFORMAÇÕES DO PRODUTO
        sb.append("┌────────────────────────────────────────────┐\n");
        sb.append("│    INFORMAÇÕES DO PRODUTO                  │\n");
        sb.append("├────────────────────────────────────────────┤\n");
        sb.append(String.format("│ %-18s: %-22s │\n", "ID", produtoId));
        sb.append(String.format("│ %-18s: %-22s │\n", "Lote", loteEmbalagem));
        sb.append(String.format("│ %-18s: %-22s │\n", "Embalamento", embalamento));
        sb.append("└────────────────────────────────────────────┘\n\n");

        // DATAS
        sb.append("┌────────────────────────────────────────────┐\n");
        sb.append("│    DATAS IMPORTANTES                       │\n");
        sb.append("├────────────────────────────────────────────┤\n");
        sb.append(String.format("│ %-18s: %-22s │\n", "Embalado em", dataEmbalamento));
        sb.append(String.format("│ %-18s: %-22s │\n", "Válido até", dataValidade));
        sb.append("└────────────────────────────────────────────┘\n\n");

        // RESPONSÁVEL
        sb.append("┌────────────────────────────────────────────┐\n");
        sb.append("│    RESPONSÁVEL                             │\n");
        sb.append("├────────────────────────────────────────────┤\n");
        sb.append(String.format("│ %-18s: %-22s │\n", "Embalado por", responsavelEmbalamento));
        sb.append("└────────────────────────────────────────────┘\n\n");

        // CÓDIGO
        sb.append("┌────────────────────────────────────────────┐\n");
        sb.append("│     CÓDIGO DO PRODUTO                      │\n");
        sb.append("├────────────────────────────────────────────┤\n");
        sb.append("│ ").append(produtoId).append(" ".repeat(Math.max(0, 38 - produtoId.length()))).append("│\n");
        sb.append("└────────────────────────────────────────────┘\n\n");

        // INSTRUÇÕES
        sb.append("┌────────────────────────────────────────────┐\n");
        sb.append("│     INFORMAÇÕES                            │\n");
        sb.append("├────────────────────────────────────────────┤\n");
        sb.append("│ • Conservar em local fresco e seco         │\n");
        sb.append("│ • Consumir até data de validade            │\n");
        sb.append("│ • Produto inspecionado e aprovado          │\n");
        sb.append("│ • Em caso de dúvida, contactar produção    │\n");
        sb.append("└────────────────────────────────────────────┘\n\n");

        // RODAPÉ
        sb.append("══════════════════════════════════════════════\n");
        sb.append("         Etiqueta gerada automaticamente      \n");
        sb.append("══════════════════════════════════════════════\n");

        return sb.toString();
    }

    /**
     * Guarda o conteúdo da etiqueta num ficheiro de texto.
     *
     * <p>
     * O ficheiro é criado na pasta {@code etiquetas_geradas} (caso não exista,
     * é criada automaticamente). O nome do ficheiro inclui o lote e um timestamp
     * no formato {@code yyyyMMdd_HHmmss}, no padrão {@code ETQ_<lote>_<timestamp>.txt}.
     * </p>
     *
     * @param conteudo  texto completo da etiqueta a gravar.
     * @param lote      lote associado à etiqueta (usado no nome do ficheiro).
     * @param produtoId identificador do produto (não utilizado no nome do ficheiro,
     *                  mas disponível para personalizações futuras).
     * @return caminho relativo do ficheiro criado.
     * @throws Exception se ocorrer algum erro ao criar a pasta ou escrever o ficheiro.
     */
    private static String salvarEmFicheiro(String conteudo, String lote, String produtoId) throws Exception {
        File pasta = new File("etiquetas_geradas");
        if (!pasta.exists()) {
            pasta.mkdirs();
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String timestamp = sdf.format(new Date());
        String nomeFicheiro = String.format("ETQ_%s_%s.txt",
                lote.replaceAll("[^a-zA-Z0-9-_]", ""),
                timestamp);

        String caminho = "etiquetas_geradas/" + nomeFicheiro;

        Files.write(Paths.get(caminho), conteudo.getBytes());

        return caminho;
    }
}
