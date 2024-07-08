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

package com.shatteredpixel.shatteredpixeldungeon.items;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Blindness;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Degrade;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.Bag;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.darts.Dart;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.darts.TippedDart;
import com.shatteredpixel.shatteredpixeldungeon.journal.Catalog;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.MissileSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.buttons.QuickSlotButton;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.particles.Emitter;
import com.watabou.utils.Bundlable;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.Reflection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class Item implements Bundlable {

	/**
	 * 字符串格式常量，用于将对象名称和等级拼接成字符串。
	 */
	protected static final String TXT_TO_STRING_LVL		= "%s %+d";
	/**
	 * 字符串格式常量，用于将对象名称和数量拼接成字符串。
	 */
	protected static final String TXT_TO_STRING_X		= "%s x%d";

	/**
	 * 投掷动作完成所需的时间
	 */
	protected static final float TIME_TO_THROW		= 1.0f;
	/**
	 * 拾取动作完成所需的时间
	 */
	protected static final float TIME_TO_PICK_UP	= 1.0f;
	/**
	 * 丢弃动作完成所需的时间
	 */
	protected static final float TIME_TO_DROP		= 1.0f;

	/**
	 * 动作命令常量：丢弃。
	 */
	public static final String AC_DROP		= "DROP";
	/**
	 * 动作命令常量：投掷。
	 */
	public static final String AC_THROW		= "THROW";

	/**
	 * 默认动作，用于指示对象的默认行为模式。
	 */
	protected String defaultAction;
	/**
	 * 标志位，指示对象是否使用目标锁定机制。
	 */
	public boolean usesTargeting;

	//TODO should these be private and accessed through methods?
	public int image = 0;
	public int icon = -1; //used as an identifier for items with randomized images
	
	public boolean stackable = false;
	protected int quantity = 1;
	public boolean dropsDownHeap = false;
	
	private int level = 0;

	public boolean levelKnown = false;
	
	public boolean cursed;
	public boolean cursedKnown;
	
	// 独有物品死后保留
	public boolean unique = false;

	// These items are preserved even if the hero's inventory is lost via unblessed ankh
	// this is largely set by the resurrection window, items can override this to always be kept
	public boolean keptThoughLostInvent = false;

	// whether an item can be included in heroes remains
	public boolean bones = false;
	
	public static final Comparator<Item> itemComparator = new Comparator<Item>() {
		@Override
		public int compare( Item lhs, Item rhs ) {
			return Generator.Category.order( lhs ) - Generator.Category.order( rhs );
		}
	};
	
	/**
	 * 获取英雄可执行的动作列表。
	 * 此方法定义了英雄能够进行的特定动作，目前包括丢弃和投掷。
	 *
	 * @param hero 表示当前操作的英雄对象，该参数用于限定动作是针对特定英雄的。
	 * @return 返回一个包含英雄可执行动作的字符串列表。列表中的每个元素代表一个动作。
	 */
	public ArrayList<String> actions( Hero hero ) {
	    // 初始化一个ArrayList用于存储英雄可执行的动作
	    ArrayList<String> actions = new ArrayList<>();
	    // 添加“丢弃”动作到动作列表
	    actions.add( AC_DROP );
	    // 添加“投掷”动作到动作列表
	    actions.add( AC_THROW );
	    // 返回包含所有动作的列表
	    return actions;
	}

	public String actionName(String action, Hero hero){
		return Messages.get(this, "ac_" + action);
	}

	/**
	 * 设置商品的数量。
	 *
	 * 该方法用于更新商品实例的库存数量。通过传入新的数量值，可以精确地控制商品的库存水平。
	 * 对于库存管理来说，这是一个关键的操作，确保了库存数据的准确性和实时性。
	 *
	 * @param quantity 商品的新数量。这个参数代表了商品的库存量，用于更新现有的库存水平。
	 */
	public void setQuantity(int quantity){
	    this.quantity = quantity;
	}
	public int getQuantity(int quantity){
		return quantity;
	}
	public final boolean doPickUp( Hero hero ) {
		return doPickUp( hero, hero.pos );
	}

	/**
	 * 尝试让英雄捡起物品。
	 *
	 * 此方法用于模拟英雄捡起背包中物品的行为。它首先检查是否可以收集物品，
	 * 如果可以，则执行捡起物品的一系列操作，包括更新游戏场景、播放音效、
	 * 计算捡起物品所需的时间，并最终返回捡起成功的结果。如果不能收集物品，
	 * 则返回捡起失败的结果。
	 *
	 * @param hero 参与捡起操作的英雄对象。
	 * @param pos 捡起物品的位置索引。
	 * @return 如果物品成功被捡起，则返回true；否则返回false。
	 */
	public boolean doPickUp(Hero hero, int pos) {
	    // 检查是否可以收集背包中的物品
	    if (collect(hero.belongings.backpack)) {
	        // 更新游戏场景，表示物品已被捡起
	        GameScene.pickUp(this, pos);
	        // 播放捡起物品的音效
	        Sample.INSTANCE.play(Assets.Sounds.ITEM);
	        // 让英雄消耗时间并进入下一个动作
	        hero.spendAndNext(TIME_TO_PICK_UP);
	        // 返回捡起成功
	        return true;
	    } else {
	        // 返回捡起失败
	        return false;
	    }
	}
	/**
	 * 让英雄丢弃背包中的所有物品。
	 * <p>
	 * 此方法模拟了英雄丢弃物品的过程。它首先消耗掉英雄丢弃物品所需的时间，然后确定物品掉落的位置，
	 * 最后将英雄背包中的所有物品掉落至地牢中相应的位置。
	 *
	 * @param hero 需要丢弃物品的英雄对象。
	 */
	public void doDrop( Hero hero ) {
	    // 消耗丢弃物品所需的时间，并进入下一游戏步骤
	    hero.spendAndNext(TIME_TO_DROP);
	    // 获取英雄当前的位置，用于确定物品掉落的位置
	    int pos = hero.pos;
	    // 从英雄的背包中分离出所有物品，然后在地牢中指定位置生成一个掉落物精灵
	    Dungeon.level.drop(detachAll(hero.belongings.backpack), pos).sprite.drop(pos);
	}

	//resets an item's properties, to ensure consistency between runs
	public void reset(){
		keptThoughLostInvent = false;
	}

	public boolean keptThroughLostInventory(){
		return keptThoughLostInvent;
	}

	public void doThrow( Hero hero ) {
		GameScene.selectCell(thrower);
	}
	
	public void execute( Hero hero, String action ) {

		GameScene.cancel();
		curUser = hero;
		curItem = this;
		
		if (action.equals( AC_DROP )) {
			
			if (hero.belongings.backpack.contains(this) || isEquipped(hero)) {
				doDrop(hero);
			}
			
		} else if (action.equals( AC_THROW )) {
			
			if (hero.belongings.backpack.contains(this) || isEquipped(hero)) {
				doThrow(hero);
			}
			
		}
	}

	//can be overridden if default action is variable
	public String defaultAction(){
		return defaultAction;
	}
	
	public void execute( Hero hero ) {
		String action = defaultAction();
		if (action != null) {
			execute(hero, defaultAction());
		}
	}
	
	protected void onThrow( int cell ) {
		Heap heap = Dungeon.level.drop( this, cell );
		if (!heap.isEmpty()) {
			heap.sprite.drop( cell );
		}
	}
	
	//takes two items and merges them (if possible)
	/**
	 * 合并两个物品实例。
	 * 如果两个物品相似（根据isSimilar方法的定义），则将它们的量合并，并将其中一个物品的量重置为0。
	 * 这个方法体现了合并两个相同类型物品的操作，常用于库存管理或购物车合并同类商品。
	 *
	 * @param other 另一个物品实例，用于合并。
	 * @return 返回合并后的当前物品实例。
	 */
	public Item merge(Item other) {
	    // 检查两个物品是否相似
	    if (isSimilar(other)) {
	        // 如果相似，合并数量
	        quantity += other.quantity;
	        // 将被合并的物品数量重置为0，表示已合并
	        other.quantity = 0;
	    }
	    // 返回合并后的当前物品实例
	    return this;
	}
	
	/**
	 * 尝试将当前物品收集到容器中。
	 *
	 * @param container 目标收集容器。
	 * @return 如果物品成功收集到容器中或者无法收集，则返回true；如果物品已经存在于容器中，则直接返回true。
	 */
	public boolean collect( Bag container ) {
	    // 如果物品数量为0或更少，或者容器已满，则认为收集成功。
	    if (quantity <= 0){
	        return true;
	    }

	    ArrayList<Item> items = container.items;

	    // 如果物品已经存在于容器中，则直接返回true。
	    if (items.contains( this )) {
	        return true;
	    }

	    // 遍历容器中的每个物品，寻找可以容纳当前物品的子容器。
	    for (Item item:items) {
	        if (item instanceof Bag && ((Bag)item).canHold( this )) {
	            if (collect( (Bag)item )){
	                return true;
	            }
	        }
	    }

	    // 如果当前物品无法被容器容纳，则返回false。
	    if (!container.canHold(this)){
	        return false;
	    }

	    // 如果物品是可堆叠的，则尝试合并到相同的物品上。
	    if (stackable) {
	        for (Item item:items) {
	            if (isSimilar( item )) {
	                item.merge( this );
	                item.updateQuickslot();
	                // 处理英雄存活且物品被收集时的逻辑，包括验证物品等级、触发收集事件等。
	                if (Dungeon.hero != null && Dungeon.hero.isAlive()) {
	                    Badges.validateItemLevelAquired( this );
	                    Talent.onItemCollected(Dungeon.hero, item);
	                    if (isIdentified()) Catalog.setSeen(getClass());
	                }
	                // 处理飞镖的情况，如果存在未收集的飞镖，则创建并尝试收集飞镖。
	                if (TippedDart.lostDarts > 0){
	                    Dart d = new Dart();
	                    d.quantity(TippedDart.lostDarts);
	                    TippedDart.lostDarts = 0;
	                    if (!d.collect()){
	                        // 在无法直接收集飞镖时，通过延迟动作的方式处理飞镖的掉落。
	                        //have to handle this in an actor as we can't manipulate the heap during pickup
	                        Actor.add(new Actor() {
	                            { actPriority = VFX_PRIO; }
	                            @Override
	                            protected boolean act() {
	                                Dungeon.level.drop(d, Dungeon.hero.pos).sprite.drop();
	                                Actor.remove(this);
	                                return true;
	                            }
	                        });
	                    }
	                }
	                return true;
	            }
	        }
	    }

	    // 处理英雄存活且物品被收集时的逻辑，包括验证物品等级、触发收集事件等。
	    if (Dungeon.hero != null && Dungeon.hero.isAlive()) {
	        Badges.validateItemLevelAquired( this );
	        Talent.onItemCollected( Dungeon.hero, this );
	        if (isIdentified()) Catalog.setSeen(getClass());
	    }

	    // 将物品添加到容器中，并进行排序以保持容器内物品的有序性。
	    items.add( this );
	    Dungeon.quickslot.replacePlaceholder(this);
	    Collections.sort( items, itemComparator );
	    updateQuickslot();
	    return true;

	}
	public boolean collect() {
		return collect( Dungeon.hero.belongings.backpack );
	}
	
	//returns a new item if the split was sucessful and there are now 2 items, otherwise null
	public Item split( int amount ){
		if (amount <= 0 || amount >= quantity()) {
			return null;
		} else {
			//pssh, who needs copy constructors?
			Item split = Reflection.newInstance(getClass());
			
			if (split == null){
				return null;
			}
			
			Bundle copy = new Bundle();
			this.storeInBundle(copy);
			split.restoreFromBundle(copy);
			split.quantity(amount);
			quantity -= amount;
			
			return split;
		}
	}

	public Item duplicate(){
		Item dupe = Reflection.newInstance(getClass());
		if (dupe == null){
			return null;
		}
		Bundle copy = new Bundle();
		this.storeInBundle(copy);
		dupe.restoreFromBundle(copy);
		return dupe;
	}
	
	public final Item detach( Bag container ) {
		
		if (quantity <= 0) {
			
			return null;
			
		} else
		if (quantity == 1) {

			if (stackable){
				Dungeon.quickslot.convertToPlaceholder(this);
			}

			return detachAll( container );
			
		} else {
			
			
			Item detached = split(1);
			updateQuickslot();
			if (detached != null) detached.onDetach( );
			return detached;
			
		}
	}
	
	public final Item detachAll( Bag container ) {
		Dungeon.quickslot.clearItem( this );

		for (Item item : container.items) {
			if (item == this) {
				container.items.remove(this);
				item.onDetach();
				container.grabItems(); //try to put more items into the bag as it now has free space
				updateQuickslot();
				return this;
			} else if (item instanceof Bag) {
				Bag bag = (Bag)item;
				if (bag.contains( this )) {
					return detachAll( bag );
				}
			}
		}

		updateQuickslot();
		return this;
	}
	
	public boolean isSimilar( Item item ) {
		return getClass() == item.getClass();
	}

	protected void onDetach(){}

	//returns the true level of the item, ignoring all modifiers aside from upgrades
	public final int trueLevel(){
		return level;
	}

	//returns the persistant level of the item, only affected by modifiers which are persistent (e.g. curse infusion)
	public int level(){
		return level;
	}
	
	//returns the level of the item, after it may have been modified by temporary boosts/reductions
	//note that not all item properties should care about buffs/debuffs! (e.g. str requirement)
	public int buffedLvl(){
		//only the hero can be affected by Degradation
		if (Dungeon.hero.buff( Degrade.class ) != null
			&& (isEquipped( Dungeon.hero ) || Dungeon.hero.belongings.contains( this ))) {
			return Degrade.reduceLevel(level());
		} else {
			return level();
		}
	}

	public void level( int value ){
		level = value;

		updateQuickslot();
	}
	
	public Item upgrade() {
		
		this.level++;

		updateQuickslot();
		
		return this;
	}
	
	final public Item upgrade( int n ) {
		for (int i=0; i < n; i++) {
			upgrade();
		}
		
		return this;
	}
	
	public Item degrade() {
		
		this.level--;
		
		return this;
	}
	
	final public Item degrade( int n ) {
		for (int i=0; i < n; i++) {
			degrade();
		}
		
		return this;
	}
	
	/**
 * 计算并返回物品可见的升级等级。如果等级已知，则返回当前等级；否则，返回0。
 *
 * @return 物品的可见升级等级。
 */
public int visiblyUpgraded() {
    return levelKnown ? level() : 0;
}

/**
 * 计算并返回物品可见的增强升级等级。如果等级已知，则返回增强后的等级；否则，返回0。
 *
 * @return 物品的可见增强升级等级。
 */
public int buffedVisiblyUpgraded() {
    return levelKnown ? buffedLvl() : 0;
}

/**
 * 判断物品是否明显受到诅咒。物品被明显诅咒当且仅当其处于诅咒状态并且诅咒状态已知。
 *
 * @return 如果物品明显受到诅咒则返回真，否则返回假。
 */
public boolean visiblyCursed() {
    return cursed && cursedKnown;
}

/**
 * 指示物品总是可升级的。
 *
 * @return 总是返回真，表示物品可升级。
 */
public boolean isUpgradable() {
    return true;
}

/**
 * 判断物品是否已被识别。物品被识别当且仅当其等级和诅咒状态均已知。
 *
 * @return 如果物品已被识别则返回真，否则返回假。
 */
public boolean isIdentified() {
    return levelKnown && cursedKnown;
}

/**
 * 判断物品是否被英雄装备。
 *
 * @param hero 英雄对象。
 * @return 如果物品被指定的英雄装备则返回真，否则返回假。
 */
public boolean isEquipped( Hero hero ) {
    return false;
}

/**
 * 标记物品为已识别状态。
 *
 * @return 返回自身引用，用于链式调用。
 */
public final Item identify(){
    return identify(true);
}

/**
 * 标记物品为已识别状态，可以选择由英雄执行识别操作。
 *
 * @param byHero 是否由英雄执行识别操作。
 * @return 返回自身引用，用于链式调用。
 */
public Item identify( boolean byHero ) {

    if (byHero && Dungeon.hero != null && Dungeon.hero.isAlive()){
        Catalog.setSeen(getClass());
        if (!isIdentified()) Talent.onItemIdentified(Dungeon.hero, this);
    }

    levelKnown = true;
    cursedKnown = true;
    Item.updateQuickslot();

    return this;
}

	
	public void onHeroGainExp( float levelPercent, Hero hero ){
		//do nothing by default
	}
	
	/**
	 * 触发英雄的特殊效果。
	 * 该方法通过向英雄的精灵对象发送一个突发信号，来激发特定的视觉效果。
	 * 具体来说，它会创造5个表示“激发”效果的微粒，这些微粒将会瞬间出现在英雄周围，提供一个视觉上的反馈，
	 * 表示某种特殊能力或状态已被激活。
	 *
	 * @param hero 方法的参数，代表需要触发特殊效果的英雄对象。这个参数不应该为null，方法内部也没有对它进行空检查。
	 * 方法直接通过这个参数的成员访问其精灵对象，然后进一步触发效果。
	 *
	 * 注意：该方法没有返回值，它的目的是引发一个视觉效果，而不是返回任何数据。
	 */
	public static void evoke( Hero hero ) {
		// 向英雄的精灵对象发送一个突发信号，创建5个表示“激发”效果的微粒
		hero.sprite.emitter().burst( Speck.factory( Speck.EVOKE ), 5 );
	}

	/**
	 * 获取物品的显示名称。
	 * 如果物品有升级表现，则将升级信息格式化到名称中。
	 * 如果物品的数量大于1，则将数量格式化到名称中。
	 *
	 * @return 格式化后的物品名称。
	 */
	public String title() {
	    // 获取物品的基本名称
	    String name = name();

	    // 如果物品有可见的升级，将升级信息添加到名称中
	    if (visiblyUpgraded() != 0)
	        name = Messages.format( TXT_TO_STRING_LVL, name, visiblyUpgraded()  );

	    // 如果物品数量大于1，将数量添加到名称中
	    if (quantity > 1)
	        name = Messages.format( TXT_TO_STRING_X, name, quantity );

	    return name;
	}

	/**
	 * 获取物品的名称。
	 *
	 * @return 物品的真正名称。
	 */
	public String name() {
	    return trueName();
	}

	/**
	 * 获取物品的真正名称。
	 * 通过Messages获取本地化后的物品名称。
	 *
	 * @return 本地化后的物品名称。
	 */
	public final String trueName() {
	    return Messages.get(this, "name");
	}

	/**
	 * 获取物品的图像索引。
	 *
	 * @return 物品的图像索引。
	 */
	public int image() {
	    return image;
	}

	/**
	 * 获取物品的发光效果。
	 * 当前物品没有发光效果，返回null。
	 *
	 * @return 物品的发光效果。
	 */
	public ItemSprite.Glowing glowing() {
	    return null;
	}

	/**
	 * 获取物品的粒子效果。
	 * 当前物品没有粒子效果，返回null。
	 *
	 * @return 物品的粒子效果。
	 */
	public Emitter emitter() {
	    return null;
	}

	/**
	 * 获取物品的描述信息。
	 * 重定向到desc方法，以获取物品的详细描述。
	 *
	 * @return 物品的描述信息。
	 */
	public String info() {
	    return desc();
	}

	/**
	 * 获取物品的详细描述。
	 * 通过Messages获取本地化后的物品描述。
	 *
	 * @return 本地化后的物品描述。
	 */
	public String desc() {
	    return Messages.get(this, "desc");
	}

	/**
	 * 获取物品的数量。
	 *
	 * @return 物品的数量。
	 */
	public int quantity() {
	    return quantity;
	}

	/**
	 * 设置物品的数量，并返回自身以支持链式调用。
	 *
	 * @param value 新的物品数量。
	 * @return 修改后的物品对象。
	 */
	public Item quantity( int value ) {
	    quantity = value;
	    return this;
	}

	//item's value in gold coins
	public int value() {
		return 0;
	}

	//item's value in energy crystals
	public int energyVal() {
		return 0;
	}
	
	/**
	 * 创建当前类的一个虚拟实例。
	 * <p>
	 * 通过反射机制创建当前类的新实例，用于实现某些特定的功能或测试。虚拟实例是指该实例的一些属性被初始化为特定值，
	 * 例如数量设置为0，等级设置为当前对象的等级，以便于在不改变现有对象状态的情况下，进行操作或测试。
	 *
	 * @return 当前类的一个新实例，如果创建失败则返回null。
	 */
	public Item virtual() {
	    // 通过反射创建当前类的新实例
	    Item item = Reflection.newInstance(getClass());
	    // 如果创建失败，则返回null
	    if (item == null) return null;

	    // 初始化新实例的quantity为0，确保它是一个新对象，不会影响到其他对象的状态
	    item.quantity = 0;
	    // 将新实例的等级设置为当前对象的等级，保持一致性或特定的测试条件
	    item.level = level;
	    // 返回新初始化的实例
	    return item;
	}
	
	public Item random() {
		return this;
	}
	
	public String status() {
		return quantity != 1 ? Integer.toString( quantity ) : null;
	}

	public static void updateQuickslot() {
		GameScene.updateItemDisplays = true;
	}
	
	private static final String QUANTITY		= "quantity";
	private static final String LEVEL			= "level";
	private static final String LEVEL_KNOWN		= "levelKnown";
	private static final String CURSED			= "cursed";
	private static final String CURSED_KNOWN	= "cursedKnown";
	private static final String QUICKSLOT		= "quickslotpos";
	private static final String KEPT_LOST       = "kept_lost";
	
	/**
	 * 将当前物品的状态存储到一个Bundle中。
	 * 此方法用于在游戏保存或加载时，序列化物品的状态。
	 * @param bundle 用于存储物品状态的Bundle对象。
	 */
	@Override
	public void storeInBundle( Bundle bundle ) {
		// 存储物品的数量。
		bundle.put( QUANTITY, quantity );
		// 存储物品的等级。
		bundle.put( LEVEL, level );
		// 存储是否已知物品的等级。
		bundle.put( LEVEL_KNOWN, levelKnown );
		// 存储物品是否被诅咒。
		bundle.put( CURSED, cursed );
		// 存储是否已知物品是否被诅咒。
		bundle.put( CURSED_KNOWN, cursedKnown );
		// 如果物品在快速槽中，存储其在快速槽中的位置。
		if (Dungeon.quickslot.contains(this)) {
			bundle.put( QUICKSLOT, Dungeon.quickslot.getSlot(this) );
		}
		// 存储物品是否在失去后仍被保留。
		bundle.put( KEPT_LOST, keptThoughLostInvent );
	}

	
	/**
	 * 从保存的Bundle中恢复物品状态。
	 * 此方法用于从保存的Bundle中恢复物品的多种属性，包括数量、已知等级、诅咒状态等。
	 * 它还根据Bundle中的等级值处理物品的升级或降级。
	 * 另外，它会检查并恢复物品在快速栏的分配状态以及丢失时的保留状态。
	 *
	 * @param bundle 包含物品保存状态的Bundle对象。
	 */
	@Override
	public void restoreFromBundle( Bundle bundle ) {
		// 从Bundle中恢复基本属性
		quantity	= bundle.getInt( QUANTITY ); // 恢复物品数量
		levelKnown	= bundle.getBoolean( LEVEL_KNOWN ); // 恢复是否已知等级
		cursedKnown	= bundle.getBoolean( CURSED_KNOWN ); // 恢复是否已知诅咒状态

		// 根据Bundle中的等级信息进行升级或降级处理
		int level = bundle.getInt( LEVEL );
		if (level > 0) {
			upgrade( level ); // 升级物品
		} else if (level < 0) {
			degrade( -level ); // 降级物品
		}

		cursed	= bundle.getBoolean( CURSED ); // 恢复诅咒状态

		// 物品首次加载时设置快速使用栏位
		if (Dungeon.hero == null) {
			if (bundle.contains(QUICKSLOT)) {
				Dungeon.quickslot.setSlot(bundle.getInt(QUICKSLOT), this); // 设置快速使用栏位
			}
		}

		keptThoughLostInvent = bundle.getBoolean( KEPT_LOST ); // 恢复丢失时保留状态
	}


	/**
	 * 计算目标位置。
	 * 本方法通过调用throwPos方法来确定英雄应该投掷到的目标位置。
	 * @param user 当前操作的英雄对象。
	 * @param dst 投掷目标的距离。
	 * @return 投掷后预计命中的位置。
	 */
	public int targetingPos( Hero user, int dst ){
	    return throwPos( user, dst );
	}

	/**
	 * 计算投掷位置。
	 * 本方法使用Ballistica类来模拟投掷轨迹，并返回轨迹与目标距离最近的碰撞位置。
	 * @param user 当前操作的英雄对象。
	 * @param dst 投掷目标的距离。
	 * @return 投掷后预计命中的位置。
	 */
	public int throwPos( Hero user, int dst){
	    return new Ballistica( user.pos, dst, Ballistica.PROJECTILE ).collisionPos;
	}

	/**
	 * 播放投掷声音。
	 * 本方法通过Sample类播放投掷动作对应的音效。
	 */
	public void throwSound(){
	    Sample.INSTANCE.play(Assets.Sounds.MISS, 0.6f, 0.6f, 1.5f);
	}
	/**
	 * 执行投掷动作的函数，用于英雄投掷物品。
	 * @param user 投掷的英雄。
	 * @param dst 目标位置。
	 */
	public void cast( final Hero user, final int dst ) {
	    // 计算投掷后物品的位置。
	    final int cell = throwPos( user, dst );
	    // 触发投掷动画。
	    user.sprite.zap( cell );
	    // 设置英雄为忙碌状态，防止其他操作。
	    user.busy();

	    // 播放投掷声音。
	    throwSound();

	    // 查找目标位置的敌人。
	    Char enemy = Actor.findChar( cell );
	    // 设置快捷槽的目标为找到的敌人。
	    QuickSlotButton.target(enemy);

	    // 计算投掷延迟。
	    final float delay = castDelay(user, dst);

	    if (enemy != null) {
	        // 如果有敌人，创建并发射导弹，命中后执行回调。
	        ((MissileSprite) user.sprite.parent.recycle(MissileSprite.class)).
	                reset(user.sprite,
	                        enemy.sprite,
	                        this,
	                        new Callback() {
	                    @Override
	                    public void call() {
	                        // 设置当前操作的用户。
	                        curUser = user;
	                        // 从用户背包中移除并处理投掷的物品。
	                        Item i = Item.this.detach(user.belongings.backpack);
	                        if (i != null) i.onThrow(cell);
	                        // 处理Improvised Projectiles天赋，为敌人施加Blindness效果。
	                        if (curUser.hasTalent(Talent.IMPROVISED_PROJECTILES)
	                                && !(Item.this instanceof MissileWeapon)
	                                && curUser.buff(Talent.ImprovisedProjectileCooldown.class) == null){
	                            if (enemy != null && enemy.alignment != curUser.alignment){
									Sample.INSTANCE.play(Assets.Sounds.HIT);
	                                Buff.affect(enemy, Blindness.class, 1f + curUser.pointsInTalent(Talent.IMPROVISED_PROJECTILES));
	                                Buff.affect(curUser, Talent.ImprovisedProjectileCooldown.class, 50f);
	                            }
	                        }
	                        // 处理Lethal Momentum天赋，移除相关Buff并进入下一轮动作。
	                        if (user.buff(Talent.LethalMomentumTracker.class) != null){
	                            user.buff(Talent.LethalMomentumTracker.class).detach();
	                            user.next();
	                        } else {
	                            // 没有特殊天赋效果，正常消耗时间进入下一轮动作。
	                            user.spendAndNext(delay);
	                        }
	                    }
	                });
	    } else {
	        // 如果没有敌人，直接创建并发射导弹，命中地面后执行回调。
			((MissileSprite) user.sprite.parent.recycle(MissileSprite.class)).
					reset(user.sprite,
							cell,
							this,
                            () -> {
								// 设置当前操作的用户
                                curUser = user;
								// 从用户背包中移除并处理投掷的物品
                                Item i = Item.this.detach(user.belongings.backpack);
                                user.spend(delay);
                                if (i != null) i.onThrow(cell);
                                user.next();
                            });
	    }
	}
	
	public float castDelay( Char user, int dst ){
		return TIME_TO_THROW;
	}
	
	protected static Hero curUser = null;
	protected static Item curItem = null;
	/**
	 * 用于抛出选择项的CellSelector监听器。
	 * 当用户在CellSelector中进行选择时，此监听器将触发相应的行为。
	 * 它是匿名内部类的实例，实现了CellSelector.Listener接口。
	 */
	protected static CellSelector.Listener thrower = new CellSelector.Listener() {
	    /**
	     * 当用户选择一个项时调用此方法。
	     * 如果用户选择了有效的目标（非null），则尝试对当前用户使用当前物品对选定目标进行操作。
	     *
	     * @param target 用户选择的目标，可能为null。
	     */
	    @Override
	    public void onSelect(Integer target) {
	        if (target != null) {
	            curItem.cast(curUser, target);
	        }
	    }

	    /**
	     * 提供一个提示信息，该信息将在CellSelector显示之前显示给用户。
	     * 这个提示信息是从资源文件中获取的，以支持多语言。
	     *
	     * @return 当前项目的提示信息。
	     */
	    @Override
	    public String prompt() {
	        return Messages.get(Item.class, "prompt");
	    }
	};
}
