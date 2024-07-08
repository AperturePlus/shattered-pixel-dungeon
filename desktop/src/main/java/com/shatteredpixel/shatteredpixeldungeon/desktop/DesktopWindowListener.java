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

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Window;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3WindowListener;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.watabou.noosa.audio.Music;

/**
 * 实现Lwjgl3WindowListener接口，用于监听桌面窗口的事件。
 * 这些事件包括窗口创建、最大化、最小化、失去焦点、获得焦点、关闭请求和文件拖放。
 */
public class DesktopWindowListener implements Lwjgl3WindowListener {

	/**
	 * 窗口创建事件。
	 * 当窗口被创建时调用，此时可以进行一些初始化的操作。
	 *
	 * @param lwjgl3Window 创建的窗口对象。
	 */
	@Override
	public void created(Lwjgl3Window lwjgl3Window) {
		// 初始化操作可以在这里进行
	}

	/**
	 * 窗口最大化事件。
	 * 当窗口被最大化或取消最大化时调用，用于更新配置以适应最大化状态。
	 *
	 * @param b 表示窗口是否被最大化。
	 */
	@Override
	public void maximized(boolean b) {
		SPDSettings.windowMaximized(b);
		if (b) {
			// 如果窗口被最大化，设置窗口分辨率为此前记录的最大化尺寸
			SPDSettings.windowResolution(DesktopPlatformSupport.previousSizes[1]);
		}
	}

	/**
	 * 窗口最小化事件。
	 * 当窗口被最小化时调用，可以在此处进行相关处理。
	 *
	 * @param b 表示窗口是否被最小化。
	 */
	@Override
	public void iconified(boolean b) {
		// 处理窗口最小化事件的逻辑
	}

	/**
	 * 窗口失去焦点事件。
	 * 当窗口失去焦点时调用，用于暂停音乐播放，如果配置不允许后台播放音乐。
	 */
	public void focusLost() {
		if (!SPDSettings.playMusicInBackground()) {
			Music.INSTANCE.pause();
		}
	}

	/**
	 * 窗口获得焦点事件。
	 * 当窗口获得焦点时调用，用于恢复音乐播放，如果配置不允许后台播放音乐。
	 */
	public void focusGained() {
		if (!SPDSettings.playMusicInBackground()) {
			Music.INSTANCE.resume();
		}
	}

	/**
	 * 获取关闭请求事件的处理结果。
	 * 此方法返回true表示允许关闭窗口，子类可以重写此方法以自定义关闭行为。
	 *
	 * @return true表示允许关闭窗口，false表示阻止关闭窗口。
	 */
	public boolean closeRequested() {
		return true;
	}

	/**
	 * 文件拖放到窗口事件。
	 * 当有文件被拖放到窗口上时调用，可以在此处处理这些文件。
	 *
	 * @param strings 被拖放的文件路径数组。
	 */
	public void filesDropped(String[] strings) {
		// 处理文件拖放事件的逻辑
	}

	/**
	 * 刷新请求事件。
	 * 当窗口需要被刷新时调用，可以在此处进行相关处理。
	 */
	public void refreshRequested() {
		// 处理窗口刷新请求的逻辑
	}
}

