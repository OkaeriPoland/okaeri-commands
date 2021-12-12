package eu.okaeri.commands.meta.pattern.element;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;

@Data
@AllArgsConstructor
public abstract class PatternElement {

    private String name;
    private int index;
    private int width;

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
            int width = getWidthFromPatternElement(part);
            String name = getNameFromPatternElement(part);
            return new RequiredElement(name, index, width);
        }

        if (part.startsWith("[") && (part.charAt(part.length() - 1) == ']')) {
            int width = getWidthFromPatternElement(part);
            String name = getNameFromPatternElement(part);
            return new OptionalElement(name, index, width);
        }

        return new StaticElement(part, index);
    }

    public static int getWidthFromPatternElement(@NonNull String element) {

        if (element.startsWith("<") || element.startsWith("[")) {
            element = element.substring(1, element.length() - 1);
        }

        if (element.endsWith("...")) {
            return -1;
        }

        if (!element.contains(":")) {
            return 1;
        }

        String[] parts = element.split(":", 2);
        int width = Integer.parseInt(parts[1]);

        if ((width <= 0) && (width != -1)) {
            throw new RuntimeException("Pattern element width must be positive or -1!");
        }

        return width;
    }

    public static String getNameFromPatternElement(@NonNull String element) {

        if (element.startsWith("<") || element.startsWith("[")) {
            element = element.substring(1, element.length() - 1);
        }

        if (element.endsWith("...")) {
            return element.substring(0, element.length() - 3);
        }

        if (element.contains(":")) {
            return element.split(":", 2)[0];
        }

        return element;
    }

    public abstract String render();
}
