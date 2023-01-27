package JSONUtils.JSONParserProvider;

//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.ObjectMapper;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class JacksonProvider extends BaseParserProvider {

    private static final String OBJECT_MAPPER_CLASS_NAME = "com.fasterxml.jackson.databind.ObjectMapper";

    @Override
    public String getProviderName() {
        return "jackson";
    }

    @Override
    public boolean checkProviderInit() {
        try {
            Class.forName(OBJECT_MAPPER_CLASS_NAME);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }


    public String toJson(Object obj) {
        try {
//          ObjectMapper objectMapper = new ObjectMapper();
//          return objectMapper.writeValueAsString(obj);
            Class objMapClass = Class.forName(OBJECT_MAPPER_CLASS_NAME);
            Object objectMapper = Class.forName(OBJECT_MAPPER_CLASS_NAME).newInstance();
            Method mWriteValueAsString = objMapClass.getMethod("writeValueAsString", Object.class);
            return (String) mWriteValueAsString.invoke(objectMapper, obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Map<String, Object> readJsonToMap(String json) {
//            ObjectMapper objectMapper = new ObjectMapper();
//            Map<String, Object> map = objectMapper.readValue(json, Map.class);
        Map<String, Object> map = (Map<String, Object>) readValue(json, Map.class);
        return map;
    }

    @Override
    public List<Object> readJsonToList(String json) {
//            ObjectMapper objectMapper = new ObjectMapper();
//            List<Object>  list = objectMapper.readValue(json, List.class);
        List<Object> list = (List<Object>) readValue(json, List.class);
        return list;
    }


    public <T> T toBean(String json, Class<T> targetCls) {
//            ObjectMapper objectMapper = new ObjectMapper();
//            return objectMapper.readValue(json, targetCls);
        return (T) readValue(json, targetCls);

    }

    private Object readValue(String json, Class targetCls) {
        try {
            Class objMapClass = Class.forName(OBJECT_MAPPER_CLASS_NAME);
            Object objectMapper = Class.forName(OBJECT_MAPPER_CLASS_NAME).newInstance();
            Method mReadValue = objMapClass.getMethod("readValue", String.class, Class.class);
            return mReadValue.invoke(objectMapper, json, targetCls);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public static void main(String[] args) {
        BaseParserProvider.setDefaultProviderName("jackson");
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
