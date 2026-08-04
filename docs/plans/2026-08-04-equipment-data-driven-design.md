# 装备数值数据驱动化 — 设计文档(STR 表试点)

- 日期:2026-08-04
- 状态:已定稿,待实现
- 关联分支:`codex/debug-panel`
- 定位:个人开发工具链的第一步(数据驱动 + 热重载)

## 背景与目标

当前调装备数值需要"改 Java → 重新编译 → 重进游戏",效率低。目标是把装备数值外置到 JSON,支持热重载,实现"改文件 → 重载 → 当场验证"的闭环。

本试点**只外置 STR 需求表**,用来完整跑通数据驱动流水线(外部文件 → 加载 → 查表 → 回退 → 热重载 → 即时验证),验证可行后再扩展到伤害/DR 系数。

## 范围(试点)

- **外置**:各 tier 的基础 STR 需求(武器 + 护甲共用同一张表)
- **本期不做**(后续再扩展):
  - 升级减需的 sqrt 规则(STR 需求在 +1/+3/+6/+10 等级递减)
  - 武器 min/max 伤害公式系数
  - 护甲 DRMin/DRMax、evasionFactor 系数
  - 法杖充能与伤害
  - 不走 tier 的独有装备(SpiritBow、决斗者特殊武器等)

## 现状代码

STR 需求只有两处实现,公式相同:

`Weapon.java:360-365`:

```java
protected static int STRReq(int tier, int lvl){
    lvl = Math.max(0, lvl);
    //strength req decreases at +1,+3,+6,+10,etc.
    return (8 + tier * 2) - (int)(Math.sqrt(8 * lvl + 1) - 1)/2;
}
```

`Armor.java:698-703`:同形(`Math.round(tier * 2)`)。

调用链:`MeleeWeapon.STRReq(lvl)` / `Armor.STRReq(lvl)` → 各自的静态 `STRReq(tier, lvl)`。

关键特性:STRReq 在**物品信息面板渲染、装备/负重计算时动态调用**,不是存进物品实例——因此热重载后无需重建物品,下一次面板/计算立即生效。

## Schema

开发读取 `devdata/equipment.json`(桌面工作目录),发布回退 `assets/data/equipment.json`:

```json
{
  "version": 1,
  "strReq": { "1": 10, "2": 12, "3": 14, "4": 16, "5": 18 }
}
```

- tier 键缺失 → 该 tier 回退到公式值 `8 + tier*2`
- 数据是"公式系数",不是任意表达式:安全、可校验、贴合现有公式(YAGNI)

## 组件

新增 `com.shatteredpixel.shatteredpixeldungeon.data.EquipmentData`:

- **静态懒加载**:首次访问时定位并解析 JSON;`reload()` 重新读取
- **int strReqBase(int tier)**:查表,缺键回退 `8 + tier*2`
- **文件定位**:桌面工作目录 `devdata/equipment.json` 存在则用外部文件,否则用 classpath 的 `assets/data/equipment.json`
- **解析失败**:仅 GLog 警告 + 保留旧表(首次加载失败则全部用回退值),不影响游戏运行

## 改动点(共 2 处)

1. `Weapon.STRReq(tier, lvl)`:`(8 + tier * 2)` → `EquipmentData.strReqBase(tier)`
2. `Armor.STRReq(tier, lvl)`:同上

## Cheat 命令(扩展 CheatService)

- `eq`:打印当前生效的 STR 表(各 tier 的 lvl 0 需求 + 数据来源 外部/内置/回退)
- `reload`:重新读取外部文件,成功/失败均 GLog 反馈

## 数据流

```
devdata/equipment.json
      ↓ (或回退 assets/data/equipment.json)
EquipmentData.load() → Weapon/Armor.STRReq 查表
      ↓
物品信息面板 / 装备与负重计算
```

热重载路径:改 JSON → `reload` → 打开物品面板即见新值(无需重建物品/重开楼层)。

## 容错策略

- 外部文件不存在 → 用内置 asset;内置也不存在 → 全部回退硬编码
- JSON 非法 / 字段类型错误 → GLog 警告 + 保留旧表
- tier 键缺失 → 该 tier 回退公式值

## 验证与测试

1. **基线回归**:不放置 `devdata` 文件时,游戏内数值与现在完全一致
2. **造装备验证**:`give` 一把 tier 3 武器 → 物品面板 STR 需求应为 14(默认表)
3. **热重载**:把 `devdata` 中 tier 3 改为 13 → `reload` → 面板立即变 13
4. **容错**:写坏 JSON → `reload` → GLog 警告、表不变;改回 → `reload` 恢复

## 后续扩展路径(不在本期)

- 同一张表扩字段:武器 min/max 系数、护甲 DR/evasion 系数、法杖充能
- 敌人属性表(HP/攻防/经验/伤害)
- levelgen 参数表(房间数、刷怪表、藏宝密度)
- 无头批量验证器(headless runner)消费同一份 JSON,做平衡回归
