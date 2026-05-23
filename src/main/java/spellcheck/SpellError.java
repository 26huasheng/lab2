package spellcheck;

public class SpellError {

    private final String word;

    private final String suggestion;

    public SpellError(String word, String suggestion) {
        this.word = word;
        this.suggestion = suggestion;
    }

    public String getWord() {
        return word;
    }

    public String getSuggestion() {
        return suggestion;
    }
}
