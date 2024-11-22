package com.ritense.processdocument.service

import com.ritense.valtimo.contract.event.DocumentDeletedEvent
import org.camunda.bpm.engine.RuntimeService
import org.camunda.bpm.engine.runtime.ProcessInstance
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.RETURNS_DEEP_STUBS
import org.mockito.Mockito.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.UUID

class ProcessDocumentDeletedEventListenerTest {

    var processDocumentDeletedEventListener: ProcessDocumentDeletedEventListener? = null
    var runtimeService = mock<RuntimeService>(RETURNS_DEEP_STUBS)

    @BeforeEach
    fun setUp() {
        processDocumentDeletedEventListener = ProcessDocumentDeletedEventListener(runtimeService)
    }


    @Test
    fun `should delete process instances with business key`() {
        val documentId = UUID.fromString("d1f1b3ed-7575-45bb-a02b-18f378ddc34d")

        val processInstance1 = mock<ProcessInstance>()
        val processInstanceId1 = "processInstanceId1"
        whenever(processInstance1.processInstanceId).thenReturn(processInstanceId1)
        val processInstance2 = mock<ProcessInstance>()
        val processInstanceId2 = "processInstanceId2"
        whenever(processInstance2.processInstanceId).thenReturn(processInstanceId2)

        whenever(runtimeService.createProcessInstanceQuery().processInstanceBusinessKey(documentId.toString()).list())
            .thenReturn(listOf(processInstance1, processInstance2))

        processDocumentDeletedEventListener!!.handle(DocumentDeletedEvent(documentId))

        verify(runtimeService).deleteProcessInstance("processInstanceId1", "Document deleted", false, true)
        verify(runtimeService).deleteProcessInstance("processInstanceId2", "Document deleted", false, true)
    }
}