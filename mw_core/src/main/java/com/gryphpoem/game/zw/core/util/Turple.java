package com.gryphpoem.game.zw.core.util;

public class Turple<T, V> {
    private T a;
    private V b;

    public Turple() {
    }

    public Turple(T a, V b) {
        this.setA(a);
        this.setB(b);
    }

    public T getA() {
        return a;
    }

    public void setA(T a) {
        this.a = a;
    }

    public V getB() {
        return b;
    }

    public void setB(V b) {
        this.b = b;
    }

    @Override
    public String toString() {
        return "Turple [a=" + a + ", b=" + b + "]";
    }

}
