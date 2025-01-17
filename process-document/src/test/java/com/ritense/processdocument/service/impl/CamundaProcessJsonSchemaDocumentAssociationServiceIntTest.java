/*
 * Copyright 2015-2024 Ritense BV, the Netherlands.
 *
 * Licensed under EUPL, Version 1.2 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ritense.processdocument.service.impl;

import static com.ritense.authorization.AuthorizationContext.runWithoutAuthorization;
import static com.ritense.valtimo.contract.authentication.AuthoritiesConstants.ADMIN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ritense.authorization.AuthorizationContext;
import com.ritense.document.domain.Document;
import com.ritense.document.domain.DocumentDefinition;
import com.ritense.document.domain.impl.JsonSchemaDocumentDefinitionId;
import com.ritense.document.domain.impl.request.ModifyDocumentRequest;
import com.ritense.document.domain.impl.request.NewDocumentRequest;
import com.ritense.document.service.DocumentDefinitionService;
import com.ritense.processdocument.BaseIntegrationTest;
import com.ritense.processdocument.domain.impl.OperatonProcessDefinitionKey;
import com.ritense.processdocument.domain.impl.OperatonProcessJsonSchemaDocumentDefinition;
import com.ritense.processdocument.domain.impl.OperatonProcessJsonSchemaDocumentDefinitionId;
import com.ritense.processdocument.domain.impl.OperatonProcessJsonSchemaDocumentInstance;
import com.ritense.processdocument.domain.impl.request.ModifyDocumentAndCompleteTaskRequest;
import com.ritense.processdocument.domain.impl.request.NewDocumentAndStartProcessRequest;
import com.ritense.processdocument.domain.impl.request.ProcessDocumentDefinitionRequest;
import com.ritense.processdocument.service.result.ModifyDocumentAndCompleteTaskResult;
import com.ritense.processdocument.service.result.NewDocumentAndStartProcessResult;
import com.ritense.valtimo.repository.operaton.dto.TaskInstanceWithIdentityLink;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional;
import org.operaton.bpm.engine.RuntimeService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;

@Tag("integration")
@Transactional
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class OperatonProcessJsonSchemaDocumentAssociationServiceIntTest extends BaseIntegrationTest {

    private static final String DOCUMENT_DEFINITION_NAME = "house";
    private static final String DOCUMENT_DEFINITION_NAME2 = "notahouse";
    private static final String PROCESS_DEFINITION_KEY = "loan-process-demo";

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private DocumentDefinitionService documentDefinitionService;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private ObjectMapper objectMapper;

    private DocumentDefinition oldDocumentDefinition;
    private DocumentDefinition newDocumentDefinition;

    @BeforeAll
    public void setup() {
        runWithoutAuthorization(() -> {
            String oldDocumentDefinitionVersion = """
                {
                    "$id": "some-test.schema",
                    "$schema": "http://json-schema.org/draft-07/schema#",
                    "title": "some-test",
                    "type": "object",
                    "properties": {
                        "name": {
                            "type": "string"
                        }
                    }
                }
            """;
            oldDocumentDefinition = documentDefinitionService.deploy(oldDocumentDefinitionVersion).documentDefinition();

            String newDocumentDefinitionVersion = """
                {
                    "$id": "some-test.schema",
                    "$schema": "http://json-schema.org/draft-07/schema#",
                    "title": "some-test",
                    "type": "object",
                    "properties": {
                        "name": {
                            "type": "integer"
                        }
                    }
                }
            """;
            newDocumentDefinition = documentDefinitionService.deploy(newDocumentDefinitionVersion).documentDefinition();

            return null;
        });
    }

    @Test
    public void findProcessDocumentDefinition() {
        final var processDocumentDefinitions = AuthorizationContext
            .runWithoutAuthorization(() -> operatonProcessJsonSchemaDocumentAssociationService
            .findProcessDocumentDefinitions(DOCUMENT_DEFINITION_NAME));

        assertThat(processDocumentDefinitions.size()).isGreaterThanOrEqualTo(1);
        assertThat(processDocumentDefinitions.get(0).processDocumentDefinitionId().processDefinitionKey().toString()).isEqualTo(PROCESS_DEFINITION_KEY);
        assertThat(processDocumentDefinitions.get(0).processDocumentDefinitionId().documentDefinitionId().name()).isEqualTo(DOCUMENT_DEFINITION_NAME);
    }

    @Test
    public void findProcessDocumentDefinitionByProcessDefinitionKey() {
        final var processDocumentDefinitions = runWithoutAuthorization(() ->
            operatonProcessJsonSchemaDocumentAssociationService.findProcessDocumentDefinitionsByProcessDefinitionKey(PROCESS_DEFINITION_KEY)
        );

        assertThat(processDocumentDefinitions.size()).isEqualTo(1);
        assertThat(processDocumentDefinitions.get(0).processDocumentDefinitionId().processDefinitionKey().toString()).isEqualTo(PROCESS_DEFINITION_KEY);
        assertThat(processDocumentDefinitions.get(0).processDocumentDefinitionId().documentDefinitionId().name()).isEqualTo(DOCUMENT_DEFINITION_NAME);
    }

    @Test
    public void shouldCreateProcessDocumentDefinition() {
        var request = new ProcessDocumentDefinitionRequest(
            "embedded-subprocess-example",
            DOCUMENT_DEFINITION_NAME,
            true,
            true
        );

        final var optionalProcessDocumentDefinition = runWithoutAuthorization(() -> operatonProcessJsonSchemaDocumentAssociationService
            .createProcessDocumentDefinition(request));

        assertThat(optionalProcessDocumentDefinition).isPresent();
    }

    @Test
    public void shouldCreateProcessDocumentDefinitionForOldVersion() {
        var request = new ProcessDocumentDefinitionRequest(
            "embedded-subprocess-example",
            oldDocumentDefinition.id().name(),
            true,
            true,
            Optional.of(oldDocumentDefinition.id().version())
        );

        runWithoutAuthorization(() -> operatonProcessJsonSchemaDocumentAssociationService
            .createProcessDocumentDefinition(request));

        List<OperatonProcessJsonSchemaDocumentDefinition> oldProcessDocumentDefinitions = operatonProcessJsonSchemaDocumentAssociationService
            .findProcessDocumentDefinitions(oldDocumentDefinition.id().name(), oldDocumentDefinition.id().version());

        List<OperatonProcessJsonSchemaDocumentDefinition> newProcessDocumentDefinitions = operatonProcessJsonSchemaDocumentAssociationService
            .findProcessDocumentDefinitions(newDocumentDefinition.id().name(), newDocumentDefinition.id().version());

        assertThat(oldProcessDocumentDefinitions.size()).isEqualTo(1);
        assertThat(newProcessDocumentDefinitions.size()).isEqualTo(0);
    }

    @Test
    public void shouldDeleteProcessDocumentDefinitionForOldVersion() {
        //create association for old version
        var request = new ProcessDocumentDefinitionRequest(
            "embedded-subprocess-example",
            oldDocumentDefinition.id().name(),
            true,
            true,
            Optional.of(oldDocumentDefinition.id().version())
        );

        runWithoutAuthorization(() -> operatonProcessJsonSchemaDocumentAssociationService
            .createProcessDocumentDefinition(request));

        //create association for new version
        var request2 = new ProcessDocumentDefinitionRequest(
            "embedded-subprocess-example",
            newDocumentDefinition.id().name(),
            true,
            true,
            Optional.of(newDocumentDefinition.id().version())
        );

        runWithoutAuthorization(() -> operatonProcessJsonSchemaDocumentAssociationService
            .createProcessDocumentDefinition(request2));

        //delete association for old version
        runWithoutAuthorization(() -> {
            operatonProcessJsonSchemaDocumentAssociationService.deleteProcessDocumentDefinition(request);
            return null;
        });

        // can't delete and select in the same transaction with JPA so we check the call instead
        verify(processDocumentDefinitionRepository).deleteById(
            OperatonProcessJsonSchemaDocumentDefinitionId.existingId(
                new OperatonProcessDefinitionKey("embedded-subprocess-example"),
                JsonSchemaDocumentDefinitionId.existingId(
                    "some-test",
                    1
                )
            )
        );
    }

    @Test
    public void shouldCreateProcessDocumentDefinitionForNewVersionWhenNoVersionSpecified() {
        var request = new ProcessDocumentDefinitionRequest(
            "embedded-subprocess-example",
            oldDocumentDefinition.id().name(),
            true,
            true
        );

        runWithoutAuthorization(() -> operatonProcessJsonSchemaDocumentAssociationService
            .createProcessDocumentDefinition(request));

        List<OperatonProcessJsonSchemaDocumentDefinition> oldProcessDocumentDefinitions = operatonProcessJsonSchemaDocumentAssociationService
            .findProcessDocumentDefinitions(oldDocumentDefinition.id().name(), oldDocumentDefinition.id().version());

        List<OperatonProcessJsonSchemaDocumentDefinition> newProcessDocumentDefinitions = operatonProcessJsonSchemaDocumentAssociationService
            .findProcessDocumentDefinitions(newDocumentDefinition.id().name(), newDocumentDefinition.id().version());

        assertThat(oldProcessDocumentDefinitions.size()).isEqualTo(0);
        assertThat(newProcessDocumentDefinitions.size()).isEqualTo(1);
    }

    @Test
    public void shouldDeleteProcessDocumentDefinitionForNewVersionWhenNoVersionSpecified() {
        //create association for old version
        var request = new ProcessDocumentDefinitionRequest(
            "embedded-subprocess-example",
            oldDocumentDefinition.id().name(),
            true,
            true,
            Optional.of(oldDocumentDefinition.id().version())
        );

        runWithoutAuthorization(() -> operatonProcessJsonSchemaDocumentAssociationService
            .createProcessDocumentDefinition(request));

        //create association for new version
        var request2 = new ProcessDocumentDefinitionRequest(
            "embedded-subprocess-example",
            newDocumentDefinition.id().name(),
            true,
            true,
            Optional.of(newDocumentDefinition.id().version())
        );

        runWithoutAuthorization(() -> operatonProcessJsonSchemaDocumentAssociationService
            .createProcessDocumentDefinition(request2));

        //delete association for latest version
        var deleteRequest = new ProcessDocumentDefinitionRequest(
            "embedded-subprocess-example",
            oldDocumentDefinition.id().name(),
            true,
            true
        );

        runWithoutAuthorization(() -> {
            operatonProcessJsonSchemaDocumentAssociationService.deleteProcessDocumentDefinition(deleteRequest);
            return null;
        });

        // can't delete and select in the same transaction with JPA so we check the call instead
        verify(processDocumentDefinitionRepository).deleteById(
            OperatonProcessJsonSchemaDocumentDefinitionId.existingId(
                new OperatonProcessDefinitionKey("embedded-subprocess-example"),
                JsonSchemaDocumentDefinitionId.existingId(
                    "some-test",
                    2
                )
            )
        );
    }

    @Test
    public void shouldNotCreateProcessDocumentDefinitionForMultipleDossiers() {
        var request = new ProcessDocumentDefinitionRequest(
            "embedded-subprocess-example",
            DOCUMENT_DEFINITION_NAME,
            true,
            true
        );
        runWithoutAuthorization(() -> {
            operatonProcessJsonSchemaDocumentAssociationService.createProcessDocumentDefinition(request);
            return assertThrows(IllegalStateException.class, () -> operatonProcessJsonSchemaDocumentAssociationService.createProcessDocumentDefinition(request));
        });
    }

    @Test
    @WithMockUser(username = "john@ritense.com", authorities = ADMIN)
    public void shouldStartMainProcessAndAssociateCallActivityCalledProcess() throws JsonProcessingException {
        String processDocumentDefinitionKey = "call-activity-subprocess-example";

        final var processDocumentRequest = new ProcessDocumentDefinitionRequest(
            processDocumentDefinitionKey,
            DOCUMENT_DEFINITION_NAME,
            true,
            true
        );
        runWithoutAuthorization(() ->
            operatonProcessJsonSchemaDocumentAssociationService.createProcessDocumentDefinition(processDocumentRequest)
        );

        final JsonNode jsonContent = objectMapper.readTree("{\"street\": \"Funenparks\"}");
        var newDocumentRequest = new NewDocumentRequest(
            DOCUMENT_DEFINITION_NAME,
            jsonContent
        );
        var newDocumentRequest2 = new NewDocumentRequest(
            DOCUMENT_DEFINITION_NAME2,
            jsonContent
        );
        runWithoutAuthorization(() -> {
            var request = new NewDocumentAndStartProcessRequest(processDocumentDefinitionKey, newDocumentRequest);
            var request2 = new NewDocumentAndStartProcessRequest(processDocumentDefinitionKey, newDocumentRequest2);

            final NewDocumentAndStartProcessResult newDocumentAndStartProcessResult = operatonProcessJsonSchemaDocumentService
                .newDocumentAndStartProcess(request);

            operatonProcessJsonSchemaDocumentService.newDocumentAndStartProcess(request2);

            final List<TaskInstanceWithIdentityLink> processInstanceTasks = operatonTaskService.getProcessInstanceTasks(
                newDocumentAndStartProcessResult.resultingProcessInstanceId().orElseThrow().toString(),
                newDocumentAndStartProcessResult.resultingDocument().orElseThrow().id().toString()
            );

            final List<OperatonProcessJsonSchemaDocumentInstance> processDocumentInstancesBeforeComplete =
                runWithoutAuthorization(() -> operatonProcessJsonSchemaDocumentAssociationService
                    .findProcessDocumentInstances(newDocumentAndStartProcessResult.resultingDocument().orElseThrow().id()));

            assertThat(processDocumentInstancesBeforeComplete).hasSize(1);
            assertThat(processDocumentInstancesBeforeComplete.get(0).isActive()).isEqualTo(true);

            final Document document = newDocumentAndStartProcessResult.resultingDocument().orElseThrow();

            final JsonNode jsonDataUpdate = objectMapper.readTree("{\"street\": \"Funenparks\"}");
            var modifyRequest = new ModifyDocumentAndCompleteTaskRequest(
                new ModifyDocumentRequest(
                    document.id().toString(),
                    jsonDataUpdate
                ),
                processInstanceTasks.iterator().next().getTaskDto().getId()
            );

            final ModifyDocumentAndCompleteTaskResult modifyDocumentAndCompleteTaskResult = operatonProcessJsonSchemaDocumentService
                .modifyDocumentAndCompleteTask(modifyRequest);

            assertThat(modifyDocumentAndCompleteTaskResult.errors()).isEmpty();
            final List<OperatonProcessJsonSchemaDocumentInstance> processDocumentInstances =
                runWithoutAuthorization(() -> operatonProcessJsonSchemaDocumentAssociationService
                    .findProcessDocumentInstances(newDocumentAndStartProcessResult.resultingDocument().orElseThrow().id()));
            assertThat(processDocumentInstances).hasSize(2);
            assertThat(processDocumentInstances.get(0).isActive()).isEqualTo(false);
            assertThat(processDocumentInstances.get(1).isActive()).isEqualTo(false);
            return null;
        });
    }

    @Test
    @WithMockUser(username = "john@ritense.com", authorities = ADMIN)
    public void shouldStartMainProcessAndNotAssociateSubProcess() throws JsonProcessingException {
        String processDocumentDefinitionKey = "embedded-subprocess-example";

        runWithoutAuthorization(() -> {

            final var processDocumentRequest = new ProcessDocumentDefinitionRequest(
                processDocumentDefinitionKey,
                DOCUMENT_DEFINITION_NAME,
                true,
                true
            );
            operatonProcessJsonSchemaDocumentAssociationService.createProcessDocumentDefinition(processDocumentRequest);

            final JsonNode jsonContent = objectMapper.readTree("{\"street\": \"Funenparks\"}");
            var newDocumentRequest = new NewDocumentRequest(
                DOCUMENT_DEFINITION_NAME,
                jsonContent
            );
            var request = new NewDocumentAndStartProcessRequest(processDocumentDefinitionKey, newDocumentRequest);

            final NewDocumentAndStartProcessResult newDocumentAndStartProcessResult = operatonProcessJsonSchemaDocumentService
                .newDocumentAndStartProcess(request);


            final List<TaskInstanceWithIdentityLink> processInstanceTasks = operatonTaskService.getProcessInstanceTasks(
                newDocumentAndStartProcessResult.resultingProcessInstanceId().orElseThrow().toString(),
                newDocumentAndStartProcessResult.resultingDocument().orElseThrow().id().toString()
            );

            final Document document = newDocumentAndStartProcessResult.resultingDocument().orElseThrow();

            final JsonNode jsonDataUpdate = objectMapper.readTree("{\"street\": \"Funenparks\"}");
            var modifyRequest = new ModifyDocumentAndCompleteTaskRequest(
                new ModifyDocumentRequest(
                    document.id().toString(),
                    jsonDataUpdate
                ),
                processInstanceTasks.iterator().next().getTaskDto().getId()
            );

            final ModifyDocumentAndCompleteTaskResult modifyDocumentAndCompleteTaskResult = operatonProcessJsonSchemaDocumentService
                .modifyDocumentAndCompleteTask(modifyRequest);

            assertThat(modifyDocumentAndCompleteTaskResult.errors()).isEmpty();
            final List<OperatonProcessJsonSchemaDocumentInstance> processDocumentInstances =
                runWithoutAuthorization(() -> operatonProcessJsonSchemaDocumentAssociationService
                    .findProcessDocumentInstances(newDocumentAndStartProcessResult.resultingDocument().orElseThrow().id()));
            assertThat(processDocumentInstances).hasSize(1);
            return null;
        });
    }

}
