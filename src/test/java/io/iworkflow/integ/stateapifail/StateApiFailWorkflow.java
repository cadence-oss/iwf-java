package io.iworkflow.integ.stateapifail;

import io.iworkflow.core.StateDef;
import io.iworkflow.core.Workflow;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class StateApiFailWorkflow implements Workflow {
    @Override
    public List<StateDef> getStates() {
        return Arrays.asList(
                StateDef.startingState(new StateApiFailWorkflowState1())
        );
    }
}
