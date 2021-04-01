package eu.okaeri.commands.service;

import lombok.*;

@Getter
@ToString
@EqualsAndHashCode
@Setter(AccessLevel.PROTECTED)
public class CommandContext {
    private Object sender;
    private Class<?> senderType;
}
