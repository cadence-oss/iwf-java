package io.iworkflow.integ.rpc;

import io.iworkflow.core.Context;
import io.iworkflow.core.ObjectWorkflow;
import io.iworkflow.core.RPC;
import io.iworkflow.core.StateDef;
import io.iworkflow.core.StateMovement;
import io.iworkflow.core.communication.Communication;
import io.iworkflow.core.persistence.DataAttributeDef;
import io.iworkflow.core.persistence.Persistence;
import io.iworkflow.core.persistence.PersistenceFieldDef;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

import static io.iworkflow.integ.rpc.Keys.*;


@Component
public class NoStartStateWorkflow implements ObjectWorkflow {
    @Override
    public List<StateDef> getWorkflowStates() {
        return Arrays.asList(
                StateDef.nonStartingState(new RpcWorkflowState2())
        );
    }

    @Override
    public List<PersistenceFieldDef> getPersistenceSchema() {
        return Arrays.asList(
                DataAttributeDef.create(Integer.class, COUNTER_KEY),
                DataAttributeDef.create(Integer.class, MAX_COUNTER)
        );
    }


    @RPC
    public Long testRpcFunc1(Context context, String input, Persistence persistence, Communication communication) {
        if (context.getWorkflowId().isEmpty() || context.getWorkflowRunId().isEmpty()) {
            throw new RuntimeException("invalid context");
        }
        communication.triggerStateMovements(StateMovement.create(RpcWorkflowState2.class));
        return RPC_OUTPUT;
    }
}
