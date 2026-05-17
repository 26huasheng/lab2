import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import core.EditorException;
import plugin.xml.XmlEditor;
import plugin.xml.command.XmlAppendChildCommand;
import plugin.xml.command.XmlDeleteCommand;
import plugin.xml.command.XmlInsertBeforeCommand;
import plugin.xml.core.IXmlNode;

public class XmlEditorCommandTest {

    private XmlEditor editor;

    @Before
    public void setUp() {
        editor = new XmlEditor();
        editor.initializeEmpty();
    }

    @Test
    public void testAppendChildAndDeleteUndo() {
        XmlAppendChildCommand appendCmd = new XmlAppendChildCommand(editor, "book", "book1", "root", "My Book");
        appendCmd.execute();

        IXmlNode book1 = editor.findElementById("book1");
        assertNotNull("\u5143\u7D20\u5E94\u88AB\u521B\u5EFA", book1);
        assertEquals("book", book1.getTagName());
        assertEquals("My Book", book1.getText());

        appendCmd.undo();

        assertNull("\u64A4\u9500\u540E\u5143\u7D20\u5E94\u88AB\u5220\u9664", editor.findElementById("book1"));
    }

    @Test
    public void testInsertBeforeAndUndo() {
        XmlAppendChildCommand appendCmd = new XmlAppendChildCommand(editor, "book", "book1", "root", "");
        appendCmd.execute();

        assertNotNull(editor.findElementById("book1"));

        XmlInsertBeforeCommand insertCmd = new XmlInsertBeforeCommand(editor, "book", "book2", "book1", "Second Book");
        insertCmd.execute();

        IXmlNode book2 = editor.findElementById("book2");
        assertNotNull("\u63D2\u5165\u7684\u5143\u7D20\u5E94\u5B58\u5728", book2);
        assertEquals("Second Book", book2.getText());

        IXmlNode root = editor.getRoot();
        assertEquals(2, root.getChildren().size());
        assertEquals("book2", root.getChildren().get(0).getId());

        insertCmd.undo();

        assertNull("\u64A4\u9500\u540E book2 \u5E94\u88AB\u5220\u9664", editor.findElementById("book2"));
        assertEquals(1, root.getChildren().size());
    }

    @Test
    public void testDeleteCommandWithDeepUndo() {
        XmlAppendChildCommand appendBook1 = new XmlAppendChildCommand(editor, "book", "book1", "root", "");
        appendBook1.execute();

        XmlAppendChildCommand appendChild1 = new XmlAppendChildCommand(editor, "title", "title1", "book1", "Harry Potter");
        appendChild1.execute();

        XmlAppendChildCommand appendChild2 = new XmlAppendChildCommand(editor, "author", "author1", "book1", "J K. Rowling");
        appendChild2.execute();

        assertNotNull(editor.findElementById("book1"));
        assertNotNull(editor.findElementById("title1"));
        assertNotNull(editor.findElementById("author1"));

        XmlDeleteCommand deleteCmd = new XmlDeleteCommand(editor, "book1");
        deleteCmd.execute();

        assertNull("\u5220\u9664\u540E book1 \u4E0D\u5E94\u5B58\u5728", editor.findElementById("book1"));
        assertNull("\u5B50\u5143\u7D20 title1 \u4E5F\u5E94\u88AB\u79FB\u9664", editor.findElementById("title1"));
        assertNull("\u5B50\u5143\u7D20 author1 \u4E5F\u5E94\u88AB\u79FB\u9664", editor.findElementById("author1"));

        deleteCmd.undo();

        IXmlNode recoveredBook1 = editor.findElementById("book1");
        assertNotNull("\u64A4\u9500\u540E book1 \u5E94\u6062\u590D", recoveredBook1);

        IXmlNode recoveredTitle1 = editor.findElementById("title1");
        assertNotNull("\u64A4\u9500\u540E \u5B50\u5143\u7D20 title1 \u5E94\u6062\u590D", recoveredTitle1);
        assertEquals("Harry Potter", recoveredTitle1.getText());

        IXmlNode recoveredAuthor1 = editor.findElementById("author1");
        assertNotNull("\u64A4\u9500\u540E \u5B50\u5143\u7D20 author1 \u5E94\u6062\u590D", recoveredAuthor1);
        assertEquals("J K. Rowling", recoveredAuthor1.getText());

        IXmlNode root = editor.getRoot();
        assertTrue("\u6839\u5143\u7D20\u5E94\u5305\u542B\u6062\u590D\u7684 book1", root.getChildren().contains(recoveredBook1));
    }

    @Test
    public void testDeleteRootShouldThrow() {
        assertThrows(EditorException.class, () -> {
            editor.deleteElement("root");
        });
    }

    @Test
    public void testInsertBeforeRootShouldThrow() {
        assertThrows(EditorException.class, () -> {
            editor.insertBefore("book", "book1", "root", "");
        });
    }
}
