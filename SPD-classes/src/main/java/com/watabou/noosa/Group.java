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

import com.watabou.utils.Random;
import com.watabou.utils.Reflection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class Group extends Gizmo {

	protected ArrayList<Gizmo> members;

	// Accessing it is a little faster,
	// than calling members.getSize()
	public int length;

	public Group() {
		members = new ArrayList<>();
		length = 0;
	}

	/**
	 * 销毁当前 GizmoManager 实例，并清理所有成员 Gizmo 对象。
	 * 此方法是同步的，确保在多线程环境下安全地销毁实例。
	 */
	@Override
	public synchronized void destroy() {
	    // 调用父类的 destroy 方法，进行必要的清理工作。
	    super.destroy();

	    // 遍历所有成员 Gizmo 对象，并逐一销毁它们。
	    for (int i = 0; i < length; i++) {
	        Gizmo g = members.get(i);
	        if (g != null) {
	            g.destroy();
	        }
	    }

	    // 清空成员 Gizmo 集合，然后将其置为 null，以释放资源。
	    if (members != null) {
	        members.clear();
	        members = null;
	    }

	    // 将成员数量重置为 0，表示已销毁所有成员。
	    length = 0;
	}

	/**
	 * 同步更新所有存在的且激活的 Gizmo 实例。
	 * 此方法通过遍历成员列表，对每个有效的 Gizmo 实例调用其 update 方法。
	 * 使用 synchronized 关键字确保更新过程的线程安全。
	 */
	@Override
	public synchronized void update() {
	    /* 遍历 Gizmo 成员数组 */
	    for (int i = 0; i < length; i++) {
	        /* 获取当前遍历到的 Gizmo 实例 */
	        Gizmo g = members.get(i);
	        /* 检查 Gizmo 实例是否存在、有效且处于激活状态 */
	        if (g != null && g.exists && g.active) {
	            /* 更新 Gizmo 实例 */
	            g.update();
	        }
	    }
	}

	/**
	 * 同步绘制所有成员小部件。
	 * 此方法通过遍历小部件集合，调用每个存在且可见的小部件的绘制方法来实现。
	 * 使用同步关键字确保在多线程环境下对小部件集合的访问是安全的。
	 */
	@Override
	public synchronized void draw() {
	    /* 遍历小部件集合 */
	    for (int i=0; i < length; i++) {
	        /* 获取当前索引对应的小部件 */
	        Gizmo g = members.get( i );
	        /* 检查小部件是否非空、存在且可见，满足条件则调用其绘制方法 */
	        if (g != null && g.exists && g.isVisible()) {
	            g.draw();
	        }
	    }
	}

	@Override
	public synchronized void kill() {
		// A killed group keeps all its members,
		// but they get killed too
		for (int i=0; i < length; i++) {
			Gizmo g = members.get( i );
			if (g != null && g.exists) {
				g.kill();
			}
		}

		super.kill();
	}

	/**
	 * 在 Gizmo 集合中同步查找指定的 Gizmo 对象。
	 *
	 * @param g 要查找的 Gizmo 对象。
	 * @return 如果找到，返回 Gizmo 对象在集合中的索引；否则返回 -1。
	 * @ synchronized 确保多个线程安全访问此方法。
	 */
	public synchronized int indexOf( Gizmo g ) {
	    // 使用成员变量 members 的 indexOf 方法来查找 g 的位置
	    return members.indexOf( g );
	}

	/**
 * 同步地将一个Gizmo对象添加到当前集合中。
 * 若待添加的Gizmo已存在父级且该父级不是当前集合，将首先从其原父级移除该Gizmo，然后将其添加至本集合。
 * 由于使用了synchronized关键字，此方法确保线程安全，并保证一个Gizmo对象在同一时间只能属于一个集合。
 *
 * @param g 待添加的Gizmo对象
 * @return 已添加的Gizmo对象
 */
public synchronized Gizmo add(Gizmo g) {

    // 检查待添加的Gizmo是否已属于当前集合
    if (g.parent == this) {
        return g;
    }

    // 若待添加的Gizmo已有父级，先从原父级移除
    if (g.parent != null) {
        g.parent.remove(g);
    }

    // 尝试在现有集合中找到空闲位置以添加新的Gizmo
    for (int i = 0; i < length; i++) {
        if (members.get(i) == null) {
            members.set(i, g);
            g.parent = this;
            return g;
        }
    }

    // 若未找到空闲位置，将新的Gizmo添加到集合末尾
    members.add(g);
    g.parent = this;
    length++;
    return g;
}


	/**
 * 同步方法，将Gizmo对象添加到集合的前端。
 * 如果要添加的对象已具有父级且该父级是此集合，则直接返回该对象。
 * 如果要添加的对象已具有父级，则首先从其当前父级中移除该对象，然后将其添加到此集合中。
 *
 * @param g 要添加的Gizmo对象
 * @return 已添加的Gizmo对象
 */
public synchronized Gizmo addToFront(Gizmo g) {

    // 检查g对象的父级是否已经是此集合，如果是，则直接返回g
    if (g.parent == this) {
        return g;
    }

    // 如果g有父级，先从其当前父级中移除g
    if (g.parent != null) {
        g.parent.remove(g);
    }

    // 从集合的末尾向前遍历，寻找第一个空位置来添加g
    // 尝试为新成员找到一个空位
    // 从集合的前端开始，不会越过非空元素
    for (int i = length - 1; i >= 0; i--) {
        // 如果当前位置为空
        if (members.get(i) == null) {
            // 如果这是第一个位置或前一个位置不为空，则在当前位置添加g
            if (i == 0 || members.get(i - 1) != null) {
                members.set(i, g);
                g.parent = this;
                return g;
            }
        } else {
            // 如果当前位置不为空，停止查找
            break;
        }
    }

    // 如果遍历完集合没有找到空位，则在集合末尾添加g
    members.add(g);
    g.parent = this;
    length++;
    return g;
}


	/**
	 * 将一个Gizmo对象添加到集合的最前面，并处理相关的父子关系。
	 * 如果g已经属于这个集合，将其移动到最前面。
	 * 如果g有其他父集合，将其从原父集合移除后添加到这个集合。
	 *
	 * @param g 要添加到集合的Gizmo对象。
	 * @return 添加或移动后的Gizmo对象。
	 */
	public synchronized Gizmo addToBack( Gizmo g ) {
	    // 检查g是否已经属于这个集合，如果是，则将其移动到最前面
	    if (g.parent == this) {
	        sendToBack( g );
	        return g;
	    }

	    // 如果g有其他父集合，将其从原父集合移除
	    if (g.parent != null) {
	        g.parent.remove( g );
	    }

	    // 如果集合不为空，且第一个元素为null，将g作为第一个元素
	    if (!members.isEmpty() && members.get( 0 ) == null) {
	        members.set( 0, g );
	        g.parent = this;
	        return g;
	    }

	    // 将g添加到集合的最前面，并更新父子关系
	    members.add( 0, g );
	    g.parent = this;
	    length++;
	    return g;
	}

	/**
	 * 回收或创建一个新的Gizmo实例。
	 *
	 * 本方法尝试从可用池中获取一个指定类别的Gizmo实例。如果存在可用实例，则返回它；
	 * 如果没有可用实例且类参数不为null，则尝试使用反射创建一个新的Gizmo实例并添加到池中。
	 *
	 * @param c Gizmo的类，用于指定需要的Gizmo类型。如果为null，则方法直接返回null。
	 * @return 一个可用的Gizmo实例，或者null（如果没有可用实例且类参数为null）。
	 */
	public synchronized Gizmo recycle(Class<? extends Gizmo> c) {
	    // 尝试从池中获取一个可用的Gizmo实例
	    Gizmo g = getFirstAvailable(c);
	    if (g != null) {
	        // 如果存在可用实例，直接返回
	        return g;
	    } else if (c == null) {
	        // 如果类参数为null，直接返回null
	        return null;
	    } else {
	        // 尝试使用反射创建一个新的Gizmo实例
	        g = Reflection.newInstance(c);
	        if (g != null) {
	            // 如果实例创建成功，将其添加到池中并返回
	            return add(g);
	        }
	    }
	    // 如果无法获取或创建新的Gizmo实例，返回null
	    return null;
	}

	    /**
     * 快速移除指定的Gizmo对象，通过将其替换为null实现。
     *
     * 如果Gizmo对象存在于集合中，它在集合中的位置会被置为null以标记移除，
     * 同时该对象的父级引用也会被设为null，然后返回该对象。
     * 如果Gizmo对象不存在于集合中，则返回null。
     *
     * @param g 要移除的Gizmo对象。
     * @return 如果存在则返回移除的Gizmo对象，否则返回null。
     */
    // 快速移除 - 替换为null
    public synchronized Gizmo erase( Gizmo g ) {
        int index = members.indexOf( g );

        if (index != -1) {
            members.set( index, null );
            g.parent = null;
            return g;
        } else {
            return null;
        }
    }


	    /**
     * 实现Gizmo对象的真实移除操作。
     * 此方法通过同步确保在处理成员集合及更新长度属性时的线程安全。
     *
     * @param g 待移除的Gizmo对象。该对象不应为null，并且应该是集合中已存在的对象。
     * @return 如果找到并成功移除了指定的Gizmo对象，则返回该对象；否则，返回null。
     */
    // 实现真实移除
    public synchronized Gizmo remove( Gizmo g ) {
        // 尝试从集合中移除指定的Gizmo对象
        if (members.remove( g )) {
            // 如果移除成功，递减长度计数器
            length--;
            // 设置Gizmo对象的父引用为空
            g.parent = null;
            // 返回移除的Gizmo对象
            return g;
        } else {
            // 如果未找到或移除失败，返回null
            return null;
        }
    }


	/**
	 * 替换集合中的一个Gizmo对象为另一个新Gizmo对象。
	 * 此方法确保线程安全地进行替换操作，并更新对象的parent引用。
	 *
	 * @param oldOne 要被替换的旧Gizmo对象。
	 * @param newOne 用于替换旧对象的新Gizmo对象。
	 * @return 如果成功替换，则返回新Gizmo对象；如果旧对象不存在于集合中，则返回null。
	 */
	public synchronized Gizmo replace( Gizmo oldOne, Gizmo newOne ) {
	    // 查找旧Gizmo对象在成员集合中的索引。
	    int index = members.indexOf( oldOne );
	    // 如果旧Gizmo对象存在于集合中。
	    if (index != -1) {
	        // 使用新Gizmo对象替换旧对象。
	        members.set( index, newOne );
	        // 更新新Gizmo对象的parent引用为当前集合对象。
	        newOne.parent = this;
	        // 清除旧Gizmo对象的parent引用，表示其不再属于当前集合。
	        oldOne.parent = null;
	        // 返回替换后的新Gizmo对象。
	        return newOne;
	    } else {
	        // 如果旧Gizmo对象不存在于集合中，返回null。
	        return null;
	    }
	}

	/**
	 * 获取第一个可用的Gizmo实例。
	 * 本方法同步执行，确保了在多线程环境下的安全性，可以避免并发访问导致的数据不一致问题。
	 *
	 * @param c Gizmo的子类类对象，用于指定需要的Gizmo类型。如果为null，则表示任何类型均可。
	 * @return 返回第一个可用的Gizmo实例，如果不存在符合条件的实例则返回null。
	 */
	public synchronized Gizmo getFirstAvailable(Class<? extends Gizmo> c) {
	    // 遍历成员列表，寻找第一个可用的Gizmo实例
	    for (int i = 0; i < length; i++) {
	        Gizmo g = members.get(i);
	        // 检查Gizmo实例是否为空，且是否不存在（即可用），并满足指定的类类型（如果有的话）
	        if (g != null && !g.exists && ((c == null) || g.getClass() == c)) {
	            return g; // 如果条件满足，立即返回该实例
	        }
	    }
	    // 如果遍历完成后没有找到符合条件的实例，返回null
	    return null;
	}

	/**
	 * 同步方法，用于计算当前活着的Gizmo对象的数量。
	 * 此方法加锁以确保线程安全，因为在并发环境下可能有多个线程尝试访问和修改成员变量。
	 *
	 * @return 返回活着的Gizmo对象的数量。
	 */
	public synchronized int countLiving() {
	    /* 初始化计数器为0，用于累计活着的Gizmo对象 */
	    int count = 0;

	    /* 遍历Gizmo数组，检查每个对象是否存活 */
	    for (int i=0; i < length; i++) {
	        /* 从数组中获取当前Gizmo对象 */
	        Gizmo g = members.get( i );
	        /* 检查对象是否非空，存在，且存活 */
	        if (g != null && g.exists && g.alive) {
	            /* 如果条件满足，增加计数器 */
	            count++;
	        }
	    }

	    /* 返回活着的Gizmo对象的数量 */
	    return count;
	}

	/**
	 * 计算当前成员中死亡的数量。
	 *
	 * 本方法通过遍历成员列表，检查每个成员的存活状态来计算死亡成员的数量。
	 * 使用synchronized关键字确保了线程安全，这意味着在多线程环境下，
	 * 对于共享资源countDead的调用将会被有序地序列化执行，避免了并发访问带来的问题。
	 *
	 * @return int 返回死亡成员的数量。
	 */
	public synchronized int countDead() {
	    /* 初始化死亡成员计数器 */
	    int count = 0;

	    /* 遍历所有成员 */
	    for (int i=0; i < length; i++) {
	        /* 获取当前成员 */
	        Gizmo g = members.get(i);
	        /* 检查成员是否死亡（非空且存活状态为false） */
	        if (g != null && !g.alive) {
	            /* 如果是，增加死亡成员计数 */
	            count++;
	        }
	    }

	    /* 返回死亡成员的总数 */
	    return count;
	}

	/**
	 * 从 Gizmo 对象集合中随机获取一个对象。
	 * 此方法在获取随机对象时确保线程安全，通过 synchronized 关键字实现了线程同步。
	 *
	 * @return 随机选择的 Gizmo 对象，如果集合为空则返回 null。
	 */
	public synchronized Gizmo random() {
	    // 当集合长度大于0时，说明有可用的 Gizmo 对象
	    if (length > 0) {
	        // 使用 Random.Int(length) 方法获取一个随机索引
	        // 然后从 members 集合中返回对应索引的 Gizmo 对象
	        return members.get(Random.Int(length));
	    } else {
	        // 如果集合为空，返回 null
	        return null;
	    }
	}

	/**
	 * 清空成员集合。
	 * 此方法同步执行，确保在多线程环境下对集合的操作是安全的。
	 * 它首先检查集合是否为空，如果为空则直接返回，不执行任何操作。
	 * 对于每个非空的成员，将其父引用设置为null，然后清空集合本身，并将长度重置为0。
	 * 这样做既释放了集合中成员的引用，也清空了集合，使得后续可以重新添加成员。
	 */
	public synchronized void clear() {
	    // 如果集合为空，则无需进行任何操作
	    if (length == 0) return;

	    // 遍历集合中的每个成员
	    for (int i=0; i < length; i++) {
	        Gizmo g = members.get(i);
	        // 如果成员不为空，则将其父引用设置为null
	        if (g != null) {
	            g.parent = null;
	        }
	    }

	    // 清空成员集合，释放所有引用
	    members.clear();
	    // 将集合长度重置为0，表示当前集合为空
	    length = 0;
	}

	/**
	 * 将指定的 Gizmo 对象带到集合的前面。
	 *
	 * 此方法用于管理一个 Gizmo 对象集合，确保被调用的 Gizmo 对象在集合中处于最前面的位置。
	 * 如果 Gizmo 对象已存在于集合中，则将其移除并重新添加，以更新其位置。
	 * 如果 Gizmo 对象不存在于集合中，则返回 null。
	 *
	 * @param g 要带到集合前面的 Gizmo 对象。
	 * @return 如果 Gizmo 对象存在于集合中，则返回该对象；否则返回 null。
	 */
	public synchronized Gizmo bringToFront( Gizmo g ) {
	    // 检查 Gizmo 对象是否存在于集合中
	    if (members.contains( g )) {
	        // 如果存在，先移除再重新添加，以更新其位置
	        members.remove( g );
	        members.add( g );
	        return g;
	    } else {
	        // 如果不存在于集合中，返回 null
	        return null;
	    }
	}

	/**
	 * 将指定的 Gizmo 对象移至成员列表的开头。
	 * 此方法用于管理一个 Gizmo 对象的集合，当某个 Gizmo 对象需要被优先处理时，可以通过此方法将其移至集合的开头。
	 *
	 * @param g 要被移至开头的 Gizmo 对象。
	 * @return 如果 g 是集合的成员，则返回 g；否则返回 null。
	 */
	public synchronized Gizmo sendToBack( Gizmo g ) {
	    // 检查 g 是否为成员对象
	    if (members.contains( g )) {
	        // 从集合中移除 g
	        members.remove( g );
	        // 将 g 添加到集合的开头
	        members.add( 0, g );
	        // 返回移动后的对象
	        return g;
	    } else {
	        // 如果 g 不是集合的成员，则返回 null
	        return null;
	    }
	}

    /**
     * 使用指定的比较器对集合中的元素进行排序。
     * 此方法检查集合是否已经根据比较器排序，如果没有则进行排序。
     * 通过synchronized关键字确保在排序过程中线程安全。
     *
     * @param c 用于排序的比较器，定义了元素的顺序。
     */
    public synchronized void sort(Comparator c){
        // 只有当集合未排序时才执行排序操作
        for (int i = 0; i < length - 1; i++) {
            // 检查当前元素与下一个元素的顺序是否正确，如果不正确则需要排序
            if (c.compare(members.get(i), members.get(i + 1)) > 0) {
                // 使用Collections.sort方法来排序，可以按照指定的比较器排序
                Collections.sort(members, c);
                // 排序完成后立即返回
                return;
            }
        }
    }

}
