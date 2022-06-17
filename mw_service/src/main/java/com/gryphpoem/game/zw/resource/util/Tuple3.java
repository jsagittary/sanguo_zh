package com.gryphpoem.game.zw.resource.util;

public class Tuple3<T1, T2, T3> {
    private T1 a;
    private T2 b;
    private T3 c;

    public T1 getA() {
        return a;
    }

    public void setA(T1 a) {
        this.a = a;
    }

    public T2 getB() {
        return b;
    }

    public void setB(T2 b) {
        this.b = b;
    }

    public T3 getC() {
        return c;
    }

    public void setC(T3 c) {
        this.c = c;
    }

    /**
     * @param a
     * @param b
     * @param c
     */
    public Tuple3(T1 a, T2 b, T3 c) {
        super();
        this.a = a;
        this.b = b;
        this.c = c;
    }

}
