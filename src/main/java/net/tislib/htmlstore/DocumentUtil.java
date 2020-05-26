package net.tislib.htmlstore;

import org.jsoup.nodes.*;

import java.util.HashMap;
import java.util.Map;

public class DocumentUtil {
    public static Document prepareDoc(Document document, TokenTreeConfig config) {
        Document newDocument = new Document(document.baseUri());

        prepareDoc(newDocument, document, config);

        return newDocument;
    }

    public static void walkElement(Element element, NodeVisitor visitor) {
        int index = 0;
        for (Node node : element.childNodes()) {
            visitor.apply(node, element, index++);
            if (node instanceof Element) {
                walkElement((Element) node, visitor);
            }
        }
    }

    private static void prepareDoc(Element newElement, Element existingElement, TokenTreeConfig config) {
        int index = 0;
        for (Node node : existingElement.childNodes()) {
            if (node instanceof TextNode) {
                Element element = new Element("text");
                element.attr("value", ((TextNode) node).text().trim());
                if (((TextNode) node).text().trim().equals("")) {
                    continue;
                }
                index++;
                if (config.isKeepIndexing()) {
                    element.attr("index", String.valueOf(index));
                }
                newElement.appendChild(element);
            } else if (node instanceof Element) {
                Element element = ((Element) node).clone();
                element.html("");
                prepareDoc(element, (Element) node, config);

                index++;
                if (config.isKeepIndexing()) {
                    element.attr("index", String.valueOf(index));
                }
                newElement.appendChild(element);
            } else if (node instanceof DocumentType) {
                /*Element element = new Element("doctype");
                element.attributes().addAll(node.attributes());

                index++;
                element.attr("index", String.valueOf(index));
                newElement.appendChild(element);
                 */
                continue;
            } else if (node instanceof Comment) {
                continue;
            } else if (node instanceof DataNode) {
                continue;
            } else {
                throw new UnsupportedOperationException();
            }
        }
    }

    public static Document cleanUp(Document document) {
        Document newDocument = new Document("");

        cleanUp(newDocument, document);

        return newDocument;
    }

    private static void cleanUp(Element newElement, Element existingElement) {
        for (Node node : existingElement.childNodes()) {
            if (node instanceof Element) {
                String tagName = ((Element) node).tagName();
                if (tagName.equals("script") || tagName.equals("style")) {
                    continue;
                }
                Element eElement = (Element) node;
                Element element = new Element(eElement.tagName());
                eElement.attributes().forEach(item -> element.attr(item.getKey(), item.getValue()));
                element.html("");
                cleanUp(element, (Element) node);
                newElement.appendChild(element);
                continue;
            } else if (node instanceof DocumentType) {
                continue;
            } else if (node instanceof TextNode) {
                String txt = ((TextNode) node).text().trim();
                if (txt.equals("")) {
                    continue;
                }
                newElement.appendChild(new TextNode(txt));
                continue;
            } else if (node instanceof Comment) {
                continue;
            }
            newElement.appendChild(node.clone());
        }
    }

    public static Map<String, String> toMap(Attributes attributes) {
        Map<String, String> map = new HashMap<>();

        attributes.forEach(item -> map.put(item.getKey(), item.getValue()));

        return map;
    }

    public static interface NodeVisitor {
        public void apply(Node node, Element parent, Integer index);
    }
}
