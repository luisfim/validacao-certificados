# Demonstração rápida

## Antes da aula

- Abra o projeto uma vez no computador que será usado. Não deixe a primeira instalação de Java, Docker ou Codespaces para a apresentação.
- Para uma apresentação sem dependências externas, use o JAR entregue e a aba **Texto de exemplo**.
- Para mostrar leitura de imagem, use Docker/Codespaces ou instale Python/OpenCV e Tesseract com português. Faça um upload antes da aula.
- Não é necessária chave de OpenAI. A opção de IA deve ficar desmarcada se não foi configurada e testada por você.
- Abra `docs/APRESENTACAO.md` em outra janela.

## Sequência sugerida

| Ação | O que mostrar / falar |
| --- | --- |
| Abrir a aplicação | O objetivo é fazer triagem de certificados acadêmicos. |
| Selecionar Ana Costa | O nome é comparado com um cadastro fixo de demonstração. |
| Exemplo Completo e Analisar | Campos organizados e aprovação na triagem. |
| Analisar novamente | A consulta ao banco identifica a repetição. |
| Exemplo Sem carga horária | O campo ausente fica pendente. |
| Exemplo Datas inconsistentes | Uma regra clara leva à rejeição. |
| Histórico > Ver | Dados e motivos ficam persistidos. |
| Abrir resposta JSON | A mesma informação da tela é oferecida pela API. |

Se usar upload, mostre o PNG/PDF completo **antes** do texto completo para obter a primeira aprovação. São o mesmo certificado, e o segundo envio será duplicado.

## Se algo falhar

- **Java não reconhecido:** instale Java 21 ou use Codespaces/Docker.
- **Porta 8080 ocupada:** feche a outra aplicação ou defina `PORT=8081` no ambiente antes de iniciar. No Docker, mude apenas a porta à esquerda em `compose.yaml` para `127.0.0.1:8081:8080`.
- **Erro de OCR/OpenCV:** use Texto de exemplo e explique que está demonstrando extração e regras sem OCR. Não diga que o arquivo foi lido se não foi.
- **Certificado completo aparece duplicado:** já há uma aprovação salva. Mostre-a no histórico ou reinicie com a pasta de dados limpa, conforme o README.
- **Campo não identificado no OCR:** mostre a pendência e o texto extraído. É uma limitação real do protótipo.
- **Sem internet:** o JAR e os exemplos por texto funcionam localmente. IA, primeiro build e criação de Codespaces precisam de rede.

## Subir no GitHub

Se já existe um repositório, abra o clone dele e copie estes arquivos para dentro, preservando a pasta `.git`. Isso mantém os commits antigos que realmente existem. Não crie outro histórico por cima.

Na raiz do repositório:

```bash
git status
git add .
git commit -m "feat: implementa MVP de validacao de certificados"
git push
```

O `.gitignore` exclui banco, chaves, ambiente Python, compilação e JAR de `bin/`. Confira `git status` antes do commit. Não use `git push --force`.

Para criar um repositório novo pelo navegador, suba os arquivos de código, `README.md`, `pom.xml`, `mvnw`, `mvnw.cmd` e as pastas de configuração `.mvn`, `.github` e `.devcontainer`, além de `src`, `scripts`, `exemplos` e `docs`. Não envie `bin`, `target`, `dados` ou `.env`.

Convidar o mentor: **Settings > Collaborators > Add people > KleberMeira**. Ele precisa aceitar o convite.

Não foram criados commits artificiais ou datas retroativas no pacote. O histórico de evolução deve refletir os commits reais.
