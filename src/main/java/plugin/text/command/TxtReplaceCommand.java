package plugin.text.command;

import core.command.AbstractCommand;
import plugin.text.TextEditor;

public class TxtReplaceCommand extends AbstractCommand {

    private final TxtDeleteCommand deleteCommand;

    private final TxtInsertCommand insertCommand;

    private final String newText;

    public TxtReplaceCommand(TextEditor editor, int line, int col, int len, String text) {
        this.newText = text;
        this.deleteCommand = new TxtDeleteCommand(editor, line, col, len);
        this.insertCommand = new TxtInsertCommand(editor, line, col, text);
    }

    @Override
    public void execute() {
        deleteCommand.execute();
        insertCommand.execute();
    }

    @Override
    public void undo() {
        insertCommand.undo();
        deleteCommand.undo();
    }

    @Override
    public String getCommandLog() {
        return timestamp + " replace " + deleteCommand.line + ":" + deleteCommand.col + " " + deleteCommand.len + " \"" + newText + "\"";
    }
}
