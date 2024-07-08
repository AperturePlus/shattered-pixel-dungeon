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

import com.badlogic.gdx.utils.SharedLibraryLoader;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;

public class public class DesktopLaunchValidator {

    /**
     * 验证启动的JVM是否正确配置，并尝试重新启动一个新的JVM如果当前的不满足条件。
     * 如果当前JVM无效应被终止，则返回false。
     *
     * @param args 启动时传入的命令行参数
     * @return 如果JVM有效或已成功重新启动则返回true，否则返回false
     */
    public static boolean verifyValidJVMState(String[] args) {

        // Mac电脑需要-XstartOnFirstThread的JVM参数
        if (SharedLibraryLoader.isMac) {

            // 如果-XstartOnFirstThread存在并且启用，我们可以直接返回true
            String threadStartEnvVar = System.getenv("JAVA_STARTED_ON_FIRST_THREAD_" +
                    ManagementFactory.getRuntimeMXBean().getName().split("@")[0]);
            if ("1".equals(threadStartEnvVar)) {
                return true;
            }

            // 检查是否已经是重新启动后的进程，如果是则返回true以避免循环重启。
            // 游戏可能仍然会崩溃，但这点无法避免。
            if ("true".equals(System.getProperty("shpdRelaunched"))) {
                System.err.println("Error: could not verify the new process running on the first thread. Try run the game anyway. " +
									"错误: 无法验证新进程是在第一条线程上运行。尝试无论如何运行游戏...");
                return true;
            }

            // 重新启动一个带有-XstartOnFirstThread参数的新JVM进程
            String sep = System.getProperty("file.separator");

            ArrayList<String> jvmArgs = new ArrayList<>();
            jvmArgs.add(System.getProperty("java.home") + sep + "bin" + sep + "java");
            jvmArgs.add("-XstartOnFirstThread");
            jvmArgs.add("-DshpdRelaunched=true");
            jvmArgs.addAll(ManagementFactory.getRuntimeMXBean().getInputArguments());
            jvmArgs.add("-cp");
            jvmArgs.add(System.getProperty("java.class.path"));
            jvmArgs.add(DesktopLauncher.class.getName());

            System.err.println("Error: ShatteredPD must run on the first thread so as to work on MacOS." +"\n"+
					"			错误: ShatteredPD 必须在第一条线程上启动才能在macOS上运行。");
            System.err.println("To avoid this error, use \"-XstartOnFirstThread\" arg to run the game." +"\n"+
								"为了避免这个错误，请使用\"-XstartOnFirstThread\"参数运行游戏");
            System.err.println("Now attempting automatically relaunch the game on the first thread.\n" +
								"现在尝试自动重新启动游戏在第一条线程上:\n");

            try {
                Process process = new ProcessBuilder(jvmArgs).redirectErrorStream(true).start();
                BufferedReader out = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;

                // 从重新启动的进程中转发控制台输出
                while ((line = out.readLine()) != null) {
                    if (line.toLowerCase().startsWith("error")) {
                        System.err.println(line);
                    } else {
                        System.out.println(line);
                    }
                }

                process.waitFor();
            } catch (Exception e) {
                e.printStackTrace();
            }

            return false;

        }

        // 如果不是Mac系统，直接返回true
        return true;
    }

}
