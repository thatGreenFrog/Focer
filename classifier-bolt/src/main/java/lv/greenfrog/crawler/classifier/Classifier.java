package lv.greenfrog.crawler.classifier;

import weka.classifiers.AbstractClassifier;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayesMultinomial;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;
import weka.core.SerializationHelper;
import weka.core.converters.ArffLoader;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Classifier {

    private ArrayList<Attribute> attributes;
    private AbstractClassifier classifier;
    private Instances instances;
    private Attribute classes;

    public Classifier(String resourceFolder) throws Exception {
        buildClassifier(resourceFolder);
        instances = new Instances("ClassifyInstances", attributes, 1);
        instances.setClassIndex(0);
    }

    public String classify(List<String> tokens) throws Exception {
        double[] vals = new double[attributes.size()];
        vals[0] = 0;
        attributes.forEach(s -> vals[attributes.indexOf(s)] = Collections.frequency(tokens, s.name()));

        DenseInstance instanceToClassify = new DenseInstance(1, vals);
        instanceToClassify.setDataset(instances);
        instanceToClassify.setClassMissing();

        try{
            double c = classifier.classifyInstance(instanceToClassify);
            return attributes.get(attributes.indexOf(classes)).value((int) c);
        }
        finally {
            instances.delete();
        }
    }

    public static void main(String[] args) throws Exception {
        new Classifier("D:\\GitHub\\Focer\\resources").classify(Arrays.asList("sdfdfgdfh", "bmw", "atļauj", "atļauj"));
    }

    public void buildClassifier(String resourceFolder) throws Exception {
        classifier = (NaiveBayesMultinomial)SerializationHelper.read(String.format("%s%sclassifier.model", resourceFolder, File.separator));
        ArffLoader loader = new ArffLoader();
        loader.setFile(new File(String.format("%s%sinstances.arff", resourceFolder, File.separator)));
        Instances dataSet = loader.getDataSet();
        attributes = new ArrayList<>();
        classes = dataSet.attribute("class");
        dataSet.enumerateAttributes().asIterator().forEachRemaining(a -> attributes.add(a));
    }
}
