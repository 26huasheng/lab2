package plugin.xml.core;

import java.util.List;
import java.util.Map;

public interface IXmlNode {

    String getId();

    void setId(String id);

    String getTagName();

    void setTagName(String tagName);

    String getText();

    void setText(String text);

    IXmlNode getParent();

    void setParent(IXmlNode parent);

    List<IXmlNode> getChildren();

    void addChild(IXmlNode child);

    void insertBefore(String targetId, IXmlNode newNode);

    void removeChild(String id);

    IXmlNode deepClone();

    IXmlNode findById(String id);

    Map<String, String> getAttributes();

    void setAttribute(String key, String value);

    String getAttribute(String key);
}
