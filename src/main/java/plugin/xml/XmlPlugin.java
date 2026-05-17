package plugin.xml;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import command.ICommand;
import core.IFileSystem;
import core.plugin.IEditorPlugin;
import editor.IEditor;
import plugin.xml.command.XmlAppendChildCommand;
import plugin.xml.command.XmlDeleteCommand;
import plugin.xml.command.XmlEditIdCommand;
import plugin.xml.command.XmlEditTextCommand;
import plugin.xml.command.XmlInsertBeforeCommand;
import plugin.xml.command.XmlTreeCommand;
import plugin.xml.core.IXmlNode;
import plugin.xml.core.XmlElement;

public class XmlPlugin implements IEditorPlugin {

    private static final Set<String> SUPPORTED_COMMANDS = new HashSet<>();

    private static final Pattern TAG_PATTERN = Pattern.compile("<(\\w+)([^>]*)>");

    private static final Pattern ATTR_PATTERN = Pattern.compile("(\\w+)=\"([^\"]*)\"");

    static {
        SUPPORTED_COMMANDS.add("insert-before");
        SUPPORTED_COMMANDS.add("append-child");
        SUPPORTED_COMMANDS.add("edit-id");
        SUPPORTED_COMMANDS.add("edit-text");
        SUPPORTED_COMMANDS.add("delete");
        SUPPORTED_COMMANDS.add("xml-tree");
    }

    @Override
    public String getSupportedExtension() {
        return ".xml";
    }

    @Override
    public IEditor createEditor(String filePath, IFileSystem fs) {
        XmlEditor editor = new XmlEditor(filePath, fs);
        List<String> lines = fs.readLines(filePath);

        int startLine = 0;
        if (!lines.isEmpty() && lines.get(0).equals("# log")) {
            startLine = 1;
        }

        StringBuilder xmlContent = new StringBuilder();
        for (int i = startLine; i < lines.size(); i++) {
            xmlContent.append(lines.get(i)).append("\n");
        }

        IXmlNode root = parseXmlToTree(xmlContent.toString());
        editor.setRoot(root);
        editor.setModified(false);
        return editor;
    }

    private IXmlNode parseXmlToTree(String xml) {
        Stack<IXmlNode> elementStack = new Stack<>();
        String[] lines = xml.split("\n", -1);
        IXmlNode root = null;

        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) {
                continue;
            }

            if (line.startsWith("<?")) {
                continue;
            }

            if (line.startsWith("</")) {
                if (!elementStack.isEmpty()) {
                    elementStack.pop();
                }
                continue;
            }

            Matcher startMatcher = TAG_PATTERN.matcher(line);
            if (startMatcher.find()) {
                String tagName = startMatcher.group(1);
                String attrPart = startMatcher.group(2);

                String id = "";
                java.util.Map<String, String> attrs = new java.util.LinkedHashMap<>();
                Matcher attrMatcher = ATTR_PATTERN.matcher(attrPart);
                while (attrMatcher.find()) {
                    String attrName = attrMatcher.group(1);
                    String attrValue = attrMatcher.group(2);
                    attrs.put(attrName, attrValue);
                    if ("id".equals(attrName)) {
                        id = attrValue;
                    }
                }

                String textContent = null;
                int tagEnd = startMatcher.end();
                if (tagEnd < line.length()) {
                    String afterTag = line.substring(tagEnd).trim();
                    int endTagPos = afterTag.indexOf("</");
                    if (endTagPos > 0) {
                        textContent = afterTag.substring(0, endTagPos).trim();
                    } else if (endTagPos < 0 && !afterTag.isEmpty()) {
                        textContent = afterTag;
                    }
                }

                IXmlNode parent = elementStack.isEmpty() ? null : elementStack.peek();
                XmlElement element = new XmlElement(tagName, id, parent);
                if (textContent != null) {
                    element.setText(textContent);
                }
                for (java.util.Map.Entry<String, String> attrEntry : attrs.entrySet()) {
                    element.setAttribute(attrEntry.getKey(), attrEntry.getValue());
                }

                if (parent != null) {
                    parent.addChild(element);
                }

                if (root == null) {
                    root = element;
                }

                if (!line.contains("/>") && !line.contains("</")) {
                    elementStack.push(element);
                }
            }
        }

        return root;
    }

    @Override
    public IEditor createEmptyEditor(String filePath, IFileSystem fs, boolean withLog) {
        XmlEditor editor = new XmlEditor(filePath, fs);
        editor.initializeEmpty();
        return editor;
    }

    @Override
    public boolean supportsCommand(String commandName) {
        return SUPPORTED_COMMANDS.contains(commandName.toLowerCase());
    }

    @Override
    public Set<String> getSupportedCommands() {
        return new java.util.HashSet<>(SUPPORTED_COMMANDS);
    }

    @Override
    public ICommand createCommand(String cmdName, String[] args, IEditor editor) {
        XmlEditor xmlEditor = (XmlEditor) editor;
        String cmd = cmdName.toLowerCase();

        switch (cmd) {
            case "insert-before":
                if (args.length < 3) {
                    throw new IllegalArgumentException("\u7528\u6CD5: insert-before <tagName> <newId> <targetId> [\"text\"]");
                }
                String ibText = args.length >= 4 ? args[3] : null;
                return new XmlInsertBeforeCommand(xmlEditor, args[0], args[1], args[2], ibText);

            case "append-child":
                if (args.length < 3) {
                    throw new IllegalArgumentException("\u7528\u6CD5: append-child <tagName> <newId> <parentId> [\"text\"]");
                }
                String acText = args.length >= 4 ? args[3] : null;
                return new XmlAppendChildCommand(xmlEditor, args[0], args[1], args[2], acText);

            case "edit-id":
                if (args.length < 2) {
                    throw new IllegalArgumentException("\u7528\u6CD5: edit-id <oldId> <newId>");
                }
                return new XmlEditIdCommand(xmlEditor, args[0], args[1]);

            case "edit-text":
                if (args.length < 1) {
                    throw new IllegalArgumentException("\u7528\u6CD5: edit-text <elementId> [\"text\"]");
                }
                String etText = args.length >= 2 ? args[1] : "";
                return new XmlEditTextCommand(xmlEditor, args[0], etText);

            case "delete":
                if (args.length < 1) {
                    throw new IllegalArgumentException("\u7528\u6CD5: delete <elementId>");
                }
                return new XmlDeleteCommand(xmlEditor, args[0]);

            case "xml-tree":
                return new XmlTreeCommand(xmlEditor);

            default:
                throw new IllegalArgumentException("\u4E0D\u652F\u6301\u7684\u547D\u4EE4: " + cmdName);
        }
    }
}
