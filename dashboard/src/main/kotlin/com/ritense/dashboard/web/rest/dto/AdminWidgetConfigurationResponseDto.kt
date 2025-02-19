/*
 * Copyright 2015-2024 Ritense BV, the Netherlands.
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

package com.ritense.dashboard.web.rest.dto

import com.fasterxml.jackson.databind.node.ObjectNode
import com.ritense.dashboard.domain.WidgetConfiguration
import java.net.URI

data class AdminWidgetConfigurationResponseDto(
    val key: String,
    val title: String,
    val dataSourceKey: String,
    val displayType: String,
    val dataSourceProperties: ObjectNode,
    val displayTypeProperties: ObjectNode,
    val url: URI?,
) {
    companion object {
        fun of(widget: WidgetConfiguration) = AdminWidgetConfigurationResponseDto(
            key = widget.key,
            title = widget.title,
            dataSourceKey = widget.dataSourceKey,
            displayType = widget.displayType,
            dataSourceProperties = widget.dataSourceProperties,
            displayTypeProperties = widget.displayTypeProperties,
            url = widget.url
        )
    }
}
