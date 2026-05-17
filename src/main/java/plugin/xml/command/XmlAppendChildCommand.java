package plugin.xml.command;

import core.command.AbstractCommand;
import plugin.xml.XmlEditor;

public class XmlAppendChildCommand extends AbstractCommand {

    private final XmlEditor editor;

    private final String tagName;

    private final String newId;

    private final String parentId;

    private final String text;

    public XmlAppendChildCommand(XmlEditor editor, String tagName, String newId, String parentId, String text) {
        this.editor = editor;
        this.tagName = tagName;
        this.newId = newId;
        this.parentId = parentId;
        this.text = text;
    }

    @Override
    public void execute() {
        editor.appendChild(tagName, newId, parentId, text);
    }

    @Override
    public void undo() {
        editor.deleteElement(newId);
    }

    @Override
    public String getCommandLog() {
        return timestamp + " append-child " + tagName + " " + newId + " " + parentId + " \"" + (text != null ? text : "") + "\"";
    }
}
