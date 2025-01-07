package com.ritense.formviewmodel.submission

import com.ritense.form.service.FormDefinitionService
import com.ritense.formviewmodel.BaseTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
class FormViewModelStartFormSubmissionHandlerFactoryTest(
    @Mock private val formDefinitionService: FormDefinitionService,
) : BaseTest() {

    private lateinit var formViewModelStartFormSubmissionHandlerFactory: FormViewModelStartFormSubmissionHandlerFactory

    @BeforeEach
    fun setUp() {
        formViewModelStartFormSubmissionHandlerFactory = FormViewModelStartFormSubmissionHandlerFactory(
            listOf(TestStartFormSubmissionHandler()),
            formDefinitionService
        )
    }

    @Test
    fun `should create submission handler`() {
        val handler = formViewModelStartFormSubmissionHandlerFactory.getHandler("test")
        assertThat(handler).isInstanceOf(TestStartFormSubmissionHandler::class.java)
    }

    @Test
    fun `should return null when no submission handler found`() {
        val handler = formViewModelStartFormSubmissionHandlerFactory.getHandler("doesNotExist")
        assertThat(handler).isNull()
    }
}