# Apresentação oral — 5 minutos, com demonstração

**Sem slides.** Deixe a aplicação aberta no navegador e use a própria tela como apoio. O tempo inclui a fala e os cliques: aproximadamente 4 minutos falando e 1 minuto para interagir e aguardar os resultados. Ensaie uma vez; a duração varia com seu ritmo.

O roteiro explica aprendizados demonstráveis no projeto. Use a primeira pessoa apenas para conceitos que você revisou e compreendeu; não é necessário inventar dificuldades ou experiências.

## Antes de começar (fora dos cinco minutos)

1. Extraia o pacote completo. Com **Java 21** instalado, abra `iniciar.bat` no Windows e mantenha o terminal aberto. Entre em **http://localhost:8080**. Se o pacote inclui `bin/validacao-certificados.jar`, não é necessário compilar.
2. Selecione **Ana Costa**, mantenha **Texto de exemplo** e deixe a IA desmarcada. Essa demonstração não precisa de chave, internet, Python ou Tesseract. Ela testa extração local, regras e banco; não executa OCR.
3. Clique em **Completo** e troque o código no fim do texto por um identificador que você ainda não usou, por exemplo `APRESENTACAO-25-NOV-01`. Não clique em analisar ainda. Se ensaiar com esse código, use outro na apresentação.
4. Deixe este roteiro em outra tela ou impresso. Não precisa lê-lo palavra por palavra.

## 0:00–0:45 — O problema

“Meu projeto é um sistema de triagem de certificados de atividades complementares. Pensei na situação de quem precisa conferir vários certificados e verificar se o nome está certo, se a carga horária foi informada, se as datas fazem sentido e se aquele documento já foi enviado.

A proposta é fazer essa primeira conferência e guardar o resultado. O sistema não concede horas automaticamente e não garante que o certificado seja verdadeiro. Ele organiza as informações e mostra os motivos pelos quais um documento passou, ficou pendente ou foi rejeitado.”

## 0:45–1:30 — O funcionamento e o primeiro aprendizado

“Uma coisa que ficou mais clara para mim foi a diferença entre ler um documento e decidir se ele atende a uma regra. No envio de imagem ou PDF, o OpenCV prepara a imagem e o Tesseract tenta ler o texto. Esse processo é o OCR.

Depois vem a organização dos campos. O projeto tem uma extração local por rótulos e uma integração opcional com IA. Mas a decisão segue regras escritas em Java, no backend com Spring Boot. Nesta demonstração eu vou usar o texto de exemplo, sem IA, para mostrar as regras e o histórico sem depender desses serviços.”

## 1:30–2:15 — Demonstração: aprovação e duplicidade

**Clique em “Analisar certificado”, com o exemplo completo preparado. Mostre o resultado e os campos.**

“Escolhi uma aluna fictícia, a Ana Costa. Aqui aparecem o nome, a atividade, a instituição, as horas e as datas. Como as informações estão presentes e atendem às regras, o resultado é aprovado na triagem.”

**Sem alterar o texto, clique novamente em “Analisar certificado”. Mostre o motivo da rejeição.**

“Agora vou enviar exatamente o mesmo certificado. Ele é rejeitado por duplicidade. Isso mostra por que salvar os dados não serve só para montar uma lista: o histórico também ajuda a decidir sobre os próximos envios.”

## 2:15–3:05 — Demonstração: pendência e rejeição

**Clique em “Sem carga horária” e depois em “Analisar certificado”.**

“Neste exemplo falta a carga horária. O resultado fica pendente. Aprendi a separar falta de informação de uma regra que foi descumprida: o sistema não deve inventar quantas horas a atividade teve. Alguém precisa revisar.”

**Clique em “Datas inconsistentes” e depois em “Analisar certificado”. Mostre o motivo.**

“Já neste outro exemplo, a emissão está antes do término da atividade. Existe uma contradição nos dados, então a regra rejeita o certificado. Além do status, eu quis mostrar o motivo, porque só dizer aprovado ou rejeitado não ajudaria muito quem vai conferir.”

## 3:05–4:15 — O que aprendi sobre API, banco e limites

**Role até “Histórico” e abra os detalhes de uma análise. Se houver tempo, mostre “Abrir resposta JSON”.**

“Os resultados ficam guardados em um banco H2 em arquivo. Isso permite consultar a análise novamente e manter o histórico quando a aplicação é reiniciada. A tela conversa com uma API: envia os dados e recebe uma resposta estruturada em JSON.

Outro conceito que entendi melhor é que uma requisição funcionar não significa que o certificado foi aprovado. A API pode criar a análise com sucesso e retornar um certificado rejeitado, porque são duas coisas diferentes: processar o pedido e atender às regras do negócio.

Também fica claro o limite da automação. OCR pode ler um campo errado e a IA também pode errar. Por isso, não basta extrair um texto e confiar em tudo que apareceu. É preciso validar os campos e deixar explícito quando falta informação. O projeto tem testes para situações como dados ausentes, datas e duplicidade.”

## 4:15–5:00 — Pendência real e fechamento

“A parte que ficou pendente foi uma tela para a coordenação registrar a decisão final. Hoje o fluxo termina na triagem. O próximo passo seria permitir que um responsável revisasse o resultado e salvasse a decisão com uma justificativa.

O principal aprendizado foi conseguir enxergar o caminho completo: receber uma entrada, organizar os dados, aplicar regras, salvar e mostrar um resultado compreensível. Também aprendi que um protótipo precisa ter um escopo definido. Nesta versão, preferi trabalhar com um modelo simples de certificado e reconhecer os limites do que foi implementado.”

## Respostas curtas, caso perguntem

- **Usou IA para desenvolver?** Sim, houve assistência de IA para gerar, revisar e testar código e documentação.
- **A IA dentro da aplicação foi testada com uma chave real?** Não. O contrato HTTP foi testado com resposta simulada. A demonstração usa o extrator local.
- **Isso demonstrou OCR?** Não: o modo texto demonstra extração local e validação. O OCR funciona no envio de arquivo quando OpenCV e Tesseract estão instalados; houve verificação real com os exemplos sintéticos fornecidos.
- **Quantos testes?** A verificação registrada em 19/11 teve 23 testes automatizados, além dos testes de OCR e persistência. Veja `VERIFICACAO.md`.
- **É um regulamento oficial da faculdade?** Não. São regras propostas para o exercício.
- **Por que H2?** Para simplificar a execução local e manter o histórico em arquivo.

## Se algo falhar ao vivo

- **Completo aparece como duplicado logo no primeiro envio:** troque o campo `Codigo` por um identificador novo e envie novamente. Depois reenvie o mesmo texto para mostrar a duplicidade.
- **Upload informa que OCR está indisponível:** volte ao modo texto e explique que as dependências do OCR não estão instaladas nesse computador. Não apresente o modo texto como OCR.
- **A página não abre:** verifique o terminal e a instalação do Java 21 antes de começar. Não dependa de instalar programas durante a apresentação.
- **Está passando de cinco minutos:** pule a abertura do JSON e encurte o trecho sobre API; preserve os três resultados e a pendência final.
