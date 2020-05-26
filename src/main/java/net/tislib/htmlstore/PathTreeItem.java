package net.tislib.htmlstore;

import lombok.Data;

import java.util.*;

@Data
public class PathTreeItem {
    private final String tag;

    private Map<String, String> attributes = new HashMap<>();

    private Set<Long> pageUrls = new HashSet<>();

    private List<PathTreeItem> children = new ArrayList<>();

    private PathTreeItem parent;
}
