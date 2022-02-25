package com.gryphpoem.game.zw.core.util;

import java.util.LinkedList;

/**
 * @ClassName FixSizeLinkedList.java
 * @Description 定长的LinkedList
 * @author QiuKun
 * @date 2019年5月16日
 */
public class FixSizeLinkedList<E> extends LinkedList<E> {

    private static final long serialVersionUID = 2355891309911720276L;

    // 定义缓存的容量
    protected int capacity;

    public FixSizeLinkedList(int capacity) {
        super();
        this.capacity = capacity;
    }

    @Override
    public boolean add(E e) {
        // 超过长度，移除最后一个
        if (size() + 1 > capacity) {
            super.removeFirst();
        }
        return super.add(e);
    }

}
