package spellcheck;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MockSpellCheckerAdapter implements ISpellChecker {

    private static final Pattern WORD_PATTERN = Pattern.compile("[a-zA-Z]+");

    private static final Pattern BAD_WORD_PATTERN = Pattern.compile("(?i)[bcdfghjklmnpqrstvwxyz]{3,}");

    @Override
    public List<SpellError> check(String text) {
        List<SpellError> results = new ArrayList<>();
        if (text == null || text.isEmpty()) {
            return results;
        }

        Matcher wordMatcher = WORD_PATTERN.matcher(text);
        while (wordMatcher.find()) {
            String word = wordMatcher.group();
            Matcher badMatcher = BAD_WORD_PATTERN.matcher(word);
            if (badMatcher.find()) {
                results.add(new SpellError(word, "建议修改"));
            }
        }
        return results;
    }
}
