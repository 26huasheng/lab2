package plugin.text.command;

import core.command.AbstractCommand;
import plugin.text.TextEditor;

public class TxtInsertCommand extends AbstractCommand {

    private final TextEditor editor;

    private final int line;

    private final int col;

    private final String text;

    public TxtInsertCommand(TextEditor editor, int line, int col, String text) {
        this.editor = editor;
        this.line = line;
        this.col = col;
        this.text = text;
    }

    @Override
    public void execute() {
        editor.insert(line, col, text);
    }

    @Override
    public void undo() {
        int totalLength = text.length();
        editor.delete(line, col, totalLength);
    }

    @Override
    public String getCommandLog() {
        return timestamp + " insert " + line + ":" + col + " \"" + text + "\"";
    }
}
