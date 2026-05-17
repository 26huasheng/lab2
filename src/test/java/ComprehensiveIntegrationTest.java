import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import command.CommandManager;
import core.IFileSystem;
import core.MockFileSystem;
import core.plugin.IEditorPlugin;
import core.plugin.PluginRegistry;
import editor.IEditor;
import plugin.sudoku.SudokuPlugin;
import plugin.text.TextPlugin;
import plugin.text.command.TxtAppendCommand;
import plugin.text.command.TxtDeleteCommand;
import plugin.text.command.TxtInsertCommand;
import plugin.text.command.TxtReplaceCommand;
import plugin.text.command.TxtShowCommand;
import plugin.xml.XmlPlugin;
import plugin.xml.command.XmlAppendChildCommand;
import plugin.xml.command.XmlDeleteCommand;
import plugin.xml.command.XmlEditIdCommand;
import plugin.xml.command.XmlEditTextCommand;
import plugin.xml.command.XmlInsertBeforeCommand;
import spellcheck.MockSpellCheckerAdapter;
import statistics.SessionStatsObserver;
import workspace.Workspace;

public class ComprehensiveIntegrationTest {

    private IFileSystem fs;

    private Workspace ws;

    @BeforeClass
    public static void registerAllPlugins() {
        PluginRegistry.register(new TextPlugin());
        PluginRegistry.register(new XmlPlugin());
        PluginRegistry.register(new SudokuPlugin());
    }

    @Before
    public void setUp() {
        fs = new MockFileSystem();
        ws = new Workspace(fs);
    }

    @Test
    public void testHelpAvailable() {
        assertNotNull(PluginRegistry.detectPlugin("test.txt"));
        assertNotNull(PluginRegistry.detectPlugin("test.xml"));
        assertNotNull(PluginRegistry.detectPlugin("test.sdk"));
    }

    @Test
    public void testTextInitWithLog() {
        ws.init("test.txt", true);
        assertNotNull(ws.getActiveEditor());
        assertEquals("test.txt", ws.getActiveFilePath());
        assertTrue(ws.getActiveEditor().isModified());
    }

    @Test
    public void testXmlInit() {
        ws.init("test.xml", false);
        assertNotNull(ws.getActiveEditor());
        String content = ws.getActiveEditor().getPlainTextContent();
        assertNotNull(content);
    }

    @Test
    public void testTextSaveAndLoad() {
        ws.init("test.txt", false);
        IEditor editor = ws.getActiveEditor();
        TxtAppendCommand cmd = new TxtAppendCommand((plugin.text.TextEditor) editor, "Hello World");
        ws.executeEditorCommand(cmd);

        ws.save("test.txt");
        assertFalse(editor.isModified());

        ws.load("test.txt");
        String content = ws.getActiveEditor().getPlainTextContent();
        assertTrue(content.contains("Hello World"));
    }

    @Test
    public void testXmlSaveAndLoad() {
        ws.init("test.xml", false);
        IEditor editor = ws.getActiveEditor();
        XmlAppendChildCommand cmd = new XmlAppendChildCommand(
                (plugin.xml.XmlEditor) editor, "book", "book1", "root", "Test Book");
        ws.executeEditorCommand(cmd);

        ws.save("test.xml");
        assertFalse(editor.isModified());
    }

    @Test
    public void testTextAppendInsertDeleteReplaceShow() {
        ws.init("test.txt", false);
        plugin.text.TextEditor editor = (plugin.text.TextEditor) ws.getActiveEditor();

        TxtAppendCommand appendCmd = new TxtAppendCommand(editor, "Line1");
        ws.executeEditorCommand(appendCmd);
        ws.executeEditorCommand(new TxtAppendCommand(editor, "Line2"));
        ws.executeEditorCommand(new TxtAppendCommand(editor, "Line3"));

        ws.executeEditorCommand(new TxtInsertCommand(editor, 3, 1, "Inserted"));

        ws.executeEditorCommand(new TxtReplaceCommand(editor, 3, 1, 8, "Replaced"));

        String content = editor.getPlainTextContent();
        assertTrue(content.contains("Replaced"));

        ws.executeEditorCommand(new TxtDeleteCommand(editor, 2, 1, 5));

        ws.executeEditorCommand(new TxtShowCommand(editor, 1, 1));
        assertNotNull(content);
    }

    @Test
    public void testTextUndoRedo() {
        ws.init("test.txt", false);
        plugin.text.TextEditor editor = (plugin.text.TextEditor) ws.getActiveEditor();
        CommandManager cmdManager = ws.getActiveEditor().getCommandManager();

        ws.executeEditorCommand(new TxtAppendCommand(editor, "Line1"));
        ws.executeEditorCommand(new TxtAppendCommand(editor, "Line2"));

        cmdManager.undo();
        String content = editor.getPlainTextContent();
        assertFalse(content.contains("Line2"));

        cmdManager.redo();
        content = editor.getPlainTextContent();
        assertTrue(content.contains("Line2"));
    }

    @Test
    public void testXmlInsertBeforeAndUndo() {
        ws.init("test.xml", false);
        plugin.xml.XmlEditor editor = (plugin.xml.XmlEditor) ws.getActiveEditor();

        ws.executeEditorCommand(new XmlAppendChildCommand(editor, "book", "book1", "root", "First"));
        ws.executeEditorCommand(new XmlAppendChildCommand(editor, "book", "book2", "root", "Second"));

        ws.executeEditorCommand(new XmlInsertBeforeCommand(editor, "book", "book1b", "book1", "Before first"));

        assertEquals(3, editor.getRoot().getChildren().size());
        assertEquals("book1b", editor.getRoot().getChildren().get(0).getId());

        editor.getCommandManager().undo();
        assertNull(editor.findElementById("book1b"));
        assertEquals(2, editor.getRoot().getChildren().size());
    }

    @Test
    public void testXmlAppendChildAndDeleteUndo() {
        ws.init("test.xml", false);
        plugin.xml.XmlEditor editor = (plugin.xml.XmlEditor) ws.getActiveEditor();

        ws.executeEditorCommand(new XmlAppendChildCommand(editor, "book", "book1", "root", "Test"));

        assertNotNull(editor.findElementById("book1"));

        ws.executeEditorCommand(new XmlAppendChildCommand(editor, "title", "title1", "book1", "Title"));
        ws.executeEditorCommand(new XmlAppendChildCommand(editor, "author", "author1", "book1", "Author"));

        ws.executeEditorCommand(new XmlDeleteCommand(editor, "book1"));

        assertNull(editor.findElementById("book1"));
        assertNull(editor.findElementById("title1"));

        editor.getCommandManager().undo();

        assertNotNull(editor.findElementById("book1"));
        assertNotNull(editor.findElementById("title1"));
        assertNotNull(editor.findElementById("author1"));
    }

    @Test
    public void testXmlEditId() {
        ws.init("test.xml", false);
        plugin.xml.XmlEditor editor = (plugin.xml.XmlEditor) ws.getActiveEditor();

        ws.executeEditorCommand(new XmlAppendChildCommand(editor, "book", "book1", "root", ""));

        ws.executeEditorCommand(new XmlEditIdCommand(editor, "book1", "book001"));

        assertNull(editor.findElementById("book1"));
        assertNotNull(editor.findElementById("book001"));

        editor.getCommandManager().undo();
        assertNotNull(editor.findElementById("book1"));
        assertNull(editor.findElementById("book001"));
    }

    @Test
    public void testXmlEditText() {
        ws.init("test.xml", false);
        plugin.xml.XmlEditor editor = (plugin.xml.XmlEditor) ws.getActiveEditor();

        ws.executeEditorCommand(new XmlAppendChildCommand(editor, "book", "book1", "root", "Original"));

        ws.executeEditorCommand(new XmlEditTextCommand(editor, "book1", "Updated"));

        assertEquals("Updated", editor.findElementById("book1").getText());

        editor.getCommandManager().undo();
        assertEquals("Original", editor.findElementById("book1").getText());
    }

    @Test
    public void testXmlTreeOutput() {
        ws.init("test.xml", false);
        plugin.xml.XmlEditor editor = (plugin.xml.XmlEditor) ws.getActiveEditor();

        ws.executeEditorCommand(new XmlAppendChildCommand(editor, "book", "book1", "root", "Book1"));

        String tree = editor.getXmlTreeString();
        assertNotNull(tree);
        assertTrue(tree.contains("book1"));
    }

    @Test
    public void testCloseAndEdit() {
        ws.init("test.txt", false);
        ws.save("test.txt");

        ws.init("test.xml", false);
        ws.save("test.xml");
        assertEquals("test.xml", ws.getActiveFilePath());

        ws.edit("test.txt");
        assertEquals("test.txt", ws.getActiveFilePath());

        ws.close("test.xml");
        assertNull(ws.getEditor("test.xml"));
    }

    @Test
    public void testEditorList() {
        ws.init("file1.txt", false);
        ws.save("file1.txt");
        ws.init("file2.xml", false);

        assertEquals(2, ws.getOpenedFiles().size());
        assertTrue(ws.getOpenedFiles().contains("file1.txt"));
        assertTrue(ws.getOpenedFiles().contains("file2.xml"));
    }

    @Test
    public void testLogOnOffShow() {
        ws.init("test.txt", true);
        plugin.text.TextEditor editor = (plugin.text.TextEditor) ws.getActiveEditor();
        ws.executeEditorCommand(new TxtAppendCommand(editor, "Hello"));

        MockFileSystem mockFs = (MockFileSystem) fs;
        assertTrue(mockFs.getMemoryDisk().containsKey("test.txt.log"));
    }

    @Test
    public void testSpellCheckViaAdapter() {
        ws.init("test.txt", false);
        plugin.text.TextEditor editor = (plugin.text.TextEditor) ws.getActiveEditor();
        ws.executeEditorCommand(new TxtAppendCommand(editor, "abcdfg"));

        MockSpellCheckerAdapter checker = new MockSpellCheckerAdapter();
        String content = editor.getPlainTextContent();
        assertFalse("\u5E94\u68C0\u6D4B\u5230\u62FC\u5199\u9519\u8BEF", checker.check(content).isEmpty());
    }

    @Test
    public void testSessionStats() {
        SessionStatsObserver observer = new SessionStatsObserver();
        ws.attachWorkspaceObserver(observer);

        ws.init("test.txt", false);
        IEditor editor = ws.getActiveEditor();

        observer.onFileDeactivated(editor);
        observer.onFileActivated(editor);

        long duration = observer.getDuration(editor);
        String formatted = observer.format(duration);
        assertNotNull(formatted);
    }

    @Test
    public void testPluginSecurityIsolation() {
        IEditorPlugin txtPlugin = PluginRegistry.detectPlugin("test.txt");
        IEditorPlugin xmlPlugin = PluginRegistry.detectPlugin("test.xml");
        IEditorPlugin sdkPlugin = PluginRegistry.detectPlugin("test.sdk");

        assertFalse(txtPlugin.supportsCommand("insert-before"));
        assertFalse(txtPlugin.supportsCommand("xml-tree"));
        assertTrue(txtPlugin.supportsCommand("append"));

        assertFalse(xmlPlugin.supportsCommand("append"));
        assertTrue(xmlPlugin.supportsCommand("insert-before"));
        assertTrue(xmlPlugin.supportsCommand("xml-tree"));

        assertFalse(sdkPlugin.supportsCommand("append"));
        assertFalse(sdkPlugin.supportsCommand("insert-before"));
        assertTrue(sdkPlugin.supportsCommand("set-number"));
    }

    @Test
    public void testSudokuInitAndSetNumber() {
        ws.init("game.sdk", false);
        plugin.sudoku.SudokuEditor editor = (plugin.sudoku.SudokuEditor) ws.getActiveEditor();

        ws.executeEditorCommand(new plugin.sudoku.SudokuSetCommand(editor, 1, 1, 5));
        assertEquals(5, editor.getNumber(1, 1));

        editor.getCommandManager().undo();
        assertEquals(0, editor.getNumber(1, 1));
    }

    @Test
    public void testEditorExceptionOnInvalidOperation() {
        ws.init("test.txt", false);
        IEditorPlugin plugin = PluginRegistry.detectPlugin("test.txt");
        assertNotNull(plugin);
        assertFalse(plugin.supportsCommand("insert-before"));

        ws.init("test.xml", false);
        plugin = PluginRegistry.detectPlugin("test.xml");
        assertNotNull(plugin);
        assertFalse(plugin.supportsCommand("append"));
    }

    @Test
    public void testSaveAll() {
        ws.init("file1.txt", false);
        ws.save("file1.txt");
        ws.init("file2.xml", false);

        ws.saveAll();
        assertFalse(ws.getEditor("file1.txt").isModified());
        assertFalse(ws.getEditor("file2.xml").isModified());
    }
}
