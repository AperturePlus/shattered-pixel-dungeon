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

import com.badlogic.gdx.Files;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3FileHandle;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Preferences;
import com.badlogic.gdx.utils.SharedLibraryLoader;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.services.news.News;
import com.shatteredpixel.shatteredpixeldungeon.services.news.NewsImpl;
import com.shatteredpixel.shatteredpixeldungeon.services.updates.UpdateImpl;
import com.shatteredpixel.shatteredpixeldungeon.services.updates.Updates;
import com.watabou.noosa.Game;
import com.watabou.utils.FileUtils;
import com.watabou.utils.Point;

import org.lwjgl.util.tinyfd.TinyFileDialogs;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Locale;

/**
 * 主类用于启动桌面版本的游戏。
 * 负责初始化游戏环境，设置窗口配置，
 * 并处理异常和系统更新检查。
 */

public class DesktopLauncher {

    /**
     * 主函数，游戏的入口点。
     * @param args 命令行参数
     */
    public static void main(String[] args) {

        // 验证JVM状态是否适合运行游戏，如果不适合则退出
        if (!DesktopLaunchValidator.verifyValidJVMState(args)) {
            return;
        }

        // 检测FreeBSD操作系统，将其视为等同于Linux
        // TODO 可能需要向libGDX请求合并此功能
        if (System.getProperty("os.name").contains("FreeBSD")) {
            SharedLibraryLoader.isLinux = true;
            // 重写SharedLibraryLoader中静态初始化器设置的错误值
            SharedLibraryLoader.isIos = false;
            // 检查系统架构以确定是否为64位
            SharedLibraryLoader.is64Bit = System.getProperty("os.arch").contains("64") || System.getProperty("os.arch").startsWith("armv8");
        }

        // 初始化游戏标题
        final String title;
        if (DesktopLauncher.class.getPackage().getSpecificationTitle() == null) {
            title = System.getProperty("Specification-Title");
        } else {
            title = DesktopLauncher.class.getPackage().getSpecificationTitle();
        }

        // 设置默认的未捕获异常处理器
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            Game.reportException(throwable);
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            throwable.printStackTrace(pw);
            pw.flush();
            String exceptionMsg = sw.toString();

            // 缩短/简化异常消息，使其更容易适应消息框
            exceptionMsg = exceptionMsg.replaceAll("\\(.*:([0-9]*)\\)", "($1)");
            exceptionMsg = exceptionMsg.replace("com.shatteredpixel.shatteredpixeldungeon.", "");
            exceptionMsg = exceptionMsg.replace("com.watabou.", "");
            exceptionMsg = exceptionMsg.replace("com.badlogic.gdx.", "");
            exceptionMsg = exceptionMsg.replace("\t", "  "); // 缩短制表符长度

            // 替换单引号和双引号，因为tinyfd不喜欢它们
            exceptionMsg = exceptionMsg.replace('\'', '’');
            exceptionMsg = exceptionMsg.replace('"', '”');

            // 如果异常消息过长，截断至1000字符
            if (exceptionMsg.length() > 1000) {
                exceptionMsg = exceptionMsg.substring(0, 1000) + "...";
            }

            // 显示游戏崩溃的对话框
            if (exceptionMsg.contains("Couldn’t create window")) {
                TinyFileDialogs.tinyfd_messageBox(title + " 已崩溃!",
                        title + " 无法初始化图形显示，很抱歉！\n\n" +
                                "这通常发生在显卡驱动配置错误或不支持openGL 2.0+的情况下。\n\n" +
                                "如果你确定游戏应该能在你的电脑上运行，请联系开发者 (Evan@ShatteredPixel.com)\n\n" +
                                "版本: " + Game.version + "\n" +
                                exceptionMsg,
                        "确认", "错误", false);
            } else {
                TinyFileDialogs.tinyfd_messageBox(title + " 已崩溃!",
                        title + " 遇到无法恢复的错误并已崩溃，很抱歉！\n\n" +
                                "如果你能，麻烦将这个错误信息发给开发者 (Evan@ShatteredPixel.com):\n\n" +
                                "版本: " + Game.version + "\n" +
                                exceptionMsg,
                        "确认", "错误", false);
            }
            System.exit(1);
        });

        // 获取游戏版本信息
        Game.version = DesktopLauncher.class.getPackage().getSpecificationVersion();
        if (Game.version == null) {
            Game.version = System.getProperty("Specification-Version");
        }

        // 尝试获取游戏版本代码
        try {
            Game.versionCode = Integer.parseInt(DesktopLauncher.class.getPackage().getImplementationVersion());
        } catch (NumberFormatException e) {
            Game.versionCode = Integer.parseInt(System.getProperty("Implementation-Version"));
        }

        // 检查更新服务是否可用
        if (UpdateImpl.supportsUpdates()) {
            Updates.service = UpdateImpl.getUpdateService();
        }
        // 检查新闻服务是否可用
        if (NewsImpl.supportsNews()) {
            News.service = NewsImpl.getNewsService();
        }

        // 创建Lwjgl3应用程序配置
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();

        // 设置窗口标题
        config.setTitle(title);

        // 初始化保存路径
        String vendor = DesktopLauncher.class.getPackage().getImplementationTitle();
        if (vendor == null) {
            vendor = System.getProperty("Implementation-Title");
        }
        vendor = vendor.split("\\.")[1];

        String basePath = "";
        Files.FileType baseFileType = null;

        // 根据操作系统设置保存路径
        if (SharedLibraryLoader.isWindows) {
            if (System.getProperties().getProperty("os.name").equals("Windows XP")) {
                basePath = "Application Data/." + vendor + "/" + title + "/";
            } else {
                basePath = "AppData/Roaming/." + vendor + "/" + title + "/";
            }
            baseFileType = Files.FileType.External;
        } else if (SharedLibraryLoader.isMac) {
            basePath = "Library/Application Support/" + title + "/";
            baseFileType = Files.FileType.External;
        } else if (SharedLibraryLoader.isLinux) {
            String XDGHome = System.getenv("XDG_DATA_HOME");
            if (XDGHome == null) XDGHome = System.getProperty("user.home") + "/.local/share";

            String titleLinux = title.toLowerCase(Locale.ROOT).replace(" ", "-");
            basePath = XDGHome + "/." + vendor + "/" + titleLinux + "/";

            baseFileType = Files.FileType.Absolute;
        }

        // 设置偏好配置文件
        config.setPreferencesConfig(basePath, baseFileType);
        SPDSettings.set(new Lwjgl3Preferences(new Lwjgl3FileHandle(basePath + SPDSettings.DEFAULT_PREFS_FILE, baseFileType)));
        FileUtils.setDefaultFileProperties(baseFileType, basePath);

        // 设置窗口大小限制
        config.setWindowSizeLimits(720, 400, -1, -1);
        Point p = SPDSettings.windowResolution();
        config.setWindowedMode(p.x, p.y);

        // 设置窗口最大化状态
        config.setMaximized(SPDSettings.windowMaximized());

        // 监听窗口事件，记录窗口是否最大化
        DesktopWindowListener listener = new DesktopWindowListener();
        config.setWindowListener(listener);

        // 设置窗口图标
        config.setWindowIcon("icons/icon_16.png", "icons/icon_32.png", "icons/icon_48.png",
                "icons/icon_64.png", "icons/icon_128.png", "icons/icon_256.png");

        // 创建并启动游戏实例
        new Lwjgl3Application(new ShatteredPixelDungeon(new DesktopPlatformSupport()), config);
    }
}
