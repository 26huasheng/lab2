import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import core.EditorException;
import core.IFileSystem;
import core.MockFileSystem;
import core.plugin.IEditorPlugin;
import core.plugin.PluginRegistry;
import plugin.sudoku.SudokuPlugin;
import plugin.text.TextPlugin;
import plugin.xml.XmlPlugin;
import workspace.Workspace;

public class PluginIsolationTest {

    private IFileSystem fs;

    private Workspace ws;

    @Before
    public void setUp() {
        PluginRegistry.register(new TextPlugin());
        PluginRegistry.register(new XmlPlugin());
        PluginRegistry.register(new SudokuPlugin());
        fs = new MockFileSystem();
        ws = new Workspace(fs);
    }

    @Test
    public void testTextRejectsXmlCommand() {
        ws.init("test.txt", false);

        IEditorPlugin plugin = PluginRegistry.detectPlugin("test.txt");
        assertNotNull(plugin);

        assertFalse("\u6587\u672C\u7C7B\u578B\u4E0D\u5E94\u652F\u6301 insert-before", plugin.supportsCommand("insert-before"));
    }

    @Test
    public void testXmlRejectsTextDeleteWithLineCol() {
        ws.init("test.xml", false);

        IEditorPlugin plugin = PluginRegistry.detectPlugin("test.xml");
        assertNotNull(plugin);

        assertFalse("\u6587\u672C\u7C7B\u578B\u4E0D\u5E94\u652F\u6301 append", plugin.supportsCommand("append"));
    }

    @Test
    public void testFileTypesRejectForeignCommands() {
        ws.init("test.txt", false);
        IEditorPlugin txtPlugin = PluginRegistry.detectPlugin("test.txt");
        assertFalse(txtPlugin.supportsCommand("set-number"));
        assertFalse(txtPlugin.supportsCommand("xml-tree"));

        ws.init("test.xml", false);
        IEditorPlugin xmlPlugin = PluginRegistry.detectPlugin("test.xml");
        assertFalse(xmlPlugin.supportsCommand("set-number"));
        assertFalse(xmlPlugin.supportsCommand("append"));
    }

    @Test
    public void testSudokuPluginRegistration() {
        IEditorPlugin plugin = PluginRegistry.detectPlugin("game.sdk");
        assertNotNull("\u5E94\u80FD\u8BC6\u522B .sdk \u6587\u4EF6", plugin);
        assertTrue("\u5E94\u652F\u6301 set-number", plugin.supportsCommand("set-number"));
        assertFalse("\u4E0D\u5E94\u652F\u6301 append", plugin.supportsCommand("append"));
    }

    @Test
    public void testSudokuInitAndSetNumber() {
        ws.init("game.sdk", false);

        IEditorPlugin plugin = PluginRegistry.detectPlugin("game.sdk");
        assertNotNull(plugin);

        assertTrue(plugin.supportsCommand("set-number"));

        plugin.createCommand("set-number", new String[]{"1", "1", "5"}, ws.getActiveEditor());
    }
}
