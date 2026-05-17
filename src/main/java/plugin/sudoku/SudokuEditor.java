package plugin.sudoku;

import java.util.ArrayList;
import java.util.List;

import core.IFileSystem;
import core.editor.AbstractEditor;

public class SudokuEditor extends AbstractEditor {

    private int[][] grid = new int[9][9];

    public SudokuEditor() {
    }

    public SudokuEditor(String filePath, IFileSystem fs) {
        this.filePath = filePath;
        this.fs = fs;
    }

    public void setNumber(int row, int col, int val) {
        if (row < 1 || row > 9 || col < 1 || col > 9) {
            throw new IllegalArgumentException("\u884C\u5217\u53F7\u5E94\u57281-9\u4E4B\u95F4");
        }
        if (val < 1 || val > 9) {
            throw new IllegalArgumentException("\u6570\u5B57\u5E94\u57281-9\u4E4B\u95F4");
        }
        grid[row - 1][col - 1] = val;
        setModified(true);
    }

    public void clearNumber(int row, int col) {
        if (row < 1 || row > 9 || col < 1 || col > 9) {
            return;
        }
        grid[row - 1][col - 1] = 0;
        setModified(true);
    }

    public int getNumber(int row, int col) {
        if (row < 1 || row > 9 || col < 1 || col > 9) {
            return -1;
        }
        return grid[row - 1][col - 1];
    }

    @Override
    protected List<String> serialize() {
        List<String> lines = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < 9; j++) {
                sb.append(grid[i][j]);
                if (j < 8) {
                    sb.append(" ");
                }
            }
            lines.add(sb.toString());
        }
        return lines;
    }

    @Override
    public String getPlainTextContent() {
        if (isModified) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 9; i++) {
                for (int j = 0; j < 9; j++) {
                    sb.append(grid[i][j]);
                }
            }
            return sb.toString();
        }
        return "";
    }
}
