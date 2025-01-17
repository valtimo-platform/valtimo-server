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
import static com.ritense.valtimo.contract.utils.AssertionConcern.assertStateTrue;
import static com.ritense.valtimo.operaton.repository.OperatonProcessDefinitionSpecificationHelper.byKey;

import com.ritense.authorization.Action;
import com.ritense.authorization.AuthorizationContext;
import com.ritense.authorization.AuthorizationService;
import com.ritense.authorization.request.AuthorizationResourceContext;
import com.ritense.authorization.request.EntityAuthorizationRequest;
import com.ritense.authorization.request.RelatedEntityAuthorizationRequest;
import com.ritense.document.domain.Document;
import com.ritense.document.domain.impl.JsonSchemaDocument;
import com.ritense.document.domain.impl.JsonSchemaDocumentDefinition;
import com.ritense.document.domain.impl.JsonSchemaDocumentDefinitionId;
import com.ritense.document.domain.impl.JsonSchemaDocumentId;
import com.ritense.document.exception.DocumentNotFoundException;
import com.ritense.document.exception.UnknownDocumentDefinitionException;
import com.ritense.document.repository.DocumentDefinitionRepository;
import com.ritense.document.service.DocumentDefinitionService;
import com.ritense.document.service.DocumentService;
import com.ritense.document.service.JsonSchemaDocumentActionProvider;
import com.ritense.processdocument.domain.ProcessDefinitionKey;
import com.ritense.processdocument.domain.ProcessDocumentDefinition;
import com.ritense.processdocument.domain.ProcessDocumentInstanceId;
import com.ritense.processdocument.domain.ProcessInstanceId;
import com.ritense.processdocument.domain.impl.OperatonProcessDefinitionKey;
import com.ritense.processdocument.domain.impl.OperatonProcessInstanceId;
import com.ritense.processdocument.domain.impl.OperatonProcessJsonSchemaDocumentDefinition;
import com.ritense.processdocument.domain.impl.OperatonProcessJsonSchemaDocumentDefinitionId;
import com.ritense.processdocument.domain.impl.OperatonProcessJsonSchemaDocumentInstance;
import com.ritense.processdocument.domain.impl.OperatonProcessJsonSchemaDocumentInstanceId;
import com.ritense.processdocument.domain.impl.ProcessDocumentInstanceDto;
import com.ritense.processdocument.domain.impl.request.ProcessDocumentDefinitionRequest;
import com.ritense.processdocument.exception.DuplicateProcessDocumentDefinitionException;
import com.ritense.processdocument.exception.ProcessDocumentDefinitionNotFoundException;
import com.ritense.processdocument.exception.UnknownProcessDefinitionException;
import com.ritense.processdocument.repository.ProcessDocumentDefinitionRepository;
import com.ritense.processdocument.repository.ProcessDocumentInstanceRepository;
import com.ritense.processdocument.service.ProcessDocumentAssociationService;
import com.ritense.valtimo.contract.authentication.ManageableUser;
import com.ritense.valtimo.contract.authentication.UserManagementService;
import com.ritense.valtimo.contract.result.FunctionResult;
import com.ritense.valtimo.contract.result.OperationError;
import com.ritense.valtimo.operaton.authorization.OperatonExecutionActionProvider;
import com.ritense.valtimo.operaton.domain.OperatonExecution;
import com.ritense.valtimo.operaton.domain.OperatonProcessDefinition;
import com.ritense.valtimo.operaton.service.OperatonRepositoryService;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.operaton.bpm.engine.HistoryService;
import org.operaton.bpm.engine.RuntimeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.transaction.annotation.Transactional;

public class OperatonProcessJsonSchemaDocumentAssociationService implements ProcessDocumentAssociationService {

    private static final Logger logger = LoggerFactory.getLogger(OperatonProcessJsonSchemaDocumentAssociationService.class);
    private final ProcessDocumentDefinitionRepository processDocumentDefinitionRepository;
    private final ProcessDocumentInstanceRepository processDocumentInstanceRepository;
    private final DocumentDefinitionRepository<JsonSchemaDocumentDefinition> documentDefinitionRepository;
    private final DocumentDefinitionService documentDefinitionService;
    private final OperatonRepositoryService repositoryService;
    private final RuntimeService runtimeService;
    private final HistoryService historyService;
    private final AuthorizationService authorizationService;
    private final DocumentService documentService;
    private final UserManagementService userManagementService;

    public OperatonProcessJsonSchemaDocumentAssociationService(
        ProcessDocumentDefinitionRepository processDocumentDefinitionRepository,
        ProcessDocumentInstanceRepository processDocumentInstanceRepository,
        DocumentDefinitionRepository<JsonSchemaDocumentDefinition> documentDefinitionRepository,
        DocumentDefinitionService documentDefinitionService,
        OperatonRepositoryService repositoryService,
        RuntimeService runtimeService,
        HistoryService historyService,
        AuthorizationService authorizationService,
        DocumentService documentService,
        UserManagementService userManagementService
    ) {
        this.processDocumentDefinitionRepository = processDocumentDefinitionRepository;
        this.processDocumentInstanceRepository = processDocumentInstanceRepository;
        this.documentDefinitionRepository = documentDefinitionRepository;
        this.documentDefinitionService = documentDefinitionService;
        this.repositoryService = repositoryService;
        this.historyService = historyService;
        this.runtimeService = runtimeService;
        this.authorizationService = authorizationService;
        this.documentService = documentService;
        this.userManagementService = userManagementService;
    }

    @Override
    public Optional<OperatonProcessJsonSchemaDocumentDefinition> findProcessDocumentDefinition(ProcessDefinitionKey processDefinitionKey) {
        denyAuthorization(OperatonProcessJsonSchemaDocumentDefinition.class);

        return processDocumentDefinitionRepository.findByProcessDefinitionKeyAndLatestDocumentDefinitionVersion(processDefinitionKey);
    }

    @Override
    public OperatonProcessJsonSchemaDocumentDefinition getProcessDocumentDefinition(ProcessDefinitionKey processDefinitionKey) {
        denyAuthorization(OperatonProcessJsonSchemaDocumentDefinition.class);

        return findProcessDocumentDefinition(processDefinitionKey)
            .orElseThrow(() -> new ProcessDocumentDefinitionNotFoundException("for processDefinitionKey '" + processDefinitionKey + "'"));
    }

    @Override
    public List<OperatonProcessJsonSchemaDocumentDefinition> findAllProcessDocumentDefinitions(ProcessDefinitionKey processDefinitionKey) {
        denyAuthorization(OperatonProcessJsonSchemaDocumentDefinition.class);

        return processDocumentDefinitionRepository.findAllByProcessDefinitionKeyAndLatestDocumentDefinitionVersion(processDefinitionKey);
    }

    @Override
    public Optional<OperatonProcessJsonSchemaDocumentDefinition> findProcessDocumentDefinition(
        ProcessDefinitionKey processDefinitionKey,
        long documentDefinitionVersion
    ) {
        denyAuthorization(OperatonProcessJsonSchemaDocumentDefinition.class);

        return processDocumentDefinitionRepository.findByProcessDefinitionKeyAndDocumentDefinitionVersion(
            processDefinitionKey,
            documentDefinitionVersion
        );
    }

    @Override
    public OperatonProcessJsonSchemaDocumentDefinition getProcessDocumentDefinition(
        ProcessDefinitionKey processDefinitionKey,
        long documentDefinitionVersion
    ) {
        denyAuthorization(OperatonProcessJsonSchemaDocumentDefinition.class);

        return findProcessDocumentDefinition(processDefinitionKey, documentDefinitionVersion)
            .orElseThrow(() -> new ProcessDocumentDefinitionNotFoundException(
                    "for processDefinitionKey '" + processDefinitionKey + "' and version '" + documentDefinitionVersion + "'"
                )
            );
    }

    @Override
    public List<OperatonProcessJsonSchemaDocumentDefinition> findProcessDocumentDefinitions(String documentDefinitionName) {
        return findProcessDocumentDefinitions(documentDefinitionName, null, null);
    }

    @Override
    public List<OperatonProcessJsonSchemaDocumentDefinition> findProcessDocumentDefinitions(
        String documentDefinitionName,
        @Nullable Boolean startableByUser
    ) {
        return findProcessDocumentDefinitions(documentDefinitionName, startableByUser, null);
    }

    @Override
    public List<OperatonProcessJsonSchemaDocumentDefinition> findProcessDocumentDefinitions(
        String documentDefinitionName,
        @Nullable Boolean startableByUser,
        @Nullable Boolean canInitializeDocument
    ) {
        List<OperatonProcessJsonSchemaDocumentDefinition> results = processDocumentDefinitionRepository
            .findAll(documentDefinitionName, startableByUser, canInitializeDocument);

        return results.stream().filter(result -> {
            OperatonProcessDefinition processDefinition = AuthorizationContext.runWithoutAuthorization(() ->
                repositoryService.findLatestProcessDefinition(
                    result.processDocumentDefinitionId().processDefinitionKey().toString()
                )
            );

            return authorizationService.hasPermission(
                new RelatedEntityAuthorizationRequest<>(
                    OperatonExecution.class,
                    OperatonExecutionActionProvider.CREATE,
                    OperatonProcessDefinition.class,
                    processDefinition.getId()
                )
            );
        }).toList();
    }

    @Override
    public List<OperatonProcessJsonSchemaDocumentDefinition> findProcessDocumentDefinitions(
        UUID documentId,
        @Nullable Boolean startableByUser,
        @Nullable Boolean canInitializeDocument
    ) {
        Document document = documentService.findBy(JsonSchemaDocumentId.existingId(documentId)).
            orElseThrow(() -> new DocumentNotFoundException("Document not found with id " + documentId));

        List<OperatonProcessJsonSchemaDocumentDefinition> results = processDocumentDefinitionRepository
            .findAll(document.definitionId().name(), startableByUser, canInitializeDocument);

        return results.stream().filter(result -> {
            OperatonProcessDefinition processDefinition = AuthorizationContext.runWithoutAuthorization(() ->
                repositoryService.findLatestProcessDefinition(
                    result.processDocumentDefinitionId().processDefinitionKey().toString()
                )
            );

            return authorizationService.hasPermission(
                new RelatedEntityAuthorizationRequest<>(
                    OperatonExecution.class,
                    OperatonExecutionActionProvider.CREATE,
                    OperatonProcessDefinition.class,
                    processDefinition.getId()
                ).withContext(
                    new AuthorizationResourceContext(
                        JsonSchemaDocument.class,
                        document
                    )
                )
            );
        }).toList();
    }

    @Override
    public List<OperatonProcessJsonSchemaDocumentDefinition> findProcessDocumentDefinitions(
        String documentDefinitionName,
        Long documentDefinitionVersion
    ) {
        return processDocumentDefinitionRepository.findAllByDocumentDefinitionNameAndVersion(documentDefinitionName, documentDefinitionVersion);
    }

    @Override
    public List<OperatonProcessJsonSchemaDocumentDefinition> findProcessDocumentDefinitionsByProcessDefinitionKey(String processDefinitionKey) {
        denyAuthorization(OperatonProcessJsonSchemaDocumentDefinition.class);

        return processDocumentDefinitionRepository.findAllByProcessDefinitionKeyAndLatestDocumentDefinitionVersion(processDefinitionKey);
    }

    @Override
    public Optional<? extends ProcessDocumentDefinition> findByDocumentDefinitionName(String documentDefinitionName) {
        denyAuthorization(OperatonProcessJsonSchemaDocumentDefinition.class);

        return processDocumentDefinitionRepository.findByDocumentDefinitionName(documentDefinitionName);
    }

    @Override
    public Optional<OperatonProcessJsonSchemaDocumentInstance> findProcessDocumentInstance(ProcessInstanceId processInstanceId) {
        denyAuthorization(OperatonProcessJsonSchemaDocumentInstance.class);
        return processDocumentInstanceRepository.findByProcessInstanceId(processInstanceId);
    }

    @Override
    public List<OperatonProcessJsonSchemaDocumentInstance> findProcessDocumentInstances(Document.Id documentId) {
        var document = documentService.findBy(documentId).orElseThrow();

        authorizationService.requirePermission(
            new EntityAuthorizationRequest<>(
                JsonSchemaDocument.class,
                JsonSchemaDocumentActionProvider.VIEW,
                (JsonSchemaDocument) document
            )
        );

        var processes = processDocumentInstanceRepository.findAllByProcessDocumentInstanceIdDocumentId(documentId);
        for (var process : processes) {
            OperatonProcessJsonSchemaDocumentInstanceId id = process.getId();
            if (id != null) {
                var operatonProcess = runtimeService.createProcessInstanceQuery()
                    .processInstanceId(id.processInstanceId().toString())
                    .singleResult();
                process.setActive(operatonProcess != null && !operatonProcess.isEnded());
            }
        }
        return processes;
    }

    @Override
    public List<ProcessDocumentInstanceDto> findProcessDocumentInstanceDtos(Document.Id documentId) {
        var document = documentService.findBy(documentId).orElseThrow();

        authorizationService.requirePermission(
            new EntityAuthorizationRequest<>(
                JsonSchemaDocument.class,
                JsonSchemaDocumentActionProvider.VIEW,
                (JsonSchemaDocument) document
            )
        );

        return processDocumentInstanceRepository.findAllByProcessDocumentInstanceIdDocumentId(documentId).stream()
            .map(process -> {
                if (process.getId() != null) {
                    var operatonProcess = historyService.createHistoricProcessInstanceQuery()
                        .processInstanceId(process.getId().processInstanceId().toString())
                        .singleResult();
                    process.setActive(operatonProcess != null && operatonProcess.getEndTime() == null);
                    var operatonProcessDefinition = runWithoutAuthorization(() ->
                        repositoryService.findLatestProcessDefinition(operatonProcess.getProcessDefinitionKey())
                    );
                    var startDateTime = LocalDateTime.ofInstant(
                        operatonProcess.getStartTime().toInstant(),
                        ZoneId.systemDefault()
                    );
                    var startedBy = operatonProcess.getStartUserId() == null ? null :
                        userManagementService.findByEmail(operatonProcess.getStartUserId()).map(ManageableUser::getFullName).orElse(null);

                    return new ProcessDocumentInstanceDto(
                        process.getId(),
                        process.processName(),
                        process.isActive(),
                        operatonProcess.getProcessDefinitionVersion(),
                        operatonProcessDefinition.getVersion(),
                        startedBy,
                        startDateTime
                    );
                }

                return new ProcessDocumentInstanceDto(
                    process.getId(),
                    process.processName(),
                    process.isActive()
                );
            })
            .toList();
    }

    @Override
    @Transactional
    public void deleteProcessDocumentInstances(String processName) {
        denyAuthorization(OperatonProcessJsonSchemaDocumentInstance.class);

        logger.debug("Remove all running process document instances for process: {}", processName);
        processDocumentInstanceRepository.deleteAllByProcessName(processName);
    }

    @Override
    @Transactional
    public void deleteProcessDocumentInstance(ProcessDocumentInstanceId processDocumentInstanceId) {
        denyAuthorization(OperatonProcessJsonSchemaDocumentInstance.class);

        logger.debug("Deleting process document instance: {}", processDocumentInstanceId);
        processDocumentInstanceRepository.deleteById(processDocumentInstanceId);
    }

    @Override
    @Transactional
    public Optional<OperatonProcessJsonSchemaDocumentDefinition> createProcessDocumentDefinition(
        ProcessDocumentDefinitionRequest request
    ) {
        denyAuthorization(OperatonProcessJsonSchemaDocumentDefinition.class);

        JsonSchemaDocumentDefinitionId documentDefinitionId;
        if (request.getDocumentDefinitionVersion().isPresent()) {
            documentDefinitionId = JsonSchemaDocumentDefinitionId.existingId(
                request.documentDefinitionName(),
                request.getDocumentDefinitionVersion().orElseThrow()
            );
        } else {
            documentDefinitionId = documentDefinitionService.findIdByName(request.documentDefinitionName());
        }

        return createProcessDocumentDefinition(
            new OperatonProcessDefinitionKey(request.processDefinitionKey()),
            documentDefinitionId,
            request.canInitializeDocument(),
            request.startableByUser()
        );
    }

    private Optional<OperatonProcessJsonSchemaDocumentDefinition> createProcessDocumentDefinition(
        OperatonProcessDefinitionKey processDefinitionKey,
        JsonSchemaDocumentDefinitionId documentDefinitionId,
        boolean canInitializeDocument,
        boolean startableByUser
    ) {
        if (!AuthorizationContext.runWithoutAuthorization(
            () -> repositoryService.processDefinitionExists(byKey(processDefinitionKey.toString())))
        ) {
            throw new UnknownProcessDefinitionException(processDefinitionKey.toString());
        }
        if (!documentDefinitionRepository.existsById(documentDefinitionId)) {
            throw new UnknownDocumentDefinitionException(documentDefinitionId.toString());
        }

        var knownProcessDocumentDefinitions = processDocumentDefinitionRepository
            .findAllByProcessDefinitionKeyAndLatestDocumentDefinitionVersion(processDefinitionKey);

        assertStateTrue(
            knownProcessDocumentDefinitions.isEmpty(),
            "Process is already in use within the context of another dossier."
        );

        final var id = OperatonProcessJsonSchemaDocumentDefinitionId.newId(
            processDefinitionKey,
            documentDefinitionId
        );
        if (processDocumentDefinitionRepository.existsById(id)) {
            throw new DuplicateProcessDocumentDefinitionException(
                processDefinitionKey.toString(),
                documentDefinitionId.toString()
            );
        }

        final var association = processDocumentDefinitionRepository.saveAndFlush(
            new OperatonProcessJsonSchemaDocumentDefinition(id, canInitializeDocument, startableByUser)
        );
        logger.info(
            "Created ProcessDocumentDefinition - associated process-definition - {} - with document-definition - {} ",
            processDefinitionKey,
            documentDefinitionId
        );
        return Optional.of(association);
    }

    @Transactional
    @Override
    public void deleteProcessDocumentDefinition(ProcessDocumentDefinitionRequest request) {
        denyAuthorization(OperatonProcessJsonSchemaDocumentDefinition.class);

        if (logger.isDebugEnabled()) {
            logger.debug(
                "Remove process document definition for document definition: {}",
                request.documentDefinitionName()
            );
        }

        JsonSchemaDocumentDefinitionId documentDefinitionId;
        if (request.getDocumentDefinitionVersion().isPresent()) {
            documentDefinitionId = JsonSchemaDocumentDefinitionId.existingId(
                request.documentDefinitionName(),
                request.getDocumentDefinitionVersion().orElseThrow()
            );
        } else {
            documentDefinitionId = documentDefinitionService.findIdByName(request.documentDefinitionName());
        }

        final var id = OperatonProcessJsonSchemaDocumentDefinitionId.existingId(
            new OperatonProcessDefinitionKey(request.processDefinitionKey()),
            documentDefinitionId
        );
        processDocumentDefinitionRepository.deleteById(id);
    }

    @Transactional
    @Override
    public void deleteProcessDocumentDefinition(String documentDefinitionName) {
        denyAuthorization(OperatonProcessJsonSchemaDocumentDefinition.class);

        processDocumentDefinitionRepository.deleteByDocumentDefinition(documentDefinitionName);
    }

    @Transactional
    @Override
    public Optional<OperatonProcessJsonSchemaDocumentInstance> createProcessDocumentInstance(
        String processInstanceId,
        UUID documentId,
        String processName
    ) {
        denyAuthorization(OperatonProcessJsonSchemaDocumentInstance.class);

        final var id = OperatonProcessJsonSchemaDocumentInstanceId.newId(
            new OperatonProcessInstanceId(processInstanceId),
            JsonSchemaDocumentId.existingId(documentId)
        );
        final var association = processDocumentInstanceRepository.saveAndFlush(
            new OperatonProcessJsonSchemaDocumentInstance(id, processName)
        );
        logger.info(
            "Created PDI - associated - processInstanceId {} with documentId - {} for process - {}",
            processInstanceId,
            processName,
            documentId
        );
        return Optional.of(association);
    }

    @Override
    public FunctionResult<OperatonProcessJsonSchemaDocumentInstance, OperationError> getProcessDocumentInstanceResult(
        ProcessDocumentInstanceId processDocumentInstanceId
    ) {
        denyAuthorization(OperatonProcessJsonSchemaDocumentInstance.class);

        final var result = processDocumentInstanceRepository.findById(processDocumentInstanceId);
        if (result.isPresent()) {
            return new FunctionResult.Successful<>(result.get());
        } else {
            final String msg = "Corresponding process-document-instance is not associated with process-document-instance-id";
            return new FunctionResult.Erroneous<>(new OperationError.FromString(msg));
        }
    }

    private <T> void denyAuthorization(Class<T> clazz) {
        authorizationService.requirePermission(
            new EntityAuthorizationRequest(
                clazz,
                Action.deny()
            )
        );
    }
}
