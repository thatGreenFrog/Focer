package lv.greenfrog.crawler.classifier;

import com.digitalpebble.stormcrawler.util.ConfUtils;
import lv.greenfrog.crawler.indexer.SolrIndexer;
import weka.classifiers.AbstractClassifier;
import weka.core.*;
import weka.core.converters.ArffLoader;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.FixedDictionaryStringToWordVector;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AbstractFocerClassifier {

    private PreProcessor preProcessor;
    private AbstractClassifier classifier;
    private Instances classifyInstances;
    private Attribute classes;
    private FixedDictionaryStringToWordVector filter;

    public AbstractFocerClassifier(String resourceFolder, String subfolder, Integer maxNgram, Integer docCount) throws Exception {
        buildClassifier(resourceFolder, subfolder, maxNgram, docCount);
    }

    public String classify(String text) throws Exception {
        //Text stemming, stop-word removal, non-word removal, case folding
        String newText = preProcessor.preProcess(text);

        double[] vals = new double[2];
        vals[0] = 0;
        vals[1] = classifyInstances.attribute(1).addStringValue(newText);

        //Create instanceToClassify object and text attributes
        Instance instanceToClassify = new DenseInstance(1, vals);
        //Add to dataset
        classifyInstances.add(instanceToClassify);
        //Class is unknown
        instanceToClassify.setClassMissing();

        //Tokenize instanceToClassify with FixedDictionaryStringToWordVector
        Instances newInstances = Filter.useFilter(classifyInstances, filter);

        //Classify instance and get class index
        double c = classifier.classifyInstance(newInstances.get(0));
        classifyInstances.delete();
        return classes.value((int) c);
    }

    public void buildClassifier(String resourceFolder, String subfolder, Integer maxNgram, int docCount) throws Exception {
        String fullFolder = String.format("%s%s%s%s", resourceFolder, File.separator, subfolder, File.separator);
        classifier = (AbstractClassifier) SerializationHelper.read(String.format("%sclassifier.model", fullFolder));

        preProcessor = new PreProcessor();

        ArffLoader loader = new ArffLoader();
        loader.setFile(new File(String.format("%sclassifier.arff", fullFolder)));
        Instances structure = loader.getStructure();
        structure.setClassIndex(0);

        classes = structure.classAttribute();

        ArrayList<Attribute> attributes = new ArrayList<>();
        attributes.add(classes);
        attributes.add(new Attribute("text", (ArrayList)null));

        classifyInstances = new Instances("Classify", attributes, 1);
        classifyInstances.setClassIndex(0);
        filter = new FixedDictionaryStringToWordVector();
        filter.setLowerCaseTokens(true);
        filter.setTFTransform(true);
        filter.setIDFTransform(true);
        filter.setDictionaryFile(new File(String.format("%sclassifier.dictionary", fullFolder)));
        filter.setInputFormat(classifyInstances);
        // fix broken m_count in dictionary build, any positive constant will work
        Field mCount = DictionaryBuilder.class.getDeclaredField("m_count");
        mCount.setAccessible(true);
        Field mVectorizer = FixedDictionaryStringToWordVector.class.getDeclaredField("m_vectorizer");
        mVectorizer.setAccessible(true);
        mCount.set(mVectorizer.get(filter), docCount);
    }

}
