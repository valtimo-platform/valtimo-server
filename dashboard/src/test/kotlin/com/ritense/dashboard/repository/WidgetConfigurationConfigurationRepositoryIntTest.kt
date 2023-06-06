/*
 * Copyright 2015-2023 Ritense BV, the Netherlands.
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

package com.ritense.dashboard.repository

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.ritense.dashboard.BaseIntegrationTest
import com.ritense.dashboard.domain.Dashboard
import com.ritense.dashboard.domain.WidgetConfiguration
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class WidgetConfigurationConfigurationRepositoryIntTest : BaseIntegrationTest() {

    @Autowired
    lateinit var dashboardRepository: DashboardRepository

    @Autowired
    lateinit var widgetConfigurationRepository: WidgetConfigurationRepository

    @Test
    fun `should save and get widget configuration with dashboard`() {
        val dashboard = dashboardRepository.save(Dashboard(key = "mine", title = "My dashboard", order = 1))
        widgetConfigurationRepository.save(
            WidgetConfiguration(
                dashboard = dashboard,
                dataSourceKey = "doorlooptijd",
                dataSourceProperties = jacksonObjectMapper().createObjectNode(),
                displayType = "gauge",
                order = 2
            )
        )

        val widgets = widgetConfigurationRepository.findAll()

        assertThat(widgets).hasSize(1)
        assertThat(widgets[0].dashboard).isEqualTo(dashboard)
        assertThat(widgets[0].dashboard.key).isEqualTo("mine")
        assertThat(widgets[0].dashboard.title).isEqualTo("My dashboard")
        assertThat(widgets[0].dashboard.order).isEqualTo(1)
        assertThat(widgets[0].dataSourceKey).isEqualTo("doorlooptijd")
        assertThat(widgets[0].dataSourceProperties).isNotNull
        assertThat(widgets[0].displayType).isEqualTo("gauge")
        assertThat(widgets[0].order).isEqualTo(2)
    }
}