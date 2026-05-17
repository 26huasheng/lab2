package plugin.xml.command;

import core.command.AbstractCommand;
import plugin.xml.XmlEditor;
import plugin.xml.core.IXmlNode;

public class XmlDeleteCommand extends AbstractCommand {

    private final XmlEditor editor;

    private final String elementId;

    private IXmlNode deletedSubtreeSnapshot;

    private String parentId;

    private int childIndex;

    public XmlDeleteCommand(XmlEditor editor, String elementId) {
        this.editor = editor;
        this.elementId = elementId;
    }

    @Override
    public void execute() {
        IXmlNode element = editor.findElementById(elementId);
        if (element == null) {
            throw new RuntimeException("\u5143\u7D20\u4E0D\u5B58\u5728: " + elementId);
        }
        if (element.getParent() == null) {
            throw new RuntimeException("\u4E0D\u80FD\u5220\u9664\u6839\u5143\u7D20");
        }

        this.parentId = element.getParent().getId();
        this.childIndex = element.getParent().getChildren().indexOf(element);

        this.deletedSubtreeSnapshot = element.deepClone();

        editor.deleteElement(elementId);
    }

    @Override
    public void undo() {
        if (deletedSubtreeSnapshot != null && parentId != null) {
            editor.restoreNode(parentId, childIndex, deletedSubtreeSnapshot);
        }
    }

    @Override
    public String getCommandLog() {
        return timestamp + " delete " + elementId;
    }
}
