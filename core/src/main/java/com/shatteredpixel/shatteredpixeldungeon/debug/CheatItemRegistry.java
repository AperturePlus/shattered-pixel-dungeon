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
import com.shatteredpixel.shatteredpixeldungeon.journal.Catalog;
import com.watabou.utils.Reflection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;

public final class CheatItemRegistry {

	public static final class Entry {
		public final String id;
		public final Catalog category;
		public final Class<? extends Item> itemClass;

		private Entry(String id, Catalog category, Class<? extends Item> itemClass) {
			this.id = id;
			this.category = category;
			this.itemClass = itemClass;
		}

		public Item create() {
			return Reflection.newInstance(itemClass);
		}

		public String displayName() {
			Item item = create();
			return item == null ? id : item.name();
		}
	}

	private static LinkedHashMap<String, Entry> entries;

	private CheatItemRegistry() {
	}

	public static synchronized Collection<Entry> entries() {
		ensureInitialized();
		return Collections.unmodifiableCollection(entries.values());
	}

	public static synchronized List<Entry> entries(Catalog category) {
		ensureInitialized();
		ArrayList<Entry> result = new ArrayList<>();
		for (Entry entry : entries.values()) {
			if (entry.category == category) result.add(entry);
		}
		return result;
	}

	public static synchronized Entry find(String query) {
		ensureInitialized();
		if (query == null) return null;
		String normalized = query.trim().toLowerCase(Locale.ENGLISH);
		Entry direct = entries.get(normalized);
		if (direct != null) return direct;
		for (Entry entry : entries.values()) {
			if (entry.displayName().toLowerCase(MessagesLocale.locale()).equals(normalized)) return entry;
		}
		return null;
	}

	public static synchronized List<Entry> search(String query) {
		ensureInitialized();
		String normalized = query == null ? "" : query.trim().toLowerCase(MessagesLocale.locale());
		ArrayList<Entry> result = new ArrayList<>();
		for (Entry entry : entries.values()) {
			if (normalized.isEmpty()
					|| entry.id.contains(normalized)
					|| entry.displayName().toLowerCase(MessagesLocale.locale()).contains(normalized)) {
				result.add(entry);
			}
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	private static void ensureInitialized() {
		if (entries != null) return;
		entries = new LinkedHashMap<>();
		for (Catalog category : Catalog.values()) {
			for (Class<?> candidate : category.items()) {
				if (!Item.class.isAssignableFrom(candidate)) continue;
				Class<? extends Item> itemClass = (Class<? extends Item>) candidate;
				Item probe = Reflection.newInstance(itemClass);
				if (probe == null) continue;
				String id = itemClass.getSimpleName().toLowerCase(Locale.ENGLISH);
				entries.putIfAbsent(id, new Entry(id, category, itemClass));
			}
		}
		ArrayList<Entry> sorted = new ArrayList<>(entries.values());
		sorted.sort(Comparator.comparing(entry -> entry.id));
		entries.clear();
		for (Entry entry : sorted) entries.put(entry.id, entry);
	}

	/* Keeps locale access isolated so registry IDs always remain English-stable. */
	private static final class MessagesLocale {
		private static Locale locale() {
			Locale locale = com.shatteredpixel.shatteredpixeldungeon.messages.Messages.locale();
			return locale == null ? Locale.ENGLISH : locale;
		}
	}
}
