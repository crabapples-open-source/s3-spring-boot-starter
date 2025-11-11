package cn.crabapples.utils;

import java.util.Random;

public class StringUtils {
    public static final char[] CHAR_ARRAY = new char[]{'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'};

    public static String genRandomFileName(String fileName) {
        String[] split = fileName.split("\\.");
        String suffix = split.length > 0 ? "." + split[split.length - 1] : "";
        Random random = new Random();
        StringBuilder name = new StringBuilder();
        for (int i = 0; i < 32; i++) {
            name.append(CHAR_ARRAY[random.nextInt(26)]);
        }
        return name + suffix;
    }
}
