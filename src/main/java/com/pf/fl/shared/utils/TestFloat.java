package com.pf.fl.shared.utils;

public class TestFloat {

    public static void main(String[] args) {
        System.out.println(String.format("%x", Float.floatToIntBits((float)19.2)));
        System.out.println(String.format("%x", Float.floatToIntBits((float)-14.5)));
    }
}
