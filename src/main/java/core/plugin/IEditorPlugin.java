package core.plugin;

import java.util.Set;

import command.ICommand;
import core.IFileSystem;
import editor.IEditor;

public interface IEditorPlugin {

    String getSupportedExtension();

    IEditor createEditor(String filePath, IFileSystem fs);

    IEditor createEmptyEditor(String filePath, IFileSystem fs, boolean withLog);

    boolean supportsCommand(String commandName);

    Set<String> getSupportedCommands();

    ICommand createCommand(String cmdName, String[] args, IEditor editor);
}
