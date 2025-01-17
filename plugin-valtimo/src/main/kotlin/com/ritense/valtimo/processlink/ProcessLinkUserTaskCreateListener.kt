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

package com.ritense.valtimo.processlink

import com.ritense.logging.withLoggingContext
import com.ritense.plugin.repository.PluginProcessLinkRepository
import com.ritense.plugin.service.PluginService
import com.ritense.processlink.domain.ActivityTypeWithEventName
import com.ritense.valtimo.contract.annotation.AllOpen
import org.operaton.bpm.engine.delegate.DelegateTask
import org.springframework.context.event.EventListener
import org.springframework.transaction.annotation.Transactional

@AllOpen
open class ProcessLinkUserTaskCreateListener(
    private val pluginProcessLinkRepository: PluginProcessLinkRepository,
    private val pluginService: PluginService,
) {

    @Transactional
    @EventListener(condition="#taskDelegate.eventName=='create'")
    fun notify(taskDelegate: DelegateTask) {
        if (taskDelegate.bpmnModelElementInstance == null) {
            return
        }
        withLoggingContext("com.ritense.document.domain.impl.JsonSchemaDocument", taskDelegate.execution.processBusinessKey) {
            val pluginProcessLinks = pluginProcessLinkRepository.findByProcessDefinitionIdAndActivityIdAndActivityType(
                taskDelegate.processDefinitionId,
                taskDelegate.execution.currentActivityId,
                ActivityTypeWithEventName.USER_TASK_CREATE
            )
            pluginProcessLinks.forEach { pluginProcessLink ->
                pluginService.invoke(taskDelegate, pluginProcessLink)
            }
        }
    }
}
