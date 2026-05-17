package editor;

import command.CommandManager;

public interface IEditor {

    boolean isModified();

    void setModified(boolean modified);

    String getFilePath();

    void save();

    String getPlainTextContent();

    CommandManager getCommandManager();
}
