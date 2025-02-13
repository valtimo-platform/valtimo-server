/*
 * Copyright 2015-2025 Ritense BV, the Netherlands.
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

package com.ritense.gzac.fvm

import com.ritense.formviewmodel.error.FormException
import com.ritense.formviewmodel.submission.FormViewModelStartFormSubmissionHandler
import com.ritense.processlink.domain.ProcessLink
import mu.KLogger
import mu.KotlinLogging
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

@Component
@Order(Ordered.LOWEST_PRECEDENCE)
class NoopFormViewModelStartFormSubmissionHandler: FormViewModelStartFormSubmissionHandler<NoopViewModel> {

    override fun supports(processLink: ProcessLink) = true

    override fun <T> handle(documentDefinitionName: String, processDefinitionKey: String, submission: T) {
        logger.debug { "Start form submission handle: documentDefinitionName=$documentDefinitionName, processDefinitionKey=$processDefinitionKey" }
        (submission as? NoopViewModel)?.let {
            if (!it.submitError.isNullOrBlank()) {
                throw FormException(it.submitError, "submitError")
            }
        }
    }

    companion object {
        private val logger: KLogger = KotlinLogging.logger {}
    }
}