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

package com.ritense.case.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.apache.commons.validator.routines.UrlValidator

@Entity
@Table(name = "case_definition")
class CaseDefinitionSettings(
    @Id
    @Column(name = "case_definition_name", nullable = false, length = 256)
    val name: String,
    @Column(name = "can_have_assignee", nullable = false)
    val canHaveAssignee: Boolean = false,
    @Column(name = "auto_assign_tasks", nullable = false)
    val autoAssignTasks: Boolean = false,
    @Column(name = "has_external_create_case_form", nullable = false)
    val hasExternalCreateCaseForm: Boolean = false,
    @Column(name = "external_create_case_form_url", nullable = true, length = 512)
    val externalCreateCaseFormUrl: String? = null,
) {
    init {
        require(
            when (autoAssignTasks) {
                true -> canHaveAssignee
                else -> true
            }
        ) { "Case property [autoAssignTasks] can only be true when [canHaveAssignee] is true." }
        require(
            when (hasExternalCreateCaseForm) {
                true -> !externalCreateCaseFormUrl.isNullOrBlank()
                else -> true
            }
        ) {
            "Case property [hasExternalCreateCaseForm] can only be true when [externalCreateCaseFormUrl] is not null or blank."
        }
        require(
            when (hasExternalCreateCaseForm) {
                true -> UrlValidator(arrayOf("http", "https")).isValid(externalCreateCaseFormUrl)
                else -> true
            }
        ) {
            "Case property [externalCreateCaseFormUrl] is not a valid URL."
        }
        require(
            when (hasExternalCreateCaseForm && !externalCreateCaseFormUrl.isNullOrBlank()) {
                true -> externalCreateCaseFormUrl.length <= 512
                else -> true
            }
        ) {
            "Case property [externalCreateCaseFormUrl] exceeds the maximum length of 512 characters."
        }
    }
}