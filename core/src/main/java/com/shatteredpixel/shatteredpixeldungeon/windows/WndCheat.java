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

package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.debug.CheatEffectRegistry;
import com.shatteredpixel.shatteredpixeldungeon.debug.CheatItemRegistry;
import com.shatteredpixel.shatteredpixeldungeon.debug.CheatItemCapabilities;
import com.shatteredpixel.shatteredpixeldungeon.debug.CheatItemConfig;
import com.shatteredpixel.shatteredpixeldungeon.debug.CheatService;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.stones.StoneOfAugmentation;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MagesStaff;
import com.shatteredpixel.shatteredpixeldungeon.journal.Catalog;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.CheckBox;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.ScrollPane;
import com.shatteredpixel.shatteredpixeldungeon.ui.ScrollingGridPane;
import com.shatteredpixel.shatteredpixeldungeon.ui.TalentIcon;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.Gizmo;
import com.watabou.noosa.Image;
import com.watabou.noosa.ui.Component;
import com.watabou.utils.Reflection;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class WndCheat extends WndTabbed {

	private static final int WIDTH = 180;
	private static final int HEIGHT = 150;
	private static final int GAP = 3;
	private static final int BTN_HEIGHT = 18;

	private final PlayerPage playerPage;
	private final ItemsPage itemsPage;
	private final InventoryPage inventoryPage;
	private final TeleportPage teleportPage;

	public WndCheat() {
		resize(WIDTH, HEIGHT);
		playerPage = new PlayerPage();
		itemsPage = new ItemsPage();
		inventoryPage = new InventoryPage();
		teleportPage = new TeleportPage();

		addPage(playerPage);
		addPage(itemsPage);
		addPage(inventoryPage);
		addPage(teleportPage);

		add(new LabeledTab(Messages.get(this, "player")) {
			@Override
			protected void select(boolean value) {
				super.select(value);
				playerPage.visible = playerPage.active = selected;
				if (selected) playerPage.rebuild();
			}
		});
		add(new LabeledTab(Messages.get(this, "items")) {
			@Override
			protected void select(boolean value) {
				super.select(value);
				itemsPage.visible = itemsPage.active = selected;
				if (selected) itemsPage.rebuild();
			}
		});
		add(new LabeledTab(Messages.get(this, "inventory")) {
			@Override
			protected void select(boolean value) {
				super.select(value);
				inventoryPage.visible = inventoryPage.active = selected;
				if (selected) inventoryPage.rebuild();
			}
		});
		add(new LabeledTab(Messages.get(this, "teleport")) {
			@Override
			protected void select(boolean value) {
				super.select(value);
				teleportPage.visible = teleportPage.active = selected;
				if (selected) teleportPage.rebuild();
			}
		});
		layoutTabs();
		select(0);
	}

	private void addPage(Component page) {
		super.add(page);
		page.setRect(0, 0, WIDTH, HEIGHT);
		page.visible = page.active = false;
	}

	private abstract class RebuildablePage extends Component {
		protected final ScrollPane pane;
		protected final DisposableContent content;

		protected RebuildablePage() {
			content = new DisposableContent();
			pane = new ScrollPane(content);
			add(pane);
		}

		@Override
		protected void layout() {
			pane.setRect(x, y, width, height);
		}

		protected abstract void rebuild();
	}

	private static class DisposableContent extends Component {
		private synchronized void clearAndDestroy() {
			for (int i = 0; i < length; i++) {
				Gizmo child = members.get(i);
				if (child != null) child.destroy();
			}
			clear();
		}
	}

	private class PlayerPage extends RebuildablePage {
		@Override
		protected void rebuild() {
			content.clearAndDestroy();
			float top = 2;
			RenderedTextBlock title = label(Messages.get(WndCheat.this, "player_status",
					Dungeon.hero.HP, Dungeon.hero.HT, Dungeon.gold), 9, Window.TITLE_COLOR);
			title.setPos(2, top);
			content.add(title);
			top = title.bottom() + 6;

			IntegerControl hp = new IntegerControl(Messages.get(WndCheat.this, "set_hp"),
					Dungeon.hero.HP, 1, Dungeon.hero.HT,
					value -> Messages.get(WndCheat.this, "hp_value", value, Dungeon.hero.HT),
					value -> finish(CheatService.setHP(value), this::rebuild));
			hp.setRect(0, top, WIDTH, BTN_HEIGHT);
			content.add(hp);
			top = hp.bottom() + GAP;

			RedButton full = button(Messages.get(WndCheat.this, "full_hp"), () ->
					finish(CheatService.setHP(Dungeon.hero.HT), this::rebuild));
			full.setRect(0, top, WIDTH, BTN_HEIGHT);
			content.add(full);
			top = full.bottom() + GAP;

			IntegerControl maxHP = new IntegerControl(Messages.get(WndCheat.this, "set_max_hp"),
					Dungeon.hero.HT, 1, Integer.MAX_VALUE,
					value -> Messages.get(WndCheat.this, "max_hp_value", value),
					value -> finish(CheatService.setMaxHP(value), this::rebuild));
			maxHP.setRect(0, top, WIDTH, BTN_HEIGHT);
			content.add(maxHP);
			top = maxHP.bottom() + GAP;

			IntegerControl gold = new IntegerControl(Messages.get(WndCheat.this, "set_gold"),
					Dungeon.gold, 0, Integer.MAX_VALUE,
					value -> Messages.get(WndCheat.this, "gold_value", value),
					value -> finish(CheatService.setGold(value), this::rebuild));
			gold.setRect(0, top, WIDTH, BTN_HEIGHT);
			content.add(gold);
			top = gold.bottom() + GAP;

			RedButton talents = button(Messages.get(WndCheat.this, "edit_talents"), () ->
					GameScene.show(new WndCheatTalents()));
			talents.setRect(0, top, WIDTH, BTN_HEIGHT);
			content.add(talents);
			top = talents.bottom() + GAP;

			RedButton console = button(Messages.get(WndCheat.this, "console"), () -> GameScene.show(new WndCheatConsole()));
			console.setRect(0, top, WIDTH, BTN_HEIGHT);
			content.add(console);
			content.setRect(0, 0, WIDTH, console.bottom());
			pane.setRect(0, 0, WIDTH, HEIGHT);
		}
	}

	private static class WndCheatTalents extends Window {
		private static final int ROW_HEIGHT = 28;

		private final DisposableContent content;
		private final ScrollPane pane;

		private WndCheatTalents() {
			content = new DisposableContent();
			pane = new ScrollPane(content);
			add(pane);
			resize(WIDTH, HEIGHT);
			pane.setRect(0, 0, WIDTH, HEIGHT);
			rebuild();
		}

		private void rebuild() {
			content.clearAndDestroy();
			float top = 2;
			for (int i = 0; i < Dungeon.hero.talents.size(); i++) {
				LinkedHashMap<Talent, Integer> tier = Dungeon.hero.talents.get(i);
				if (tier.isEmpty()) continue;

				RenderedTextBlock heading = labelStatic(
						Messages.get(WndCheat.class, "talent_tier", i + 1), 8, TITLE_COLOR);
				heading.setPos(2, top);
				content.add(heading);
				top = heading.bottom() + 2;

				for (Talent talent : tier.keySet()) {
					int points = tier.get(talent);
					IntegerControl row = new IntegerControl(
							Messages.get(WndCheat.class, "talent_points", talent.title()),
							points, 0, talent.maxPoints(),
							value -> Messages.get(WndCheat.class, "talent_row", talent.title(),
									CheatService.talentId(talent), value, talent.maxPoints()),
							value -> {
								CheatService.Result result = CheatService.setTalentPoints(talent, value);
								CheatService.log(result);
								if (result.success) rebuild();
								return result.success;
							}, 6);
					row.multiline(true);
					row.leftJustify(true);
					row.icon(new TalentIcon(talent));
					row.setRect(0, top, WIDTH, ROW_HEIGHT);
					content.add(row);
					top = row.bottom() + 1;
				}
				top += GAP;
			}
			content.setRect(0, 0, WIDTH, Math.max(top, HEIGHT));
			pane.setRect(0, 0, WIDTH, HEIGHT);
		}
	}

	private enum Filter { ALL, EQUIPMENT, CONSUMABLE }

	private class ItemsPage extends Component {

		private final RedButton search;
		private final RedButton clear;
		private final RedButton[] filters = new RedButton[Filter.values().length];
		private final ScrollingGridPane grid;
		private final float[] scrollPositions = new float[Filter.values().length];
		private String query = "";
		private Filter filter = Filter.ALL;

		private ItemsPage() {
			search = button(Messages.get(WndCheat.this, "search"), this::askSearch, 7);
			add(search);
			clear = button(Messages.get(WndCheat.this, "clear"), () -> {
				query = "";
				scrollPositions[filter.ordinal()] = 0;
				rebuild();
			}, 7);
			add(clear);

			String[] labels = new String[]{Messages.get(WndCheat.this, "filter_all"),
					Messages.get(WndCheat.this, "filter_equipment"),
					Messages.get(WndCheat.this, "filter_consumables")};
			for (int i = 0; i < filters.length; i++) {
				final Filter selected = Filter.values()[i];
				filters[i] = button(labels[i], () -> {
					filter = selected;
					rebuild();
				}, 7);
				add(filters[i]);
			}

			grid = new ScrollingGridPane() {
				@Override
				public synchronized void update() {
					super.update();
					scrollPositions[filter.ordinal()] = content().camera.scroll.y;
				}
			};
			add(grid);
		}

		@Override
		protected void layout() {
			float searchWidth = width - 42;
			search.setRect(x, y, searchWidth, BTN_HEIGHT);
			clear.setRect(search.right() + 2, y, width - searchWidth - 2, BTN_HEIGHT);
			float filterWidth = width / filters.length;
			for (int i = 0; i < filters.length; i++) {
				filters[i].setRect(x + i * filterWidth, search.bottom() + 1, filterWidth, BTN_HEIGHT);
			}
			grid.setRect(x, filters[0].bottom() + 1, width, height - 2 * BTN_HEIGHT - 2);
		}

		private void askSearch() {
			GameScene.show(new WndTextInput(Messages.get(WndCheat.this, "search_title"),
					Messages.get(WndCheat.this, "search_hint"), query, 40, false,
					Messages.get(WndCheat.this, "apply"), Messages.get(WndCheat.this, "cancel")) {
				@Override
				public void onSelect(boolean positive, String text) {
					if (!positive) return;
					query = text == null ? "" : text.trim();
					scrollPositions[filter.ordinal()] = 0;
					rebuild();
				}
			});
		}

		protected void rebuild() {
			grid.clear();
			CheatItemRegistry.Group group = filter == Filter.EQUIPMENT
					? CheatItemRegistry.Group.EQUIPMENT
					: filter == Filter.CONSUMABLE ? CheatItemRegistry.Group.CONSUMABLE : null;
			List<CheatItemRegistry.Entry> matches = CheatItemRegistry.search(query, group);
			search.text(query.isEmpty() ? Messages.get(WndCheat.this, "search")
					: Messages.get(WndCheat.this, "search_value", query));
			clear.enable(!query.isEmpty());
			for (int i = 0; i < filters.length; i++) {
				filters[i].textColor(i == filter.ordinal() ? TITLE_COLOR : Window.WHITE);
			}
			grid.addHeader(Messages.get(WndCheat.this, "match_count", matches.size()), 8, true);
			if (matches.isEmpty()) {
				grid.addHeader(Messages.get(WndCheat.this, "no_matches"), 7, true);
			} else {
				for (Catalog category : Catalog.values()) {
					ArrayList<CheatItemRegistry.Entry> entries = new ArrayList<>();
					for (CheatItemRegistry.Entry entry : matches) {
						if (entry.category == category) entries.add(entry);
					}
					if (entries.isEmpty()) continue;
					grid.addHeader(Messages.titleCase(category.title()) + " (" + entries.size() + ")");
					for (CheatItemRegistry.Entry entry : entries) {
						Item preview = entry.create();
						if (preview == null) continue;
						grid.addItem(new ScrollingGridPane.GridItem(new ItemSprite(preview)) {
							@Override
							public boolean onClick(float x, float y) {
								if (!inside(x, y)) return false;
								GameScene.show(new WndCheatItemConfig(entry));
								return true;
							}
						});
					}
				}
			}
			grid.setRect(grid.left(), grid.top(), grid.width(), grid.height());
			grid.scrollTo(0, scrollPositions[filter.ordinal()]);
		}
	}

	private class InventoryPage extends RebuildablePage {
		@Override
		protected void rebuild() {
			content.clearAndDestroy();
			float top = 1;
			LinkedHashMap<String, Item> slots = CheatService.itemSlots();
			if (slots.isEmpty()) {
				RenderedTextBlock empty = label(Messages.get(WndCheat.this, "inventory_empty"), 8, Window.TITLE_COLOR);
				empty.setPos(2, top);
				content.add(empty);
				top = empty.bottom();
			} else {
				for (String slot : slots.keySet()) {
					Item item = slots.get(slot);
					String status = item.stackable ? " x" + item.quantity() : " " + String.format("%+d", item.trueLevel());
					RedButton row = button(slot + ": " + item.name() + status, () ->
							GameScene.show(new WndCheatItemConfig(slot, this::rebuild)), 7);
					row.leftJustify = true;
					row.icon(new ItemSprite(item));
					row.setRect(0, top, WIDTH, BTN_HEIGHT);
					content.add(row);
					top = row.bottom() + 1;
				}
			}
			content.setRect(0, 0, WIDTH, top);
			pane.setRect(0, 0, WIDTH, HEIGHT);
		}
	}

	private class TeleportPage extends RebuildablePage {
		private int targetFloor = Dungeon.depth;

		@Override
		protected void rebuild() {
			content.clearAndDestroy();
			float top = 2;
			RenderedTextBlock info = label(Messages.get(WndCheat.this, "tp_info", Dungeon.depth), 8, Window.TITLE_COLOR);
			info.maxWidth(WIDTH - 4);
			info.setPos(2, top);
			content.add(info);
			top = info.bottom() + 6;
			RedButton select = button(Messages.get(WndCheat.this, "tp_select"), () -> {
				hide();
				CheatService.log(CheatService.beginTeleportSelection());
			});
			select.setRect(0, top, WIDTH, BTN_HEIGHT);
			content.add(select);
			top = select.bottom() + GAP;
			RedButton coordinates = button(Messages.get(WndCheat.this, "tp_coordinates"), () ->
					GameScene.show(new WndCheatCoordinates()));
			coordinates.setRect(0, top, WIDTH, BTN_HEIGHT);
			content.add(coordinates);
			top = coordinates.bottom() + GAP;
			IntegerControl floor = new IntegerControl(Messages.get(WndCheat.this, "tp_floor"),
					targetFloor, CheatService.MIN_MAIN_FLOOR, CheatService.MAX_MAIN_FLOOR,
					value -> Messages.get(WndCheat.this, "floor_value", value),
					value -> {
						targetFloor = value;
						return true;
					});
			floor.setRect(0, top, WIDTH, BTN_HEIGHT);
			content.add(floor);
			top = floor.bottom() + GAP;

			RedButton jump = button(Messages.get(WndCheat.this, "tp_floor_apply"), () ->
					finish(CheatService.changeFloor(targetFloor), null));
			jump.setRect(0, top, WIDTH, BTN_HEIGHT);
			content.add(jump);
			content.setRect(0, 0, WIDTH, jump.bottom());
			pane.setRect(0, 0, WIDTH, HEIGHT);
		}
	}

	private static class WndCheatItemConfig extends Window {
		private static final int FORM_WIDTH = 160;
		private static final int FORM_HEIGHT = 150;

		private final CheatItemRegistry.Entry entry;
		private final String slot;
		private final Runnable refreshParent;
		private final Item item;
		private final CheatItemCapabilities capabilities;
		private final CheatItemConfig config;
		private final DisposableContent content = new DisposableContent();
		private final ScrollPane pane = new ScrollPane(content);

		private WndCheatItemConfig(CheatItemRegistry.Entry entry) {
			this.entry = entry;
			this.slot = null;
			this.refreshParent = null;
			this.item = entry.create();
			this.capabilities = CheatItemCapabilities.forItem(item);
			this.config = CheatItemConfig.forNewItem(item);
			initialize();
		}

		private WndCheatItemConfig(String slot, Runnable refreshParent) {
			this.entry = null;
			this.slot = slot;
			this.refreshParent = refreshParent;
			this.item = CheatService.resolveSlot(slot);
			this.capabilities = CheatItemCapabilities.forItem(item);
			this.config = CheatItemConfig.fromItem(item);
			config.quantity = Math.max(CheatService.MIN_QUANTITY,
					Math.min(CheatService.MAX_QUANTITY, config.quantity));
			if (capabilities.level) {
				config.level = Math.max(CheatService.MIN_ITEM_LEVEL,
						Math.min(CheatService.MAX_ITEM_LEVEL, config.level));
			}
			initialize();
		}

		private void initialize() {
			add(pane);
			resize(FORM_WIDTH, FORM_HEIGHT);
			pane.setRect(0, 0, FORM_WIDTH, FORM_HEIGHT);
			normalizeRanges();
			rebuild();
		}

		private void rebuild() {
			content.clearAndDestroy();
			float top = 2;
			String titleText = slot == null ? item.name() + " [" + entry.id + "]" : slot + ": " + item.name();
			RenderedTextBlock title = labelStatic(titleText, 9, TITLE_COLOR);
			title.maxWidth(FORM_WIDTH - 4);
			title.setPos(2, top);
			content.add(title);
			top = title.bottom() + GAP;

			top = addHeading(Messages.get(WndCheat.class, "basic_attributes"), top);
			IntegerControl quantity = new IntegerControl(Messages.get(WndCheat.class, "quantity"),
					config.quantity, CheatService.MIN_QUANTITY, CheatService.MAX_QUANTITY,
					value -> Messages.get(WndCheat.class, "quantity_value", value), value -> {
						config.quantity = value;
						return true;
					});
			quantity.enable(capabilities.quantity);
			top = addRow(quantity, top);

			int levelMin = Math.min(CheatService.MIN_ITEM_LEVEL, config.level);
			int levelMax = Math.max(CheatService.MAX_ITEM_LEVEL, config.level);
			IntegerControl level = new IntegerControl(Messages.get(WndCheat.class, "level"), config.level,
					levelMin, levelMax,
					value -> Messages.get(WndCheat.class, "level_value", value), value -> {
						config.level = value;
						normalizeRanges();
						rebuild();
						return true;
					});
			level.enable(capabilities.level);
			top = addRow(level, top);

			top = addCheck(Messages.get(WndCheat.class, "identified"), config.identified,
					capabilities.identification, value -> {
						config.identified = value;
						if (!value) {
							config.enchantHardened = false;
							config.glyphHardened = false;
							config.wandResinBonus = 0;
						}
						normalizeRanges();
						rebuild();
					}, top);
			top = addCheck(Messages.get(WndCheat.class, "cursed"), config.cursed, true, value -> {
				config.cursed = value;
				if (!value) cleanseEffects();
				rebuild();
			}, top);

			if (capabilities.hasEffects()) {
				top += GAP;
				top = addHeading(Messages.get(WndCheat.class, "attached_effects"), top);
			}
			if (capabilities.weaponEffects) {
				top = addSelectButton(Messages.get(WndCheat.class, "enchantment_value",
						effectName(true)), () -> openEffectSelector(true), top);
				top = addSelectButton(Messages.get(WndCheat.class, "augment_value",
						augmentName(config.weaponAugment)), () -> openWeaponAugment(), top);
			}
			if (capabilities.armorEffects) {
				top = addSelectButton(Messages.get(WndCheat.class, "glyph_value",
						effectName(false)), () -> openEffectSelector(false), top);
				top = addSelectButton(Messages.get(WndCheat.class, "augment_value",
						augmentName(config.armorAugment)), () -> openArmorAugment(), top);
			}
			if (capabilities.hardening && item instanceof Weapon) {
				top = addCheck(Messages.get(WndCheat.class, "hardened"), config.enchantHardened,
						config.identified, value -> config.enchantHardened = value, top);
			}
			if (capabilities.hardening && item instanceof Armor) {
				top = addCheck(Messages.get(WndCheat.class, "hardened"), config.glyphHardened,
						config.identified, value -> config.glyphHardened = value, top);
			}
			if (capabilities.mastery && item instanceof Weapon) {
				top = addCheck(Messages.get(WndCheat.class, "mastery"), config.weaponMastery,
						true, value -> config.weaponMastery = value, top);
			}
			if (capabilities.mastery && item instanceof Armor) {
				top = addCheck(Messages.get(WndCheat.class, "mastery"), config.armorMastery,
						true, value -> config.armorMastery = value, top);
			}
			if (capabilities.curseInfusion) {
				boolean infusion = item instanceof Weapon ? config.weaponCurseInfusion
						: item instanceof Armor ? config.armorCurseInfusion : config.wandCurseInfusion;
				top = addCheck(Messages.get(WndCheat.class, "curse_infusion"), infusion, true,
						this::setCurseInfusion, top);
			}
			if (capabilities.wandEffects) {
				int maxResin = maxResin();
				IntegerControl resin = new IntegerControl(Messages.get(WndCheat.class, "resin"),
						config.wandResinBonus, 0, maxResin,
						value -> Messages.get(WndCheat.class, "resin_value", value), value -> {
							config.wandResinBonus = value;
							normalizeRanges();
							rebuild();
							return true;
						});
				resin.enable(config.identified && maxResin > 0);
				top = addRow(resin, top);
				int maxCharges = maxWandCharges();
				IntegerControl charges = new IntegerControl(Messages.get(WndCheat.class, "charges"),
						config.wandCharges, 0, maxCharges,
						value -> Messages.get(WndCheat.class, "charges_value", value, maxCharges), value -> {
							config.wandCharges = value;
							return true;
						});
				top = addRow(charges, top);
			}
			if (capabilities.staffImbue) {
				top = addSelectButton(Messages.get(WndCheat.class, "staff_wand_value", staffWandName()),
						this::openStaffWandSelector, top);
				if (config.staffWandClass != null) {
					int maxCharges = maxStaffWandCharges();
					IntegerControl charges = new IntegerControl(Messages.get(WndCheat.class, "charges"),
							config.staffWandCharges, 0, maxCharges,
							value -> Messages.get(WndCheat.class, "charges_value", value, maxCharges), value -> {
								config.staffWandCharges = value;
								return true;
							});
					top = addRow(charges, top);
				}
			}

			top += GAP;
			RedButton apply = new RedButton(Messages.get(WndCheat.class, slot == null ? "give" : "apply")) {
				@Override
				protected void onClick() {
					CheatService.Result result = slot == null
							? CheatService.give(entry, config.copy())
							: CheatService.updateItem(slot, config.copy());
					CheatService.log(result);
					if (result.success) {
						hide();
						if (refreshParent != null) refreshParent.run();
					}
				}
			};
			if (slot == null) {
				apply.setRect(0, top, FORM_WIDTH, BTN_HEIGHT);
				content.add(apply);
			} else {
				apply.setRect(0, top, (FORM_WIDTH - GAP) / 2f, BTN_HEIGHT);
				content.add(apply);
				RedButton delete = new RedButton(Messages.get(WndCheat.class, "delete")) {
					@Override
					protected void onClick() {
						CheatService.Result result = CheatService.deleteItem(slot);
						CheatService.log(result);
						if (result.success) {
							hide();
							if (refreshParent != null) refreshParent.run();
						}
					}
				};
				delete.enable(slot.startsWith("bag:"));
				delete.setRect(apply.right() + GAP, top, FORM_WIDTH - apply.right() - GAP, BTN_HEIGHT);
				content.add(delete);
			}
			content.setRect(0, 0, FORM_WIDTH, apply.bottom() + 2);
			pane.setRect(0, 0, FORM_WIDTH, FORM_HEIGHT);
		}

		private float addHeading(String text, float top) {
			RenderedTextBlock heading = labelStatic(text, 8, TITLE_COLOR);
			heading.setPos(2, top);
			content.add(heading);
			return heading.bottom() + 2;
		}

		private float addRow(Component component, float top) {
			component.setRect(0, top, FORM_WIDTH, BTN_HEIGHT);
			content.add(component);
			return component.bottom() + 1;
		}

		private float addSelectButton(String text, Runnable action, float top) {
			RedButton button = new RedButton(text, 7) {
				@Override protected void onClick() { action.run(); }
			};
			button.leftJustify = true;
			return addRow(button, top);
		}

		private float addCheck(String text, boolean checked, boolean enabled,
				BoolConsumer handler, float top) {
			CheckBox checkBox = new CheckBox(text) {
				@Override
				protected void onClick() {
					super.onClick();
					handler.accept(checked());
				}
			};
			checkBox.checked(checked);
			checkBox.enable(enabled);
			return addRow(checkBox, top);
		}

		private void openEffectSelector(boolean weapon) {
			GameScene.show(new WndCheatEffectSelect(weapon, (mode, effectClass) -> {
				if (weapon) {
					config.enchantmentMode = mode;
					config.enchantmentClass = effectClass == null ? null
							: effectClass.asSubclass(Weapon.Enchantment.class);
					if (!isCurseSelection(true)) config.weaponCurseInfusion = false;
				} else {
					config.glyphMode = mode;
					config.glyphClass = effectClass == null ? null : effectClass.asSubclass(Armor.Glyph.class);
					if (!isCurseSelection(false)) config.armorCurseInfusion = false;
				}
				if (isCurseSelection(weapon)) config.cursed = true;
				rebuild();
			}));
		}

		private void openWeaponAugment() {
			Weapon.Augment[] values = Weapon.Augment.values();
			String[] labels = new String[values.length];
			for (int i = 0; i < values.length; i++) labels[i] = augmentName(values[i]);
			GameScene.show(new WndCheatOptionSelect(Messages.get(WndCheat.class, "augment"), labels, index -> {
				config.weaponAugment = values[index];
				rebuild();
			}));
		}

		private void openArmorAugment() {
			Armor.Augment[] values = Armor.Augment.values();
			String[] labels = new String[values.length];
			for (int i = 0; i < values.length; i++) labels[i] = augmentName(values[i]);
			GameScene.show(new WndCheatOptionSelect(Messages.get(WndCheat.class, "augment"), labels, index -> {
				config.armorAugment = values[index];
				rebuild();
			}));
		}

		private void openStaffWandSelector() {
			ArrayList<CheatItemRegistry.Entry> entries = new ArrayList<>(CheatItemRegistry.entries(Catalog.WANDS));
			boolean allowNone = ((MagesStaff) item).wandClass() == null;
			String[] labels = new String[entries.size() + (allowNone ? 1 : 0)];
			int offset = allowNone ? 1 : 0;
			if (allowNone) labels[0] = Messages.get(WndCheat.class, "effect_none");
			for (int i = 0; i < entries.size(); i++) labels[i + offset] = entries.get(i).displayName()
					+ " [" + entries.get(i).id + "]";
			GameScene.show(new WndCheatOptionSelect(Messages.get(WndCheat.class, "staff_wand"), labels, index -> {
				if (allowNone && index == 0) {
					config.staffWandClass = null;
					config.staffWandCharges = 0;
				} else {
					CheatItemRegistry.Entry selected = entries.get(index - offset);
					config.staffWandClass = selected.itemClass.asSubclass(Wand.class);
					config.staffWandCharges = maxStaffWandCharges();
				}
				rebuild();
			}));
		}

		private void setCurseInfusion(boolean value) {
			if (item instanceof Weapon) {
				config.weaponCurseInfusion = value;
				if (value && !isCurseSelection(true)) {
					config.enchantmentMode = CheatItemConfig.EffectMode.RANDOM_CURSE;
					config.enchantmentClass = null;
				}
			} else if (item instanceof Armor) {
				config.armorCurseInfusion = value;
				if (value && !isCurseSelection(false)) {
					config.glyphMode = CheatItemConfig.EffectMode.RANDOM_CURSE;
					config.glyphClass = null;
				}
			} else if (item instanceof Wand) {
				config.wandCurseInfusion = value;
			}
			if (value) config.cursed = true;
			rebuild();
		}

		private boolean isCurseSelection(boolean weapon) {
			CheatItemConfig.EffectMode mode = weapon ? config.enchantmentMode : config.glyphMode;
			if (mode == CheatItemConfig.EffectMode.RANDOM_CURSE) return true;
			if (mode != CheatItemConfig.EffectMode.EXACT) return false;
			Class<?> effectClass = weapon ? config.enchantmentClass : config.glyphClass;
			CheatEffectRegistry.Entry effect = CheatEffectRegistry.find(weapon
					? CheatEffectRegistry.Kind.ENCHANTMENT : CheatEffectRegistry.Kind.GLYPH, effectClass);
			return effect != null && effect.isCurse();
		}

		private void cleanseEffects() {
			if (isCurseSelection(true)) {
				config.enchantmentMode = CheatItemConfig.EffectMode.NONE;
				config.enchantmentClass = null;
			}
			if (isCurseSelection(false)) {
				config.glyphMode = CheatItemConfig.EffectMode.NONE;
				config.glyphClass = null;
			}
			config.weaponCurseInfusion = false;
			config.armorCurseInfusion = false;
			config.wandCurseInfusion = false;
		}

		private String effectName(boolean weapon) {
			CheatItemConfig.EffectMode mode = weapon ? config.enchantmentMode : config.glyphMode;
			switch (mode) {
				case RANDOM_GOOD: return Messages.get(WndCheat.class, "effect_random_good");
				case RANDOM_CURSE: return Messages.get(WndCheat.class, "effect_random_curse");
				case EXACT:
					CheatEffectRegistry.Entry effect = CheatEffectRegistry.find(weapon
							? CheatEffectRegistry.Kind.ENCHANTMENT : CheatEffectRegistry.Kind.GLYPH,
							weapon ? config.enchantmentClass : config.glyphClass);
					return effect == null ? Messages.get(WndCheat.class, "effect_none")
							: effect.displayName() + " [" + effect.id + "]";
				default: return Messages.get(WndCheat.class, "effect_none");
			}
		}

		private String augmentName(Enum<?> augment) {
			return Messages.get(StoneOfAugmentation.WndAugment.class, augment.name());
		}

		private String staffWandName() {
			if (config.staffWandClass == null) return Messages.get(WndCheat.class, "effect_none");
			Wand wand = Reflection.newInstance(config.staffWandClass);
			return wand == null ? config.staffWandClass.getSimpleName() : wand.name();
		}

		private int maxResin() {
			return config.identified && config.level >= 0 && config.level <= 2 ? 3 - config.level : 0;
		}

		private int maxWandCharges() {
			Wand probe = (Wand) item.duplicate();
			if (probe == null) return 0;
			probe.resinBonus = 0;
			probe.level(config.level);
			probe.resinBonus = config.wandResinBonus;
			probe.updateLevel();
			return probe.maxCharges;
		}

		private int maxStaffWandCharges() {
			if (config.staffWandClass == null) return 0;
			Wand wand = Reflection.newInstance(config.staffWandClass);
			if (wand == null) return 0;
			MagesStaff staff = new MagesStaff(wand);
			staff.level(config.level);
			staff.updateWand(false);
			return staff.wand().maxCharges;
		}

		private void normalizeRanges() {
			if (capabilities.wandEffects) {
				config.wandResinBonus = Math.max(0, Math.min(maxResin(), config.wandResinBonus));
				config.wandCharges = Math.max(0, Math.min(maxWandCharges(), config.wandCharges));
			}
			if (capabilities.staffImbue && config.staffWandClass != null) {
				config.staffWandCharges = Math.max(0,
						Math.min(maxStaffWandCharges(), config.staffWandCharges));
			}
		}
	}

	private static class WndCheatOptionSelect extends Window {
		private WndCheatOptionSelect(String titleText, String[] options, IndexConsumer consumer) {
			int width = 150;
			DisposableContent content = new DisposableContent();
			ScrollPane pane = new ScrollPane(content);
			add(pane);
			float top = 2;
			RenderedTextBlock title = labelStatic(titleText, 9, TITLE_COLOR);
			title.maxWidth(width - 4);
			title.setPos(2, top);
			content.add(title);
			top = title.bottom() + GAP;
			for (int i = 0; i < options.length; i++) {
				final int index = i;
				RedButton button = new RedButton(options[i], 7) {
					@Override protected void onClick() {
						hide();
						consumer.accept(index);
					}
				};
				button.leftJustify = true;
				button.setRect(0, top, width, BTN_HEIGHT);
				content.add(button);
				top = button.bottom() + 1;
			}
			content.setRect(0, 0, width, top);
			resize(width, Math.min(HEIGHT, (int) top));
			pane.setRect(0, 0, width, Math.min(HEIGHT, (int) top));
		}
	}

	private static class WndCheatEffectSelect extends Window {
		private WndCheatEffectSelect(boolean weapon, EffectConsumer consumer) {
			int width = 160;
			DisposableContent content = new DisposableContent();
			ScrollPane pane = new ScrollPane(content);
			add(pane);
			float top = 2;
			String titleText = Messages.get(WndCheat.class, weapon ? "enchantment" : "glyph");
			RenderedTextBlock title = labelStatic(titleText, 9, TITLE_COLOR);
			title.setPos(2, top);
			content.add(title);
			top = title.bottom() + GAP;
			top = addEffectChoice(content, width, Messages.get(WndCheat.class, "effect_none"), null,
					() -> consumer.accept(CheatItemConfig.EffectMode.NONE, null), top);
			top = addEffectChoice(content, width, Messages.get(WndCheat.class, "effect_random_good"), null,
					() -> consumer.accept(CheatItemConfig.EffectMode.RANDOM_GOOD, null), top);
			top = addEffectChoice(content, width, Messages.get(WndCheat.class, "effect_random_curse"), null,
					() -> consumer.accept(CheatItemConfig.EffectMode.RANDOM_CURSE, null), top);

			List<CheatEffectRegistry.Entry> effects = weapon
					? CheatEffectRegistry.enchantments() : CheatEffectRegistry.glyphs();
			for (CheatEffectRegistry.Rarity rarity : CheatEffectRegistry.Rarity.values()) {
				RenderedTextBlock heading = labelStatic(Messages.get(WndCheat.class,
						"rarity_" + rarity.name().toLowerCase()), 7, TITLE_COLOR);
				heading.setPos(2, top + 1);
				content.add(heading);
				top = heading.bottom() + 2;
				for (CheatEffectRegistry.Entry effect : effects) {
					if (effect.rarity != rarity) continue;
					Object instance = effect.create();
					Image icon = null;
					if (instance instanceof Weapon.Enchantment) {
						icon = new ItemSprite(ItemSpriteSheet.WORN_SHORTSWORD,
								((Weapon.Enchantment) instance).glowing());
					} else if (instance instanceof Armor.Glyph) {
						icon = new ItemSprite(ItemSpriteSheet.ARMOR_CLOTH,
								((Armor.Glyph) instance).glowing());
					}
					Image finalIcon = icon;
					top = addEffectChoice(content, width, effect.displayName() + " [" + effect.id + "]",
							finalIcon, () -> consumer.accept(CheatItemConfig.EffectMode.EXACT,
								effect.effectClass), top);
				}
			}
			content.setRect(0, 0, width, top);
			resize(width, HEIGHT);
			pane.setRect(0, 0, width, HEIGHT);
		}

		private float addEffectChoice(DisposableContent content, int width, String text,
				Image icon, Runnable action, float top) {
			RedButton button = new RedButton(text, 7) {
				@Override protected void onClick() {
					WndCheatEffectSelect.this.hide();
					action.run();
				}
			};
			button.leftJustify = true;
			if (icon != null) button.icon(icon);
			button.setRect(0, top, width, BTN_HEIGHT);
			content.add(button);
			return button.bottom() + 1;
		}
	}

	private static class IntegerControl extends Component {
		private static final float CONTROL_GAP = 1;
		private static final float STEP_WIDTH = 26;

		private final String inputTitle;
		private final int min;
		private final int max;
		private final IntValueFormatter formatter;
		private final IntValueHandler handler;
		private final RedButton decrease;
		private final RedButton valueButton;
		private final RedButton increase;

		private int value;
		private boolean enabled = true;

		private IntegerControl(String inputTitle, int value, int min, int max,
				IntValueFormatter formatter, IntValueHandler handler) {
			this(inputTitle, value, min, max, formatter, handler, 8);
		}

		private IntegerControl(String inputTitle, int value, int min, int max,
				IntValueFormatter formatter, IntValueHandler handler, int textSize) {
			if (min > max || value < min || value > max) {
				throw new IllegalArgumentException("Invalid integer control range or value");
			}
			this.inputTitle = inputTitle;
			this.value = value;
			this.min = min;
			this.max = max;
			this.formatter = formatter;
			this.handler = handler;

			decrease = new RedButton("-1", 7) {
				@Override protected void onClick() {
					if (IntegerControl.this.value > IntegerControl.this.min) {
						changeTo(IntegerControl.this.value - 1);
					}
				}
			};
			add(decrease);

			valueButton = new RedButton("", textSize) {
				@Override protected void onClick() {
					askNumberStatic(inputTitle, IntegerControl.this.value,
							IntegerControl.this.min, IntegerControl.this.max, IntegerControl.this::changeTo);
				}
			};
			add(valueButton);

			increase = new RedButton("+1", 7) {
				@Override protected void onClick() {
					if (IntegerControl.this.value < IntegerControl.this.max) {
						changeTo(IntegerControl.this.value + 1);
					}
				}
			};
			add(increase);
			refresh();
		}

		private void changeTo(int newValue) {
			if (newValue < min || newValue > max) {
				GLog.w(Messages.get(WndCheat.class, "number_out_of_range", min, max));
				return;
			}
			int previous = value;
			value = newValue;
			refresh();
			if (!handler.apply(newValue)) {
				value = previous;
				refresh();
			}
		}

		private void enable(boolean enabled) {
			this.enabled = enabled;
			refresh();
		}

		private void multiline(boolean multiline) {
			valueButton.multiline = multiline;
		}

		private void leftJustify(boolean leftJustify) {
			valueButton.leftJustify = leftJustify;
		}

		private void icon(Image icon) {
			valueButton.icon(icon);
		}

		private void refresh() {
			valueButton.text(formatter.format(value));
			valueButton.enable(enabled);
			decrease.enable(enabled && value > min);
			increase.enable(enabled && value < max);
		}

		@Override
		protected void layout() {
			float stepWidth = Math.min(STEP_WIDTH, (width - 2 * CONTROL_GAP) / 3f);
			decrease.setRect(x, y, stepWidth, height);
			increase.setRect(right() - stepWidth, y, stepWidth, height);
			valueButton.setRect(decrease.right() + CONTROL_GAP, y,
					increase.left() - decrease.right() - 2 * CONTROL_GAP, height);
		}
	}

	private static class WndCheatCoordinates extends WndTextInput {
		private WndCheatCoordinates() {
			super(Messages.get(WndCheat.class, "tp_coordinates"), Messages.get(WndCheat.class, "tp_coordinates_hint"),
					"", 20, false, Messages.get(WndCheat.class, "apply"), Messages.get(WndCheat.class, "cancel"));
		}

		@Override
		public void onSelect(boolean positive, String text) {
			if (!positive) return;
			String[] coords = text.trim().split("[,\\s]+");
			if (coords.length != 2) {
				CheatService.log(CheatService.execute("tp invalid"));
				return;
			}
			CheatService.log(CheatService.execute("tp " + coords[0] + " " + coords[1]));
		}
	}

	private interface IntConsumer {
		void accept(int value);
	}

	private interface IndexConsumer {
		void accept(int value);
	}

	private interface BoolConsumer {
		void accept(boolean value);
	}

	private interface EffectConsumer {
		void accept(CheatItemConfig.EffectMode mode, Class<?> effectClass);
	}

	private interface IntValueFormatter {
		String format(int value);
	}

	private interface IntValueHandler {
		boolean apply(int value);
	}

	private static void askNumberStatic(String title, int initial, int min, int max, IntConsumer consumer) {
		String range = Messages.get(WndCheat.class, "number_range", min, max);
		GameScene.show(new WndTextInput(title, range, Integer.toString(initial), 11, false,
				Messages.get(WndCheat.class, "apply"), Messages.get(WndCheat.class, "cancel")) {
			@Override
			public void onSelect(boolean positive, String text) {
				if (!positive) return;
				final int value;
				try {
					value = Integer.parseInt(text.trim());
				} catch (NumberFormatException e) {
					GLog.w(Messages.get(WndCheat.class, "invalid_integer"));
					return;
				}
				if (value < min || value > max) {
					GLog.w(Messages.get(WndCheat.class, "number_out_of_range", min, max));
					return;
				}
				consumer.accept(value);
			}
		});
	}

	private boolean finish(CheatService.Result result, Runnable refresh) {
		CheatService.log(result);
		if (result.success && refresh != null) refresh.run();
		return result.success;
	}

	private RedButton button(String text, Runnable action) {
		return button(text, action, 9);
	}

	private RedButton button(String text, Runnable action, int size) {
		return new RedButton(text, size) {
			@Override protected void onClick() { action.run(); }
		};
	}

	private RenderedTextBlock label(String text, int size, int color) {
		return labelStatic(text, size, color);
	}

	private static RenderedTextBlock labelStatic(String text, int size, int color) {
		RenderedTextBlock label = PixelScene.renderTextBlock(text, size);
		label.hardlight(color);
		return label;
	}
}
