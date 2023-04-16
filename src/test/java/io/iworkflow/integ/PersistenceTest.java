package io.iworkflow.integ;

import com.google.common.collect.ImmutableMap;
import io.iworkflow.core.Client;
import io.iworkflow.core.ClientOptions;
import io.iworkflow.integ.persistence.BasicPersistenceWorkflow;
import io.iworkflow.spring.TestSingletonWorkerService;
import io.iworkflow.spring.controller.WorkflowRegistry;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static io.iworkflow.integ.persistence.BasicPersistenceWorkflow.TEST_SEARCH_ATTRIBUTE_INT;
import static io.iworkflow.integ.persistence.BasicPersistenceWorkflow.TEST_SEARCH_ATTRIBUTE_KEYWORD;

public class PersistenceTest {

    @BeforeEach
    public void setup() throws ExecutionException, InterruptedException {
        TestSingletonWorkerService.startWorkerIfNotUp();
    }

    @Test
    public void testPersistenceWorkflow() throws InterruptedException {
        final Client client = new Client(WorkflowRegistry.registry, ClientOptions.localDefault);
        final String wfId = "basic-persistence-test-id" + System.currentTimeMillis() / 1000;
        final String runId = client.createObject(
                BasicPersistenceWorkflow.class, wfId, 10, "start");
        final String output = client.getSingleResultWithWait(String.class, wfId);
        Map<String, Object> map =
                client.getDataAttributes(BasicPersistenceWorkflow.class, wfId, runId, Arrays.asList(BasicPersistenceWorkflow.TEST_DATA_OBJECT_KEY));
        Assertions.assertEquals(
                "query-start-query-decide", map.get(BasicPersistenceWorkflow.TEST_DATA_OBJECT_KEY));
        Map<String, Object> allDataObjects = client.getAllDataAttributes(BasicPersistenceWorkflow.class, wfId, runId);
        Assertions.assertEquals(3, allDataObjects.size());

        Assertions.assertEquals("query-start-query-decide", allDataObjects.get(BasicPersistenceWorkflow.TEST_DATA_OBJECT_KEY));

        Assertions.assertEquals("test-value-2", output);

        final Map<String, Object> searchAttributes1 = client.getSearchAttributes(BasicPersistenceWorkflow.class,
                wfId, "", Arrays.asList(TEST_SEARCH_ATTRIBUTE_KEYWORD, TEST_SEARCH_ATTRIBUTE_INT));

        final Map<String, Object> searchAttributes2 = client.getAllSearchAttributes(BasicPersistenceWorkflow.class,
                wfId, "");

        Assertions.assertEquals(searchAttributes1, searchAttributes2);
        Assertions.assertEquals(ImmutableMap.builder()
                .put(TEST_SEARCH_ATTRIBUTE_INT, 2L)
                .put(TEST_SEARCH_ATTRIBUTE_KEYWORD, "keyword-2")
                .build(), searchAttributes1);
    }

}
