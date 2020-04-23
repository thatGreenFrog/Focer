package lv.greenfrog.page_parser.exceptions;

import java.util.HashMap;
import java.util.Map;

public class LanguageException extends PageParserException {

    public static final String WRONG_LANGUAGE = "Expected language -> lv. Got -> %s. URL -> %s";
    public static final String LANGUAGE_NOT_DETECTED = "Detector was unable to detect language";

    public LanguageException(String message) {
        super(message);
    }
}
