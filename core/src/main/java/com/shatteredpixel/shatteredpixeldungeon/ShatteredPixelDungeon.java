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

import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.TitleScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.WelcomeScene;
import com.watabou.noosa.Game;
import com.watabou.noosa.audio.Music;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.DeviceCompat;
import com.watabou.utils.PlatformSupport;

public class ShatteredPixelDungeon extends Game {

	//variable constants for specific older versions of shattered, used for data conversion
	public static final int v1_2_3 = 628; //v1.2.3 is kept for now, for old rankings score logic

	//savegames from versions older than v1.4.3 are no longer supported, and data from them is ignored
	public static final int v1_4_3 = 668;

	public static final int v2_0_2 = 700;
	public static final int v2_1_4 = 737; //iOS was 737, other platforms were 736
	public static final int v2_2_1 = 755; //iOS was 755 (also called v2.2.2), other platforms were 754
	public static final int v2_3_2 = 768;
	public static final int v2_4_0 = 780;
	
	/**
	 * ShatteredPixelDungeon类的构造函数。
	 * 该构造函数初始化游戏场景，并为兼容旧版本设置了物品和房间的别名。
	 *
	 * @param platform 平台支持对象，用于提供与平台相关的功能。
	 */
	public ShatteredPixelDungeon( PlatformSupport platform ) {
	    // 调用超类构造函数，初始化场景和平台。
	    super( sceneClass == null ? WelcomeScene.class : sceneClass, platform );

	    // 为旧版本的UnstableBrew设置别名，以保持兼容性
	    //pre-v2.4.0
	    com.watabou.utils.Bundle.addAlias(
	            com.shatteredpixel.shatteredpixeldungeon.items.potions.brews.UnstableBrew.class,
	            "com.shatteredpixel.shatteredpixeldungeon.items.potions.AlchemicalCatalyst" );
	    // 为旧版本的UnstableSpell设置别名
	    com.watabou.utils.Bundle.addAlias(
	            com.shatteredpixel.shatteredpixeldungeon.items.spells.UnstableSpell.class,
	            "com.shatteredpixel.shatteredpixeldungeon.items.spells.ArcaneCatalyst" );
	    // 为旧版本的ElixirOfFeatherFall设置别名
	    com.watabou.utils.Bundle.addAlias(
	            com.shatteredpixel.shatteredpixeldungeon.items.potions.elixirs.ElixirOfFeatherFall.class,
	            "com.shatteredpixel.shatteredpixeldungeon.items.spells.FeatherFall" );
	    // 为旧版本的ElixirOfFeatherFall的Buff效果设置别名
	    com.watabou.utils.Bundle.addAlias(
	            com.shatteredpixel.shatteredpixeldungeon.items.potions.elixirs.ElixirOfFeatherFall.FeatherBuff.class,
	            "com.shatteredpixel.shatteredpixeldungeon.items.spells.FeatherFall$FeatherBuff" );
	    // 为旧版本的AquaBrew设置别名
	    com.watabou.utils.Bundle.addAlias(
	            com.shatteredpixel.shatteredpixeldungeon.items.potions.brews.AquaBrew.class,
	            "com.shatteredpixel.shatteredpixeldungeon.items.spells.AquaBlast" );

	    // 为旧版本的EntranceRoom和ExitRoom设置别名，以保持兼容性
	    com.watabou.utils.Bundle.addAlias(
	            com.shatteredpixel.shatteredpixeldungeon.levels.rooms.standard.entrance.EntranceRoom.class,
	            "com.shatteredpixel.shatteredpixeldungeon.levels.rooms.standard.EntranceRoom" );
	    com.watabou.utils.Bundle.addAlias(
	            com.shatteredpixel.shatteredpixeldungeon.levels.rooms.standard.exit.ExitRoom.class,
	            "com.shatteredpixel.shatteredpixeldungeon.levels.rooms.standard.ExitRoom" );

	    // 为旧版本的ConjuredBomb设置别名
	    //pre-v2.3.0
	    com.watabou.utils.Bundle.addAlias(
	            com.shatteredpixel.shatteredpixeldungeon.items.bombs.Bomb.ConjuredBomb.class,
	            "com.shatteredpixel.shatteredpixeldungeon.items.bombs.Bomb$MagicalBomb" );

	    // 为旧版本的BlacksmithRoom、MassGraveRoom、RitualSiteRoom和RotGardenRoom设置别名，以保持兼容性
	    //pre-v2.2.0
	    com.watabou.utils.Bundle.addAlias(
	            com.shatteredpixel.shatteredpixeldungeon.levels.rooms.quest.BlacksmithRoom.QuestEntrance.class,
	            "com.shatteredpixel.shatteredpixeldungeon.levels.rooms.standard.BlacksmithRoom$QuestEntrance" );
	    com.watabou.utils.Bundle.addAlias(
	            com.shatteredpixel.shatteredpixeldungeon.levels.rooms.quest.BlacksmithRoom.class,
	            "com.shatteredpixel.shatteredpixeldungeon.levels.rooms.standard.BlacksmithRoom" );
	    com.watabou.utils.Bundle.addAlias(
	            com.shatteredpixel.shatteredpixeldungeon.levels.rooms.quest.MassGraveRoom.class,
	            "com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special.MassGraveRoom" );
	    com.watabou.utils.Bundle.addAlias(
	            com.shatteredpixel.shatteredpixeldungeon.levels.rooms.quest.MassGraveRoom.Bones.class,
	            "com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special.MassGraveRoom$Bones" );
	    com.watabou.utils.Bundle.addAlias(
	            com.shatteredpixel.shatteredpixeldungeon.levels.rooms.quest.RitualSiteRoom.class,
	            "com.shatteredpixel.shatteredpixeldungeon.levels.rooms.standard.RitualSiteRoom" );
	    com.watabou.utils.Bundle.addAlias(
	            com.shatteredpixel.shatteredpixeldungeon.levels.rooms.quest.RitualSiteRoom.RitualMarker.class,
	            "com.shatteredpixel.shatteredpixeldungeon.levels.rooms.standard.RitualSiteRoom$RitualMarker" );
	    com.watabou.utils.Bundle.addAlias(
	            com.shatteredpixel.shatteredpixeldungeon.levels.rooms.quest.RotGardenRoom.class,
	            "com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special.RotGardenRoom" );
	}
	
	/**
	 * 初始化资源，创建游戏对象。
	 * 此方法在游戏启动时调用，用于设置游戏的用户界面、加载音效和音乐。
	 * 它首先调用超类的创建方法，然后更新系统用户界面，加载绑定设置。
	 * 接着，根据设置启用和调整音乐及音效的音量，并加载所有音效。
	 */
	@Override
	public void create() {
		super.create(); // 调用超类的create方法，进行基础的初始化

		updateSystemUI(); // 更新游戏的系统用户界面，以适应当前设备或用户设置
		SPDAction.loadBindings(); // 加载用户绑定的操作设置，提供自定义的游戏控制

		// 根据设置启用或禁用音乐，并设置音乐音量
		Music.INSTANCE.enable(SPDSettings.music());
		Music.INSTANCE.volume(SPDSettings.musicVol() * SPDSettings.musicVol() / 100f);

		// 根据设置启用或禁用音效，并设置音效音量
		Sample.INSTANCE.enable(SPDSettings.soundFx());
		Sample.INSTANCE.volume(SPDSettings.SFXVol() * SPDSettings.SFXVol() / 100f);

		// 加载所有音效资源，以便在游戏中使用
		Sample.INSTANCE.load(Assets.Sounds.all);
	}


	    /**
     * 结束当前活动（Activity）的方法。
     * 根据平台不同，此方法会定制结束活动的行为。
     * 对于非iOS平台，调用父类的finish方法来正常结束活动。
     * 对于iOS平台，由于苹果的指导原则限制直接退出，
     * 因此不会真正结束应用，而是切换到标题屏幕。
     */
    @Override
    public void finish() {
        if (!DeviceCompat.isiOS()) {
            super.finish();
        } else {
            // 不允许在iOS上退出应用，因此切换至标题界面
            switchScene(TitleScene.class);
        }
    }


	public static void switchNoFade(Class<? extends PixelScene> c){
		switchNoFade(c, null);
	}

	/**
	 * 切换场景而不使用淡入淡出效果。
	 *
	 * 此方法用于在不同的场景之间进行切换，但与通常的切换方法不同的是，它不使用淡入淡出效果。
	 * 这种切换方式可能适用于某些场景过渡时不需要平滑过渡效果的情况，比如游戏加载界面或菜单界面的切换。
	 *
	 * @param c 要切换到的场景的类类型。必须继承自PixelScene。
	 * @param callback 场景切换后的回调函数。用于在场景切换完成后执行特定的逻辑。
	 */
	public static void switchNoFade(Class<? extends PixelScene> c, SceneChangeCallback callback) {
	    // 禁用场景切换时的淡入淡出效果
	    PixelScene.noFade = true;
	    // 执行场景切换，参数包括目标场景的类类型和切换后的回调函数
	    switchScene(c, callback);
	}
	
	/**
	 * 无缝重置场景方法。
	 * 该方法用于在不产生闪烁的情况下重置当前场景。如果当前场景是像素场景，则采用特定的方式进行重置；
	 * 否则，使用一般方式重置场景。
	 *
	 * @param callback 场景变更回调接口。用于在场景重置后执行特定的操作或更新。
	 */
	public static void seamlessResetScene(SceneChangeCallback callback) {
	    // 检查当前场景是否为像素场景
	    if (scene() instanceof PixelScene) {
	        // 对像素场景进行特殊处理，先保存窗口状态
	        ((PixelScene) scene()).saveWindows();
	        // 切换到新的像素场景，并传入回调函数
	        switchNoFade((Class<? extends PixelScene>) sceneClass, callback);
	    } else {
	        // 对于非像素场景，使用普通方式重置场景
	        resetScene();
	    }
	}
	
	/**
	 * 无缝重置场景的方法。
	 * 通过调用重载的seamlessResetScene方法，其中传递一个null参数来实现。
	 * 此方法的存在提供了一种简化的重置场景的途径，不需要显式传递参数。
	 */
	public static void seamlessResetScene(){
	    seamlessResetScene(null);
	}
	
	/**
	 * 切换场景的函数。
	 * 本函数在调用父类的场景切换方法后，会对当前场景进行额外的处理。
	 * 如果当前场景是PixelScene的实例，将恢复这个场景中的窗口设置。
	 * 这样的设计允许对特定类型的场景进行定制化的处理，增强了场景切换的灵活性。
	 */
	@Override
	protected void switchScene() {
	    // 调用父类的switchScene方法，完成基本的场景切换操作
	    super.switchScene();

	    // 检查当前场景是否是PixelScene的实例
	    if (scene instanceof PixelScene){
	        // 如果是，将其窗口设置恢复到之前的状态
	        ((PixelScene) scene).restoreWindows();
	    }
	}
	
	/**
	 * 根据指定的宽度和高度调整窗口大小。
	 *
	 * 此方法重写了父类的resize方法，以实现更具体的窗口调整逻辑。
	 * 它首先检查宽度和高度是否为0，如果是，则不进行任何操作直接返回。
	 * 接着，它检查当前场景是否为PixelScene实例，并且新的宽度和高度是否与游戏的
	 * 默认宽度和高度不同。如果是，它将设置PixelScene的noFade标志为true，并调用
	 * saveWindows方法保存当前窗口设置。
	 * 最后，它调用超类的resize方法来实际调整窗口大小，并通过updateDisplaySize方法
	 * 更新显示尺寸。
	 *
	 * @param width 新窗口的宽度。
	 * @param height 新窗口的高度。
	 */
	@Override
	public void resize( int width, int height ) {
	    // 检查宽度或高度是否为0，如果是，则不进行任何操作
	    if (width == 0 || height == 0){
	        return;
	    }

	    // 如果当前场景是PixelScene实例，并且新的尺寸不同于游戏的默认尺寸
	    if (scene instanceof PixelScene &&
	            (height != Game.height || width != Game.width)) {
	        // 禁止淡入淡出效果
	        PixelScene.noFade = true;
	        // 保存当前窗口设置
	        ((PixelScene) scene).saveWindows();
	    }

	    // 调用超类的resize方法来实际调整窗口大小
	    super.resize( width, height );

	    // 更新显示尺寸
	    updateDisplaySize();

	}
	
	/**
	 * 销毁当前对象。
	 * 该方法重写了父类的destroy方法，以确保在对象销毁时，特定的线程能够被正确结束。
	 * 这是清理资源和避免内存泄漏的重要步骤。
	 *
	 * @see #destroy()
	 */
	@Override
	public void destroy(){
	    super.destroy(); // 调用父类的destroy方法，进行必要的销毁操作。
	    GameScene.endActorThread(); // 结束游戏场景中的演员线程，确保线程资源被正确回收。
	}
	
	public void updateDisplaySize(){
		platform.updateDisplaySize();
	}

	public static void updateSystemUI() {
		platform.updateSystemUI();
	}
}