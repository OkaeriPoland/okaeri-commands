package eu.okaeri.commands.meta.pattern;

import eu.okaeri.commands.meta.ArgumentMeta;
import eu.okaeri.commands.meta.pattern.element.OptionalElement;
import eu.okaeri.commands.meta.pattern.element.PatternElement;
import eu.okaeri.commands.meta.pattern.element.RequiredElement;
import eu.okaeri.commands.meta.pattern.element.StaticElement;
import lombok.Data;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Data
public class PatternMeta {

    public static PatternMeta of(String pattern) {

        // create meta
        PatternMeta meta = new PatternMeta();
        List<PatternElement> patternElements = Arrays.stream(pattern.split(" "))
                .map(part -> {
                    if (part.startsWith("<") && (part.charAt(part.length() - 1) == '>')) {
                        return new RequiredElement(part.substring(1, part.length() - 1));
                    } else if (part.startsWith("[") && (part.charAt(part.length() - 1) == ']')) {
                        return new OptionalElement(part.substring(1, part.length() - 1));
                    } else {
                        return new StaticElement(part);
                    }
                })
                .collect(Collectors.toList());
        meta.elements = Collections.unmodifiableList(patternElements);
        meta.raw = meta.elements.stream().map(PatternElement::render).collect(Collectors.joining(" "));

        // validate meta
        boolean foundOptional = false;
        for (PatternElement element : meta.elements) {
            if (!(element instanceof OptionalElement)) {
                if (foundOptional) {
                    throw new IllegalArgumentException("only other optional arguments are allowed after optional argument: " + pattern);
                }
                continue;
            }
            foundOptional = true;
        }

        // validate rendering
        if (!pattern.equals(meta.raw)) {
            throw new IllegalArgumentException("failed to create PatternMeta, rendered version (" + meta.raw + ") does not match original (" + pattern + ")");
        }

        return meta;
    }

    private List<PatternElement> elements;
    private String raw;

    public boolean matches(String args) {

        String[] argsArr = args.split(" ");

        for (int i = 0; i < this.elements.size(); i++) {

            PatternElement element = this.elements.get(i);

            // no such index in arguments and not optional (missing element)
            if ((argsArr.length <= i) && !(element instanceof OptionalElement)) {
                return false;
            }

            // static element does not match
            if ((element instanceof StaticElement) && !argsArr[i].equals(element.getName())) {
                return false;
            }
        }

        return true;
    }

    public Optional<PatternElement> getElementByName(String name) {
        if (name == null) throw new IllegalArgumentException("name cannot be null");
        return this.elements.stream()
                .filter(element -> name.equals(element.getName()))
                .findAny();
    }

    public String getValueByArgument(ArgumentMeta argument, String args) {

        String[] parts = args.split(" ");
        String name = argument.getName();

        for (int i = 0; i < this.elements.size(); i++) {

            PatternElement element = this.elements.get(i);
            if (!name.equals(element.getName())) {
                continue;
            }

            if (element instanceof OptionalElement) {
                return  (parts.length <= i) ? null : parts[i];
            }

            return parts[i];
        }

        throw new IllegalArgumentException("no such element for named parameter '" + name + " in " + args);
    }
}
