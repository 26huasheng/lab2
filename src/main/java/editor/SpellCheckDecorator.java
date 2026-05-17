package editor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SpellCheckDecorator extends EditorDecorator {

    private static final Pattern SPELL_CHECK_PATTERN = Pattern.compile("(?i)[bcdfghjklmnpqrstvwxyz]{3,}");

    public SpellCheckDecorator(IEditor delegate) {
        super(delegate);
    }

    public void checkSpelling(String text) {
        Matcher matcher = SPELL_CHECK_PATTERN.matcher(text);
        if (matcher.find()) {
            System.out.println("[Warning] \u7591\u4F3C\u62FC\u5199\u9519\u8BEF: " + text);
        }
    }

    public void checkFullContent() {
        String content = delegate.getPlainTextContent();
        if (content != null && !content.isEmpty()) {
            checkSpelling(content);
        }
    }
}
