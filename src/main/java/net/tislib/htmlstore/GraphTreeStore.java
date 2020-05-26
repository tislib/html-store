package net.tislib.htmlstore;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;

import java.util.HashSet;
import java.util.Set;

@RequiredArgsConstructor
public class GraphTreeStore implements HtmlStore {

    private final TokenTreeConfig config;

    private final CachedContainer<String> cachedContainer = new CachedContainer<>();
    private final CachedContainer<String> textList = new CachedContainer<>();

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final CachedContainer<String> nodes = new CachedContainer<>();
    private final Set<String> vertices = new HashSet<>();

    @Override
    public void put(String key, String htmlContent) {
        Document document = Jsoup.parse(htmlContent);
        DocumentUtil.walkElement(document, (node, parent, index) -> {
            String nodeStr = toString(node, 0);
            String parentStr = toString(parent, 0);

            Integer nodeIndex = nodes.store(nodeStr);
            Integer parentIndex = nodes.store(parentStr);

            vertices.add(nodeIndex + "->" + parentIndex);
        });
    }

    private String toString(Node node, Integer i) {
        if (node instanceof TextNode) {
            TextNode textNode = (TextNode) node;
            int index = textList.store(textNode.text());
            return "text-" + i + "-" + index;
        } else if (node instanceof Element) {
            Element element = (Element) node;
            return element.tagName() + "-" + i + " " + element.attributes().toString();
        }
        return "none-" + i;
    }

    @Override
    public String get(String key) {
        return null;
    }

    @Override
    public String export() {
        StringBuilder stringBuilder = new StringBuilder();
        nodes.getMap().entrySet().forEach(item -> {
            stringBuilder.append(item.getValue()).append("\n");
        });
        vertices.forEach(item -> {
            stringBuilder.append(item).append("\n");
        });

        return stringBuilder.toString();
    }
}
