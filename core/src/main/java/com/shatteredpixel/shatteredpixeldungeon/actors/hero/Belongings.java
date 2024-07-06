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

import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.GamesInProgress;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.LostInventory;
import com.shatteredpixel.shatteredpixeldungeon.items.EquipableItem;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.KindOfWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.KindofMisc;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.ClassArmor;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Artifact;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.Bag;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.Ring;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfRemoveCurse;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

import java.util.ArrayList;
import java.util.Iterator;

public class Belongings implements Iterable<Item> {

	private Hero owner;

	/**
	 * 背包类，作为英雄的装备之一，用于存储物品。
	 * 它扩展了Bag类，增加了特定于背包的特性，如容量计算。
	 */
	public static class Backpack extends Bag {

	    {
	        // 初始化背包的图像标识
	        image = ItemSpriteSheet.BACKPACK;
	    }

	    /**
	     * 计算背包的实际容量。
	     * 背包的容量会根据是否装备了副武器进行动态调整。
	     *
	     * @return 背包的总容量，考虑了内部物品袋和副武器的影响。
	     */
	    public int capacity(){
	        // 获取背包的基础容量
	        int cap = super.capacity();
	        // 遍历背包中的物品，增加内部物品袋的容量
	        for (Item item : items){
	            if (item instanceof Bag){
	                cap++;
	            }
	        }
	        // 如果英雄存在且装备了副武器，则减少一个容量单位
	        if (Dungeon.hero != null && Dungeon.hero.belongings.secondWep != null){
	            //副武器仍占一个格子
	            cap--;
	        }
	        return cap;
	    }
	}

	public Backpack backpack;
	/**
	 * 构造函数，用于初始化 Belongings 类的实例。
	 *
	 * @param owner 英雄的所有者，用于关联装备和英雄。
	 */
	public Belongings( Hero owner ) {
		this.owner = owner;
		// 初始化背包，并将其所有者设置为英雄
		backpack = new Backpack();
		backpack.owner = owner;
	}

	// 英雄持有的武器
	public KindOfWeapon weapon = null;
	// 英雄穿戴的护甲
	public Armor armor = null;
	// 英雄持有的神器
	public Artifact artifact = null;
	// 英雄持有的杂项物品
	public KindofMisc misc = null;
	// 英雄佩戴的戒指
	public Ring ring = null;

	//used when thrown weapons temporary become the current weapon
	public KindOfWeapon thrownWeapon = null;

	//used to ensure that the duelist always uses the weapon she's using the ability of
	public KindOfWeapon abilityWeapon = null;

	//used by the champion subclass
	public KindOfWeapon secondWep = null;


	/**
	 * 获取攻击所使用的武器。
	 * 优先返回投掷武器，如果投掷武器不存在，则返回能力武器，如果两者都不存在，则调用weapon()方法获取武器。
	 * 这样设计的目的是为了根据不同的战斗策略选择最适合的武器进行攻击。
	 *	//*** these accessor methods are so that worn items can be affected by various effects/debuffs
	 * 	// we still want to access the raw equipped items in cases where effects should be ignored though,
	 * 	// such as when equipping something, showing an interface, or dealing with items from a dead hero
	 *
	 * 	//normally the primary equipped weapon, but can also be a thrown weapon or an ability's weapon
	 * @return 当前攻击所使用的武器。
	 */
	public KindOfWeapon attackingWeapon() {
	    // 检查是否有投掷武器，如果有，则直接返回投掷武器。
	    if (thrownWeapon != null) return thrownWeapon;
	    // 如果没有投掷武器，但有能力和武器，则返回能力武器。
	    if (abilityWeapon != null) return abilityWeapon;
	    // 如果既没有投掷武器也没有能力武器，则调用weapon()方法获取默认武器。
	    return weapon();
	}

	//we cache whether belongings are lost to avoid lots of calls to hero.buff(LostInventory.class)
	private boolean lostInvent;
	public void lostInventory( boolean val ){
		lostInvent = val;
	}

	public boolean lostInventory(){
		return lostInvent;
	}

	/**
	 * 检查是否失去装备。如果没有失去装备，或者装备被标记为在失去装备后仍保留，
	 * 则返回该装备。否则返回null。
	 *
	 * @return 当前的武器装备，如果没有失去装备或装备被保留，则为null。
	 */
	public KindOfWeapon weapon() {
	    if (!lostInventory() || (weapon != null && weapon.keptThroughLostInventory())) {
	        return weapon;
	    } else {
	        return null;
	    }
	}

	/**
	 * 检查是否失去装备。如果没有失去装备，或者装备被标记为在失去装备后仍保留，
	 * 则返回该装备。否则返回null。
	 *
	 * @return 当前的护甲装备，如果没有失去装备或装备被保留，则为null。
	 */
	public Armor armor() {
	    if (!lostInventory() || (armor != null && armor.keptThroughLostInventory())) {
	        return armor;
	    } else {
	        return null;
	    }
	}

	/**
	 * 检查是否失去装备。如果没有失去装备，或者装备被标记为在失去装备后仍保留，
	 * 则返回该装备。否则返回null。
	 *
	 * @return 当前的神器装备，如果没有失去装备或装备被保留，则为null。
	 */
	public Artifact artifact() {
	    if (!lostInventory() || (artifact != null && artifact.keptThroughLostInventory())) {
	        return artifact;
	    } else {
	        return null;
	    }
	}

	/**
	 * 检查是否失去装备。如果没有失去装备，或者装备被标记为在失去装备后仍保留，
	 * 则返回该装备。否则返回null。
	 *
	 * @return 当前的杂项装备，如果没有失去装备或装备被保留，则为null。
	 */
	public KindofMisc misc() {
	    if (!lostInventory() || (misc != null && misc.keptThroughLostInventory())) {
	        return misc;
	    } else {
	        return null;
	    }
	}

	/**
	 * 获取角色的戒指装备。
	 * 如果角色没有失去装备，或者戒指装备在失去装备后仍被保留，则返回戒指装备。
	 * 否则，返回null，表示角色没有可用的戒指装备。
	 *
	 * @return 当前角色的戒指装备，如果没有或不可用，则返回null。
	 */
	public Ring ring(){
	    if (!lostInventory() || (ring != null && ring.keptThroughLostInventory())){
	        return ring;
	    } else {
	        return null;
	    }
	}

	/**
	 * 获取角色的第二件武器装备。
	 * 如果角色没有失去装备，或者第二件武器装备在失去装备后仍被保留，则返回第二件武器装备。
	 * 否则，返回null，表示角色没有可用的第二件武器装备。
	 *
	 * @return 当前角色的第二件武器装备，如果没有或不可用，则返回null。
	 */
	public KindOfWeapon secondWep(){
	    if (!lostInventory() || (secondWep != null && secondWep.keptThroughLostInventory())){
	        return secondWep;
	    } else {
	        return null;
	    }
	}

	// ***
	
	private static final String WEAPON		= "weapon";
	private static final String ARMOR		= "armor";
	private static final String ARTIFACT   = "artifact";
	private static final String MISC       = "misc";
	private static final String RING       = "ring";

	private static final String SECOND_WEP = "second_wep";

	/**
	 * 将当前装备存储到一个Bundle中。
	 * 此方法用于将角色当前装备的各类物品存储到Bundle中，以便于在游戏的其他部分或保存后重新加载。
	 * @param bundle 用于存储装备的Bundle对象，通过此对象可以将装备信息在不同场景间传递。
	 */
	public void storeInBundle( Bundle bundle ) {
	    // 将背包中的物品存储到Bundle中
	    backpack.storeInBundle( bundle );

	    // 逐个将当前装备的武器、护甲、神器、杂项、戒指和第二武器存储到Bundle中
	    bundle.put( WEAPON, weapon );
	    bundle.put( ARMOR, armor );
	    bundle.put( ARTIFACT, artifact );
	    bundle.put( MISC, misc );
	    bundle.put( RING, ring );
	    bundle.put( SECOND_WEP, secondWep );
	}
	
	/**
	 * 从Bundle中恢复角色装备。
	 * 此方法用于在角色加载或重新创建时，从保存的Bundle中恢复角色的装备状态。
	 * 它清空现有的背包，并从Bundle中逐一恢复各种装备，包括武器、护甲、饰品等。
	 * 每种装备在恢复后都会尝试激活，以确保装备的功能可用。
	 *
	 * @param bundle 包含角色装备数据的Bundle对象，用于恢复角色装备状态。
	 */
	public void restoreFromBundle( Bundle bundle ) {
	    // 清空背包，以便重新从Bundle加载装备
	    backpack.clear();
	    // 背包自身从Bundle中恢复状态
	    backpack.restoreFromBundle( bundle );

	    // 从Bundle中恢复武器，并尝试激活
	    weapon = (KindOfWeapon) bundle.get(WEAPON);
	    if (weapon() != null)       weapon().activate(owner);

	    // 从Bundle中恢复护甲，并尝试激活
	    armor = (Armor)bundle.get( ARMOR );
	    if (armor() != null)        armor().activate( owner );

	    // 从Bundle中恢复饰品，并尝试激活
	    artifact = (Artifact) bundle.get(ARTIFACT);
	    if (artifact() != null)     artifact().activate(owner);

	    // 从Bundle中恢复杂项装备，并尝试激活
	    misc = (KindofMisc) bundle.get(MISC);
	    if (misc() != null)         misc().activate( owner );

	    // 从Bundle中恢复戒指，并尝试激活
	    ring = (Ring) bundle.get(RING);
	    if (ring() != null)         ring().activate( owner );

	    // 从Bundle中恢复第二武器，并尝试激活
	    secondWep = (KindOfWeapon) bundle.get(SECOND_WEP);
	    if (secondWep() != null)    secondWep().activate(owner);
	}
	
	/**
	 * 预览游戏中的护甲信息。
	 * 此方法用于根据传入的Bundle对象更新GamesInProgress.Info实例中的护甲等级信息。
	 * 如果Bundle中包含护甲信息，则根据护甲类型设置护甲等级；否则，设置护甲等级为0。
	 *
	 * @param info 游戏进行中的信息，此方法将更新其中的护甲等级。
	 * @param bundle 一个Bundle对象，可能包含护甲信息。
	 */
	public static void preview( GamesInProgress.Info info, Bundle bundle ) {
		// 检查Bundle中是否包含护甲信息
		if (bundle.contains( ARMOR )){
			Armor armor = ((Armor)bundle.get( ARMOR ));

			// 如果护甲是ClassArmor类型，则设置护甲等级为6；否则，使用护甲自身的等级
			if (armor instanceof ClassArmor){
				info.armorTier = 6;
			} else {
				info.armorTier = armor.tier;
			}
		} else {
			// 如果Bundle中不包含护甲信息，将护甲等级设置为0
			info.armorTier = 0;
		}
	}

	//ignores lost inventory debuff
	/**
	 * 获取包含所有包的列表。
	 * 此方法检索当前容器中的所有包，包括直接包含的背包以及任何嵌套的包。
	 * 它返回一个ArrayList，其中包含了所有找到的包。
	 *
	 * @return ArrayList<Bag> - 包含所有包的列表。
	 */
	public ArrayList<Bag> getBags(){
	    // 初始化一个空的ArrayList，用于存储所有包。
	    ArrayList<Bag> result = new ArrayList<>();

	    // 将直接包含的背包添加到结果列表中。
	    result.add(backpack);

	    // 遍历当前容器中的所有物品。
	    for (Item i : this){
	        // 如果当前物品是包，则将其添加到结果列表中。
	        if (i instanceof Bag){
	            result.add((Bag)i);
	        }
	    }

	    // 返回包含所有包的列表。
	    return result;
	}
	
	@SuppressWarnings("unchecked")
	/**
	 * 根据指定的类类型，从当前容器中获取一个项目对象。
	 * 此方法允许泛型类型T继承自Item类，提高了代码的通用性和灵活性。
	 *
	 * @param itemClass 用于指定所需项目类型的Class对象。这个参数用来在容器中搜索匹配的项目。
	 * @return 如果找到匹配的项目且该项目在丢失库存后仍被保留，则返回该项目对象；否则返回null。
	 * @throws 无
	 */
	public<T extends Item> T getItem( Class<T> itemClass ) {
	    /* 检查是否发生了丢失库存的情况 */
	    boolean lostInvent = lostInventory();

	    /* 遍历容器中的每个项目 */
	    for (Item item : this) {
	        /* 检查当前项目是否是指定类类型的实例 */
	        if (itemClass.isInstance( item )) {
	            /* 如果当前项目在丢失库存后仍被保留，或者当前没有发生丢失库存的情况 */
	            if (!lostInvent || item.keptThroughLostInventory()) {
	                /* 将当前项目强制转换为泛型类型T，并返回 */
	                return (T) item;
	            }
	        }
	    }

	    /* 如果没有找到匹配的项目或者所有匹配的项目都在丢失库存后丢失，则返回null */
	    return null;
	}

	/**
	 * 根据指定的类类型，获取所有符合条件的物品实例列表。
	 * 此方法允许泛型类型T继承自Item类，提高了代码的通用性和灵活性。
	 *
	 * @param itemClass 物品类的Class对象，用于后续的类型判断和实例化。
	 * @return 一个ArrayList，包含所有符合条件的物品实例。列表中的每个元素都类型为T或其子类。
	 *
	 * 方法首先检查是否失去了库存（lostInventory），如果失去了库存，则只会添加那些在失去库存后仍被保留的物品。
	 * 这个方法利用了Java的泛型和反射机制，来实现类型安全的物品检索。
	 */
	public<T extends Item> ArrayList<T> getAllItems( Class<T> itemClass ) {
	    // 初始化结果列表，用于存储所有符合条件的物品实例。
	    ArrayList<T> result = new ArrayList<>();

	    // 检查是否失去了库存。
	    boolean lostInvent = lostInventory();

	    // 遍历当前集合中的每个物品。
	    for (Item item : this) {
	        // 判断当前物品是否是指定类类型或其子类的实例。
	        if (itemClass.isInstance( item )) {
	            // 如果没有失去库存，或者当前物品在失去库存后仍被保留，则添加到结果列表中。
	            if (!lostInvent || item.keptThroughLostInventory()) {
	                result.add((T) item);
	            }
	        }
	    }

	    // 返回符合条件的物品列表。
	    return result;
	}
	
	public boolean contains( Item contains ){

		boolean lostInvent = lostInventory();
		
		for (Item item : this) {
			if (contains == item) {
				if (!lostInvent || item.keptThroughLostInventory()) {
					return true;
				}
			}
		}
		
		return false;
	}
	
	public Item getSimilar( Item similar ){

		boolean lostInvent = lostInventory();
		
		for (Item item : this) {
			if (similar != item && similar.isSimilar(item)) {
				if (!lostInvent || item.keptThroughLostInventory()) {
					return item;
				}
			}
		}
		
		return null;
	}
	
	public ArrayList<Item> getAllSimilar( Item similar ){
		ArrayList<Item> result = new ArrayList<>();

		boolean lostInvent = lostInventory();
		
		for (Item item : this) {
			if (item != similar && similar.isSimilar(item)) {
				if (!lostInvent || item.keptThroughLostInventory()) {
					result.add(item);
				}
			}
		}
		
		return result;
	}

	//triggers when a run ends, so ignores lost inventory effects
	public void identify() {
		for (Item item : this) {
			item.identify(false);
		}
	}
	
	public void observe() {
		if (weapon() != null) {
			weapon().identify();
			Badges.validateItemLevelAquired(weapon());
		}
		if (armor() != null) {
			armor().identify();
			Badges.validateItemLevelAquired(armor());
		}
		if (artifact() != null) {
			artifact().identify();
			Badges.validateItemLevelAquired(artifact());
		}
		if (misc() != null) {
			misc().identify();
			Badges.validateItemLevelAquired(misc());
		}
		if (ring() != null) {
			ring().identify();
			Badges.validateItemLevelAquired(ring());
		}
		if (secondWep() != null){
			secondWep().identify();
			Badges.validateItemLevelAquired(secondWep());
		}
		for (Item item : backpack) {
			if (item instanceof EquipableItem || item instanceof Wand) {
				item.cursedKnown = true;
			}
		}
		Item.updateQuickslot();
	}
	
	public void uncurseEquipped() {
		ScrollOfRemoveCurse.uncurse( owner, armor(), weapon(), artifact(), misc(), ring(), secondWep());
	}
	
	public Item randomUnequipped() {
		if (owner.buff(LostInventory.class) != null) return null;

		return Random.element( backpack.items );
	}
	
	public int charge( float charge ) {
		
		int count = 0;
		
		for (Wand.Charger charger : owner.buffs(Wand.Charger.class)){
			charger.gainCharge(charge);
			count++;
		}
		
		return count;
	}

	@Override
	public Iterator<Item> iterator() {
		return new ItemIterator();
	}
	
	private class ItemIterator implements Iterator<Item> {

		private int index = 0;
		
		private Iterator<Item> backpackIterator = backpack.iterator();
		
		private Item[] equipped = {weapon, armor, artifact, misc, ring, secondWep};
		private int backpackIndex = equipped.length;
		
		@Override
		public boolean hasNext() {
			
			for (int i=index; i < backpackIndex; i++) {
				if (equipped[i] != null) {
					return true;
				}
			}
			
			return backpackIterator.hasNext();
		}

		@Override
		public Item next() {
			
			while (index < backpackIndex) {
				Item item = equipped[index++];
				if (item != null) {
					return item;
				}
			}
			
			return backpackIterator.next();
		}

		@Override
		public void remove() {
			switch (index) {
			case 0:
				equipped[0] = weapon = null;
				break;
			case 1:
				equipped[1] = armor = null;
				break;
			case 2:
				equipped[2] = artifact = null;
				break;
			case 3:
				equipped[3] = misc = null;
				break;
			case 4:
				equipped[4] = ring = null;
				break;
			case 5:
				equipped[5] = secondWep = null;
				break;
			default:
				backpackIterator.remove();
			}
		}
	}
}
