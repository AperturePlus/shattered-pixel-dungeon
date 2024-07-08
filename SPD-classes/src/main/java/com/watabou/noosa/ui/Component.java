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

package com.watabou.noosa.ui;

import com.watabou.noosa.Group;

/**
 * 组件类，继承自Group，代表一个具有位置和大小的图形元素。
 */
public class Component extends Group {

	// 组件的左上角x坐标
	protected float x;
	// 组件的左上角y坐标
	protected float y;
	// 组件的宽度
	protected float width;
	// 组件的高度
	protected float height;

	/**
	 * 构造函数，初始化组件的位置和大小。
	 */
	public Component() {
		super();
		createChildren();
	}

	/**
	 * 设置组件的位置。
	 *
	 * @param x 组件的新x坐标
	 * @param y 组件的新y坐标
	 * @return 返回当前组件实例，允许链式调用
	 */
	public Component setPos( float x, float y ) {
		this.x = x;
		this.y = y;
		layout();

		return this;
	}

	/**
	 * 设置组件的大小。
	 *
	 * @param width 组件的新宽度
	 * @param height 组件的新高度
	 * @return 返回当前组件实例，允许链式调用
	 */
	public Component setSize( float width, float height ) {
		this.width = width;
		this.height = height;
		layout();

		return this;
	}

	/**
	 * 设置组件的位置和大小。
	 *
	 * @param x 组件的新x坐标
	 * @param y 组件的新y坐标
	 * @param width 组件的新宽度
	 * @param height 组件的新高度
	 * @return 返回当前组件实例，允许链式调用
	 */
	public Component setRect( float x, float y, float width, float height ) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		layout();

		return this;
	}

	/**
	 * 检查给定坐标是否在组件内部。
	 *
	 * @param x 检查的x坐标
	 * @param y 检查的y坐标
	 * @return 如果坐标在组件内部返回true，否则返回false
	 */
	public boolean inside( float x, float y ) {
		return x >= this.x && y >= this.y && x < this.x + width && y < this.y + height;
	}

	/**
	 * 将当前组件的形状和尺寸设置为与另一个组件相同。
	 *
	 * @param c 要匹配形状和尺寸的组件
	 */
	public void fill( Component c ) {
		setRect( c.x, c.y, c.width, c.height );
	}

	/**
	 * 获取组件的左边缘x坐标。
	 *
	 * @return 组件的左边缘x坐标
	 */
	public float left() {
		return x;
	}

	/**
	 * 获取组件的右边缘x坐标。
	 *
	 * @return 组件的右边缘x坐标
	 */
	public float right() {
		return x + width;
	}

	/**
	 * 获取组件的中心x坐标。
	 *
	 * @return 组件的中心x坐标
	 */
	public float centerX() {
		return x + width / 2;
	}

	/**
	 * 获取组件的上边缘y坐标。
	 *
	 * @return 组件的上边缘y坐标
	 */
	public float top() {
		return y;
	}

	/**
	 * 获取组件的下边缘y坐标。
	 *
	 * @return 组件的下边缘y坐标
	 */
	public float bottom() {
		return y + height;
	}

	/**
	 * 获取组件的中心y坐标。
	 *
	 * @return 组件的中心y坐标
	 */
	public float centerY() {
		return y + height / 2;
	}

	/**
	 * 获取组件的宽度。
	 *
	 * @return 组件的宽度
	 */
	public float width() {
		return width;
	}

	/**
	 * 获取组件的高度。
	 *
	 * @return 组件的高度
	 */
	public float height() {
		return height;
	}

	/**
	 * 创建组件的子元素。这个方法是抽象的，需要在子类中实现。
	 */
	protected void createChildren() {
	}

	/**
	 * 布局组件及其子元素。这个方法是抽象的，需要在子类中实现。
	 */
	protected void layout() {
	}
}

