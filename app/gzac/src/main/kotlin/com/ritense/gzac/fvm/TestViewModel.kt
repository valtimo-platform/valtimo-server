package com.ritense.gzac.fvm

import com.ritense.formviewmodel.error.FormException
import com.ritense.formviewmodel.viewmodel.Submission
import com.ritense.formviewmodel.viewmodel.ViewModel
import com.ritense.valtimo.camunda.domain.CamundaTask

data class TestViewModel(
    val test: String
) : ViewModel, Submission {
    override fun update(task: CamundaTask?, page: Int?): ViewModel {
        if (test == "error") {
            throw FormException(
                component = "test",
                message = "Test error"
            )
        }
        return this
    }
}
