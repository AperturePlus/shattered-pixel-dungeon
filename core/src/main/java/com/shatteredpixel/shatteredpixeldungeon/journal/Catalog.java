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

package com.shatteredpixel.shatteredpixeldungeon.journal;

import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.watabou.utils.Bundle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * 物品分类枚举，用于识别游戏中的不同物品类型。
 */
public enum Catalog {

    WEAPONS, ARMOR, WANDS, RINGS, ARTIFACTS, POTIONS, SCROLLS;

    /**
     * 用于跟踪每个物品类型中已看到的物品的映射。
     */
    private LinkedHashMap<Class<? extends Item>, Boolean> seen = new LinkedHashMap<>();

    /**
     * 返回此分类中所有已知物品的类。
     *
     * @return 已知物品类的集合。
     */
    public Collection<Class<? extends Item>> items(){
        return seen.keySet();
    }

    /**
     * 检查是否已看到此分类中的所有物品。
     *
     * @return 如果所有物品都已看到，则为true；否则为false。
     */
    public boolean allSeen(){
        for (Class<?extends Item> item : items()){
            if (!seen.get(item)){
                return false;
            }
        }
        return true;
    }

    /**
     * 静态初始化块，用于为每个分类初始化seen映射。
     */
    static {
        // 根据物品生成类型初始化每个分类的seen映射。
        for (Class weapon : Generator.Category.WEP_T1.classes){
            WEAPONS.seen.put( weapon, false);
        }
        // 省略其他类型初始化，模式相同...
    }

    /**
     * 为每个分类设置相应的徽章。
     */
    public static LinkedHashMap<Catalog, Badges.Badge> catalogBadges = new LinkedHashMap<>();
    static {
        catalogBadges.put(WEAPONS, Badges.Badge.ALL_WEAPONS_IDENTIFIED);
        // 省略其他分类的徽章设置，模式相同...
    }

    /**
     * 检查给定物品类是否已被看到。
     *
     * @param itemClass 物品类。
     * @return 如果物品已被看到，则为true；否则为false。
     */
    public static boolean isSeen(Class<? extends Item> itemClass){
        for (Catalog cat : values()) {
            if (cat.seen.containsKey(itemClass)) {
                return cat.seen.get(itemClass);
            }
        }
        return false;
    }

    /**
     * 标记给定物品类为已看到，并更新保存需求。
     *
     * @param itemClass 物品类。
     */
    public static void setSeen(Class<? extends Item> itemClass){
        for (Catalog cat : values()) {
            if (cat.seen.containsKey(itemClass) && !cat.seen.get(itemClass)) {
                cat.seen.put(itemClass, true);
                Journal.saveNeeded = true;
            }
        }
        Badges.validateItemsIdentified();
    }

    /**
     * 用于存储已看到的物品信息的键。
     */
    private static final String CATALOG_ITEMS = "catalog_items";

    /**
     * 将已看到的物品信息存储到bundle中。
     *
     * @param bundle 用于存储数据的Bundle对象。
     */
    public static void store( Bundle bundle ){
        Badges.loadGlobal();
        ArrayList<Class> seen = new ArrayList<>();
        // 如果未解锁全部物品识别徽章，则存储每个分类中已看到的物品。
        if (!Badges.isUnlocked(Badges.Badge.ALL_ITEMS_IDENTIFIED)) {
            for (Catalog cat : values()) {
                // 如果分类的徽章未解锁，则添加已看到的物品类。
                if (!Badges.isUnlocked(catalogBadges.get(cat))) {
                    for (Class<? extends Item> item : cat.items()) {
                        if (cat.seen.get(item)) seen.add(item);
                    }
                }
            }
        }
        bundle.put(CATALOG_ITEMS, seen.toArray(new Class[0]));
    }

    /**
     * 从bundle中恢复已看到的物品信息。
     *
     * @param bundle 用于恢复数据的Bundle对象。
     */
    public static void restore( Bundle bundle ){
        Badges.loadGlobal();
        // 如果已解锁全部物品识别徽章，则视为所有物品都已看到。
        if (Badges.isUnlocked(Badges.Badge.ALL_ITEMS_IDENTIFIED)){
            for ( Catalog cat : values()){
                for (Class<? extends Item> item : cat.items()){
                    cat.seen.put(item, true);
                }
            }
            return;
        }
        // 根据分类的徽章状态恢复物品的已看到状态。
        for (Catalog cat : values()){
            if (Badges.isUnlocked(catalogBadges.get(cat))){
                for (Class<? extends Item> item : cat.items()){
                    cat.seen.put(item, true);
                }
            }
        }
        // 从bundle中恢复特定的已看到物品信息。
        if (bundle.contains(CATALOG_ITEMS)) {
            List<Class> seenClasses = new ArrayList<>();
            if (bundle.contains(CATALOG_ITEMS)) {
                seenClasses = Arrays.asList(bundle.getClassArray(CATALOG_ITEMS));
            }
            for (Catalog cat : values()) {
                for (Class<? extends Item> item : cat.items()) {
                    if (seenClasses.contains(item)) {
                        cat.seen.put(item, true);
                    }
                }
            }
        }
    }
}

