package iwf.core;

import com.google.common.base.Preconditions;
import iwf.gen.api.ApiClient;
import iwf.gen.api.DefaultApi;
import iwf.gen.models.*;

public class Client {
    private final Registry registry;
    private final DefaultApi defaultApi;

    private final ClientOptions clientOptions;

    private final ObjectEncoder objectEncoder = new JacksonJsonObjectEncoder();

    public Client(final Registry registry, final ClientOptions clientOptions) {
        this.clientOptions = clientOptions;
        this.registry = registry;
        this.defaultApi = new ApiClient()
                .setBasePath(clientOptions.getServerUrl())
                .buildClient(DefaultApi.class);
    }

    public void SignalWorkflow(
            final String workflowId,
            final String workflowRunId,
            final String signalName,
            final Object signalValue) {
        defaultApi.apiV1WorkflowSignalPost(new WorkflowSignalRequest()
                .workflowId(workflowId)
                .workflowRunId(workflowRunId)
                .signalName(signalName)
                .signalValue(new EncodedObject()
                        .encoding(objectEncoder.getEncodingType())
                        .data(objectEncoder.toData(signalValue))));
    }

    public String StartWorkflow(
            final Class<? extends Workflow> workflowClass,
            final String startStateId,
            final String workflowId,
            final WorkflowStartOptions options) {
        return StartWorkflow(workflowClass, startStateId, null, workflowId, options);
    }
    
    public String StartWorkflow(
            final Class<? extends Workflow> workflowClass,
            final String startStateId,
            final Object input,
            final String workflowId,
            final WorkflowStartOptions options) {
        final String wfType = workflowClass.getSimpleName();
        final StateDef stateDef = registry.getWorkflowState(wfType, startStateId);
        if (stateDef == null || !stateDef.getCanStartWorkflow()) {
            throw new RuntimeException("invalid start stateId " + startStateId);
        }

        final String data = objectEncoder.toData(input);

        WorkflowStartResponse workflowStartResponse = defaultApi.apiV1WorkflowStartPost(new WorkflowStartRequest()
                .workflowId(workflowId)
                .iwfWorkerUrl(clientOptions.getWorkerUrl())
                .iwfWorkflowType(wfType)
                .workflowTimeoutSeconds(options.getWorkflowTimeoutSeconds())
                .stateInput(new EncodedObject()
                        .encoding(objectEncoder.getEncodingType())
                        .data(data)
                )
                .startStateId(startStateId));
        return workflowStartResponse.getWorkflowRunId();
    }

    public <T> T GetSingleWorkflowStateOutputWithLongWait(
            Class<T> valueClass,
            final String workflowId) {
        WorkflowGetResponse workflowGetResponse = defaultApi.apiV1WorkflowGetWithLongWaitPost(
                new WorkflowGetRequest()
                        .needsResults(true)
                        .workflowId(workflowId)
        );

        String checkErrorMessage = "this workflow should have exactly one state output";
        Preconditions.checkNotNull(workflowGetResponse.getResults(), checkErrorMessage);
        Preconditions.checkArgument(workflowGetResponse.getResults().size() == 1, checkErrorMessage);
        Preconditions.checkNotNull(workflowGetResponse.getResults().get(0).getCompletedStateOutput(), checkErrorMessage);

        //TODO validate encoding type

        final StateCompletionOutput output = workflowGetResponse.getResults().get(0);
        return objectEncoder.fromData(output.getCompletedStateOutput().getData(), valueClass);
    }
}
