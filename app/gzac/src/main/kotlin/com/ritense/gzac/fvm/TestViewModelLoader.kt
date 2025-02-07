package com.ritense.gzac.fvm

import com.ritense.document.domain.impl.JsonSchemaDocument
import com.ritense.formviewmodel.viewmodel.FormViewModelLoader
import com.ritense.formviewmodel.viewmodel.ViewModelLoader
import com.ritense.processlink.domain.ProcessLink
import com.ritense.valtimo.camunda.domain.CamundaTask
import org.springframework.stereotype.Component

@Component
class TestViewModelLoader : FormViewModelLoader<TestViewModel>() {
    override fun load(task: CamundaTask?, document: JsonSchemaDocument?): TestViewModel = TestViewModel("test")

    override fun getFormName(): String = "fvm-test"
}