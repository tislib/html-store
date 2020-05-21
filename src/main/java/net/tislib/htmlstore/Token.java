package net.tislib.htmlstore;

import java.net.URL;
import java.util.List;
import java.util.Objects;

public class Token {
    public List<PathItem> paths;
    public String text;
    public URL url;

    @Override
    public String toString() {
        return "Token{" +
                "paths=" + paths +
                ", text='" + text + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Token token = (Token) o;
        return paths.equals(token.paths) &&
                text.equals(token.text);
    }

    @Override
    public int hashCode() {
        return Objects.hash(paths, text);
    }
}
