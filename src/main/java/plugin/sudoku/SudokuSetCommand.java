package plugin.sudoku;

import core.command.AbstractCommand;

public class SudokuSetCommand extends AbstractCommand {

    private final SudokuEditor editor;

    private final int row;

    private final int col;

    private final int val;

    private int oldVal;

    public SudokuSetCommand(SudokuEditor editor, int row, int col, int val) {
        this.editor = editor;
        this.row = row;
        this.col = col;
        this.val = val;
    }

    @Override
    public void execute() {
        this.oldVal = editor.getNumber(row, col);
        editor.setNumber(row, col, val);
    }

    @Override
    public void undo() {
        if (oldVal == 0) {
            editor.clearNumber(row, col);
        } else {
            editor.setNumber(row, col, oldVal);
        }
    }

    @Override
    public String getCommandLog() {
        return timestamp + " set-number " + row + " " + col + " " + val;
    }
}
