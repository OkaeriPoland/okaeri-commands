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
        return of("", pattern, Collections.emptyList());
    }

    public static PatternMeta of(@NonNull String patternPrefix, @NonNull String pattern, @NonNull List<ArgumentMeta> arguments) {

        // prefix
        if (!patternPrefix.isEmpty()) {
            if (pattern.isEmpty()) {
                pattern = patternPrefix;
            } else {
                pattern = patternPrefix + " " + pattern;
            }
        }
        String finalPattern = pattern;

        // create meta
        AtomicInteger position = new AtomicInteger();
        AtomicInteger argumentIndex = new AtomicInteger();
        PatternMeta meta = new PatternMeta();

        meta.setElements(Collections.unmodifiableList(Arrays.stream(finalPattern.split(" "))
                .map(part -> {
                    int positionValue = position.getAndAdd(PatternElement.getWidthFromPatternElement(part));
                    int argumentValue = argumentIndex.getAndIncrement();
                    PatternElement element = PatternElement.of(part, positionValue);

                    if (!(element instanceof StaticElement)) {

                        if ((element instanceof OptionalElement) && (argumentValue < arguments.size())) {
                            ArgumentMeta argumentMeta = arguments.get(argumentValue);
                            if (!argumentMeta.isOptional()) {
                                throw new IllegalArgumentException("Pattern describes optional element but argument is " +
                                        "not java.lang.Optional nor eu.okaeri.commands.service.Option\nPattern: " + finalPattern + "\nArguments: " + arguments);
                            }
                        }

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
                .collect(Collectors.toList())));
        meta.setNameToElement(meta.getElements().stream().filter(e -> !(e instanceof StaticElement)).collect(Collectors.toMap(PatternElement::getName, e -> e)));
        meta.setRaw(meta.getElements().stream().map(PatternElement::render).collect(Collectors.joining(" ")));

        meta.setStaticElements(Math.toIntExact(meta.getElements().stream().filter(element -> element instanceof StaticElement).count()));
        meta.setStaticOnly(meta.getElements().size() == meta.getStaticElements());

        meta.setConsuming(meta.getElements().stream().mapToInt(PatternElement::getWidth).anyMatch(i -> i == -1));
        meta.setElementsWidth(meta.isConsuming() ? -1 : meta.getElements().stream().mapToInt(PatternElement::getWidth).sum());

        // validate meta (only optional after optional)
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

        // validate meta (only last consuming)
        boolean foundConsuming = false;
        for (PatternElement element : meta.getElements()) {
            if (foundConsuming) {
                throw new IllegalArgumentException("consuming argument (width: -1) should be the last argument: " + pattern);
            }
            if (element.getWidth() != -1) {
                continue;
            }
            foundConsuming = true;
        }

        // validate rendering
        if (!meta.applicable(pattern)) {
            throw new IllegalArgumentException("failed to create PatternMeta, rendered version (" + meta.raw + ") does not match original (" + pattern + ")");
        }

        return meta;
    }

    private List<PatternElement> elements;
    private Map<String, PatternElement> nameToElement;
    private String raw;

    private int staticElements;
    private boolean staticOnly;
    private boolean consuming;
    private int elementsWidth;

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

            // check width
            if (currentElement.getWidth() != testElement.getWidth()) {
                return false;
            }

            // check type equality
            if (!currentElement.getClass().isAssignableFrom(testElement.getClass())) {
                return false;
            }

            String currentName = currentElement.getName();
            String testName = testElement.getName();

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

    public Optional<PatternElement> getCurrentElement(@NonNull String args) {

        boolean opensNext = args.endsWith(" ");
        String[] argsArr = args.split(" ");

        // not consuming and args length exceeds command width
        if (!this.isConsuming() && (argsArr.length > this.getElementsWidth())) {
            return Optional.empty();
        }

        int argAtIndex = ((argsArr.length + (opensNext ? 1 : 0)) - 1);
        List<PatternElement> elements = this.getElements();

        int argIndex = 0;
        int patternIndex = 0;

        for (PatternElement element : elements) {

            // determine end of command args
            if (argIndex >= argsArr.length) {
                // command opens next argument, return if available
                if (opensNext && (elements.size() > patternIndex)) {
                    return Optional.of(elements.get(patternIndex));
                }
                // no match through whole elements
                return Optional.empty();
            }

            // filter out mismatched
            if (element instanceof StaticElement) {
                if (!element.getName().startsWith(argsArr[argIndex])) {
                    return Optional.empty();
                }
            }

            // return element if got this far
            // and is at searched arg index
            if (argIndex == argAtIndex) {
                return Optional.of(element);
            }

            // increment counters
            argIndex += element.getWidth();
            patternIndex += 1;
        }

        return Optional.empty();
    }

    public Optional<PatternElement> getElementAtWidth(int width) {
        int currentWidth = 0;
        for (PatternElement element : this.getElements()) {
            if ((width >= currentWidth) && (width < (currentWidth + element.getWidth()))) {
                return Optional.of(element);
            }
            currentWidth += element.getWidth();
        }
        return Optional.empty();
    }

    public boolean matches(@NonNull String args) {

        String[] argsArr = args.split(" ");
        if (argsArr.length < this.getStaticElements()) {
            return false;
        }

        int argIndex = 0;
        int patternIndex = 0;

        List<PatternElement> elements = this.getElements();
        int elementsSize = elements.size();

        for (PatternElement element : elements) {

            // no such index in arguments and not optional (missing element)
            if ((argsArr.length <= patternIndex) && !(element instanceof OptionalElement)) {
                return false;
            }

            // static element does not match
            if ((element instanceof StaticElement) && !argsArr[argIndex].equals(element.getName())) {
                return false;
            }

            // empty element
            if ((element instanceof RequiredElement) && argsArr[argIndex].isEmpty()) {
                return false;
            }

            // last element
            if (patternIndex == (elementsSize - 1)) {

                // calculate remaining width needed to consume the command
                int remaining = (argsArr.length - argIndex);

                // last element is not consuming the command fully
                if ((element.getWidth() != -1) && (element.getWidth() < remaining)) {
                    return false;
                }

                // last element is over-consuming the command
                if (!(element instanceof OptionalElement) && (element.getWidth() > remaining)) {
                    return false;
                }
            }

            argIndex += element.getWidth();
            patternIndex += 1;
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

        if ((element instanceof OptionalElement) && (parts.length < (element.getIndex() + element.getWidth()))) {
            return null;
        }

        if (element != null) {
            if (element.getWidth() == -1) {
                if (parts.length <= element.getIndex()) {
                    return null;
                }
                return String.join(" ", Arrays.copyOfRange(parts, element.getIndex(), parts.length));
            }
            if (element.getWidth() > 1) {
                return String.join(" ", Arrays.copyOfRange(parts, element.getIndex(), element.getIndex() + element.getWidth()));
            }
            return parts[element.getIndex()];
        }

        throw new IllegalArgumentException("no such element for named parameter '" + name + " in " + Arrays.toString(parts));
    }

    public String render() {
        return this.getElements().stream()
                .map(PatternElement::render)
                .collect(Collectors.joining(" "));
    }
}
