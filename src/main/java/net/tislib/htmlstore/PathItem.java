package net.tislib.htmlstore;

import java.util.Map;
import java.util.Objects;

public class PathItem {
    public Map<String, String> attributes;
    public int index;
    public String tag;

    @Override
    public String toString() {
        return "PathItem{" +
                "attributes=" + attributes +
                ", index=" + index +
                ", tag='" + tag + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PathItem pathItem = (PathItem) o;
        return attributes.equals(pathItem.attributes) &&
                tag.equals(pathItem.tag);
    }

    @Override
    public int hashCode() {
        return Objects.hash(attributes, tag);
    }
}
