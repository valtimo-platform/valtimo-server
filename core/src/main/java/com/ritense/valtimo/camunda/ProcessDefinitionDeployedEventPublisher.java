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

package com.ritense.valtimo.camunda;

import static com.ritense.logging.LoggingContextKt.withLoggingContext;

import com.ritense.valtimo.camunda.domain.CamundaProcessDefinition;
import com.ritense.valtimo.event.ProcessDefinitionDeployedEvent;
import java.util.ArrayList;
import java.util.List;
import org.camunda.bpm.engine.impl.persistence.deploy.Deployer;
import org.camunda.bpm.engine.impl.persistence.entity.DeploymentEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;

public class ProcessDefinitionDeployedEventPublisher implements Deployer {

    private final ApplicationEventPublisher applicationEventPublisher;

    private List<ProcessDefinitionDeployedEvent> events = new ArrayList<>();
    private boolean isApplicationReady = false;

    public ProcessDefinitionDeployedEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReadyEvent() {
        isApplicationReady = true;
        events.forEach(event ->
            withLoggingContext(CamundaProcessDefinition.class, event.getProcessDefinitionId(), () ->
                applicationEventPublisher.publishEvent(event)
            )
        );
        events = null;
    }

    @Override
    public void deploy(DeploymentEntity deployment) {
        if (deployment.isNew() && deployment.getDeployedArtifacts() != null) {
            final var processDefinitions = deployment.getDeployedArtifacts(ProcessDefinitionEntity.class);
            // GET THE CASE DEFINITION ID from a new file

            if (processDefinitions != null) {
                processDefinitions.forEach(definition -> publishEvent(deployment, definition));
            }
        }
    }

    public void publishEvent(DeploymentEntity deployment, ProcessDefinitionEntity processDefinition) {
        //TODO get the caseDefinitionId
        final var event = new ProcessDefinitionDeployedEvent(deployment, processDefinition);
        if (isApplicationReady) {
            withLoggingContext(CamundaProcessDefinition.class, event.getProcessDefinitionId(), () ->
                applicationEventPublisher.publishEvent(event)
            );
        } else {
            events.add(event);
        }
    }

}
