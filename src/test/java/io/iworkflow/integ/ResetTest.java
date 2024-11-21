package io.iworkflow.integ;

import com.google.common.collect.ImmutableMap;
import io.iworkflow.core.Client;
import io.iworkflow.core.ClientOptions;
import io.iworkflow.core.ResetWorkflowTypeAndOptions;
import io.iworkflow.gen.models.StateCompletionOutput;
import io.iworkflow.gen.models.WorkflowResetType;
import io.iworkflow.gen.models.WorkflowStatus;
import io.iworkflow.integ.rpc.RpcLockingWorkflow;
import io.iworkflow.integ.rpc.RpcLockingWorkflowState2;
import io.iworkflow.spring.TestSingletonWorkerService;
import io.iworkflow.spring.controller.WorkflowRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static io.iworkflow.integ.rpc.RpcLockingWorkflow.TEST_DATA_OBJECT_KEY;
import static io.iworkflow.integ.rpc.RpcLockingWorkflow.TEST_SEARCH_ATTRIBUTE_INT;
import static io.iworkflow.integ.rpc.RpcLockingWorkflow.TEST_SEARCH_ATTRIBUTE_KEYWORD;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ResetTest {

    public static final Long RPC_OUTPUT = 100L;
    public static final String HARDCODED_STR = "random-string";

    @BeforeEach
    public void setup() throws ExecutionException, InterruptedException {
        TestSingletonWorkerService.startWorkerIfNotUp();
    }

    @Test
    public void testResetWithLockingReapplyUpdate() {
        final Client client = new Client(WorkflowRegistry.registry, ClientOptions.localDefault);
        final String wfId = "test_reset_with_locking_reapply_update" + System.currentTimeMillis() / 1000;
        final String runId = client.startWorkflow(
                RpcLockingWorkflow.class, wfId, 120);

        final RpcLockingWorkflow rpcStub = client.newRpcStub(RpcLockingWorkflow.class, wfId);
        client.invokeRPC(rpcStub::testRpcWithLocking);

        final List<StateCompletionOutput> result = client.getComplexWorkflowResultWithWait(wfId);
        //How many times RpcLockingWorkflowState2 is reached.
        assertEquals(result.get(1).getCompletedStateOutput().getData(), "\"The execute method was executed 2 times\"");

        //reset the execution count in RpcLockingWorkflowState2 for the reset workflow.
        RpcLockingWorkflowState2.resetCounter();

        final WorkflowStatus originalWorkflowStatus = client.describeWorkflow(wfId, runId).getWorkflowStatus();

        final Map<String, Object> originalDataAttributes = client.getAllDataAttributes(RpcLockingWorkflow.class, wfId, runId);

        final Map<String, Object> originalSearchAttributes = client.getAllSearchAttributes(RpcLockingWorkflow.class, wfId, runId);

        final Map<String, Object> expectedDataAttributes = ImmutableMap.of(
                TEST_DATA_OBJECT_KEY, HARDCODED_STR
        );

        final Map<String, Object> expectedSearchAttributes = ImmutableMap.of(
                TEST_SEARCH_ATTRIBUTE_KEYWORD, HARDCODED_STR,
                TEST_SEARCH_ATTRIBUTE_INT, RPC_OUTPUT
        );

        assertEquals(originalWorkflowStatus, WorkflowStatus.COMPLETED);

        //First workflow set data attributes and search attributes
        assertEquals(expectedDataAttributes, originalDataAttributes);
        assertEquals(expectedSearchAttributes, originalSearchAttributes);
        //reset the count in RpcLockingWorkflowState2 for the reset workflow.
        RpcLockingWorkflowState2.resetCounter();

        final String currentWorkflowRunId = client.resetWorkflow(wfId, runId, ResetWorkflowTypeAndOptions.builder()
                .resetType(WorkflowResetType.BEGINNING)
                .reason("testing reset")
                .build());
        final List<StateCompletionOutput> replayResult = client.getComplexWorkflowResultWithWait(wfId);
        //How many times RpcLockingWorkflowState2 is reached.
        assertEquals(replayResult.get(1).getCompletedStateOutput().getData(), "\"The execute method was executed 2 times\"");

        //reset the execution count in RpcLockingWorkflowState2 for the reset workflow.
        RpcLockingWorkflowState2.resetCounter();

        final WorkflowStatus resetWorkflowStatus = client.describeWorkflow(wfId, currentWorkflowRunId).getWorkflowStatus();

        final Map<String, Object> currentDataAttributes = client.getAllDataAttributes(RpcLockingWorkflow.class, wfId, currentWorkflowRunId);

        final Map<String, Object> currentSearchAttributes = client.getAllSearchAttributes(RpcLockingWorkflow.class, wfId, currentWorkflowRunId);

        assertEquals(expectedDataAttributes, currentDataAttributes);
        assertEquals(expectedSearchAttributes, currentSearchAttributes);
        assertEquals(resetWorkflowStatus, WorkflowStatus.COMPLETED);

    }

    @Test
    public void testResetWithLockingSkipReapplyUpdate() throws InterruptedException {
        final Client client = new Client(WorkflowRegistry.registry, ClientOptions.localDefault);
        final String wfId = "test_reset_with_locking_skip_reapply_update" + System.currentTimeMillis() / 1000;
        final String runId = client.startWorkflow(
                RpcLockingWorkflow.class, wfId, 10);

        final RpcLockingWorkflow rpcStub = client.newRpcStub(RpcLockingWorkflow.class, wfId);
        client.invokeRPC(rpcStub::testRpcWithLocking);

        final List<StateCompletionOutput> result = client.getComplexWorkflowResultWithWait(wfId, runId);
        //How many times RpcLockingWorkflowState2 is reached.
        assertEquals(result.get(1).getCompletedStateOutput().getData(), "\"The execute method was executed 2 times\"");

        //reset the execution count in RpcLockingWorkflowState2 for the reset workflow.
        RpcLockingWorkflowState2.resetCounter();

        final WorkflowStatus originalWorkflowStatus = client.describeWorkflow(wfId, runId).getWorkflowStatus();

        final Map<String, Object> originalDataAttributes = client.getAllDataAttributes(RpcLockingWorkflow.class, wfId, runId);

        final Map<String, Object> originalSearchAttributes = client.getAllSearchAttributes(RpcLockingWorkflow.class, wfId, runId);

        final Map<String, Object> expectedDataAttributes = ImmutableMap.of(
                TEST_DATA_OBJECT_KEY, HARDCODED_STR
        );

        final Map<String, Object> expectedSearchAttributes = ImmutableMap.of(
                TEST_SEARCH_ATTRIBUTE_KEYWORD, HARDCODED_STR,
                TEST_SEARCH_ATTRIBUTE_INT, RPC_OUTPUT
        );

        //First workflow completed
        assertEquals(originalWorkflowStatus, WorkflowStatus.COMPLETED);
        //First workflow set data attributes and search attributes
        assertEquals(expectedDataAttributes, originalDataAttributes);
        assertEquals(expectedSearchAttributes, originalSearchAttributes);
        //reset the count in RpcLockingWorkflowState2 for the reset workflow.
        RpcLockingWorkflowState2.resetCounter();

        //Skip reapplying update meaning locking RPC calls will not be reapplyed.
        final String currentWorkflowRunId = client.resetWorkflow(wfId, runId, ResetWorkflowTypeAndOptions.builder()
                .resetType(WorkflowResetType.BEGINNING)
                .reason("testing reset")
                .skipUpdateReapply(true)
                .skipSignalReapply(true) //Skipping signal reapply won't change history, because only locking RPC calls were invoked. There are no signals in history.
                .build());

        Thread.sleep(10000); //wait 10 seconds for workflow to timeout.

        final WorkflowStatus resetWorkflowStatus = client.describeWorkflow(wfId, currentWorkflowRunId).getWorkflowStatus();
        //Since the locking RPC calls were not reapplied we don't expect the data attributes or the search attrbutes to be set.
        assertThrows(IllegalStateException.class, () -> client.getAllDataAttributes(RpcLockingWorkflow.class, wfId, currentWorkflowRunId));

        assertThrows(IllegalStateException.class, () -> client.getAllSearchAttributes(RpcLockingWorkflow.class, wfId, currentWorkflowRunId));
        //Rpc call is not reapplied so workflow times out.
        assertEquals(resetWorkflowStatus, WorkflowStatus.TIMEOUT);
    }

    @Test
    public void testResetWithoutLockingReapplySignal() throws InterruptedException {
        final Client client = new Client(WorkflowRegistry.registry, ClientOptions.localDefault);
        final String wfId = "test_reset_no_locking_reapply_signal" + System.currentTimeMillis() / 1000;
        final String runId = client.startWorkflow(
                RpcLockingWorkflow.class, wfId, 10);

        final RpcLockingWorkflow rpcStub = client.newRpcStub(RpcLockingWorkflow.class, wfId);
        client.invokeRPC(rpcStub::testRpcWithoutLocking);

        final List<StateCompletionOutput> result = client.getComplexWorkflowResultWithWait(wfId);
        //How many times RpcLockingWorkflowState2 is reached.
        assertEquals(result.get(1).getCompletedStateOutput().getData(), "\"The execute method was executed 2 times\"");

        //reset the execution count in RpcLockingWorkflowState2 for the reset workflow.
        RpcLockingWorkflowState2.resetCounter();

        final WorkflowStatus originalWorkflowStatus = client.describeWorkflow(wfId, runId).getWorkflowStatus();

        final Map<String, Object> originalDataAttributes = client.getAllDataAttributes(RpcLockingWorkflow.class, wfId, runId);

        final Map<String, Object> originalSearchAttributes = client.getAllSearchAttributes(RpcLockingWorkflow.class, wfId, runId);

        final Map<String, Object> expectedDataAttributes = ImmutableMap.of(
                TEST_DATA_OBJECT_KEY, HARDCODED_STR
        );

        final Map<String, Object> expectedSearchAttributes = ImmutableMap.of(
                TEST_SEARCH_ATTRIBUTE_KEYWORD, HARDCODED_STR,
                TEST_SEARCH_ATTRIBUTE_INT, RPC_OUTPUT
        );

        assertEquals(originalWorkflowStatus, WorkflowStatus.COMPLETED);

        //First workflow set data attributes and search attributes
        assertEquals(expectedDataAttributes, originalDataAttributes);
        assertEquals(expectedSearchAttributes, originalSearchAttributes);

        final String currentWorkflowRunId = client.resetWorkflow(wfId, runId, ResetWorkflowTypeAndOptions.builder()
                .resetType(WorkflowResetType.BEGINNING)
                .reason("testing reset")
                .build());

        final List<StateCompletionOutput> currentResult = client.getComplexWorkflowResultWithWait(wfId, currentWorkflowRunId);
        final WorkflowStatus resetWorkflowStatus = client.describeWorkflow(wfId, currentWorkflowRunId).getWorkflowStatus();

        final Map<String, Object> currentDataAttributes = client.getAllDataAttributes(RpcLockingWorkflow.class, wfId, currentWorkflowRunId);

        final Map<String, Object> currentSearchAttributes = client.getAllSearchAttributes(RpcLockingWorkflow.class, wfId, currentWorkflowRunId);

        //How many times RpcLockingWorkflowState2 is reached.
        assertEquals(currentResult.get(1).getCompletedStateOutput().getData(), "\"The execute method was executed 2 times\"");

        //reset the execution count in RpcLockingWorkflowState2 for the reset workflow.
        RpcLockingWorkflowState2.resetCounter();

        assertEquals(WorkflowStatus.COMPLETED, resetWorkflowStatus);
        assertEquals(expectedDataAttributes, currentDataAttributes);
        assertEquals(expectedSearchAttributes, currentSearchAttributes);
    }

    @Test
    public void testResetWithoutLockingSkipReapplySignal() throws InterruptedException {
        final Client client = new Client(WorkflowRegistry.registry, ClientOptions.localDefault);
        final String wfId = "test_reset_no_locking_skip_reapply_signal" + System.currentTimeMillis() / 1000;
        final String runId = client.startWorkflow(
                RpcLockingWorkflow.class, wfId, 10);

        final RpcLockingWorkflow rpcStub = client.newRpcStub(RpcLockingWorkflow.class, wfId);
        client.invokeRPC(rpcStub::testRpcWithoutLocking);

        final List<StateCompletionOutput> result = client.getComplexWorkflowResultWithWait(wfId);
        //How many times RpcLockingWorkflowState2 is reached.
        assertEquals(result.get(1).getCompletedStateOutput().getData(), "\"The execute method was executed 2 times\"");

        //reset the execution count in RpcLockingWorkflowState2 for the reset workflow.
        RpcLockingWorkflowState2.resetCounter();

        final WorkflowStatus originalWorkflowStatus = client.describeWorkflow(wfId, runId).getWorkflowStatus();

        final Map<String, Object> originalDataAttributes = client.getAllDataAttributes(RpcLockingWorkflow.class, wfId, runId);

        final Map<String, Object> originalSearchAttributes = client.getAllSearchAttributes(RpcLockingWorkflow.class, wfId, runId);

        final Map<String, Object> expectedDataAttributes = ImmutableMap.of(
                TEST_DATA_OBJECT_KEY, HARDCODED_STR
        );

        final Map<String, Object> expectedSearchAttributes = ImmutableMap.of(
                TEST_SEARCH_ATTRIBUTE_KEYWORD, HARDCODED_STR,
                TEST_SEARCH_ATTRIBUTE_INT, RPC_OUTPUT
        );

        assertEquals(originalWorkflowStatus, WorkflowStatus.COMPLETED);

        //First workflow set data attributes and search attributes
        assertEquals(expectedDataAttributes, originalDataAttributes);
        assertEquals(expectedSearchAttributes, originalSearchAttributes);

        final String currentWorkflowRunId = client.resetWorkflow(wfId, runId, ResetWorkflowTypeAndOptions.builder()
                .resetType(WorkflowResetType.BEGINNING)
                .skipSignalReapply(true)
                .skipUpdateReapply(true) //Skipping update reapply won't change history, because only non-locking RPC calls were invoked. There are no updates in history.
                .reason("testing reset")
                .build());

        Thread.sleep(10000); //wait 10 seconds for workflow to timeout.

        final WorkflowStatus resetWorkflowStatus = client.describeWorkflow(wfId, currentWorkflowRunId).getWorkflowStatus();
        //Since the non-locking RPC calls were not reapplied we don't expect the data attributes or the search attributes to be set.
        assertThrows(IllegalStateException.class, () -> client.getAllDataAttributes(RpcLockingWorkflow.class, wfId, currentWorkflowRunId));

        assertThrows(IllegalStateException.class, () -> client.getAllSearchAttributes(RpcLockingWorkflow.class, wfId, currentWorkflowRunId));
        //Rpc call is not reapplied so workflow times out.
        assertEquals(WorkflowStatus.TIMEOUT, resetWorkflowStatus, "Workflow with runId " + currentWorkflowRunId + " has the wrong status");
    }
}
