# 牡丹鹦鹉羽色图鉴

`parapet:lovebird` 有 **122 种**已命名外观。输入下面的指令即可生成一只紫罗兰欧帕费氏。把引号里的值替换为目录中的其他 ID，就能指定另一种羽色。

```mcfunction
/summon parapet:lovebird ~ ~ ~ {Plumage:"fischer_violet_opaline"}
```

[桃脸 45 色图鉴](../art/lovebird-peach.png) · [费氏 51 色图鉴](../art/lovebird-fischer.png) · [面罩 26 色图鉴](../art/lovebird-masked.png)

## 先看是哪一种牡丹

| 游戏中的物种 | 常见中文称呼 | 原始绿成鸟的识别线索 | 羽色数量 |
| --- | --- | --- | ---: |
| 桃脸牡丹 *Agapornis roseicollis* | 桃面、小鹦／小樱、面类 | 桃红色的脸延至喉部，浅角色喙，眼周羽毛紧贴眼缘 | 45 |
| 费氏牡丹 *Agapornis fischeri* | 费氏、费沙氏、头类 | 橙红脸过渡到金黄色喉胸，橄榄色后脑，白色裸眼圈 | 51 |
| 面罩牡丹 *Agapornis personatus* | 黄领、黑头、头类 | 深褐近黑的头罩与黄颈环之间界线清楚，同样有白色裸眼圈 | 26 |

“头类”这个叫法同时包括费氏和面罩。判断蓝色个体时还可以看头部，蓝费氏保留浅色脸，蓝面罩呈深色头罩、白色颈领。前缀 `peach_`、`fischer_`、`masked_` 分别锁定三种鸟。[协会的实鸟展览图册](https://www.abe-international.com/mutations/) · [桃脸成鸟与色型描述](https://www.agaporniden.be/Peach-faced%20lovebird.htm)

## 怎样配出新羽色

两只已驯服的成年牡丹离笼并取消停留后，各喂一份小麦、甜菜、西瓜或南瓜种子。它们会靠近求偶约 15 秒。亲鸟约 25 分钟后可再次配对，雏鸟正常成长约 100 分钟。幼鸟从出生就显示它长大后的羽色，与成鸟等大，身边偶尔落下白色羽粉。

| 双亲的显色 | 雏鸟能是什么颜色 |
| --- | --- |
| 显色 ID 不同 | 从全部 122 款中等概率抽取一个显色，每款 1/122。同组和跨组都适用，例如桃脸绿和费氏蓝可以配对。再按亲鸟性格权重选择一方，把它传出的一个颜色存作携带色。 |
| 显色 ID 相同 | 每位亲鸟从自己的显色和携带色中各以 1/2 概率传出一个，两项相同时固定传那个颜色。再按亲鸟性格决定哪一方的颜色显现，另一方成为携带色。若本组有双亲四个羽色位都没有的颜色，雏鸟还有 1/16 概率变出其中一种。 |

显色指眼前看到的花色，携带色能传给后代。主人蹲下空手右击鸟，可以查看性格、亲密度和与显色不同的携带色。大胆、温和、好奇和害羞亲鸟的显色权重分别为 4、3、2、1。异色牡丹的这组权重用于决定携带色来源。122 色均匀抽取是模组为收藏玩法设定的规则。[性格权重、携带色与成长说明](plumage-inheritance.md)

## 按商品名找羽色

| 想找的外观 | 可以从这个 ID 开始 | 看哪里 |
| --- | --- | --- |
| 绿桃、黄桃 | `peach_green`、`peach_lutino` | 绿桃红脸、绿身、蓝腰。黄桃同样保留暖色脸，身体鲜黄、红眼 |
| 荷兰蓝、白脸、海绿 | `peach_aqua`、`peach_turquoise`、`peach_seagreen` | 荷兰蓝的脸仍带暖色；白脸松石脸部更浅、胸腹偏蓝；海绿介于两者之间 |
| 墨系、双暗 | `peach_mauve`、`fischer_mauve`、`masked_olive` | 先确定基础色。双暗蓝系呈烟灰紫，绿系呈橄榄色。“墨”是深浅描述，选鸟时还需要物种和底色 |
| 紫罗兰 | `peach_violet`、`fischer_violet`、`masked_violet` | 在蓝系或松石系底色上呈现蓝紫羽色 |
| 派特、花桃 | `peach_green_pied`、`fischer_blue_pied` | 绿系呈不规则黄色斑，蓝系呈白斑。目录另列 `recessive_pied` 隐性派特 |
| 欧帕、欧泊、Opaline | `peach_green_opaline`、`peach_violet_opaline`、`fischer_blue_opaline` | 桃脸的脸色延展至头顶和后颈，形成整顶头色；绿系呈红头，松石系可呈浅色头 |
| 欧翼、Euwing | `fischer_blue_euwing`、`fischer_green_euwing_opaline` | 较深的翅与较浅的背腹形成对比，折翼时背上有 V 形区 |
| 美桂、澳桂 | `peach_green_cinnamon`、`peach_green_pallid` | 美桂的深色羽偏棕；澳桂是 Pallid 在部分市场的名称，翅羽更浅、偏灰 |
| 淡化、淡彩 | `peach_green_dilute`、`fischer_blue_dilute`、`fischer_blue_pastel` | 目录分别用 Dilute、Pastel 标注相应的浅色表现。各协会和商家的英文用词有差异 |
| 大理石纹、羽缘纹 | `peach_green_marbled`、`masked_blue_edged` | 翅上每片羽毛的中心和边缘形成规则的深浅纹 |
| 奶油、白化、黑眼白 | `peach_creamino`、`fischer_albino`、`fischer_blue_dec` | 奶油色可留淡黄。蓝系 Ino 显红眼白羽，蓝系 DEC 则保留深眼 |
| 净白头费氏、白头派特 | `fischer_blue_sable`、`fischer_blue_sable_pied` | Sable 是选育外观的商品名，头顶和后脑的深色减少。叠加派特后，翅背还会出现浅斑 |

在 [122 款完整目录](../src/main/resources/data/parapet/plumage/lovebird.json) 里搜索中文名、英文名或 ID 都能找到具体条目。每行的 `species` 是物种，`base` 是贴图使用的基础色系，`traits` 是该条目带有的花纹或减色特征。例如 `fischer_blue_dilute_euwing_opaline` 的条目同时记录淡化、欧翼和欧帕。商家有这一组合的实鸟记录。[Parrot Crown 牡丹目录](https://parrotcrown.com/product-category/available-birds-parrots/small-birds-parrots/lovebird/)

## 取名依据

目录收录育种协会分类、展览记录与商家实例中能辨识的外观。相同外观下的携带基因、不同血统和同义商品名共用一个 ID。不同地区商家持续推出新组合，122 款是这个版本可在游戏中选择的范围。

- 三份分物种的协会标准可用于核对基础色、暗因子和可展示的复合花色。[AGASSCOM 桃脸标准](https://agasscom.org/site/en/standards-and-nomenclatures/agasscom-nomenclature/group-1-agapornis-roseicollis/) · [费氏标准](https://agasscom.org/site/en/standards-and-nomenclatures/agasscom-nomenclature/group-3-agapornis-fischeri/) · [面罩标准](https://agasscom.org/site/en/standards-and-nomenclatures/agasscom-nomenclature/group-2-agapornis-personatus/)
- 不同协会对少数突变使用不同术语，尤以桃脸的澳桂、Pallid 和 Dilute 最容易混淆。目录中的英文名保留这些差异。[AGASSCOM 桃脸原始突变与别称](https://agasscom.org/site/en/standards-and-nomenclatures/original-mutations/group-1-agapornis-roseicollis/) · [ABE 育种术语](https://www.abe-international.com/genetics/)
- 费氏的半蓝、欧翼、淡彩派特与 Sable 均能找到实际繁育或商品记录。在白眼圈鸟的展会命名中，Blue1Blue2 可呈松石样外观。[BVA Masters 展会分类](https://bva-p.aobarcelos.pt/index.php/component/phocadownload/category/3-bva-masters/11-bva-masters-2025?Itemid=101&download=52%3Alista-classes-bva-masters-2025) · [Tampa Lovebirds 育种场](https://tampalovebirds.com/) · [Brighter Days Aviary 颜色目录](https://brighterdaysaviary.wixsite.com/mysite/pricing)
