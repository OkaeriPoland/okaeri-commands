package eu.okaeri.commands.meta.pattern;

import eu.okaeri.commands.meta.ArgumentMeta;
import eu.okaeri.commands.meta.pattern.element.OptionalElement;
import eu.okaeri.commands.meta.pattern.element.PatternElement;
import eu.okaeri.commands.meta.pattern.element.RequiredElement;
import eu.okaeri.commands.meta.pattern.element.StaticElement;
import lombok.Data;
import lombok.NonNull;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Data
public class PatternMeta {

    public static PatternMeta of(@NonNull String pattern) {
        return of(pattern, Collections.emptyList());
    }

    public static PatternMeta of(@NonNull String pattern, @NonNull List<ArgumentMeta> arguments) {

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
        meta.setElements(Collections.unmodifiableList(patternElements));
        meta.setRaw(meta.getElements().stream().map(PatternElement::render).collect(Collectors.joining(" ")));
        meta.setStaticElements(Math.toIntExact(meta.getElements().stream().filter(element -> element instanceof StaticElement).count()));
        meta.setStaticOnly(meta.getElements().size() == meta.getStaticElements());
        meta.setNameToElement(meta.getElements().stream().filter(e -> !(e instanceof StaticElement)).collect(Collectors.toMap(PatternElement::getName, e -> e)));

        // validate meta
        boolean foundOptional = false;
        for (PatternElement element : meta.getElements()) {
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
    private Map<String, PatternElement> nameToElement;

    private int staticElements;
    private boolean staticOnly;
    private String raw;

    public boolean applicable(@NonNull String pattern) {

        String[] parts = pattern.split(" ");
        if (parts.length != this.getElements().size()) {
            return false;
        }

        if (this.isStaticOnly()) {
            return this.getRaw().equals(pattern);
        }

        for (int i = 0; i < this.getElements().size(); i++) {

            String part = parts[i];
            PatternElement currentElement = this.getElements().get(i);
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

    public boolean matches(@NonNull String args) {

        String[] argsArr = args.split(" ");
        if (argsArr.length < this.getStaticElements()) {
            return false;
        }

        for (int i = 0; i < this.getElements().size(); i++) {

            PatternElement element = this.getElements().get(i);

            // no such index in arguments and not optional (missing element)
            if ((argsArr.length <= i) && !(element instanceof OptionalElement)) {
                return false;
            }

            // static element does not match
            if ((element instanceof StaticElement) && !argsArr[i].equals(element.getName())) {
                return false;
            }

            // empty element
            if ((element instanceof RequiredElement) && argsArr[i].isEmpty()) {
                return false;
            }
        }

        return true;
    }

    public Optional<PatternElement> getElementByName(@NonNull String name) {
        return this.getElements().stream()
                .filter(element -> name.equals(element.getName()))
                .findAny();
    }

    public String getValueByArgument(@NonNull ArgumentMeta argument, @NonNull String[] parts) {

        String name = argument.getName();
        PatternElement element = this.getNameToElement().get(name);

        if (element instanceof OptionalElement) {
            return  (parts.length <= element.getIndex()) ? null : parts[element.getIndex()];
        }

        if (element != null) {
            return parts[element.getIndex()];
        }

        throw new IllegalArgumentException("no such element for named parameter '" + name + " in " + Arrays.toString(parts));
    }
}
