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
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Belongings;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.Bag;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.Potion;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.CorpseDust;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.Ring;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.Scroll;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.Trinket;
import com.shatteredpixel.shatteredpixeldungeon.journal.Notes;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.buttons.QuickSlotButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.Toolbar;
import com.shatteredpixel.shatteredpixeldungeon.utils.DungeonSeed;
import com.watabou.noosa.Game;
import com.watabou.utils.Bundlable;
import com.watabou.utils.Bundle;
import com.watabou.utils.FileUtils;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum Rankings {
	
	INSTANCE;
	
	public static final int TABLE_SIZE	= 11;
	
	public static final String RANKINGS_FILE = "rankings.dat";
	
	public ArrayList<Record> records;
	public int lastRecord;
	public int totalNumber;
	public int wonNumber;

	//The number of runs which are only present locally, not in the cloud
	public int localTotal;
	public int localWon;

	public Record latestDaily;
	public Record latestDailyReplay = null; //not stored, only meant to be temp
	public LinkedHashMap<Long, Integer> dailyScoreHistory = new LinkedHashMap<>();

	/**
	 * 提交游戏结果，包括胜利状态和失败原因。
	 * 此方法用于记录游戏的结束状态，包括版本、日期、胜利状态、英雄信息、游戏难度等。
	 * 如果是每日挑战，还会记录每日挑战的得分和重玩状态。
	 *
	 * @param win 表示游戏是否胜利。
	 * @param cause 游戏失败的原因，可以是类实例或其类。
	 */
	public void submit( boolean win, Object cause ) {
	    // 加载游戏数据
	    load();

	    // 创建一个新的记录对象来存储游戏结果
	    Record rec = new Record();

	    // 提取并格式化游戏版本号
	    //we trim version to just the numbers, ignoring alpha/beta, etc.
	    Pattern p = Pattern.compile("\\d+\\.\\d+\\.\\d+");
	    Matcher m = p.matcher(ShatteredPixelDungeon.version);
	    if (m.find()) {
	        rec.version = "v" + m.group();
	    } else {
	        rec.version = "";
	    }

	    // 格式化当前日期
	    DateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.ROOT);
	    rec.date = format.format(new Date(Game.realTime));

	    // 根据cause的类型，设置记录的失败原因类
	    rec.cause = cause instanceof Class ? (Class)cause : cause.getClass();
	    // 设置胜利状态
	    rec.win = win;
	    // 设置英雄类别和装备等级
	    rec.heroClass = Dungeon.hero.heroClass;
	    rec.armorTier = Dungeon.hero.tier();
	    rec.herolevel = Dungeon.hero.lvl;
	    // 设置游戏深度和是否正在上升
	    if (Statistics.highestAscent == 0){
	        rec.depth = Statistics.deepestFloor;
	        rec.ascending = false;
	    } else {
	        rec.depth = Statistics.highestAscent;
	        rec.ascending = true;
	    }
	    // 计算得分并设置
	    rec.score = calculateScore();
	    // 设置自定义种子和每日挑战状态
	    rec.customSeed = Dungeon.customSeedText;
	    rec.daily = Dungeon.daily;

	    // 验证高分记录
	    Badges.validateHighScore( rec.score );

	    // 保存游戏数据
	    INSTANCE.saveGameData(rec);

	    // 生成唯一的游戏ID
	    rec.gameID = UUID.randomUUID().toString();

	    // 处理每日挑战的特殊情况
	    if (rec.daily){
	        // 如果是每日重玩，则更新最新的每日重玩记录
	        if (Dungeon.dailyReplay){
	            latestDailyReplay = rec;
	            return;
	        }

	        // 更新最新的每日挑战记录和得分历史
	        latestDaily = rec;
	        if (Dungeon.seed <= DungeonSeed.TOTAL_SEEDS) {
	            dailyScoreHistory.put(Dungeon.seed, rec.score);
	        } else {
	            dailyScoreHistory.put(Dungeon.seed - DungeonSeed.TOTAL_SEEDS, rec.score);
	        }
	        // 保存当前每日挑战记录
	        save();
	        return;
	    }

	    // 将记录添加到历史记录列表，并根据得分排序
	    records.add( rec );
	    Collections.sort( records, scoreComparator );

	    // 更新最新的记录位置，并保持记录列表长度不超过预设大小
	    lastRecord = records.indexOf( rec );
	    int size = records.size();
	    while (size > TABLE_SIZE) {
	        if (lastRecord == size - 1) {
	            records.remove( size - 2 );
	            lastRecord--;
	        } else {
	            records.remove( size - 1 );
	        }
	        size = records.size();
	    }

	    // 更新总的遊戲數量和勝利數量
	    if (rec.customSeed.isEmpty()) {
	        totalNumber++;
	        if (win) {
	            wonNumber++;
	        }
	    }

	    // 驗證遊戲次數以更新徽章狀態
	    Badges.validateGamesPlayed();

	    // 保存更新后的遊戲记录
	    save();
	}

	/**
	 * 计算游戏得分。
	 * 根据玩家是否获胜，以及玩家收集的黄金和等级，计算游戏得分。
	 * 获胜时得分会得到额外的加成，且深度越深，得分加成越高。
	 *
	 * @param win 表示玩家是否获胜，如果获胜，得分会得到加成。
	 * @return 返回计算得到的游戏得分。
	 */
	private int score(boolean win) {
	    // 计算得分公式
	    // 如果获胜，得分基于收集的黄金、英雄等级和地牢深度进行计算
	    // 如果未获胜，得分只基于收集的黄金和英雄等级
	    return (Statistics.goldCollected + Dungeon.hero.lvl * (win ? 26 : Dungeon.depth) * 100) * (win ? 2 : 1);
	}

	/**
 * 计算玩家分数，基于游戏进度、宝藏、探索、击败首领、完成任务以及胜败条件。
 * 不同版本的游戏采用不同的计分规则。
 *
 * @return 根据计分规则计算出的总分数。
 */

// 假设已加载排名信息，或游戏即将结束
public int calculateScore(){
    // 计算适用于版本1.3.0及以后的分数

    if (Dungeon.initialVersion > ShatteredPixelDungeon.v1_2_3){
        // 进度分数：英雄等级与最深楼层乘积，限制最大值为50000
        Statistics.progressScore = Dungeon.hero.lvl * Statistics.deepestFloor * 65;
        Statistics.progressScore = Math.min(Statistics.progressScore, 50_000);

        // 计算携带物品价值，考虑尸粉特殊条件
        if (Statistics.heldItemValue == 0) {
            for (Item i : Dungeon.hero.belongings) {
                Statistics.heldItemValue += i.value();
                // 若玩家保留尸粉且到达第10层，给予额外任务分数
                if (i instanceof CorpseDust && Statistics.deepestFloor >= 10){
                    Statistics.questScores[1] = 2000;
                }
            }
        }
        // 宝藏分数：收集金币与携带物品价值之和，限制最大值为20000
        Statistics.treasureScore = Statistics.goldCollected + Statistics.heldItemValue;
        Statistics.treasureScore = Math.min(Statistics.treasureScore, 20_000);

        // 探索分数：每层楼面探索奖励
        Statistics.exploreScore = 0;
        int scorePerFloor = Statistics.floorsExplored.size * 50;
        for (Boolean b : Statistics.floorsExplored.valueList()){
            if (b) Statistics.exploreScore += scorePerFloor;
        }

        // 总首领分数：所有击败首领的分数之和
        Statistics.totalBossScore = 0;
        for (int i : Statistics.bossScores){
            if (i > 0) Statistics.totalBossScore += i;
        }

        // 总任务分数：所有完成任务的分数之和
        Statistics.totalQuestScore = 0;
        for (int i : Statistics.questScores){
            if (i > 0) Statistics.totalQuestScore += i;
        }

        // 胜利倍数：根据胜利条件和升天状态调整
        Statistics.winMultiplier = 1f;
        if (Statistics.gameWon)         Statistics.winMultiplier += 1f;
        if (Statistics.ascended)        Statistics.winMultiplier += 0.5f;

    // 版本1.3.0之前的运行有不同的分数计算方式
    // 只有进度和宝藏分数，并且各自可能比现在大50%
    // 胜利倍数在胜利时简单地加倍，挑战倍数与1.3.0相同
    } else {
        Statistics.progressScore = Dungeon.hero.lvl * Statistics.deepestFloor * 100;
        Statistics.treasureScore = Math.min(Statistics.goldCollected, 30_000);

        // 其他分数初始化为零
        Statistics.exploreScore = Statistics.totalBossScore = Statistics.totalQuestScore = 0;

        // 胜利倍数：如果游戏胜利则为2倍，否则为1倍
        Statistics.winMultiplier = Statistics.gameWon ? 2 : 1;
    }

    // 挑战倍数：根据激活的挑战数量计算
    Statistics.chalMultiplier = (float)Math.pow(1.25, Challenges.activeChallenges());
    Statistics.chalMultiplier = Math.round(Statistics.chalMultiplier*20f)/20f;

    // 总分数：所有分数之和
    Statistics.totalScore = Statistics.progressScore + Statistics.treasureScore + Statistics.exploreScore
                 + Statistics.totalBossScore + Statistics.totalQuestScore;

    // 总分数乘以胜利倍数和挑战倍数
    Statistics.totalScore *= Statistics.winMultiplier * Statistics.chalMultiplier;

    // 返回总分数
    return Statistics.totalScore;
}


	public static final String HERO         = "hero";
	public static final String STATS        = "stats";
	public static final String BADGES       = "badges";
	public static final String HANDLERS     = "handlers";
	public static final String CHALLENGES   = "challenges";
	public static final String GAME_VERSION = "game_version";
	public static final String SEED         = "seed";
	public static final String CUSTOM_SEED	= "custom_seed";
	public static final String DAILY	    = "daily";
	public static final String DAILY_REPLAY	= "daily_replay";

	/**
 * 保存当前游戏数据。
 * 此方法负责将各种类型的游戏数据打包到 Bundle 中，包括英雄信息、装备、统计信息、徽章以及挑战状态等。
 * 它确保了当游戏恢复或加载时，玩家的进度和状态能够被正确地还原。
 *
 * @param rec 记录对象，用于存储游戏数据。
 */
public void saveGameData(Record rec){
    // 如果英雄不存在，则直接返回空的数据记录
    if (Dungeon.hero == null){
        rec.gameData = null;
        return;
    }

    // 创建一个新的 Bundle 来存储游戏数据
    rec.gameData = new Bundle();

    // 获取英雄及其所有物品
    Belongings belongings = Dungeon.hero.belongings;

    // 保存英雄及其物品
    ArrayList<Item> allItems = (ArrayList<Item>) belongings.backpack.items.clone();

    // 移除不会在排行榜屏幕上显示的物品
    for (Item item : belongings.backpack.items.toArray( new Item[0])) {
        if (item instanceof Bag){
            for (Item bagItem : ((Bag) item).items.toArray( new Item[0])){
                if (Dungeon.quickslot.contains(bagItem)
                        && !Dungeon.quickslot.contains(item)){
                    belongings.backpack.items.add(bagItem);
                }
            }
        }
        // 移除非饰品且不在快捷栏中的物品
        if (!(item instanceof Trinket) && !Dungeon.quickslot.contains(item)) {
            belongings.backpack.items.remove(item);
        }
    }

    // 清除所有增益效果（与装备绑定的增益将在重新加载时重新应用）
    for(Buff b : Dungeon.hero.buffs()){
        Dungeon.hero.remove(b);
    }

    // 将英雄信息存入 Bundle
    rec.gameData.put( HERO, Dungeon.hero );

    // 保存统计信息
    Bundle stats = new Bundle();
    Statistics.storeInBundle(stats);
    rec.gameData.put( STATS, stats);

    // 保存徽章信息
    Bundle badges = new Bundle();
    Badges.saveLocal(badges);
    rec.gameData.put( BADGES, badges);

    // 保存处理器信息，如卷轴和药水的状态
    Bundle handler = new Bundle();
    Scroll.saveSelectively(handler, belongings.backpack.items);
    Potion.saveSelectively(handler, belongings.backpack.items);
    // 包含可能佩戴的戒指
    if (belongings.misc != null)        belongings.backpack.items.add(belongings.misc);
    if (belongings.ring != null)        belongings.backpack.items.add(belongings.ring);
    Ring.saveSelectively(handler, belongings.backpack.items);
    rec.gameData.put( HANDLERS, handler);

    // 在保存后恢复物品列表
    belongings.backpack.items = allItems;

    // 保存挑战状态
    rec.gameData.put( CHALLENGES, Dungeon.challenges );

    // 存储游戏版本
    rec.gameData.put( GAME_VERSION, Dungeon.initialVersion );

    // 存储种子信息
    rec.gameData.put( SEED, Dungeon.seed );
    rec.gameData.put( CUSTOM_SEED, Dungeon.customSeedText );
    rec.gameData.put( DAILY, Dungeon.daily );
    rec.gameData.put( DAILY_REPLAY, Dungeon.dailyReplay );
}


	/**
	 * 加载游戏数据。
	 * 该方法用于从记录对象中恢复游戏状态，包括英雄、道具、统计数据等。
	 * 如果记录中的游戏数据为空，则不进行任何操作。
	 *
	 * @param rec 记录对象，包含要加载的游戏数据。
	 */
	public void loadGameData(Record rec){
	    // 尝试从记录中获取游戏数据包
	    Bundle data = rec.gameData;

	    // 清理现有游戏状态，为加载新数据做准备
	    Actor.clear();
	    Dungeon.hero = null;
	    Dungeon.level = null;
	    Generator.fullReset();
	    Notes.reset();
	    Dungeon.quickslot.reset();
	    QuickSlotButton.reset();
	    Toolbar.swappedQuickslots = false;

	    // 如果游戏数据为空，则直接返回，不进行加载
	    if (data == null) return;

	    // 从游戏数据中恢复特定类型的物品
	    Bundle handler = data.getBundle(HANDLERS);
	    Scroll.restore(handler);
	    Potion.restore(handler);
	    Ring.restore(handler);

	    // 加载成就数据
	    Badges.loadLocal(data.getBundle(BADGES));

	    // 恢复主角对象
	    Dungeon.hero = (Hero)data.get(HERO);
	    Dungeon.hero.belongings.identify();

	    // 恢复游戏统计数据
	    Statistics.restoreFromBundle(data.getBundle(STATS));

	    // 恢复挑战次数
	    Dungeon.challenges = data.getInt(CHALLENGES);

	    // 记录游戏的初始版本
	    Dungeon.initialVersion = data.getInt(GAME_VERSION);

	    // 根据游戏版本，处理特定版本的逻辑
	    if (Dungeon.initialVersion <= ShatteredPixelDungeon.v1_2_3){
	        Statistics.gameWon = rec.win;
	    }
	    // 计算并设置游戏得分
	    rec.score = calculateScore();

	    // 如果存在种子数据，则恢复种子及相关设置
	    if (rec.gameData.contains(SEED)){
	        Dungeon.seed = rec.gameData.getLong(SEED);
	        Dungeon.customSeedText = rec.gameData.getString(CUSTOM_SEED);
	        Dungeon.daily = rec.gameData.getBoolean(DAILY);
	        Dungeon.dailyReplay = rec.gameData.getBoolean(DAILY_REPLAY);
	    } else {
	        // 如果没有种子数据，重置相关设置
	        Dungeon.seed = -1;
	        Dungeon.customSeedText = "";
	        Dungeon.daily = Dungeon.dailyReplay = false;
	    }
	}
	
	private static final String RECORDS	= "records";
	private static final String LATEST	= "latest";
	private static final String TOTAL	= "total";
	private static final String WON     = "won";

	public static final String LATEST_DAILY	        = "latest_daily";
	public static final String DAILY_HISTORY_DATES  = "daily_history_dates";
	public static final String DAILY_HISTORY_SCORES = "daily_history_scores";

	/**
	 * 保存游戏进度。
	 * 此方法将当前的游戏记录、最新记录、总记录数、获胜次数以及每日得分历史记录存储到一个Bundle中，
	 * 然后将这个Bundle写入到文件中，用于持久化游戏数据。
	 */
	public void save() {
	    // 创建一个Bundle对象来存储所有需要保存的数据
	    Bundle bundle = new Bundle();

	    // 将游戏记录、最新记录、总记录数、获胜次数保存到Bundle中
	    bundle.put( RECORDS, records );
	    bundle.put( LATEST, lastRecord );
	    bundle.put( TOTAL, totalNumber );
	    bundle.put( WON, wonNumber );

	    // 保存最新的每日记录
	    bundle.put(LATEST_DAILY, latestDaily);

	    // 将每日得分历史记录的日期和分数分别存储到数组中
	    long[] dates = new long[dailyScoreHistory.size()];
	    int[] scores = new int[dailyScoreHistory.size()];
	    int i = 0;
	    for (Long l : dailyScoreHistory.keySet()){
	        dates[i] = l;
	        scores[i] = dailyScoreHistory.get(l);
	        i++;
	    }
	    // 将日期和分数数组保存到Bundle中
	    bundle.put(DAILY_HISTORY_DATES, dates);
	    bundle.put(DAILY_HISTORY_SCORES, scores);

	    // 尝试将Bundle保存到文件中，如果发生IOException，则报告异常
	    try {
	        FileUtils.bundleToFile( RANKINGS_FILE, bundle);
	    } catch (IOException e) {
	        ShatteredPixelDungeon.reportException(e);
	    }

	}
	
	/**
	 * 加载记录数据。
	 * 从指定的文件中加载排名、记录及相关统计信息，如总记录数、获胜数等。
	 * 如果文件不存在或无法读取，则记录将为空。
	 */
	public void load() {
	    // 如果记录已经加载，则直接返回，避免重复加载
	    if (records != null) {
	        return;
	    }

	    // 初始化记录列表
	    records = new ArrayList<>();

	    try {
	        // 从文件中加载数据包
	        Bundle bundle = FileUtils.bundleFromFile( RANKINGS_FILE );

	        // 将数据包中的记录添加到列表中
	        for (Bundlable record : bundle.getCollection( RECORDS )) {
	            records.add( (Record)record );
	        }
	        // 从数据包中获取最新的记录编号
	        lastRecord = bundle.getInt( LATEST );

	        // 从数据包中获取总记录数，如果未设置，则默认为记录列表的大小
	        totalNumber = bundle.getInt( TOTAL );
	        if (totalNumber == 0) {
	            totalNumber = records.size();
	        }

	        // 从数据包中获取获胜记录数，如果未设置，则通过遍历记录列表计算
	        wonNumber = bundle.getInt( WON );
	        if (wonNumber == 0) {
	            for (Record rec : records) {
	                if (rec.win) {
	                    wonNumber++;
	                }
	            }
	        }

	        // 如果数据包中包含每日记录信息，则加载每日记录及其分数历史
	        if (bundle.contains(LATEST_DAILY)){
	            latestDaily = (Record) bundle.get(LATEST_DAILY);

	            // 清空每日分数历史记录
	            dailyScoreHistory.clear();
	            // 加载每日分数历史记录
	            int[] scores = bundle.getIntArray(DAILY_HISTORY_SCORES);
	            int i = 0;
	            long latestDate = 0;
	            for (long date : bundle.getLongArray(DAILY_HISTORY_DATES)){
	                dailyScoreHistory.put(date, scores[i]);
	                if (date > latestDate) latestDate = date;
	                i++;
	            }
	            // 更新最新的每日记录日期
	            if (latestDate > SPDSettings.lastDaily()){
	                SPDSettings.lastDaily(latestDate);
	            }
	        }

	    } catch (IOException e) {
	        // 文件读取异常处理
	    }
	}
	
	// Record类用于存储游戏的记录信息，包括比赛结果、英雄等级、深度等详细数据。
	public static class Record implements Bundlable {

		// 定义了Bundle中用于存储各种数据的键名。
		private static final String CAUSE   = "cause";
		private static final String WIN		= "win";
		private static final String SCORE	= "score";
		private static final String CLASS	= "class";
		private static final String TIER	= "tier";
		private static final String LEVEL	= "level";
		private static final String DEPTH	= "depth";
		private static final String ASCEND	= "ascending";
		private static final String DATA	= "gameData";
		private static final String ID      = "gameID";
		private static final String SEED    = "custom_seed";
		private static final String DAILY   = "daily";

		private static final String DATE    = "date";
		private static final String VERSION = "version";

		// 存储比赛失败的原因，可能是异常或其他特定情况。
		public Class cause;
		// 标记比赛是否获胜。
		public boolean win;

		// 存储英雄的类型。
		public HeroClass heroClass;
		// 存储护甲的等级。
		public int armorTier;
		// 存储英雄的等级。
		public int herolevel;
		// 存储比赛的深度。
		public int depth;
		// 标记是否在递增模式下进行比赛。
		public boolean ascending;

		// 存储游戏的具体数据。
		public Bundle gameData;
		// 存储游戏的唯一标识符。
		public String gameID;

		// 用于记录得分，注意，实际显示的分数应从游戏数据重新计算。
		public int score;

		// 存储自定义的种子号。
		public String customSeed;
		// 标记是否为每日比赛。
		public boolean daily;

		// 存储比赛的日期。
		public String date;
		// 存储比赛的版本信息。
		public String version;

		/**
		 * 根据比赛结果和设置生成比赛描述。
		 * @return 返回描述字符串，基于比赛结果、是否递增以及失败原因。
		 */
		public String desc(){
			if (win){
				if (ascending){
					return Messages.get(this, "ascended");
				} else {
					return Messages.get(this, "won");
				}
			} else if (cause == null) {
				return Messages.get(this, "something");
			} else {
				String result = Messages.get(cause, "rankings_desc", (Messages.get(cause, "name")));
				if (result.contains(Messages.NO_TEXT_FOUND)){
					return Messages.get(this, "something");
				} else {
					return result;
				}
			}
		}

		/**
		 * 从Bundle中恢复Record对象的状态。
		 * @param bundle 包含Record数据的Bundle对象。
		 */
		@Override
		public void restoreFromBundle( Bundle bundle ) {

			// 恢复失败原因，如果不存在则设为null。
			if (bundle.contains( CAUSE )) {
				cause = bundle.getClass( CAUSE );
			} else {
				cause = null;
			}

			// 恢复获胜状态、分数、自定义种子和每日比赛标记。
			win		    = bundle.getBoolean( WIN );
			score	    = bundle.getInt( SCORE );
			customSeed  = bundle.getString( SEED );
			daily       = bundle.getBoolean( DAILY );

			// 恢复英雄类型、护甲等级、英雄等级、深度和递增状态。
			heroClass	= bundle.getEnum( CLASS, HeroClass.class );
			armorTier	= bundle.getInt( TIER );
			herolevel   = bundle.getInt( LEVEL );
			depth       = bundle.getInt( DEPTH );
			ascending   = bundle.getBoolean( ASCEND );

			// 恢复日期和版本信息，如果不存在则设为null。
			if (bundle.contains( DATE )){
				date = bundle.getString( DATE );
				version = bundle.getString( VERSION );
			} else {
				date = version = null;
			}

			// 恢复游戏数据和游戏ID，如果游戏ID不存在，则生成一个新的UUID。
			if (bundle.contains(DATA))  gameData = bundle.getBundle(DATA);
			if (bundle.contains(ID))   gameID = bundle.getString(ID);
			if (gameID == null) gameID = UUID.randomUUID().toString();

		}

		/**
		 * 将Record对象的状态存储到Bundle中。
		 * @param bundle 用于存储Record数据的Bundle对象。
		 */
		@Override
		public void storeInBundle( Bundle bundle ) {

			// 如果存在失败原因，则存储到Bundle中。
			if (cause != null) bundle.put( CAUSE, cause );

			// 存储获胜状态、分数、自定义种子和每日比赛标记。
			bundle.put( WIN, win );
			bundle.put( SCORE, score );
			bundle.put( SEED, customSeed );
			bundle.put( DAILY, daily );

			// 存储英雄类型、护甲等级、英雄等级、深度和递增状态。
			bundle.put( CLASS, heroClass );
			bundle.put( TIER, armorTier );
			bundle.put( LEVEL, herolevel );
			bundle.put( DEPTH, depth );
			bundle.put( ASCEND, ascending );

			// 存储日期和版本信息。
			bundle.put( DATE, date );
			bundle.put( VERSION, version );

			// 如果存在游戏数据，则存储到Bundle中。
			if (gameData != null) bundle.put( DATA, gameData );
			// 存储游戏ID。
			bundle.put( ID, gameID );
		}
	}

	public static final Comparator<Record> scoreComparator = new Comparator<Rankings.Record>() {
				/**
		 * 重写比较方法，用于排序两个 Record 对象。
		 * 比较逻辑首先检查自定义种子是否存在，然后比较分数，最后比较游戏ID的哈希值。
		 *
		 * @param lhs 左侧的 Record 对象，用于比较。
		 * @param rhs 右侧的 Record 对象，用于比较。
		 * @return 返回一个整数值表示比较结果：
		 *         小于0 表示 lhs 应排在 rhs 之前，
		 *         等于0 表示 lhs 和 rhs 相等，
		 *         大于0 表示 lhs 应排在 rhs 之后。
		 */
		@Override
		public int compare(Record lhs, Record rhs) {
			// 这里覆盖了自定义种子运行和每日记录的情况
			if (rhs.customSeed.isEmpty() && !lhs.customSeed.isEmpty()) {
				return +1;
			} else if (lhs.customSeed.isEmpty() && !rhs.customSeed.isEmpty()) {
				return -1;
			}

			// 比较分数，如果分数相等则继续比较游戏ID
			int result = (int)Math.signum(rhs.score - lhs.score);
			if (result == 0) {
				return (int)Math.signum(rhs.gameID.hashCode() - lhs.gameID.hashCode());
			} else {
				return result;
			}
		}

	};
}
