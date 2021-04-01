package eu.okaeri.commands.meta.pattern.element;

public class StaticElement extends PatternElement {

    public StaticElement(String name) {
        super(name);
    }

    @Override
    public String render() {
        return this.getName();
    }
}