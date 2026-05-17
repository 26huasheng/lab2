package plugin.text.command;

import java.util.Iterator;

import core.command.AbstractCommand;
import plugin.text.TextEditor;

public class TxtShowCommand extends AbstractCommand {

    private final TextEditor editor;

    private final int startLine;

    private final int endLine;

    public TxtShowCommand(TextEditor editor, int startLine, int endLine) {
        this.editor = editor;
        this.startLine = startLine;
        this.endLine = endLine;
    }

    @Override
    public void execute() {
        Iterator<String> it = editor.getLineIterator(startLine, endLine);
        int lineNum = startLine;
        while (it.hasNext()) {
            System.out.println(lineNum + ": " + it.next());
            lineNum++;
        }
    }

    @Override
    public void undo() {
    }

    @Override
    public String getCommandLog() {
        return "";
    }
}
