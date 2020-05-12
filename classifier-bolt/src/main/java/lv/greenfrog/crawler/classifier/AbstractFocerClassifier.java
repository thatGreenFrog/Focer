package lv.greenfrog.crawler.classifier;

import weka.classifiers.AbstractClassifier;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;
import weka.core.SerializationHelper;
import weka.core.converters.ArffLoader;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AbstractFocerClassifier {

    private PreProcessor preProcessor;
    private AbstractClassifier classifier;
    private Instances structure;
    private ArrayList<Attribute> attributes;
    private Attribute classes;

    public AbstractFocerClassifier(String resourceFolder, String subfolder, Integer maxNgram) throws Exception {
        buildClassifier(resourceFolder, subfolder, maxNgram);
    }

    public String classify(String text) throws Exception {
        String newText = preProcessor.preProcess(text);
        List<String> tokens = preProcessor.tokenize(newText);

        double[] vals = new double[attributes.size()];
        vals[0] = 0;
        attributes.forEach(s -> vals[attributes.indexOf(s)] = Math.log(1 + Collections.frequency(tokens, s.name())));

        DenseInstance instanceToClassify = new DenseInstance(1, vals);
        instanceToClassify.setDataset(structure);
        instanceToClassify.setClassMissing();

        double c = classifier.classifyInstance(instanceToClassify);
        return classes.value((int) c);
    }

    public void buildClassifier(String resourceFolder, String subfolder, Integer maxNgram) throws Exception {
        String fullFolder = String.format("%s%s%s%s", resourceFolder, File.separator, subfolder, File.separator);
        classifier = (AbstractClassifier) SerializationHelper.read(String.format("%sclassifier.model", fullFolder));

        preProcessor = new PreProcessor(maxNgram);

        ArffLoader loader = new ArffLoader();
        loader.setFile(new File(String.format("%sclassifier.arff", fullFolder)));
        structure = loader.getStructure();
        structure.setClassIndex(0);

        classes = structure.classAttribute();

        attributes = new ArrayList<>();
        structure.enumerateAttributes().asIterator().forEachRemaining(a -> attributes.add(a));
    }

}
