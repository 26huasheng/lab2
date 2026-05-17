package plugin.text;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import core.EditorException;
import core.IFileSystem;
import core.editor.AbstractEditor;

public class TextEditor extends AbstractEditor {

    private List<String> lines = new ArrayList<>();

    public TextEditor() {
    }

    public TextEditor(String filePath, IFileSystem fs) {
        this.filePath = filePath;
        this.fs = fs;
    }

    public void append(String text) {
        if (text.contains("\n")) {
            String[] splitLines = text.split("\n", -1);
            for (String line : splitLines) {
                lines.add(line);
            }
        } else {
            lines.add(text);
        }
        setModified(true);
    }

    public void insert(int line, int col, String text) {
        if (lines.isEmpty()) {
            if (line != 1 || col != 1) {
                throw new EditorException("\u7A7A\u6587\u4EF6\u53EA\u80FD\u57281:1\u4F4D\u7F6E\u63D2\u5165");
            }
            if (text.contains("\n")) {
                String[] splitLines = text.split("\n", -1);
                for (String splitLine : splitLines) {
                    lines.add(splitLine);
                }
            } else {
                lines.add(text);
            }
            setModified(true);
            return;
        }

        if (line < 1 || line > lines.size() + 1) {
            throw new EditorException("\u884C\u53F7\u6216\u5217\u53F7\u8D8A\u754C");
        }

        if (line == lines.size() + 1) {
            lines.add("");
        }

        int currentLineLength = lines.get(line - 1).length();
        if (col < 1 || col > currentLineLength + 1) {
            throw new EditorException("\u884C\u53F7\u6216\u5217\u53F7\u8D8A\u754C");
        }

        if (text.contains("\n")) {
            String[] splitLines = text.split("\n", -1);
            String currentLine = lines.get(line - 1);
            String before = currentLine.substring(0, col - 1);
            String after = currentLine.substring(col - 1);

            lines.remove(line - 1);

            lines.add(line - 1, before + splitLines[0]);
            for (int i = 1; i < splitLines.length; i++) {
                lines.add(line - 1 + i, splitLines[i]);
            }

            int lastInsertedIndex = line - 1 + splitLines.length - 1;
            String lastInsertedLine = lines.get(lastInsertedIndex);
            lines.set(lastInsertedIndex, lastInsertedLine + after);
        } else {
            String currentLine = lines.get(line - 1);
            String before = currentLine.substring(0, col - 1);
            String after = currentLine.substring(col - 1);
            lines.set(line - 1, before + text + after);
        }

        setModified(true);
    }

    public String delete(int line, int col, int len) {
        if (lines.isEmpty()) {
            throw new EditorException("\u884C\u53F7\u6216\u5217\u53F7\u8D8A\u754C");
        }

        if (line < 1 || line > lines.size()) {
            throw new EditorException("\u884C\u53F7\u6216\u5217\u53F7\u8D8A\u754C");
        }

        String currentLine = lines.get(line - 1);
        int currentLineLength = currentLine.length();

        if (col < 1 || col > currentLineLength + 1) {
            throw new EditorException("\u884C\u53F7\u6216\u5217\u53F7\u8D8A\u754C");
        }

        if (col - 1 + len > currentLineLength) {
            throw new EditorException("\u5220\u9664\u957F\u5EA6\u8D85\u51FA\u884C\u5C3E");
        }

        String deleted = currentLine.substring(col - 1, col - 1 + len);
        String newLine = currentLine.substring(0, col - 1) + currentLine.substring(col - 1 + len);

        if (newLine.isEmpty()) {
            lines.remove(line - 1);
        } else {
            lines.set(line - 1, newLine);
        }

        setModified(true);
        return deleted;
    }

    public String replace(int line, int col, int len, String text) {
        String oldText = delete(line, col, len);
        insert(line, col, text);
        return oldText;
    }

    public Iterator<String> getLineIterator(int startLine, int endLine) {
        if (lines.isEmpty()) {
            return java.util.Collections.emptyIterator();
        }

        int actualStart = Math.max(1, startLine);
        int actualEnd = Math.min(lines.size(), endLine);

        if (actualStart > actualEnd) {
            throw new EditorException("\u884C\u8303\u56F4\u8D8A\u754C");
        }

        return lines.subList(actualStart - 1, actualEnd).iterator();
    }

    public int getLineCount() {
        return lines.size();
    }

    public List<String> getLines() {
        return new ArrayList<>(lines);
    }

    @Override
    protected List<String> serialize() {
        return new ArrayList<>(lines);
    }

    @Override
    public String getPlainTextContent() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < lines.size(); i++) {
            if (i > 0) {
                sb.append("\n");
            }
            sb.append(lines.get(i));
        }
        return sb.toString();
    }
}
