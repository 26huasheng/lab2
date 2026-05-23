package spellcheck;

import java.util.List;

public interface ISpellChecker {

    List<SpellError> check(String text);
}
