import static org.junit.Assert.*;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import spellcheck.ISpellChecker;
import spellcheck.MockSpellCheckerAdapter;

public class SpellCheckAdapterTest {

    private ISpellChecker checker;

    @Before
    public void setUp() {
        checker = new MockSpellCheckerAdapter();
    }

    @Test
    public void testDetectConsecutiveConsonants() {
        List<String> results = checker.check("abcdfg");
        assertFalse("\u5E94\u68C0\u6D4B\u5230\u62FC\u5199\u9519\u8BEF", results.isEmpty());
        assertTrue(results.get(0).contains("abcdfg"));
    }

    @Test
    public void testValidWordPasses() {
        List<String> results = checker.check("hello");
        assertTrue("\u6B63\u786E\u5355\u8BCD\u5E94\u901A\u8FC7", results.isEmpty());
    }

    @Test
    public void testEmptyTextReturnsEmpty() {
        List<String> results = checker.check("");
        assertTrue(results.isEmpty());
    }

    @Test
    public void testNullTextReturnsEmpty() {
        List<String> results = checker.check(null);
        assertTrue(results.isEmpty());
    }

    @Test
    public void testDetectSpellingErrorInPhrase() {
        List<String> results = checker.check("This is a bcdfg word");
        assertFalse("\u5E94\u68C0\u6D4B\u5230\u62FC\u5199\u9519\u8BEF", results.isEmpty());
        boolean foundBadWord = false;
        for (String r : results) {
            if (r.contains("bcdfg")) {
                foundBadWord = true;
                break;
            }
        }
        assertTrue("\u5E94\u5305\u542B bcdfg \u9519\u8BEF\u63D0\u793A", foundBadWord);
    }

    @Test
    public void testMultipleErrors() {
        List<String> results = checker.check("abcdfg xyzpqr");
        assertEquals(2, results.size());
    }
}
