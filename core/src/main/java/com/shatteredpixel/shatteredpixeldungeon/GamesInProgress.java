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

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.watabou.utils.Bundle;
import com.watabou.utils.FileUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

public class GamesInProgress {
	
	// Define the maximum number of slots, equal to the number of HeroClass enums, indicating the maximum number of hero slots available
	public static final int MAX_SLOTS = HeroClass.values().length;

	// Store the information status of each slot, using the slot number as the key
	//null means we have loaded info and it is empty, no entry means unknown.
	private static HashMap<Integer, Info> slotStates = new HashMap<>();

	// Current slot indicator, used to track the slot currently being operated on
	public static int curSlot;

	// Store the currently selected hero class, allowing for global access and modification
	public static HeroClass selectedClass;

	// Format string for game folder, used to generate the path of the game folder, %d is a placeholder for the game number
	private static final String GAME_FOLDER = "game%d";

	// Name of the game data file, used to store the game's save data
	private static final String GAME_FILE	= "game.dat";

	// Name format of the depth data file, used to store the depth information of the game, %d is a placeholder for the depth number
	private static final String DEPTH_FILE	= "depth%d.dat";

	// Name format of the depth branch data file, used to store the branch information of a specific depth, %d is a placeholder for the depth and branch numbers
	private static final String DEPTH_BRANCH_FILE	= "depth%d-branch%d.dat";

	
	public static boolean gameExists( int slot ){
		return FileUtils.dirExists(gameFolder(slot))
				&& FileUtils.fileLength(gameFile(slot)) > 1;
	}
	
	public static String gameFolder( int slot ){
		return Messages.format(GAME_FOLDER, slot);
	}
	
	public static String gameFile( int slot ){
		return gameFolder(slot) + "/" + GAME_FILE;
	}
	
	public static String depthFile( int slot, int depth, int branch ) {
		if (branch == 0) {
			return gameFolder(slot) + "/" + Messages.format(DEPTH_FILE, depth);
		} else {
			return gameFolder(slot) + "/" + Messages.format(DEPTH_BRANCH_FILE, depth, branch);
		}
	}
	
	/**
	 * 寻找第一个空闲插槽。
	 * 该方法通过遍历插槽的方式，寻找从1到最大插槽数MAX_SLOTS中第一个可用的插槽。
	 * 如果找到空闲插槽，即check方法返回null，则返回该插槽的编号。
	 * 如果所有插槽均不可用，即遍历完成后未找到空闲插槽，则返回-1。
	 *
	 * @return 第一个空闲插槽的编号，如果所有插槽均不可用则返回-1。
	 */
	public static int firstEmpty() {
	    // 从1开始遍历到最大插槽数MAX_SLOTS
	    for (int i = 1; i <= MAX_SLOTS; i++) {
	        // 检查插槽i是否可用，如果可用（check方法返回null）则返回插槽编号i
	        if (check(i) == null) return i;
	    }
	    // 如果遍历完成后未找到空闲插槽，则返回-1
	    return -1;
	}
	
	/**
	 * 检查所有插槽并返回符合条件的Info对象列表。
	 *
	 * 此方法遍历所有插槽，对每个插槽调用check方法进行检查。
	 * 如果插槽中的Info对象符合条件，则将其添加到结果列表中。
	 * 最后，对结果列表按照scoreComparator进行排序。
	 *
	 * @return ArrayList<Info> 包含所有符合条件的Info对象的列表，已按score排序。
	 */
	public static ArrayList<Info> checkAll(){
	    // 初始化结果列表用于存储符合条件的Info对象。
	    ArrayList<Info> result = new ArrayList<>();
	    // 遍历所有插槽，插槽编号从1到MAX_SLOTS。
	    for (int i = 1; i <= MAX_SLOTS; i++){
	        // 调用check方法检查当前插槽，如果符合条件则返回Info对象。
	        Info curr = check(i);
	        // 如果check方法返回非空，则将该Info对象添加到结果列表。
	        if (curr != null) result.add(curr);
	    }
	    // 对结果列表进行排序，使用预定义的scoreComparator比较器。
	    Collections.sort(result, scoreComparator);
	    // 返回排序后的结果列表。
	    return result;
	}
	
	/**
	 * Checks the status of a game slot and returns related information.
	 *
	 * @param slot The index of the game slot to check.
	 * @return Info object containing game information, or null if the game does not exist or an error occurs.
	 */
	public static Info check( int slot ) {
	    // Check if the slot has already been checked before
	    if (slotStates.containsKey( slot )) {
	        // Directly return the previously obtained information
	        return slotStates.get( slot );
	    } else if (!gameExists( slot )) {
	        // If the game does not exist, record the state as null and return null
	        slotStates.put(slot, null);
	        return null;
	    } else {
	        // Initialize Info object to try to load game information
	        Info info;
	        try {
	            // Load the game's configuration file
	            Bundle bundle = FileUtils.bundleFromFile(gameFile(slot));
	            info = new Info();
	            info.slot = slot;
	            // Preview the game information
	            Dungeon.preview(info, bundle);
	            // If the game version is earlier than v1.4.3, do not support loading, set info to null
	            // saves from before v1.4.3 are not supported
	            if (info.version < ShatteredPixelDungeon.v1_4_3) {
	                info = null;
	            }
	        } catch (IOException e) {
	            // If an IO exception occurs, the game information cannot be loaded, return null
	            info = null;
	        } catch (Exception e) {
	            // If any other exception occurs, report the exception and return null
	            ShatteredPixelDungeon.reportException( e );
	            info = null;
	        }
	        // Update the state of the slot
	        slotStates.put( slot, info );
	        // Return the loaded game information
	        return info;
	    }
	}

	/**
	 * 设置指定槽位的信息。
	 * 此方法用于存储和记录游戏中的特定槽位的相关信息，包括但不限于角色属性、地牢进度和游戏设置。
	 * 信息通过一个Info对象进行封装，并存储在一个名为slotStates的集合中，以便于后续访问和查询。
	 *
	 * @param slot 指定的槽位编号，此编号用于唯一标识槽位及其相关信息。
	 */
	public static void set(int slot) {
	    // 创建一个新的Info对象，用于存储槽位信息。
	    Info info = new Info();
	    // 设置槽位的编号。
	    info.slot = slot;

	    // 更新Info对象中的地牢相关属性，包括深度、挑战数量等。
	    info.depth = Dungeon.depth;
	    info.challenges = Dungeon.challenges;

	    // 更新Info对象中的随机数生成器种子及相关设置，包括每日挑战状态。
	    info.seed = Dungeon.seed;
	    info.customSeed = Dungeon.customSeedText;
	    info.daily = Dungeon.daily;
	    info.dailyReplay = Dungeon.dailyReplay;

	    // 更新Info对象中的英雄属性，包括等级、力量、生命值等。
	    info.level = Dungeon.hero.lvl;
	    info.str = Dungeon.hero.STR;
	    info.strBonus = Dungeon.hero.STR() - Dungeon.hero.STR;
	    info.exp = Dungeon.hero.exp;
	    info.hp = Dungeon.hero.HP;
	    info.ht = Dungeon.hero.HT;
	    info.shld = Dungeon.hero.shielding();
	    info.heroClass = Dungeon.hero.heroClass;
	    info.subClass = Dungeon.hero.subClass;
	    info.armorTier = Dungeon.hero.tier();

	    // 更新Info对象中的统计信息，包括收集的金币数量和最深到达的楼层。
	    info.goldCollected = Statistics.goldCollected;
	    info.maxDepth = Statistics.deepestFloor;

	    // 将槽位信息存储在slotStates集合中，以便后续访问和查询。
	    slotStates.put(slot, info);
	}
	
	public static void setUnknown( int slot ) {
		slotStates.remove( slot );
	}
	
	public static void delete( int slot ) {
		slotStates.put( slot, null );
	}
	
	/**
	 * 用于存储游戏相关数据的静态内部类。
	 * 包含了角色信息、关卡信息以及游戏进度等各种数据字段。
	 */
	public static class Info {
	    /**
	     * 角色当前所在的槽位。
	     * 用于表示角色在队伍中的位置。
	     */
	    public int slot;

	    /**
	     * 角色当前所处的深度。
	     * 表示角色在游戏中的进度，即当前关卡的深度。
	     */
	    public int depth;
	    /**
	     * 游戏的版本号。
	     * 用于区分不同版本的游戏数据。
	     */
	    public int version;
	    /**
	     * 角色面临的挑战数量。
	     * 表示角色当前需要应对的额外挑战数量。
	     */
	    public int challenges;

	    /**
	     * 游戏的随机种子。
	     * 用于生成游戏中的随机事件和敌人的种子。
	     */
	    public long seed;
	    /**
	     * 用户自定义的随机种子字符串。
	     * 如果用户设置了自定义种子，则以此字符串作为随机种子。
	     */
	    public String customSeed;
	    /**
	     * 标记当前游戏是否为每日挑战。
	     */
	    public boolean daily;
	    /**
	     * 标记当前游戏的每日挑战是否可以重玩。
	     */
	    public boolean dailyReplay;

	    /**
	     * 角色的等级。
	     */
	    public int level;
	    /**
	     * 角色的力量值。
	     */
	    public int str;
	    /**
	     * 角色的力量加成。
	     */
	    public int strBonus;
	    /**
	     * 角色的经验值。
	     */
	    public int exp;
	    /**
	     * 角色的当前生命值。
	     */
	    public int hp;
	    /**
	     * 角色的饱食度，但在游戏ui中不显示。
	     */
	    public int ht;
	    /**
	     * 角色的护甲值。
	     */
	    public int shld;
	    /**
	     * 角色的职业。
	     */
	    public HeroClass heroClass;
	    /**
	     * 角色的副职业。
	     * 为角色提供额外的技能和属性加成。
	     */
	    public HeroSubClass subClass;
	    /**
	     * 角色装备的护甲等级。
	     * 影响角色对物理伤害的减免能力。
	     */
	    public int armorTier;

	    /**
	     * 角色在游戏中收集到的金币数量。
	     */
	    public int goldCollected;
	    /**
	     * 角色在游戏中的最大深度。
	     * 表示角色曾经达到过的最深关卡。
	     */
	    public int maxDepth;
	}
	
    /**
     * 用于比较游戏进度的评分的比较器。
     * 此比较器根据游戏的级别、最大深度和已收集的黄金数量来计算一个综合评分，
     * 并根据这个综合评分来决定游戏进度的排序。
     * 比较器的逻辑是，首先通过级别、最大深度和已收集黄金数量计算出一个评分值，
     * 然后比较两个游戏的评分值，根据评分值的差值的符号来决定两个游戏进度的顺序。
     *
     * @param lhs 第一个游戏进度的信息对象。
     * @param rhs 第二个游戏进度的信息对象。
     * @return 返回一个整数，表示两个游戏进度的评分的比较结果。
     */
    public static final Comparator<GamesInProgress.Info> scoreComparator = (lhs, rhs) -> {
        // 计算第一个游戏的综合评分
        int lScore = (lhs.level * lhs.maxDepth * 100) + lhs.goldCollected;
        // 计算第二个游戏的综合评分
        int rScore = (rhs.level * rhs.maxDepth * 100) + rhs.goldCollected;
        // 根据两个游戏的评分差值的符号决定比较结果
        return (int)Math.signum( rScore - lScore );
    };
}
