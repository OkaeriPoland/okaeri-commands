package eu.okaeri.commands.meta.pattern.element;

public class OptionalElement extends PatternElement {

    public OptionalElement(String name, int index) {
        super(name, index);
    }

    @Override
    public String render() {
        return (this.getName() == null) ? "?" : ("[" + this.getName() + "]");
    }
}
