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

package com.shatteredpixel.shatteredpixeldungeon.ui;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.watabou.gltextures.TextureCache;
import com.watabou.glwrap.Blending;
import com.watabou.noosa.Game;
import com.watabou.noosa.Image;
import com.watabou.noosa.NoosaScript;
import com.watabou.noosa.NoosaScriptNoLighting;
import com.watabou.noosa.SkinnedBlock;
import com.watabou.noosa.ui.Component;

public class Archs extends Component {

	private static final float SCROLL_SPEED	= 20f;

	private SkinnedBlock arcsBg;
	private SkinnedBlock arcsFg;
	private Image darkness;

	private static float offsB = 0;
	private static float offsF = 0;

	public boolean reversed = false;

		/**
	 * 创建子组件。此方法初始化背景弧形和前景弧形组件以及暗化效果组件。
	 * 弧形组件使用 SkinnedBlock 实现，支持皮肤和动画特性。暗化组件是一个用于创建背景渐变效果的 Image。
	 */
	@Override
	protected void createChildren() {
		/**
		 * 初始化背景弧形组件。使用特殊脚本禁用光照效果以优化性能。
		 * 绘制过程中禁用混合模式，以提升渲染效率，因为背景弧形没有透明度组件。
		 */
		arcsBg = new SkinnedBlock( 1, 1, Assets.Interfaces.ARCS_BG ) {
			/**
			 * 返回一个没有光照效果的脚本实例。
			 * @return NoosaScriptNoLighting 的实例。
			 */
			@Override
			protected NoosaScript script() {
				return NoosaScriptNoLighting.get();
			}

			/**
			 * 自定义绘制方法，禁用并重新启用混合模式。
			 */
			@Override
			public void draw() {
				// 背景弧形没有透明度组件，禁用混合模式可以提升性能
				Blending.disable();
				super.draw();
				Blending.enable();
			}
		};
		arcsBg.autoAdjust = true;
		arcsBg.offsetTo( 0,  offsB );
		add( arcsBg );

		/**
		 * 初始化前景弧形组件。同样使用没有光照效果的脚本。
		 */
		arcsFg = new SkinnedBlock( 1, 1, Assets.Interfaces.ARCS_FG ) {
			/**
			 * 返回一个没有光照效果的脚本实例。
			 * @return NoosaScriptNoLighting 的实例。
			 */
			@Override
			protected NoosaScript script() {
				return NoosaScriptNoLighting.get();
			}
		};
		arcsFg.autoAdjust = true;
		arcsFg.offsetTo( 0,  offsF );
		add( arcsFg );

		/**
		 * 创建暗化效果组件，使用预定义的颜色渐变创建一个渐变纹理。
		 * 设置角度为 90 度，以实现垂直方向的渐变效果。
		 */
		darkness= new Image(TextureCache.createGradient(0x00000000, 0x22000000, 0x55000000, 0x99000000, 0xEE000000));
		darkness.angle = 90;
		add(darkness);
	}


	/**
	 * 布局函数，用于调整界面元素的位置和大小。
	 * 本函数主要负责调整背景弧形、前景弧形和暗色遮罩的布局。
	 * 它们都是界面的重要组成部分，通过精确的布局，使得界面看起来更加美观和统一。
	 */
	@Override
	protected void layout() {
		// 调整背景弧形的大小以适应当前视图的宽度和高度。
		arcsBg.size( width, height );
		// 计算并设置背景弧形的水平偏移，以使其居中对齐。
		// 这里使用了弧形纹理宽度的四分之一作为参考点，然后根据视图宽度的余数进行微调。
		arcsBg.offset( arcsBg.texture.width / 4 - (width % arcsBg.texture.width) / 2, 0 );

		// 调整前景弧形的大小以适应当前视图的宽度和高度。
		// 前景弧形的布局调整与背景弧形类似，旨在保持界面元素的对称性和美观性。
		arcsFg.size( width, height );
		// 计算并设置前景弧形的水平偏移，以使其居中对齐。
		arcsFg.offset( arcsFg.texture.width / 4 - (width % arcsFg.texture.width) / 2, 0 );

		// 设置暗色遮罩的位置和大小。
		// 暗色遮罩位于界面的最下方，其宽度等于视图的宽度，高度根据视图的高度按比例缩放。
		// 这样做可以使得暗色遮罩在不同大小的视图中都能保持相对的比例，不会出现变形。
		darkness.x = width;
		darkness.scale.x = height/5f;
		darkness.scale.y = width;
	}


	/**
	 * 根据游戏的流逝时间更新背景和前景的弧度图形的位置。
	 * 该方法重写了父类的update方法，以实现特定的滚动效果。
	 * 它通过计算位移量并根据背景和前景的不同滚动速度来调整它们的位置。
	 */
	@Override
	public void update() {
	    // 调用父类的update方法，确保基础更新逻辑被执行。
	    super.update();

	    // 计算位移量，基于游戏的流逝时间和滚动速度。
	    float shift = Game.elapsed * SCROLL_SPEED;
	    // 如果背景滚动方向设置为反向，则位移量取反，实现反向滚动。
	    if (reversed) {
	        shift = -shift;
	    }

	    // 更新背景弧度图形的位置，沿Y轴偏移shift量。
	    arcsBg.offset(0, shift);
	    // 更新前景弧度图形的位置，沿Y轴偏移shift的两倍量，以创建深度感。
	    arcsFg.offset(0, shift * 2);

	    // 记录背景和前景的当前偏移量，用于后续可能的检查或计算。
	    offsB = arcsBg.offsetY();
	    offsF = arcsFg.offsetY();
	}
}
