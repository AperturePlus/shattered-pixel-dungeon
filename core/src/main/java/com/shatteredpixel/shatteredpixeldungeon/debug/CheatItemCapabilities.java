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
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ScrollOfEnchantment;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.SpiritBow;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MagesStaff;

public final class CheatItemCapabilities {

	public final boolean quantity;
	public final boolean level;
	public final boolean identification;
	public final boolean weaponEffects;
	public final boolean armorEffects;
	public final boolean hardening;
	public final boolean mastery;
	public final boolean curseInfusion;
	public final boolean wandEffects;
	public final boolean staffImbue;

	private CheatItemCapabilities(Item item) {
		quantity = item.stackable;
		level = item.isUpgradable();
		identification = CheatService.canBeUnidentified(item);
		weaponEffects = item instanceof Weapon && ScrollOfEnchantment.enchantable(item);
		armorEffects = item instanceof Armor && ScrollOfEnchantment.enchantable(item);
		hardening = item.isUpgradable() && (item instanceof Weapon || item instanceof Armor);
		mastery = (item instanceof Weapon && !(item instanceof SpiritBow)) || item instanceof Armor;
		curseInfusion = (item instanceof Weapon && (item.isUpgradable() || item instanceof SpiritBow))
				|| item instanceof Armor && item.isUpgradable()
				|| item instanceof Wand;
		wandEffects = item instanceof Wand;
		staffImbue = item instanceof MagesStaff;
	}

	public static CheatItemCapabilities forItem(Item item) {
		return new CheatItemCapabilities(item);
	}

	public boolean hasEffects() {
		return weaponEffects || armorEffects || hardening || mastery || curseInfusion
				|| wandEffects || staffImbue;
	}
}
