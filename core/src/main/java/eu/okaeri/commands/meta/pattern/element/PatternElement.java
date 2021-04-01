package eu.okaeri.commands.meta.pattern.element;

import lombok.*;

@Data
@AllArgsConstructor
public abstract class PatternElement {

    private String name;

    public abstract String render();
}
