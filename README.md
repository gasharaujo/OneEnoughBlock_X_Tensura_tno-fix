# OneEnoughBlock X Tensura TNO Fix

Mod de compatibilidade para **Minecraft 1.21.1 / NeoForge**, por **gasharaujo**.

Corrige o crash `Invalid descriptor` do mixin
`com.tensura_tno.mixin.client.OneEnoughBlockReplacementCacheMixin` ao usar
**OneEnoughBlock 1.1.5** com **Tensura TNO 1.0.6.5**.

## Instalação

1. Mantenha OneEnoughBlock, OneEnoughItem, Tensura TNO e as dependências normais deles.
2. Coloque `oneenoughblock-tensura-tno-fix-1.0.1.jar` na pasta `mods`.
3. Reinicie o jogo. Em servidores, instale a correção também no servidor.

MixinSquared 0.3.8 está incluído no JAR; não é necessário baixá-lo separadamente.
As dependências de mods e do loader não têm versão máxima. Minecraft fica restrito a 1.21.1.
Versões futuras não ficam bloqueadas por metadados, mas isso não garante compatibilidade
com qualquer mudança de API.

## Como a correção funciona

O injector antigo do TNO seleciona `resolveTargetByTags` apenas pelo nome, mas seu
callback exige os parâmetros `Block, RegistryLookup`. O OneEnoughBlock instalado
oferece as sobrecargas de um e dois argumentos, e o Mixin tenta aplicar o mesmo callback a ambas. A assinatura de um argumento é rejeitada mesmo com
`require = 0`.

O mod usa o [annotation adjuster do MixinSquared](https://github.com/Bawnorton/MixinSquared/wiki/Mixin-Annotation-Adjuster)
para restringir **somente esse injector antigo** ao descritor completo de dois argumentos.
Se a sobrecarga antiga não existir, o injector opcional é ignorado corretamente.
Um mixin com a assinatura atual encaminha a consulta ao próprio
`OneEnoughBlockCompat.CACHE.resolveByTags(block, null)` do TNO. O valor nulo usa
o caminho de tags do holder interno que o TNO já oferece.

Os demais injectors do TNO continuam ativos, incluindo publicação das regras,
cache direto e invalidação nas recargas. Nenhum JAR original é modificado.
A adaptação moderna só é ativada quando a inspeção de bytecode encontra a combinação afetada.

## Compilação

Requer JDK 21. O Gradle Wrapper baixa as dependências públicas.

```powershell
.\gradlew.bat clean build
```

Saída: `build/libs/oneenoughblock-tensura-tno-fix-1.0.1.jar`.
O projeto não precisa dos JARs proprietários dos mods para compilar.

Para executar também a regressão contra os JARs instalados:

```powershell
.\gradlew.bat test "-PtensuraJar=C:\caminho\tensura_tno-1.0.6.5.jar" "-PoneEnoughBlockJar=C:\caminho\OneEnoughBlock-neoforge-1.21.1-1.1.5.jar"
```

Esses testes verificam as assinaturas reais e executam o cache original do TNO
para testar tags, regras ausentes, bloco nulo e recarga de regras.
Sem os caminhos, os dois testes de integração são pulados; os demais testes continuam.
Os JARs dos mods e os mundos do usuário não são incluídos no repositório.

## Licença

MIT. MixinSquared é de Bawnorton e também usa a licença MIT; sua licença está
preservada dentro do JAR incorporado. Este projeto não redistribui código do
OneEnoughBlock ou do Tensura TNO.

## Verificação com o motor Mixin real

Além dos testes acima, forneça o JAR oficial com nomes Mojang do Minecraft e execute:

```powershell
.\gradlew.bat build "-PtensuraJar=C:\caminho\tensura_tno-1.0.6.5.jar" "-PoneEnoughBlockJar=C:\caminho\OneEnoughBlock-neoforge-1.21.1-1.1.5.jar" "-PminecraftJar=C:\caminho\client-1.21.1-official.jar"
```

O harness usa os arquivos originais como dados e aplica o motor Sponge Mixin real.
Ele exige que a execução sem correção reproduza o erro de assinatura e que a execução
corrigida tenha sucesso, mantendo os handlers originais do TNO e o adapter moderno.
Essa verificação não abre o jogo nem carrega ou altera mundos.

## Teste no modpack

A versão **1.0.1** foi validada na instância real **Tensura Neo Otherworld 1.1.3c**, incluindo abertura do menu, criação e entrada no mundo. Veja [VALIDATION.md](VALIDATION.md) para o escopo e as versões testadas.
