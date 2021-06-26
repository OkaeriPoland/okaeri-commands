package eu.okaeri.commands.type.resolver;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public abstract class BasicTypeResolver<T> implements TypeResolver<T> {

    @Override
    @SuppressWarnings("unchecked")
    public boolean supports(Type type) {

        if (type instanceof Class<?>) {
            return this.supports((Class<?>) type);
        }

        if (!(type instanceof ParameterizedType)) {
            return false;
        }

        ParameterizedType parameterizedType = (ParameterizedType) type;
        Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();

        return (actualTypeArguments.length == 1) && this.supports((Class<?>) actualTypeArguments[0]);
    }
}
