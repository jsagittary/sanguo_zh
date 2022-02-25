package com.hundredcent.game;

/**
 * @author xwind
 * @date 2022/1/6
 */
public class PayTableIdx {
    public static void main(String[] args) {
        String serialId = "5326_15253261000026_20220106000329637_537";
        int idx = serialId == null ? 0 : (serialId.hashCode() & Integer.MAX_VALUE) % 10;
        System.out.println(idx);
    }
}
