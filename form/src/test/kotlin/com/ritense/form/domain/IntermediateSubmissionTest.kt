package com.ritense.form.domain

import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.ritense.form.BaseTest
import com.ritense.valtimo.contract.utils.SecurityUtils
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mockStatic
import java.util.UUID

internal class IntermediateSubmissionTest : BaseTest() {

    @Test
    fun `should create intermediate submission`() {
        mockStatic(SecurityUtils::class.java).use { mockedUtils ->
            mockedUtils.`when`<Any> { SecurityUtils.getCurrentUserLogin() }.thenReturn("userId")

            val submissionId = SubmissionId.newId(UUID.fromString("f04d58c2-8f40-4ff2-a34a-7ae2d16a42f6"))
            val content = JsonNodeFactory.instance.objectNode().apply { put("key", "value") }
            val taskInstanceId = "taskInstanceId"
            val intermediateSubmission = IntermediateSubmission.new(
                submissionId = submissionId,
                content = content,
                taskInstanceId = taskInstanceId
            )

            assertThat(intermediateSubmission).isNotNull
            assertThat(intermediateSubmission.submissionId).isEqualTo(submissionId)
            assertThat(intermediateSubmission.content).isEqualTo(content)
            assertThat(intermediateSubmission.taskInstanceId).isEqualTo(taskInstanceId)
            assertThat(intermediateSubmission.createdOn).isNotNull
            assertThat(intermediateSubmission.editedOn).isNull()
            assertThat(intermediateSubmission.editedBy).isNull()
        }
    }

    @Test
    fun `should change intermediate submission`() {
        mockStatic(SecurityUtils::class.java).use { mockedUtils ->
            mockedUtils.`when`<Any> { SecurityUtils.getCurrentUserLogin() }.thenReturn("userId")

            val submissionId = SubmissionId.newId(UUID.fromString("f04d58c2-8f40-4ff2-a34a-7ae2d16a42f6"))
            val content = JsonNodeFactory.instance.objectNode().apply { put("key", "original") }
            val taskInstanceId = "taskInstanceId"
            var intermediateSubmission = IntermediateSubmission.new(
                submissionId = submissionId,
                content = content,
                taskInstanceId = taskInstanceId
            )

            val contentChanged = JsonNodeFactory.instance.objectNode().apply { put("key", "new value") }
            intermediateSubmission = intermediateSubmission.changeSubmissionContent(contentChanged)

            assertThat(intermediateSubmission.content).isEqualTo(contentChanged)
            assertThat(intermediateSubmission.editedOn).isNotNull
            assertThat(intermediateSubmission.editedBy).isNotNull
        }
    }

}