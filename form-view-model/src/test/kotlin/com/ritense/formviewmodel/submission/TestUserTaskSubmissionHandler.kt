package com.ritense.formviewmodel.submission

import com.ritense.formviewmodel.error.FormException
import com.ritense.formviewmodel.viewmodel.TestViewModel
import com.ritense.valtimo.operaton.domain.OperatonTask

class TestUserTaskSubmissionHandler : FormViewModelUserTaskSubmissionHandler<TestViewModel> {

    override fun supports(formName: String): Boolean {
        return formName == "test"
    }

    override fun <T> handle(submission: T, task: OperatonTask, businessKey: String) {
        submission as TestViewModel
        if (submission.age!! < 18) {
            throw FormException("Age should be 18 or older")
        }
    }

}