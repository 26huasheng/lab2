package plugin.xml.core;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class XmlElement implements IXmlNode {

    private String id;

    private String tagName;

    private String text;

    private IXmlNode parent;

    private List<IXmlNode> children;

    private Map<String, String> attributes;

    public XmlElement(String tagName, String id) {
        this.tagName = tagName;
        this.id = id;
        this.text = "";
        this.parent = null;
        this.children = new ArrayList<>();
        this.attributes = new LinkedHashMap<>();
    }

    public XmlElement(String tagName, String id, IXmlNode parent) {
        this.tagName = tagName;
        this.id = id;
        this.text = "";
        this.parent = parent;
        this.children = new ArrayList<>();
        this.attributes = new LinkedHashMap<>();
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getTagName() {
        return tagName;
    }

    @Override
    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    @Override
    public String getText() {
        return text;
    }

    @Override
    public void setText(String text) {
        this.text = text != null ? text : "";
    }

    @Override
    public IXmlNode getParent() {
        return parent;
    }

    @Override
    public void setParent(IXmlNode parent) {
        this.parent = parent;
    }

    @Override
    public List<IXmlNode> getChildren() {
        return children;
    }

    @Override
    public void addChild(IXmlNode child) {
        if (child == null) {
            return;
        }
        if (child.getParent() != null) {
            child.getParent().getChildren().remove(child);
        }
        child.setParent(this);
        children.add(child);
    }

    @Override
    public void insertBefore(String targetId, IXmlNode newNode) {
        if (newNode == null) {
            return;
        }
        int targetIndex = -1;
        for (int i = 0; i < children.size(); i++) {
            if (children.get(i).getId().equals(targetId)) {
                targetIndex = i;
                break;
            }
        }
        if (targetIndex < 0) {
            throw new RuntimeException("\u76EE\u6807\u5143\u7D20\u4E0D\u5B58\u5728: " + targetId);
        }
        if (newNode.getParent() != null) {
            newNode.getParent().getChildren().remove(newNode);
        }
        newNode.setParent(this);
        children.add(targetIndex, newNode);
    }

    @Override
    public void removeChild(String id) {
        IXmlNode target = null;
        for (IXmlNode child : children) {
            if (child.getId().equals(id)) {
                target = child;
                break;
            }
        }
        if (target != null) {
            target.setParent(null);
            children.remove(target);
        }
    }

    @Override
    public IXmlNode deepClone() {
        XmlElement clone = new XmlElement(this.tagName, this.id);
        clone.text = this.text;
        clone.attributes = new LinkedHashMap<>(this.attributes);
        for (IXmlNode child : this.children) {
            IXmlNode clonedChild = child.deepClone();
            clonedChild.setParent(clone);
            clone.children.add(clonedChild);
        }
        return clone;
    }

    @Override
    public IXmlNode findById(String id) {
        if (this.id != null && this.id.equals(id)) {
            return this;
        }
        for (IXmlNode child : children) {
            IXmlNode result = child.findById(id);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    @Override
    public Map<String, String> getAttributes() {
        return attributes;
    }

    @Override
    public void setAttribute(String key, String value) {
        attributes.put(key, value);
    }

    @Override
    public String getAttribute(String key) {
        return attributes.get(key);
    }
}
