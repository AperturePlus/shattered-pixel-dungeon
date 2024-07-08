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

package com.shatteredpixel.shatteredpixeldungeon;

import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.watabou.utils.Bundlable;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

import java.util.ArrayList;
import java.util.Collection;

public class QuickSlot {

    /**
     * 插槽包含玩家背包中的物品。唯一的例外是数量为0的情况，
     * 这可能发生在可堆叠物品被“用尽”的时候，这些被称为占位符。
     */

    //注意当前的最大尺寸编码为6，由于用户界面的限制，但实际上可以设置得更大而没有问题。
    public static final int SIZE = 6;
    private Item[] slots = new Item[SIZE];


    /**
     * 设置指定插槽的物品。
     * @param slot 插槽索引。
     * @param item 要设置的物品。
     */
    public void setSlot(int slot, Item item){
        clearItem(item); //不允许同一物品出现在多个插槽中。
        slots[slot] = item;
    }

    /**
     * 清空指定插槽的物品。
     * @param slot 要清空的插槽索引。
     */
    public void clearSlot(int slot){
        slots[slot] = null;
    }

    /**
     * 重置所有插槽为空。
     */
    public void reset(){
        slots = new Item[SIZE];
    }

    /**
     * 获取指定插槽的物品。
     * @param slot 插槽索引。
     * @return 返回该插槽的物品。
     */
    public Item getItem(int slot){
        return slots[slot];
    }

    /**
     * 查找指定物品所在的插槽。
     * @param item 物品。
     * @return 返回物品所在插槽的索引，如果未找到则返回-1。
     */
    public int getSlot(Item item) {
        for (int i = 0; i < SIZE; i++) {
            if (getItem(i) == item) {
                return i;
            }
        }
        return -1;
    }

    /**
     * 判断指定插槽是否为占位符。
     * @param slot 插槽索引。
     * @return 如果插槽内有物品且其数量为0，则返回true。
     */
    public boolean isPlaceholder(int slot){
        return getItem(slot) != null && getItem(slot).quantity() == 0;
    }

    /**
     * 判断指定插槽是否有非占位符的物品。
     * @param slot 插槽索引。
     * @return 如果插槽内有物品且其数量大于0，则返回true。
     */
    public boolean isNonePlaceholder(int slot){
        return getItem(slot) != null && getItem(slot).quantity() > 0;
    }

    /**
     * 清除指定物品在所有插槽中的存在。
     * @param item 要清除的物品。
     */
    public void clearItem(Item item){
        if (contains(item)) {
            clearSlot(getSlot(item));
        }
    }

    /**
     * 检查物品是否存在于任何插槽中。
     * @param item 物品。
     * @return 如果物品存在于任意插槽中，则返回true。
     */
    public boolean contains(Item item){
        return getSlot(item) != -1;
    }

    /**
     * 替换指定插槽的占位符为新的物品。
     * @param item 新的物品。
     */
    public void replacePlaceholder(Item item) {
        for (int i = 0; i < SIZE; i++) {
            if (isPlaceholder(i) && item.isSimilar(getItem(i))) {
                setSlot(i, item);
            }
        }
    }

    /**
     * 将指定物品转换为占位符。
     * @param item 要转换的物品。
     */
    public void convertToPlaceholder(Item item){

        if (contains(item)) {
            Item placeholder = item.virtual();
            if (placeholder == null) return;

            for (int i = 0; i < SIZE; i++) {
                if (getItem(i) == item) setSlot(i, placeholder);
            }
        }
    }

    /**
     * 随机选择一个非占位符的物品。
     * @return 返回随机选择的非占位符物品，如果没有则返回null。
     */
    public Item randomNonePlaceholder(){

        ArrayList<Item> result = new ArrayList<>();
        for (int i = 0; i < SIZE; i ++) {
            if (getItem(i) != null && !isPlaceholder(i)) {
                result.add(getItem(i));
            }
        }
        return Random.element(result);
    }

    //用于存储占位符的键名
    private final String PLACEHOLDERS = "placeholders";
    //用于存储占位符位置的键名
    private final String PLACEMENTS = "placements";

    /**
     * 保存占位符和它们的位置信息到数据包中。
     * @param bundle 数据包。
     */
    public void storePlaceholders(Bundle bundle){
        ArrayList<Item> placeholders = new ArrayList<>(SIZE);
        boolean[] placements = new boolean[SIZE];

        for (int i = 0; i < SIZE; i++) {
            if (isPlaceholder(i)) {
                placeholders.add(getItem(i));
                placements[i] = true;
            }
        }
        bundle.put( PLACEHOLDERS, placeholders );
        bundle.put( PLACEMENTS, placements );
    }

    /**
     * 从数据包中恢复占位符和它们的位置信息。
     * @param bundle 数据包。
     */
    public void restorePlaceholders(Bundle bundle){
        Collection<Bundlable> placeholders = bundle.getCollection(PLACEHOLDERS);
        boolean[] placements = bundle.getBooleanArray( PLACEMENTS );

        int i = 0;
        for (Bundlable item : placeholders){
            while (!placements[i]){
                i++;
            }
            setSlot( i, (Item)item );
            i++;
        }

    }

}

