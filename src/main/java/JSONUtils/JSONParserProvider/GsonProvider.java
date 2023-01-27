package JSONUtils.JSONParserProvider;


import JSONUtils.JSONParserProvider.adapter.GsonValueAdapter;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;


public class GsonProvider extends BaseParserProvider {
    private static final String GSON_CLASS_NAME = "com.google.gson.Gson";

    static {
        BaseParserProvider.registerValueAdapter(GsonProvider.class, new GsonValueAdapter());
    }

    @Override
    public String getProviderName() {
        return "gson";
    }

    @Override
    public boolean checkProviderInit() {
        try {
            Class.forName(GSON_CLASS_NAME);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    @Override
    public String toJson(Object obj) {
        try {
            Class gsonClass = Class.forName(GSON_CLASS_NAME);
            Object gsonObj = Class.forName(GSON_CLASS_NAME).newInstance();
            Method mToJson = gsonClass.getMethod("toJson", Object.class);
            return (String) mToJson.invoke(gsonObj, obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Map<String, Object> readJsonToMap(String json) {
        return fromJson(json, Map.class);
    }

    @Override
    public List<Object> readJsonToList(String json) {
        return fromJson(json, List.class);
    }

    @Override
    public <T> T toBean(String json, Class<T> targetCls) {
        return fromJson(json, targetCls);
    }

    private <T> T fromJson(String json, Class<T> targetCls) {
        try {
            Class jsonCls = Class.forName(GSON_CLASS_NAME);
            Object jsonObj = jsonCls.newInstance();
            Method mFromJson = jsonCls.getMethod("fromJson", String.class, Class.class);
            return (T) mFromJson.invoke(jsonObj, json, targetCls);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        BaseParserProvider.setDefaultProviderName("gson");
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
