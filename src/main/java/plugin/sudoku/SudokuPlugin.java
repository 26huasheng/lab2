package plugin.sudoku;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import command.ICommand;
import core.IFileSystem;
import core.plugin.IEditorPlugin;
import editor.IEditor;

public class SudokuPlugin implements IEditorPlugin {

    private static final Set<String> SUPPORTED_COMMANDS = new HashSet<>();

    static {
        SUPPORTED_COMMANDS.add("set-number");
    }

    @Override
    public String getSupportedExtension() {
        return ".sdk";
    }

    @Override
    public IEditor createEditor(String filePath, IFileSystem fs) {
        SudokuEditor editor = new SudokuEditor(filePath, fs);
        List<String> lines = fs.readLines(filePath);
        for (int i = 0; i < lines.size() && i < 9; i++) {
            String[] parts = lines.get(i).split("\\s+");
            for (int j = 0; j < parts.length && j < 9; j++) {
                int val = Integer.parseInt(parts[j]);
                if (val >= 1 && val <= 9) {
                    editor.setNumber(i + 1, j + 1, val);
                }
            }
        }
        editor.setModified(false);
        return editor;
    }

    @Override
    public IEditor createEmptyEditor(String filePath, IFileSystem fs, boolean withLog) {
        return new SudokuEditor(filePath, fs);
    }

    @Override
    public boolean supportsCommand(String commandName) {
        return SUPPORTED_COMMANDS.contains(commandName.toLowerCase());
    }

    @Override
    public Set<String> getSupportedCommands() {
        return new HashSet<>(SUPPORTED_COMMANDS);
    }

    @Override
    public ICommand createCommand(String cmdName, String[] args, IEditor editor) {
        SudokuEditor sudokuEditor = (SudokuEditor) editor;
        String cmd = cmdName.toLowerCase();

        switch (cmd) {
            case "set-number":
                if (args.length < 3) {
                    throw new IllegalArgumentException("\u7528\u6CD5: set-number <row> <col> <val>");
                }
                int row = Integer.parseInt(args[0]);
                int col = Integer.parseInt(args[1]);
                int val = Integer.parseInt(args[2]);
                return new SudokuSetCommand(sudokuEditor, row, col, val);

            default:
                throw new IllegalArgumentException("\u4E0D\u652F\u6301\u7684\u547D\u4EE4: " + cmdName);
        }
    }
}
