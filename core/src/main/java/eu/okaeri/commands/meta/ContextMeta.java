package eu.okaeri.commands.meta;

import eu.okaeri.commands.Commands;
import eu.okaeri.commands.annotation.Context;
import lombok.Data;
import lombok.NonNull;

import java.lang.reflect.Parameter;

/**
 * One per executor parameter filled with the sender from the {@link eu.okaeri.commands.service.CommandData}.
 */
@Data
public class ContextMeta {

    private Class<?> type;
    private boolean required;
    private String invalid;
    private int index;

    public static ContextMeta of(@NonNull Commands commands, @NonNull Parameter parameter, int index) {

        ContextMeta meta = new ContextMeta();
        meta.type = parameter.getType();
        meta.index = index;

        // an explicitly annotated parameter is enforced even when there is no sender at all,
        // a parameter narrowed by its declared type alone falls back to the MissingArgumentHandler
        meta.required = commands.isContextRequired(parameter);

        Context context = parameter.getAnnotation(Context.class);
        meta.invalid = (context == null) ? "" : context.invalid();

        return meta;
    }

    public boolean accepts(Object sender) {
        return this.type.isInstance(sender);
    }
}
