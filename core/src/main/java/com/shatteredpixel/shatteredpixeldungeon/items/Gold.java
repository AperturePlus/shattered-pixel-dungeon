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

package com.shatteredpixel.shatteredpixeldungeon.items;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.Statistics;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Random;

import java.util.ArrayList;

/**
 * 金子类继承自物品类，代表游戏中的金币。
 */
public class Gold extends Item {

    {
        // 初始化金子的图像和堆叠属性。
        image = ItemSpriteSheet.GOLD;
        stackable = true;
    }

    /**
     * 默认构造函数，调用带数量参数的构造函数。
     */
    public Gold() {
        this(1);
    }

    /**
     * 带数量参数的构造函数，用于指定金子的数量。
     *
     * @param value 金子的数量。
     */
    public Gold(int value) {
        this.quantity = value;
    }

    /**
     * 重写actions方法，金子没有特殊动作。
     *
     * @param hero 与金子交互的英雄。
     * @return 一个空的字符串列表。
     */
    @Override
    public ArrayList<String> actions(Hero hero) {
        return new ArrayList<>();
    }

    /**
     * 重写pickUp方法，处理英雄捡起金子的逻辑。
     *
     * @param hero 捡起金子的英雄。
     * @param pos 金子的位置。
     * @return 总是返回true，表示金子可以被捡起。
     */
    @Override
    public boolean doPickUp(Hero hero, int pos) {
        // 增加英雄捡起的金子数量到地下城的金子总量和统计信息中。
        Dungeon.gold += quantity;
        Statistics.goldCollected += quantity;
        // 检查金子收集成就。
        Badges.validateGoldCollected();
        // 显示捡起金子的动画和信息。
        GameScene.pickUp(this, pos);
        hero.sprite.showStatusWithIcon(CharSprite.NEUTRAL, Integer.toString(quantity), FloatingText.GOLD);
        // 花费捡起金子的时间。
        hero.spendAndNext(TIME_TO_PICK_UP);
        // 播放捡起金子的声音。
        Sample.INSTANCE.play(Assets.Sounds.GOLD, 1, 1, Random.Float(0.9f, 1.1f));
        // 更新快速槽位。
        updateQuickslot();
        return true;
    }

    /**
     * 金子不可升级。
     *
     * @return 总是返回false。
     */
    @Override
    public boolean isUpgradable() {
        return false;
    }

    /**
     * 金子总是被识别的。
     *
     * @return 总是返回true。
     */
    @Override
    public boolean isIdentified() {
        return true;
    }

    /**
     * 生成一个随机数量的金子。
     *
     * @return 生成的金子对象本身，数量为随机值。
     */
    @Override
    public Item random() {
        // 金子的数量根据地下城的深度随机生成。
        quantity = Random.IntRange(30 + Dungeon.depth * 10, 60 + Dungeon.depth * 20);
        return this;
    }

}
