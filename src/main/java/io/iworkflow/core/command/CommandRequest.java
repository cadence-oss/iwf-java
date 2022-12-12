package io.iworkflow.core.command;

import org.immutables.value.Value;

import java.util.Arrays;
import java.util.List;

@Value.Immutable
public abstract class CommandRequest {
    public abstract List<BaseCommand> getCommands();

    public abstract io.iworkflow.gen.models.CommandRequest.DeciderTriggerTypeEnum getDeciderTriggerType();

    // empty command request will jump to decide stage immediately.
    // It doesn't matter whatever DeciderTriggerType is provided. But it's required so we have to put one.
    public static final CommandRequest empty = ImmutableCommandRequest.builder().deciderTriggerType(io.iworkflow.gen.models.CommandRequest.DeciderTriggerTypeEnum.ALL_COMMAND_COMPLETED).build();

    public static CommandRequest forAllCommandCompleted(final BaseCommand... commands) {
        return ImmutableCommandRequest.builder().addAllCommands(Arrays.asList(commands)).deciderTriggerType(io.iworkflow.gen.models.CommandRequest.DeciderTriggerTypeEnum.ALL_COMMAND_COMPLETED).build();
    }

    public static CommandRequest forAnyCommandCompleted(final BaseCommand... commands) {
        return ImmutableCommandRequest.builder().addAllCommands(Arrays.asList(commands)).deciderTriggerType(io.iworkflow.gen.models.CommandRequest.DeciderTriggerTypeEnum.ANY_COMMAND_COMPLETED).build();
    }
}
