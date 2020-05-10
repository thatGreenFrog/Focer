package lv.greenfrog.crawler.classifier;

import java.io.File;

public class MultiClassFocerClassifier extends AbstractFocerClassifier{

    public MultiClassFocerClassifier(String resourceFolder) throws Exception {
        super(String.format("%s%smulti", resourceFolder, File.separator));
    }

    @Override
    protected int getMaxNgram() {
        return 1;
    }

}
