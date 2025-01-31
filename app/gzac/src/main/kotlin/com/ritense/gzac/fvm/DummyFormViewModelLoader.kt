package com.ritense.gzac.fvm

import com.ritense.formviewmodel.viewmodel.ViewModelLoader
import com.ritense.processlink.domain.ProcessLink
import com.ritense.valtimo.camunda.domain.CamundaTask
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

@Component
@Order(Ordered.LOWEST_PRECEDENCE)
class DummyFormViewModelLoader(
) : ViewModelLoader<DummyViewModel> {

    override fun supports(processLink: ProcessLink) = true

    override fun load(task: CamundaTask?): DummyViewModel {
        return DummyViewModel()
    }
}