# Validação da versão 1.0.1

Validação realizada em 17/11/2026 na instância real **Tensura Neo Otherworld 1.1.3c**, com:

- Minecraft 1.21.1;
- NeoForge 21.1.265 / FML 4.0.45;
- OneEnoughBlock 1.1.5;
- Tensura TNO 1.0.6.5;
- OneEnoughBlock X Tensura TNO Fix 1.0.1.

A execução foi iniciada pelo CurseForge, com os mods e as configurações da instância.
Os logs da execução confirmaram, em sequência:

1. Carregamento da versão 1.0.1 do mod de correção.
2. Abertura do menu principal e da seleção de mundos.
3. Ajuste do injector legado e aplicação de `ModernTagResolverMixin` ao `BlockReplacementCache`.
4. Recarga do cache de substituições e abertura da tela de criação de mundo.
5. Inicialização do servidor integrado e conclusão da geração da área inicial.
6. Entrada do jogador no mundo, sem repetição dos dois crashes corrigidos.

A validação cobre a inicialização, a criação e a entrada no mundo; não representa um teste prolongado de todas as funções do modpack.
Os logs brutos, dados da conta e arquivos do mundo não são publicados.

## Regressão automatizada

- Seis testes de compatibilidade, metadados e cache original do TNO aprovados.
- O harness reproduziu o crash original de assinatura sem a correção.
- O harness reforçado reproduziu o erro da versão 1.0.0 ao rejeitar leituras de bytecode não transformado, como o ModLauncher real.
- Com a API padrão do provedor de bytecode, a aplicação dos mixins foi aprovada sob a mesma restrição.
