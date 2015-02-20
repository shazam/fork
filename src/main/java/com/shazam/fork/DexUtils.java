package com.shazam.fork;

public class DexUtils {

    public static String getClassName(String typeDescriptor) {
        int finalSlashIndex = getFinalSlashIndex(typeDescriptor);
        return typeDescriptor.substring(finalSlashIndex + 1, typeDescriptor.length() - 1);
    }

    public static String getPackageName(String typeDescriptor) {
        int finalSlashIndex = getFinalSlashIndex(typeDescriptor);
        return typeDescriptor.substring(1, finalSlashIndex).replace('/', '.');
    }

    private static int getFinalSlashIndex(String typeDescriptor) {
        return typeDescriptor.lastIndexOf('/');
    }
}
