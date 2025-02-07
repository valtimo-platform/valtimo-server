package com.ritense.document.service

import com.ritense.authorization.AuthorizationContext
import com.ritense.document.BaseIntegrationTest
import com.ritense.document.domain.CaseTagColor
import com.ritense.document.web.rest.dto.CaseTagCreateRequestDto
import com.ritense.document.web.rest.dto.CaseTagUpdateRequestDto
import jakarta.validation.ConstraintViolationException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional


@Transactional
class CaseTagServiceIntTest @Autowired constructor(
    private val caseTagService: CaseTagService
) : BaseIntegrationTest() {

    @Test
    fun `should create a case tag`() {

        val request = CaseTagCreateRequestDto(
            key = "some-tag",
            title = "Some Tag",
            color = CaseTagColor.COOLGRAY
        )

        AuthorizationContext.runWithoutAuthorization {
            caseTagService.create("house", request)
        }

        assertEquals(CaseTagColor.COOLGRAY, caseTagService.get("house", "some-tag" ).color)
    }

    @Test
    fun `should throw error when creating status with invalid key`() {
        AuthorizationContext.runWithoutAuthorization {
            val exception = assertThrows<ConstraintViolationException> {
                caseTagService.create(
                    "house",
                    CaseTagCreateRequestDto(
                        key = "<this-is-not-a-valid-tag#>",
                        title = "Some Tag",
                        color = CaseTagColor.COOLGRAY
                    )
                )
            }
            kotlin.test.assertEquals("""create.request.key: must match "[a-z][a-z0-9-_]+"""", exception.message)
        }
    }

    @Test
    fun `should update tag for existing tag`() {
        AuthorizationContext.runWithoutAuthorization {
            caseTagService.create(
                "house",
                CaseTagCreateRequestDto(
                    key = "some-tag",
                    title = "Some Tag",
                    color = CaseTagColor.COOLGRAY
                )
            )

            caseTagService.update(
                "house",
                "some-tag",
                CaseTagUpdateRequestDto(
                    key = "some-tag",
                    title = "New Title",
                    color = CaseTagColor.BLUE
                )
            )

            val updatedTag = caseTagService.get("house","some-tag" )

            assertEquals(CaseTagColor.BLUE ,updatedTag.color)
            assertEquals("New Title" ,updatedTag.title)
        }
    }

}