import de.l3s.boilerpipe.extractors.ArticleExtractor;
import lv.greenfrog.crawler.classifier.PreProcessor;
import org.apache.storm.shade.org.apache.commons.collections.FastArrayList;
import org.apache.tika.exception.TikaException;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.html.BoilerpipeContentHandler;
import org.apache.tika.parser.html.HtmlParser;
import org.apache.tika.sax.BodyContentHandler;
import org.apache.tika.sax.TeeContentHandler;
import org.xml.sax.SAXException;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.*;

public class Main {

    public static BoilerpipeContentHandler handler = new BoilerpipeContentHandler(new BodyContentHandler(-1), ArticleExtractor.getInstance());
    public static HtmlParser parser = new HtmlParser();

    public static void main(String[] args) throws Exception {
        PreProcessor preProcessor = new PreProcessor();

        List<String> binaryClasses = Arrays.asList("Positive", "Negative");
        handleTrainingData("D:\\WebPages\\Binary\\Training", "binary", preProcessor, binaryClasses);
        handleTrainingData("D:\\WebPages\\Binary\\Testing", "binary_test", preProcessor, binaryClasses);

        List<String> multiClasses = Arrays.asList("Auto", "Culture", "Finance", "Lifestyle", "Politics", "Sports", "Tech");
        handleTrainingData("D:\\WebPages\\Multi-class\\Training", "multi", preProcessor, multiClasses);
        handleTrainingData("D:\\WebPages\\Multi-class\\Testing", "multi_test", preProcessor, multiClasses);

    }

    private static void handleTrainingData(String folders, String type, PreProcessor preProcessor, List<String> classes) throws IOException {
        ArrayList<Attribute> atts = new ArrayList<>();
        atts.add(new Attribute("text", (FastArrayList)null));
        atts.add(new Attribute("class", classes));
        Instances data = new Instances(type, atts, 10000);
        for(File f : Objects.requireNonNull(new File(folders).listFiles())) {
            if (f.isDirectory()) {
                Arrays.stream(Objects.requireNonNull(f.listFiles()))
                        .map(t -> {
                            try {
                                return preProcessor.preProcess(getText(Files.readAllBytes(t.toPath())));
                            } catch (TikaException | SAXException | IOException e) {
                                e.printStackTrace();
                            }
                            return "";
                        })
                        .filter(s -> !s.isEmpty())
                        .distinct()
                        .forEach(s -> {
                            double[] inst = new double[2];
                            inst[0] = data.attribute(0).addStringValue(s);
                            inst[1] = classes.indexOf(f.getName());
                            data.add(new DenseInstance(1, inst));
                        });
            }
        }
        saveData(data, String.format("%s_data.arff", type));
    }

    private static String getText(byte[] content) throws TikaException, SAXException, IOException {
        parser.parse(new ByteArrayInputStream(content),
                new TeeContentHandler(handler),
                new org.apache.tika.metadata.Metadata(),
                new ParseContext());
        String text = handler.getTextDocument().getText(true, false);
        handler.recycle();
        return text;
    }

    private static void saveData(Instances data, String fileName) throws IOException {
        File file = new File("D:\\WebPages\\" + fileName);
        file.delete();
        Files.writeString(file.toPath(), data.toString(), StandardOpenOption.CREATE_NEW);

    }
}
