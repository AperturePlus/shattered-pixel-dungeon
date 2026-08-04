/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2026 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.debug;

import com.badlogic.gdx.utils.Os;
import com.badlogic.gdx.utils.SharedLibraryLoader;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Roots;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Belongings;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.Bag;
import com.shatteredpixel.shatteredpixeldungeon.items.keys.Key;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTeleportation;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.SpiritBow;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MagesStaff;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.InterlevelScene;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.Game;
import com.watabou.utils.DeviceCompat;
import com.watabou.utils.Point;
import com.watabou.utils.Reflection;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;

public final class CheatService {

	public static final int MIN_QUANTITY = 1;
	public static final int MAX_QUANTITY = 999;
	public static final int MIN_ITEM_LEVEL = -10;
	public static final int MAX_ITEM_LEVEL = 100;
	public static final int MIN_MAIN_FLOOR = 1;
	public static final int MAX_MAIN_FLOOR = 26;

	public static final class Result {
		public final boolean success;
		public final String message;

		private Result(boolean success, String message) {
			this.success = success;
			this.message = message;
		}
	}

	private CheatService() {
	}

	public static boolean isAvailable() {
		return SharedLibraryLoader.os == Os.Windows
				&& Game.version != null
				&& DeviceCompat.isDebug()
				&& Game.scene() instanceof GameScene
				&& Dungeon.hero != null
				&& Dungeon.hero.isAlive()
				&& Dungeon.level != null;
	}

	public static Result execute(String input) {
		Result availability = requireAvailable();
		if (availability != null) return availability;
		if (input == null || input.trim().isEmpty()) return failure("empty_command");

		String[] args = input.trim().split("\\s+");
		String command = args[0].toLowerCase(Locale.ENGLISH);
		try {
			switch (command) {
			case "help":
					return successText(Messages.get(CheatService.class, "help"));
				case "hp":
					if (args.length != 2) return usage("hp_usage");
					if ("full".equalsIgnoreCase(args[1])) return setHP(Dungeon.hero.HT);
					return setHP(parseInt(args[1]));
				case "maxhp":
					if (args.length != 2) return usage("maxhp_usage");
					return setMaxHP(parseInt(args[1]));
				case "gold":
					if (args.length != 2) return usage("gold_usage");
					return setGold(parseInt(args[1]));
				case "talents":
					if (args.length != 1) return usage("talents_usage");
					return listTalents();
				case "talent":
					if (args.length != 3) return usage("talent_usage");
					return setTalentPoints(findTalent(args[1]), parseInt(args[2]), args[1]);
				case "catalog":
					return catalog(join(args, 1));
				case "give":
					return executeGive(args);
				case "items":
					if (args.length != 1) return usage("items_usage");
					return listItems();
				case "item":
					return executeItem(args);
				case "tp":
					if (args.length == 2 && "select".equalsIgnoreCase(args[1])) return beginTeleportSelection();
					if (args.length != 3) return usage("tp_usage");
					return teleport(parseInt(args[1]), parseInt(args[2]));
				case "floor":
					if (args.length != 2) return usage("floor_usage");
					return changeFloor(parseInt(args[1]));
				default:
					return failure("unknown_command", command);
			}
		} catch (NumberFormatException e) {
			return failure("invalid_number");
		}
	}

	public static void log(Result result) {
		if (result.success) GLog.i(result.message);
		else GLog.w(result.message);
	}

	public static Result setHP(int hp) {
		Result availability = requireAvailable();
		if (availability != null) return availability;
		if (hp < 1 || hp > Dungeon.hero.HT) return failure("hp_range", Dungeon.hero.HT);
		Dungeon.hero.HP = hp;
		return success("hp_set", hp, Dungeon.hero.HT);
	}

	public static Result setMaxHP(int maxHP) {
		Result availability = requireAvailable();
		if (availability != null) return availability;
		if (maxHP < 1) return failure("maxhp_range");

		Hero hero = Dungeon.hero;
		int previousHP = hero.HP;
		int previousHT = hero.HT;
		int previousBoost = hero.HTBoost;

		// Calculate the active level/item/buff contribution without the persistent cheat boost.
		hero.HTBoost = 0;
		hero.updateHT(false);
		int htWithoutBoost = hero.HT;

		hero.HP = previousHP;
		hero.HT = previousHT;
		hero.HTBoost = previousBoost;

		long updatedBoost = (long)maxHP - htWithoutBoost;
		if (updatedBoost < Integer.MIN_VALUE || updatedBoost > Integer.MAX_VALUE) {
			return failure("maxhp_range");
		}

		hero.HTBoost = (int)updatedBoost;
		hero.updateHT(false);
		return success("maxhp_set", hero.HT);
	}

	public static Result setGold(int gold) {
		Result availability = requireAvailable();
		if (availability != null) return availability;
		if (gold < 0) return failure("gold_range");
		Dungeon.gold = gold;
		return success("gold_set", gold);
	}

	public static LinkedHashMap<Talent, Integer> talentSlots() {
		LinkedHashMap<Talent, Integer> talents = new LinkedHashMap<>();
		if (Dungeon.hero == null) return talents;
		for (LinkedHashMap<Talent, Integer> tier : Dungeon.hero.talents) {
			talents.putAll(tier);
		}
		return talents;
	}

	public static Talent findTalent(String id) {
		if (id == null) return null;
		String normalized = id.trim().toUpperCase(Locale.ENGLISH).replace('-', '_');
		try {
			Talent talent = Talent.valueOf(normalized);
			return talentSlots().containsKey(talent) ? talent : null;
		} catch (IllegalArgumentException e) {
			return null;
		}
	}

	public static Result setTalentPoints(Talent talent, int points) {
		return setTalentPoints(talent, points, talent == null ? "" : talentId(talent));
	}

	private static Result setTalentPoints(Talent talent, int points, String requestedId) {
		Result availability = requireAvailable();
		if (availability != null) return availability;
		if (talent == null) return failure("unknown_talent", requestedId);

		LinkedHashMap<Talent, Integer> containingTier = null;
		for (LinkedHashMap<Talent, Integer> tier : Dungeon.hero.talents) {
			if (tier.containsKey(talent)) {
				containingTier = tier;
				break;
			}
		}
		if (containingTier == null) return failure("unknown_talent", requestedId);
		if (points < 0 || points > talent.maxPoints()) {
			return failure("talent_range", talentId(talent), talent.maxPoints());
		}

		int previous = containingTier.get(talent);
		if (points > previous) {
			for (int i = previous; i < points; i++) {
				Dungeon.hero.upgradeTalent(talent);
			}
		} else if (points < previous) {
			containingTier.put(talent, points);
		}

		Dungeon.hero.updateHT(false);
		Dungeon.observe();
		GameScene.updateFog();
		Item.updateQuickslot();
		return success("talent_set", talentId(talent), points, talent.maxPoints());
	}

	public static Result give(CheatItemRegistry.Entry entry, int quantity, int level,
			boolean cursed, boolean identified) {
		if (entry == null) return failure("unknown_item", "");
		Item preview = entry.create();
		if (preview == null) return failure("item_create_failed", entry.id);
		CheatItemConfig config = CheatItemConfig.forNewItem(preview);
		config.quantity = quantity;
		config.level = level;
		config.cursed = cursed;
		config.identified = identified;
		return give(entry, config);
	}

	public static Result give(CheatItemRegistry.Entry entry, CheatItemConfig config) {
		Result availability = requireAvailable();
		if (availability != null) return availability;
		if (entry == null) return failure("unknown_item", "");

		Item item = entry.create();
		if (item == null) return failure("item_create_failed", entry.id);
		if (item.unique && Dungeon.hero.belongings.getItem(item.getClass()) != null) {
			return failure("unique_item", entry.id);
		}
		PreparedConfig prepared = prepareConfig(item, config, false, entry.id);
		if (prepared.error != null) return prepared.error;
		applyConfig(item, config, prepared);
		if (item instanceof Key) ((Key) item).depth = Dungeon.depth;

		boolean stored = addWithoutPickupSideEffects(item, Dungeon.hero.belongings.backpack);
		if (!stored) {
			Heap heap = Dungeon.level.drop(item, Dungeon.hero.pos);
			if (heap.sprite != null) heap.sprite.drop(Dungeon.hero.pos);
			Item.updateQuickslot();
			return success("item_dropped", entry.id);
		}
		Item.updateQuickslot();
		GameScene.updateKeyDisplay();
		return success("item_given", entry.id, config.quantity, config.level);
	}

	public static Result setItemQuantity(String slot, int quantity) {
		Result availability = requireAvailable();
		if (availability != null) return availability;
		Item item = resolveSlot(slot);
		if (item == null) return failure("invalid_slot", slot);
		if (!item.stackable) return failure("not_stackable", slot);
		if (quantity < MIN_QUANTITY || quantity > MAX_QUANTITY) return failure("quantity_range", MAX_QUANTITY);
		item.quantity(quantity);
		Item.updateQuickslot();
		return success("item_updated", slot);
	}

	public static Result setItemLevel(String slot, int level) {
		Result availability = requireAvailable();
		if (availability != null) return availability;
		Item item = resolveSlot(slot);
		if (item == null) return failure("invalid_slot", slot);
		if (!item.isUpgradable()) return failure("not_upgradable", slot);
		if (level < MIN_ITEM_LEVEL || level > MAX_ITEM_LEVEL) {
			return failure("level_range", MIN_ITEM_LEVEL, MAX_ITEM_LEVEL);
		}
		Item testItem = item.duplicate();
		if (testItem == null || !applyLevel(testItem, level)) return failure("level_unreachable", slot, level);
		applyLevel(item, level);
		Item.updateQuickslot();
		return success("item_updated", slot);
	}

	public static Result setItemIdentified(String slot, boolean identified) {
		Result availability = requireAvailable();
		if (availability != null) return availability;
		Item item = resolveSlot(slot);
		if (item == null) return failure("invalid_slot", slot);
		if (identified) {
			item.identify(false);
		} else {
			if (!canBeUnidentified(item)) return failure("always_identified", slot);
			item.levelKnown = false;
			item.cursedKnown = false;
		}
		Item.updateQuickslot();
		return success("item_updated", slot);
	}

	public static boolean canBeUnidentified(Item item) {
		if (item == null) return false;
		boolean oldLevelKnown = item.levelKnown;
		boolean oldCursedKnown = item.cursedKnown;
		item.levelKnown = false;
		item.cursedKnown = false;
		boolean result = !item.isIdentified();
		item.levelKnown = oldLevelKnown;
		item.cursedKnown = oldCursedKnown;
		return result;
	}

	public static Result setItemCursed(String slot, boolean cursed) {
		Result availability = requireAvailable();
		if (availability != null) return availability;
		Item item = resolveSlot(slot);
		if (item == null) return failure("invalid_slot", slot);
		item.cursed = cursed;
		item.cursedKnown = true;
		Item.updateQuickslot();
		return success("item_updated", slot);
	}

	public static Result updateItem(String slot, int quantity, int level, boolean identified, boolean cursed) {
		Item item = resolveSlot(slot);
		if (item == null) return failure("invalid_slot", slot);
		CheatItemConfig config = CheatItemConfig.fromItem(item);
		config.quantity = quantity;
		config.level = level;
		config.identified = identified;
		config.cursed = cursed;
		if (!cursed) cleanseConfig(config);
		return updateItem(slot, config);
	}

	public static Result updateItem(String slot, CheatItemConfig config) {
		Result availability = requireAvailable();
		if (availability != null) return availability;
		Item item = resolveSlot(slot);
		if (item == null) return failure("invalid_slot", slot);
		PreparedConfig prepared = prepareConfig(item, config, true, slot);
		if (prepared.error != null) return prepared.error;
		applyConfig(item, config, prepared);
		refreshItemState();
		return success("item_updated", slot);
	}

	public static Result deleteItem(String slot) {
		Result availability = requireAvailable();
		if (availability != null) return availability;
		if (slot == null || !slot.toLowerCase(Locale.ENGLISH).startsWith("bag:")) {
			return failure("equipped_delete");
		}
		Item item = resolveSlot(slot);
		if (item == null) return failure("invalid_slot", slot);
		item.detachAll(Dungeon.hero.belongings.backpack);
		Item.updateQuickslot();
		GameScene.updateKeyDisplay();
		return success("item_deleted", slot);
	}

	public static LinkedHashMap<String, Item> itemSlots() {
		LinkedHashMap<String, Item> slots = new LinkedHashMap<>();
		if (Dungeon.hero == null) return slots;
		Belongings b = Dungeon.hero.belongings;
		if (b.weapon != null) slots.put("weapon", b.weapon);
		if (b.armor != null) slots.put("armor", b.armor);
		if (b.ring != null) slots.put("ring", b.ring);
		if (b.artifact != null) slots.put("artifact", b.artifact);
		if (b.misc != null) slots.put("misc", b.misc);
		if (b.secondWep != null) slots.put("second", b.secondWep);
		for (int i = 0; i < b.backpack.items.size(); i++) slots.put("bag:" + i, b.backpack.items.get(i));
		return slots;
	}

	public static Item resolveSlot(String slot) {
		if (slot == null) return null;
		return itemSlots().get(slot.toLowerCase(Locale.ENGLISH));
	}

	private static final class PreparedConfig {
		private Result error;
		private Weapon.Enchantment enchantment;
		private Armor.Glyph glyph;
		private Wand staffWand;
		private int wandMaxCharges;
		private int staffWandMaxCharges;
	}

	private static PreparedConfig prepareConfig(Item item, CheatItemConfig config,
			boolean editing, String itemId) {
		PreparedConfig prepared = new PreparedConfig();
		if (config == null) {
			prepared.error = failure("invalid_item_config");
			return prepared;
		}
		CheatItemCapabilities capabilities = CheatItemCapabilities.forItem(item);
		boolean weaponItem = item instanceof Weapon;
		boolean armorItem = item instanceof Armor;
		boolean wandItem = item instanceof Wand;
		boolean staffItem = item instanceof MagesStaff;
		if (config.quantity < MIN_QUANTITY || config.quantity > MAX_QUANTITY) {
			prepared.error = failure("quantity_range", MAX_QUANTITY);
			return prepared;
		}
		if (!item.stackable && config.quantity != MIN_QUANTITY) {
			prepared.error = failure("not_stackable", itemId);
			return prepared;
		}
		if (item.isUpgradable() && (config.level < MIN_ITEM_LEVEL || config.level > MAX_ITEM_LEVEL)) {
			prepared.error = failure("level_range", MIN_ITEM_LEVEL, MAX_ITEM_LEVEL);
			return prepared;
		}
		int fixedLevel = editing ? item.trueLevel() : 0;
		if (!item.isUpgradable() && config.level != fixedLevel) {
			prepared.error = failure("not_upgradable", itemId);
			return prepared;
		}
		if (!config.identified && !canBeUnidentified(item)) {
			prepared.error = failure("always_identified", itemId);
			return prepared;
		}
		if ((config.enchantHardened || config.glyphHardened) && !config.identified) {
			prepared.error = failure("harden_requires_identified");
			return prepared;
		}

		Item levelProbe = item.duplicate();
		if (levelProbe == null || item.isUpgradable() && !applyLevel(levelProbe, config.level)) {
			prepared.error = failure("level_unreachable", itemId, config.level);
			return prepared;
		}

		if (!capabilities.weaponEffects && config.enchantmentMode != CheatItemConfig.EffectMode.NONE) {
			prepared.error = failure("effect_unavailable", itemId);
			return prepared;
		}
		if (!capabilities.armorEffects && config.glyphMode != CheatItemConfig.EffectMode.NONE) {
			prepared.error = failure("effect_unavailable", itemId);
			return prepared;
		}
		if (!capabilities.hardening && (config.enchantHardened || config.glyphHardened)
				|| !capabilities.mastery && (config.weaponMastery || config.armorMastery)
				|| !capabilities.curseInfusion && (config.weaponCurseInfusion
				|| config.armorCurseInfusion || config.wandCurseInfusion)) {
			prepared.error = failure("effect_unavailable", itemId);
			return prepared;
		}
		if (!weaponItem && (config.weaponAugment != Weapon.Augment.NONE || config.enchantHardened
				|| config.weaponCurseInfusion || config.weaponMastery)
				|| !armorItem && (config.armorAugment != Armor.Augment.NONE || config.glyphHardened
				|| config.armorCurseInfusion || config.armorMastery)
				|| !wandItem && (config.wandCurseInfusion || config.wandResinBonus != 0 || config.wandCharges != 0)
				|| !staffItem && (config.staffWandClass != null || config.staffWandCharges != 0)) {
			prepared.error = failure("effect_unavailable", itemId);
			return prepared;
		}
		if (config.weaponAugment == null) config.weaponAugment = Weapon.Augment.NONE;
		if (config.armorAugment == null) config.armorAugment = Armor.Augment.NONE;

		prepared.enchantment = createEnchantment(config);
		if (config.enchantmentMode != CheatItemConfig.EffectMode.NONE && prepared.enchantment == null) {
			prepared.error = failure("effect_create_failed");
			return prepared;
		}
		prepared.glyph = createGlyph(config);
		if (config.glyphMode != CheatItemConfig.EffectMode.NONE && prepared.glyph == null) {
			prepared.error = failure("effect_create_failed");
			return prepared;
		}

		boolean curseEnchant = prepared.enchantment != null && prepared.enchantment.curse();
		boolean curseGlyph = prepared.glyph != null && prepared.glyph.curse();
		if ((curseEnchant || curseGlyph) && !config.cursed) {
			prepared.error = failure("curse_effect_requires_cursed");
			return prepared;
		}
		if (config.weaponCurseInfusion && (!config.cursed || !curseEnchant)
				|| config.armorCurseInfusion && (!config.cursed || !curseGlyph)
				|| config.wandCurseInfusion && !config.cursed) {
			prepared.error = failure("infusion_requires_curse");
			return prepared;
		}

		if (item instanceof Wand) {
			if (!config.identified && config.wandResinBonus != 0) {
				prepared.error = failure("resin_requires_identified");
				return prepared;
			}
			int maxResin = config.level >= 0 && config.level <= 2 ? 3 - config.level : 0;
			if (config.wandResinBonus < 0 || config.wandResinBonus > maxResin) {
				prepared.error = failure("resin_range", maxResin);
				return prepared;
			}
			Wand probe = (Wand) levelProbe;
			probe.resinBonus = config.wandResinBonus;
			probe.updateLevel();
			prepared.wandMaxCharges = probe.maxCharges;
			if (config.wandCharges < 0 || config.wandCharges > prepared.wandMaxCharges) {
				prepared.error = failure("charge_range", prepared.wandMaxCharges);
				return prepared;
			}
		}

		if (item instanceof MagesStaff) {
			MagesStaff staff = (MagesStaff) item;
			if (editing && staff.wandClass() != null && config.staffWandClass == null) {
				prepared.error = failure("staff_wand_remove");
				return prepared;
			}
			if (config.staffWandClass != null) {
				if (!isCatalogWand(config.staffWandClass)) {
					prepared.error = failure("effect_unavailable", itemId);
					return prepared;
				}
				prepared.staffWand = Reflection.newInstance(config.staffWandClass);
				if (prepared.staffWand == null) {
					prepared.error = failure("effect_create_failed");
					return prepared;
				}
				Wand probeWand = Reflection.newInstance(config.staffWandClass);
				if (probeWand == null) {
					prepared.error = failure("effect_create_failed");
					return prepared;
				}
				MagesStaff probe = new MagesStaff(probeWand);
				if (!applyLevel(probe, config.level)) {
					prepared.error = failure("level_unreachable", itemId, config.level);
					return prepared;
				}
				prepared.staffWandMaxCharges = probe.wand().maxCharges;
				if (config.staffWandCharges < 0 || config.staffWandCharges > prepared.staffWandMaxCharges) {
					prepared.error = failure("charge_range", prepared.staffWandMaxCharges);
					return prepared;
				}
			}
		}
		return prepared;
	}

	@SuppressWarnings("unchecked")
	private static Weapon.Enchantment createEnchantment(CheatItemConfig config) {
		switch (config.enchantmentMode) {
			case NONE:
				return null;
			case RANDOM_GOOD:
				return Weapon.Enchantment.random();
			case RANDOM_CURSE:
				return Weapon.Enchantment.randomCurse();
			case EXACT:
				CheatEffectRegistry.Entry entry = CheatEffectRegistry.find(
						CheatEffectRegistry.Kind.ENCHANTMENT, config.enchantmentClass);
				return entry == null ? null : (Weapon.Enchantment) entry.create();
			default:
				return null;
		}
	}

	private static Armor.Glyph createGlyph(CheatItemConfig config) {
		switch (config.glyphMode) {
			case NONE:
				return null;
			case RANDOM_GOOD:
				return Armor.Glyph.random();
			case RANDOM_CURSE:
				return Armor.Glyph.randomCurse();
			case EXACT:
				CheatEffectRegistry.Entry entry = CheatEffectRegistry.find(
						CheatEffectRegistry.Kind.GLYPH, config.glyphClass);
				return entry == null ? null : (Armor.Glyph) entry.create();
			default:
				return null;
		}
	}

	private static void applyConfig(Item item, CheatItemConfig config, PreparedConfig prepared) {
		item.quantity(config.quantity);
		if (item.isUpgradable()) applyLevel(item, config.level);
		item.cursed = config.cursed;

		if (item instanceof MagesStaff) {
			MagesStaff staff = (MagesStaff) item;
			if (config.staffWandClass != null && staff.wandClass() != config.staffWandClass) {
				if (staff.wand() != null) staff.wand().stopCharging();
				staff.imbueWand(prepared.staffWand, null);
			}
			if (staff.wand() != null) {
				staff.wand().curCharges = Math.min(config.staffWandCharges, staff.wand().maxCharges);
			}
		}

		if (item instanceof Weapon) {
			Weapon weapon = (Weapon) item;
			weapon.enchant(prepared.enchantment);
			weapon.augment = config.weaponAugment;
			weapon.enchantHardened = config.enchantHardened;
			weapon.masteryPotionBonus = config.weaponMastery && !(weapon instanceof SpiritBow);
			weapon.curseInfusionBonus = config.weaponCurseInfusion;
			if (weapon instanceof MagesStaff) ((MagesStaff) weapon).updateWand(false);
		}
		if (item instanceof Armor) {
			Armor armor = (Armor) item;
			armor.inscribe(prepared.glyph);
			armor.augment = config.armorAugment;
			armor.glyphHardened = config.glyphHardened;
			armor.masteryPotionBonus = config.armorMastery;
			armor.curseInfusionBonus = config.armorCurseInfusion;
		}
		if (item instanceof Wand) {
			Wand wand = (Wand) item;
			wand.curseInfusionBonus = config.wandCurseInfusion;
			wand.resinBonus = config.wandResinBonus;
			wand.updateLevel();
			wand.curCharges = config.wandCharges;
		}

		if (config.identified) {
			item.identify(false);
		} else {
			item.levelKnown = false;
			item.cursedKnown = false;
			if (item instanceof Wand) ((Wand) item).curChargeKnown = false;
		}
	}

	private static void cleanseConfig(CheatItemConfig config) {
		if (config.enchantmentMode == CheatItemConfig.EffectMode.EXACT
				&& config.enchantmentClass != null) {
			CheatEffectRegistry.Entry entry = CheatEffectRegistry.find(
					CheatEffectRegistry.Kind.ENCHANTMENT, config.enchantmentClass);
			if (entry != null && entry.isCurse()) {
				config.enchantmentMode = CheatItemConfig.EffectMode.NONE;
				config.enchantmentClass = null;
			}
		} else if (config.enchantmentMode == CheatItemConfig.EffectMode.RANDOM_CURSE) {
			config.enchantmentMode = CheatItemConfig.EffectMode.NONE;
		}
		if (config.glyphMode == CheatItemConfig.EffectMode.EXACT && config.glyphClass != null) {
			CheatEffectRegistry.Entry entry = CheatEffectRegistry.find(
					CheatEffectRegistry.Kind.GLYPH, config.glyphClass);
			if (entry != null && entry.isCurse()) {
				config.glyphMode = CheatItemConfig.EffectMode.NONE;
				config.glyphClass = null;
			}
		} else if (config.glyphMode == CheatItemConfig.EffectMode.RANDOM_CURSE) {
			config.glyphMode = CheatItemConfig.EffectMode.NONE;
		}
		config.weaponCurseInfusion = false;
		config.armorCurseInfusion = false;
		config.wandCurseInfusion = false;
	}

	private static boolean isCatalogWand(Class<? extends Wand> wandClass) {
		for (CheatItemRegistry.Entry entry : CheatItemRegistry.entries(com.shatteredpixel.shatteredpixeldungeon.journal.Catalog.WANDS)) {
			if (entry.itemClass == wandClass) return true;
		}
		return false;
	}

	private static void refreshItemState() {
		Item.updateQuickslot();
		GameScene.updateKeyDisplay();
		if (Dungeon.hero != null) Dungeon.hero.updateHT(false);
		Dungeon.observe();
		GameScene.updateFog();
	}

	public static Result teleport(int x, int y) {
		Result availability = requireAvailable();
		if (availability != null) return availability;
		if (x < 0 || x >= Dungeon.level.width() || y < 0 || y >= Dungeon.level.height()) {
			return failure("tp_bounds", Dungeon.level.width() - 1, Dungeon.level.height() - 1);
		}
		return teleportCell(Dungeon.level.pointToCell(new Point(x, y)));
	}

	public static Result teleportCell(int cell) {
		Result availability = requireAvailable();
		if (availability != null) return availability;
		if (cell < 0 || cell >= Dungeon.level.length()) return failure("tp_invalid_cell");
		if ((!Dungeon.level.passable[cell] && !Dungeon.level.avoid[cell])
				|| (Actor.findChar(cell) != null && Actor.findChar(cell) != Dungeon.hero)) {
			return failure("tp_invalid_cell");
		}
		ScrollOfTeleportation.appear(Dungeon.hero, cell);
		Dungeon.level.occupyCell(Dungeon.hero);
		Buff.detach(Dungeon.hero, Roots.class);
		Dungeon.observe();
		GameScene.updateFog();
		Dungeon.hero.interrupt();
		return success("tp_done", cell % Dungeon.level.width(), cell / Dungeon.level.width());
	}

	public static Result beginTeleportSelection() {
		Result availability = requireAvailable();
		if (availability != null) return availability;
		GameScene.selectCell(new CellSelector.Listener() {
			@Override
			public void onSelect(Integer cell) {
				if (cell != null) log(teleportCell(cell));
			}

			@Override
			public String prompt() {
				return Messages.get(CheatService.class, "tp_prompt");
			}
		});
		return success("tp_selecting");
	}

	public static Result changeFloor(int depth) {
		Result availability = requireAvailable();
		if (availability != null) return availability;
		if (depth < MIN_MAIN_FLOOR || depth > MAX_MAIN_FLOOR) return failure("floor_range");
		if (Dungeon.depth == depth && Dungeon.branch == 0) return failure("floor_same");
		InterlevelScene.mode = InterlevelScene.Mode.RETURN;
		InterlevelScene.returnDepth = depth;
		InterlevelScene.returnBranch = 0;
		InterlevelScene.returnPos = -1;
		Game.switchScene(InterlevelScene.class);
		return success("floor_changing", depth);
	}

	private static Result executeGive(String[] args) {
		if (args.length < 2) return usage("give_usage");
		CheatItemRegistry.Entry entry = CheatItemRegistry.find(args[1]);
		if (entry == null) return failure("unknown_item", args[1]);
		int quantity = 1;
		int level = 0;
		boolean cursed = false;
		boolean identified = true;
		int numeric = 0;
		for (int i = 2; i < args.length; i++) {
			if ("--cursed".equalsIgnoreCase(args[i])) cursed = true;
			else if ("--unidentified".equalsIgnoreCase(args[i])) identified = false;
			else if (numeric++ == 0) quantity = parseInt(args[i]);
			else if (numeric == 2) level = parseInt(args[i]);
			else return usage("give_usage");
		}
		return give(entry, quantity, level, cursed, identified);
	}

	private static Result executeItem(String[] args) {
		if (args.length < 3) return usage("item_usage");
		String slot = args[1].toLowerCase(Locale.ENGLISH);
		String operation = args[2].toLowerCase(Locale.ENGLISH);
		if ("delete".equals(operation) && args.length == 3) return deleteItem(slot);
		if (args.length != 4) return usage("item_usage");
		switch (operation) {
			case "qty":
				return setItemQuantity(slot, parseInt(args[3]));
			case "level":
				return setItemLevel(slot, parseInt(args[3]));
			case "identified":
				return setItemIdentified(slot, parseBoolean(args[3]));
			case "cursed":
				return setItemCursed(slot, parseBoolean(args[3]));
			default:
				return usage("item_usage");
		}
	}

	private static Result catalog(String filter) {
		List<CheatItemRegistry.Entry> matches = CheatItemRegistry.search(filter);
		if (matches.isEmpty()) return failure("catalog_empty", filter);
		StringBuilder output = new StringBuilder();
		int shown = Math.min(30, matches.size());
		for (int i = 0; i < shown; i++) {
			CheatItemRegistry.Entry entry = matches.get(i);
			if (i > 0) output.append('\n');
			output.append(entry.id).append(" — ").append(entry.displayName());
		}
		if (matches.size() > shown) output.append('\n').append(Messages.get(CheatService.class, "catalog_more", matches.size() - shown));
		return successText(output.toString());
	}

	private static Result listTalents() {
		StringBuilder output = new StringBuilder();
		for (int i = 0; i < Dungeon.hero.talents.size(); i++) {
			LinkedHashMap<Talent, Integer> tier = Dungeon.hero.talents.get(i);
			if (tier.isEmpty()) continue;
			if (output.length() > 0) output.append('\n');
			output.append(Messages.get(CheatService.class, "talent_tier", i + 1));
			for (Talent talent : tier.keySet()) {
				output.append('\n').append(talentId(talent)).append(": ")
						.append(talent.title()).append(" +").append(tier.get(talent))
						.append('/').append(talent.maxPoints());
			}
		}
		return successText(output.toString());
	}

	private static Result listItems() {
		LinkedHashMap<String, Item> slots = itemSlots();
		if (slots.isEmpty()) return successText(Messages.get(CheatService.class, "items_empty"));
		StringBuilder output = new StringBuilder();
		for (String slot : slots.keySet()) {
			Item item = slots.get(slot);
			if (output.length() > 0) output.append('\n');
			output.append(slot).append(": ").append(item.name())
					.append(" x").append(item.quantity()).append(" ").append(String.format(Locale.ENGLISH, "%+d", item.trueLevel()));
		}
		return successText(output.toString());
	}

	public static String talentId(Talent talent) {
		return talent.name().toLowerCase(Locale.ENGLISH);
	}

	private static boolean addWithoutPickupSideEffects(Item item, Bag container) {
		if (item.unique && Dungeon.hero.belongings.getItem(item.getClass()) != null) return false;
		if (item.stackable) {
			for (Item existing : container.items) {
				if (item.isSimilar(existing)) {
					existing.merge(item);
					return true;
				}
			}
		}
		for (Item existing : container.items) {
			if (existing instanceof Bag && ((Bag) existing).canHold(item)
					&& addWithoutPickupSideEffects(item, (Bag) existing)) return true;
		}
		if (!container.canHold(item)) return false;
		container.items.add(item);
		if (item instanceof Bag) ((Bag) item).owner = Dungeon.hero;
		Collections.sort(container.items, Item.itemComparator);
		Dungeon.quickslot.replacePlaceholder(item);
		return true;
	}

	private static boolean applyLevel(Item item, int targetLevel) {
		int difference = targetLevel - item.trueLevel();
		if (difference > 0) item.upgrade(difference);
		else if (difference < 0) item.degrade(-difference);
		return item.trueLevel() == targetLevel;
	}

	private static boolean parseBoolean(String value) {
		if ("true".equalsIgnoreCase(value)) return true;
		if ("false".equalsIgnoreCase(value)) return false;
		throw new NumberFormatException("not a boolean");
	}

	private static int parseInt(String value) {
		return Integer.parseInt(value);
	}

	private static String join(String[] args, int start) {
		if (start >= args.length) return "";
		StringBuilder result = new StringBuilder();
		for (int i = start; i < args.length; i++) {
			if (result.length() > 0) result.append(' ');
			result.append(args[i]);
		}
		return result.toString();
	}

	private static Result requireAvailable() {
		return isAvailable() ? null : failure("unavailable");
	}

	private static Result usage(String key) {
		return new Result(false, Messages.get(CheatService.class, key));
	}

	private static Result successText(String text) {
		return new Result(true, text);
	}

	private static Result success(String key, Object... args) {
		return new Result(true, Messages.get(CheatService.class, key, args));
	}

	private static Result failure(String key, Object... args) {
		return new Result(false, Messages.get(CheatService.class, key, args));
	}
}
