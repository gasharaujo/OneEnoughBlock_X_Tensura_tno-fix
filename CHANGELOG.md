# Changelog

## 1.0.1

- Corrige o crash na preparação do mixin causado por solicitar bytecode não transformado, uma operação não suportada pelo ModLauncher.
- Usa a API padrão do provedor de bytecode para inspecionar a compatibilidade.
- O harness de regressão agora rejeita leituras não transformadas, reproduzindo essa restrição real da plataforma.
- Autor e dependências sem limite máximo permanecem iguais.

## 1.0.0

- Restringe o injector antigo de tags do TNO ao descritor completo de dois argumentos.
- Adiciona a adaptação da sobrecarga de um argumento, usando o cache original do TNO.
- Preserva os handlers originais, a publicação das regras e as recargas.
- Inclui MixinSquared 0.3.8 no JAR.
- Autor: gasharaujo. Dependências não Minecraft sem versão máxima.

Validado localmente com Minecraft 1.21.1, OneEnoughBlock 1.1.5 e Tensura TNO 1.0.6.5:
seis testes aprovados, reprodução do crash no Mixin original e aplicação bem-sucedida
da correção pelo motor Sponge Mixin 0.8.7. A validação não iniciou um mundo do jogo.
