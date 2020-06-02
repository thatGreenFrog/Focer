package lv.greenfrog.crawler.classifier;

import org.apache.lucene.analysis.lv.LatvianStemmer;
import weka.core.tokenizers.NGramTokenizer;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class PreProcessor {

    private final LatvianStemmer stemmer;
    private final String stopWords;

    public PreProcessor() throws IOException {
        InputStream stopWordsStream = this.getClass().getClassLoader().getResourceAsStream("stop_words.txt");
        if(stopWordsStream == null) stopWordsStream = this.getClass().getResourceAsStream("stop_words.txt");
        stopWords = Arrays.stream(new String(stopWordsStream.readAllBytes(), StandardCharsets.UTF_8).split("\\s+"))
                    .map(s -> String.format("(%s)", s))
                    .collect(Collectors.joining("|"));

        this.stemmer = new LatvianStemmer();

    }

    public String preProcess(String text) {
        //Create array of words
        return Arrays.stream(text.split("\\s"))
                //Case folding
                .map(String::toLowerCase)
                //Remove non words
                .map(s -> s.replaceAll("[^A-Za-zāčēģīķļņšūž ]", ""))
                //Filter out stop words
                .filter(s -> !s.matches(stopWords) && !s.isEmpty())
                //Stem words
                .map(s -> {
                    char[] sArr = s.toCharArray();
                    int l = stemmer.stem(sArr, s.length());
                    return String.valueOf(sArr, 0, l);
                })
                //Collect to String
                .collect(Collectors.joining(" "));
    }

    public LatvianStemmer getStemmer() {
        return stemmer;
    }

    public String getStopWords() {
        return stopWords;
    }
}
