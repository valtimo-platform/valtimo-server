package com.ritense.formviewmodel.viewmodel

import com.ritense.valtimo.operaton.domain.OperatonTask

class TestViewModelLoader : ViewModelLoader<TestViewModel> {
    override fun load(task: OperatonTask?): TestViewModel {
        return TestViewModel()
    }

    override fun supports(formName: String): Boolean {
        return formName == getFormName()
    }

    override fun getFormName(): String = "test"
}