package io.iworkflow.core.communication;

import org.immutables.value.Value;

@Value.Immutable
public abstract class InternalChannelDef implements CommunicationMethodDef {

    /**
     * iWF will verify if the name has been registered for the internal channel created using this method,
     * allowing users to create only one internal channel with the same name and type.
     *
     * @param type  required.
     * @param name  required. The unique name.
     * @return an internal channel definition
     */
    public static InternalChannelDef create(final Class type, final String name) {
        return ImmutableInternalChannelDef.builder()
                .name(name)
                .valueType(type)
                .isPrefix(false)
                .build();
    }

    /**
     * iWF now supports dynamically created internal channels with a shared name prefix and the same type.
     * (E.g., dynamically created internal channels of type String can be named with a common prefix like: internal_channel_prefix_1: "one", internal_channel_prefix_2: "two")
     * iWF will verify if the prefix has been registered for internal channels created using this method,
     * allowing users to create multiple internal channels with the same name prefix and type.
     *
     * @param type          required.
     * @param namePrefix    required. The common name prefix of a set of internal channels to be created later.
     * @return an internal channel definition
     */
    public static InternalChannelDef createByPrefix(final Class type, final String namePrefix) {
        return ImmutableInternalChannelDef.builder()
                .name(namePrefix)
                .valueType(type)
                .isPrefix(true)
                .build();
    }
}
