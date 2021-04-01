package eu.okaeri.commands.meta.pattern.element;

public class StaticElement extends PatternElement {

    public StaticElement(String name, int index) {
        super(name, index);
    }

    @Override
    public String render() {
        return this.getName();
    }
}