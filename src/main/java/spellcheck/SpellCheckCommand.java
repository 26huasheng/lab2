package spellcheck;

import java.util.List;

import core.command.AbstractCommand;
import editor.IEditor;
import plugin.text.TextEditor;
import plugin.xml.XmlEditor;
import plugin.xml.core.IXmlNode;

public class SpellCheckCommand extends AbstractCommand {

    private final IEditor editor;

    private final ISpellChecker checker;

    public SpellCheckCommand(IEditor editor, ISpellChecker checker) {
        this.editor = editor;
        this.checker = checker;
    }

    @Override
    public void execute() {
        if (editor instanceof TextEditor) {
            checkTextEditor((TextEditor) editor);
        } else if (editor instanceof XmlEditor) {
            checkXmlEditor((XmlEditor) editor);
        } else {
            System.out.println("当前文件类型不支持拼写检查");
        }
    }

    private void checkTextEditor(TextEditor textEditor) {
        List<String> lines = textEditor.getLines();
        if (lines.isEmpty()) {
            System.out.println("文档为空，无需拼写检查");
            return;
        }

        boolean foundAny = false;
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            List<SpellError> errors = checker.check(line);
            for (SpellError error : errors) {
                if (!foundAny) {
                    System.out.println("拼写检查结果:");
                    foundAny = true;
                }
                int col = line.indexOf(error.getWord()) + 1;
                System.out.println("第" + (i + 1) + "行，第" + col + "列: \"" + error.getWord()
                        + "\" -> 建议: " + error.getSuggestion());
            }
        }

        if (!foundAny) {
            System.out.println("未发现拼写错误");
        }
    }

    private void checkXmlEditor(XmlEditor xmlEditor) {
        IXmlNode root = xmlEditor.getRoot();
        if (root == null) {
            System.out.println("文档为空，无需拼写检查");
            return;
        }

        boolean foundAny = false;
        foundAny = collectXmlErrors(root, foundAny);

        if (!foundAny) {
            System.out.println("未发现拼写错误");
        }
    }

    private boolean collectXmlErrors(IXmlNode node, boolean foundAny) {
        String text = node.getText();
        if (text != null && !text.isEmpty()) {
            List<SpellError> errors = checker.check(text);
            for (SpellError error : errors) {
                if (!foundAny) {
                    System.out.println("拼写检查结果:");
                    foundAny = true;
                }
                System.out.println("元素 " + node.getId() + ": \"" + error.getWord()
                        + "\" -> 建议: " + error.getSuggestion());
            }
        }
        for (IXmlNode child : node.getChildren()) {
            foundAny = collectXmlErrors(child, foundAny);
        }
        return foundAny;
    }

    @Override
    public void undo() {
    }

    @Override
    public String getCommandLog() {
        return "";
    }
}
