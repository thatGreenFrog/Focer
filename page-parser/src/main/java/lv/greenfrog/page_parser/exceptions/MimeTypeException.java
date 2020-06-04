package lv.greenfrog.page_parser.exceptions;

public class MimeTypeException extends PageParserException {

    public MimeTypeException(String mimeType, String url) {
        super(String.format("Expected MimeType -> HTML. Got -> %s. On URL -> %s", mimeType, url));
    }
}
