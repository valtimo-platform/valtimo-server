/*
 *  Copyright 2015-2022 Ritense BV, the Netherlands.
 *
 *  Licensed under EUPL, Version 1.2 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" basis,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.ritense.valtimo.formflow.web.rest

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import com.ritense.formflow.domain.definition.FormFlowDefinition
import com.ritense.formflow.domain.definition.FormFlowDefinitionId
import com.ritense.formflow.domain.definition.FormFlowNextStep
import com.ritense.formflow.domain.definition.FormFlowStep
import com.ritense.formflow.domain.definition.FormFlowStepId
import com.ritense.formflow.service.FormFlowService
import com.ritense.valtimo.contract.utils.TestUtil
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders

internal class FormFlowResourceTest {
    lateinit var mockMvc: MockMvc
    lateinit var formFlowResource: FormFlowResource
    lateinit var formFlowService: FormFlowService

    @BeforeEach
    fun init() {
        formFlowService = mock()
        formFlowResource = FormFlowResource(formFlowService)
        mockMvc = MockMvcBuilders.standaloneSetup(formFlowResource).build()
    }

    @Test
    fun `should create form flow instance for definition key without additional parameters`() {
        val step1 = FormFlowStep(FormFlowStepId("key2"))
        val step2 = FormFlowStep(FormFlowStepId("key3"))
        val definition = FormFlowDefinition(
            id = FormFlowDefinitionId.newId("key1"), "step1", mutableSetOf(step1, step2))

        whenever(formFlowService.findLatestDefinitionByKey("inkomens_loket")).thenReturn(definition)
        mockMvc
            .perform(
                MockMvcRequestBuilders
                    .post("/api/form-flow/demo/definition/{instanceId}/instance",
                    "inkomens_loket")
                    .accept(MediaType.APPLICATION_JSON_VALUE)
            ).andExpect(status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$").isNotEmpty)
            .andExpect(MockMvcResultMatchers.jsonPath("$.formFlowInstanceId").isNotEmpty)
            .andExpect(MockMvcResultMatchers.jsonPath("$.currentStepId").isNotEmpty)
    }

    @Test
    fun `should create form flow instance for definition key with additional parameters`() {
        val step1 = FormFlowStep(FormFlowStepId("key2"))
        val step2 = FormFlowStep(FormFlowStepId("key3"))
        val definition = FormFlowDefinition(
            id = FormFlowDefinitionId.newId("key1"), "step1", mutableSetOf(step1, step2))

        val additionalProperties: MutableMap<String, Any> = mutableMapOf(Pair("property1", "input1"))

        whenever(formFlowService.findLatestDefinitionByKey("inkomens_loket")).thenReturn(definition)
        mockMvc
            .perform(
                MockMvcRequestBuilders
                    .post("/api/form-flow/demo/definition/{instanceId}/instance",
                        "inkomens_loket")
                    .content(TestUtil.convertObjectToJsonBytes(additionalProperties))
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .accept(MediaType.APPLICATION_JSON_VALUE)
            ).andExpect(status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$").isNotEmpty)
            .andExpect(MockMvcResultMatchers.jsonPath("$.formFlowInstanceId").isNotEmpty)
            .andExpect(MockMvcResultMatchers.jsonPath("$.currentStepId").isNotEmpty)
    }

    @Test
    fun `should complete step for form flow instance without submission data`() {
        val step1 = FormFlowStep(FormFlowStepId("step1"), mutableListOf(FormFlowNextStep(step = "step2")))
        val step2 = FormFlowStep(FormFlowStepId("step2"))
        val definition = FormFlowDefinition(
            id = FormFlowDefinitionId.newId("key1"), "step1", mutableSetOf(step1, step2))

        val instance = definition.createInstance(mutableMapOf())

        whenever(formFlowService.getInstanceById(instance.id)).thenReturn(instance)
        mockMvc
            .perform(
                MockMvcRequestBuilders
                    .post("/api/form-flow/demo/instance/{instanceId}/step/{stepId}/complete",
                        instance.id.id.toString(), instance.currentFormFlowStepInstanceId!!.id.toString()
                    )
                    .accept(MediaType.APPLICATION_JSON_VALUE)
            ).andExpect(status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$").isNotEmpty)
            .andExpect(MockMvcResultMatchers.jsonPath("$.formFlowInstanceId").isNotEmpty)
            .andExpect(MockMvcResultMatchers.jsonPath("$.currentStepId").isNotEmpty)
    }

    @Test
    fun `should complete step for form flow instance with submission data`() {
        val step1 = FormFlowStep(FormFlowStepId("step1"), mutableListOf(FormFlowNextStep(step = "step2")))
        val step2 = FormFlowStep(FormFlowStepId("step2"))
        val definition = FormFlowDefinition(
            id = FormFlowDefinitionId.newId("key1"), "step1", mutableSetOf(step1, step2))

        val instance = definition.createInstance(mutableMapOf())

        whenever(formFlowService.getInstanceById(instance.id)).thenReturn(instance)
        mockMvc
            .perform(
                MockMvcRequestBuilders
                    .post("/api/form-flow/demo/instance/{instanceId}/step/{stepId}/complete",
                        instance.id.id.toString(), instance.currentFormFlowStepInstanceId!!.id.toString()
                    )
                    .content("{\"data\": \"data\"}")
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .accept(MediaType.APPLICATION_JSON_VALUE)
            ).andExpect(status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$").isNotEmpty)
            .andExpect(MockMvcResultMatchers.jsonPath("$.formFlowInstanceId").isNotEmpty)
            .andExpect(MockMvcResultMatchers.jsonPath("$.currentStepId").isNotEmpty)
    }

}