import core.EditorException;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import plugin.text.TextEditor;

import static org.junit.Assert.*;

public class TextEditorBoundaryTest {

    private TextEditor editor;

    @Before
    public void setUp() {
        editor = new TextEditor();
    }

    @Test
    public void testDeleteOutOfBounds() {
        assertThrows(EditorException.class, () -> {
            editor.delete(1, 1, 5);
        });
    }

    @Test
    public void testInsertMultiline() {
        editor.insert(1, 1, "A\nB");

        List<String> result = new ArrayList<>();
        Iterator<String> it = editor.getLineIterator(1, 2);
        while (it.hasNext()) {
            result.add(it.next());
        }

        assertEquals(2, result.size());
        assertEquals("A", result.get(0));
        assertEquals("B", result.get(1));
    }

    @Test
    public void testInsertInEmptyFileAtWrongPosition() {
        assertThrows(EditorException.class, () -> {
            editor.insert(2, 1, "text");
        });

        assertThrows(EditorException.class, () -> {
            editor.insert(1, 2, "text");
        });
    }

    @Test
    public void testColumnOutOfBounds() {
        editor.insert(1, 1, "Hello");

        assertThrows(EditorException.class, () -> {
            editor.insert(1, 7, "X");
        });

        assertThrows(EditorException.class, () -> {
            editor.delete(1, 7, 1);
        });
    }

    @Test
    public void testDeleteLengthExceedsLineEnd() {
        editor.insert(1, 1, "Hello");

        assertThrows(EditorException.class, () -> {
            editor.delete(1, 3, 10);
        });
    }

    @Test
    public void testLineOutOfBounds() {
        editor.insert(1, 1, "Hello");

        assertThrows(EditorException.class, () -> {
            editor.insert(3, 1, "X");
        });

        assertThrows(EditorException.class, () -> {
            editor.delete(3, 1, 1);
        });
    }
}
