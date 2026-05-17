import command.CommandManager;
import core.EditorException;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import plugin.text.TextEditor;
import plugin.text.command.TxtAppendCommand;
import plugin.text.command.TxtInsertCommand;

import static org.junit.Assert.*;

public class CommandStateTest {

    private TextEditor editor;

    private CommandManager cmdManager;

    @Before
    public void setUp() {
        editor = new TextEditor();
        cmdManager = new CommandManager();
    }

    @Test
    public void testUndoRedoChain() {
        cmdManager.executeCommand(new TxtAppendCommand(editor, "Hello"), msg -> {});
        cmdManager.executeCommand(new TxtInsertCommand(editor, 2, 1, "World"), msg -> {});

        cmdManager.undo();
        cmdManager.undo();

        cmdManager.redo();

        List<String> result = new ArrayList<>();
        Iterator<String> it = editor.getLineIterator(1, 1);
        while (it.hasNext()) {
            result.add(it.next());
        }

        assertEquals(1, result.size());
        assertEquals("Hello", result.get(0));
    }

    @Test
    public void testRedoStackClearedOnNewCommand() {
        cmdManager.executeCommand(new TxtAppendCommand(editor, "Line1"), msg -> {});
        cmdManager.executeCommand(new TxtAppendCommand(editor, "Line2"), msg -> {});

        cmdManager.undo();

        cmdManager.executeCommand(new TxtAppendCommand(editor, "Line3"), msg -> {});

        assertThrows(EditorException.class, () -> {
            cmdManager.redo();
        });
    }

    @Test
    public void testUndoEmptyStack() {
        assertThrows(EditorException.class, () -> {
            cmdManager.undo();
        });
    }

    @Test
    public void testRedoEmptyStack() {
        assertThrows(EditorException.class, () -> {
            cmdManager.redo();
        });
    }

    @Test
    public void testMultipleUndoRestoresEmptyState() {
        cmdManager.executeCommand(new TxtAppendCommand(editor, "Line1"), msg -> {});
        cmdManager.executeCommand(new TxtAppendCommand(editor, "Line2"), msg -> {});

        cmdManager.undo();
        cmdManager.undo();

        assertEquals(0, getLineCount(editor));
    }

    private int getLineCount(TextEditor editor) {
        int count = 0;
        try {
            Iterator<String> it = editor.getLineIterator(1, Integer.MAX_VALUE);
            while (it.hasNext()) {
                it.next();
                count++;
            }
        } catch (Exception e) {
            return count;
        }
        return count;
    }

    @Test
    public void testDisplayCommandMustNotAffectUndo() {
        cmdManager.executeCommand(new TxtAppendCommand(editor, "Line1"), msg -> {});
        assertEquals(1, getLineCount(editor));

        cmdManager.executeCommand(new TxtAppendCommand(editor, "Line2"), msg -> {});
        assertEquals(2, getLineCount(editor));

        cmdManager.undo();
        assertEquals(1, getLineCount(editor));

        List<String> lines = new ArrayList<>();
        Iterator<String> it = editor.getLineIterator(1, 1);
        while (it.hasNext()) {
            lines.add(it.next());
        }
        assertEquals("Line1", lines.get(0));
    }

    @Test
    public void testUndoAfterEditMustWorkWithoutDisplayPollution() {
        cmdManager.executeCommand(new TxtAppendCommand(editor, "Important"), msg -> {});
        assertEquals(1, getLineCount(editor));

        cmdManager.undo();
        assertEquals(0, getLineCount(editor));
    }
}
