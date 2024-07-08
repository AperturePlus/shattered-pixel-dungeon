/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2024 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.shatteredpixel.shatteredpixeldungeon;

import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Amok;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.AscensionChallenge;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Awareness;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Dread;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Light;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MagicalSight;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MindVision;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.RevealedArea;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Terror;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.huntress.SpiritHawk;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mimic;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Blacksmith;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Ghost;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Imp;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Wandmaker;
import com.shatteredpixel.shatteredpixeldungeon.items.special.Amulet;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.TalismanOfForesight;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.Potion;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.Ring;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.Scroll;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.MimicTooth;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfRegrowth;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfWarding;
import com.shatteredpixel.shatteredpixeldungeon.journal.Notes;
import com.shatteredpixel.shatteredpixeldungeon.levels.caves.CavesBossLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.caves.CavesLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.city.CityBossLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.city.CityLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.DeadEndLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.HallsBossLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.HallsLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.LastLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.MiningLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.prison.PrisonBossLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.prison.PrisonLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.RegularLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.SewerBossLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.SewerLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.LevelTransition;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.secret.SecretRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special.SpecialRoom;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.buttons.QuickSlotButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.Toolbar;
import com.shatteredpixel.shatteredpixeldungeon.utils.DungeonSeed;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndResurrect;
import com.watabou.noosa.Game;
import com.watabou.utils.BArray;
import com.watabou.utils.Bundlable;
import com.watabou.utils.Bundle;
import com.watabou.utils.FileUtils;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;
import com.watabou.utils.SparseArray;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.TimeZone;

public class Dungeon {

	/**
	 * 有限掉落物品枚举，用于记录游戏中具有限定数量的物品掉落情况。
	 * 每个枚举常量代表一种特定的物品，这种设计便于管理和追踪这些有限物品的生成次数和状态。
	 */
	public static enum LimitedDrops {
		//limited world drops
		STRENGTH_POTIONS,
		//canceled upgrade_scroll limit
		UPGRADE_SCROLLS,
		ARCANE_STYLI,
		ENCH_STONE,
		INT_STONE,
		TRINKET_CATA,
		LAB_ROOM, //actually a room, but logic is the same

		//Health potion sources
		//enemies
		SWARM_HP,
		NECRO_HP,
		BAT_HP,
		WARLOCK_HP,
		//Demon spawners are already limited in their spawnrate, no need to limit their health drops
		//alchemy
		COOKING_HP,
		BLANDFRUIT_SEED,

		//Other limited enemy drops
		SLIME_WEP,
		SKELE_WEP,
		THEIF_MISC,
		GUARD_ARM,
		SHAMAN_WAND,
		DM200_EQUIP,
		GOLEM_EQUIP,

		//containers
		VELVET_POUCH,
		SCROLL_HOLDER,
		POTION_BANDOLIER,
		MAGICAL_HOLSTER,

		//lore documents
		LORE_SEWERS,
		LORE_PRISON,
		LORE_CAVES,
		LORE_CITY,
		LORE_HALLS;

		public int count = 0;

		/**
 * 判断物品是否已被丢弃。
 *
 * 此方法用于确定物品是否被丢弃过，即物品的数量是否不是零。
 * 它并不直接暴露具体的数量值，而是仅提供布尔结果来指示物品的状态，
 * 这在一定程度上保护了数据的隐私性。
 *
 * @return boolean - 如果物品已被丢弃（数量不为0），则返回true；否则，返回false。
 */
	public boolean dropped(){
    // 检查物品是否被丢弃，通过判断数量是否不为0
    	return count != 0;
	}

		public void drop(){
			count = 1;
		}

		/**
		 * 重置所有限定掉落物的计数器。
		 * 该方法遍历所有的LimitedDrops枚举实例，并将它们的计数器重置为0。
		 * 这样做是为了确保每个掉落物在下一次掉落前都有一个公平的起始状态。
		 * 该方法设计为静态方法，可以通过类名直接调用，而不需要实例化对象。
		 */
		public static void reset(){
		    // 遍历所有的LimitedDrops枚举值
		    for (LimitedDrops lim : values()){
		        // 重置计数器
		        lim.count = 0;
		    }
		}

		/**
		 * 将LimitedDrops枚举中的所有条目存储到Bundle中。
		 * 此方法遍历LimitedDrops枚举的所有值，并将每个值的名称和计数存储到提供的Bundle中。
		 * 这样做的目的是为了在不同场景间传递和恢复特定于枚举的配置或状态。
		 *
		 * @param bundle 用于存储LimitedDrops枚举值名称和计数的Bundle对象。
		 */
		public static void store( Bundle bundle ){
		    // 遍历LimitedDrops枚举的所有值
		    for (LimitedDrops lim : values()){
		        // 将枚举值的名称作为键，计数作为值存储到Bundle中
		        bundle.put(lim.name(), lim.count);
		    }
		}

		/**
		 * 从给定的Bundle中恢复LimitedDrops枚举中每个项目的计数。
		 * 此方法用于在游戏的保存和加载过程中处理特定物品的限制掉落次数。
		 * 它首先检查Bundle中是否包含特定于每个LimitedDrops枚举值的键，
		 * 如果存在，则从Bundle中恢复计数；如果不存在，则将计数重置为0。
		 * 此外，对于旧版本的游戏存档，在特定条件下调整升级卷轴的计数，
		 * 以适应游戏机制的更新。
		 *
		 * @param bundle 用于恢复LimitedDrops枚举值计数的Bundle对象。
		 */
		public static void restore(Bundle bundle) {
		    // 遍历LimitedDrops枚举中的每个项目
		    for (LimitedDrops lim : values()) {
		        // 检查bundle中是否包含当前项目的名字
		        if (bundle.contains(lim.name())) {
		            // 如果包含，从bundle中获取计数并赋值给当前项目
		            lim.count = bundle.getInt(lim.name());
		        } else {
		            // 如果不包含，将计数重置为0
		            lim.count = 0;
		        }
		    }

		    // 仅在游戏版本低于v2.2.0且挑战了无卷轴挑战的情况下处理升级卷轴的特殊逻辑
		    //pre-v2.2.0 saves
		    if (Dungeon.version < 750 && Dungeon.isChallenged(Challenges.NO_SCROLLS) && UPGRADE_SCROLLS.count > 0) {
		        // 在保持总数不变的情况下，调整升级卷轴的可见计数，每两个只显示一个
		        // 这里的逻辑是将count增加自身值减1，实际上实现了显示计数的调整
		        UPGRADE_SCROLLS.count += UPGRADE_SCROLLS.count - 1;
		    }
		}

	}

	/**
	 * 定义了一个静态变量challenges，用于跟踪当前面临的挑战数量。
	 * 这个变量在游戏的多个地方可能会被访问，以便了解玩家所面临的挑战情况。
	 */
	public static int challenges;

	/**
	 * 定义了一个静态变量mobsToChampion，用于记录玩家需要击败的怪物数量才能挑战冠军。
	 * 这个变量帮助游戏管理怪物与冠军挑战之间的进度。
	 */
	public static int mobsToChampion;

	/**
	 * 定义了一个静态变量hero，用于表示当前玩家所控制的英雄角色。
	 * 这个变量在整个游戏中被广泛使用，以获取英雄的状态、能力等信息。
	 */
	public static Hero hero;

	/**
	 * 定义了一个静态变量level，用于表示当前玩家所处的等级。
	 * 通过这个变量，游戏可以调整难度、敌人强度等与等级相关的因素。
	 */
	public static Level level;

	/**
	 * 定义了一个静态变量quickslot，它是一个QuickSlot类的实例，用于快速访问和使用物品或技能。
	 * 这个变量使得玩家可以在游戏中快速使用常用物品或技能，而不需要进入背包或技能菜单。
	 */
	public static QuickSlot quickslot = new QuickSlot();

	/**
	 * 定义了一个静态变量depth，用于表示玩家当前所处的地下城深度。
	 * 地下城深度可能影响游戏难度、敌人类型、掉落物等，这个变量对于游戏进度管理至关重要。
	 */
	public static int depth;

	//determines path the hero is on. Current uses:
	// 0 is the default path
	// 1 is for quest sub-floors
	public static int branch;

	//keeps track of what levels the game should try to load instead of creating fresh
	// Stores the generated levels, as an ArrayList of Integer types.
	public static ArrayList<Integer> generatedLevels = new ArrayList<>();

	// 金币数量
	public static int gold;
	// 充能数量？
	public static int energy;

	// Set of chapters that have been unlocked.
	public static HashSet<Integer> chapters;

	// 存储掉落物品的稀疏数组
	public static SparseArray<ArrayList<Item>> droppedItems;

	// The initial version number when the game starts, only assigned once.
	//first variable is only assigned when game is started, second is updated every time game is saved
	public static int initialVersion;
	// The current version number, updated every time the game is saved.
	public static int version;

	// Flag indicating if daily rewards have been claimed.
	public static boolean daily;
	// Flag indicating if the daily replay has been used.
	public static boolean dailyReplay;
	// Custom seed text for generating game levels or scenarios.
	public static String customSeedText = "";
	// The seed used for generating game content, can be influenced by customSeedText.
	public static long seed;

/**
 * 初始化游戏环境与设置，准备新游戏运行。
 * 此方法设置游戏初始状态，包括版本信息、挑战设置、种子生成，
 * 并重置各种游戏统计信息和资源。对于开始新游戏或重置当前游戏状态至关重要。
 */
public static void init() {
    // 初始化版本信息及挑战设置
    initialVersion = version = Game.versionCode;
    challenges = SPDSettings.challenges();
    mobsToChampion = -1;

    // 根据是否为每日挑战或自定义种子设定游戏种子
    if (daily) {
        // 确保每日挑战种子不在用户可输入范围内
        seed = SPDSettings.lastDaily() + DungeonSeed.TOTAL_SEEDS;
        // 格式化上次每日挑战日期
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.ROOT);
        format.setTimeZone(TimeZone.getTimeZone("UTC"));
        customSeedText = format.format(new Date(SPDSettings.lastDaily()));
    } else if (!SPDSettings.customSeed().isEmpty()) {
        customSeedText = SPDSettings.customSeed();
        seed = DungeonSeed.convertFromText(customSeedText);
    } else {
        customSeedText = "";
        seed = DungeonSeed.randomSeed();
    }

    // 清理现有角色并重置新角色ID计数器
    Actor.clear();
    Actor.resetNextID();

    // 增加随机数生成器的偏移量以避免输出模式重复
    Random.pushGenerator(seed + 1);

    // 初始化卷轴标签、药水颜色、戒指宝石等游戏元素
    Scroll.initLabels();
    Potion.initColors();
    Ring.initGems();

    // 为本次运行准备特殊房间和秘密房间
    SpecialRoom.initForRun();
    SecretRoom.initForRun();

    // 完全重置关卡生成器
    Generator.fullReset();

    // 重置随机数生成器至默认状态
    Random.resetGenerators();

    // 重置游戏统计信息和笔记
    Statistics.reset();
    Notes.reset();

    // 重置快速栏及工具栏状态
    quickslot.reset();
    QuickSlotButton.reset();
    Toolbar.swappedQuickslots = false;

    // 初始化基础游戏状态，如深度、分支及资源数量
    depth = 1;
    branch = 0;
    generatedLevels.clear();

    gold = 0;
    energy = 0;

    droppedItems = new SparseArray<>();

    LimitedDrops.reset();

    chapters = new HashSet<>();

    // 重置各NPC的支线任务状态
    Ghost.Quest.reset();
    Wandmaker.Quest.reset();
    Blacksmith.Quest.reset();
    Imp.Quest.reset();

    // 创建新英雄并准备游戏
    hero = new Hero();
    hero.live();

    // 重置徽章
    Badges.reset();

    // 使用已选择的职业初始化英雄
    GamesInProgress.selectedClass.initHero(hero);
}

/**
 * 检查当前游戏是否启用了指定的挑战。
 *
 * @param mask 要检查的挑战掩码。
 * @return 如果挑战激活则返回true，否则返回false。
 */
public static boolean isChallenged( int mask ) {
    return (challenges & mask) != 0;
}

/**
 * 检查指定深度和分支的关卡是否已被生成。
 *
 * @param depth 关卡深度。
 * @param branch 关卡分支。
 * @return 如果关卡已生成则返回true，否则返回false。
 */
public static boolean levelHasBeenGenerated(int depth, int branch){
    return generatedLevels.contains(depth + 1000*branch);
}



	
	public static Level newLevel() {
		
		Dungeon.level = null;
		Actor.clear();
		
		Level level;
		if (branch == 0) {
			switch (depth) {
				case 1:
				case 2:
				case 3:
				case 4:
					level = new SewerLevel();
					break;
				case 5:
					level = new SewerBossLevel();
					break;
				case 6:
				case 7:
				case 8:
				case 9:
					level = new PrisonLevel();
					break;
				case 10:
					level = new PrisonBossLevel();
					break;
				case 11:
				case 12:
				case 13:
				case 14:
					level = new CavesLevel();
					break;
				case 15:
					level = new CavesBossLevel();
					break;
				case 16:
				case 17:
				case 18:
				case 19:
					level = new CityLevel();
					break;
				case 20:
					level = new CityBossLevel();
					break;
				case 21:
				case 22:
				case 23:
				case 24:
					level = new HallsLevel();
					break;
				case 25:
					level = new HallsBossLevel();
					break;
				case 26:
					level = new LastLevel();
					break;
				default:
					level = new DeadEndLevel();
			}
		} else if (branch == 1) {
			switch (depth) {
				case 11:
				case 12:
				case 13:
				case 14:
					level = new MiningLevel();
					break;
				default:
					level = new DeadEndLevel();
			}
		} else {
			level = new DeadEndLevel();
		}

		//dead end levels get cleared, don't count as generated
		if (!(level instanceof DeadEndLevel)){
			//this assumes that we will never have a depth value outside the range 0 to 999
			// or -500 to 499, etc.
			if (!generatedLevels.contains(depth + 1000*branch)) {
				generatedLevels.add(depth + 1000 * branch);
			}

			if (depth > Statistics.deepestFloor && branch == 0) {
				Statistics.deepestFloor = depth;

				if (Statistics.qualifiedForNoKilling) {
					Statistics.completedWithNoKilling = true;
				} else {
					Statistics.completedWithNoKilling = false;
				}
			}
		}

		Statistics.qualifiedForBossRemainsBadge = false;
		
		level.create();
		
		if (branch == 0) Statistics.qualifiedForNoKilling = !bossLevel();
		Statistics.qualifiedForBossChallengeBadge = false;
		
		return level;
	}
	
	public static void resetLevel() {
		
		Actor.clear();
		
		level.reset();
		switchLevel( level, level.entrance() );
	}

	public static long seedCurDepth(){
		return seedForDepth(depth, branch);
	}

	public static long seedForDepth(int depth, int branch){
		int lookAhead = depth;
		lookAhead += 30*branch; //Assumes depth is always 1-30, and branch is always 0 or higher

		Random.pushGenerator( seed );

			for (int i = 0; i < lookAhead; i ++) {
				Random.Long(); //we don't care about these values, just need to go through them
			}
			long result = Random.Long();

		Random.popGenerator();
		return result;
	}
	
	public static boolean shopOnLevel() {
		return depth == 6 || depth == 11 || depth == 16;
	}
	
	public static boolean bossLevel() {
		return bossLevel( depth );
	}
	
	public static boolean bossLevel( int depth ) {
		return depth == 5 || depth == 10 || depth == 15 || depth == 20 || depth == 25;
	}

	//value used for scaling of damage values and other effects.
	//is usually the dungeon depth, but can be set to 26 when ascending
	public static int scalingDepth(){
		if (Dungeon.hero != null && Dungeon.hero.buff(AscensionChallenge.class) != null){
			return 26;
		} else {
			return depth;
		}
	}

	public static boolean interfloorTeleportAllowed(){
		if (Dungeon.level.locked
				|| Dungeon.level instanceof MiningLevel
				|| (Dungeon.hero != null && Dungeon.hero.belongings.getItem(Amulet.class) != null)){
			return false;
		}
		return true;
	}
	
	public static void switchLevel( final Level level, int pos ) {

		//Position of -2 specifically means trying to place the hero the exit
		if (pos == -2){
			LevelTransition t = level.getTransition(LevelTransition.Type.REGULAR_EXIT);
			if (t != null) pos = t.cell();
		}

		//Place hero at the entrance if they are out of the map (often used for pox = -1)
		// or if they are in solid terrain (except in the mining level, where that happens normally)
		if (pos < 0 || pos >= level.length()
				|| (!(level instanceof MiningLevel) && !level.passable[pos] && !level.avoid[pos])){
			pos = level.getTransition(null).cell();
		}
		
		PathFinder.setMapSize(level.width(), level.height());
		
		Dungeon.level = level;
		hero.pos = pos;

		if (hero.buff(AscensionChallenge.class) != null){
			hero.buff(AscensionChallenge.class).onLevelSwitch();
		}

		Mob.restoreAllies( level, pos );

		Actor.init();

		level.addRespawner();
		
		for(Mob m : level.mobs){
			if (m.pos == hero.pos && !Char.hasProp(m, Char.Property.IMMOVABLE)){
				//displace mob
				for(int i : PathFinder.NEIGHBOURS8){
					if (Actor.findChar(m.pos+i) == null && level.passable[m.pos + i]){
						m.pos += i;
						break;
					}
				}
			}
		}
		
		Light light = hero.buff( Light.class );
		hero.viewDistance = light == null ? level.viewDistance : Math.max( Light.DISTANCE, level.viewDistance );
		
		hero.curAction = hero.lastAction = null;

		observe();
		try {
			saveAll();
		} catch (IOException e) {
			ShatteredPixelDungeon.reportException(e);
			/*This only catches IO errors. Yes, this means things can go wrong, and they can go wrong catastrophically.
			But when they do the user will get a nice 'report this issue' dialogue, and I can fix the bug.*/
		}
	}

	public static void dropToChasm( Item item ) {
		int depth = Dungeon.depth + 1;
		ArrayList<Item> dropped = Dungeon.droppedItems.get( depth );
		if (dropped == null) {
			Dungeon.droppedItems.put( depth, dropped = new ArrayList<>() );
		}
		dropped.add( item );
	}

	public static boolean posNeeded() {
		//2 POS each floor set
		int posLeftThisSet = 2 - (LimitedDrops.STRENGTH_POTIONS.count - (depth / 5) * 2);
		if (posLeftThisSet <= 0) return false;

		int floorThisSet = (depth % 5);

		//pos drops every two floors, (numbers 1-2, and 3-4) with a 50% chance for the earlier one each time.
		int targetPOSLeft = 2 - floorThisSet/2;
		if (floorThisSet % 2 == 1 && Random.Int(2) == 0) targetPOSLeft --;

		if (targetPOSLeft < posLeftThisSet) return true;
		else return false;

	}

	/**
	 * 判断当前深度是否需要收集升级卷轴（SOU）。
	 * 此方法根据当前深度及已收集的升级卷轴数量计算剩余所需SOU，
	 * 并通过概率计算决定是否应拾取SOU。
	 *
	 * @return boolean - 若需要SOU则返回true，否则返回false。
	 */
	public static boolean souNeeded() {
		/* 计算当前套装中剩余的SOU数量。每套装含3个SOU。*/
    /* 计算考虑了已收集的升级卷轴数量及随深度减少所需
	 * SOU 的情况，计算公式为：3 - (depth / 5) * 3。*/
		int souLeftThisSet;
	    souLeftThisSet = 3 - (LimitedDrops.UPGRADE_SCROLLS.count - (depth / 5) * 3);
	    /* If there are no more SOU needed in the current set, returns false. */
	    if (souLeftThisSet <= 0) return false;

	    /* Calculates the current floor number within the set. */
	    int floorThisSet = (depth % 5);
	    /* Determines if SOU should be picked up based on a probability. The probability is proportional to the number of remaining SOU and the number of floors left in the set. */
	    /*chance is floors left / scrolls left*/
	    return Random.Int(5 - floorThisSet) < souLeftThisSet;
	}
	
	public static boolean asNeeded() {
		//1 AS each floor set
		int asLeftThisSet = 1 - (LimitedDrops.ARCANE_STYLI.count - (depth / 5));
		if (asLeftThisSet <= 0) return false;

		int floorThisSet = (depth % 5);
		//chance is floors left / scrolls left
		return Random.Int(5 - floorThisSet) < asLeftThisSet;
	}

	public static boolean enchStoneNeeded(){
		//1 enchantment stone, spawns on chapter 2 or 3
		if (!LimitedDrops.ENCH_STONE.dropped()){
			int region = 1+depth/5;
			if (region > 1){
				int floorsVisited = depth - 5;
				if (floorsVisited > 4) floorsVisited--; //skip floor 10
				return Random.Int(9-floorsVisited) == 0; //1/8 chance each floor
			}
		}
		return false;
	}

	public static boolean intStoneNeeded(){
		//one stone on floors 1-3
		return depth < 5 && !LimitedDrops.INT_STONE.dropped() && Random.Int(4-depth) == 0;
	}

	public static boolean trinketCataNeeded(){
		//one trinket catalyst on floors 1-3
		return depth < 5 && !LimitedDrops.TRINKET_CATA.dropped() && Random.Int(4-depth) == 0;
	}

	public static boolean labRoomNeeded(){
		//one laboratory each floor set, in floor 3 or 4, 1/2 chance each floor
		int region = 1+depth/5;
		if (region > LimitedDrops.LAB_ROOM.count){
			int floorThisRegion = depth%5;
			if (floorThisRegion >= 4 || (floorThisRegion == 3 && Random.Int(2) == 0)){
				return true;
			}
		}
		return false;
	}

	// 1/4
	// 3/4 * 1/3 = 3/12 = 1/4
	// 3/4 * 2/3 * 1/2 = 6/24 = 1/4
	// 1/4

	private static final String INIT_VER	= "init_ver";
	private static final String VERSION		= "version";
	private static final String SEED		= "seed";
	private static final String CUSTOM_SEED	= "custom_seed";
	private static final String DAILY	    = "daily";
	private static final String DAILY_REPLAY= "daily_replay";
	private static final String CHALLENGES	= "challenges";
	private static final String MOBS_TO_CHAMPION	= "mobs_to_champion";
	private static final String HERO		= "hero";
	private static final String DEPTH		= "depth";
	private static final String BRANCH		= "branch";
	private static final String GENERATED_LEVELS    = "generated_levels";
	private static final String GOLD		= "gold";
	private static final String ENERGY		= "energy";
	private static final String DROPPED     = "dropped%d";
	private static final String PORTED      = "ported%d";
	private static final String LEVEL		= "level";
	private static final String LIMDROPS    = "limited_drops";
	private static final String CHAPTERS	= "chapters";
	private static final String QUESTS		= "quests";
	private static final String BADGES		= "badges";
	
	public static void saveGame( int save ) {
		try {
			Bundle bundle = new Bundle();

			bundle.put( INIT_VER, initialVersion );
			bundle.put( VERSION, version = Game.versionCode );
			bundle.put( SEED, seed );
			bundle.put( CUSTOM_SEED, customSeedText );
			bundle.put( DAILY, daily );
			bundle.put( DAILY_REPLAY, dailyReplay );
			bundle.put( CHALLENGES, challenges );
			bundle.put( MOBS_TO_CHAMPION, mobsToChampion );
			bundle.put( HERO, hero );
			bundle.put( DEPTH, depth );
			bundle.put( BRANCH, branch );

			bundle.put( GOLD, gold );
			bundle.put( ENERGY, energy );

			for (int d : droppedItems.keyArray()) {
				bundle.put(Messages.format(DROPPED, d), droppedItems.get(d));
			}

			quickslot.storePlaceholders( bundle );

			Bundle limDrops = new Bundle();
			LimitedDrops.store( limDrops );
			bundle.put ( LIMDROPS, limDrops );
			
			int count = 0;
			int ids[] = new int[chapters.size()];
			for (Integer id : chapters) {
				ids[count++] = id;
			}
			bundle.put( CHAPTERS, ids );
			
			Bundle quests = new Bundle();
			Ghost		.Quest.storeInBundle( quests );
			Wandmaker	.Quest.storeInBundle( quests );
			Blacksmith	.Quest.storeInBundle( quests );
			Imp			.Quest.storeInBundle( quests );
			bundle.put( QUESTS, quests );
			
			SpecialRoom.storeRoomsInBundle( bundle );
			SecretRoom.storeRoomsInBundle( bundle );
			
			Statistics.storeInBundle( bundle );
			Notes.storeInBundle( bundle );
			Generator.storeInBundle( bundle );

			int[] bundleArr = new int[generatedLevels.size()];
			for (int i = 0; i < generatedLevels.size(); i++){
				bundleArr[i] = generatedLevels.get(i);
			}
			bundle.put( GENERATED_LEVELS, bundleArr);
			
			Scroll.save( bundle );
			Potion.save( bundle );
			Ring.save( bundle );

			Actor.storeNextID( bundle );
			
			Bundle badges = new Bundle();
			Badges.saveLocal( badges );
			bundle.put( BADGES, badges );
			
			FileUtils.bundleToFile( GamesInProgress.gameFile(save), bundle);
			
		} catch (IOException e) {
			GamesInProgress.setUnknown( save );
			ShatteredPixelDungeon.reportException(e);
		}
	}
	
	public static void saveLevel( int save ) throws IOException {
		Bundle bundle = new Bundle();
		bundle.put( LEVEL, level );
		
		FileUtils.bundleToFile(GamesInProgress.depthFile( save, depth, branch ), bundle);
	}
	
	public static void saveAll() throws IOException {
		if (hero != null && (hero.isAlive() || WndResurrect.instance != null)) {
			
			Actor.fixTime();
			updateLevelExplored();
			saveGame( GamesInProgress.curSlot );
			saveLevel( GamesInProgress.curSlot );

			GamesInProgress.set( GamesInProgress.curSlot );

		}
	}
	
	public static void loadGame( int save ) throws IOException {
		loadGame( save, true );
	}
	
	public static void loadGame( int save, boolean fullLoad ) throws IOException {
		
		Bundle bundle = FileUtils.bundleFromFile( GamesInProgress.gameFile( save ) );

		//pre-1.3.0 saves
		if (bundle.contains(INIT_VER)){
			initialVersion = bundle.getInt( INIT_VER );
		} else {
			initialVersion = bundle.getInt( VERSION );
		}

		version = bundle.getInt( VERSION );

		seed = bundle.contains( SEED ) ? bundle.getLong( SEED ) : DungeonSeed.randomSeed();
		customSeedText = bundle.getString( CUSTOM_SEED );
		daily = bundle.getBoolean( DAILY );
		dailyReplay = bundle.getBoolean( DAILY_REPLAY );

		Actor.clear();
		Actor.restoreNextID( bundle );

		quickslot.reset();
		QuickSlotButton.reset();
		Toolbar.swappedQuickslots = false;

		Dungeon.challenges = bundle.getInt( CHALLENGES );
		Dungeon.mobsToChampion = bundle.getInt( MOBS_TO_CHAMPION );
		
		Dungeon.level = null;
		Dungeon.depth = -1;
		
		Scroll.restore( bundle );
		Potion.restore( bundle );
		Ring.restore( bundle );

		quickslot.restorePlaceholders( bundle );
		
		if (fullLoad) {
			
			LimitedDrops.restore( bundle.getBundle(LIMDROPS) );

			chapters = new HashSet<>();
			int ids[] = bundle.getIntArray( CHAPTERS );
			if (ids != null) {
				for (int id : ids) {
					chapters.add( id );
				}
			}
			
			Bundle quests = bundle.getBundle( QUESTS );
			if (!quests.isNull()) {
				Ghost.Quest.restoreFromBundle( quests );
				Wandmaker.Quest.restoreFromBundle( quests );
				Blacksmith.Quest.restoreFromBundle( quests );
				Imp.Quest.restoreFromBundle( quests );
			} else {
				Ghost.Quest.reset();
				Wandmaker.Quest.reset();
				Blacksmith.Quest.reset();
				Imp.Quest.reset();
			}
			
			SpecialRoom.restoreRoomsFromBundle(bundle);
			SecretRoom.restoreRoomsFromBundle(bundle);
		}
		
		Bundle badges = bundle.getBundle(BADGES);
		if (!badges.isNull()) {
			Badges.loadLocal( badges );
		} else {
			Badges.reset();
		}
		
		Notes.restoreFromBundle( bundle );
		
		hero = null;
		hero = (Hero)bundle.get( HERO );
		
		depth = bundle.getInt( DEPTH );
		branch = bundle.getInt( BRANCH );

		gold = bundle.getInt( GOLD );
		energy = bundle.getInt( ENERGY );

		Statistics.restoreFromBundle( bundle );
		Generator.restoreFromBundle( bundle );

		generatedLevels.clear();
		if (bundle.contains(GENERATED_LEVELS)){
			for (int i : bundle.getIntArray(GENERATED_LEVELS)){
				generatedLevels.add(i);
			}
		//pre-v2.1.1 saves
		} else  {
			for (int i = 1; i <= Statistics.deepestFloor; i++){
				generatedLevels.add(i);
			}
		}

		droppedItems = new SparseArray<>();
		for (int i=1; i <= 26; i++) {
			
			//dropped items
			ArrayList<Item> items = new ArrayList<>();
			if (bundle.contains(Messages.format( DROPPED, i )))
				for (Bundlable b : bundle.getCollection( Messages.format( DROPPED, i ) ) ) {
					items.add( (Item)b );
				}
			if (!items.isEmpty()) {
				droppedItems.put( i, items );
			}

		}
	}
	
	public static Level loadLevel( int save ) throws IOException {
		
		Dungeon.level = null;
		Actor.clear();

		Bundle bundle = FileUtils.bundleFromFile( GamesInProgress.depthFile( save, depth, branch ));

		Level level = (Level)bundle.get( LEVEL );

		if (level == null){
			throw new IOException();
		} else {
			return level;
		}
	}
	
	public static void deleteGame( int save, boolean deleteLevels ) {

		if (deleteLevels) {
			String folder = GamesInProgress.gameFolder(save);
			for (String file : FileUtils.filesInDir(folder)){
				if (file.contains("depth")){
					FileUtils.deleteFile(folder + "/" + file);
				}
			}
		}

		FileUtils.overwriteFile(GamesInProgress.gameFile(save), 1);
		
		GamesInProgress.delete( save );
	}
	
	public static void preview( GamesInProgress.Info info, Bundle bundle ) {
		info.depth = bundle.getInt( DEPTH );
		info.version = bundle.getInt( VERSION );
		info.challenges = bundle.getInt( CHALLENGES );
		info.seed = bundle.getLong( SEED );
		info.customSeed = bundle.getString( CUSTOM_SEED );
		info.daily = bundle.getBoolean( DAILY );
		info.dailyReplay = bundle.getBoolean( DAILY_REPLAY );

		Hero.preview( info, bundle.getBundle( HERO ) );
		Statistics.preview( info, bundle );
	}
	
	public static void fail( Object cause ) {
		if (WndResurrect.instance == null) {
			updateLevelExplored();
			Statistics.gameWon = false;
			Rankings.INSTANCE.submit( false, cause );
		}
	}
	
	public static void win( Object cause ) {

		updateLevelExplored();
		Statistics.gameWon = true;

		hero.belongings.identify();

		Rankings.INSTANCE.submit( true, cause );
	}

	public static void updateLevelExplored(){
		if (branch == 0 && level instanceof RegularLevel && !Dungeon.bossLevel()){
			Statistics.floorsExplored.put( depth, level.isLevelExplored(depth));
		}
	}

	//default to recomputing based on max hero vision, in case vision just shrank/grew
	public static void observe(){
		int dist = Math.max(Dungeon.hero.viewDistance, 8);
		dist *= 1f + 0.25f*Dungeon.hero.pointsInTalent(Talent.FARSIGHT);

		if (Dungeon.hero.buff(MagicalSight.class) != null){
			dist = Math.max( dist, MagicalSight.DISTANCE );
		}

		observe( dist+1 );
	}
	
	public static void observe( int dist ) {

		if (level == null) {
			return;
		}
		
		level.updateFieldOfView(hero, level.heroFOV);

		int x = hero.pos % level.width();
		int y = hero.pos / level.width();
	
		//left, right, top, bottom
		int l = Math.max( 0, x - dist );
		int r = Math.min( x + dist, level.width() - 1 );
		int t = Math.max( 0, y - dist );
		int b = Math.min( y + dist, level.height() - 1 );
	
		int width = r - l + 1;
		int height = b - t + 1;
		
		int pos = l + t * level.width();
	
		for (int i = t; i <= b; i++) {
			BArray.or( level.visited, level.heroFOV, pos, width, level.visited );
			pos+=level.width();
		}

		//always visit adjacent tiles, even if they aren't seen
		for (int i : PathFinder.NEIGHBOURS9){
			level.visited[hero.pos+i] = true;
		}
	
		GameScene.updateFog(l, t, width, height);

		boolean stealthyMimics = MimicTooth.stealthyMimics();
		if (hero.buff(MindVision.class) != null){
			for (Mob m : level.mobs.toArray(new Mob[0])){
				if (stealthyMimics && m instanceof Mimic && m.alignment == Char.Alignment.NEUTRAL){
					continue;
				}

				BArray.or( level.visited, level.heroFOV, m.pos - 1 - level.width(), 3, level.visited );
				BArray.or( level.visited, level.heroFOV, m.pos - 1, 3, level.visited );
				BArray.or( level.visited, level.heroFOV, m.pos - 1 + level.width(), 3, level.visited );
				//updates adjacent cells too
				GameScene.updateFog(m.pos, 2);
			}
		}

		if (hero.buff(Awareness.class) != null){
			for (Heap h : level.heaps.valueList()){
				BArray.or( level.visited, level.heroFOV, h.pos - 1 - level.width(), 3, level.visited );
				BArray.or( level.visited, level.heroFOV, h.pos - 1, 3, level.visited );
				BArray.or( level.visited, level.heroFOV, h.pos - 1 + level.width(), 3, level.visited );
				GameScene.updateFog(h.pos, 2);
			}
		}

		for (TalismanOfForesight.CharAwareness c : hero.buffs(TalismanOfForesight.CharAwareness.class)){
			Char ch = (Char) Actor.findById(c.charID);
			if (ch == null || !ch.isAlive()) continue;
			BArray.or( level.visited, level.heroFOV, ch.pos - 1 - level.width(), 3, level.visited );
			BArray.or( level.visited, level.heroFOV, ch.pos - 1, 3, level.visited );
			BArray.or( level.visited, level.heroFOV, ch.pos - 1 + level.width(), 3, level.visited );
			GameScene.updateFog(ch.pos, 2);
		}

		for (TalismanOfForesight.HeapAwareness h : hero.buffs(TalismanOfForesight.HeapAwareness.class)){
			if (Dungeon.depth != h.depth || Dungeon.branch != h.branch) continue;
			BArray.or( level.visited, level.heroFOV, h.pos - 1 - level.width(), 3, level.visited );
			BArray.or( level.visited, level.heroFOV, h.pos - 1, 3, level.visited );
			BArray.or( level.visited, level.heroFOV, h.pos - 1 + level.width(), 3, level.visited );
			GameScene.updateFog(h.pos, 2);
		}

		for (RevealedArea a : hero.buffs(RevealedArea.class)){
			if (Dungeon.depth != a.depth || Dungeon.branch != a.branch) continue;
			BArray.or( level.visited, level.heroFOV, a.pos - 1 - level.width(), 3, level.visited );
			BArray.or( level.visited, level.heroFOV, a.pos - 1, 3, level.visited );
			BArray.or( level.visited, level.heroFOV, a.pos - 1 + level.width(), 3, level.visited );
			GameScene.updateFog(a.pos, 2);
		}

		for (Char ch : Actor.chars()){
			if (ch instanceof WandOfWarding.Ward
					|| ch instanceof WandOfRegrowth.Lotus
					|| ch instanceof SpiritHawk.HawkAlly){
				x = ch.pos % level.width();
				y = ch.pos / level.width();

				//left, right, top, bottom
				dist = ch.viewDistance+1;
				l = Math.max( 0, x - dist );
				r = Math.min( x + dist, level.width() - 1 );
				t = Math.max( 0, y - dist );
				b = Math.min( y + dist, level.height() - 1 );

				width = r - l + 1;
				height = b - t + 1;

				pos = l + t * level.width();

				for (int i = t; i <= b; i++) {
					BArray.or( level.visited, level.heroFOV, pos, width, level.visited );
					pos+=level.width();
				}
				GameScene.updateFog(ch.pos, dist);
			}
		}

		GameScene.afterObserve();
	}

	//we store this to avoid having to re-allocate the array with each pathfind
	private static boolean[] passable;

	private static void setupPassable(){
		if (passable == null || passable.length != Dungeon.level.length())
			passable = new boolean[Dungeon.level.length()];
		else
			BArray.setFalse(passable);
	}

	public static boolean[] findPassable(Char ch, boolean[] pass, boolean[] vis, boolean chars){
		return findPassable(ch, pass, vis, chars, chars);
	}

	public static boolean[] findPassable(Char ch, boolean[] pass, boolean[] vis, boolean chars, boolean considerLarge){
		setupPassable();
		if (ch.flying || ch.buff( Amok.class ) != null) {
			BArray.or( pass, Dungeon.level.avoid, passable );
		} else {
			System.arraycopy( pass, 0, passable, 0, Dungeon.level.length() );
		}

		if (considerLarge && Char.hasProp(ch, Char.Property.LARGE)){
			BArray.and( passable, Dungeon.level.openSpace, passable );
		}

		ch.modifyPassable(passable);

		if (chars) {
			for (Char c : Actor.chars()) {
				if (vis[c.pos]) {
					passable[c.pos] = false;
				}
			}
		}

		return passable;
	}

	public static PathFinder.Path findPath(Char ch, int to, boolean[] pass, boolean[] vis, boolean chars) {

		return PathFinder.find( ch.pos, to, findPassable(ch, pass, vis, chars) );

	}
	
	public static int findStep(Char ch, int to, boolean[] pass, boolean[] visible, boolean chars ) {

		if (Dungeon.level.adjacent( ch.pos, to )) {
			return Actor.findChar( to ) == null && pass[to] ? to : -1;
		}

		return PathFinder.getStep( ch.pos, to, findPassable(ch, pass, visible, chars) );

	}
	
	public static int flee( Char ch, int from, boolean[] pass, boolean[] visible, boolean chars ) {
		boolean[] passable = findPassable(ch, pass, visible, false, true);
		passable[ch.pos] = true;

		//only consider other chars impassable if our retreat step may collide with them
		if (chars) {
			for (Char c : Actor.chars()) {
				if (c.pos == from || Dungeon.level.adjacent(c.pos, ch.pos)) {
					passable[c.pos] = false;
				}
			}
		}

		//chars affected by terror have a shorter lookahead and can't approach the fear source
		boolean canApproachFromPos = ch.buff(Terror.class) == null && ch.buff(Dread.class) == null;
		return PathFinder.getStepBack( ch.pos, from, canApproachFromPos ? 8 : 4, passable, canApproachFromPos );
		
	}

}
