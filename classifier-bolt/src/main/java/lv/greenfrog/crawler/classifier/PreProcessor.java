package lv.greenfrog.crawler.classifier;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.lv.LatvianStemmer;
import org.apache.lucene.analysis.shingle.ShingleFilter;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.util.Version;
import weka.core.tokenizers.NGramTokenizer;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    public List<String> preProcess(String text) {
        Stream<String> textList = Arrays.stream(caseFolding(removeNonWords(text)).split("\\s"));
        textList = stem(removeStopWords(textList));
        return tokenize(textList.collect(Collectors.joining(" ")));
    }

    public List<String> tokenize(String text) {
        tokenizer.tokenize(text);
        List<String> tokens = new ArrayList<>();
        while(tokenizer.hasMoreElements()){
            tokens.add(tokenizer.nextElement());
        }
        return tokens;
    }

    public String caseFolding(String text){
        return text.toLowerCase();
    }

    public Stream<String> stem(Stream<String> text){
        return text
                .map(s -> s.substring(0, stemmer.stem(s.toCharArray(), s.length())));
    }

    public Stream<String> removeStopWords(Stream<String> text){
        return text
                .filter(s -> !s.matches(stopWords));
    }

    public String removeNonWords(String text){
        return text.replaceAll("[^A-Za-zāčēģīķļņšūž ]", "");
    }

}
