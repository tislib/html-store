package net.tislib.htmlstore;

import org.jsoup.nodes.*;

import java.util.HashMap;
import java.util.Map;

public class TokenUtil {
    public static Document prepareDoc(Document document) {
        Document newDocument = new Document(document.baseUri());

        prepareDoc(newDocument, document);

        return newDocument;
    }

    private static void prepareDoc(Element newElement, Element existingElement) {
        int index = 0;
        for (Node node : existingElement.childNodes()) {
            if (node instanceof TextNode) {
                Element element = new Element("text");
                element.attr("value", ((TextNode) node).text().trim());
                if (((TextNode) node).text().trim().equals("")) {
                    continue;
                }
                index++;
                element.attr("index", String.valueOf(index));
                newElement.appendChild(element);
            } else if (node instanceof Element) {
                Element element = ((Element) node).clone();
                element.html("");
                prepareDoc(element, (Element) node);

                index++;
                element.attr("index", String.valueOf(index));
                newElement.appendChild(element);
            }
        }
    }

    public static Map<String, String> toMap(Attributes attributes) {
        Map<String, String> map = new HashMap<>();

        attributes.forEach(item -> map.put(item.getKey(), item.getValue()));

        return map;
    }
}
