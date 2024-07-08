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

package com.watabou.noosa;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.graphics.glutils.GLVersion;
import com.badlogic.gdx.utils.TimeUtils;
import com.watabou.glscripts.Script;
import com.watabou.gltextures.TextureCache;
import com.watabou.glwrap.Blending;
import com.watabou.glwrap.Vertexbuffer;
import com.watabou.input.ControllerHandler;
import com.watabou.input.InputHandler;
import com.watabou.input.PointerEvent;
import com.watabou.noosa.audio.Music;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Callback;
import com.watabou.utils.DeviceCompat;
import com.watabou.utils.PlatformSupport;
import com.watabou.utils.Reflection;

import java.io.PrintWriter;
import java.io.StringWriter;

public class Game implements ApplicationListener {

	public static Game instance;

	//actual size of the display
	public static int dispWidth;
	public static int dispHeight;
	
	// Size of the EGL surface view
	public static int width;
	public static int height;

	//number of pixels from bottom of view before rendering starts
	public static int bottomInset;

	// Density: mdpi=1, hdpi=1.5, xhdpi=2...
	public static float density = 1;
	
	public static String version;
	public static int versionCode;
	
	// Current scene
	protected Scene scene;
	// New scene we are going to switch to
	protected Scene requestedScene;
	// true if scene switch is requested
	protected boolean requestedReset = true;
	// callback to perform logic during scene change
	protected SceneChangeCallback onChange;
	// New scene class
	protected static Class<? extends Scene> sceneClass;
	
	public static float timeScale = 1f;
	public static float elapsed = 0f;
	public static float timeTotal = 0f;
	public static long realTime = 0;

	public static InputHandler inputHandler;
	
	public static PlatformSupport platform;
	
	/**
	 * Game类的构造函数。
	 *
	 * 该构造函数用于初始化Game实例，绑定特定的场景类和平台支持。
	 * @param c 场景类的Class对象，表示游戏将要加载的场景类型。
	 * @param platform 平台支持对象，提供与具体运行平台相关的功能支持。
	 */
	public Game(Class<? extends Scene> c, PlatformSupport platform) {
	    // 存储场景类的Class对象，用于后续创建场景实例。
	    sceneClass = c;

	    // 设置当前Game实例为全局唯一实例，用于其他部分访问。
	    instance = this;
	    // 初始化平台支持对象，以便在游戏运行中调用平台特定的功能。
	    this.platform = platform;
	}
	
		/**
	 * 初始化游戏或应用程序。此方法在应用程序启动过程中被调用。
	 * 它设置运行应用程序所必需的基本配置和资源。
	 */
	@Override
	public void create() {
		// 获取设备屏幕的像素密度，用于适应不同密度下的图形显示。
		density = Gdx.graphics.getDensity();
		// 如果无法获取到密度值（正无穷大），则设定一个兼容性的默认值。
		if (density == Float.POSITIVE_INFINITY){
			density = 100f / 160f; // 如果无法找到密度，则假设为100PPI
		}
		// 获取屏幕的高度和宽度，用于布局和渲染。
		dispHeight = Gdx.graphics.getDisplayMode().height;
		dispWidth = Gdx.graphics.getDisplayMode().width;

		// 初始化输入处理器以处理用户输入。
		inputHandler = new InputHandler( Gdx.input );
		// 如果检测到支持控制器，则添加控制器监听器。
		if (ControllerHandler.controllersSupported()){
			Controllers.addListener(new ControllerHandler());
		}

		// 刷新GPU相关的资源，确保它们与当前的图形环境兼容。
		// 更新存储在GPU上的纹理和顶点数据。
		versionContextRef = Gdx.graphics.getGLVersion();
		Blending.useDefault();
		// 重新加载纹理和顶点缓冲区，确保它们在设备上正确显示。
		TextureCache.reload();
		Vertexbuffer.reload();
	}


	private GLVersion versionContextRef;
	
		@Override
    /**
     * 重设游戏窗口或屏幕大小。
     *
     * 此方法根据新的宽度和高度调整游戏的渲染区域。如果宽度或高度为0，
     * 则直接返回，不进行任何操作。
     *
     * @param width 新的宽度
     * @param height 新的高度
     */
	public void resize(int width, int height) {
		if (width == 0 || height == 0){
			// 如果宽度或高度为0，不执行后续操作。
			return;
		}

		// 如果EGL上下文被销毁，我们需要刷新存储在GPU上的一些数据。
		// 这里检查GLVersion是否有新的对象引用。
		if (versionContextRef != Gdx.graphics.getGLVersion()) {
			versionContextRef = Gdx.graphics.getGLVersion();
			// 使用默认的混合模式。
			Blending.useDefault();
			// 重新加载纹理缓存。
			TextureCache.reload();
			// 重新加载顶点缓冲区。
			Vertexbuffer.reload();
		}

		height -= bottomInset;
		if (height != Game.height || width != Game.width) {

			// 更新游戏的宽度和高度。
			Game.width = width;
			Game.height = height;

			// TODO 可能更适合将此放在平台支持中。
			if (Gdx.app.getType() != Application.ApplicationType.Android){
				// 设置显示宽度和高度（非Android平台）。
				Game.dispWidth = Game.width;
				Game.dispHeight = Game.height;
			}

			// 重置场景。
			resetScene();
		}
	}


	///justResumed is used for two purposes:
	//firstly, to clear pointer events when the game is resumed,
	// this helps with input errors caused by system gestures on iOS/Android
	//secondly, as a bit of a hack to improve start time metrics on Android,
	// as texture refreshing leads to slow warm starts. TODO would be nice to fix this properly
	private boolean justResumed = true;

		/**
	 * 渲染当前帧。
	 * 此方法负责更新和渲染游戏场景。它处理各种情况，例如应用程序恢复，
	 * 重置摄像机，以及执行绘图逻辑。
	 */
	@Override
	public void render() {
		// 确保只有一个应用程序实例运行，以避免奇怪的问题
		if (instance != this){
			// 结束当前实例
			finish();
			return;
		}

		if (justResumed){
			// 清除所有指针事件
			PointerEvent.clearPointerEvents();
			justResumed = false;
			// 如果是安卓设备则直接返回
			if (DeviceCompat.isAndroid()) return;
		}

		// 重置带有光照效果的摄像机
		NoosaScript.get().resetCamera();
		// 重置无光照效果的摄像机
		NoosaScriptNoLighting.get().resetCamera();
		// 禁用剪裁测试
		Gdx.gl.glDisable(Gdx.gl.GL_SCISSOR_TEST);
		// 清除颜色缓冲区
		Gdx.gl.glClear(Gdx.gl.GL_COLOR_BUFFER_BIT);
		// 执行绘图操作
		draw();

		// 再次禁用剪裁测试
		Gdx.gl.glDisable( Gdx.gl.GL_SCISSOR_TEST );

		// 执行每帧的更新步骤
		step();
	}

	
	@Override
	public void pause() {
		PointerEvent.clearPointerEvents();
		
		if (scene != null) {
			scene.onPause();
		}
		
		Script.reset();
	}
	
	@Override
	public void resume() {
		justResumed = true;
	}
	
	public void finish(){
		Gdx.app.exit();
		
	}
	
	public void destroy(){
		if (scene != null) {
			scene.destroy();
			scene = null;
		}
		
		sceneClass = null;
		Music.INSTANCE.stop();
		Sample.INSTANCE.reset();
	}
	
	@Override
	public void dispose() {
		destroy();
	}
	
	public static void resetScene() {
		switchScene( instance.sceneClass );
	}

	public static void switchScene(Class<? extends Scene> c) {
		switchScene(c, null);
	}
	
	public static void switchScene(Class<? extends Scene> c, SceneChangeCallback callback) {
		instance.sceneClass = c;
		instance.requestedReset = true;
		instance.onChange = callback;
	}
	
	public static Scene scene() {
		return instance.scene;
	}

	public static boolean switchingScene() {
		return instance.requestedReset;
	}
	
	/**
	 * 执行场景切换的步骤。
	 * 此方法负责检查是否请求了场景重置，如果请求了，则进行场景重置并更新场景。
	 * 场景重置包括创建新场景并切换到新场景。
	 * 如果没有请求重置，则仅执行场景的更新操作。
	 */
	protected void step() {
	    // 检查是否请求了场景重置
	    if (requestedReset) {
	        // 将重置请求标记为已完成
	        requestedReset = false;

	        // 尝试使用反射创建新场景实例
	        requestedScene = Reflection.newInstance(sceneClass);
	        // 如果新场景实例创建成功，则进行场景切换
	        if (requestedScene != null){
	            switchScene();
	        }
	    }

	    // 更新当前场景
	    update();
	}
	
	protected void draw() {
		if (scene != null) scene.draw();
	}
	
	protected void switchScene() {

		Camera.reset();
		
		if (scene != null) {
			scene.destroy();
		}
		//clear any leftover vertex buffers
		Vertexbuffer.clear();
		scene = requestedScene;
		if (onChange != null) onChange.beforeCreate();
		scene.create();
		if (onChange != null) onChange.afterCreate();
		onChange = null;
		
		Game.elapsed = 0f;
		Game.timeScale = 1f;
		Game.timeTotal = 0f;
	}

	protected void update() {
		Game.elapsed = Game.timeScale * Gdx.graphics.getDeltaTime();
		Game.timeTotal += Game.elapsed;
		
		Game.realTime = TimeUtils.millis();

		inputHandler.processAllEvents();

		Music.INSTANCE.update();
		Sample.INSTANCE.update();
		scene.update();
		Camera.updateAll();
	}
	
	/**
 * 报告异常。
 * 此方法用于记录和输出异常信息。如果全局实例和应用程序对象可用，它将使用实例的日志记录方法；
 * 否则，它会退回到标准错误输出机制。
 *
 * @param tr 表示要报告的异常的 Throwable 对象。
 */
public static void reportException( Throwable tr ) {
    // 检查实例和应用程序是否已初始化以使用优化的日志记录方法
    if (instance != null && Gdx.app != null) {
        instance.logException(tr);
    } else {
        // 如果异常发生在初始化阶段的回退日志记录机制
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        tr.printStackTrace(pw);
        pw.flush();
        System.err.println(sw.toString());
    }
}

	
	protected void logException( Throwable tr ){
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		tr.printStackTrace(pw);
		pw.flush();
		Gdx.app.error("GAME", sw.toString());
	}
	
	public static void runOnRenderThread(Callback c){
		Gdx.app.postRunnable(new Runnable() {
			@Override
			public void run() {
				c.call();
			}
		});
	}
	
	public static void vibrate( int milliseconds ) {
		if (platform.supportsVibration()) {
			platform.vibrate(milliseconds);
		}
	}

	public interface SceneChangeCallback{
		void beforeCreate();
		void afterCreate();
	}
	
}
