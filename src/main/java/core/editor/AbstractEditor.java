package core.editor;

import java.util.List;

import command.CommandManager;
import core.EditorException;
import core.IFileSystem;
import editor.IEditor;

public abstract class AbstractEditor implements IEditor {

    protected String filePath;

    protected boolean isModified = false;

    protected CommandManager commandManager = new CommandManager();

    protected IFileSystem fs;

    @Override
    public boolean isModified() {
        return isModified;
    }

    @Override
    public void setModified(boolean modified) {
        this.isModified = modified;
    }

    @Override
    public String getFilePath() {
        return filePath;
    }

    @Override
    public CommandManager getCommandManager() {
        return commandManager;
    }

    @Override
    public void save() {
        if (filePath == null || fs == null) {
            throw new EditorException("\u6587\u4EF6\u8DEF\u5F84\u672A\u8BBE\u7F6E\uFF0C\u65E0\u6CD5\u4FDD\u5B58");
        }
        fs.writeLines(filePath, serialize());
        this.isModified = false;
    }

    protected abstract List<String> serialize();
}
