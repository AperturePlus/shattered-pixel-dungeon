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

import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.watabou.input.ControllerHandler;
import com.watabou.input.GameAction;
import com.watabou.input.KeyBindings;
import com.watabou.input.KeyEvent;
import com.watabou.input.PointerEvent;
import com.watabou.noosa.Camera;
import com.watabou.noosa.Game;
import com.watabou.noosa.PointerArea;
import com.watabou.noosa.ui.Component;
import com.watabou.utils.Signal;

public class Button extends Component {

	public static float longClick = 0.5f;
	
	protected PointerArea hotArea;
	protected Tooltip hoverTip;

	//only one button should be pressed at a time
	protected static Button pressedButton;
	protected float pressTime;
	protected boolean clickReady;

	/**
	 * 创建按钮的子元素，主要包括热区和键盘监听器。
	 * 热区用于处理鼠标事件，如按下、释放和点击，以及悬停开始和结束。
	 * 键盘监听器用于处理与按钮关联的键盘事件。
	 */
	@Override
	protected void createChildren() {
		// 创建一个热区，用于识别按钮区域内的鼠标事件。
		hotArea = new PointerArea( 0, 0, 0, 0 ) {
			/**
			 * 当鼠标按下时触发。
			 * 设置当前按下的按钮为this按钮，重置按压时间和准备点击状态。
			 * 调用Button.this的onPointerDown方法，传递鼠标事件。
			 */
			@Override
			protected void onPointerDown( PointerEvent event ) {
				pressedButton = Button.this;
				pressTime = 0;
				clickReady = true;
				Button.this.onPointerDown();
			}
			/**
			 * 当鼠标释放时触发。
			 * 如果释放时的按钮与按下时的按钮相同，则重置按下的按钮状态。
			 * 否则，取消可能的点击动作。
			 * 调用Button.this的onPointerUp方法，传递鼠标事件。
			 */
			@Override
			protected void onPointerUp( PointerEvent event ) {
				if (pressedButton == Button.this){
					pressedButton = null;
				} else {
					//cancel any potential click, only one button can be activated at a time
					clickReady = false;
				}
				Button.this.onPointerUp();
			}
			/**
			 * 当鼠标点击时触发。
			 * 如果准备点击状态为true，则根据点击的鼠标按钮执行相应的点击动作。
			 * 包括常规点击、右键点击和中键点击。
			 */
			@Override
			protected void onClick( PointerEvent event ) {
				if (clickReady) {
					killTooltip();
					switch (event.button){
						case PointerEvent.LEFT: default:
							Button.this.onClick();
							break;
						case PointerEvent.RIGHT:
							Button.this.onRightClick();
							break;
						case PointerEvent.MIDDLE:
							Button.this.onMiddleClick();
							break;
					}

				}
			}

			/**
			 * 当鼠标悬停开始时触发。
			 * 根据按钮的hoverText和关联的键盘绑定，生成并显示工具提示。
			 */
			@Override
			protected void onHoverStart(PointerEvent event) {
				String text = hoverText();
				if (text != null){
					int key = 0;
					if (keyAction() != null){
						key = KeyBindings.getFirstKeyForAction(keyAction(), ControllerHandler.controllerActive);
					}

					if (key == 0 && secondaryTooltipAction() != null){
						key = KeyBindings.getFirstKeyForAction(secondaryTooltipAction(), ControllerHandler.controllerActive);
					}

					if (key != 0){
						text += " _(" + KeyBindings.getKeyName(key) + ")_";
					}
					hoverTip = new Tooltip(Button.this, text, 80);
					Button.this.parent.addToFront(hoverTip);
					hoverTip.camera = camera();
					alignTooltip(hoverTip);
				}
			}
			/**
			 * 当鼠标悬停结束时触发。
			 * 销毁当前的工具提示。
			 */
			@Override
			protected void onHoverEnd(PointerEvent event) {
				killTooltip();
			}
		};
		add( hotArea );

		// 添加键盘监听器，用于处理与按钮关联的键盘事件。
		KeyEvent.addKeyListener( keyListener = new Signal.Listener<KeyEvent>() {
			/**
			 * 当键盘事件触发时检查是否与按钮相关联。
			 * 如果按钮处于激活状态，且事件对应的键盘动作与按钮的动作匹配，
			 * 则根据键盘事件的状态执行按下或释放操作，并可能触发点击动作。
			 */
			@Override
			public boolean onSignal ( KeyEvent event ) {
				if ( active && KeyBindings.getActionForKey( event ) == keyAction()){
					if (event.pressed){
						pressedButton = Button.this;
						pressTime = 0;
						clickReady = true;
						Button.this.onPointerDown();
					} else {
						Button.this.onPointerUp();
						if (pressedButton == Button.this) {
							pressedButton = null;
							if (clickReady) onClick();
						}
					}
					return true;
				}
				return false;
			}
		});
	}

	
	private Signal.Listener<KeyEvent> keyListener;
	
	public GameAction keyAction(){
		return null;
	}

	//used in cases where the main key action isn't bound, but a secondary action can be used for the tooltip
	public GameAction secondaryTooltipAction(){
		return null;
	}

	@Override
	public void update() {
		super.update();
		
		hotArea.active = visible;
		
		if (pressedButton == this && (pressTime += Game.elapsed) >= longClick) {
			pressedButton = null;
			if (onLongClick()) {

				hotArea.reset();
				clickReady = false; //did a long click, can't do a regular one
				onPointerUp();

				if (SPDSettings.vibration()) {
					Game.vibrate(50);
				}
			}
		}
	}
	
	protected void onPointerDown() {}
	protected void onPointerUp() {}
	protected void onClick() {} //left click, default key type
	protected void onRightClick() {}
	protected void onMiddleClick() {}
	protected boolean onLongClick() {
		return false;
	}

	protected String hoverText() {
		return null;
	}

	//TODO might be nice for more flexibility here
	private void alignTooltip( Tooltip tip ){
		tip.setPos(x, y-tip.height()-1);
		Camera cam = camera();
		//shift left if there's no room on the right
		if (tip.right() > (cam.width+cam.scroll.x)){
			tip.setPos(tip.left() - (tip.right() - (cam.width+cam.scroll.x)), tip.top());
		}
		//move to the bottom if there's no room on top
		if (tip.top() < 0){
			tip.setPos(tip.left(), bottom()+1);
		}
	}

	public void killTooltip(){
		if (hoverTip != null){
			hoverTip.killAndErase();
			hoverTip = null;
		}
	}
	
	@Override
	protected void layout() {
		hotArea.x = x;
		hotArea.y = y;
		hotArea.width = width;
		hotArea.height = height;
	}
	
	@Override
	public synchronized void destroy () {
		super.destroy();
		KeyEvent.removeKeyListener( keyListener );
		killTooltip();
	}

	public void givePointerPriority(){
		hotArea.givePointerPriority();
	}
	
}
