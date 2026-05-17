package spellcheck;

import java.util.List;

import core.command.AbstractCommand;
import editor.IEditor;

public class SpellCheckCommand extends AbstractCommand {

    private final IEditor editor;

    private final ISpellChecker checker;

    public SpellCheckCommand(IEditor editor, ISpellChecker checker) {
        this.editor = editor;
        this.checker = checker;
    }

    @Override
    public void execute() {
        String content = editor.getPlainTextContent();
        if (content == null || content.isEmpty()) {
            System.out.println("\u6587\u6863\u4E3A\u7A7A\uFF0C\u65E0\u9700\u62FC\u5199\u68C0\u67E5");
            return;
        }

        List<String> errors = checker.check(content);
        if (errors.isEmpty()) {
            System.out.println("\u672A\u53D1\u73B0\u62FC\u5199\u9519\u8BEF");
            return;
        }

        System.out.println("\u62FC\u5199\u68C0\u67E5\u7ED3\u679C:");
        for (String error : errors) {
            System.out.println(error);
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
