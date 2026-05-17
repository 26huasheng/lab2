package plugin.text;

import java.util.HashSet;
import java.util.Set;

import command.ICommand;
import core.IFileSystem;
import core.plugin.IEditorPlugin;
import editor.IEditor;
import plugin.text.command.TxtAppendCommand;
import plugin.text.command.TxtDeleteCommand;
import plugin.text.command.TxtInsertCommand;
import plugin.text.command.TxtReplaceCommand;
import plugin.text.command.TxtShowCommand;

public class TextPlugin implements IEditorPlugin {

    private static final Set<String> SUPPORTED_COMMANDS = new HashSet<>();

    static {
        SUPPORTED_COMMANDS.add("append");
        SUPPORTED_COMMANDS.add("insert");
        SUPPORTED_COMMANDS.add("delete");
        SUPPORTED_COMMANDS.add("replace");
        SUPPORTED_COMMANDS.add("show");
    }

    @Override
    public String getSupportedExtension() {
        return ".txt";
    }

    @Override
    public IEditor createEditor(String filePath, IFileSystem fs) {
        TextEditor editor = new TextEditor(filePath, fs);
        java.util.List<String> lines = fs.readLines(filePath);
        for (String line : lines) {
            editor.append(line);
        }
        editor.setModified(false);
        return editor;
    }

    @Override
    public IEditor createEmptyEditor(String filePath, IFileSystem fs, boolean withLog) {
        TextEditor editor = new TextEditor(filePath, fs);
        if (withLog) {
            editor.append("# log");
            editor.setModified(true);
        }
        return editor;
    }

    @Override
    public boolean supportsCommand(String commandName) {
        return SUPPORTED_COMMANDS.contains(commandName.toLowerCase());
    }

    @Override
    public Set<String> getSupportedCommands() {
        return new java.util.HashSet<>(SUPPORTED_COMMANDS);
    }

    @Override
    public ICommand createCommand(String cmdName, String[] args, IEditor editor) {
        TextEditor textEditor = (TextEditor) editor;
        String cmd = cmdName.toLowerCase();

        switch (cmd) {
            case "append":
                if (args.length < 1) {
                    throw new IllegalArgumentException("\u7528\u6CD5: append \"text\"");
                }
                return new TxtAppendCommand(textEditor, args[0]);

            case "insert":
                if (args.length < 2) {
                    throw new IllegalArgumentException("\u7528\u6CD5: insert <line:col> \"text\"");
                }
                String[] insertPos = args[0].split(":");
                if (insertPos.length != 2) {
                    throw new IllegalArgumentException("\u4F4D\u7F6E\u683C\u5F0F\u9519\u8BEF\uFF0C\u5E94\u4E3A line:col");
                }
                int insertLine = Integer.parseInt(insertPos[0]);
                int insertCol = Integer.parseInt(insertPos[1]);
                return new TxtInsertCommand(textEditor, insertLine, insertCol, args[1]);

            case "delete":
                if (args.length < 2) {
                    throw new IllegalArgumentException("\u7528\u6CD5: delete <line:col> <len>");
                }
                String[] deletePos = args[0].split(":");
                if (deletePos.length != 2) {
                    throw new IllegalArgumentException("\u4F4D\u7F6E\u683C\u5F0F\u9519\u8BEF\uFF0C\u5E94\u4E3A line:col");
                }
                int deleteLine = Integer.parseInt(deletePos[0]);
                int deleteCol = Integer.parseInt(deletePos[1]);
                int deleteLen = Integer.parseInt(args[1]);
                return new TxtDeleteCommand(textEditor, deleteLine, deleteCol, deleteLen);

            case "replace":
                if (args.length < 3) {
                    throw new IllegalArgumentException("\u7528\u6CD5: replace <line:col> <len> \"text\"");
                }
                String[] replacePos = args[0].split(":");
                if (replacePos.length != 2) {
                    throw new IllegalArgumentException("\u4F4D\u7F6E\u683C\u5F0F\u9519\u8BEF\uFF0C\u5E94\u4E3A line:col");
                }
                int replaceLine = Integer.parseInt(replacePos[0]);
                int replaceCol = Integer.parseInt(replacePos[1]);
                int replaceLen = Integer.parseInt(args[1]);
                return new TxtReplaceCommand(textEditor, replaceLine, replaceCol, replaceLen, args[2]);

            case "show":
                int showStart = 1;
                int showEnd = textEditor.getLineCount();
                if (args.length >= 1 && !args[0].isEmpty()) {
                    String[] showRange = args[0].split(":");
                    if (showRange.length == 2) {
                        showStart = Integer.parseInt(showRange[0]);
                        showEnd = Integer.parseInt(showRange[1]);
                    } else {
                        showStart = Integer.parseInt(args[0]);
                    }
                }
                return new TxtShowCommand(textEditor, showStart, showEnd);

            default:
                throw new IllegalArgumentException("\u4E0D\u652F\u6301\u7684\u547D\u4EE4: " + cmdName);
        }
    }
}
