package plugin.xml.command;

import core.command.AbstractCommand;
import plugin.xml.XmlEditor;

public class XmlEditIdCommand extends AbstractCommand {

    private final XmlEditor editor;

    private final String oldId;

    private final String newId;

    public XmlEditIdCommand(XmlEditor editor, String oldId, String newId) {
        this.editor = editor;
        this.oldId = oldId;
        this.newId = newId;
    }

    @Override
    public void execute() {
        editor.editId(oldId, newId);
    }

    @Override
    public void undo() {
        editor.editId(newId, oldId);
    }

    @Override
    public String getCommandLog() {
        return timestamp + " edit-id " + oldId + " " + newId;
    }
}
