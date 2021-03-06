package eu.okaeri.commands.meta;

import eu.okaeri.commands.Commands;
import eu.okaeri.commands.annotation.Arg;
import eu.okaeri.commands.service.Option;
import lombok.Data;
import lombok.NonNull;
import lombok.SneakyThrows;

import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Optional;

@Data
public class ArgumentMeta {

    private boolean optional;
    private String name;
    private int index;
    private Class<?> type;
    private Class<?> rawType;
    private Type parameterizedType;

    public static boolean isArg(Parameter parameter) {
        return parameter.getAnnotation(Arg.class) != null;
    }

    @SneakyThrows
    public static ArgumentMeta of(@NonNull Commands commands, @NonNull Parameter parameter, int index) {

        Arg arg = parameter.getAnnotation(Arg.class);

        if (arg == null) {
            throw new IllegalArgumentException("cannot create ArgumentMeta from Parameter without @Arg annotation");
        }

        ArgumentMeta meta = new ArgumentMeta();
        meta.name = commands.resolveText(arg.value().isEmpty() ? parameter.getName() : arg.value());
        meta.index = index;
        meta.rawType = parameter.getType();
        meta.parameterizedType = parameter.getParameterizedType();
        meta.type = resolveType(meta.rawType, meta.parameterizedType);
        meta.optional = Option.class.isAssignableFrom(meta.rawType) || Optional.class.isAssignableFrom(meta.rawType);

        return meta;
    }

    private static Class<?> resolveType(Class<?> rawType, Type parameterizedType) {

        if (!Option.class.isAssignableFrom(rawType) && !Optional.class.isAssignableFrom(rawType)) {
            return rawType;
        }

        Type paramType = ((ParameterizedType) parameterizedType).getRawType();
        if (paramType instanceof Class<?>) {
            rawType = (Class<?>) paramType;
        }

        return rawType;
    }

    public Object wrap(Object value) {

        if (!this.optional) {
            return value;
        }

        if (Option.class.isAssignableFrom(this.rawType)) {
            return Option.of(value);
        }

        if (Optional.class.isAssignableFrom(this.rawType)) {
            return Optional.ofNullable(value);
        }

        return value;
    }
}
