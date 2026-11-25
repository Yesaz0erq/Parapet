# 鹦鹉栖息地、驯服与选笼

在主世界寻找野生鹦鹉时，先找表中的生物群系，再看落点。林鸟可以落在有光的树冠上，水边专性鸟还需要附近有地表水。表中每行对应一个可召唤的实体，命令格式为 `/summon parapet:<实体 ID> ~ ~ ~`。

**信物和食物有各自的用途。** 表内各物品都可以单独用来接近和尝试驯服该鸟。普通鸟粮和优质鸟粮通用于全部物种。游戏给三种吸蜜鹦鹉、黄头亚马逊和绯红金刚设置了可可豆信物。它只触发驯服互动，吸蜜鹦鹉平时吃西瓜片或甜浆果，其他物种按模组的种子食物规则取食。物种条目中的植物和食物描述指野外生态，表里的具体 Minecraft 作物是玩法设定。

**笼子尺寸按宽 × 深 × 高计算。** 普通笼为 1×1×1，大型笼为 2×1×2，豪华笼为 3×2×3，超级豪华笼和巨型笼均为 3×3×3。体型栏同时列出最低可用笼档。小型鸟可用全部五档，中型鸟从大型笼起，大型鸟从豪华笼起。成鸟长度以厘米计，沿用模组建模的喙尖至尾尖参考长度。长尾鸟的模型体长与笼档分别设定。

## 出现地点

自然生成与新区块生成均校验维度、物种生物群系和海洋标签。同一生物群系即使出现在自定义维度也不会自然生成本模组的鹦鹉。河面或海面不能作为落脚处。草地、泥土、树叶、天然原木和干露出的泥地都可供落脚，位置需要有足够空间，原始亮度至少为 9。干旱和近水物种还可以落在沙、沙砾或恶地陶瓦上，崖地物种也可以落在裸岩上。

标有“近水”的物种还需要落点四周约 12 格内有露天水体。举例来说，进入一片热带草原后，虎皮可以在干地成群出现。找蓝喉金刚要继续看近水林岛，那里还得位于它在本世界占用的极少数区域中。

## 一片适宜栖地能遇到多少只

原版先在当前生物群系的被动动物表中按权重选择物种，再从该鸟的群数范围随机选取本群的目标数量。游戏将主世界划成 128 × 128 格区域，按世界种子和鸟种分别决定哪些区域可供该种分布。一处落点要同时通过区域与栖地条件才能生成。进入新区域可以发现另一批当地鸟种。在同一区域反复等待不会重抽分布。

| 游戏遭遇档 | 物种在原版被动动物表中的权重 | 该物种可占用的区域比例 |
| --- | ---: | ---: |
| 常见 | 3 | 100% |
| 较常见 | 2 | 1/2 |
| 局部少见 | 1 | 1/4 |
| 稀有 | 1 | 1/12，约 8.33% |
| 极稀有 | 1 | 1/40，2.5% |
| 重引入模拟 | 1 | 1/192，约 0.52% |

区域比例是分布机会。若同一群系的候选权重总和为 W，权重 3 的鸟一次被选中的概率为 3/W。生成还受亮度、地表、空间和原版被动动物上限约束，所以表里“每群 4–8 只”的虎皮也可能只落下较少几只。成对觅食和大群夜栖是两种观察情境，游戏把目标群数设在 1–8 只之间，呈现可同时遇到的一小群。野外稀少的李尔金刚仍会随同伴出现。

六档是根据野外分布范围、当地相对丰度、专项保护资料和结群习性选择的玩法值。IUCN 等级衡量灭绝风险，野外遭遇还取决于地点。人工选育花色、商人种蛋价格和宠物市场的流行程度各用自己的规则。下表按实体列出设置与物种资料，核对日期为 2026 年 11 月 25 日。

## 常见伴侣鹦鹉

| 鸟种与实体 ID | 参考体长 | 体型 · 最低笼档 | 野外遭遇 · 每群目标 | 物种驯服信物 | 游戏生物群系 | 原生分布、群性与资料 |
| --- | ---: | --- | --- | --- | --- | --- |
| 虎皮鹦鹉 `budgerigar` | 18 | 小型 · 普通 | 常见 · 4–8 只 | 小麦种子、甜菜种子 | 热带草原、热带高原、平原 | 澳大利亚内陆的干草地、金合欢灌丛和河岸疏林，随种子与水源移动。遇到水源时可聚大群。 [物种资料](https://parrots.org/encyclopedia/budgerigar/) |
| 玄凤鹦鹉 `cockatiel` | 31 | 中型 · 大型 | 常见 · 3–7 只 | 小麦种子、西瓜种子 | 热带草原、热带高原、向日葵平原 | 澳大利亚半干旱内陆的草原、开阔林和农耕地。澳洲内陆常见，随季节和食物移动，常结群活动。 [物种资料](https://parrots.org/encyclopedia/cockatiel/) |
| 绿颊锥尾鹦鹉 `green_cheek_conure` | 26 | 中型 · 大型 | 较常见 · 3–5 只 | 南瓜种子、西瓜种子 | 稀疏丛林、热带高原 | 玻利维亚、巴西西南部至巴拉圭及阿根廷北部的低地、山麓林地和林缘。南美林冠中的社交鸟，繁殖期外常见十余只同群。 [物种资料](https://parrots.org/encyclopedia/green-cheeked-conure/) |
| 牡丹鹦鹉 `lovebird` | 15 | 小型 · 普通 | 较常见 · 3–6 只 | 小麦种子、甜菜种子 | 热带草原、热带高原、疏林恶地 | 桃脸来自非洲西南部干旱疏林。费氏与面罩原产坦桑尼亚的稀树草原和开阔林。桃脸、费氏与面罩野外结群，分布和受胁程度各自不同。 [桃脸](https://parrots.org/encyclopedia/rosy-faced-lovebird/)、[费氏](https://parrots.org/encyclopedia/fischers-lovebird/)、[面罩](https://parrots.org/encyclopedia/masked-lovebird/) |
| 和尚鹦鹉 `monk_parakeet` | 29 | 中型 · 大型 | 常见 · 3–7 只 | 小麦种子、南瓜种子 | 平原、向日葵平原、热带草原 | 南美洲中南部有散树的开阔平原、查科灌木地及农田。南美原生草地与林缘可见群居和公共巢。 [物种资料](https://parrots.org/encyclopedia/monk-parakeet/) |
| 凯克鹦鹉 `caique` | 23 | 中型 · 大型 | 局部少见 · 2–4 只 | 西瓜种子、南瓜种子 | 丛林、稀疏丛林 | 黑头型以亚马孙河以北的湿润林地为主，白腹类群在南岸的亚马孙低地林。两个凯克类群多以林冠小群活动，亚马孙河两岸分布有别。 [黑头](https://parrots.org/encyclopedia/black-headed-parrot/)、[白腹](https://parrots.org/encyclopedia/white-bellied-parrot/) |
| 巴布亚折衷鹦鹉 `eclectus` | 35 | 大型 · 豪华 | 较常见 · 1–3 只 | 西瓜种子、南瓜种子 | 丛林、红树林沼泽 | 新几内亚及周边岛群、澳洲东北等地的低地林、林缘与红树林。巴布亚林地可遇单鸟、成对鸟和小型取食群。 [物种资料](https://parrots.org/encyclopedia/papuan-eclectus/) |
| 太阳锥尾鹦鹉 `sun_conure` | 30 | 中型 · 大型 | 稀有 · 3–6 只 | 西瓜种子、南瓜种子 | 稀疏丛林、热带草原 | 南美洲圭亚那和巴西北部的热带稀树草原与林地交界。野外濒危、分布局限，遇到时仍常是一群。 [物种资料](https://parrots.org/encyclopedia/sun-conure/) |
| 葵花凤头鹦鹉 `sulfur_crested_cockatoo` | 50 | 大型 · 豪华 | 常见 · 3–7 只 | 小麦种子、南瓜种子 | 森林、稀疏丛林、平原 | 澳大利亚东部等地的开阔林地、林缘以及靠近树林的农地。澳洲开阔林地常见群体取食和集体夜栖。 [物种资料](https://parrots.org/encyclopedia/sulphur-crested-cockatoo/) |
| 非洲灰鹦鹉 `african_grey` | 30 / 33 | 中型 · 大型 | 稀有 · 2–5 只 | 南瓜种子、西瓜种子 | 丛林、稀疏丛林、红树林沼泽 | 刚果灰鹦鹉分布于西非至中非湿润森林带，提姆那灰鹦鹉集中在西非上几内亚低地林、林缘及部分红树林。刚果灰与提姆那灰均受捕捉、森林损失影响，集体夜栖后分小群觅食。 [灰鹦鹉](https://parrots.org/encyclopedia/grey-parrot/)、[提姆那](https://parrots.org/encyclopedia/timneh-parrot/) |

## 长翼、吸蜜、亚马逊与长尾鹦鹉

| 鸟种与实体 ID | 参考体长 | 体型 · 最低笼档 | 野外遭遇 · 每群目标 | 物种驯服信物 | 游戏生物群系 | 原生分布、群性与资料 |
| --- | ---: | --- | --- | --- | --- | --- |
| 贾丁氏鹦鹉 `jardines_parrot` | 28 | 中型 · 大型 | 局部少见 · 2–4 只 | 南瓜种子、甜菜种子 | 丛林、风袭森林 | 西非和中非低地原始林，以及肯尼亚与坦桑尼亚的山地林。非洲森林的分布不连续，在适合的林区成小群移动。 [物种资料](https://parrots.org/encyclopedia/red-fronted-parrot/) |
| 秋草鹦鹉 `bourkes_parrot` | 19 | 小型 · 普通 | 较常见 · 3–6 只 | 小麦种子、甜菜种子 | 热带高原、疏林恶地 | 澳大利亚内陆干燥的密枝金合欢灌丛和低矮疏林。澳洲干旱灌木地的游荡鸟，旱季水点可聚集较大群。 [物种资料](https://parrots.org/encyclopedia/bourkes-parrot/) |
| 彩虹吸蜜鹦鹉 `rainbow_lorikeet` | 26 | 中型 · 大型 | 常见 · 3–6 只 | 可可豆 | 繁花森林、稀疏丛林 | 澳大利亚东部多花木的林地、森林边缘及城市树冠。追随开花树木，以活跃的小群觅食，夜栖群可更大。 [物种资料](https://parrots.org/encyclopedia/rainbow-lorikeet/) |
| 红吸蜜鹦鹉 `red_lory` | 31 | 中型 · 大型 | 局部少见 · 3–6 只 | 可可豆 | 丛林、红树林沼泽 | 印度尼西亚马鲁古群岛的原生林、次生低地林及部分沿海红树林。原生范围限摩鹿加群岛部分岛屿，在当地花源附近可聚集。 [物种资料](https://parrots.org/encyclopedia/red-lory/) |
| 暗色吸蜜鹦鹉 `dusky_lory` | 25 | 中型 · 大型 | 较常见 · 4–7 只 | 可可豆 | 丛林、稀疏丛林 | 新几内亚低地至山麓的森林、次生林及有开花树的林缘。新几内亚的群居性吸蜜鹦鹉，花源引来活跃的群体。 [物种资料](https://parrots.org/encyclopedia/dusky-lory/) |
| 蓝额亚马逊鹦鹉 `blue_fronted_amazon` | 36 | 大型 · 豪华 | 较常见 · 2–4 只 | 南瓜种子、甜菜种子 | 热带草原、稀疏丛林 | 巴西内陆至玻利维亚、巴拉圭和阿根廷北部的季节性疏林和棕榈林地。南美分布局部较广，全球近危，可成对或成群。 [物种资料](https://parrots.org/encyclopedia/blue-fronted-amazon/) |
| 黄头亚马逊鹦鹉 `yellow_headed_amazon` | 37 | 大型 · 豪华 | 稀有 · 2–3 只 | 南瓜种子、可可豆 | 稀疏丛林、红树林沼泽 | 墨西哥至中美洲部分地区的热带落叶林、河岸林及部分沿海沼泽林。全球濒危，原生种群受栖地丧失与捕捉影响，常见成对活动。 [物种资料](https://parrots.org/encyclopedia/yellow-headed-amazon/) |
| 橙翅亚马逊鹦鹉 `orange_winged_amazon` | 31 | 大型 · 豪华 | 较常见 · 2–5 只 | 西瓜种子、南瓜种子 | 丛林、红树林沼泽、沼泽（近水） | 南美洲北部与东部的低地湿林、沿河林、泛滥林和棕榈沼泽。热带南美近水林地中，多对可合成松散群体。 [物种资料](https://parrots.org/encyclopedia/orange-winged-amazon/) |
| 月轮鹦鹉 `rose_ringed_parakeet` | 40 | 中型 · 大型 | 常见 · 3–7 只 | 小麦种子、西瓜种子 | 热带草原、稀疏丛林、向日葵平原 | 非洲和南亚的热带开阔林、散树农田与果园。本表采用原生环境。原生地的开阔林地和农地适应力强，经常集群夜栖。 [物种资料](https://parrots.org/encyclopedia/rose-ringed-parakeet/) |
| 东部玫瑰鹦鹉 `eastern_rosella` | 30 | 中型 · 大型 | 常见 · 2–4 只 | 小麦种子、甜菜种子 | 平原、森林、繁花森林 | 澳大利亚东南部与塔斯马尼亚的散树草地、桉树林缘和农牧地。澳洲开阔林地通常见成双或小群，繁殖期外可更多。 [物种资料](https://parrots.org/encyclopedia/eastern-rosella/) |
| 绯红玫瑰鹦鹉 `crimson_rosella` | 36 | 中型 · 大型 | 较常见 · 1–3 只 | 甜菜种子、西瓜种子 | 森林、原始桦木森林、风袭森林 | 澳大利亚东部、东南部湿润森林和山林。黄色地域型也利用较干内陆的河岸桉林。林地中成年鸟少成大群，幼鸟的聚群规模可更大。 [物种资料](https://parrots.org/encyclopedia/crimson-rosella/) |
| 塞内加尔鹦鹉 `senegal_parrot` | 23 | 中型 · 大型 | 较常见 · 2–4 只 | 小麦种子、南瓜种子 | 热带草原、热带高原 | 西非稀树草原、猴面包树疏林和保留散树的农地。西非稀树草原多成对，繁殖期外也会结群。 [物种资料](https://parrots.org/encyclopedia/senegal-parrot/) |

## 金刚鹦鹉

| 鸟种与实体 ID | 参考体长 | 体型 · 最低笼档 | 野外遭遇 · 每群目标 | 物种驯服信物 | 游戏生物群系 | 原生分布、群性与资料 |
| --- | ---: | --- | --- | --- | --- | --- |
| 蓝黄金刚鹦鹉 `blue_and_yellow_macaw` | 86 | 大型 · 豪华 | 局部少见 · 2–4 只 | 西瓜种子、南瓜种子 | 丛林、红树林沼泽、稀疏丛林（近水） | 南美热带低地林缘、季节性泛滥林和近水棕榈林。分布广但当地不一定常见，成对活动，亦在河岸集群。 [物种资料](https://parrots.org/encyclopedia/blue-and-yellow-macaw/) |
| 蓝喉金刚鹦鹉 `blue_throated_macaw` | 85 | 大型 · 豪华 | 极稀有 · 2–3 只 | 南瓜种子 | 热带草原、沼泽（近水） | 玻利维亚贝尼平原季节淹水的草原，借棕榈林岛取食与栖息。玻利维亚局域极危种，通常成对，特定地点可集体夜栖。 [物种资料](https://parrots.org/encyclopedia/blue-throated-macaw/) |
| 军金刚鹦鹉 `military_macaw` | 73 | 大型 · 豪华 | 稀有 · 2–4 只 | 南瓜种子、甜菜种子 | 风袭热带草原、疏林恶地 | 墨西哥至南美分散分布，常见于林木覆盖的山麓、干燥山谷与崖地。山麓和峡谷有间断种群，常见一对或小群。 [物种资料](https://parrots.org/encyclopedia/military-macaw/) |
| 大绿金刚鹦鹉 `great_green_macaw` | 81 | 大型 · 豪华 | 极稀有 · 2–3 只 | 南瓜种子 | 丛林 | 中美洲至南美西北部的大树低地雨林。部分地区尤其依赖山扁桃（*Dipteryx panamensis*）。全球极危，低地林中通常见成对或小群。 [物种资料](https://parrots.org/encyclopedia/great-green-macaw/) |
| 绯红金刚鹦鹉 `scarlet_macaw` | 85 | 大型 · 豪华 | 局部少见 · 2–4 只 | 可可豆、南瓜种子 | 丛林、稀疏丛林 | 墨西哥南部经中美洲至南美热带低地的湿润林，也进入季节林和沿河林。全球分布较广，部分历史分布地损失显著，森林内以家庭小群为代表。 [物种资料](https://parrots.org/encyclopedia/scarlet-macaw/) |
| 红绿金刚鹦鹉 `red_and_green_macaw` | 90 | 大型 · 豪华 | 局部少见 · 2–3 只 | 南瓜种子、西瓜种子 | 丛林、稀疏丛林 | 巴拿马东部与南美热带的低地常绿林、雨林和林带。南美低地林中常成对或小群取食。 [物种资料](https://parrots.org/encyclopedia/red-and-green-macaw/) |
| 红额金刚鹦鹉 `red_fronted_macaw` | 60 | 大型 · 豪华 | 极稀有 · 2–4 只 | 南瓜种子、小麦种子 | 恶地、疏林恶地、风袭热带草原 | 玻利维亚安第斯干热山谷，有仙人掌、落叶灌木以及可繁殖的崖壁。全球极危，活动集中在玻利维亚少数干旱峡谷，成对或小群。 [物种资料](https://parrots.org/encyclopedia/red-fronted-macaw/) |
| 栗额金刚鹦鹉 `chestnut_fronted_macaw` | 45 | 大型 · 豪华 | 较常见 · 2–4 只 | 西瓜种子、甜菜种子 | 稀疏丛林、红树林沼泽 | 中美洲南部与南美洲北部的低地次生林、林缘及棕榈沼泽。在分布区内较常见，通常两只或小群活动。 [物种资料](https://parrots.org/encyclopedia/chestnut-fronted-macaw/) |
| 紫蓝金刚鹦鹉 `hyacinth_macaw` | 95 | 大型 · 豪华 | 稀有 · 2–4 只 | 南瓜种子 | 热带草原、沼泽、稀疏丛林 | 南美热带内陆的潘塔纳尔湿地、巴西塞拉多疏林等棕榈丰富的地区。全球易危，适地可遇伴侣与家庭小群，资源分布局限。 [物种资料](https://parrots.org/encyclopedia/hyacinth-macaws/) |
| 靛蓝金刚鹦鹉 `lear_macaw` | 73 | 大型 · 豪华 | 极稀有 · 3–6 只 | 南瓜种子 | 风蚀恶地、疏林恶地 | 巴西巴伊亚州卡廷加刺灌丛，栖息于砂岩崖壁，依赖利库里棕榈果。全球濒危，巴伊亚特有。常集体栖崖并成群外出觅食。 [物种资料](https://parrots.org/encyclopedia/lears-macaw/) |
| 小蓝金刚鹦鹉 `spix_macaw` | 56 | 大型 · 豪华 | 重引入模拟 · 1–2 只 | 西瓜种子、小麦种子 | 热带草原、疏林恶地（近水） | 历史范围集中在巴西巴伊亚半干旱地带的季节性河道，两岸有卡拉伊巴树。官方仍列野外灭绝，存在人工繁育和原产地重引入项目。 [物种资料](https://parrots.org/encyclopedia/spixs-macaw/) |
| 红腹金刚鹦鹉 `red_bellied_macaw` | 46 | 大型 · 豪华 | 较常见 · 4–7 只 | 西瓜种子 | 沼泽、红树林沼泽（近水） | 热带南美季节性泛滥地与沼泽中的毛里榈林，是棕榈湿地专性很强的鸟。依赖毛里榈湿地，合适棕榈林中结群性强。 [物种资料](https://parrots.org/encyclopedia/red-bellied-macaw/) |
| 蓝头金刚鹦鹉 `blue_headed_macaw` | 41 | 中型 · 大型 | 稀有 · 2–3 只 | 西瓜种子、南瓜种子 | 丛林、稀疏丛林 | 秘鲁东部、玻利维亚西北部及巴西西端的西亚马孙低地林和安第斯山麓林缘。全球易危，西亚马孙地区多见双鸟或三只同行。 [物种资料](https://parrots.org/encyclopedia/blue-headed-macaw/) |
| 蓝翅金刚鹦鹉 `blue_winged_macaw` | 43 | 中型 · 大型 | 局部少见 · 2–3 只 | 南瓜种子、西瓜种子 | 稀疏丛林、热带草原 | 巴西、巴拉圭及阿根廷东北部历史分布地的热带、亚热带季节林和水边林缘。分布不连续，成对或小群，部分地区曾严重衰退。 [物种资料](https://parrots.org/encyclopedia/blue-winged-macaw/) |
| 黄领金刚鹦鹉 `yellow_collared_macaw` | 38 | 中型 · 大型 | 较常见 · 2–4 只 | 甜菜种子、南瓜种子 | 热带草原、热带高原 | 玻利维亚、巴西、巴拉圭和阿根廷北部的开阔林、查科灌丛以及季节林。南美中部常以伴侣或小群活动，在丰富食源附近可聚集。 [物种资料](https://parrots.org/encyclopedia/yellow-collared-macaw/) |
| 红肩金刚鹦鹉 `red_shouldered_macaw` | 30 | 中型 · 大型 | 较常见 · 3–6 只 | 甜菜种子、西瓜种子 | 热带草原、稀疏丛林、沼泽 | 南美洲北部和巴西的草原疏林、棕榈林与湿地林缘。草原及棕榈林群居，繁殖期外容易形成较大的群。 [物种资料](https://parrots.org/encyclopedia/red-shouldered-macaw/) |

## 几个容易估错的物种

- **斯皮克斯金刚。** 巴西 ICMBio 在 2026 年的公告中仍认定该鸟野外灭绝，同时记录人工繁育、放归及放归后繁殖。游戏保留在历史河岸林极少数地点遇到 1–2 只的机会，用来呈现重引入项目。可直接通过商人种蛋获得。[ICMBio 2026 年管理公告](https://www.gov.br/icmbio/pt-br/assuntos/noticias/icmbio-amplia-atuacao-na-emergencia-sanitaria-e-monitora-34-ararinhas-azuis-positivas-para-circovirus/) · [巴西国家物种行动计划](https://www.gov.br/icmbio/pt-br/assuntos/biodiversidade/pan/pan-ararinha-azul/pan-ararinha-azul/)
- **李尔金刚与蓝喉金刚。** 李尔局限于巴西巴伊亚少数地点，结群飞出砂岩崖壁觅食。蓝喉是玻利维亚贝尼的极危特有鸟，平日多成对活动。即使单个公共夜栖地点能统计到数百只，这个数字也不能外推到普通水边林地。两者均用极稀有档，前者一群目标 3–6 只，后者 2–3 只。[巴伊亚州野外普查](https://www.ba.gov.br/comunicacao/2024/09/noticias/inema-participa-do-censo-populacional-da-arara-azul-de-lear) · [Armonía 蓝喉种群普查](https://armoniabolivia.org/second-census-of-the-blue-throated-macaw-starts-in-the-llanos-de-moxos/)
- **黄头亚马逊与太阳锥尾。** 两者在原产地均受到捕捉压力。伯利兹 2025 年《国家红色名录》把黄头亚马逊的全球等级列为濒危，本国等级列为极危。圭亚那的太阳锥尾野外调查显示其历史分布已收缩，幸存种群仍会结群活动。游戏均给稀有档，但太阳锥尾每群目标可达 6 只。[伯利兹林业部门红色名录](https://forest.gov.bz/wp-content/uploads/2025/08/Belize-National-Red-List-2025-1-3.pdf) · [WPT 太阳锥尾野外调查](https://parrots.org/projects/sun-conure-surveys/)
- **红额与蓝翅金刚。** 玻利维亚红额金刚极危，保育团队在有限的安第斯河谷开展全国普查。蓝翅金刚的 2025 年 IUCN 评估为无危，但其分布区过去经历局部衰退，适宜季节林中按局部少见档出现。[红额金刚全国普查](https://dev.armoniabolivia.org/national-population-census-of-the-red-fronted-macaw-in-bolivia/) · [蓝翅金刚 2025 年 IUCN 评估](https://doi.org/10.2305/IUCN.UK.2025-2.RLTS.T22685606A234776590.en)

## 地域型和现实分布的对应方式

原版世界没有澳大利亚、坦桑尼亚或亚马孙河这样的固定地理边界。游戏用温湿度、林冠疏密和地形近似真实栖息地，例如用风袭森林表示贾丁氏鹦鹉使用的非洲山地林，用疏林恶地表示半干旱木本灌丛。鸟种可在相似地点相遇，图鉴所列分布指现实原生范围。[贾丁氏鹦鹉](https://parrots.org/encyclopedia/red-fronted-parrot/) · [红额金刚鹦鹉](https://parrots.org/encyclopedia/red-fronted-macaw/)

- 灰鹦鹉和凯克鹦鹉的自然群体按世界种子划分约 256×256 格地域。同一地域里的灰鹦鹉取刚果或提姆那型，凯克取黑头或白腹型。现实中的两类灰鹦鹉分布于不同的非洲森林带。黑头和白腹凯克则分布于亚马孙流域的不同区域。[刚果灰鹦鹉](https://parrots.org/encyclopedia/grey-parrot/) · [提姆那灰鹦鹉](https://parrots.org/encyclopedia/timneh-parrot/) · [黑头凯克](https://parrots.org/encyclopedia/black-headed-parrot/) · [白腹凯克](https://parrots.org/encyclopedia/white-bellied-parrot/)
- 牡丹在疏林恶地选桃脸。在热带草原和热带高原按地域选桃脸、费氏或面罩。自然群体的大多数个体采用本种野生绿色原色，另有八分之一的机会从该物种全部收录羽色中抽取一款，便于在生存模式收集。其他鸟的人工色型和地域羽色沿用各自的羽色池。[牡丹羽色名录](lovebird-colors.md)

生存种蛋、创造刷怪蛋、指令和繁殖均可在自然分布区之外生成鸟。刷新规则只作用于新生成的野生群体，已在世界中的鸟继续生活。金刚鹦鹉的学名和识别特征见[金刚图鉴](macaws.md)，十二种伴侣鹦鹉的性别和羽色命令见[伴侣鹦鹉图鉴](companion-parrots.md)。
