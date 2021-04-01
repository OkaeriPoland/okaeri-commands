package eu.okaeri.commands.meta.pattern.element;

public class RequiredElement extends PatternElement {

    public RequiredElement(String name, int index) {
        super(name, index);
    }

    @Override
    public String render() {
        return (this.getName() == null) ? "*" : ("<" + this.getName() + ">");
    }
}
