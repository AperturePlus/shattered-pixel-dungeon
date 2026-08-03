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
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.Bag;
import com.shatteredpixel.shatteredpixeldungeon.items.keys.Key;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTeleportation;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.InterlevelScene;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.Game;
import com.watabou.utils.DeviceCompat;
import com.watabou.utils.Point;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;

public final class CheatService {

	public static final int MAX_QUANTITY = 999;
	public static final int MIN_ITEM_LEVEL = -10;
	public static final int MAX_ITEM_LEVEL = 100;

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

	public static Result give(CheatItemRegistry.Entry entry, int quantity, int level,
			boolean cursed, boolean identified) {
		Result availability = requireAvailable();
		if (availability != null) return availability;
		if (entry == null) return failure("unknown_item", "");
		if (quantity < 1 || quantity > MAX_QUANTITY) return failure("quantity_range", MAX_QUANTITY);
		if (level < MIN_ITEM_LEVEL || level > MAX_ITEM_LEVEL) {
			return failure("level_range", MIN_ITEM_LEVEL, MAX_ITEM_LEVEL);
		}

		Item item = entry.create();
		if (item == null) return failure("item_create_failed", entry.id);
		if (!item.stackable && quantity != 1) return failure("not_stackable", entry.id);
		if (!item.isUpgradable() && level != 0) return failure("not_upgradable", entry.id);

		item.quantity(quantity);
		if (item.isUpgradable() && !applyLevel(item, level)) return failure("level_unreachable", entry.id, level);
		item.cursed = cursed;
		if (identified) item.identify(false);
		else {
			item.levelKnown = false;
			item.cursedKnown = false;
		}
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
		return success("item_given", entry.id, quantity, level);
	}

	public static Result setItemQuantity(String slot, int quantity) {
		Result availability = requireAvailable();
		if (availability != null) return availability;
		Item item = resolveSlot(slot);
		if (item == null) return failure("invalid_slot", slot);
		if (!item.stackable) return failure("not_stackable", slot);
		if (quantity < 1 || quantity > MAX_QUANTITY) return failure("quantity_range", MAX_QUANTITY);
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
		Result availability = requireAvailable();
		if (availability != null) return availability;
		Item item = resolveSlot(slot);
		if (item == null) return failure("invalid_slot", slot);
		if (item.stackable && (quantity < 1 || quantity > MAX_QUANTITY)) {
			return failure("quantity_range", MAX_QUANTITY);
		}
		if (item.isUpgradable()) {
			if (level < MIN_ITEM_LEVEL || level > MAX_ITEM_LEVEL) {
				return failure("level_range", MIN_ITEM_LEVEL, MAX_ITEM_LEVEL);
			}
			Item testItem = item.duplicate();
			if (testItem == null || !applyLevel(testItem, level)) return failure("level_unreachable", slot, level);
		}
		if (!identified && !canBeUnidentified(item)) return failure("always_identified", slot);

		if (item.stackable) item.quantity(quantity);
		if (item.isUpgradable()) applyLevel(item, level);
		item.cursed = cursed;
		item.cursedKnown = true;
		if (identified) item.identify(false);
		else {
			item.levelKnown = false;
			item.cursedKnown = false;
		}
		Item.updateQuickslot();
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
		if (depth < 1 || depth > 26) return failure("floor_range");
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
