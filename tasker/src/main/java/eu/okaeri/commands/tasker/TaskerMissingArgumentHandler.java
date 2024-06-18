package eu.okaeri.commands.tasker;

import eu.okaeri.commands.handler.argument.MissingArgumentHandler;
import eu.okaeri.commands.meta.CommandMeta;
import eu.okaeri.commands.service.CommandData;
import eu.okaeri.commands.service.Invocation;
import eu.okaeri.commands.tasker.annotation.Chain;
import eu.okaeri.tasker.core.Tasker;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.Parameter;

@RequiredArgsConstructor
public class TaskerMissingArgumentHandler implements MissingArgumentHandler {

    protected final Tasker tasker;
    protected final MissingArgumentHandler parent;

    @Override
    public Object resolve(@NonNull Invocation invocation, @NonNull CommandData data, @NonNull CommandMeta command, @NonNull Parameter param, int index) {

        Chain chain = param.getAnnotation(Chain.class);
        if (chain == null) {
            return this.parent.resolve(invocation, data, command, param, index);
        }

        String chainName = chain.value();
        if (chainName.isEmpty()) {
            return this.tasker.newChain();
        }

        return this.tasker.newSharedChain(chainName);
    }
}
