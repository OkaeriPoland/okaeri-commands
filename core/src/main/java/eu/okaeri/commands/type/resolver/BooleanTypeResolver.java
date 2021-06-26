package eu.okaeri.commands.type.resolver;

import eu.okaeri.commands.meta.ArgumentMeta;
import eu.okaeri.commands.service.CommandContext;
import eu.okaeri.commands.service.InvocationContext;
import lombok.AllArgsConstructor;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

@AllArgsConstructor
public class BooleanTypeResolver extends BasicTypeResolver<Boolean> {

    private final Set<String> trueValues = new HashSet<>(Arrays.asList("true", "y", "yes", "on", "1"));
    private final Set<String> falseValues = new HashSet<>(Arrays.asList("false", "n", "no", "off", "0"));

    @Override
    public boolean supports(Class<?> type) {
        return Boolean.class.isAssignableFrom(type) || (type == boolean.class);
    }

    @Override
    public Boolean resolve(InvocationContext invocationContext, CommandContext commandContext, ArgumentMeta argumentMeta, String text) {

        String lowerCaseText = text.toLowerCase(Locale.ROOT);

        if (this.trueValues.contains(lowerCaseText)) {
            return true;
        }

        if (this.falseValues.contains(lowerCaseText)) {
            return false;
        }

        throw new IllegalArgumentException("non-boolean value was provided");
    }
}
