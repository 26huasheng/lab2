package plugin.text.command;

import core.command.AbstractCommand;
import plugin.text.TextEditor;

public class TxtAppendCommand extends AbstractCommand {

    private final TextEditor editor;

    private final String text;

    private int appendedLineCount;

    public TxtAppendCommand(TextEditor editor, String text) {
        this.editor = editor;
        this.text = text;
        this.appendedLineCount = text.contains("\n") ? text.split("\n", -1).length : 1;
    }

    @Override
    public void execute() {
        editor.append(text);
    }

    @Override
    public void undo() {
        for (int i = 0; i < appendedLineCount; i++) {
            int lineCount = editor.getLineCount();
            if (lineCount > 0) {
                String lastLine = editor.getLineIterator(lineCount, lineCount).next();
                editor.delete(lineCount, 1, lastLine.length());
            }
        }
    }

    @Override
    public String getCommandLog() {
        return timestamp + " append \"" + text + "\"";
    }
}
