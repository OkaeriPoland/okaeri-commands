package eu.okaeri.commands.meta.pattern.element;

import lombok.*;

@Data
@AllArgsConstructor
public abstract class PatternElement {

    public static PatternElement of(String part, int index) {

        if (part.startsWith("<") && (part.charAt(part.length() - 1) == '>')) {
            return new RequiredElement(part.substring(1, part.length() - 1), index);
        }

        if (part.startsWith("[") && (part.charAt(part.length() - 1) == ']')) {
            return new OptionalElement(part.substring(1, part.length() - 1), index);
        }

        if ("*".equals(part)) {
            return new RequiredElement(null, index);
        }

        if ("?".equals(part)) {
            return new OptionalElement(null, index);
        }

        return new StaticElement(part, index);
    }

    private String name;
    private int index;

    public abstract String render();
}
