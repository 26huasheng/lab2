package plugin.xml.command;

import core.command.AbstractCommand;
import plugin.xml.XmlEditor;

public class XmlTreeCommand extends AbstractCommand {

    private final XmlEditor editor;

    public XmlTreeCommand(XmlEditor editor) {
        this.editor = editor;
    }

    @Override
    public void execute() {
        String tree = editor.getXmlTreeString();
        if (tree.isEmpty()) {
            System.out.println("\u7A7A\u6587\u6863");
        } else {
            System.out.print(tree);
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
