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

import com.shatteredpixel.shatteredpixeldungeon.debug.CheatService;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;

public class WndCheatConsole extends WndTextInput {

	private static String lastCommand = "";

	public WndCheatConsole() {
		super(Messages.get(WndCheatConsole.class, "title"),
				Messages.get(WndCheatConsole.class, "body"), lastCommand, 200,
				false, Messages.get(WndCheatConsole.class, "execute"),
				Messages.get(WndCheatConsole.class, "cancel"));
	}

	@Override
	public void onSelect(boolean positive, String text) {
		if (!positive) return;
		lastCommand = text == null ? "" : text.trim();
		CheatService.log(CheatService.execute(lastCommand));
	}
}
