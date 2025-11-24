# ExperimentosHash - Como executar e salvar resultados

## Compilação

```bash
javac ExperimentosHash.java
```

## Execução

### Ver resultados na tela

```bash
java ExperimentosHash
```

### Salvar resultados em arquivo CSV

#### No PowerShell (Windows):
```powershell
java ExperimentosHash | Out-File -FilePath "resultados_hash.csv" -Encoding UTF8
```

#### No CMD (Windows):
```cmd
java ExperimentosHash > resultados_hash.csv
```

#### No Linux/Mac:
```bash
java ExperimentosHash > resultados_hash.csv
```

## Resultado

O programa irá gerar um arquivo `resultados_hash.csv` contendo:
- Cabeçalho: `m,n,func,seed,ins_ms,coll_tbl,coll_lst,find_ms_hits,find_ms_misses,cmp_hits,cmp_misses,checksum`
- 27 linhas de dados (3 tamanhos de tabela × 3 tamanhos de conjunto × 3 funções hash)

## Nota

Como o código segue as restrições de Java básico (sem try-catch, sem APIs avançadas), o salvamento em arquivo é feito através de redirecionamento de saída padrão (STDOUT).

Link Youtube: https://youtu.be/qv9V6us852c