import core.IFileSystem;
import core.MockFileSystem;
import core.plugin.PluginRegistry;
import log.FileLogger;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import plugin.text.TextPlugin;
import plugin.text.TextEditor;
import plugin.text.command.TxtAppendCommand;
import workspace.Workspace;

import static org.junit.Assert.*;

public class ObserverDecoupleTest {

    private MockFileSystem fs;

    private Workspace ws;

    @Before
    public void setUp() {
        PluginRegistry.register(new TextPlugin());
        fs = new MockFileSystem();
        ws = new Workspace(fs);
    }

    @Test
    public void testInitWithLogAndObserverTrigger() {
        ws.init("test.txt", true);

        TxtAppendCommand appendCmd = new TxtAppendCommand(
                (TextEditor) ws.getActiveEditor(), "Hello World");
        ws.executeEditorCommand(appendCmd);

        Map<String, List<String>> memoryDisk = fs.getMemoryDisk();

        assertTrue("\u65E5\u5FD7\u6587\u4EF6\u5E94\u88AB\u521B\u5EFA", memoryDisk.containsKey("test.txt.log"));

        List<String> logLines = memoryDisk.get("test.txt.log");

        assertFalse("\u65E5\u5FD7\u6587\u4EF6\u4E0D\u5E94\u4E3A\u7A7A", logLines.isEmpty());

        boolean hasSessionStart = false;
        boolean hasAppendAction = false;
        boolean hasTimestamp = false;

        for (String line : logLines) {
            if (line.contains("session start at")) {
                hasSessionStart = true;
            }
            if (line.contains("append")) {
                hasAppendAction = true;
            }
            if (line.matches("\\d{8} \\d{2}:\\d{2}:\\d{2}.*")) {
                hasTimestamp = true;
            }
        }

        assertTrue("\u65E5\u5FD7\u5E94\u5305\u542B session start \u6807\u8BB0", hasSessionStart);
        assertTrue("\u65E5\u5FD7\u5E94\u5305\u542B append \u52A8\u4F5C\u8BB0\u5F55", hasAppendAction);
        assertTrue("\u65E5\u5FD7\u5E94\u5305\u542B\u65F6\u95F4\u6233", hasTimestamp);
    }

    @Test
    public void testInitWithoutLog() {
        ws.init("test.txt", false);

        Map<String, List<String>> memoryDisk = fs.getMemoryDisk();

        assertFalse("\u65E0\u65E5\u5FD7\u6A21\u5F0F\u4E0D\u5E94\u521B\u5EFA\u65E5\u5FD7\u6587\u4EF6", memoryDisk.containsKey("test.txt.log"));
    }

    @Test
    public void testManualLogOn() {
        ws.init("test.txt", false);

        ws.executeEditorCommand(new TxtAppendCommand(
                (TextEditor) ws.getActiveEditor(), "First line"));

        assertFalse("\u672A\u5F00\u542F\u65E5\u5FD7\u65F6\u4E0D\u5E94\u6709\u65E5\u5FD7\u6587\u4EF6", fs.getMemoryDisk().containsKey("test.txt.log"));

        ws.executeEditorCommand(new TxtAppendCommand(
                (TextEditor) ws.getActiveEditor(), "Second line"));

        FileLogger logger = new FileLogger(fs, "test.txt.log");
        ws.attachObserver(logger);

        ws.executeEditorCommand(new TxtAppendCommand(
                (TextEditor) ws.getActiveEditor(), "Third line"));

        assertTrue("\u624B\u52A8\u5F00\u542F\u65E5\u5FD7\u540E\u5E94\u521B\u5EFA\u65E5\u5FD7\u6587\u4EF6", fs.getMemoryDisk().containsKey("test.txt.log"));
    }
}
