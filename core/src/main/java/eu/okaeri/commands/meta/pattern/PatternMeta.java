package eu.okaeri.commands.meta.pattern;

import eu.okaeri.commands.meta.ArgumentMeta;
import eu.okaeri.commands.meta.pattern.element.OptionalElement;
import eu.okaeri.commands.meta.pattern.element.PatternElement;
import eu.okaeri.commands.meta.pattern.element.StaticElement;
import lombok.Data;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Data
public class PatternMeta {

    public static PatternMeta of(String pattern) {
        return of(pattern, Collections.emptyList());
    }

    public static PatternMeta of(String pattern, List<ArgumentMeta> arguments) {

        // create meta
        AtomicInteger position = new AtomicInteger();
        AtomicInteger argumentIndex = new AtomicInteger();
        PatternMeta meta = new PatternMeta();
        List<PatternElement> patternElements = Arrays.stream(pattern.split(" "))
                .map(part -> {
                    int positionValue = position.getAndIncrement();
                    int argumentValue = argumentIndex.getAndIncrement();

                    PatternElement element = PatternElement.of(part, positionValue);
                    if (!(element instanceof StaticElement)) {

                        if (element.getName() == null) {
                            String metaName = (argumentValue < arguments.size())
                                    ? arguments.get(argumentValue).getName()
                                    : null;
                            element.setName(metaName);
                        }

                        return element;
                    }

                    argumentIndex.decrementAndGet();
                    return element;
                })
                .collect(Collectors.toList());
        meta.elements = Collections.unmodifiableList(patternElements);
        meta.raw = meta.elements.stream().map(PatternElement::render).collect(Collectors.joining(" "));
        meta.staticElements = Math.toIntExact(meta.elements.stream().filter(element -> element instanceof StaticElement).count());

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
        if (!meta.applicable(pattern)) {
            throw new IllegalArgumentException("failed to create PatternMeta, rendered version (" + meta.raw + ") does not match original (" + pattern + ")");
        }

        return meta;
    }

    private List<PatternElement> elements;
    private int staticElements;
    private String raw;

    public boolean applicable(String pattern) {

        String[] parts = pattern.split(" ");
        if (parts.length != this.elements.size()) {
            return false;
        }

        for (int i = 0; i < this.elements.size(); i++) {

            String part = parts[i];
            PatternElement currentElement = this.elements.get(i);
            PatternElement testElement = PatternElement.of(part, i);

            String currentName = currentElement.getName();
            String testName = testElement.getName();

            // check type equality
            if (!currentElement.getClass().isAssignableFrom(testElement.getClass())) {
                return false;
            }

            // unnamed parameter
            if ((testName == null) && (currentName != null)) {
                continue;
            }

            // check name equality
            if (!Objects.equals(currentName, testName)) {
                return false;
            }
        }

        return true;
    }

    public boolean matches(String args) {

        String[] argsArr = args.split(" ");
        if (argsArr.length < this.staticElements) {
            return false;
        }

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
