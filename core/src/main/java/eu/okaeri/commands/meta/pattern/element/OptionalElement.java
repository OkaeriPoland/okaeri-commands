package eu.okaeri.commands.meta.pattern.element;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class OptionalElement extends PatternElement {

    public OptionalElement(String name, int index, int width) {
        super(name, index, width);
    }

    @Override
    public String render() {
        if (this.getWidth() == -1) {
            return (this.getName() == null) ? "?..." : ("[" + this.getName() + "...]");
        }
        if (this.getWidth() > 1) {
            return (this.getName() == null) ? ("?:" + this.getWidth()) : ("[" + this.getName() + ":" + this.getWidth() + "]");
        }
        return (this.getName() == null) ? "?" : ("[" + this.getName() + "]");
    }
}
