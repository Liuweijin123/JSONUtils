package JSONUtils.JSONParserProvider;

import JSONUtils.JSONNode;
import JSONUtils.JSONParserProvider.adapter.DefaultValueAdapter;
import JSONUtils.JSONParserProvider.adapter.GsonValueAdapter;
import JSONUtils.JSONParserProvider.adapter.ValueAdapter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public abstract class BaseParserProvider {
    private static Map<String, BaseParserProvider> providers = new HashMap<String, BaseParserProvider>();
    private static String defaultProviderName;
    private static final List<Class> supportClasses = Arrays.asList(JacksonProvider.class, FastJsonProvider.class, GsonProvider.class);
    private static Map<Class<? extends BaseParserProvider>, ValueAdapter> valueAdapterMap = new HashMap<>();
    private static ValueAdapter defaultValueAdapter = new DefaultValueAdapter();

    public static String getDefaultProviderName() {
        return BaseParserProvider.defaultProviderName;
    }

    public static void setDefaultProviderName(String providerName) {
        synchronized (defaultProviderName) {
            defaultProviderName = providerName;
        }
    }

    static {
        for (Class cls : supportClasses) {
            try {
                BaseParserProvider provider = (BaseParserProvider) cls.newInstance();
                if (provider.checkProviderInit()) {
                    if (defaultProviderName == null)
                        defaultProviderName = provider.getProviderName();
                    BaseParserProvider.addProvider(provider);
                } else {
                    System.out.println("<"+provider.getProviderName() + "解析器不支持,忽略该解析器"+">");
                }
            } catch (InstantiationException | IllegalAccessException e) {
                System.out.println(cls.getName() + "解析器无法初始化");
            }
        }

    }

    public static BaseParserProvider getProvider(String providerName) {
        if (!providers.containsKey(providerName))
            throw new IllegalArgumentException("解析器[" + providerName + "]未实现");
        return providers.get(providerName);
    }


    public static BaseParserProvider getDefaultProvider() {
        if (defaultProviderName == null)
            throw new IllegalArgumentException("找不到任何可用的json解析器，请引入json解析依赖包（目前支持jackson,fastJson,gson）");
        return getProvider(defaultProviderName);

    }

    public static void addProvider(BaseParserProvider parserProvider) {
        if (!providers.containsKey(parserProvider.getProviderName())) {
            synchronized (providers) {
                if (!providers.containsKey(parserProvider.getProviderName())) {
                    providers.put(parserProvider.getProviderName(), parserProvider);
                }
            }
        }
    }

    public static void registerValueAdapter(Class<? extends BaseParserProvider> registerCls, ValueAdapter adapter) {
        valueAdapterMap.put(registerCls, adapter);
    }

    public static ValueAdapter getDefaultValueAdapter() {

        ValueAdapter valueAdapter = defaultValueAdapter;
        BaseParserProvider provider = getDefaultProvider();
        if (valueAdapterMap.containsKey(provider.getClass())) {
            valueAdapter = valueAdapterMap.get(provider.getClass());
        }
        return valueAdapter;
    }

    public JSONNode parse(String json) {
        if (json != null) {
            JSONNode result = null;
            if (json.startsWith("{")) {
                Map<String, Object> map = readJsonToMap(json);
                result = JSONNode.mapConvertToNode(map);
            } else if (json.startsWith("[")) {
                List<Object> list = readJsonToList(json);
                result = JSONNode.listConvertToNode(list);
            } else if (json.startsWith("\"") && json.endsWith("\"")) {
                result = new JSONNode().setValue(json.substring(1, json.length() - 1));
            } else {
                result = new JSONNode().setValue(json);
            }
            return result;
        }

        return null;
    }

    public abstract String getProviderName();

    public abstract boolean checkProviderInit();

    public abstract String toJson(Object obj);

    public abstract Map<String, Object> readJsonToMap(String json);

    public abstract List<Object> readJsonToList(String json);

    public abstract <T> T toBean(String json, Class<T> targetCls);
}
