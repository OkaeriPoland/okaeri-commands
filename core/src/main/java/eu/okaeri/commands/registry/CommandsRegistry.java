package eu.okaeri.commands.registry;

import eu.okaeri.commands.meta.CommandMeta;
import eu.okaeri.commands.service.CommandService;

import java.util.List;
import java.util.Optional;

public interface CommandsRegistry {

    CommandsRegistry register(Class<? extends CommandService> clazz);

    CommandsRegistry register(CommandService service);

    List<CommandMeta> findByLabel(String label);

    Optional<CommandMeta> findByLabelAndArgs(String label, String args);
}
