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
import com.shatteredpixel.shatteredpixeldungeon.debug.CheatItemRegistry;
import com.shatteredpixel.shatteredpixeldungeon.debug.CheatService;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.journal.Catalog;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.CheckBox;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.ScrollPane;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.watabou.noosa.Gizmo;
import com.watabou.noosa.ui.Component;

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

			RedButton hp = button(Messages.get(WndCheat.this, "set_hp"), () -> askNumber(
					Messages.get(WndCheat.this, "set_hp"), Dungeon.hero.HP,
					value -> finish(CheatService.setHP(value), this::rebuild)));
			hp.setRect(0, top, (WIDTH - GAP) / 2f, BTN_HEIGHT);
			content.add(hp);
			RedButton full = button(Messages.get(WndCheat.this, "full_hp"), () ->
					finish(CheatService.setHP(Dungeon.hero.HT), this::rebuild));
			full.setRect(hp.right() + GAP, top, WIDTH - hp.right() - GAP, BTN_HEIGHT);
			content.add(full);
			top = hp.bottom() + GAP;

			RedButton maxHP = button(Messages.get(WndCheat.this, "set_max_hp"), () -> askNumber(
					Messages.get(WndCheat.this, "set_max_hp"), Dungeon.hero.HT,
					value -> finish(CheatService.setMaxHP(value), this::rebuild)));
			maxHP.setRect(0, top, WIDTH, BTN_HEIGHT);
			content.add(maxHP);
			top = maxHP.bottom() + GAP;

			RedButton gold = button(Messages.get(WndCheat.this, "set_gold"), () -> askNumber(
					Messages.get(WndCheat.this, "set_gold"), Dungeon.gold,
					value -> finish(CheatService.setGold(value), this::rebuild)));
			gold.setRect(0, top, WIDTH, BTN_HEIGHT);
			content.add(gold);
			top = gold.bottom() + GAP;

			RedButton console = button(Messages.get(WndCheat.this, "console"), () -> GameScene.show(new WndCheatConsole()));
			console.setRect(0, top, WIDTH, BTN_HEIGHT);
			content.add(console);
			content.setRect(0, 0, WIDTH, console.bottom());
			pane.setRect(0, 0, WIDTH, HEIGHT);
		}
	}

	private class ItemsPage extends RebuildablePage {
		@Override
		protected void rebuild() {
			content.clearAndDestroy();
			float top = 1;
			for (Catalog category : Catalog.values()) {
				List<CheatItemRegistry.Entry> entries = CheatItemRegistry.entries(category);
				if (entries.isEmpty()) continue;
				RenderedTextBlock heading = label(category.title(), 8, Window.TITLE_COLOR);
				heading.setPos(2, top);
				content.add(heading);
				top = heading.bottom() + 2;
				for (CheatItemRegistry.Entry entry : entries) {
					Item preview = entry.create();
					if (preview == null) continue;
					RedButton row = button(preview.name() + "  [" + entry.id + "]", () ->
							GameScene.show(new WndCheatGiveItem(entry)), 6);
					row.leftJustify = true;
					row.icon(new ItemSprite(preview));
					row.setRect(0, top, WIDTH, BTN_HEIGHT);
					content.add(row);
					top = row.bottom() + 1;
				}
				top += GAP;
			}
			content.setRect(0, 0, WIDTH, top);
			pane.setRect(0, 0, WIDTH, HEIGHT);
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
							GameScene.show(new WndCheatEditItem(slot, this::rebuild)), 7);
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
			RedButton floor = button(Messages.get(WndCheat.this, "tp_floor"), () -> askNumber(
					Messages.get(WndCheat.this, "tp_floor"), Dungeon.depth,
					value -> finish(CheatService.changeFloor(value), null)));
			floor.setRect(0, top, WIDTH, BTN_HEIGHT);
			content.add(floor);
			content.setRect(0, 0, WIDTH, floor.bottom());
			pane.setRect(0, 0, WIDTH, HEIGHT);
		}
	}

	private static class WndCheatGiveItem extends Window {
		private final CheatItemRegistry.Entry entry;
		private int quantity = 1;
		private int level = 0;
		private final RedButton quantityButton;
		private final RedButton levelButton;
		private final CheckBox identified;
		private final CheckBox cursed;

		private WndCheatGiveItem(CheatItemRegistry.Entry entry) {
			this.entry = entry;
			Item preview = entry.create();
			int width = 145;
			float top = 2;
			RenderedTextBlock title = labelStatic(preview.name() + " [" + entry.id + "]", 9, TITLE_COLOR);
			title.maxWidth(width - 4);
			title.setPos(2, top);
			add(title);
			top = title.bottom() + GAP;

			quantityButton = new RedButton("") {
				@Override protected void onClick() {
					askNumberStatic(Messages.get(WndCheat.class, "quantity"), quantity,
							value -> { quantity = value; refresh(); });
				}
			};
			quantityButton.enable(preview.stackable);
			quantityButton.setRect(0, top, width, BTN_HEIGHT);
			add(quantityButton);
			top = quantityButton.bottom() + GAP;

			levelButton = new RedButton("") {
				@Override protected void onClick() {
					askNumberStatic(Messages.get(WndCheat.class, "level"), level,
							value -> { level = value; refresh(); });
				}
			};
			levelButton.enable(preview.isUpgradable());
			levelButton.setRect(0, top, width, BTN_HEIGHT);
			add(levelButton);
			top = levelButton.bottom() + GAP;

			identified = new CheckBox(Messages.get(WndCheat.class, "identified"));
			identified.checked(true);
			identified.enable(CheatService.canBeUnidentified(preview));
			identified.setRect(0, top, width, BTN_HEIGHT);
			add(identified);
			top = identified.bottom() + GAP;
			cursed = new CheckBox(Messages.get(WndCheat.class, "cursed"));
			cursed.setRect(0, top, width, BTN_HEIGHT);
			add(cursed);
			top = cursed.bottom() + GAP;

			RedButton give = new RedButton(Messages.get(WndCheat.class, "give")) {
				@Override protected void onClick() {
					CheatService.Result result = CheatService.give(entry, quantity, level, cursed.checked(), identified.checked());
					CheatService.log(result);
					if (result.success) hide();
				}
			};
			give.setRect(0, top, width, BTN_HEIGHT);
			add(give);
			resize(width, (int) give.bottom());
			refresh();
		}

		private void refresh() {
			quantityButton.text(Messages.get(WndCheat.class, "quantity_value", quantity));
			levelButton.text(Messages.get(WndCheat.class, "level_value", level));
		}
	}

	private static class WndCheatEditItem extends Window {
		private final String slot;
		private final Runnable refreshParent;
		private int quantity;
		private int level;
		private final RedButton quantityButton;
		private final RedButton levelButton;
		private final CheckBox identified;
		private final CheckBox cursed;

		private WndCheatEditItem(String slot, Runnable refreshParent) {
			this.slot = slot;
			this.refreshParent = refreshParent;
			Item item = CheatService.resolveSlot(slot);
			quantity = item.quantity();
			level = item.trueLevel();
			int width = 145;
			float top = 2;
			RenderedTextBlock title = labelStatic(slot + ": " + item.name(), 9, TITLE_COLOR);
			title.maxWidth(width - 4);
			title.setPos(2, top);
			add(title);
			top = title.bottom() + GAP;

			quantityButton = new RedButton("") {
				@Override protected void onClick() {
					askNumberStatic(Messages.get(WndCheat.class, "quantity"), quantity,
							value -> { quantity = value; refresh(); });
				}
			};
			quantityButton.enable(item.stackable);
			quantityButton.setRect(0, top, width, BTN_HEIGHT);
			add(quantityButton);
			top = quantityButton.bottom() + GAP;

			levelButton = new RedButton("") {
				@Override protected void onClick() {
					askNumberStatic(Messages.get(WndCheat.class, "level"), level,
							value -> { level = value; refresh(); });
				}
			};
			levelButton.enable(item.isUpgradable());
			levelButton.setRect(0, top, width, BTN_HEIGHT);
			add(levelButton);
			top = levelButton.bottom() + GAP;

			identified = new CheckBox(Messages.get(WndCheat.class, "identified"));
			identified.checked(item.isIdentified());
			identified.enable(CheatService.canBeUnidentified(item));
			identified.setRect(0, top, width, BTN_HEIGHT);
			add(identified);
			top = identified.bottom() + GAP;
			cursed = new CheckBox(Messages.get(WndCheat.class, "cursed"));
			cursed.checked(item.cursed);
			cursed.setRect(0, top, width, BTN_HEIGHT);
			add(cursed);
			top = cursed.bottom() + GAP;

			RedButton apply = new RedButton(Messages.get(WndCheat.class, "apply")) {
				@Override protected void onClick() { applyChanges(); }
			};
			apply.setRect(0, top, (width - GAP) / 2f, BTN_HEIGHT);
			add(apply);
			RedButton delete = new RedButton(Messages.get(WndCheat.class, "delete")) {
				@Override protected void onClick() {
					CheatService.Result result = CheatService.deleteItem(slot);
					CheatService.log(result);
					if (result.success) {
						hide();
						refreshParent.run();
					}
				}
			};
			delete.enable(slot.startsWith("bag:"));
			delete.setRect(apply.right() + GAP, top, width - apply.right() - GAP, BTN_HEIGHT);
			add(delete);
			resize(width, (int) apply.bottom());
			refresh();
		}

		private void applyChanges() {
			Item item = CheatService.resolveSlot(slot);
			if (item == null) return;
			CheatService.Result result = CheatService.updateItem(slot, quantity, level,
					identified.checked(), cursed.checked());
			CheatService.log(result);
			if (result.success) {
				hide();
				refreshParent.run();
			}
		}

		private void refresh() {
			quantityButton.text(Messages.get(WndCheat.class, "quantity_value", quantity));
			levelButton.text(Messages.get(WndCheat.class, "level_value", level));
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

	private void askNumber(String title, int initial, IntConsumer consumer) {
		askNumberStatic(title, initial, consumer);
	}

	private static void askNumberStatic(String title, int initial, IntConsumer consumer) {
		GameScene.show(new WndTextInput(title, null, Integer.toString(initial), 11, false,
				Messages.get(WndCheat.class, "apply"), Messages.get(WndCheat.class, "cancel")) {
			@Override
			public void onSelect(boolean positive, String text) {
				if (!positive) return;
				try {
					consumer.accept(Integer.parseInt(text.trim()));
				} catch (NumberFormatException e) {
					CheatService.log(CheatService.execute("gold not-a-number"));
				}
			}
		});
	}

	private void finish(CheatService.Result result, Runnable refresh) {
		CheatService.log(result);
		if (result.success && refresh != null) refresh.run();
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
