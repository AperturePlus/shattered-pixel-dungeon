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

package com.shatteredpixel.shatteredpixeldungeon.actors.hero;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MagesStaff;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;
import com.watabou.noosa.Game;

/**
 * 英雄副职业枚举，定义了所有英雄的副职业类型，并关联了各自的图标。
 */
public enum HeroSubClass {

    NONE(HeroIcon.NONE),

    BERSERKER(HeroIcon.BERSERKER),
    GLADIATOR(HeroIcon.GLADIATOR),

    BATTLEMAGE(HeroIcon.BATTLEMAGE),
    WARLOCK(HeroIcon.WARLOCK),

    ASSASSIN(HeroIcon.ASSASSIN),
    FREERUNNER(HeroIcon.FREERUNNER),

    SNIPER(HeroIcon.SNIPER),
    WARDEN(HeroIcon.WARDEN),

    CHAMPION(HeroIcon.CHAMPION),
    MONK(HeroIcon.MONK);

    int icon;

    /**
     * 构造函数，为每个英雄副职业初始化图标。
     * @param icon 英雄副职业对应的图标。
     */
    HeroSubClass(int icon){
        this.icon = icon;
    }

    /**
     * 获取英雄副职业的标题。
     * @return 英雄副职业的标题字符串。
     */
    public String title() {
        return Messages.get(this, name());
    }

    /**
     * 获取英雄副职业的简短描述。
     * @return 英雄副职业的简短描述字符串。
     */
    public String shortDesc() {
        return Messages.get(this, name()+"_short_desc");
    }

    /**
     * 获取英雄副职业的详细描述，对战法师进行特殊处理以包含法杖效果。
     * @return 英雄副职业的详细描述字符串。
     */
    public String desc() {
        //Include the staff effect description in the battlemage's desc if possible
        if (this == BATTLEMAGE){
            String desc = Messages.get(this, name() + "_desc");
            if (Game.scene() instanceof GameScene){
                MagesStaff staff = Dungeon.hero.belongings.getItem(MagesStaff.class);
                if (staff != null && staff.wandClass() != null){
                    desc += "\n\n" + Messages.get(staff.wandClass(), "bmage_desc");
                    desc = desc.replaceAll("_", "");
                }
            }
            return desc;
        } else {
            return Messages.get(this, name() + "_desc");
        }
    }

    /**
     * 获取英雄副职业的图标。
     * @return 英雄副职业的图标编号。
     */
    public int icon(){
        return icon;
    }

}

