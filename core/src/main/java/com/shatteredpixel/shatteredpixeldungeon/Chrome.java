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

import com.watabou.noosa.NinePatch;

public class Chrome {

	/**
	 * 定义了界面元素的类型。
	 * 本枚举用于标识各种界面元素，如提示框、窗口、按钮、标签等，以支持不同的界面展示和交互方式。
	 */
	public enum Type {
	    TOAST,       // 提示框，用于显示短暂的提示信息。
	    TOAST_TR,    // 顶部提示框，常用于显示操作结果或指引信息。
	    TOAST_WHITE, // 白色提示框，用于与界面背景形成对比，突出显示信息。
	    WINDOW,      // 窗口，用于展示较为复杂的界面内容或进行操作。
	    WINDOW_SILVER, // 银色窗口，常用于展示特殊风格的界面或高级功能。
	    RED_BUTTON,  // 红色按钮，用于表示重要或危险的操作。
	    GREY_BUTTON, // 灰色按钮，用于表示普通或次级操作。
	    GREY_BUTTON_TR, // 顶部灰色按钮，常用于页面顶部的操作栏中。
	    TAG,         // 标签，用于对内容进行分类或标记。
	    GEM,         // 宝石，常用于表示成就或积分等。
	    SCROLL,      // 滚动条，用于控制内容的滚动。
	    TAB_SET,     // 选项卡组，用于展示多个相互关联的内容。
	    TAB_SELECTED, // 选中的选项卡，用于突出当前激活的内容。
	    TAB_UNSELECTED, // 未选中的选项卡，用于区分非激活的内容。
	    BLANK        // 空白类型，用于占位或作为默认值。
	}
	
	/**
	 * 根据类型获取NinePatch对象。
	 * NinePatch是一种图形对象，用于处理图片的拉伸和裁剪，以适应不同尺寸的需求。
	 * 此方法根据传入的类型，返回一个预定义的NinePatch对象，每个对象对应着不同的图形和拉伸模式。
	 *
	 * @param type 图形类型，决定返回哪个NinePatch对象。
	 * @return 对应类型的NinePatch对象，如果类型不被支持，则返回null。
	 */
	public static NinePatch get( Type type ) {
	    // 定义资产路径，这里固定为Chrome界面风格的资源。
	    String Asset = Assets.Interfaces.CHROME;
	    // 根据type的值，选择不同的资源切割方式创建NinePatch对象。
	    switch (type) {
	        case WINDOW:
	            // 窗口背景，左上、右上、左下、右下和中间的切线位置分别为0,0,20,20,6。
	            return new NinePatch( Asset, 0, 0, 20, 20, 6 );
	        case WINDOW_SILVER:
	            // 银色窗口背景，切割位置相应调整。
	            return new NinePatch( Asset, 86, 0, 22, 22, 7 );
	        case TOAST:
	            // 提示框背景，切割位置不同。
	            return new NinePatch( Asset, 20, 0, 9, 9, 4 );
	        case TOAST_TR:
	        case GREY_BUTTON_TR:
	            // 在某些情况下，切割位置会共享，如提示框右上角和灰色按钮右上角。
	            return new NinePatch( Asset, 20, 9, 9, 9, 4 );
	        case TOAST_WHITE:
	            // 白色提示框背景，切割位置又有所不同。
	            return new NinePatch( Asset, 29, 0, 9, 9, 4 );
	        case RED_BUTTON:
	            // 红色按钮，切割位置和大小进一步变化。
	            return new NinePatch( Asset, 38, 0, 6, 6, 2 );
	        case GREY_BUTTON:
	            // 灰色按钮，切割位置和红色按钮相同，但大小可能不同。
	            return new NinePatch( Asset, 38, 6, 6, 6, 2 );
	        case TAG:
	            // 标签，切割和拉伸模式更为复杂。
	            return new NinePatch( Asset, 22, 18, 16, 14, 3 );
	        case GEM:
	            // 宝石，需要完整的32x32区域进行拉伸和裁剪。
	            return new NinePatch( Asset, 0, 32, 32, 32, 13 );
	        case SCROLL:
	            // 滚动条，需要指定水平和垂直方向的拉伸区域。
	            return new NinePatch( Asset, 32, 32, 32, 32, 5, 11, 5, 11 );
	        case TAB_SET:
	            // 标签页背景，切割和拉伸模式类似于窗口背景。
	            return new NinePatch( Asset, 64, 0, 20, 20, 6 );
	        case TAB_SELECTED:
	            // 选中的标签页，需要更精细的控制拉伸区域。
	            return new NinePatch( Asset, 65, 22, 8, 13, 3, 7, 3, 5 );
	        case TAB_UNSELECTED:
	            // 未选中的标签页，和选中的标签页切割位置相同，但可能有不同的拉伸需求。
	            return new NinePatch( Asset, 75, 22, 8, 13, 3, 7, 3, 5 );
	        case BLANK:
	            // 空白区域，可能不需要任何拉伸和裁剪。
	            return new NinePatch( Asset, 45, 0, 1, 1, 0, 0, 0, 0 );
	        default:
	            // 如果传入的类型不被识别，则返回null。
	            return null;
	    }
	}
}
