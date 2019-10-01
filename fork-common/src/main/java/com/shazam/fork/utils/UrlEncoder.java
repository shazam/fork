package com.shazam.fork.utils;

import javax.annotation.Nonnull;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import static java.nio.charset.StandardCharsets.UTF_8;

public class UrlEncoder {
    private UrlEncoder() {}

    @Nonnull
    public static String encodeUrl(@Nonnull String string) {
        try {
            return URLEncoder.encode(string, UTF_8.name())
                    .replaceAll("\\+", "%20");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return "";
        }
    }
}
