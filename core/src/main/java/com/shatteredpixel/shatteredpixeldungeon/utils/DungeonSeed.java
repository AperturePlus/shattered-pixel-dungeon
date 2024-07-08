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

package com.shatteredpixel.shatteredpixeldungeon.utils;

import com.watabou.utils.Random;

import java.util.Locale;

//This class defines the parameters for seeds in ShatteredPD and contains a few convenience methods
public class DungeonSeed {

    /**
     * 定义所有可能种子的总数。
     * 最大的可能种子值为26^9。
     */
    public static long TOTAL_SEEDS = 5429503678976L; // 最大的可能种子值为26^9

    /**
     * 种子代码的形式为 @@@-@@@-@@@，其中 @ 是A至Z之间的任意字母（仅大写）。
     * 这实际上是一种基数为26的数字系统，因此有26^9个独特的种子。
     */

    /**
     * 种子代码的存在使得分享和输入种子变得更加容易。
     * "ZZZ-ZZZ-ZZZ"比5,429,503,678,975更容易输入和分享。
     */

    /**
     * 生成一个随机种子，排除包含元音的种子（以最小化随机出现的真实单词）。
     * 这意味着有21^9 = 794,280,046,581个独特的种子可以被随机生成。
     *
     * @return 长整型的随机种子值。
     */
    public static long randomSeed(){
        Long seed;
        String seedText;
        do {
            seed = Random.Long(TOTAL_SEEDS);
            seedText = convertToCode(seed);
        } while (seedText.contains("A") || seedText.contains("E") || seedText.contains("I") || seedText.contains("O") || seedText.contains("U"));
        return seed;
    }

    /**
     * 将种子代码 (@@@@@@@@@) 转换为其等效的长整型值。
     *
     * @param code 字符串形式的种子代码。
     * @return 对应的长整型种子值。
     */
    public static long convertFromCode(String code ){
        //如果代码格式正确，强制转换为大写
        if (code.length() == 11 && code.charAt(3) == '-' && code.charAt(7) == '-'){
            code = code.toUpperCase(java.util.Locale.ROOT);
        }

        //忽略空白字符和破折号
        code = code.replaceAll("[-\\s]", "");

        if (code.length() != 9) {
            throw new IllegalArgumentException("codes must be 9 A-Z characters.");
        }

        long result = 0;
        for (int i = 8; i >= 0; i--) {
            char c = code.charAt(i);
            if (c > 'Z' || c < 'A')
                throw new IllegalArgumentException("codes must be 9 A-Z characters.");

            result += (c - 65) * Math.pow(26, (8 - i));
        }
        return result;
    }

    /**
     * 将长整型值转换为其等效的种子代码。
     *
     * @param seed 长整型的种子值。
     * @return 对应的种子代码字符串。
     */
    public static String convertToCode(long seed ){
        if (seed < 0 || seed >= TOTAL_SEEDS) {
            throw new IllegalArgumentException("seeds must be within the range [0, TOTAL_SEEDS)");
        }

        //这几乎给出了正确的答案，但它是0-p而不是A-Z
        String interrim = Long.toString(seed, 26);
        StringBuilder result = new StringBuilder();

        //进行转换
        for (int i = 0; i < 9; i++) {

            if (i < interrim.length()){
                char c = interrim.charAt(i);
                if (c <= '9') c += 17; //将0-9转换为A-J
                else          c -= 22; //将a-p转换为K-Z

                result.append(c);

            } else {
                result.insert(0, 'A'); //填充A（零）直到长度达到9

            }
        }

        //插入破折号以便于阅读
        result.insert(3, '-');
        result.insert(7, '-');

        return result.toString();
    }

    /**
     * 从任意用户文本输入创建种子。
     *
     * @param inputText 用户输入的文本。
     * @return 对应的长整型种子值。
     */
    public static long convertFromText(String inputText ){
        if (inputText.isEmpty()) return -1;

        //首先检查输入是否为种子代码，如果是则使用该格式
        try {
            return convertFromCode(inputText);
        } catch (IllegalArgumentException e){

        }

        //然后检查输入是否为数字（忽略空格），如果是则解析为长整型种子（处理溢出）
        try {
            return Long.parseLong(inputText.replaceAll("\\s", "")) % TOTAL_SEEDS;
        } catch (NumberFormatException e){

        }

        //最后，如果用户输入了未格式化的文本，则将其转换为等效的长整型种子
        long total = 0;
        for (char c : inputText.toCharArray()){
            total = 31 * total + c;
        }
        if (total < 0) total += Long.MAX_VALUE;
        total %= TOTAL_SEEDS;
        return total;
    }


    /**
     * 格式化文本输入。
     *
     * @param inputText 用户输入的文本。
     * @return 格式化后的文本或原始输入文本。
     */
    public static String formatText(String inputText ){
        try {
            //如果种子匹配代码，则转换为使用代码系统
            return convertToCode(convertFromCode(inputText));
        } catch (IllegalArgumentException e){
            //否则返回输入文本
            return inputText;
        }
    }

}

