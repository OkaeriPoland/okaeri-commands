package eu.okaeri.commands.meta.pattern.element;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class StaticElement extends PatternElement {

    public StaticElement(String name, int index) {
        super(name, index, 1);
    }

    @Override
    public String render() {
        return this.getName();
    }
}