package eu.okaeri.commands.meta.pattern.element;

public class OptionalElement extends PatternElement {

    public OptionalElement(String name) {
        super(name);
    }

    @Override
    public String render() {
        return "[" + this.getName() + "]";
    }
}
