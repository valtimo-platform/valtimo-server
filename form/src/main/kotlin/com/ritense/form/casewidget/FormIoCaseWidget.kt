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

package com.ritense.form.casewidget

import com.ritense.case_.domain.tab.CaseWidgetTabWidget
import com.ritense.case_.domain.tab.CaseWidgetTabWidgetId
import com.ritense.case_.rest.dto.CaseWidgetAction
import com.ritense.valtimo.contract.annotation.AllOpen
import io.hypersistence.utils.hibernate.type.json.JsonType
import jakarta.persistence.Column
import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity
import org.hibernate.annotations.Type

@AllOpen
@Entity
@DiscriminatorValue("formio")
class FormIoCaseWidget(
    id: CaseWidgetTabWidgetId,
    title: String,
    order: Int,
    width: Int,
    highContrast: Boolean,
    actions: List<CaseWidgetAction>,

    @Type(value = JsonType::class)
    @Column(name = "properties", nullable = false)
    val properties: FormIoWidgetProperties
) : CaseWidgetTabWidget(
    id, title, order, width, highContrast, actions
)