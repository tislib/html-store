package net.tislib.htmlstore;

public class SelectionHelper {

    public static String getUniqueCssSelector(PathTreeItem item) {
        StringBuilder selector = new StringBuilder(item.getTag());

        if (item.getAttributes().get("class") != null) {
            String[] classList = item.getAttributes().get("class").split(" ");

            for (String className : classList) {
                selector.append(".").append(className);
            }
        }

        if (item.getParent() != null && !item.getParent().getTag().equals("root")) {
            String parentSelector = getUniqueCssSelector(item.getParent());
            selector = new StringBuilder(parentSelector + " > " + selector);
        }

        return selector.toString();
    }

    public static String predictFieldName(PathTreeItem item) {
        return "";
    }

    private PathTreeItem findRoot(PathTreeItem item) {
        if (item.getParent() != null) {
            return findRoot(item.getParent());
        }
        return item;
    }

}
