package plugin.xml.command;

import core.command.AbstractCommand;
import plugin.xml.XmlEditor;
import plugin.xml.core.IXmlNode;

public class XmlEditTextCommand extends AbstractCommand {

    private final XmlEditor editor;

    private final String elementId;

    private final String newText;

    private String oldText;

    public XmlEditTextCommand(XmlEditor editor, String elementId, String newText) {
        this.editor = editor;
        this.elementId = elementId;
        this.newText = newText;
    }

    @Override
    public void execute() {
        IXmlNode element = editor.findElementById(elementId);
        if (element != null) {
            this.oldText = element.getText() != null ? element.getText() : "";
        }
        editor.editText(elementId, newText);
    }

    @Override
    public void undo() {
        editor.editText(elementId, oldText);
    }

    @Override
    public String getCommandLog() {
        return timestamp + " edit-text " + elementId + " \"" + (newText != null ? newText : "") + "\"";
    }
}
