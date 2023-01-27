package JSONUtils.JSONParserProvider.adapter;

public class GsonValueAdapter implements ValueAdapter {
    @Override
    public Object castTo(Object src) {
        Object target = src;
        if (target instanceof Double) {//gson默认会将数字转成double，这里是把double转回来
            if (((Double) target) == ((Double) target).intValue()) {
                target = ((Double) target).intValue();
            } else if (((Double) target) == ((Double) target).longValue()) {
                target = ((Double) target).longValue();
            }
        }
        return target;
    }
}
