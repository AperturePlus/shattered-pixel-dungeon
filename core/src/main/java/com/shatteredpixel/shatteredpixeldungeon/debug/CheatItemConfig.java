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

import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.SpiritBow;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MagesStaff;

public class CheatItemConfig {

	public enum EffectMode {
		NONE,
		RANDOM_GOOD,
		RANDOM_CURSE,
		EXACT
	}

	public int quantity = CheatService.MIN_QUANTITY;
	public int level = 0;
	public boolean identified = true;
	public boolean cursed = false;

	public EffectMode enchantmentMode = EffectMode.NONE;
	public Class<? extends Weapon.Enchantment> enchantmentClass;
	public Weapon.Augment weaponAugment = Weapon.Augment.NONE;
	public boolean enchantHardened;
	public boolean weaponCurseInfusion;
	public boolean weaponMastery;

	public EffectMode glyphMode = EffectMode.NONE;
	public Class<? extends Armor.Glyph> glyphClass;
	public Armor.Augment armorAugment = Armor.Augment.NONE;
	public boolean glyphHardened;
	public boolean armorCurseInfusion;
	public boolean armorMastery;

	public boolean wandCurseInfusion;
	public int wandResinBonus;
	public int wandCharges;

	public Class<? extends Wand> staffWandClass;
	public int staffWandCharges;

	public static CheatItemConfig forNewItem(Item item) {
		CheatItemConfig config = fromItem(item);
		config.quantity = CheatService.MIN_QUANTITY;
		config.level = 0;
		config.identified = true;
		config.cursed = false;
		if (item instanceof Wand) {
			config.wandCharges = ((Wand) item).maxCharges;
		}
		return config;
	}

	@SuppressWarnings("unchecked")
	public static CheatItemConfig fromItem(Item item) {
		CheatItemConfig config = new CheatItemConfig();
		config.quantity = item.quantity();
		config.level = item.trueLevel();
		config.identified = item.isIdentified();
		config.cursed = item.cursed;

		if (item instanceof Weapon) {
			Weapon weapon = (Weapon) item;
			config.weaponAugment = weapon.augment;
			config.enchantHardened = weapon.enchantHardened;
			config.weaponCurseInfusion = weapon.curseInfusionBonus;
			config.weaponMastery = weapon.masteryPotionBonus && !(weapon instanceof SpiritBow);
			if (weapon.enchantment != null) {
				config.enchantmentMode = EffectMode.EXACT;
				config.enchantmentClass = (Class<? extends Weapon.Enchantment>) weapon.enchantment.getClass();
			}
		}
		if (item instanceof Armor) {
			Armor armor = (Armor) item;
			config.armorAugment = armor.augment;
			config.glyphHardened = armor.glyphHardened;
			config.armorCurseInfusion = armor.curseInfusionBonus;
			config.armorMastery = armor.masteryPotionBonus;
			if (armor.glyph != null) {
				config.glyphMode = EffectMode.EXACT;
				config.glyphClass = (Class<? extends Armor.Glyph>) armor.glyph.getClass();
			}
		}
		if (item instanceof Wand) {
			Wand wand = (Wand) item;
			config.wandCurseInfusion = wand.curseInfusionBonus;
			config.wandResinBonus = wand.resinBonus;
			config.wandCharges = wand.curCharges;
		}
		if (item instanceof MagesStaff) {
			Wand wand = ((MagesStaff) item).wand();
			if (wand != null) {
				config.staffWandClass = (Class<? extends Wand>) wand.getClass();
				config.staffWandCharges = wand.curCharges;
			}
		}
		return config;
	}

	public CheatItemConfig copy() {
		CheatItemConfig copy = new CheatItemConfig();
		copy.quantity = quantity;
		copy.level = level;
		copy.identified = identified;
		copy.cursed = cursed;
		copy.enchantmentMode = enchantmentMode;
		copy.enchantmentClass = enchantmentClass;
		copy.weaponAugment = weaponAugment;
		copy.enchantHardened = enchantHardened;
		copy.weaponCurseInfusion = weaponCurseInfusion;
		copy.weaponMastery = weaponMastery;
		copy.glyphMode = glyphMode;
		copy.glyphClass = glyphClass;
		copy.armorAugment = armorAugment;
		copy.glyphHardened = glyphHardened;
		copy.armorCurseInfusion = armorCurseInfusion;
		copy.armorMastery = armorMastery;
		copy.wandCurseInfusion = wandCurseInfusion;
		copy.wandResinBonus = wandResinBonus;
		copy.wandCharges = wandCharges;
		copy.staffWandClass = staffWandClass;
		copy.staffWandCharges = staffWandCharges;
		return copy;
	}
}
