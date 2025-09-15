package com.kanako.chatbaseonnetty.base.utils;

public class Base62Utils {
    private static final String BASE62 = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final char[] CHAR_ARRAY = BASE62.toCharArray();
    private static final int BASE = CHAR_ARRAY.length;

    public static String encode(long num) {
        StringBuilder stringBuilder = new StringBuilder();
        while (num > 0) {
            stringBuilder.insert(0, CHAR_ARRAY[(int) (num % BASE)]);
            num = num / BASE;
        }
        return stringBuilder.toString();
    }

    public static long decode(String str) {
        long num = 0;
        for (int i = 0; i < str.length(); i++) {
            num = num * BASE + BASE62.indexOf(str.charAt(i));
        }
        return num;
    }
}
