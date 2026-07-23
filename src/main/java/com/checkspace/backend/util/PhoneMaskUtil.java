package com.checkspace.backend.util;

public class PhoneMaskUtil {

    public static String mask(String phone) {
        if (phone == null || phone.length() < 4) return "XXXXXXXXXX";
        String lastFour = phone.substring(phone.length() - 4);
        return "XXXXXX" + lastFour;
    }
}