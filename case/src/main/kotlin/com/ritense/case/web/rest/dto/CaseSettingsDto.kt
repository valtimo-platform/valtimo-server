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

package com.ritense.case.web.rest.dto

import com.ritense.case.domain.CaseDefinitionSettings

data class CaseSettingsDto(
    val canHaveAssignee: Boolean? = null,
    val autoAssignTasks: Boolean? = null,
    val hasExternalCreateCaseForm: Boolean? = null,
    val externalCreateCaseFormUrl: String? = null,
) {
    fun update(currentSettings: CaseDefinitionSettings): CaseDefinitionSettings {
        return CaseDefinitionSettings(
            name = currentSettings.name,
            canHaveAssignee = getSettingForUpdate(currentSettings.canHaveAssignee, this.canHaveAssignee) ?: false,
            autoAssignTasks = when (this.canHaveAssignee) {
                false -> false
                else -> getSettingForUpdate(currentSettings.autoAssignTasks, this.autoAssignTasks) ?: false
            },
            hasExternalCreateCaseForm = getSettingForUpdate(currentSettings.hasExternalCreateCaseForm, this.hasExternalCreateCaseForm) ?: false,
            externalCreateCaseFormUrl = when (this.hasExternalCreateCaseForm) {
                false -> null
                else -> getSettingForUpdate(currentSettings.externalCreateCaseFormUrl, this.externalCreateCaseFormUrl)
            }
        )
    }

    private fun <T> getSettingForUpdate(currentValue: T?, newValue: T?): T? {
        return newValue ?: currentValue
    }

    companion object {

        fun from(settings: CaseDefinitionSettings) = CaseSettingsDto(
            canHaveAssignee = settings.canHaveAssignee,
            autoAssignTasks = settings.autoAssignTasks,
            hasExternalCreateCaseForm = settings.hasExternalCreateCaseForm,
            externalCreateCaseFormUrl = settings.externalCreateCaseFormUrl
        )
    }
}