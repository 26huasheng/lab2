import static org.junit.Assert.*;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import spellcheck.ISpellChecker;
import spellcheck.MockSpellCheckerAdapter;
import spellcheck.SpellError;

public class SpellCheckAdapterTest {

    private ISpellChecker checker;

    @Before
    public void setUp() {
        checker = new MockSpellCheckerAdapter();
    }

    @Test
    public void testDetectConsecutiveConsonants() {
        List<SpellError> results = checker.check("abcdfg");
        assertFalse("应检测到拼写错误", results.isEmpty());
        assertTrue(results.get(0).getWord().contains("abcdfg"));
    }

    @Test
    public void testValidWordPasses() {
        List<SpellError> results = checker.check("hello");
        assertTrue("正确单词应通过", results.isEmpty());
    }

    @Test
    public void testEmptyTextReturnsEmpty() {
        List<SpellError> results = checker.check("");
        assertTrue(results.isEmpty());
    }

    @Test
    public void testNullTextReturnsEmpty() {
        List<SpellError> results = checker.check(null);
        assertTrue(results.isEmpty());
    }

    @Test
    public void testDetectSpellingErrorInPhrase() {
        List<SpellError> results = checker.check("This is a bcdfg word");
        assertFalse("应检测到拼写错误", results.isEmpty());
        boolean foundBadWord = false;
        for (SpellError r : results) {
            if (r.getWord().contains("bcdfg")) {
                foundBadWord = true;
                break;
            }
        }
        assertTrue("应包含 bcdfg 错误提示", foundBadWord);
    }

    @Test
    public void testMultipleErrors() {
        List<SpellError> results = checker.check("abcdfg xyzpqr");
        assertEquals(2, results.size());
    }
}
