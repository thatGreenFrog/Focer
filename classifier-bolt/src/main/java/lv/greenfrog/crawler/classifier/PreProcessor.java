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
    private final NGramTokenizer tokenizer;

    public PreProcessor() throws IOException {
        InputStream stopWordsStream = this.getClass().getClassLoader().getResourceAsStream("stop_words.txt");
        if(stopWordsStream == null) stopWordsStream = this.getClass().getResourceAsStream("stop_words.txt");
        stopWords = Arrays.stream(new String(stopWordsStream.readAllBytes(), StandardCharsets.UTF_8).split("\\s+"))
                    .map(s -> String.format("(%s)", s))
                    .collect(Collectors.joining("|"));

        this.stemmer = new LatvianStemmer();

        tokenizer = new NGramTokenizer();
    }

    public String preProcess(String text) {
        return Arrays.stream(text.split("\\s"))
                .map(String::toLowerCase)
                .map(s -> s.replaceAll("[^A-Za-zāčēģīķļņšūž ]", ""))
                .filter(s -> !s.matches(stopWords) && !s.isEmpty())
                .map(s -> s.substring(0, stemmer.stem(s.toCharArray(), s.length())))
                .collect(Collectors.joining(" "));
    }

    public List<String> tokenize(String text) {
        tokenizer.tokenize(text);
        List<String> tokens = new ArrayList<>();
        while(tokenizer.hasMoreElements()){
            tokens.add(tokenizer.nextElement());
        }
        return tokens;
    }

    public LatvianStemmer getStemmer() {
        return stemmer;
    }

    public String getStopWords() {
        return stopWords;
    }

    public NGramTokenizer getTokenizer() {
        return tokenizer;
    }
}
