package JSONUtils;

import JSONUtils.JSONParserProvider.BaseParserProvider;

import java.util.*;
import java.util.stream.Collectors;

public class JSONNode {
    private String name;
    private Object value;

    private JSONNode parent;
    private Map<String, JSONNode> children;
    private boolean isArray;
    private boolean isLeaf;

    public JSONNode() {
        children = new HashMap<String, JSONNode>();
        isArray = false;
        isLeaf = true;
    }

    public JSONNode(String name) {
        this();
        this.name = name;
    }

    public JSONNode(Integer index) {
        this(String.valueOf(index));
    }

    public JSONNode(String name, Object value) {
        this(name);
        this.isLeaf = true;
        this.value = value;
    }

    public JSONNode(int index, Object value) {
        this(String.valueOf(index));
        this.value = value;
        this.isArray = true;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public JSONNode getParent() {
        return parent;
    }

    public boolean isArray() {
        return isArray;
    }

    public boolean isLeaf() {
        return isLeaf;
    }

    public Map<String, JSONNode> getChildren() {
        return children;
    }

    public boolean containsKey(Object key) {
        return this.getChildren().containsKey(key);
    }

    public JSONNode get(Object key) {
        return this.getChildren().get(key);
    }

    public Object getValue() {
        if (this.isLeaf)
            return this.value;
        return this.children.values().stream().collect(Collectors.toList());
    }

    public JSONNode setValue(Object value) {
        if (!(value instanceof JSONNode)) {
            if (value instanceof Collection)
                this.isArray = true;
            else
                this.isArray = false;
            this.isLeaf = true;
            this.value = value;
        } else {
            this.isLeaf = false;
            this.value = null;
            if (!((JSONNode) value).isEmpty()) {
                this.children.put(((JSONNode) value).getName(), (JSONNode) value);
                ((JSONNode) value).parent = this;
            }
        }
        return this;
    }


    public JSONNode copyNew() {
        BaseParserProvider parserProvider = BaseParserProvider.getDefaultProvider();
        return parserProvider.parse(this.toJson());
    }


    public JSONNode put(String name, Object value) {
        if (name == null || name.trim().equals(""))
            throw new IllegalArgumentException("name不能为空");
        this.isArray = false;
        setChildValue(name, value);
        return this;
    }

    public JSONNode put(Integer index, Object value) {
        this.isArray = true;
        setChildValue(String.valueOf(index), value);
        return this;
    }

    public JSONNode add(Object value) {
        Integer index = this.children.size();
        while (this.children.containsKey(String.valueOf(index))) {
            index++;
        }
        put(index, value);
        return this;
    }

    private JSONNode setChildValue(String name, Object value) {
        JSONNode child = new JSONNode(name).setValue(value);
        this.setValue(child);
        return this;
    }


    public boolean isEmpty() {
        return this.isLeaf == true && this.name == null && this.value == null;
    }

    public String toJson() {
        StringBuilder result = new StringBuilder("");

        boolean notArrayElement = (this.parent == null || !this.parent.isArray);
        if ((name != null && !name.trim().equals("")) && notArrayElement)
            result.append("{\"" + this.name + "\":");

        BaseParserProvider parserProvider = BaseParserProvider.getDefaultProvider();
        if (this.isLeaf) {
            if (this.value != null)
                result.append(parserProvider.toJson(this.value));
            else
                result.append("null");
        } else {
            boolean isFirstChild = true;
            String leftSign = "", rightSign = "";
            if (this.isArray) {
                leftSign = "[";
                rightSign = "]";
            } else if (!this.isLeaf) {
                leftSign = "{";
                rightSign = "}";
            }
            if (this.children.isEmpty()) {
                result.append(leftSign + rightSign);
            } else {
                for (JSONNode child : this.children.values()) {
                    if (isFirstChild) {
                        isFirstChild = false;
                        result.append(leftSign);
                    } else
                        result.append(",");

                    if (this.isArray) {
                        if (child.isLeaf)
                            result.append(parserProvider.toJson(child.value));
                        else {
                            String childJson = child.toJson();
                            if (!notArrayElement) {
                                Integer startIndex = child.name == null ? 1 : (leftSign + "\"" + child.name + "\":").length();
                                childJson = childJson.substring(startIndex, childJson.length() - 1);
                            }
                            result.append(childJson);
                        }
                    } else {
                        if (child.isLeaf)
                            result.append("\"" + child.getName() + "\":" + parserProvider.toJson(child.value));
                        else {
                            String childJson = child.toJson();
                            childJson = childJson.substring(1, childJson.length() - 1);
                            result.append(childJson);
                        }
                    }
                }
                result.append(rightSign);
            }
        }
        if ((name != null && !name.trim().equals("")) && notArrayElement)
            result.append("}");
        return result.toString();
    }

    public static JSONNode mapConvertToNode(Map<String, Object> map) {
        JSONNode result = new JSONNode();
        result.isLeaf = false;
        result.isArray = false;
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (entry.getValue() instanceof Map) {
                JSONNode child = mapConvertToNode((Map<String, Object>) entry.getValue());
                child.setName(entry.getKey());
                result.getChildren().put(child.getName(), child);
                child.parent = result;
            } else if (entry.getValue() instanceof List) {
                JSONNode child = listConvertToNode((List<Object>) entry.getValue());
                child.setName(entry.getKey());
                result.getChildren().put(child.getName(), child);
                child.parent = result;
            } else {
                result.put(entry.getKey(), BaseParserProvider.getDefaultValueAdapter().castTo(entry.getValue()));
            }
        }
        return result;
    }

    public static JSONNode listConvertToNode(List<Object> list) {
        JSONNode result = new JSONNode();
        result.isLeaf = false;
        result.isArray = true;
        Integer index = 0;
        for (Object value : list) {
            if (value instanceof Map) {
                JSONNode child = JSONNode.mapConvertToNode((Map<String, Object>) value);
                child.setName(String.valueOf(index));
                result.getChildren().put(child.getName(), child);
                child.parent = result;
            } else if (value instanceof List) {
                JSONNode child = listConvertToNode((List<Object>) value);
                child.setName(String.valueOf(index));
                result.getChildren().put(child.getName(), child);
                child.parent = result;
            } else {
                result.add(BaseParserProvider.getDefaultValueAdapter().castTo(value));
            }
            index++;
        }
        return result;
    }
}
