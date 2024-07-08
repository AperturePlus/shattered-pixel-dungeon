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

package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.GamesInProgress;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.InterlevelScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.StartScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.HeroSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.tags.ActionIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.ui.buttons.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.utils.DungeonSeed;
import com.watabou.noosa.Game;

import java.util.Locale;

/**
 * 表示正在进行的游戏窗口的类，继承自Window。
 * 该窗口用于展示玩家当前正在进行的游戏的详细信息，并提供继续或清除游戏选项。
 */
public class WndGameInProgress extends Window {

	// 窗口宽度常量
	private static final int WIDTH = 120;

	// 组件间距常量
	private int GAP = 6;

	// 组件位置变量
	private float pos;

	/**
	 * 构造函数，初始化窗口。
	 * @param slot 游戏存档槽的索引，用于加载对应的游戏信息。
	 */
	public WndGameInProgress(final int slot){
		// 加载游戏进度信息
		final GamesInProgress.Info info = GamesInProgress.check(slot);

		// 根据英雄子类是否存在决定显示的类名
		String className = null;
		if (info.subClass != HeroSubClass.NONE){
			className = info.subClass.title();
		} else {
			className = info.heroClass.title();
		}

		// 初始化窗口标题
		IconTitle title = new IconTitle();
		title.icon( HeroSprite.avatar(info.heroClass, info.armorTier) );
		title.label((Messages.get(this, "title", info.level, className)).toUpperCase(Locale.ENGLISH));
		title.color(Window.TITLE_COLOR);
		title.setRect( 0, 0, WIDTH, 0 );
		add(title);

		// 根据挑战次数调整组件间距
		if (info.challenges > 0) GAP -= 2;

		// 设置标题底部到下一个组件的距离
		pos = title.bottom() + GAP;

		// 如果有挑战未完成，添加挑战按钮
		if (info.challenges > 0) {
			RedButton btnChallenges = new RedButton( Messages.get(this, "challenges") ) {
				@Override
				protected void onClick() {
					Game.scene().add( new WndChallenges( info.challenges, false ) );
				}
			};
			btnChallenges.icon(Icons.get(Icons.CHALLENGE_ON));
			float btnW = btnChallenges.reqWidth() + 2;
			btnChallenges.setRect( (WIDTH - btnW)/2, pos, btnW , 18 );
			add( btnChallenges );

			// 更新组件间距
			pos = btnChallenges.bottom() + GAP;
		}

		// 为属性加间隔
		pos += GAP;

		// 显示力量属性信息
		int strBonus = info.strBonus;
		if (strBonus > 0)           statSlot( Messages.get(this, "str"), info.str + " + " + strBonus );
		else if (strBonus < 0)      statSlot( Messages.get(this, "str"), info.str + " - " + -strBonus );
		else                        statSlot( Messages.get(this, "str"), info.str );
		// 显示生命属性信息
		if (info.shld > 0)  statSlot( Messages.get(this, "health"), info.hp + "+" + info.shld + "/" + info.ht );
		else                statSlot( Messages.get(this, "health"), (info.hp) + "/" + info.ht );
		// 显示经验属性信息
		statSlot( Messages.get(this, "exp"), info.exp + "/" + Hero.maxExp(info.level) );

		// 添加间隔
		pos += GAP;

		// 显示金币属性信息
		statSlot( Messages.get(this, "gold"), info.goldCollected );
		// 显示深度属性信息
		statSlot( Messages.get(this, "depth"), info.maxDepth );
		// 根据游戏类型显示不同的种子信息
		if (info.daily) {
			if (info.dailyReplay) {
				statSlot(Messages.get(this, "replay_for"), "_" + info.customSeed + "_");
			} else {
				statSlot(Messages.get(this, "daily_for"), "_" + info.customSeed + "_");
			}
		} else if (!info.customSeed.isEmpty()){
			statSlot( Messages.get(this, "custom_seed"), "_" + info.customSeed + "_" );
		} else {
			statSlot( Messages.get(this, "dungeon_seed"), DungeonSeed.convertToCode(info.seed) );
		}

		// 添加间隔
		pos += GAP;

		// 创建并添加继续游戏按钮
		RedButton cont = new RedButton(Messages.get(this, "continue")){
			@Override
			protected void onClick() {
				super.onClick();

				GamesInProgress.curSlot = slot;

				Dungeon.hero = null;
				Dungeon.daily = Dungeon.dailyReplay = false;
				ActionIndicator.clearAction();
				InterlevelScene.mode = InterlevelScene.Mode.CONTINUE;
				ShatteredPixelDungeon.switchScene(InterlevelScene.class);
			}
		};

		// 创建并添加清除游戏按钮
		RedButton erase = new RedButton( Messages.get(this, "erase")){
			@Override
			protected void onClick() {
				super.onClick();

				ShatteredPixelDungeon.scene().add(new WndOptions(Icons.get(Icons.WARNING),
						Messages.get(WndGameInProgress.class, "erase_warn_title"),
						Messages.get(WndGameInProgress.class, "erase_warn_body"),
						Messages.get(WndGameInProgress.class, "erase_warn_yes"),
						Messages.get(WndGameInProgress.class, "erase_warn_no") ) {
					@Override
					protected void onSelect( int index ) {
						if (index == 0) {
							Dungeon.deleteGame(slot, true);
							ShatteredPixelDungeon.switchNoFade(StartScene.class);
						}
					}
				} );
			}
		};

		// 设置按钮图标和位置
		cont.icon(Icons.get(Icons.ENTER));
		cont.setRect(0, pos, WIDTH/2 -1, 20);
		add(cont);

		erase.icon(Icons.get(Icons.CLOSE));
		erase.setRect(WIDTH/2 + 1, pos, WIDTH/2 - 1, 20);
		add(erase);

		// 调整窗口大小
		resize(WIDTH, (int)cont.bottom()+1);
	}

	/**
	 * 显示属性信息的方法。
	 * @param label 属性的标签。
	 * @param value 属性的值。
	 */
	private void statSlot( String label, String value ) {
		// 渲染并添加属性标签
		RenderedTextBlock txt = PixelScene.renderTextBlock( label, 8 );
		txt.setPos(0, pos);
		add( txt );

		// 根据值的长度调整字体大小，并渲染并添加属性值
		int size = 8;
		if (value.length() >= 14) size -=2;
		if (value.length() >= 18) size -=1;
		txt = PixelScene.renderTextBlock( value, size );
		txt.setPos(WIDTH * 0.55f, pos + (6 - txt.height())/2);
		PixelScene.align(txt);
		add( txt );

		// 更新组件位置
		pos += GAP + txt.height();
	}

	/**
	 * 显示整数属性信息的方法，内部调用statSlot方法。
	 * @param label 属性的标签。
	 * @param value 属性的整数值。
	 */
	private void statSlot( String label, int value ) {
		statSlot( label, Integer.toString( value ) );
	}
}

