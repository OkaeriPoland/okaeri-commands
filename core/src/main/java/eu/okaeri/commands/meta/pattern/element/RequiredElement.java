package eu.okaeri.commands.meta.pattern.element;

public class RequiredElement extends PatternElement {

    public RequiredElement(String name) {
        super(name);
    }

    @Override
    public String render() {
        return "<" + this.getName() + ">";
    }
}
