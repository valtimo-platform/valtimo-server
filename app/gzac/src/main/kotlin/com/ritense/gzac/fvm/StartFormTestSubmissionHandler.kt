package com.ritense.gzac.fvm

import com.ritense.formviewmodel.error.FormException
import com.ritense.formviewmodel.submission.FormViewModelStartFormSubmissionHandler
import org.springframework.stereotype.Component

@Component
class StartFormTestSubmissionHandler : FormViewModelStartFormSubmissionHandler<TestViewModel> {
    override fun supports(formName: String): Boolean = formName == "fvm-test"

    override fun <T> handle(documentDefinitionName: String, processDefinitionKey: String, submission: T) {
        throw FormException("This is a Business error", "test")
    }
}