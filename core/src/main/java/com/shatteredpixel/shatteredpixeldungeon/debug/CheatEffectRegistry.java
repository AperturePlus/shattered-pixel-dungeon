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

import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.watabou.utils.Reflection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public final class CheatEffectRegistry {

	public enum Kind { ENCHANTMENT, GLYPH }
	public enum Rarity { COMMON, UNCOMMON, RARE, CURSE }

	public static final class Entry {
		public final String id;
		public final Kind kind;
		public final Rarity rarity;
		public final Class<?> effectClass;

		private Entry(Kind kind, Rarity rarity, Class<?> effectClass) {
			this.kind = kind;
			this.rarity = rarity;
			this.effectClass = effectClass;
			this.id = effectClass.getSimpleName().toLowerCase(Locale.ENGLISH);
		}

		public Object create() {
			return Reflection.newInstance(effectClass);
		}

		public String displayName() {
			Object effect = create();
			if (effect instanceof Weapon.Enchantment) return ((Weapon.Enchantment) effect).name();
			if (effect instanceof Armor.Glyph) return ((Armor.Glyph) effect).name();
			return id;
		}

		public boolean isCurse() {
			return rarity == Rarity.CURSE;
		}
	}

	private static List<Entry> enchantments;
	private static List<Entry> glyphs;

	private CheatEffectRegistry() {
	}

	public static synchronized List<Entry> enchantments() {
		ensureInitialized();
		return enchantments;
	}

	public static synchronized List<Entry> glyphs() {
		ensureInitialized();
		return glyphs;
	}

	public static Entry find(Kind kind, Class<?> effectClass) {
		if (effectClass == null) return null;
		for (Entry entry : kind == Kind.ENCHANTMENT ? enchantments() : glyphs()) {
			if (entry.effectClass == effectClass) return entry;
		}
		return null;
	}

	private static void ensureInitialized() {
		if (enchantments != null) return;
		ArrayList<Entry> ench = new ArrayList<>();
		addAll(ench, Kind.ENCHANTMENT, Rarity.COMMON, Weapon.Enchantment.common);
		addAll(ench, Kind.ENCHANTMENT, Rarity.UNCOMMON, Weapon.Enchantment.uncommon);
		addAll(ench, Kind.ENCHANTMENT, Rarity.RARE, Weapon.Enchantment.rare);
		addAll(ench, Kind.ENCHANTMENT, Rarity.CURSE, Weapon.Enchantment.curses);
		enchantments = Collections.unmodifiableList(ench);

		ArrayList<Entry> gly = new ArrayList<>();
		addAll(gly, Kind.GLYPH, Rarity.COMMON, Armor.Glyph.common);
		addAll(gly, Kind.GLYPH, Rarity.UNCOMMON, Armor.Glyph.uncommon);
		addAll(gly, Kind.GLYPH, Rarity.RARE, Armor.Glyph.rare);
		addAll(gly, Kind.GLYPH, Rarity.CURSE, Armor.Glyph.curses);
		glyphs = Collections.unmodifiableList(gly);
	}

	private static void addAll(List<Entry> target, Kind kind, Rarity rarity, Class<?>[] classes) {
		for (Class<?> effectClass : classes) target.add(new Entry(kind, rarity, effectClass));
	}
}
