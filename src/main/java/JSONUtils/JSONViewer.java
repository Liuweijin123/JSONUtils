package JSONUtils;


import JSONUtils.JSONParserProvider.BaseParserProvider;
import JSONUtils.JSONParserProvider.JacksonProvider;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * json查询工具类
 */
public class JSONViewer {
    private JSONNode nodeValue;
    private Boolean isFailure = false;
    private Boolean isThrow;
    private List<String> nodeNames;
    private JSONViewer parent = null;

    public JSONViewer() {
        this(new JSONNode());
    }

    public JSONViewer(Object json) {
        this(json, false);
    }

    public JSONViewer(Object json, boolean isThrow) {
        this.isThrow = isThrow;
        try {
            if (json == null) {
                this.isFailure = true;
            } else if (json instanceof JSONNode) {
                this.nodeValue = (JSONNode) json;
            } else if (json instanceof String) {
                this.nodeValue = BaseParserProvider.getDefaultProvider().parse((String) json);
            } else if (json instanceof JSONViewer) {
                JSONViewer wrap = ((JSONViewer) json);
                this.nodeValue = wrap.nodeValue;
                this.isFailure = !wrap.isSuccess();
                this.parent = wrap.parent;
                this.isThrow = wrap.isThrow;
                this.nodeNames = new ArrayList<>(wrap.nodeNames);
            } else {
                this.nodeValue = BaseParserProvider.getDefaultProvider().parse(toJson(json));
            }
            if (this.nodeNames == null)
                this.nodeNames = new ArrayList<>();
        } catch (Exception e) {
            isFailure = true;
            if (this.isThrow) {
                throw new IllegalArgumentException("json转换失败", e);
            }
        }

    }

    /**
     * 将obj转为JSONViewer对象
     *
     * @param obj
     * @return
     */
    public static JSONViewer of(Object obj) {
        return new JSONViewer(obj);
    }

    /**
     * 复制当前节点
     *
     * @return
     */
    public JSONViewer copyNew() {
        return new JSONViewer(this.toString());
    }

    /**
     * 获取根节点
     *
     * @return
     */
    public JSONViewer root() {
        JSONViewer root = this;
        while (root.parent != null) {
            root = root.parent;
        }
        return root;
    }

    /**
     * 获取当前节点的父节点
     *
     * @return
     */
    public JSONViewer getParent() {
        return new JSONViewer(this.parent);
    }

    /**
     * 获取当前节点下面 节点名=[name]的节点
     *
     * @param name
     * @return
     */
    public JSONViewer node(String name) {
        JSONViewer copyNew = new JSONViewer(this);
        copyNew.parent = this;
        copyNew.nodeNames.add(name);
        copyNew.nodeValue = null;
        if (copyNew.isFailure == false) {
            if (name != null && this.nodeValue.containsKey(name)) {
                copyNew.nodeValue = this.nodeValue.get(name);
            } else {
                copyNew.isFailure = true;
                if (this.isThrow) {
                    throw new IllegalArgumentException("找不到node[" + name + "]");
                }
            }
        }
        return copyNew;
    }

    /**
     * 通过路径获取节点
     * 例如： viewer.path("/a/0/b");//从根节点开始找
     * viewer.path("../a/0/b");//从当前的节点的父节点开始找
     * viewer.path("a/b/c"); //从当前节点开始找
     *
     * @param pathName
     * @return
     */
    public JSONViewer path(String pathName) {
        if (pathName == null)
            pathName = "";
        if (pathName.startsWith("/"))
            pathName = "root" + pathName;

        JSONViewer result = this;
        List<String> nodeNames = Arrays.asList(pathName.split("/"));
        for (String name : nodeNames) {
            switch (name) {
                case "root":
                    result = result.root();
                    break;
                case "..":
                    result = result.getParent();
                    break;
                case ".":
                    break;
                default:
                    result = result.node(name);
                    break;
            }
        }
        return result;
    }

    /**
     * 获取节点名
     *
     * @return
     */
    public String getName() {
        if (this.nodeNames.size() == 0)
            return "";
        return nodeNames.get(nodeNames.size() - 1);
    }

    /**
     * 获取当前节点的所有子节点
     *
     * @return
     */
    public List<JSONViewer> getChildren() {
        List<JSONViewer> resultList = new ArrayList<>();
        if (this.getVal() instanceof JSONNode) {
            for (String key : ((JSONNode) getVal()).getChildren().keySet()) {
                resultList.add(this.node(key));
            }
        }
        return resultList;
    }


    /**
     * 获取当前节点的值
     *
     * @return
     */
    public Object getVal() {
        if (isFailure)
            return null;
        Object val = this.nodeValue;
        if (this.nodeValue != null && this.nodeValue.isLeaf())
            val = ((JSONNode) val).getValue();
        return val;
    }


    /**
     * 设置当前节点的值
     *
     * @param val
     * @return
     */
    public JSONViewer setVal(Object val) {
        if (this.nodeValue == null)
            this.nodeValue = new JSONNode(this.getName());

        if (val instanceof JSONViewer) {
            this.nodeValue.setValue(((JSONViewer) val).nodeValue);
        } else {
            if (val instanceof Collection || val instanceof Map) {
                this.nodeValue = BaseParserProvider.getDefaultProvider().parse(toJson(val));
            } else {
                this.nodeValue.setValue(val);
            }
        }
        this.isFailure = false;
        this.nodeValue.setName(this.getName());
        if (this.nodeNames.size() > 0) {
            this.parent.setVal(this);
        }
        return this;
    }

    /**
     * 获取值并将它转换成字符串
     *
     * @param defaultVal 默认值
     * @return
     */
    public String getValStr(String defaultVal) {
        String str = getValStr();
        if (str == null)
            str = defaultVal;
        return str;
    }

    /**
     * 获取值并将它转换成字符串
     *
     * @param exception 值不存在时抛异常
     * @return
     */
    public String getValStr(RuntimeException exception) {
        if (this.getVal() == null)
            throw exception;
        return getValStr();
    }

    /**
     * 获取值并将它转换成字符串
     *
     * @return
     */
    public String getValStr() {
        if (this.getVal() == null)
            return null;
        return this.getVal().toString();
    }

    /**
     * 判断当前节点的值是否等于某个对象
     *
     * @param obj
     * @return
     */
    public Boolean valEqualObj(Object obj) {
        if (this.getVal() == null)
            return false;
        return this.getVal().equals(obj);
    }

    /**
     * 判断当前节点的值是否等效于某个字符串
     *
     * @param str
     * @return
     */
    public Boolean valEqualStr(String str) {
        return stringEquals(this.getValStr(), str);
    }

    /**
     * 判断当前节点的值是否匹配某个正值表达式
     *
     * @param regex
     * @return
     */
    public Boolean valMatchStr(String regex) {
        if (this.getValStr() == null)
            return false;
        return this.getValStr().matches(regex);
    }

    /**
     * 是否成功
     *
     * @return
     */
    public Boolean isSuccess() {
        return !isFailure;
    }


    /**
     * 是否为叶子节点
     *
     * @return
     */
    public boolean isLeaf() {
        if (isFailure)
            return false;
        return this.nodeValue.isLeaf();
    }

    /**
     * 是否为数组
     *
     * @return
     */
    public boolean isArray() {
        if (isFailure)
            return false;
        return this.nodeValue.isArray();
    }

    /**
     * 获取节点路径
     *
     * @return
     */
    public String getPath() {
        return "/" + this.nodeNames.stream().collect(Collectors.joining("/"));
    }

    /**
     * 查找 节点名=[name] 的第一个节点
     *
     * @param name 属性名
     * @return
     */
    public JSONViewer findFirstByNodeName(String name) {
        return this.findFirstByNodeName(name, null);
    }

    /**
     * 查找 节点名=[name] 并且 节点值=[value] 的第一个节点
     *
     * @param name  属性名
     * @param value 属性值
     * @return
     */
    public JSONViewer findFirstByNodeName(String name, String value) {
        List<JSONViewer> resultList = new ArrayList<>();
        if (!this.isThrow) {
            findWithChildrenByNodeName(resultList, name, value, this, true);
            if (resultList.size() > 0) {
                return resultList.get(0);
            }
        } else {
            findWithChildrenByNodeName(resultList, name, value, this, false);
            if (resultList.size() != 1) {
                new IllegalArgumentException(name + "=" + value + "的结果不符合");
            }
            return resultList.get(0);
        }
        return new JSONViewer(null);
    }

    /**
     * 查找 节点名=[name] 的所有节点
     *
     * @param propertyName
     * @return
     */
    public List<JSONViewer> findByNodeName(String propertyName) {
        return findByNodeName(propertyName, null);
    }

    /**
     * 查找 节点名=[name] 并且 节点值=[value] 的所有节点
     *
     * @param name  节点名
     * @param value 节点值
     * @return
     */
    public List<JSONViewer> findByNodeName(String name, String value) {
        List<JSONViewer> results = new ArrayList<>();
        this.findWithChildrenByNodeName(results, name, value, this, false);
        return results;
    }

    /**
     * 通过对象作为条件查询第一条并返回转换成成beam的结果
     * @param mode
     * @param <T>
     * @return
     */
    public <T> T findToBeamByObj(T mode) {
        String json = toJson(mode);
        JSONViewer jsonViewer = findFirstByJson(json);
        if (jsonViewer.isSuccess())
            return (T) jsonViewer.toBean(mode.getClass());
        return null;
    }

    public <T> List<T> findToListByObj(T mode) {
        String json = toJson(mode);
        List<T> results = new ArrayList<>();
        List<JSONViewer> jsonViewers = findByJson(json);
        for (JSONViewer viewer : jsonViewers) {
            if (viewer.isSuccess())
                results.add((T) viewer.toBean(mode.getClass()));
        }
        return results;
    }


    public JSONViewer findFirstByJson(String json) {
        List<JSONViewer> results = findByJson(json, true);
        if (results.size() > 0)
            return results.get(0);
        return new JSONViewer(null);
    }


    public List<JSONViewer> findByJson(String json) {
        return findByJson(json, false);
    }

    private List<JSONViewer> findByJson(String json, boolean isFindFirst) {
        List<KeyValue> allPathAndValues = new ArrayList<>();
        new JSONViewer(json).filter(t -> {
            String path = t.getPath();
            if (path != null && path.length() > 1) {
                if (t.isLeaf()) {
                    allPathAndValues.add(new KeyValue(t.getPath().substring(1), t.getValStr()));
                }
            }
            return false;
        });
        List<JSONViewer> results = new ArrayList<>();
        this.findWithChildrenByPredicate(results, t -> {
            boolean isMatch = true;
            for (KeyValue keyValue : allPathAndValues) {
                if (!t.path(keyValue.key).valMatchStr(keyValue.getValue())) {
                    isMatch = false;
                    break;
                }
            }
            return isMatch;
        }, this, false);
        return results;
    }

    /**
     * 通过条件只筛选出第一个节点
     *
     * @param predicate
     * @return
     */
    public JSONViewer filterFirst(Predicate<JSONViewer> predicate) {
        List<JSONViewer> resultList = new ArrayList<>();
        if (!this.isThrow) {
            findWithChildrenByPredicate(resultList, predicate, this, true);
            if (resultList.size() > 0) {
                return resultList.get(0);
            }
        } else {
            findWithChildrenByPredicate(resultList, predicate, this, false);
            if (resultList.size() != 1) {
                new IllegalArgumentException(predicate.toString() + "的结果不符合");
            }
            return resultList.get(0);
        }
        return new JSONViewer(null);
    }

    /**
     * 遍历所有节点并通过过滤条件进行过滤
     *
     * @param predicate
     * @return
     */
    public List<JSONViewer> filter(Predicate<JSONViewer> predicate) {
        List<JSONViewer> results = new ArrayList<>();
        findWithChildrenByPredicate(results, predicate, this, false);
        return results;
    }

    private void findWithChildrenByNodeName(List<JSONViewer> result, String propertyName, String propertyVal, JSONViewer parent, boolean isFindFirst) {
        Predicate<JSONViewer> predicate = current -> {
            if (stringEquals(current.getName(), propertyName)) {
                if (propertyVal != null && stringEquals(current.getValStr(), propertyVal)) {
                    return true;
                } else if (propertyVal == null && stringEquals(current.getName(), propertyName)) {
                    return true;
                }
            }
            return false;
        };
        findWithChildrenByPredicate(result, predicate, parent, isFindFirst);
    }

    private void findWithChildrenByPredicate(List<JSONViewer> result, Predicate<JSONViewer> predicate, JSONViewer parent, boolean isFindFirst) {
        if (isFindFirst && result.size() > 0)
            return;
        if (parent == this && predicate.test(this)) {
            result.add(this);
        }
        if (parent != null && parent.getVal() != null) {
            for (JSONViewer child : parent.getChildren()) {
                if (predicate.test(child)) {
                    result.add(child);
                }
                findWithChildrenByPredicate(result, predicate, child, isFindFirst);
            }
        }
    }


    /**
     * 获取当前的值，并将值转换成新的JSONViewer对象
     *
     * @return
     */
    public JSONViewer getAndConvertTo() {
        JSONViewer v = new JSONViewer(this.getVal(), this.isThrow);
        v.parent = this.parent;
        v.isFailure = this.isFailure;
        v.nodeNames = this.nodeNames;
        return v;
    }


    /**
     * 转成json格式的字符串
     *
     * @return
     */
    @Override
    public String toString() {
        String result = "";
        if (this.getVal() instanceof JSONNode)
            result = ((JSONNode) this.getVal()).toJson();
        else
            result = JacksonProvider.getDefaultProvider().toJson(this.getVal());
        if (this.getName() != null && !this.getName().trim().equals("")
                && (result.startsWith("{\"" + this.getName()) || result.startsWith("[\"" + this.getName())))
            result = result.substring(this.getName().length() + 4, result.length() - 1);
        return result;
    }

    /**
     * 转成bean
     *
     * @param targetCls
     * @param <T>
     * @return
     */
    public <T> T toBean(Class<T> targetCls) {
        try {
            if (isSuccess()) {
//                if (this.getVal() instanceof JSONNode)
//                    return JacksonProvider.getDefaultProvider().toBean(((JSONNode) this.getVal()).toJson(), targetCls);
                return JacksonProvider.getDefaultProvider().toBean(this.toString(), targetCls);
            }
        } catch (Exception e) {
            if (this.isThrow)
                throw new RuntimeException("json转换异常", e);
        }
        return null;
    }

    /**
     * 转成list
     *
     * @param targetCls
     * @param <T>
     * @return
     */
    public <T> List<T> toList(Class<T> targetCls) {
        try {
            if (isSuccess()) {
                List<T> results = new ArrayList<>();
                List<JSONNode> children = null;
                if (this.getVal() instanceof JSONNode && ((JSONNode) this.getVal()).isArray())
                    children = (List<JSONNode>) ((JSONNode) this.getVal()).getChildren().values();
                else
                    children = (List<JSONNode>) JacksonProvider.getDefaultProvider().parse(this.toString()).getChildren().values();

                for (JSONNode child : children) {
                    results.add(JacksonProvider.getDefaultProvider().toBean(child.toJson(), targetCls));
                }
                return results;

            }
        } catch (Exception e) {
            if (this.isThrow)
                throw new RuntimeException("json转换异常", e);
        }
        return null;
    }

    private static boolean stringEquals(String str1, String str2) {
        if (str1 == null || str2 == null)
            return false;
        return str1.equals(str2);
    }

    private static String toJson(Object obj) {
        String result = "";
        if (obj instanceof Collection) {
            //数组特殊处理，防止堆栈溢出
            StringBuilder sb = new StringBuilder("[");
            boolean isFirst = true;
            for (Object item : (Collection) obj) {
                if (isFirst)
                    isFirst = false;
                else
                    sb.append(",");
                if (item instanceof JSONViewer)
                    sb.append(item.toString());
                else
                    sb.append(BaseParserProvider.getDefaultProvider().toJson(item));
            }
            sb.append("]");
            result = sb.toString();
        } else {
            result = BaseParserProvider.getDefaultProvider().toJson(obj);
        }
        return result;
    }

    private static class KeyValue {
        private String key;
        private String value;

        public KeyValue(String key, String value) {
            this.key = key;
            this.value = value;
        }

        public String getKey() {
            return key;
        }

        public String getValue() {
            return value;
        }
    }


}
