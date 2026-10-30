# Vilas & Câmbio (VilaEconomy)

Mod de **Minecraft 26.3 para Fabric** que transforma as vilas em lugares vivos: dinheiro em
notas, carteira, banqueiros que dão missões, dez profissões novas, vilas por níveis e
vilas no Nether — além de mais trocas para os aldeões da vanilla e para os piglins.

> Versão: **1.0.0** · Minecraft **26.3** · Fabric Loader **0.20.1** · Fabric API **0.165.0+26.3** · Java **25**

O `.jar` pronto para jogar fica em [`releases/villaeconomy-1.0.0.jar`](releases/villaeconomy-1.0.0.jar)
(também publicado na aba **Releases** do repositório).

---

## 1. Instalação

1. Instale o **Fabric Loader 0.20.1+** para Minecraft **26.3** (instalador oficial do Fabric).
2. Coloque na pasta `mods`:
   - `fabric-api-0.165.0+26.3.jar`
   - `villaeconomy-1.0.0.jar`
3. Pronto. O mod funciona no **cliente** (texturas, render do aldeão do Nether) e em
   **servidor dedicado** (tudo o mais). Em servidor, apenas os jogadores com o mod instalado
   veem o aldeão do Nether corretamente — o resto do conteúdo é só de servidor.

---

## 2. Dinheiro: notas e carteira

A moeda **não é esmeralda**. Existem cinco notas:

| Nota | Valor | Como aparece |
|------|-------|--------------|
| Nota de $10 | 10 | trocas, saques, missões |
| Nota de $20 | 20 | trocas, saques |
| Nota de $50 | 50 | trocas, saques, saque em notas |
| Nota de $100 | 100 | trocas de nível alto, piglins |
| Nota de $200 | 200 | trocas de nível alto, piglins |

- **Carteira**: o dinheiro "digital" fica salvo no jogador (attachment persistente
  `villaeconomy:carteira`), então não se perde ao morrer.
- **Cédulas no inventário** são itens normais: podem ser guardadas em baú, trocadas entre
  jogadores e usadas nas trocas com aldeões.
- **Depósito**: `/banco depositar` (ou `/carteira`) converte todas as notas do inventário em
  saldo na carteira.
- **Saque**: `/banco sacar <valor>` entrega notas de 200/100/50/20/10 (sempre múltiplo de 10).
- **Trocar nota por nota maior por crafting**: 2× $10 → 1× $20, 5× $10 → 1× $50,
  2× $50 → 1× $100, 2× $100 → 1× $200 (sempre com o mesmo valor total).

### Como ganhar dinheiro

1. **Missões de banqueiro** (principal): recompensa direto na carteira, quanto mais difícil,
   maior o valor (de ~$90 até ~$1.300+).
2. **Banqueiro do Nether**: troca **1 ouro → 4 notas de $10** (uma das trocas de nível 1).
3. **Piglins**: ao jogar ouro para um piglin, além do loot normal agora podem cair
   **notas de $50, $100 e $200**, blocos de ouro, garra de netherita e maçã dourada.

### Loja do banco

`/banco loja` abre a loja, que cobra da carteira: ferro, carvão, esmeralda, ouro, diamante,
lápis, blaze rod, pérola do End, poção de cura e sucata de netherita
(`/banco comprar <item> [quantidade]`).

---

## 3. Profissões novas (10)

Cada profissão tem **posto de trabalho próprio (bloco craftável)** e **trocas próprias em
dinheiro, do nível 1 ao 5** (10 ofertas por profissão: 2 por nível, com desconto por reputação).

### Overworld

| Profissão | Posto de trabalho | Nível mínimo da vila |
|-----------|-------------------|----------------------|
| Banqueiro | Balcão do Banqueiro | 2 |
| Mineiro | Mesa de Mineração | 1 |
| Alquimista | Alambique | 1 |
| Joalheiro | Bancada do Joalheiro | 2 |
| Guarda | Posto de Guarda | 3 |
| Leiloeiro | Pódio de Leilão | 4 |
| Tesoureiro | Cofre do Tesoureiro | 5 |

### Nether

| Profissão | Posto de trabalho | Pagamento |
|-----------|-------------------|-----------|
| Banqueiro do Nether | Balcão do Nether | ouro (e devolve notas) |
| Ferreiro do Ouro | Forja de Ouro | ouro |
| Caçador de Relíquias | Totem de Relíquias | ouro |

Os blocos de trabalho são POIs reais (`PoiType`), então aldeões sem profissão **pegam a
profissão sozinhos** se o bloco estiver por perto — igual à vanilla.

### Mais trocas para os aldeões da vanilla

As trocas extras do mod são **injetadas nas tags de troca da vanilla**
(`minecraft:farmer/level_1`, `minecraft:librarian/level_2`, ...), então fazendeiros,
bibliotecários, clérigos, açougueiros, pastores, pescadores, ferreiros de ferramenta/arma,
armeiro, seleiro, pedreiro, cartógrafo e flecheiro passam a **vender itens por dinheiro**
(o custo é nota de $10 a $100, e não esmeralda).

---

## 4. Vilas por níveis (6 níveis)

O nível é calculado pelo número de aldeões num raio de 48 blocos e é recalculado a cada
5 segundos. Ao subir de nível, o jogador recebe uma mensagem e novas profissões são
liberadas automaticamente para moradores desempregados.

| Nível | Nome | Moradores | Desbloqueia |
|-------|------|-----------|-------------|
| 0 | Sem vila | < 3 | — |
| 1 | Acampamento | 3 | Mineiro, Alquimista |
| 2 | Povoado | 7 | **Banqueiro**, Joalheiro, **missões** |
| 3 | Vila | 12 | Guarda |
| 4 | Vila Grande | 18 | Leiloeiro |
| 5 | Cidade | 26 | Tesoureiro |
| 6 | Metrópole | 34 | tudo liberado (4 estrelas e recompensas maiores) |

`/vila` mostra nível, número de moradores, se existe banqueiro, quais profissões já existem
e quais são as próximas a serem liberadas.

**Toda vila com nível 2+ recebe um banqueiro**: se não existir nenhum, um morador
desempregado é promovido automaticamente.

---

## 5. Banqueiros e missões

- Cada vila tem **um banqueiro** (aldeão com a profissão `villaeconomy:banqueiro`, ou
  `villaeconomy:banqueiro_nether` no Nether).
- Perto do banqueiro (até 32 blocos) use `/missao` para ver o **quadro de missões**;
  clique em `[Aceitar]` para pegar uma.
- **Novas missões a cada 30 minutos** (1.800 segundos de tempo de jogo) por banqueiro;
  o tempo restante aparece no chat ("Troca em X min").
- Você pode manter até **5 missões ativas**. `/missao` lista as ativas, `/missao abandonar <n>`
  descarta.
- Três tipos de missão:
  - **Matar** (`MATAR`): zumbis, esqueletos, creepers, endermans, bruxas, blazes, piglins,
    wither skeletons... Conta automaticamente ao matar.
  - **Minerar** (`MINERAR`): carvão, ferro, redstone, lápis, diamante, nether quartz,
    debris ancestral... Conta automaticamente ao quebrar o bloco.
  - **Entregar** (`ENTREGAR`): podridão, ossos, linha, trigo, pólvora, lingotes, blaze rod,
    pérola do End, diamante, bloco de ouro... Use `/missao entregar <n>` com os itens no
    inventário.
- **Dificuldade 1 a 5** (★ a ★★★★★). Dificuldade maior = mais alvos + recompensa maior.
  A dificuldade máxima sorteada sobe junto com o nível da vila:
  `recompensa = base[dificuldade] × variação × 10`, com base 90 / 200 / 380 / 700 / 1300.
- Perto do banqueiro, **agachado + clique direito** abre o menu do banco (depósito, saques,
  loja, missões, vila).

---

## 6. Nether

- A entidade **Aldeão do Nether** (`villaeconomy:nether_villager`) é um aldeão de verdade:
  troca, reproduz, aceita profissão, tem banqueiro e missões. É imune ao fogo, usa sons de
  piglin e um tipo de aldeão próprio (`villaeconomy:nether`), com textura avermelhada.
- As **vilas do Nether** nascem via worldgen: feature `villaeconomy:nether_village`,
  colocada em todos os biomas do Nether (passo `surface_structures`, `rarity_filter` 1/80,
  altura 34–90). A vila tem praça de blackstone, muros com quatro portas, lanternas de alma,
  quatro barraquinhas com cogumelo brilhante, balcão do banqueiro, forja de ouro e totem de
  relíquias — já vem habitada por 1 banqueiro do Nether, ferreiros do ouro e caçadores de
  relíquias.
- Todas as trocas do Nether são **pagas em ouro** (lingotes, pepitas e blocos) e o banqueiro
  do Nether converte ouro em notas.

---

## 7. Piglins

Além do bate-papo normal com ouro, o pool de barganha dos piglins foi ampliado
(`minecraft:gameplay/piglin_bartering`): notas de $50/$100/$200, bloco de ouro, garra de
netherita, maçã dourada e lingotes de ouro. Continua sendo **ouro** que eles aceitam.

---

## 8. Comandos

| Comando | O que faz |
|---------|-----------|
| `/carteira` | mostra saldo, valor das notas no inventário e atalhos |
| `/banco` | menu do banco (depositar, sacar 100/500/1.000, loja, missões, vila) |
| `/banco saldo` | saldo da carteira |
| `/banco depositar` | converte todas as notas do inventário em saldo |
| `/banco sacar <valor>` | saca notas (múltiplo de 10, escolhe as notas automaticamente) |
| `/banco loja` | itens à venda |
| `/banco comprar <item> [qtd]` | compra com saldo da carteira |
| `/missao` | lista missões ativas + quadro do banqueiro mais próximo |
| `/missao aceitar <1-3>` | aceita uma missão do quadro |
| `/missao entregar <1-5>` | entrega os itens de uma missão de entrega |
| `/missao abandonar <1-5>` | abandona uma missão |
| `/vila` | nível da vila, moradores, profissões e próximos desbloqueios |

---

## 9. Receitas (principais)

- **Balcão do Banqueiro**: 3 ouro em cima, ouro + esmeralda no meio, 3 pedra lisa embaixo.
- **Mesa de Mineração**, **Alambique**, **Bancada do Joalheiro**, **Posto de Guarda**,
  **Pódio de Leilão**, **Cofre do Tesoureiro**, **Balcão do Nether**, **Forja de Ouro**,
  **Totem de Relíquias**: cada um com receita própria (veja `data/villaeconomy/recipe/`).
- **Subida de nota**: 2 notas iguais formam a nota do dobro do valor.

---

## 10. Compilando

```bash
./gradlew build          # gera build/libs/villaeconomy-1.0.0.jar
./gradlew runClient      # cliente de desenvolvimento
./gradlew runServer      # servidor de desenvolvimento
```

O CI do GitHub Actions (`.github/workflows/`) faz três coisas:

| Workflow | O que faz |
|----------|-----------|
| `build.yml` | compila, guarda o log em `tools/build-log.txt` e publica o jar em `releases/` |
| `server-test.yml` | sobe um servidor dedicado de verdade, com RCON, e valida blocos, entidades, NBT dos aldeões e comandos (`tools/rcon-results.txt`) |
| `api-dump.yml` | opcional: extrai assinaturas de classes do jar deobfuscado para `tools/dump/` (usado para conferir a API do 26.3) |

### Estrutura

```
src/main/java/com/jonas/villaeconomy/
├── VilaEconomy.java          # entrypoint comum
├── command/ModCommands.java  # /carteira /banco /missao /vila
├── economy/                  # notas, carteira, banco, interação com banqueiro
├── entity/                   # aldeão do Nether + registro da entidade
├── loot/                     # barganha dos piglins
├── mission/                  # missões, catálogo, quadro, progresso
├── registry/                 # itens, blocos, POIs, profissões, tipos, aba criativa
├── village/                  # níveis de vila, banqueiro automático, profissões
└── worldgen/                 # feature e colocação das vilas do Nether

src/client/java/...           # renderer do aldeão do Nether
src/main/resources/
├── assets/villaeconomy/      # texturas, modelos, lang (pt_br e en_us)
└── data/villaeconomy/        # receitas, loot tables, trocas (villager_trade/trade_set/tags), worldgen
```

Os recursos (texturas, modelos, trocas, receitas, worldgen, idiomas) são gerados pelo script
`tools/gen_resources.py` (`python3 tools/gen_resources.py`, requer `pillow`). As texturas base
da vanilla, quando presentes em `tools/dump/`, são usadas para gerar as variações das
profissões; sem elas o script mantém as texturas já existentes.

---

## 11. Licença

Código sob a licença do arquivo [LICENSE](LICENSE).
