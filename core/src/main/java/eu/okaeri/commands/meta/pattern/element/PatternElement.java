package eu.okaeri.commands.meta.pattern.element;

import lombok.*;

@Data
@AllArgsConstructor
public abstract class PatternElement {

    public static PatternElement of(String part, int index) {

        switch (part) {
            case "*":
                return new RequiredElement(null, index, 1);
            case "*...":
                return new RequiredElement(null, index, -1);
            case "?":
                return new OptionalElement(null, index, 1);
            case "?...":
                return new OptionalElement(null, index, -1);
        }

        if (part.startsWith("*:")) {
            int width = getWidthFromPatternElement(part);
            return new RequiredElement(null, index, width);
        }

        if (part.startsWith("?:")) {
            int width = getWidthFromPatternElement(part);
            return new OptionalElement(null, index, width);
        }

        if (part.startsWith("<") && (part.charAt(part.length() - 1) == '>')) {
            String element = part.substring(1, part.length() - 1);
            int width = getWidthFromPatternElement(element);
            String name = getNameFromPatternElement(element);
            return new RequiredElement(name, index, width);
        }

        if (part.startsWith("[") && (part.charAt(part.length() - 1) == ']')) {
            String element = part.substring(1, part.length() - 1);
            int width = getWidthFromPatternElement(element);
            String name = getNameFromPatternElement(element);
            return new OptionalElement(name, index, width);
        }

        return new StaticElement(part, index);
    }

    public static int getWidthFromPatternElement(@NonNull String element) {

        if (!element.contains(":")) {
            return 1;
        }

        if (element.endsWith("...")) {
            return -1;
        }

        String[] parts = element.split(":", 2);
        int width = Integer.parseInt(parts[1]);

        if ((width <= 0) && (width != -1)) {
            throw new RuntimeException("Pattern element width must be positive or -1!");
        }

        return width;
    }

    public static String getNameFromPatternElement(@NonNull String element) {

        if (!element.contains(":")) {
            return element.split(":", 2)[0];
        }

        if (element.endsWith("...")) {
            return element.substring(0, element.length() - 3);
        }

        return element;
    }

    private String name;
    private int index;
    private int width;

    public abstract String render();
}
