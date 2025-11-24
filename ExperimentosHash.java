import java.util.Random;
import java.io.PrintWriter;

/**
 * Programa experimental para tabela hash com encadeamento separado.
 * Gera conjuntos de dados, aplica diferentes funcoes de hashing, mede desempenho
 * e imprime resultados em formato CSV.
 */
public class ExperimentosHash {

    // Constantes gerais
    private static final int REPS_INSERCAO = 5;
    private static final int REPS_BUSCA = 5;

    // Tamanhos da tabela m
    private static final int NUM_M = 3;
    private static final int M0 = 1009;
    private static final int M1 = 10007;
    private static final int M2 = 100003;
    private static final int TAM_ARRAY_M = 3;

    // Tamanhos de conjuntos de dados n
    private static final int NUM_N = 3;
    private static final int N0 = 1000;
    private static final int N1 = 10000;
    private static final int N2 = 100000;
    private static final int TAM_ARRAY_N = 3;

    // Seeds publicas
    private static final int NUM_SEEDS = 3;
    private static final long S0 = 137L;
    private static final long S1 = 271828L;
    private static final long S2 = 314159L;
    private static final int TAM_ARRAY_SEEDS = 3;

    // Dominio das chaves (9 digitos)
    private static final int BASE_CHAVE = 100000000;
    private static final int INTERVALO_CHAVE = 900000000;

    // Funcoes de hashing
    private static final int FUNC_DIV = 0;
    private static final int FUNC_MUL = 1;
    private static final int FUNC_FOLD = 2;
    private static final int NUM_FUNCS = 3;

    private static final String ROTULO_DIV = "H_DIV";
    private static final String ROTULO_MUL = "H_MUL";
    private static final String ROTULO_FOLD = "H_FOLD";

    // Constante A para hashing por multiplicacao
    private static final double A_MULT = 0.6180339887;

    // Nome do arquivo CSV de saida
    private static final String NOME_ARQUIVO_CSV = "resultados_hash.csv";

    /**
     * Classe que representa um RegistroDados simples com uma chave inteira.
     */
    static class RegistroDados {
        int codigo;

        public RegistroDados(int c) {
            this.codigo = c;
        }
    }

    /**
     * NoLista de lista encadeada simples usado nos compartimentos da tabela hash.
     */
    static class NoLista {
        RegistroDados dado;
        NoLista proximo;

        public NoLista(RegistroDados r) {
            this.dado = r;
            this.proximo = null;
        }
    }

    /**
     * RespostaInsercao de uma insercao na tabela hash, contendo colisoes de tabela
     * e passos percorridos na lista.
     */
    static class RespostaInsercao {
        int colisoesTabela;
        int passosLista;

        public RespostaInsercao() {
            this.colisoesTabela = 0;
            this.passosLista = 0;
        }
    }

    /**
     * RespostaBusca de uma busca na tabela hash.
     */
    static class RespostaBusca {
        boolean encontrado;
        int comparacoes;
        int passosLista;

        public RespostaBusca() {
            this.encontrado = false;
            this.comparacoes = 0;
            this.passosLista = 0;
        }
    }

    /**
     * Tabela hash com encadeamento separado.
     */
    static class TabelaHash {
        private int tamanho;
        private NoLista[] baldes;

        public TabelaHash(int m) {
            this.tamanho = m;
            this.baldes = new NoLista[m];
        }

        public int hashDivisao(int chave) {
            int h = chave % this.tamanho;
            if (h < 0) {
                h = h + this.tamanho;
            }
            return h;
        }

        public int hashMultiplicacao(int chave) {
            long x = (long) chave;
            double prod = A_MULT * (double) x;
            int parteInteira = (int) prod;
            double fracao = prod - (double) parteInteira;
            double valor = (double) this.tamanho * fracao;
            int h = (int) valor;
            if (h < 0) {
                h = -h;
            }
            if (h >= this.tamanho) {
                h = h - this.tamanho;
            }
            return h;
        }

        public int hashFolding(int chave) {
            int temp = chave;
            int soma = 0;
            while (temp > 0) {
                int bloco = temp % 1000;
                soma = soma + bloco;
                temp = temp / 1000;
            }
            int h = soma % this.tamanho;
            if (h < 0) {
                h = h + this.tamanho;
            }
            return h;
        }

        public int hash(int chave, int funcao) {
            int h;
            if (funcao == FUNC_DIV) {
                h = hashDivisao(chave);
            } else {
                if (funcao == FUNC_MUL) {
                    h = hashMultiplicacao(chave);
                } else {
                    h = hashFolding(chave);
                }
            }
            return h;
        }

        public RespostaInsercao inserir(int chave, int funcao) {
            RespostaInsercao resposta = new RespostaInsercao();
            int h = hash(chave, funcao);
            NoLista cabeca = this.baldes[h];

            if (cabeca == null) {
                this.baldes[h] = new NoLista(new RegistroDados(chave));
            } else {
                resposta.colisoesTabela = 1;
                NoLista atual = cabeca;
                int passos = 1;
                while (atual.proximo != null) {
                    atual = atual.proximo;
                    passos++;
                }
                resposta.passosLista = passos;
                atual.proximo = new NoLista(new RegistroDados(chave));
            }

            return resposta;
        }

        public RespostaBusca buscar(int chave, int funcao) {
            RespostaBusca resposta = new RespostaBusca();
            int h = hash(chave, funcao);
            NoLista atual = this.baldes[h];

            while (atual != null) {
                resposta.comparacoes++;
                if (atual.dado.codigo == chave) {
                    resposta.encontrado = true;
                    break;
                }
                if (atual.proximo != null) {
                    resposta.passosLista++;
                }
                atual = atual.proximo;
            }

            return resposta;
        }
    }

    /**
     * ResumoInsercao de um experimento de insercao.
     */
    static class ResumoInsercao {
        double tempoMedioMs;
        double mediaColisoesTabela;
        double mediaPassosLista;
        int checksum;
        TabelaHash tabelaFinal;

        public ResumoInsercao() {
            this.tempoMedioMs = 0.0;
            this.mediaColisoesTabela = 0.0;
            this.mediaPassosLista = 0.0;
            this.checksum = 0;
            this.tabelaFinal = null;
        }
    }

    /**
     * ResumoBusca de um experimento de busca.
     */
    static class ResumoBusca {
        double tempoMedioHitsMs;
        double tempoMedioMissesMs;
        double mediaComparacoesHits;
        double mediaComparacoesMisses;

        public ResumoBusca() {
            this.tempoMedioHitsMs = 0.0;
            this.tempoMedioMissesMs = 0.0;
            this.mediaComparacoesHits = 0.0;
            this.mediaComparacoesMisses = 0.0;
        }
    }

    /**
     * Gera um vetor de chaves inteiras de 9 digitos usando Random(seed).
     */
    private static int[] gerarConjuntoDados(int tamanhoConjunto, long seed) {
        int[] dados = new int[tamanhoConjunto];
        Random geradorAleatorio = new Random(seed);
        int i = 0;
        while (i < tamanhoConjunto) {
            int valor = BASE_CHAVE + geradorAleatorio.nextInt(INTERVALO_CHAVE);
            dados[i] = valor;
            i++;
        }
        return dados;
    }

    /**
     * Imprime informacao de auditoria no STDERR: funcao, m e seed.
     */
    private static void imprimirAuditoriaInicio(String rotuloFuncao, int tamanhoTabela, long seed) {
        System.err.println(rotuloFuncao + " " + tamanhoTabela + " " + seed);
    }

    /**
     * Imprime o checksum no STDERR.
     */
    private static void imprimirChecksumAuditoria(int checksum) {
        System.err.println("checksum " + checksum);
    }

    /**
     * Executa experimento de insercao para uma combinacao (m, funcao, conjunto de dados, seed).
     */
    private static ResumoInsercao executarInsercao(int[] dados, int tamanhoConjunto, int tamanhoTabela, 
        int funcao, String rotuloFuncao, long seed) {
        ResumoInsercao resumo = new ResumoInsercao();

        imprimirAuditoriaInicio(rotuloFuncao, tamanhoTabela, seed);

        TabelaHash tabelaFinal = null;

        long somaTemposNs = 0L;
        long totalColisoesTabela = 0L;
        long totalPassosLista = 0L;

        long somaHashChecksum = 0L;
        int limiteChecksum = 10;

        TabelaHash tabelaAquecimento = new TabelaHash(tamanhoTabela);
        int limiteAquecimento = tamanhoConjunto;
        if (limiteAquecimento > 1000) {
            limiteAquecimento = 1000;
        }
        int iAq = 0;
        while (iAq < limiteAquecimento) {
            tabelaAquecimento.inserir(dados[iAq], funcao);
            iAq++;
        }

        int rep = 0;
        while (rep < REPS_INSERCAO) {
            TabelaHash tabela = new TabelaHash(tamanhoTabela);

            long inicio = System.nanoTime();

            int i = 0;
            while (i < tamanhoConjunto) {
                if (rep == 0 && i < limiteChecksum) {
                    int h = tabela.hash(dados[i], funcao);
                    somaHashChecksum += h;
                }

                RespostaInsercao respostaInsercao = tabela.inserir(dados[i], funcao);
                totalColisoesTabela += respostaInsercao.colisoesTabela;
                totalPassosLista += respostaInsercao.passosLista;

                i++;
            }

            long fim = System.nanoTime();
            long tempoNs = fim - inicio;
            somaTemposNs += tempoNs;

            tabelaFinal = tabela;

            rep++;
        }

        // Converter nanosegundos pra milissegundos e tirar media
        double tempoMedioMs = somaTemposNs / (1000000.0 * REPS_INSERCAO);
        double mediaColisoesTabela = (double) totalColisoesTabela / (tamanhoConjunto * REPS_INSERCAO);
        double mediaPassosLista = (double) totalPassosLista / (tamanhoConjunto * REPS_INSERCAO);

        int checksum = (int) (somaHashChecksum % 1000003L);

        resumo.tempoMedioMs = tempoMedioMs;
        resumo.mediaColisoesTabela = mediaColisoesTabela;
        resumo.mediaPassosLista = mediaPassosLista;
        resumo.checksum = checksum;
        resumo.tabelaFinal = tabelaFinal;

        imprimirChecksumAuditoria(checksum);

        return resumo;
    }

    /**
     * Executa experimento de busca para uma combinacao (tabela, dados, funcao, seed).
     */
    private static ResumoBusca executarBusca(TabelaHash tabela, int[] dados, int tamanhoConjunto, int funcao, long seed) {
        ResumoBusca resumo = new ResumoBusca();

        int[] consultas = new int[tamanhoConjunto];
        boolean[] ehHit = new boolean[tamanhoConjunto];

        int metade = tamanhoConjunto / 2;
        int i = 0;
        while (i < metade) {
            consultas[i] = dados[i];
            ehHit[i] = true;
            i++;
        }

        // Gerar chaves ausentes pra metade das buscas (misses)
        Random geradorAusentes = new Random(seed + 1L);
        i = metade;
        while (i < tamanhoConjunto) {
            int candidato = BASE_CHAVE + geradorAusentes.nextInt(INTERVALO_CHAVE);
            RespostaBusca teste = tabela.buscar(candidato, funcao);
            if (!teste.encontrado) {
                consultas[i] = candidato;
                ehHit[i] = false;
                i++;
            }
        }

        // Embaralhar as consultas (Fisher-Yates)
        Random geradorEmbaralhamento = new Random(seed + 2L);
        int j = tamanhoConjunto - 1;
        while (j > 0) {
            int r = geradorEmbaralhamento.nextInt(j + 1);
            int temp = consultas[j];
            consultas[j] = consultas[r];
            consultas[r] = temp;
            boolean tempBool = ehHit[j];
            ehHit[j] = ehHit[r];
            ehHit[r] = tempBool;
            j--;
        }

        // Esse loop era so pra aquecer mas ja aquecemos antes
        
        long totalNsHits = 0L;
        long totalNsMisses = 0L;
        long totalCmpHits = 0L;
        long totalCmpMisses = 0L;
        int totalHits = 0;
        int totalMisses = 0;

        int rep = 0;
        while (rep < REPS_BUSCA) {
            int idx = 0;
            while (idx < tamanhoConjunto) {
                int chave = consultas[idx];

                long inicio = System.nanoTime();
                RespostaBusca resposta = tabela.buscar(chave, funcao);
                long fim = System.nanoTime();
                long dt = fim - inicio;

                if (ehHit[idx]) {
                    totalNsHits += dt;
                    totalCmpHits += resposta.comparacoes;
                    totalHits++;
                } else {
                    totalNsMisses += dt;
                    totalCmpMisses += resposta.comparacoes;
                    totalMisses++;
                }

                idx++;
            }
            rep++;
        }

        double tempoMedioHitsMs = 0.0;
        double tempoMedioMissesMs = 0.0;
        double mediaCmpHits = 0.0;
        double mediaCmpMisses = 0.0;

        if (totalHits > 0) {
            tempoMedioHitsMs = totalNsHits / (1000000.0 * totalHits);
            mediaCmpHits = (double) totalCmpHits / totalHits;
        }

        if (totalMisses > 0) {
            tempoMedioMissesMs = totalNsMisses / (1000000.0 * totalMisses);
            mediaCmpMisses = (double) totalCmpMisses / totalMisses;
        }

        resumo.tempoMedioHitsMs = tempoMedioHitsMs;
        resumo.tempoMedioMissesMs = tempoMedioMissesMs;
        resumo.mediaComparacoesHits = mediaCmpHits;
        resumo.mediaComparacoesMisses = mediaCmpMisses;

        return resumo;
    }

    /**
     * Retorna o rotulo da funcao de hashing dado o codigo interno.
     */
    private static String obterRotuloFuncao(int funcao) {
        String rotulo;
        if (funcao == FUNC_DIV) {
            rotulo = ROTULO_DIV;
        } else {
            if (funcao == FUNC_MUL) {
                rotulo = ROTULO_MUL;
            } else {
                rotulo = ROTULO_FOLD;
            }
        }
        return rotulo;
    }

    /**
     * Metodo principal: executa todos os experimentos e imprime CSV em STDOUT e salva em arquivo.
     */
    public static void main(String[] args) {
        int[] valoresM = new int[TAM_ARRAY_M];
        valoresM[0] = M0;
        valoresM[1] = M1;
        valoresM[2] = M2;

        int[] valoresN = new int[TAM_ARRAY_N];
        valoresN[0] = N0;
        valoresN[1] = N1;
        valoresN[2] = N2;

        long[] valoresSeeds = new long[TAM_ARRAY_SEEDS];
        valoresSeeds[0] = S0;
        valoresSeeds[1] = S1;
        valoresSeeds[2] = S2;

        PrintWriter escritorArquivo = null;
        boolean arquivoAberto = false;

        String cabecalho = "m,n,func,seed,ins_ms,coll_tbl,coll_lst,find_ms_hits,find_ms_misses,cmp_hits,cmp_misses,checksum";
        System.out.println(cabecalho);

        int tentativa = 0;
        while (tentativa < 1) {
            escritorArquivo = abrirArquivo(NOME_ARQUIVO_CSV);
            if (escritorArquivo != null) {
                arquivoAberto = true;
                escritorArquivo.println(cabecalho);
            }
            tentativa++;
        }

        // Testar todas as combinacoes de m, n e funcoes
        int im = 0;
        while (im < NUM_M) {
            int tamanhoTabela = valoresM[im];

            int in = 0;
            while (in < NUM_N) {
                int tamanhoConjunto = valoresN[in];
                long seed = valoresSeeds[in];

                int[] dados = gerarConjuntoDados(tamanhoConjunto, seed);

                int ifunc = 0;
                while (ifunc < NUM_FUNCS) {
                    int funcao = ifunc;
                    String rotuloFuncao = obterRotuloFuncao(funcao);

                    ResumoInsercao resumoInsercao = executarInsercao(dados, tamanhoConjunto, tamanhoTabela, 
                                                                       funcao, rotuloFuncao, seed);

                    ResumoBusca resumoBusca = executarBusca(resumoInsercao.tabelaFinal, dados, tamanhoConjunto, 
                                                             funcao, seed);

                    String linha = tamanhoTabela + "," + tamanhoConjunto + "," + rotuloFuncao + "," + seed + ","
                            + resumoInsercao.tempoMedioMs + ","
                            + resumoInsercao.mediaColisoesTabela + ","
                            + resumoInsercao.mediaPassosLista + ","
                            + resumoBusca.tempoMedioHitsMs + ","
                            + resumoBusca.tempoMedioMissesMs + ","
                            + resumoBusca.mediaComparacoesHits + ","
                            + resumoBusca.mediaComparacoesMisses + ","
                            + resumoInsercao.checksum;

                    System.out.println(linha);

                    if (arquivoAberto) {
                        escritorArquivo.println(linha);
                    }

                    ifunc++;
                }

                in++;
            }

            im++;
        }

        if (arquivoAberto) {
            escritorArquivo.close();
            System.err.println("Resultados salvos em: " + NOME_ARQUIVO_CSV);
        }
    }

    /**
     * Como nao podemos usar try-catch e todas as opcoes de escrita em arquivo
     * lancam excecoes verificadas, simplesmente informamos que os resultados
     * estao sendo impressos em STDOUT que pode ser redirecionado pelo usuario.
     */
    private static PrintWriter abrirArquivo(String nomeArquivo) {
        System.err.println("NOTA: Redirecione a saida para arquivo usando: java ExperimentosHash > " + nomeArquivo);
        return null;
    }
}
