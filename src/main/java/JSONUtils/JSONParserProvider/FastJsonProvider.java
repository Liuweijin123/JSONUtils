package JSONUtils.JSONParserProvider;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FastJsonProvider extends BaseParserProvider {
    private static final String JSON_CLASS_NAME = "com.alibaba.fastjson.JSON";

    @Override
    public String getProviderName() {
        return "fastJson";
    }

    @Override
    public boolean checkProviderInit() {
        try {
            Class.forName(JSON_CLASS_NAME);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    @Override
    public String toJson(Object obj) {
        try {
            Class JSONCls = Class.forName(JSON_CLASS_NAME);
            Method mToJSONString = JSONCls.getMethod("toJSONString", Object.class);
            return (String) mToJSONString.invoke(null, obj);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Map<String, Object> readJsonToMap(String json) {
        return parseObject(json, Map.class);
    }

    @Override
    public List<Object> readJsonToList(String json) {
        return parseObject(json,List.class);
    }


    @Override
    public <T> T toBean(String json, Class<T> targetCls) {
        return parseObject(json,targetCls);
    }

    private <T> T parseObject(String json, Class<T> targetCls) {
        try {
            Class jsonCls = Class.forName(JSON_CLASS_NAME);
            Method mParseObject = jsonCls.getMethod("parseObject", String.class, Class.class);
            return (T) mParseObject.invoke(null, json, targetCls);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        BaseParserProvider.setDefaultProviderName("fastJson");
        BaseParserProvider provider = BaseParserProvider.getDefaultProvider();
        String json = "{\"testName\":{\"testName2\":{\"testName3\":\"testValue3\"},\"testName4\":{\"testName5\":\"testValue7\"}}}";
        System.out.println(provider.parse(json).toJson());
        json = "{\"test\":[{\"test2\":\"testValue\",\"test1\":\"testValue1\"}]}";
        System.out.println(provider.parse(json).toJson());
        json = "[{\"test2\":\"testValue\",\"test1\":\"testValue1\"}]";
        System.out.println(provider.parse(json).toJson());
        json = "{\"testName\":{\"testName2\":{},\"testName4\":{\"testName5\":\"testValue7\"}}}";
        System.out.println(provider.parse(json).toJson());
        json = "{\"testName\":{\"testName2\":[],\"testName4\":{\"testName5\":\"testValue7\"}}}";
        System.out.println(provider.parse(json).toJson());
    }
}
