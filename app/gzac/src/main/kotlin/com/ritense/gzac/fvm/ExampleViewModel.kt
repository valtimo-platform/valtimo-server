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

import com.ritense.document.domain.impl.JsonSchemaDocument
import com.ritense.formviewmodel.error.FormErrorsException
import com.ritense.formviewmodel.error.FormErrorsException.ComponentError
import com.ritense.formviewmodel.viewmodel.Submission
import com.ritense.formviewmodel.viewmodel.ViewModel
import com.ritense.valtimo.camunda.domain.CamundaTask
import mu.KLogger
import mu.KotlinLogging
import java.time.OffsetDateTime

class ExampleViewModel(
    val enableUpdateValidation: Boolean? = false,
    val enableSubmissionValidation: Boolean? = false,
    val enableBusinessValidation: Boolean? = false,
    val container: ExampleContainer = ExampleContainer(),
) : ViewModel, Submission {

    override fun update(task: CamundaTask?, page: Int?, document: JsonSchemaDocument?): ViewModel {
        logger.info { "Update called on ${this::class.java.simpleName}, taskId=${task?.id}, page=$page" }

        if (enableUpdateValidation == true) {
            validate()
        }

        return this
    }

    fun validate() {
        val errors = mutableListOf<ComponentError>()
        errors.addAll(
            container.validate().map { it.withParent("container") }
        )
        if (enableBusinessValidation == true) {
           errors.add(ComponentError(component = null, message = "Business Error"))
        }

        if (errors.isNotEmpty()) {
            throw FormErrorsException(errors)
        }
    }

    companion object {
        private val logger: KLogger = KotlinLogging.logger {}
    }
}

class ExampleContainer(
    val positiveNumber: Int? = null,
    val futureDateTime: OffsetDateTime? = null,
    val maxLengthString: String? = null
) {
    fun validate(): List<ComponentError> {
        val errors = mutableListOf<ComponentError>()

        if (positiveNumber != null && positiveNumber < 0) {
            errors.add(ComponentError("positiveNumber", "number must be greater than zero"))
        }

        if (futureDateTime != null && futureDateTime.isBefore(OffsetDateTime.now())) {
            errors.add(ComponentError("futureDateTime", "datetime must be in the future"))
        }

        if (maxLengthString != null && maxLengthString.length > 20) {
            errors.add(ComponentError("maxLengthString", "lenght cannot be greater than 20"))
        }

        return errors
    }
}