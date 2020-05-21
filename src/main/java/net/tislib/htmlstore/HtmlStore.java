package net.tislib.htmlstore;

import java.util.HashMap;
import java.util.Map;

public class HtmlStore {

    Map<String, String> data = new HashMap<>();

    public void put(String item, String htmlContent) {
        data.put(item, htmlContent);
    }

    public String get(String item) {
        return data.get(item);
    }
}
