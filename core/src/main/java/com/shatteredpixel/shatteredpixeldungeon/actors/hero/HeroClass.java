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

package com.shatteredpixel.shatteredpixeldungeon.actors.hero;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.QuickSlot;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.ArmorAbility;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.duelist.Challenge;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.duelist.ElementalStrike;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.duelist.Feint;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.huntress.NaturesPower;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.huntress.SpectralBlades;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.huntress.SpiritHawk;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.mage.ElementalBlast;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.mage.WarpBeacon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.mage.WildMagic;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.rogue.DeathMark;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.rogue.ShadowClone;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.rogue.SmokeBomb;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.warrior.Endure;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.warrior.HeroicLeap;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.warrior.Shockwave;
import com.shatteredpixel.shatteredpixeldungeon.items.special.BrokenSeal;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.Waterskin;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.ClothArmor;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.CloakOfShadows;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.VelvetPouch;
import com.shatteredpixel.shatteredpixeldungeon.items.food.Food;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHealing;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfInvisibility;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfLiquidFlame;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfMindVision;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfStrength;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfIdentify;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfLullaby;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfMagicMapping;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfMirrorImage;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfRage;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfUpgrade;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfMagicMissile;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.SpiritBow;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Dagger;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Gloves;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MagesStaff;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Rapier;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.WornShortsword;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.ThrowingKnife;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.ThrowingSpike;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.ThrowingStone;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.watabou.utils.DeviceCompat;

public enum HeroClass {

	    /**
	     * 战士职业，关联了狂战士和角斗士两个子职业。
	     */
	    WARRIOR(HeroSubClass.BERSERKER, HeroSubClass.GLADIATOR),
	    /**
	     * 法师职业，关联了战斗法师和术士两个子职业。
	     */
	    MAGE(HeroSubClass.BATTLEMAGE, HeroSubClass.WARLOCK),
	    /**
	     * 刺客职业，关联了刺客和自由奔跑者两个子职业。
	     */
	    ROGUE(HeroSubClass.ASSASSIN, HeroSubClass.FREERUNNER),
	    /**
	     * 猎人职业，关联了狙击手和守望者两个子职业。
	     */
	    HUNTRESS(HeroSubClass.SNIPER, HeroSubClass.WARDEN),
	    /**
	     * 决斗者职业，关联了冠军和修道者两个子职业。
	     */
	    DUELIST(HeroSubClass.CHAMPION, HeroSubClass.MONK);

	    /**
	     * 每个英雄职业都关联一个或多个子职业。
	     * 这个数组存储了与每个英雄职业相关的子职业。
	     */
	    private HeroSubClass[] subClasses;

	    /**
	     * 构造函数，为每个英雄职业初始化子职业数组。
	     * @param subClasses 与该英雄职业相关的子职业数组。
	     */

	HeroClass( HeroSubClass...subClasses ) {
		this.subClasses = subClasses;
	}

	public void initHero( Hero hero ) {

		hero.heroClass = this;
		Talent.initClassTalents(hero);

		Item i = new ClothArmor().identify();
		if (!Challenges.isItemBlocked(i)) hero.belongings.armor = (ClothArmor)i;

		i = new Food();
		if (!Challenges.isItemBlocked(i)) i.collect();

		new VelvetPouch().collect();
		Dungeon.LimitedDrops.VELVET_POUCH.drop();

		Waterskin waterskin = new Waterskin();
		waterskin.collect();

		new ScrollOfIdentify().identify();

		switch (this) {
			case WARRIOR:
				initWarrior( hero );
				break;

			case MAGE:
				initMage( hero );
				break;

			case ROGUE:
				initRogue( hero );
				break;

			case HUNTRESS:
				initHuntress( hero );
				break;

			case DUELIST:
				initDuelist( hero );
				break;
		}

		if (SPDSettings.quickslotWaterskin()) {
			for (int s = 0; s < QuickSlot.SIZE; s++) {
				if (Dungeon.quickslot.getItem(s) == null) {
					Dungeon.quickslot.setSlot(s, waterskin);
					break;
				}
			}
		}

	}

	public Badges.Badge masteryBadge() {
		switch (this) {
			case WARRIOR:
				return Badges.Badge.MASTERY_WARRIOR;
			case MAGE:
				return Badges.Badge.MASTERY_MAGE;
			case ROGUE:
				return Badges.Badge.MASTERY_ROGUE;
			case HUNTRESS:
				return Badges.Badge.MASTERY_HUNTRESS;
			case DUELIST:
				return Badges.Badge.MASTERY_DUELIST;
		}
		return null;
	}

	/**
	 * 初始化战士英雄的装备和道具。
	 * 为战士装备一把短剑，并设置投掷石头的数量，将石头放入快捷栏。
	 * 如果战士已有护甲，则为护甲附上封印。此外，识别一瓶治疗药水和一张愤怒卷轴。
	 *
	 * @param hero 要初始化的战士英雄。
	 */
	private static void initWarrior( Hero hero ) {
	    // 为英雄装备一把短剑，并进行识别
	    (hero.belongings.weapon = new WornShortsword()).identify();
	    // 创建投掷石头，设置数量为3，并收集到英雄的物品栏
	    ThrowingStone stones = new ThrowingStone();
	    stones.quantity(3).collect();
	    // 将投掷石头设置到快捷栏的第1个位置
	    Dungeon.quickslot.setSlot(0, stones);

	    // 如果英雄已经装备了护甲，则为护甲附上一个封印
	    if (hero.belongings.armor != null){
	        hero.belongings.armor.affixSeal(new BrokenSeal());
	    }

	    // 识别一瓶治疗药水，用于恢复英雄的生命值
	    new PotionOfHealing().identify();
	    // 识别一张愤怒卷轴，用于增强英雄的攻击力
	    new ScrollOfRage().identify();
	}

	/**
	 * 初始化法师英雄的装备和物品。
	 * 这个方法专门为法师英雄配置了魔法杖和其他魔法物品，以确保他们在游戏开始时具有一定的战斗力和特殊能力。
	 * @param hero 法师英雄实例，将为其装备魔法杖和其他物品。
	 */
	private static void initMage(Hero hero) {
	    // 创建一个魔法杖，这里使用了魔法飞弹杖作为法师的初始武器。
	    MagesStaff staff;
	    staff = new MagesStaff(new WandOfMagicMissile());

	    // 将创建的魔法杖赋值给英雄的武器。
	    (hero.belongings.weapon = staff).identify();
	    // 激活英雄的武器，使英雄可以使用此武器的特殊能力。
	    hero.belongings.weapon.activate(hero);

	    // 将魔法杖设置到快速槽中，方便英雄快速使用。
	    Dungeon.quickslot.setSlot(0, staff);

		//鉴定一张卷轴
	    new ScrollOfUpgrade().identify();
		//鉴定一瓶药水
	    new PotionOfLiquidFlame().identify();
	}

	/**
	 * 初始化rogue（中译为”盗贼“）英雄的装备和道具。
	 * 这个方法为rogue英雄配备了特定的武器、护符、投掷武器，并设置了快速使用槽位。
	 * 武器选择了一把匕首，护符选择了阴影斗篷，投掷武器是三把飞刀。
	 * 阴影斗篷在激活后为英雄提供了隐形的能力，飞刀可以收集以备后用。
	 * 此外，还为英雄配备了魔法地图卷轴和隐身药水，以帮助他们在地下城探索。
	 *
	 * @param hero 要初始化的英雄对象，特定于rogue类。
	 */
	private static void initRogue(Hero hero) {
	    // 为英雄配备匕首作为主要武器，并识别武器属性
	    (hero.belongings.weapon = new Dagger()).identify();

	    // 创建阴影斗篷并识别其属性，然后激活斗篷的隐形效果
	    CloakOfShadows cloak = new CloakOfShadows();
	    (hero.belongings.artifact = cloak).identify();
	    hero.belongings.artifact.activate(hero);

	    // 创建三把飞刀，并设置数量为3，然后收集到英雄的物品栏中
	    ThrowingKnife knives = new ThrowingKnife();
	    knives.quantity(3).collect();

	    // 设置阴影斗篷到快速使用槽位0，设置飞刀到快速使用槽位1
	    Dungeon.quickslot.setSlot(0, cloak);
	    Dungeon.quickslot.setSlot(1, knives);

	    // 鉴定地图卷轴
	    new ScrollOfMagicMapping().identify();
	    // 鉴定隐身药水
	    new PotionOfInvisibility().identify();
	}

/**
 * 初始化猎人英雄的装备和物品。
 * 这个方法专门用于设置猎人英雄的初始武器和道具，以确保她在游戏开始时具有一定的战斗力和功能性。
 * @param hero 代表猎人英雄的对象，这个方法将对这个英雄的装备进行初始化。
 */
private static void initHuntress( Hero hero ) {
    // 为英雄装备手套武器，并通过identify()方法赋予它特定的属性。
    (hero.belongings.weapon = new Gloves()).identify();
    // 创建一把灵能弓
    SpiritBow bow = new SpiritBow();
	//将其收集到英雄的物品栏中
    bow.identify().collect();
    // 将弓设置到快速槽中
    Dungeon.quickslot.setSlot(0, bow);
    // 创建并识别一瓶药水
    new PotionOfMindVision().identify();
    // 创建并识别一张卷轴
    new ScrollOfLullaby().identify();
}


	/**
	 * 初始化决斗者角色的装备和道具。
	 * 这个方法专门为决斗者角色设计，用于在游戏开始或角色创建时配置其初始武器、副武器和其他道具。
	 * @param hero 传入的参数为决斗者角色对象，此方法将对这个角色的对象进行装备初始化。
	 */
	private static void initDuelist( Hero hero ) {
	    // 为决斗者装备一把细剑专武
	    (hero.belongings.weapon = new Rapier()).identify();
	    // 激活武器
	    hero.belongings.weapon.activate(hero);
	    // 创建投掷道具，并设置其数量为2，然后将其收集到角色的道具栏中。
	    ThrowingSpike spikes = new ThrowingSpike();
	    spikes.quantity(2).collect();

	    // 将武器设置到快捷栏的第一个位置，方便玩家快速使用。
	    Dungeon.quickslot.setSlot(0, hero.belongings.weapon);
	    // 将投掷尖刺设置到快捷栏的第二个位置。
	    Dungeon.quickslot.setSlot(1, spikes);

	    // 识别并鉴定力量药水
	    new PotionOfStrength().identify();
	    // 识别并鉴定一张卷轴
	    new ScrollOfMirrorImage().identify();
	}

	/**
	 * 获取英雄职业的标题。
	 * 通过调用Messages类的静态方法get，使用英雄职业类和职业名称作为键，获取对应的职业标题。
	 * @return 返回英雄职业的标题字符串。
	 */
	public String title() {
	    return Messages.get(HeroClass.class, name());
	}

	/**
	 * 获取英雄职业的描述。
	 * 通过在职业名称后添加"_desc"后缀，作为键，从Messages中获取英雄职业的详细描述。
	 * @return 返回英雄职业的描述字符串。
	 */
	public String desc(){
	    return Messages.get(HeroClass.class, name()+"_desc");
	}

	/**
	 * 获取英雄职业的简短描述。
	 * 通过在职业名称后添加"_desc_short"后缀，作为键，从Messages中获取英雄职业的简短描述。
	 * @return 返回英雄职业的简短描述字符串。
	 */
	public String shortDesc(){
	    return Messages.get(HeroClass.class, name()+"_desc_short");
	}

	/**
	 * 获取英雄职业的转职列表。
	 * @return 返回一个HeroSubClass类型的数组，代表了该英雄职业的所有子类。
	 */
	public HeroSubClass[] subClasses() {
	    return subClasses;
	}

	/**
	 * 根据英雄类型返回相应的护甲技能数组。
	 * 每种英雄类型都有独特的护甲技能，此方法通过switch-case语句根据英雄类型来返回相应的技能数组。
	 *
	 * @return ArmorAbility[] - 包含特定英雄类型护甲技能的数组。
	 */
	public ArmorAbility[] armorAbilities() {
	    // 根据英雄类型选择护甲技能
	    switch (this) {
	        // 战士的技能组合，包括英勇跳跃、冲击波和耐受
	        case WARRIOR: default:
	            return new ArmorAbility[]{new HeroicLeap(), new Shockwave(), new Endure()};
	        // 法师的技能组合，包括元素爆破、野性魔法和时空 Beacon
	        case MAGE:
	            return new ArmorAbility[]{new ElementalBlast(), new WildMagic(), new WarpBeacon()};
	        // 刺客的技能组合，包括烟雾弹、死亡标记和影子 clone
	        case ROGUE:
	            return new ArmorAbility[]{new SmokeBomb(), new DeathMark(), new ShadowClone()};
	        // 猎人的技能组合，包括幽灵之刃、自然之力和精神鹰
	        case HUNTRESS:
	            return new ArmorAbility[]{new SpectralBlades(), new NaturesPower(), new SpiritHawk()};
	        //决斗者的技能组合，包括挑战、元素打击和佯攻
	        case DUELIST:
	            return new ArmorAbility[]{new Challenge(), new ElementalStrike(), new Feint()};
	    }
	}

	/**
	 * 根据当前对象的类型返回对应的精灵表图像路径。
	 * 该方法用于根据角色类型从资源包中获取对应角色的精灵表图像路径。
	 * 精灵表是一种将多个动画帧或静态图像组合到单个图像文件中的技术，用于优化游戏性能和资源管理。
	 *
	 * @return 返回对应角色类型的精灵表图像路径字符串。
	 */
	public String spriteSheet() {
	    // 根据当前对象的类型选择对应的精灵表图像
	    switch (this) {
	        // 战士和默认情况使用同一张精灵表
	        case WARRIOR: default:
	            return Assets.Sprites.WARRIOR;
	        // 法师使用单独的精灵表
	        case MAGE:
	            return Assets.Sprites.MAGE;
	        // 赏金猎人使用单独的精灵表
	        case ROGUE:
	            return Assets.Sprites.ROGUE;
	        // 猎人使用单独的精灵表
	        case HUNTRESS:
	            return Assets.Sprites.HUNTRESS;
	        //决斗者使用单独的精灵表
	        case DUELIST:
	            return Assets.Sprites.DUELIST;
	    }
	}

	/**
	 * 根据当前角色类型返回对应的开场艺术画资源。
	 * @return 对应角色类型的开场漫画资源字符串。
	 */
	public String splashArt() {
	    // 根据角色类型选择相应的开场动画资源
	    switch (this) {
	        // 默认情况，以及战士类型的角色，使用战士的开场动画资源
	        case WARRIOR: default:
	            return Assets.Splashes.WARRIOR;
	        // 法师类型的角色，使用法师的开场动画资源
	        case MAGE:
	            return Assets.Splashes.MAGE;
	        // 刺客类型的角色，使用刺客的开场动画资源
	        case ROGUE:
	            return Assets.Splashes.ROGUE;
	        // 猎人类型的角色，使用猎人的开场动画资源
	        case HUNTRESS:
	            return Assets.Splashes.HUNTRESS;
	        //决斗者类型的角色，使用决斗者的开场动画资源
	        case DUELIST:
	            return Assets.Splashes.DUELIST;
	    }
	}

	/**
 * 检查当前角色是否已解锁。
 * 此方法根据设备的调试状态和玩家的徽章解锁状态来判断当前角色是否解锁。
 * 如果设备处于调试模式，则认为角色默认解锁。
 * 否则，依据不同的角色类型检查对应的解锁徽章状态。
 *
 * @return 如果角色已解锁则返回true，否则返回false。
 */
public boolean isUnlocked(){
    // 若设备处于调试模式，角色始终视为已解锁
    // 调试构建时始终解锁
    if (DeviceCompat.isDebug()) return true;

    // 根据不同的角色情况判断解锁状态
    switch (this){
        // 对于WARRIOR类别及默认情况，角色视为已解锁
        case WARRIOR: default:
            return true;
        // 检查MAGE类别徽章是否解锁
        case MAGE:
            return Badges.isUnlocked(Badges.Badge.UNLOCK_MAGE);
        // 检查ROGUE类别徽章是否解锁
        case ROGUE:
            return Badges.isUnlocked(Badges.Badge.UNLOCK_ROGUE);
        // 检查HUNTRESS类别徽章是否解锁
        case HUNTRESS:
            return Badges.isUnlocked(Badges.Badge.UNLOCK_HUNTRESS);
        // 检查DUELIST类别徽章是否解锁
        case DUELIST:
            return Badges.isUnlocked(Badges.Badge.UNLOCK_DUELIST);
    }
}

	
	public String unlockMsg() {
		return shortDesc() + "\n\n" + Messages.get(HeroClass.class, name()+"_unlock");
	}

}
