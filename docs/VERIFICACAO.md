# Verificação da entrega

Verificações realizadas em 19/11/2026.

## Testes automatizados Java

23 testes passaram, sem falhas ou erros:

- 10 testes de regras: campos, nome, limites de datas, datas impossíveis, emissão futura, ordem das datas e carga horária.
- 9 testes de API e banco: gravação/consulta, duplicidade por hash e por código, possível repetição sem código, reenvio corrigido, entrada inválida, IA sem chave, arquivo falso e PDF com duas páginas.
- 4 testes de extração: campos ausentes, rótulos repetidos, contrato HTTP da IA com resposta simulada e JSON inválido.

## Execução do JAR e OCR real

A aplicação foi compilada e iniciada com Java 21. Foram enviados os arquivos de exemplo pela API:

| Verificação | Resultado |
| --- | --- |
| PNG completo, OpenCV e Tesseract português | Aprovado na triagem; campos extraídos |
| PDF completo após aprovação do mesmo certificado em PNG | Rejeitado por duplicidade de emissor/código |
| PDF sem carga horária | Pendente |
| PNG com emissão anterior ao término | Rejeitado pela regra de datas |
| Arquivo de texto disfarçado de PNG | HTTP 415 |
| Upload maior que 5 MB | HTTP 413 |
| Encerrar e reiniciar o JAR usando o mesmo banco | Histórico preservado |

## Interface

JavaScript passou na verificação de sintaxe. As interações foram verificadas em DOM simulado com jsdom, conectado à API real: carregamento de alunos e exemplos, análise, duplicidade, pendência, histórico, troca de abas e mensagem de arquivo ausente.

Não houve inspeção visual em navegador: o ambiente de verificação bloqueou a abertura do Chromium. Essa limitação não afetou os testes de API, OCR ou persistência.

## Limites desta verificação

- Não foi usada uma chave real da OpenAI. Os testes da integração simulam o servidor HTTP; não medem a qualidade do modelo remoto.
- Docker, Codespaces e o script Windows foram preparados, mas não executados neste ambiente. O JAR foi executado em Linux.
- O OCR foi verificado com os documentos sintéticos fornecidos, sem afirmar suporte a qualquer layout ou foto.
- Os testes Java não dependem de OCR instalado. A verificação real do OCR foi uma execução separada com OpenCV 4.11 e Tesseract em português.
