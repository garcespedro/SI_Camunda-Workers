package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;
import java.util.Map;

/**
 * Classe responsável por gerir o stock de alimentos da aplicação.
 *
 * <p>
 * A classe {@code StockManager} carrega automaticamente um ficheiro JSON com o
 * stock de alimentos disponível e fornece métodos para verificar se uma lista
 * de alimentos e quantidades pode ser retirada do stock.
 * </p>
 *
 * <p>
 * O ficheiro obrigatoriamente colocado em
 * {@code resources/stock_alimentos.json} deve conter um mapa no formato:
 * <pre>
 * {
 *   "arroz": 10,
 *   "feijao": 5,
 *   "tomate": 12
 * }
 * </pre>
 * </p>
 */
public class StockManager {

    /**
     * Estrutura que mantém em memória o stock disponível, onde:
     * <ul>
     *     <li>Chave → nome do alimento (minúsculas);</li>
     *     <li>Valor → quantidade disponível.</li>
     * </ul>
     */
    private static Map<String, Integer> stock;

    // Bloco estático executado automaticamente ao carregar a classe.
    static {
        carregarStock();
    }

    /**
     * Carrega o stock de alimentos a partir do ficheiro JSON {@code stock_alimentos.json}.
     *
     * <p>
     * Este método utiliza Jackson para converter o ficheiro num {@link Map}.
     * Caso o ficheiro não seja encontrado ou ocorra algum erro de leitura, é lançada
     * uma {@link RuntimeException}, impedindo a aplicação de continuar sem stock válido.
     * </p>
     */
    private static void carregarStock() {
        try {
            ObjectMapper mapper = new ObjectMapper();

            InputStream is = StockManager.class
                    .getClassLoader()
                    .getResourceAsStream("stock_alimentos.json");

            if (is == null) {
                throw new RuntimeException("stock_alimentos.json não encontrado!");
            }

            stock = mapper.readValue(
                    is,
                    new com.fasterxml.jackson.core.type.TypeReference<Map<String, Integer>>() {}
            );

            System.out.println("Stock carregado: " + stock.size() + " itens");

        } catch (Exception e) {
            System.err.println("ERRO ao carregar stock: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * Verifica se todos os alimentos requisitados existem no stock em quantidade suficiente.
     *
     * <p>
     * As listas de alimentos e quantidades devem ter o mesmo tamanho, onde cada índice
     * corresponde ao alimento e à quantidade desejada.
     * O nome do alimento é normalizado para minúsculas antes da verificação.
     * </p>
     *
     * <p>Exemplo:</p>
     * <pre>
     * String[] alimentos = {"arroz", "tomate"};
     * int[] quantidades = {3, 2};
     * boolean podeAtender = StockManager.verificarPedido(alimentos, quantidades);
     * </pre>
     *
     * @param alimentos lista de alimentos pedidos.
     * @param quantidades lista de quantidades correspondentes.
     * @return {@code true} se todos os alimentos existem com stock igual ou superior ao pedido,
     *         {@code false} caso falte algum.
     */
    public static boolean verificarPedido(String[] alimentos, int[] quantidades) {
        for (int i = 0; i < alimentos.length; i++) {

            String alimento = alimentos[i].trim().toLowerCase();
            int quantidade = quantidades[i];

            Integer stockDisponivel = stock.get(alimento);

            // Se o item não existir ou a quantidade for insuficiente
            if (stockDisponivel == null || stockDisponivel < quantidade) {
                return false;
            }
        }

        return true; // Todos os alimentos estão disponíveis.
    }
}
