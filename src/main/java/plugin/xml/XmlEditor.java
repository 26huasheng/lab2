package plugin.xml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import core.EditorException;
import core.IFileSystem;
import core.editor.AbstractEditor;
import plugin.xml.core.IXmlNode;
import plugin.xml.core.XmlElement;

public class XmlEditor extends AbstractEditor {

    private IXmlNode root;

    private Map<String, IXmlNode> idMap = new HashMap<>();

    private boolean hasLogHeader = false;

    public XmlEditor() {
    }

    public XmlEditor(String filePath, IFileSystem fs) {
        this.filePath = filePath;
        this.fs = fs;
    }

    public void initializeEmpty() {
        this.root = new XmlElement("root", "root", null);
        this.root.setAttribute("id", "root");
        this.idMap.clear();
        this.idMap.put("root", this.root);
        this.isModified = true;
    }

    public void setRoot(IXmlNode root) {
        this.root = root;
        rebuildIdMap();
    }

    public IXmlNode getRoot() {
        return root;
    }

    public void reindexIdMap() {
        idMap.clear();
        if (root != null) {
            indexSubtree(root);
        }
    }

    public void setHasLogHeader(boolean hasLogHeader) {
        this.hasLogHeader = hasLogHeader;
    }

    private void rebuildIdMap() {
        idMap.clear();
        if (root != null) {
            indexSubtree(root);
        }
    }

    private void indexSubtree(IXmlNode node) {
        if (node.getId() != null && !node.getId().isEmpty()) {
            idMap.put(node.getId(), node);
        }
        for (IXmlNode child : node.getChildren()) {
            indexSubtree(child);
        }
    }

    public IXmlNode findElementById(String id) {
        return idMap.get(id);
    }

    public void appendChild(String tagName, String newId, String parentId, String text) {
        if (idMap.containsKey(newId)) {
            throw new EditorException("\u5143\u7D20ID\u5DF2\u5B58\u5728: " + newId);
        }
        IXmlNode parent = idMap.get(parentId);
        if (parent == null) {
            throw new EditorException("\u7236\u5143\u7D20\u4E0D\u5B58\u5728: " + parentId);
        }
        XmlElement newElement = new XmlElement(tagName, newId, parent);
        if (text != null && !text.isEmpty()) {
            newElement.setText(text);
        }
        newElement.setAttribute("id", newId);
        parent.addChild(newElement);
        idMap.put(newId, newElement);
        this.isModified = true;
    }

    public void insertBefore(String tagName, String newId, String targetId, String text) {
        if (idMap.containsKey(newId)) {
            throw new EditorException("\u5143\u7D20ID\u5DF2\u5B58\u5728: " + newId);
        }
        IXmlNode target = idMap.get(targetId);
        if (target == null) {
            throw new EditorException("\u76EE\u6807\u5143\u7D20\u4E0D\u5B58\u5728: " + targetId);
        }
        IXmlNode targetParent = target.getParent();
        if (targetParent == null) {
            throw new EditorException("\u4E0D\u80FD\u5728\u6839\u5143\u7D20\u524D\u63D2\u5165\u5143\u7D20");
        }
        XmlElement newElement = new XmlElement(tagName, newId, targetParent);
        if (text != null && !text.isEmpty()) {
            newElement.setText(text);
        }
        newElement.setAttribute("id", newId);
        targetParent.insertBefore(targetId, newElement);
        idMap.put(newId, newElement);
        this.isModified = true;
    }

    public void editId(String oldId, String newId) {
        IXmlNode element = idMap.get(oldId);
        if (element == null) {
            throw new EditorException("\u5143\u7D20\u4E0D\u5B58\u5728: " + oldId);
        }
        if (element.getParent() == null) {
            throw new EditorException("\u4E0D\u5EFA\u8BAE\u4FEE\u6539\u6839\u5143\u7D20ID");
        }
        if (idMap.containsKey(newId)) {
            throw new EditorException("\u76EE\u6807ID\u5DF2\u5B58\u5728: " + newId);
        }
        idMap.remove(oldId);
        element.setId(newId);
        element.setAttribute("id", newId);
        idMap.put(newId, element);
        this.isModified = true;
    }

    public void editText(String elementId, String text) {
        IXmlNode element = idMap.get(elementId);
        if (element == null) {
            throw new EditorException("\u5143\u7D20\u4E0D\u5B58\u5728: " + elementId);
        }
        element.setText(text != null ? text : "");
        this.isModified = true;
    }

    public void deleteElement(String elementId) {
        IXmlNode element = idMap.get(elementId);
        if (element == null) {
            throw new EditorException("\u5143\u7D20\u4E0D\u5B58\u5728: " + elementId);
        }
        if (element.getParent() == null) {
            throw new EditorException("\u4E0D\u80FD\u5220\u9664\u6839\u5143\u7D20");
        }
        removeFromIdMapRecursive(element);
        element.getParent().removeChild(elementId);
        this.isModified = true;
    }

    public void restoreNode(String parentId, int index, IXmlNode snapshot) {
        IXmlNode parent = idMap.get(parentId);
        if (parent == null) {
            throw new EditorException("\u6062\u590D\u5931\u8D25: \u7236\u5143\u7D20\u4E0D\u5B58\u5728 " + parentId);
        }
        snapshot.setParent(parent);
        if (index >= 0 && index < parent.getChildren().size()) {
            parent.getChildren().add(index, snapshot);
        } else {
            parent.getChildren().add(snapshot);
        }
        indexSubtreeForRestore(snapshot);
        this.isModified = true;
    }

    private void indexSubtreeForRestore(IXmlNode node) {
        if (node.getId() != null && !node.getId().isEmpty()) {
            idMap.put(node.getId(), node);
        }
        for (IXmlNode child : node.getChildren()) {
            indexSubtreeForRestore(child);
        }
    }

    private void removeFromIdMapRecursive(IXmlNode node) {
        idMap.remove(node.getId());
        for (IXmlNode child : node.getChildren()) {
            removeFromIdMapRecursive(child);
        }
    }

    public String getXmlTreeString() {
        if (root == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        buildTreeString(root, "", true, sb);
        return sb.toString();
    }

    private void buildTreeString(IXmlNode node, String prefix, boolean isRoot, StringBuilder sb) {
        if (!isRoot) {
            sb.append(prefix);
            IXmlNode p = node.getParent();
            boolean isLast = p != null && p.getChildren().indexOf(node) == p.getChildren().size() - 1;
            sb.append(isLast ? "\u2514\u2500\u2500 " : "\u251C\u2500\u2500 ");
        }
        sb.append(node.getTagName());
        sb.append(" [");
        List<String> attrParts = new ArrayList<>();
        for (Map.Entry<String, String> attr : node.getAttributes().entrySet()) {
            attrParts.add(attr.getKey() + "=\"" + attr.getValue() + "\"");
        }
        sb.append(String.join(", ", attrParts));
        sb.append("]");
        if (node.getText() != null && !node.getText().isEmpty()) {
            sb.append("\n").append(prefix);
            if (!isRoot) {
                IXmlNode p = node.getParent();
                boolean isLast = p != null && p.getChildren().indexOf(node) == p.getChildren().size() - 1;
                sb.append(isLast ? "    " : "\u2502   ");
            }
            sb.append("\u201C").append(node.getText()).append("\u201D");
        }
        sb.append("\n");
        String childPrefix = prefix;
        if (!isRoot) {
            IXmlNode p = node.getParent();
            boolean isLast = p != null && p.getChildren().indexOf(node) == p.getChildren().size() - 1;
            childPrefix = prefix + (isLast ? "    " : "\u2502   ");
        }
        for (int i = 0; i < node.getChildren().size(); i++) {
            buildTreeString(node.getChildren().get(i), childPrefix, false, sb);
        }
    }

    @Override
    protected List<String> serialize() {
        List<String> lines = new ArrayList<>();
        if (hasLogHeader) {
            lines.add("# log");
        }
        lines.add("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        if (root != null) {
            serializeElement(root, 0, lines);
        }
        return lines;
    }

    private void serializeElement(IXmlNode node, int indent, List<String> lines) {
        StringBuilder indentStr = new StringBuilder();
        for (int i = 0; i < indent; i++) {
            indentStr.append("    ");
        }
        StringBuilder openTag = new StringBuilder();
        openTag.append(indentStr).append("<").append(node.getTagName());
        for (Map.Entry<String, String> attr : node.getAttributes().entrySet()) {
            openTag.append(" ").append(attr.getKey()).append("=\"").append(attr.getValue()).append("\"");
        }
        if (node.getChildren().isEmpty() && (node.getText() == null || node.getText().isEmpty())) {
            openTag.append(" />");
            lines.add(openTag.toString());
            return;
        }
        openTag.append(">");
        lines.add(openTag.toString());
        if (node.getText() != null && !node.getText().isEmpty()) {
            lines.add(indentStr + "    " + node.getText());
        }
        for (IXmlNode child : node.getChildren()) {
            serializeElement(child, indent + 1, lines);
        }
        lines.add(indentStr + "</" + node.getTagName() + ">");
    }

    @Override
    public String getPlainTextContent() {
        StringBuilder sb = new StringBuilder();
        if (root != null) {
            collectTextContent(root, sb);
        }
        return sb.toString();
    }

    private void collectTextContent(IXmlNode node, StringBuilder sb) {
        if (node.getText() != null && !node.getText().isEmpty()) {
            if (sb.length() > 0) {
                sb.append(" ");
            }
            sb.append(node.getText());
        }
        for (IXmlNode child : node.getChildren()) {
            collectTextContent(child, sb);
        }
    }
}
