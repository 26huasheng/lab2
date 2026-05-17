package plugin.text.command;

import core.command.AbstractCommand;
import plugin.text.TextEditor;

public class TxtDeleteCommand extends AbstractCommand {

    private final TextEditor editor;

    public final int line;

    public final int col;

    public final int len;

    private String deletedText;

    public TxtDeleteCommand(TextEditor editor, int line, int col, int len) {
        this.editor = editor;
        this.line = line;
        this.col = col;
        this.len = len;
    }

    @Override
    public void execute() {
        deletedText = editor.delete(line, col, len);
    }

    @Override
    public void undo() {
        editor.insert(line, col, deletedText);
    }

    @Override
    public String getCommandLog() {
        return timestamp + " delete " + line + ":" + col + " len=" + len;
    }
}
