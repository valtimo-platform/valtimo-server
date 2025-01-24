package com.ritense.case.web.dto

import com.ritense.case.domain.CaseDefinitionSettings
import com.ritense.case.web.rest.dto.CaseSettingsDto
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertFalse

class CaseSettingsDtoTest {

    @Test
    fun `should update case settings when value is not null`() {
        val currentCaseSettings = CaseDefinitionSettings(
            name = "name"
        )
        assertThat(currentCaseSettings.canHaveAssignee).isFalse()

        val caseSettingsDto = CaseSettingsDto(
            canHaveAssignee = true
        )
        assertThat(caseSettingsDto.canHaveAssignee!!).isTrue()

        val updatedCaseSettings = caseSettingsDto.update(currentCaseSettings)
        assertThat(updatedCaseSettings.canHaveAssignee).isTrue()
    }

    @Test
    fun `should update case setting 'hasExternalCreateCaseForm' when url value is not null`() {
        val externalFormUrl = "https://example.com/create-case-form"
        val currentCaseSettings = CaseDefinitionSettings(
            name = "name"
        )

        assertThat(currentCaseSettings.hasExternalCreateCaseForm).isFalse()
        assertThat(currentCaseSettings.externalCreateCaseFormUrl).isNull()

        val caseSettingsDto = CaseSettingsDto(
            hasExternalCreateCaseForm = true,
            externalCreateCaseFormUrl = externalFormUrl
        )
        val updatedCaseSettings = caseSettingsDto.update(currentCaseSettings)

        assertThat(updatedCaseSettings.hasExternalCreateCaseForm).isTrue()
        assertThat(updatedCaseSettings.externalCreateCaseFormUrl).isEqualTo(externalFormUrl)
    }

    @Test
    fun `should throw IllegalArgumentException when updating case setting 'hasExternalCreateCaseForm' and url value is blank`() {
        val currentCaseSettings = CaseDefinitionSettings(
            name = "name"
        )
        assertThat(currentCaseSettings.hasExternalCreateCaseForm).isFalse()
        assertThat(currentCaseSettings.externalCreateCaseFormUrl).isNull()

        val caseSettingsDto = CaseSettingsDto(
            hasExternalCreateCaseForm = true,
            externalCreateCaseFormUrl = "   "
        )
        assertThat(caseSettingsDto.hasExternalCreateCaseForm).isTrue()
        assertThat(caseSettingsDto.externalCreateCaseFormUrl).isBlank()

        assertThrows<IllegalArgumentException> {
            caseSettingsDto.update(currentCaseSettings)
        }.let { exception ->
            assertThat(exception.message)
                .isEqualTo("Case property [hasExternalCreateCaseForm] can only be true when [externalCreateCaseFormUrl] is not null or blank.")
        }
    }

    @Test
    fun `should throw IllegalArgumentException when updating case setting 'hasExternalCreateCaseForm' is not a valid url`() {
        val currentCaseSettings = CaseDefinitionSettings(
            name = "name"
        )
        assertThat(currentCaseSettings.hasExternalCreateCaseForm).isFalse()
        assertThat(currentCaseSettings.externalCreateCaseFormUrl).isNull()

        val caseSettingsDto = CaseSettingsDto(
            hasExternalCreateCaseForm = true,
            externalCreateCaseFormUrl = "this is not a valid url"
        )
        assertThat(caseSettingsDto.hasExternalCreateCaseForm).isTrue()
        assertThat(caseSettingsDto.externalCreateCaseFormUrl).isNotBlank()

        assertThrows<IllegalArgumentException> {
            caseSettingsDto.update(currentCaseSettings)
        }.let { exception ->
            assertThat(exception.message)
                .isEqualTo("Case property [externalCreateCaseFormUrl] can only be true when [externalCreateCaseFormUrl] is a valid URL.")
        }
    }

    @Test
    fun `should not update case settings when value is null`() {
        val currentCaseSettings = CaseDefinitionSettings(
            name = "name"
        )
        assertFalse(currentCaseSettings.canHaveAssignee)

        val caseSettingsDto = CaseSettingsDto()
        assertThat(caseSettingsDto.canHaveAssignee).isNull()
        assertThat(caseSettingsDto.hasExternalCreateCaseForm).isNull()

        val updatedCaseSettings = caseSettingsDto.update(currentCaseSettings)
        assertThat(updatedCaseSettings.canHaveAssignee).isFalse()
        assertThat(updatedCaseSettings.hasExternalCreateCaseForm).isFalse()
    }

    @Test
    fun `should set autoAssignTasks to false when canHaveAssignee is set to false`() {
        val currentCaseSettings = CaseDefinitionSettings(
            name = "case-name",
            canHaveAssignee = true,
            autoAssignTasks = true
        )
        assertThat(currentCaseSettings.autoAssignTasks).isTrue()
        assertThat(currentCaseSettings.canHaveAssignee).isTrue()

        val caseSettingsDto = CaseSettingsDto(
            canHaveAssignee = false
        )
        assertThat(caseSettingsDto.autoAssignTasks).isNull()

        val updatedCaseSettings = caseSettingsDto.update(currentCaseSettings)
        assertThat(updatedCaseSettings.canHaveAssignee).isFalse()
        assertThat(updatedCaseSettings.autoAssignTasks).isFalse()
    }
}