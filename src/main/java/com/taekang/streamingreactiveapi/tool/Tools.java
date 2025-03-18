package com.taekang.streamingreactiveapi.tool;

import java.net.URI;
import java.net.URISyntaxException;

public class Tools {

    public static String getPathSegments(String url, int index) throws URISyntaxException {
        URI uri = new URI(url);
        String path = uri.getPath();
        String[] pathSegments = path.split("/");

        // 첫 번째 요소는 빈 문자열이므로 index + 1을 사용
        if (index >= 0 && index < pathSegments.length) {
            return pathSegments[index];
        } else {
            throw new IllegalArgumentException("Invalid index: " + index);
        }
    }
}
