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

public class Gizmo {
	
	public boolean exists;
	public boolean alive;
	public boolean active;
	public boolean visible;
	
	public Group parent;
	
	public Camera camera;
	
	public Gizmo() {
		exists	= true;
		alive	= true;
		active	= true;
		visible	= true;
	}
	
	public void destroy() {
		parent = null;
	}
	
	public void update() {
	}
	
	public void draw() {
	}
	
	/**
	 * 设置对象为死亡状态。
	 * 该方法将对象的 alive 和 exists 属性设置为 false，表示对象不再存活且不存在。
	 * 这是一种用于标记对象状态的逻辑操作，通常用于在游戏或模拟环境中表示实体的生命周期。
	 *
	 * @see #alive 是否表示对象处于存活状态的标志。
	 * @see #exists 是否表示对象在逻辑上存在的标志。
	 */
	public void kill() {
	    alive = false;
	    exists = false;
	}
	
	/**
 * 使对象复活，将存活状态和存在状态设置为真。
 * 此方法通常用于反向操作之前的“kill”操作，
 * 将对象恢复至可用状态。
 *
 * 本方法无参数。
 *
 * @return 无，此方法直接修改对象的状态。
 */
// 并非完全与"kill"方法相反
	public void revive() {
    	alive = true;
    	exists = true;
	}

	
	/**
	 * 获取相机对象。
	 * 如果当前对象有相机，则直接返回；如果没有，尝试从父对象获取相机。
	 * 如果父对象也不存在相机，则返回null。
	 *
	 * @return 当前对象或父对象的相机，如果都没有则返回null。
	 */
	public Camera camera() {
	    // 检查当前对象是否有相机
	    if (camera != null) {
	        return camera;
	    } else if (parent != null) {
	        // 如果当前对象没有相机，尝试从父对象获取
	        return this.camera = parent.camera();
	    } else {
	        // 如果没有父对象，说明无法获取相机
	        return null;
	    }
	}

	/**
	 * 判断当前对象是否可见。
	 * 如果当前对象有父对象，需要同时满足当前对象和父对象的可见性条件。
	 *
	 * @return 当前对象是否可见，如果存在父对象，则还需要考虑父对象的可见性。
	 */
	public boolean isVisible() {
	    // 检查当前对象是否有父对象
	    if (parent == null) {
	        // 如果没有父对象，直接根据当前对象的可见性返回
	        return visible;
	    } else {
	        // 如果有父对象，需要同时满足当前对象和父对象的可见性条件
	        return visible && parent.isVisible();
	    }
	}

	/**
	 * 判断当前对象是否活跃。
	 * 如果当前对象有父对象，需要同时满足当前对象和父对象的活跃性条件。
	 *
	 * @return 当前对象是否活跃，如果存在父对象，则还需要考虑父对象的活跃性。
	 */
	public boolean isActive() {
	    // 检查当前对象是否有父对象
	    if (parent == null) {
	        // 如果没有父对象，直接根据当前对象的活跃性返回
	        return active;
	    } else {
	        // 如果有父对象，需要同时满足当前对象和父对象的活跃性条件
	        return active && parent.isActive();
	    }
	}
	
	/**
	 * 销毁当前对象并删除其在父对象中的引用。
	 * 此方法首先调用 {@code kill} 方法来执行当前对象的销毁操作，然后检查当前对象是否有父对象。
	 * 如果存在父对象，则进一步调用父对象的 {@code erase} 方法，将当前对象从父对象的引用列表中删除。
	 * 这种设计模式常用于对象的清理工作，确保对象被彻底销毁，不会造成内存泄漏。
	 */
	public void killAndErase() {
	    kill();
	    // 如果当前对象有父对象，则尝试从父对象中删除当前对象的引用
	    if (parent != null) {
	        parent.erase( this );
	    }
	}
	
	/**
	 * 从其父级中移除当前对象。
	 *
	 * 此方法假设当前对象有一个父级，并且这个父级对象有一个remove方法可以用来移除子对象。
	 * 如果当前对象没有父级，则此方法不执行任何操作。
	 * 这种设计模式常见于树形结构中，例如文件系统或组织结构。
	 */
	public void remove() {
	    // 检查当前对象是否有父级
	    if (parent != null) {
	        // 如果有父级，则调用父级的remove方法移除当前对象
	        parent.remove( this );
	    }
	}
}
