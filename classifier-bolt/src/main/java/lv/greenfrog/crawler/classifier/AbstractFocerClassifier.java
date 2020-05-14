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
    private ArrayList<Attribute> attributes;
    private Attribute classes;
    private FixedDictionaryStringToWordVector filter;

    public AbstractFocerClassifier(String resourceFolder, String subfolder, Integer maxNgram, Integer docCount) throws Exception {
        buildClassifier(resourceFolder, subfolder, maxNgram, docCount);
    }

    public String classify(String text) throws Exception {
        String newText = preProcessor.preProcess(text);

        double[] vals = new double[2];
        vals[0] = 0;
        vals[1] = classifyInstances.attribute(1).addStringValue(newText);

        DenseInstance instanceToClassify = new DenseInstance(1, vals);
        classifyInstances.add(instanceToClassify);
        instanceToClassify.setDataset(classifyInstances);
        instanceToClassify.setClassMissing();

        Instances newInstances = Filter.useFilter(classifyInstances, filter);

        double c = classifier.classifyInstance(newInstances.get(0));
        classifyInstances.delete();
        return classes.value((int) c);
    }

    public void buildClassifier(String resourceFolder, String subfolder, Integer maxNgram, int docCount) throws Exception {
        String fullFolder = String.format("%s%s%s%s", resourceFolder, File.separator, subfolder, File.separator);
        classifier = (AbstractClassifier) SerializationHelper.read(String.format("%sclassifier.model", fullFolder));

        preProcessor = new PreProcessor(maxNgram);

        ArffLoader loader = new ArffLoader();
        loader.setFile(new File(String.format("%sclassifier.arff", fullFolder)));
        Instances structure = loader.getStructure();
        structure.setClassIndex(0);

        classes = structure.classAttribute();

        attributes = new ArrayList<>();
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



    public static void main(String[] args) throws Exception {
        String text = "Šipačovs nosaukts par KHL labāko hokejistu spēlētāju iekšējā balsojumā\n" +
                "2020. gada 13. maijs 16:06\n" +
                "Šipačovs nosaukts par KHL labāko hokejistu spēlētāju iekšējā balsojumā\n" +
                "Vadims Šipačovs\n" +
                "FOTO: dynamo.ru\n" +
                "Par Kontinentālās hokeja līgas (KHL) aizvadītās sezonas labāko hokejistu spēlētāju iekšējā balsojumā atzīts Maskavas \"Dinamo\" spīdeklis Vadims Šipačovs, vēsta KHL.\n" +
                "Spēlētāji pēc sezonas priekšlaicīgā noslēguma aprīlī aizpildīja anketas, kurās atbildēja uz vairākiem interesantiem jautājumiem. Hokejistiem bija jānorāda smieklīgākie, labākie un cienītākie spēlētāji savā komandā un visā KHL. \n" +
                "Spēlētāju balsojumā par pagājušās sezonas labāko hokejistu nosaukts Šipačovs, kurš saņēmis 31,4% balsu. Otro vietu ieguva Maskavas CSKA uzbrucējs Kirils Kaprizovs - 24,6%, bet labāko trijnieku noslēdza vēl viens \"dinamietis\" Dimitrijs Jaškins (10,9%).\n" +
                "Par cienītāko spēlētāju līgā tiek uzskatīts pieredzes un panākumiem bagātais Pāvels Dacjuks no Jekaterinburgas \"Avtomobilist\", kurš ieguva 32% no visām iesniegtajām atbildēm. 22% saņēma Sergejs Mozjakins (Magņitagorskas \"Metallurg\"), bet 11,5% piešķirti Danisam Zaripovam (Kazaņas \"Ak Bars\").\n" +
                "Dacjuks vairāk vai mazāk tika minēts visās atbildēs. Tāpat visās kategorijās pavīdēja arī Šipačova un Mozjakina vārdi.\n" +
                "Savukārt par smieklīgāko līgas hokejistu nosaukts \"Soču\" spēlētājs Andrejs Altibarmakjans (7%). Šajā kategorijā par Rīgas komandas jautrāko personu atzīts pieredzējušais aizsargs Kristaps Sotnieks.\n" +
                "Rīgas \"Dinamo\" hokejisti pieminēti arī pārējās kategorijās. Par cienītāko rīdzinieku atzīts kapteinis Lauris Dārziņš, bet Aleksandrs Salāks izpelnījies labākā spēlētāja godu.\n" +
                "KHL 7.maijā paziņoja galīgo vietu sadalījumu 2019./2020.gada sezonā, ierindojot Rīgas \"Dinamo\" 23.vietā un nenosakot KHL čempionu un Gagarina kausa īpašnieku.";
        AbstractFocerClassifier b = new AbstractFocerClassifier("D:\\GitHub\\Focer\\resources\\resources", "binary", 1, 4649);
        System.out.println(b.classify(text));
        AbstractFocerClassifier m = new AbstractFocerClassifier("D:\\GitHub\\Focer\\resources\\resources", "multi", 3, 2349);
        System.out.println(m.classify(text));
    }

}
