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

import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.watabou.utils.DeviceCompat;
import com.watabou.utils.Signal;

/**
 * GLog类用于游戏日志记录，提供不同类型的日志输出方法。
 * 它通过静态方法简化了日志记录的过程，并支持参数化日志消息。
 */
public class GLog {

    /**
     * 日志标签，用于标识游戏日志。
     */
    public static final String TAG = "GAME";

    /**
     * 日志前缀，表示正面消息。
     */
    public static final String POSITIVE = "++ ";
    /**
     * 日志前缀，表示负面消息。
     */
    public static final String NEGATIVE = "-- ";
    /**
     * 日志前缀，表示警告消息。
     */
    public static final String WARNING = "** ";
    /**
     * 日志前缀，表示高亮消息。
     */
    public static final String HIGHLIGHT = "@@ ";

    /**
     * 换行符，用于日志输出中的换行。
     */
    public static final String NEW_LINE = "\n";

    /**
     * 信号量，用于更新日志显示。
     * 通过dispatch方法分发日志消息，可以在其他地方订阅此信号量以更新日志显示。
     */
    public static Signal<String> update = new Signal<>();

    /**
     * 输出普通日志。
     *
     * @param text 日志文本，可以包含格式化占位符。
     * @param args 格式化占位符的参数。
     */
    public static void newLine(){
        update.dispatch(NEW_LINE);
    }

    /**
     * 输出普通日志。
     * 如果提供了参数，则使用Messages.format方法格式化日志文本。
     * 最终通过DeviceCompat.log方法输出日志。
     *
     * @param text 日志文本，可以包含格式化占位符。
     * @param args 格式化占位符的参数。
     */
    public static void i( String text, Object... args ) {
        if (args.length > 0) {
            text = Messages.format( text, args );
        }
        DeviceCompat.log( TAG, text );
        update.dispatch(text);
    }

    /**
     * 输出正面消息日志。
     * 在文本前添加POSITIVE前缀，然后调用i方法输出日志。
     *
     * @param text 日志文本，可以包含格式化占位符。
     * @param args 格式化占位符的参数。
     */
    public static void p( String text, Object... args ) {
        i(POSITIVE + text, args);
    }

    /**
     * 输出负面消息日志。
     * 在文本前添加NEGATIVE前缀，然后调用i方法输出日志。
     *
     * @param text 日志文本，可以包含格式化占位符。
     * @param args 格式化占位符的参数。
     */
    public static void n( String text, Object... args ) {
        i(NEGATIVE + text, args);
    }

    /**
     * 输出警告消息日志。
     * 在文本前添加WARNING前缀，然后调用i方法输出日志。
     *
     * @param text 日志文本，可以包含格式化占位符。
     * @param args 格式化占位符的参数。
     */
    public static void w( String text, Object... args ) {
        i(WARNING + text, args);
    }

    /**
     * 输出高亮消息日志。
     * 在文本前添加HIGHLIGHT前缀，然后调用i方法输出日志。
     *
     * @param text 日志文本，可以包含格式化占位符。
     * @param args 格式化占位符的参数。
     */
    public static void h( String text, Object... args ) {
        i(HIGHLIGHT + text, args);
    }
}

