//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.fasterxml.jackson.databind.ObjectWriter;
//import lombok.SneakyThrows;
//import net.tislib.htmlstore.HtmlStore;
//import net.tislib.htmlstore.TokenTree;
//import net.tislib.htmlstore.TokenTreeConfig;
//import org.apache.commons.io.IOUtils;
//
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.FileOutputStream;
//import java.nio.charset.StandardCharsets;
//
//public class Test {
//
//    private final TokenTree tokenTree = new TokenTree(TokenTreeConfig.builder().build());
//    private final ObjectMapper objectMapper = new ObjectMapper();
//
//    @org.junit.Test
//    public void compressDecompress() {
//        String testData = this.getClass().getClassLoader().getResource("test-data").getFile();
//
//        for (String item : new File(testData).list()) {
//            String htmlContent = readFile(item);
//            tokenTree.put(item, htmlContent);
//
//            writeFileObject(item + ".json", tokenTree.getData(item));
//        }
//        writeFileObject("template.json", tokenTree.getDataTemplate());
//
//    }
//
//    @SneakyThrows
//    private String readFile(String item) {
//        try (FileInputStream fis = new FileInputStream(this.getClass().getClassLoader().getResource("test-data/" + item).getFile())) {
//            return IOUtils.toString(fis, StandardCharsets.UTF_8);
//        }
//    }
//
//    @SneakyThrows
//    private void writeFileObject(String name, Object item) {
//        try (FileOutputStream fos = new FileOutputStream(this.getClass().getClassLoader().getResource("export/").getFile() + name + ".json")) {
//            ObjectWriter writer = objectMapper.writer().withDefaultPrettyPrinter();
//            writer.writeValue(fos, item);
//        }
//    }
//
//}
