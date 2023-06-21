/*
 * Copyright 2015-2023 Ritense BV, the Netherlands.
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

package com.ritense.valtimo.service;

import com.ritense.resource.service.ResourceService;
import com.ritense.valtimo.camunda.domain.CamundaIdentityLink;
import com.ritense.valtimo.camunda.domain.CamundaTask;
import com.ritense.valtimo.camunda.dto.CamundaIdentityLinkDto;
import com.ritense.valtimo.camunda.dto.CamundaTaskDto;
import com.ritense.valtimo.camunda.repository.CamundaIdentityLinkRepository;
import com.ritense.valtimo.camunda.repository.CamundaTaskRepository;
import com.ritense.valtimo.contract.authentication.ManageableUser;
import com.ritense.valtimo.contract.authentication.UserManagementService;
import com.ritense.valtimo.contract.authentication.model.ValtimoUserBuilder;
import com.ritense.valtimo.contract.event.TaskAssignedEvent;
import com.ritense.valtimo.contract.utils.RequestHelper;
import com.ritense.valtimo.contract.utils.SecurityUtils;
import com.ritense.valtimo.domain.contexts.Context;
import com.ritense.valtimo.domain.contexts.ContextProcess;
import com.ritense.valtimo.helper.DelegateTaskHelper;
import com.ritense.valtimo.repository.camunda.dto.TaskExtended;
import com.ritense.valtimo.repository.camunda.dto.TaskInstanceWithIdentityLink;
import com.ritense.valtimo.security.exceptions.TaskNotFoundException;
import com.ritense.valtimo.service.util.FormUtils;
import com.ritense.valtimo.web.rest.dto.TaskCompletionDTO;
import org.camunda.bpm.engine.FormService;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.form.TaskFormData;
import org.camunda.bpm.engine.impl.form.validator.FormFieldValidationException;
import org.camunda.bpm.engine.task.IdentityLinkType;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toSet;

public class CamundaTaskService {

    private static final String CONTEXT = "context";

    private static final String NO_USER = null;
    private final TaskService taskService;
    private final FormService formService;
    private final ContextService contextService;
    private final DelegateTaskHelper delegateTaskHelper;
    private final CamundaTaskRepository camundaTaskRepository;
    private final CamundaIdentityLinkRepository camundaIdentityLinkRepository;
    private final CamundaProcessService camundaProcessService;
    private final Optional<ResourceService> optionalResourceService;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final RuntimeService runtimeService;
    private final UserManagementService userManagementService;

    public CamundaTaskService(
        TaskService taskService,
        FormService formService,
        ContextService contextService,
        DelegateTaskHelper delegateTaskHelper,
        CamundaTaskRepository camundaTaskRepository,
        CamundaIdentityLinkRepository camundaIdentityLinkRepository,
        CamundaProcessService camundaProcessService,
        Optional<ResourceService> optionalResourceService,
        ApplicationEventPublisher applicationEventPublisher,
        RuntimeService runtimeService,
        UserManagementService userManagementService
    ) {
        this.taskService = taskService;
        this.formService = formService;
        this.contextService = contextService;
        this.delegateTaskHelper = delegateTaskHelper;
        this.camundaTaskRepository = camundaTaskRepository;
        this.camundaIdentityLinkRepository = camundaIdentityLinkRepository;
        this.camundaProcessService = camundaProcessService;
        this.optionalResourceService = optionalResourceService;
        this.applicationEventPublisher = applicationEventPublisher;
        this.runtimeService = runtimeService;
        this.userManagementService = userManagementService;
    }

    public CamundaTask findTaskById(String taskId) {
        CamundaTask task = camundaTaskRepository.findById(taskId)
            .orElseThrow(() -> new TaskNotFoundException(String.format("Cannot find task %s", taskId)));
        // TODO: task.initializeFormKey();
        return task;
    }

    public void assign(String taskId, String assignee) throws IllegalStateException {
        final CamundaTask task = findTaskById(taskId);
        final String currentAssignee = task.getAssignee();
        try {
            camundaTaskRepository.setAssignee(task.getId(), assignee);
            publishTaskAssignedEvent(task, currentAssignee, assignee);
        } catch (Exception ex) {
            throw new IllegalStateException("Cannot claim task: reason is the task doesn't exist.", ex);
        }
    }

    public void unassign(String taskId) {
        final CamundaTask task = findTaskById(taskId);
        try {
            camundaTaskRepository.setAssignee(task.getId(), NO_USER);
        } catch (Exception ex) {
            throw new IllegalStateException("Cannot claim task: reason is the task doesn't exist.", ex);
        }
    }

    public List<ManageableUser> getCandidateUsers(String taskId) {
        final CamundaTask task = findTaskById(taskId);
        final Optional<CamundaIdentityLink> first = camundaIdentityLinkRepository.findAllByTaskIdAndType(task.getId(), IdentityLinkType.CANDIDATE)
            .stream()
            .findFirst();

        if (first.isPresent()) {
            return userManagementService.findByRole(first.get().getGroupId());
        } else {
            return Collections.emptyList();
        }
    }

    public void completeTaskWithoutFormData(String taskId) {
        taskService.complete(taskId);
    }

    public void completeTask(String taskId, Map<String, Object> variables) {
        final CamundaTask task = findTaskById(taskId);
        try {
            if (variables == null || variables.isEmpty()) {
                completeTaskWithoutFormData(task.getId());
            } else {
                formService.submitTaskForm(task.getId(), FormUtils.createTypedVariableMap(variables));
            }
        } catch (FormFieldValidationException ex) {
            throw ex;
        } catch (ProcessEngineException ex) {
            throw new IllegalStateException("Cannot complete task: when no task exists with the given id.", ex);
        }
    }

    public void completeTaskAndDeleteFiles(String taskId, TaskCompletionDTO taskCompletionDTO) {
        completeTask(taskId, taskCompletionDTO.getVariables());
        optionalResourceService.ifPresent(
            amazonS3Service -> taskCompletionDTO.getFilesToDelete().forEach(amazonS3Service::removeResource));
    }

    public Page<TaskExtended> findTasksFiltered(
        TaskFilter taskFilter, Pageable pageable
    ) throws IllegalAccessException {
        var parameters = buildTaskFilterParameters(taskFilter);
        Page<TaskExtended> tasks = camundaTaskRepository.findTasks(pageable, parameters);
        if (!tasks.isEmpty()) {
            tasks.forEach(task -> task.setContext(taskService.getVariable(task.getId(), CONTEXT)));

            final var tasksGroupedByAssignee = tasks
                .stream()
                .collect(groupingBy(t -> t.getAssignee() == null ? "" : t.getAssignee()));

            tasksGroupedByAssignee.forEach((assigneeEmail, tasksExtended) -> {
                if (!assigneeEmail.isEmpty()) {
                    userManagementService.findByEmail(assigneeEmail).ifPresent(user -> {
                        final var valtimoUser = new ValtimoUserBuilder()
                            .id(user.getId())
                            .firstName(user.getFirstName())
                            .lastName(user.getLastName())
                            .build();
                        tasksExtended.forEach(taskExtended -> taskExtended.setValtimoAssignee(valtimoUser));
                    });
                }
            });
        }
        return tasks;
    }

    public List<TaskInstanceWithIdentityLink> getProcessInstanceTasks(String processInstanceId, String businessKey) {
        return camundaTaskRepository.findAllByProcessInstanceIdOrderByCreateTimeDesc(processInstanceId)
            .stream()
            .map(task -> {
                final var identityLinks = getIdentityLinks(task.getId());
                return new TaskInstanceWithIdentityLink(
                    businessKey,
                    CamundaTaskDto.Companion.fromEntity(task),
                    delegateTaskHelper.isTaskPublic(task),
                    getProcessDefinitionKey(task.getProcessDefinition().getId()),
                    identityLinks
                );
            })
            .collect(Collectors.toList());
    }

    public List<CamundaIdentityLinkDto> getIdentityLinks(String taskId) {
        final List<CamundaIdentityLink> identityLinksForTask = camundaIdentityLinkRepository.findAllByTaskId(taskId);
        return identityLinksForTask.stream().map(CamundaIdentityLinkDto.Companion::fromEntity).collect(Collectors.toList());
    }

    public Map<String, Object> getTaskVariables(String taskInstanceId) {
        return taskService.getVariables(taskInstanceId);
    }

    public enum TaskFilter {
        MINE, OPEN, ALL
    }

    private void publishTaskAssignedEvent(CamundaTask task, String formerAssignee, String newAssignee) {
        final String businessKey = runtimeService
            .createProcessInstanceQuery()
            .processInstanceId(task.getProcessInstanceId())
            .singleResult()
            .getBusinessKey();

        applicationEventPublisher.publishEvent(
            new TaskAssignedEvent(
                UUID.randomUUID(),
                RequestHelper.getOrigin(),
                LocalDateTime.now(),
                SecurityUtils.getCurrentUserLogin(),
                formerAssignee,
                newAssignee,
                task.getId(),
                task.getName(),
                LocalDateTime.ofInstant(task.getCreateTime().toInstant(), ZoneId.systemDefault()),
                task.getProcessDefinition().getId(),
                task.getProcessInstanceId(),
                businessKey
            )
        );
    }

    private Map<String, Object> buildTaskFilterParameters(TaskFilter taskFilter) throws IllegalAccessException {
        Map<String, Object> parameters = new HashMap<>();
        String currentUserLogin = SecurityUtils.getCurrentUserLogin();
        List<String> userRoles = SecurityUtils.getCurrentUserRoles();

        if (taskFilter == TaskFilter.MINE) {
            if (currentUserLogin == null) {
                throw new IllegalStateException("Cannot find currentUserLogin");
            }
            parameters.put("assignee", currentUserLogin);
            parameters.put("includeAssignedTasks", true);
        } else if (taskFilter == TaskFilter.ALL) {
            parameters.put("candidateGroups", userRoles);
            parameters.put("includeAssignedTasks", true);
        } else if (taskFilter == TaskFilter.OPEN) {
            parameters.put("candidateGroups", userRoles);
        }

        //Always filter on context
        Context context = contextService.getContextOfCurrentUser();
        parameters.put(
            "processDefinitionKeys",
            context.getProcesses().stream().map(ContextProcess::getProcessDefinitionKey).collect(toSet())
        );
        return parameters;
    }

    public boolean hasTaskFormData(String taskId) {
        final TaskFormData taskFormData = formService.getTaskFormData(taskId);
        return taskFormData == null || taskFormData.getFormKey() != null || !taskFormData.getFormFields().isEmpty();
    }

    /**
     * Gets the process definition key based on the process definition id. See url below that explains how the Id is created.
     * https://github.com/camunda/camunda-bpm-platform/blob/master/engine/src/main/java/org/camunda/bpm/engine/impl/AbstractDefinitionDeployer.java#L331-L351
     *
     * @param processDefinitionId The ID of the process definition
     * @return The key of the process definition
     */
    private String getProcessDefinitionKey(String processDefinitionId) {
        if (processDefinitionId.contains(":")) {
            return processDefinitionId.substring(0, processDefinitionId.indexOf(':'));
        } else {
            return camundaProcessService.findProcessDefinitionById(processDefinitionId).getKey();
        }
    }

}