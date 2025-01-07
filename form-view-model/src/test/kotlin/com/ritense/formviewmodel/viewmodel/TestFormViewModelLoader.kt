package com.ritense.formviewmodel.viewmodel

import com.ritense.valtimo.camunda.domain.CamundaTask

class TestFormViewModelLoader() : FormViewModelLoader<TestViewModel>() {
    override fun load(task: CamundaTask?): TestViewModel {
        return TestViewModel()
    }

    override fun supports(formName: String): Boolean {
        return formName == getFormName()
    }

    override fun getFormName(): String = "test"
}