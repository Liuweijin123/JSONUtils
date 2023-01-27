package JSONUtils.JSONParserProvider.adapter;

public class DefaultValueAdapter implements ValueAdapter {
    @Override
    public Object castTo(Object src) {
        return src;
    }
}
