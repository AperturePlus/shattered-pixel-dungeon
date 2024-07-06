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

import com.shatteredpixel.shatteredpixeldungeon.messages.Languages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.watabou.noosa.Game;
import com.watabou.noosa.audio.Music;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.DeviceCompat;
import com.watabou.utils.GameSettings;
import com.watabou.utils.Point;

import java.util.Locale;

public class SPDSettings extends GameSettings {
	
	//Version info
	
		// 配置键值
	public static final String KEY_VERSION      = "version";

	/**
	 * 设置游戏版本号。
	 *
	 * @param value 游戏版本数值。
	 */
	public static void version( int value)  {
		put( KEY_VERSION, value );
	}

	/**
	 * 获取游戏版本号。
	 *
	 * @return 游戏版本数值。
	 */
	public static int version() {
		return getInt( KEY_VERSION, 0 );
	}

	// 显示相关配置键值
	// 显示设置

	public static final String KEY_FULLSCREEN	= "fullscreen";
	public static final String KEY_LANDSCAPE	= "landscape";
	public static final String KEY_POWER_SAVER 	= "power_saver";
	public static final String KEY_ZOOM			= "zoom";
	public static final String KEY_BRIGHTNESS	= "brightness";
	public static final String KEY_GRID 	    = "visual_grid";
	public static final String KEY_CAMERA_FOLLOW= "camera_follow";
	public static final String KEY_SCREEN_SHAKE = "screen_shake";

	/**
	 * 设置全屏模式。
	 * 同时根据设置更新系统UI。
	 *
	 * @param value 是否开启全屏模式。
	 */
	public static void fullscreen( boolean value ) {
		put( KEY_FULLSCREEN, value );
		ShatteredPixelDungeon.updateSystemUI();
	}

	/**
	 * 检查当前是否为全屏模式。
	 *
	 * @return 是否处于全屏模式，桌面设备默认为全屏。
	 */
	public static boolean fullscreen() {
		return getBoolean( KEY_FULLSCREEN, DeviceCompat.isDesktop() );
	}

	/**
	 * 设置横屏模式。
	 * 更新显示尺寸以适配设置。
	 *
	 * @param value 是否开启横屏模式。
	 */
	public static void landscape( boolean value ){
		put( KEY_LANDSCAPE, value );
		((ShatteredPixelDungeon)ShatteredPixelDungeon.instance).updateDisplaySize();
	}

	
	//can return null because we need to directly handle the case of landscape not being set
	// as there are different defaults for different devices
	/**
	 * 检查当前配置是否以横屏方式显示。
	 * 如果配置中包含横屏设置，则返回该设置值；否则，返回null。
	 */
	public static Boolean landscape(){
	    if (contains(KEY_LANDSCAPE)){
	        return getBoolean(KEY_LANDSCAPE, false);
	    } else {
	        return null;
	    }
	}

	/**
	 * 设置省电模式状态。
	 * @param value 省电模式的新状态，true为开启，false为关闭。
	 */
	public static void powerSaver( boolean value ){
	    put( KEY_POWER_SAVER, value );
	    ((ShatteredPixelDungeon)ShatteredPixelDungeon.instance).updateDisplaySize();
	}

	/**
	 * 获取当前省电模式的状态。
	 * @return 省电模式的状态，如果未设置则默认为false。
	 */
	public static boolean powerSaver(){
	    return getBoolean( KEY_POWER_SAVER, false );
	}

	/**
	 * 设置画面缩放级别。
	 * @param value 新的缩放级别。
	 */
	public static void zoom( int value ) {
	    put( KEY_ZOOM, value );
	}

	/**
	 * 获取当前的缩放级别。
	 * @return 当前的缩放级别，如果没有设置，则默认为0。
	 */
	public static int zoom() {
	    return getInt( KEY_ZOOM, 0 );
	}

	/**
	 * 设置屏幕亮度。
	 * @param value 新的亮度级别。
	 */
	public static void brightness( int value ) {
	    put( KEY_BRIGHTNESS, value );
	    GameScene.updateFog();
	}

	/**
	 * 获取当前的屏幕亮度。
	 * @return 当前的亮度级别，如果没有设置，则默认为0，并在一定范围内[-1, 1]。
	 */
	public static int brightness() {
	    return getInt( KEY_BRIGHTNESS, 0, -1, 1 );
	}

	/**
	 * 设置视觉网格大小。
	 * @param value 新的视觉网格大小设置。
	 */
	public static void visualGrid( int value ){
	    put( KEY_GRID, value );
	    GameScene.updateMap();
	}

	/**
	 * 获取当前的视觉网格大小。
	 * @return 当前的视觉网格大小设置，如果没有设置，则默认为0，并在一定范围内[-1, 2]。
	 */
	public static int visualGrid() {
	    return getInt( KEY_GRID, 0, -1, 2 );
	}

	/**
	 * 设置相机跟随模式。
	 * @param value 新的相机跟随模式设置。
	 */
	public static void cameraFollow( int value ){
	    put( KEY_CAMERA_FOLLOW, value );
	}

	/**
	 * 获取当前的相机跟随模式。
	 * @return 当前的相机跟随模式设置，如果没有设置，则默认为4，并在一定范围内[1, 4]。
	 */
	public static int cameraFollow() {
	    return getInt( KEY_CAMERA_FOLLOW, 4, 1, 4 );
	}

	/**
	 * 设置屏幕震动强度。
	 * @param value 新的屏幕震动强度设置。
	 */
	public static void screenShake( int value ){
	    put( KEY_SCREEN_SHAKE, value );
	}

	/**
	 * 获取当前的屏幕震动强度。
	 * @return 当前的屏幕震动强度设置，如果没有设置，则默认为2，并在一定范围内[0, 4]。
	 */
	public static int screenShake() {
	    return getInt( KEY_SCREEN_SHAKE, 2, 0, 4 );
	}

	// Interface related constants

	/**
	 * 用户界面大小设置的键。
	 */
	public static final String KEY_UI_SIZE 	    = "full_ui";
	/**
	 * 屏幕缩放设置的键。
	 */
	public static final String KEY_SCALE		= "scale";
	/**
	 * 快速交换槽设置的键。
	 */
	public static final String KEY_QUICK_SWAP	= "quickslot_swapper";
	/**
	 * 是否翻转工具栏设置的键。
	 */
	public static final String KEY_FLIPTOOLBAR	= "flipped_ui";
	/**
	 * 是否翻转标签设置的键。
	 */
	public static final String KEY_FLIPTAGS 	= "flip_tags";
	/**
	 * 工具栏模式设置的键。
	 */
	public static final String KEY_BARMODE		= "toolbar_mode";
	/**
	 * 快速槽位水皮肤设置的键。
	 */
	public static final String KEY_SLOTWATERSKIN= "quickslot_waterskin";
	/**
	 * 系统字体设置的键。
	 */
	public static final String KEY_SYSTEMFONT	= "system_font";
	/**
	 * 是否启用振动设置的键。
	 */
	public static final String KEY_VIBRATION    = "vibration";

	/**
	 * 设置用户界面大小。
	 * @param value 用户界面大小的新设置，影响界面的布局和尺寸。
	 */
	public static void interfaceSize( int value ){
	    put( KEY_UI_SIZE, value );
	}

	/**
	 * 获取当前的用户界面大小设置。
	 * @return 当前的用户界面大小设置，该设置可能基于设备类型和屏幕尺寸动态调整。
	 */
	public static int interfaceSize(){
	    int size = getInt( KEY_UI_SIZE, DeviceCompat.isDesktop() ? 2 : 0 );
	    if (size > 0){
	        // 根据屏幕尺寸判断是否足够显示完整UI，如果空间不足，则强制使用移动设备UI布局
	        float wMin = Game.width / PixelScene.MIN_WIDTH_FULL;
	        float hMin = Game.height / PixelScene.MIN_HEIGHT_FULL;
	        if (Math.min(wMin, hMin) < 2*Game.density){
	            size = 0;
	        }
	    }
	    return size;
	}
	public static void scale( int value ) {
		put( KEY_SCALE, value );
	}

	public static int scale() {
		return getInt( KEY_SCALE, 0 );
	}
	
	public static void quickSwapper(boolean value ){ put( KEY_QUICK_SWAP, value ); }
	
	public static boolean quickSwapper(){ return getBoolean( KEY_QUICK_SWAP, true); }
	
	public static void flipToolbar( boolean value) {
		put(KEY_FLIPTOOLBAR, value );
	}
	
	public static boolean flipToolbar(){ return getBoolean(KEY_FLIPTOOLBAR, false); }
	
	public static void flipTags( boolean value) {
		put(KEY_FLIPTAGS, value );
	}
	
	public static boolean flipTags(){ return getBoolean(KEY_FLIPTAGS, false); }
	
	public static void toolbarMode( String value ) {
		put( KEY_BARMODE, value );
	}
	
	public static String toolbarMode() {
		return getString(KEY_BARMODE, PixelScene.landscape() ? "GROUP" : "SPLIT");
	}

	public static void quickslotWaterskin( boolean value ){
		put( KEY_SLOTWATERSKIN, value);
	}

	public static boolean quickslotWaterskin(){
		return getBoolean( KEY_SLOTWATERSKIN, true );
	}

	public static void systemFont(boolean value){
		put(KEY_SYSTEMFONT, value);
	}

	public static boolean systemFont(){
		//return getBoolean(KEY_SYSTEMFONT,
				//(language() == Languages.KOREAN || language() == Languages.CHINESE || language() == Languages.JAPANESE));
		return getBoolean(KEY_SYSTEMFONT, language() == Languages.CHINESE);

	}

	public static void vibration(boolean value){
		put(KEY_VIBRATION, value);
	}

	public static boolean vibration(){
		return getBoolean(KEY_VIBRATION, true);
	}

	//Game State
	
	/**
	 * 该类提供了一些静态方法来处理应用程序的首选项。
	 * 它主要用于存储和检索用户的设置和进度数据。
	 */

	    // 存储最后一个玩的课程的键
	    public static final String KEY_LAST_CLASS = "last_class";
	    // 存储挑战完成数量的键
	    public static final String KEY_CHALLENGES = "challenges";
	    // 存储用户自定义随机数种子的键
	    public static final String KEY_CUSTOM_SEED = "custom_seed";
	    // 存储上一次完成的每日挑战时间的键
	    public static final String KEY_LAST_DAILY = "last_daily";
	    // 存储是否展示过介绍的键
	    public static final String KEY_INTRO = "intro";
	    // 存储是否已经提醒过支持的键
	    public static final String KEY_SUPPORT_NAGGED = "support_nagged";

	    /**
	     * 保存是否已经展示过介绍的设置。
	     *
	     * @param value true表示已经展示过介绍，false表示还没有。
	     */
	    public static void intro(boolean value) {
	        put(KEY_INTRO, value);
	    }

	    /**
	     * 获取是否已经展示过介绍的设置。
	     *
	     * @return 如果已经展示过介绍则返回true，否则返回false。如果值不存在，默认为true。
	     */
	    public static boolean intro() {
	        return getBoolean(KEY_INTRO, true);
	    }

	    /**
	     * 保存最后一个玩的课程的设置。
	     *
	     * @param value 最后一个玩的课程的编号。
	     */
	    public static void lastClass(int value) {
	        put(KEY_LAST_CLASS, value);
	    }

	    /**
	     * 获取最后一个玩的课程的设置。
	     *
	     * @return 最后一个玩的课程的编号。如果值不存在，默认为0。
	     */
	    public static int lastClass() {
	        return getInt(KEY_LAST_CLASS, 0, 0, 3);
	    }

	    /**
	     * 保存已完成挑战的数量的设置。
	     *
	     * @param value 完成的挑战数量。
	     */
	    public static void challenges(int value) {
	        put(KEY_CHALLENGES, value);
	    }

	    /**
	     * 获取已完成挑战的数量的设置。
	     *
	     * @return 完成的挑战数量。如果值不存在，默认为0。
	     */
	    public static int challenges() {
	        return getInt(KEY_CHALLENGES, 0, 0, Challenges.MAX_VALUE);
	    }

	    /**
	     * 保存用户自定义随机数种子的设置。
	     *
	     * @param value 用户自定义的随机数种子。
	     */
	    public static void customSeed(String value) {
	        put(KEY_CUSTOM_SEED, value);
	    }

	    /**
	     * 获取用户自定义随机数种子的设置。
	     *
	     * @return 用户自定义的随机数种子。如果值不存在，默认为空字符串。
	     */
	    public static String customSeed() {
	        return getString(KEY_CUSTOM_SEED, "", 20);
	    }

	    /**
	     * 保存上一次完成的每日挑战时间的设置。
	     *
	     * @param value 上一次完成的每日挑战的时间戳。
	     */
	    public static void lastDaily(long value) {
	        put(KEY_LAST_DAILY, value);
	    }

	    /**
	     * 获取上一次完成的每日挑战时间的设置。
	     *
	     * @return 上一次完成的每日挑战的时间戳。如果值不存在，默认为0。
	     */
	    public static long lastDaily() {
	        return getLong(KEY_LAST_DAILY, 0);
	    }

	    /**
	     * 保存是否已经提醒过支持的设置。
	     *
	     * @param value true表示已经提醒过支持，false表示还没有。
	     */
	    public static void supportNagged(boolean value) {
	        put(KEY_SUPPORT_NAGGED, value);
	    }

	    /**
	     * 获取是否已经提醒过支持的设置。
	     *
	     * @return 如果已经提醒过支持则返回true，否则返回false。如果值不存在，默认为false。
	     */
	    public static boolean supportNagged() {
	        return getBoolean(KEY_SUPPORT_NAGGED, false);
	    }


	//Input

	/**
	 * 控制器灵敏度设置的键名常量。
	 * 用于存储和检索控制器灵敏度的配置值。
	 */
	public static final String KEY_CONTROLLER_SENS  = "controller_sens";

	/**
	 * 移动保持灵敏度设置的键名常量。
	 * 用于存储和检索移动保持灵敏度的配置值。
	 */
	public static final String KEY_MOVE_SENS        = "move_sens";

	/**
	 * 设置控制器指针灵敏度。
	 *
	 * @param value 灵敏度值，用于调整控制器的敏感程度。
	 */
	public static void controllerPointerSensitivity( int value ){
		put( KEY_CONTROLLER_SENS, value );
	}

	/**
	 * 获取控制器指针灵敏度。
	 *
	 * @return 当前设置的控制器指针灵敏度值。
	 *         如果没有设置，默认值为5，范围为1到10。
	 */
	public static int controllerPointerSensitivity(){
		return getInt(KEY_CONTROLLER_SENS, 5, 1, 10);
	}

	/**
	 * 设置移动保持灵敏度。
	 *
	 * @param value 灵敏度值，用于调整移动保持的敏感程度。
	 */
	public static void movementHoldSensitivity( int value ){
		put( KEY_MOVE_SENS, value );
	}

	/**
	 * 获取移动保持灵敏度。
	 *
	 * @return 当前设置的移动保持灵敏度值。
	 *         如果没有设置，默认值为3，范围为0到4。
	 */
	public static int movementHoldSensitivity(){
		return getInt(KEY_MOVE_SENS, 3, 0, 4);
	}


	//Connectivity

	public static final String KEY_NEWS     = "news";
	public static final String KEY_UPDATES	= "updates";
	public static final String KEY_BETAS	= "betas";
	public static final String KEY_WIFI     = "wifi";

	public static final String KEY_NEWS_LAST_READ = "news_last_read";

	public static void news(boolean value){
		put(KEY_NEWS, value);
	}

	public static boolean news(){
		return getBoolean(KEY_NEWS, true);
	}

	public static void updates(boolean value){
		put(KEY_UPDATES, value);
	}

	public static boolean updates(){
		return getBoolean(KEY_UPDATES, true);
	}

	public static void betas(boolean value){
		put(KEY_BETAS, value);
	}

	public static boolean betas(){
		return getBoolean(KEY_BETAS, Game.version.contains("BETA") || Game.version.contains("RC"));
	}

	public static void WiFi(boolean value){
		put(KEY_WIFI, value);
	}

	public static boolean WiFi(){
		return getBoolean(KEY_WIFI, true);
	}

	public static void newsLastRead(long lastRead){
		put(KEY_NEWS_LAST_READ, lastRead);
	}

	public static long newsLastRead(){
		return getLong(KEY_NEWS_LAST_READ, 0);
	}

	//Audio
	
	public static final String KEY_MUSIC		= "music";
	public static final String KEY_MUSIC_VOL    = "music_vol";
	public static final String KEY_SOUND_FX		= "soundfx";
	public static final String KEY_SFX_VOL      = "sfx_vol";
	public static final String KEY_IGNORE_SILENT= "ignore_silent";
	public static final String KEY_MUSIC_BG     = "music_bg";
	
	/**
	 * 控制音乐是否启用。
	 * @param value true表示启用音乐，false表示禁用音乐。
	 */
	public static void music( boolean value ) {
	    Music.INSTANCE.enable( value );
	    put( KEY_MUSIC, value );
	}

	/**
	 * 获取当前音乐启用状态。
	 * @return 音乐是否启用的布尔值。
	 */
	public static boolean music() {
	    return getBoolean( KEY_MUSIC, true );
	}

	/**
	 * 设置音乐音量。
	 * @param value 音乐音量的百分比（0-100）。
	 */
	public static void musicVol( int value ){
	    Music.INSTANCE.volume(value*value/100f);
	    put( KEY_MUSIC_VOL, value );
	}

	/**
	 * 获取当前音乐音量。
	 * @return 当前音乐音量的百分比（0-100）。
	 */
	public static int musicVol(){
	    return getInt( KEY_MUSIC_VOL, 10, 0, 10 );
	}

	/**
	 * 控制音效是否启用。
	 * @param value true表示启用音效，false表示禁用音效。
	 */
	public static void soundFx( boolean value ) {
	    Sample.INSTANCE.enable( value );
	    put( KEY_SOUND_FX, value );
	}

	/**
	 * 获取当前音效启用状态。
	 * @return 音效是否启用的布尔值。
	 */
	public static boolean soundFx() {
	    return getBoolean( KEY_SOUND_FX, true );
	}

	/**
	 * 设置音效音量。
	 * @param value 音效音量的百分比（0-100）。
	 */
	public static void SFXVol( int value ) {
	    Sample.INSTANCE.volume(value*value/100f);
	    put( KEY_SFX_VOL, value );
	}

	/**
	 * 获取当前音效音量。
	 * @return 当前音效音量的百分比（0-100）。
	 */
	public static int SFXVol() {
	    return getInt( KEY_SFX_VOL, 10, 0, 10 );
	}

	/**
	 * 设置是否忽略静音模式。
	 * @param value true表示忽略设备的静音模式，false表示尊重设备的静音模式。
	 */
	public static void ignoreSilentMode( boolean value ){
	    put( KEY_IGNORE_SILENT, value);
	    Game.platform.setHonorSilentSwitch(!value);
	}

	public static boolean ignoreSilentMode(){
		return getBoolean( KEY_IGNORE_SILENT, false);
	}

	public static void playMusicInBackground( boolean value ){
		put( KEY_MUSIC_BG, value);
	}

	public static boolean playMusicInBackground(){
		return getBoolean( KEY_MUSIC_BG, true);
	}
	
	//Languages
	
	public static final String KEY_LANG         = "language";
	
	public static void language(Languages lang) {
		put( KEY_LANG, lang.code());
	}
	
	public static Languages language() {
		String code = getString(KEY_LANG, null);
		if (code == null){
			return Languages.matchLocale(Locale.getDefault());
		} else {
			return Languages.matchCode(code);
		}
	}

	//Window management (desktop only atm)
	
	public static final String KEY_WINDOW_WIDTH     = "window_width";
	public static final String KEY_WINDOW_HEIGHT    = "window_height";
	public static final String KEY_WINDOW_MAXIMIZED = "window_maximized";
	
	public static void windowResolution( Point p ){
		put(KEY_WINDOW_WIDTH, p.x);
		put(KEY_WINDOW_HEIGHT, p.y);
	}
	
	public static Point windowResolution(){
		return new Point(
				getInt( KEY_WINDOW_WIDTH, 800, 720, Integer.MAX_VALUE ),
				getInt( KEY_WINDOW_HEIGHT, 600, 400, Integer.MAX_VALUE )
		);
	}
	
	public static void windowMaximized( boolean value ){
		put( KEY_WINDOW_MAXIMIZED, value );
	}
	
	public static boolean windowMaximized(){
		return getBoolean( KEY_WINDOW_MAXIMIZED, false );
	}
}
