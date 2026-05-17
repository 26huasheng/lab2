package plugin.xml.command;

import core.command.AbstractCommand;
import plugin.xml.XmlEditor;

public class XmlInsertBeforeCommand extends AbstractCommand {

    private final XmlEditor editor;

    private final String tagName;

    private final String newId;

    private final String targetId;

    private final String text;

    public XmlInsertBeforeCommand(XmlEditor editor, String tagName, String newId, String targetId, String text) {
        this.editor = editor;
        this.tagName = tagName;
        this.newId = newId;
        this.targetId = targetId;
        this.text = text;
    }

    @Override
    public void execute() {
        editor.insertBefore(tagName, newId, targetId, text);
    }

    @Override
    public void undo() {
        editor.deleteElement(newId);
    }

    @Override
    public String getCommandLog() {
        return timestamp + " insert-before " + tagName + " " + newId + " " + targetId + " \"" + (text != null ? text : "") + "\"";
    }
}
