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

package com.shatteredpixel.shatteredpixeldungeon.desktop;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.PixmapPacker;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.watabou.input.ControllerHandler;
import com.watabou.noosa.Game;
import com.watabou.utils.PlatformSupport;
import com.watabou.utils.Point;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 用于支持桌面平台的平台支持类，继承自PlatformSupport类。
 */
public class DesktopPlatformSupport extends PlatformSupport {

    // 用于存储先前的窗口大小，以解决不保存最大化大小到设置中的问题
    protected static Point[] previousSizes = null;

    /**
     * 更新显示大小。
     * 如果之前没有记录窗口大小，则初始化数组并记录当前窗口大小。
     * 否则，将最新的窗口大小存储到数组中，并根据是否全屏设置窗口大小。
     */
    @Override
    public void updateDisplaySize() {
        if (previousSizes == null){
            previousSizes = new Point[2];
            previousSizes[1] = SPDSettings.windowResolution();
        } else {
            previousSizes[1] = previousSizes[0];
        }
        previousSizes[0] = new Point(Game.width, Game.height);
        if (!SPDSettings.fullscreen()) {
            SPDSettings.windowResolution( previousSizes[0] );
        }
    }

    /**
     * 更新系统用户界面。
     * 延迟执行以在正确的线程中设置全屏或窗口模式。
     */
    @Override
    public void updateSystemUI() {
        Gdx.app.postRunnable( new Runnable() {
            @Override
            public void run () {
                if (SPDSettings.fullscreen()){
                    Gdx.graphics.setFullscreenMode( Gdx.graphics.getDisplayMode() );
                } else {
                    Point p = SPDSettings.windowResolution();
                    Gdx.graphics.setWindowedMode( p.x, p.y );
                }
            }
        } );
    }

    /**
     * 检查是否连接到未计费网络。
     * 桌面平台上假设用户不在乎，因此始终返回true。
     *
     * @return 始终返回true。
     */
    @Override
    public boolean connectedToUnmeteredNetwork() {
        return true; //no easy way to check this in desktop, just assume user doesn't care
    }

    /**
     * 检查平台是否支持振动。
     * 仅当通过控制器支持振动时返回true。
     *
     * @return 如果支持振动则返回true，否则返回false。
     */
    @Override
    public boolean supportsVibration() {
        return ControllerHandler.vibrationSupported();
    }

    /* FONT SUPPORT */

    // 自定义像素字体，用于拉丁语和西里尔语
    private static FreeTypeFontGenerator basicFontGenerator;
    // Droid SansFallback，用于亚洲字体
    private static FreeTypeFontGenerator asianFontGenerator;

    /**
     * 设置字体生成器。
     * 根据pageSize和systemfont的值初始化或更新字体生成器。
     *
     * @param pageSize 页面大小。
     * @param systemfont 是否使用系统字体。
     */
    @Override
    public void setupFontGenerators(int pageSize, boolean systemfont) {
        // 如果字体生成器已设置且参数未改变，则不执行任何操作
        if (fonts != null && this.pageSize == pageSize && this.systemfont == systemfont){
            return;
        }
        this.pageSize = pageSize;
        this.systemfont = systemfont;

        resetGenerators(false);
        fonts = new HashMap<>();

        if (systemfont) {
            basicFontGenerator = asianFontGenerator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/droid_sans.ttf"));
        } else {
            basicFontGenerator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/pixel_font.ttf"));
            asianFontGenerator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/droid_sans.ttf"));
        }

        fonts.put(basicFontGenerator, new HashMap<>());
        fonts.put(asianFontGenerator, new HashMap<>());

        packer = new PixmapPacker(pageSize, pageSize, Pixmap.Format.RGBA8888, 1, false);
    }

    // 用于匹配亚洲字符的正则表达式匹配器
    private static Matcher asianMatcher = Pattern.compile("\\p{InHangul_Syllables}|" +
            "\\p{InCJK_Unified_Ideographs}|\\p{InCJK_Symbols_and_Punctuation}|\\p{InHalfwidth_and_Fullwidth_Forms}|" +
            "\\p{InHiragana}|\\p{InKatakana}").matcher("");

    /**
     * 根据输入字符串获取适当的字体生成器。
     * 如果字符串包含亚洲字符，则返回asianFontGenerator，否则返回basicFontGenerator。
     *
     * @param input 输入字符串。
     * @return 相应的字体生成器。
     */
    @Override
    protected FreeTypeFontGenerator getGeneratorForString( String input ){
        if (asianMatcher.reset(input).find()){
            return asianFontGenerator;
        } else {
            return basicFontGenerator;
        }
    }

    // 用于分割文本的正则表达式模式，考虑了换行符、下划线和亚洲字符
    private Pattern regularsplitter = Pattern.compile(
            "(?<=\n)|(?=\n)|(?<=_)|(?=_)|" +
                    "(?<=\\p{InHiragana})|(?=\\p{InHiragana})|" +
                    "(?<=\\p{InKatakana})|(?=\\p{InKatakana})|" +
                    "(?<=\\p{InCJK_Unified_Ideographs})|(?=\\p{InCJK_Unified_Ideographs})|" +
                    "(?<=\\p{InCJK_Symbols_and_Punctuation})|(?=\\p{InCJK_Symbols_and_Punctuation})");

    // 用于多行文本分割的正则表达式模式，除了考虑换行符和下划线，还考虑了单词边
    private Pattern regularsplitterMultiline = Pattern.compile(
            "(?<= )|(?= )|(?<=\n)|(?=\n)|(?<=_)|(?=_)|" +
                    "(?<=\\p{InHiragana})|(?=\\p{InHiragana})|" +
                    "(?<=\\p{InKatakana})|(?=\\p{InKatakana})|" +
                    "(?<=\\p{InCJK_Unified_Ideographs})|(?=\\p{InCJK_Unified_Ideographs})|" +
                    "(?<=\\p{InCJK_Symbols_and_Punctuation})|(?=\\p{InCJK_Symbols_and_Punctuation})");

    /**
     * 根据multiline标志分割文本。
     * 如果multiline为true，则使用regularsplitterMultiline模式；否则使用regularsplitter模式。
     *
     * @param text 待分割的文本。
     * @param multiline 是否考虑多行。
     * @return 分割后的字符串数组。
     */
    @Override
    public String[] splitforTextBlock(String text, boolean multiline) {
        if (multiline) {
            return regularsplitterMultiline.split(text);
        } else {
            return regularsplitter.split(text);
        }
    }
}

