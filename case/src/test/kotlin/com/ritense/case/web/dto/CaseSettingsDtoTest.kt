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
    fun `should update case setting 'hasExternalStartCaseForm' when url value is not null`() {
        val externalFormUrl = "https://example.com/create-case-form"
        val currentCaseSettings = CaseDefinitionSettings(
            name = "name"
        )

        assertThat(currentCaseSettings.hasExternalStartCaseForm).isFalse()
        assertThat(currentCaseSettings.externalStartCaseFormUrl).isNull()

        val caseSettingsDto = CaseSettingsDto(
            hasExternalStartCaseForm = true,
            externalStartCaseFormUrl = externalFormUrl
        )
        val updatedCaseSettings = caseSettingsDto.update(currentCaseSettings)

        assertThat(updatedCaseSettings.hasExternalStartCaseForm).isTrue()
        assertThat(updatedCaseSettings.externalStartCaseFormUrl).isEqualTo(externalFormUrl)
    }

    @Test
    fun `should throw IllegalArgumentException when updating case setting 'hasExternalStartCaseForm' and url value is blank`() {
        val currentCaseSettings = CaseDefinitionSettings(
            name = "name"
        )
        assertThat(currentCaseSettings.hasExternalStartCaseForm).isFalse()
        assertThat(currentCaseSettings.externalStartCaseFormUrl).isNull()

        val caseSettingsDto = CaseSettingsDto(
            hasExternalStartCaseForm = true,
            externalStartCaseFormUrl = "   "
        )
        assertThat(caseSettingsDto.hasExternalStartCaseForm).isTrue()
        assertThat(caseSettingsDto.externalStartCaseFormUrl).isBlank()

        assertThrows<IllegalArgumentException> {
            caseSettingsDto.update(currentCaseSettings)
        }.let { exception ->
            assertThat(exception.message)
                .isEqualTo("Case property [hasExternalStartCaseForm] can only be true when [externalStartCaseFormUrl] is not null or blank.")
        }
    }

    @Test
    fun `should throw IllegalArgumentException when updating case setting 'hasExternalStartCaseForm' is not a valid url`() {
        val currentCaseSettings = CaseDefinitionSettings(
            name = "name"
        )
        assertThat(currentCaseSettings.hasExternalStartCaseForm).isFalse()
        assertThat(currentCaseSettings.externalStartCaseFormUrl).isNull()

        val caseSettingsDto = CaseSettingsDto(
            hasExternalStartCaseForm = true,
            externalStartCaseFormUrl = "this is not a valid url"
        )
        assertThat(caseSettingsDto.hasExternalStartCaseForm).isTrue()
        assertThat(caseSettingsDto.externalStartCaseFormUrl).isNotBlank()

        assertThrows<IllegalArgumentException> {
            caseSettingsDto.update(currentCaseSettings)
        }.let { exception ->
            assertThat(exception.message)
                .isEqualTo("Case property [externalStartCaseFormUrl] is not a valid URL.")
        }
    }

    @Test
    fun `should throw IllegalArgumentException when updating case setting 'hasExternalStartCaseForm' exceeds 512 characters`() {
        val currentCaseSettings = CaseDefinitionSettings(
            name = "name"
        )
        assertThat(currentCaseSettings.hasExternalStartCaseForm).isFalse()
        assertThat(currentCaseSettings.externalStartCaseFormUrl).isNull()

        val caseSettingsDto = CaseSettingsDto(
            hasExternalStartCaseForm = true,
            externalStartCaseFormUrl = "https://www.example.com/search?param1=value10&param2=value20&param3=value30&param4=value40&param5=value50&param6=value60&param7=value70&param8=value80&param9=value90&param10=value100&param11=value110&param12=value120&param13=value130&param14=value140&param15=value150&param16=value160&param17=value170&param18=value180&param19=value190&param20=value200&param21=value210&param22=value220&param23=value230&param24=value240&param25=value250&param26=value260&param27=value270&param28=value280&param29=value290&extra_param=xxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"
        )
        assertThat(caseSettingsDto.hasExternalStartCaseForm).isTrue()
        assertThat(caseSettingsDto.externalStartCaseFormUrl).isNotBlank()

        assertThrows<IllegalArgumentException> {
            caseSettingsDto.update(currentCaseSettings)
        }.let { exception ->
            assertThat(exception.message)
                .isEqualTo("Case property [externalStartCaseFormUrl] exceeds the maximum length of 512 characters.")
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
        assertThat(caseSettingsDto.hasExternalStartCaseForm).isNull()

        val updatedCaseSettings = caseSettingsDto.update(currentCaseSettings)
        assertThat(updatedCaseSettings.canHaveAssignee).isFalse()
        assertThat(updatedCaseSettings.hasExternalStartCaseForm).isFalse()
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