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
package com.ritense

import com.ritense.case.deployment.CaseTabDeploymentService
import com.ritense.case.deployment.CaseTaskListDeploymentService
import com.ritense.outbox.OutboxService
import com.ritense.testutilscommon.junit.extension.LiquibaseRunnerExtension
import com.ritense.valtimo.contract.authentication.UserManagementService
import com.ritense.valtimo.contract.mail.MailSender
import com.ritense.valtimo.repository.CamundaSearchProcessInstanceRepository
import jakarta.inject.Inject
import org.camunda.bpm.engine.RuntimeService
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.mock.mockito.SpyBean
import org.springframework.core.io.support.ResourcePatternResolver
import org.springframework.test.context.junit.jupiter.SpringExtension

@SpringBootTest(properties = ["valtimo.outbox.enabled=true"])
@ExtendWith(SpringExtension::class, LiquibaseRunnerExtension::class)
@Tag("integration")
abstract class BaseIntegrationTest {
    @Inject
    lateinit var runtimeService: RuntimeService

    @MockBean
    lateinit var userManagementService: UserManagementService

    @MockBean
    lateinit var mailSender: MailSender

    @SpyBean
    lateinit var outboxService: OutboxService

    @SpyBean
    lateinit var camundaSearchProcessInstanceRepository: CamundaSearchProcessInstanceRepository

    @SpyBean
    lateinit var resourcePatternResolver: ResourcePatternResolver

    @SpyBean
    lateinit var caseTabDeploymentService: CaseTabDeploymentService

    @SpyBean
    lateinit var caseTaskListDeploymentService: CaseTaskListDeploymentService

    @BeforeEach
    fun beforeEach() {
    }

    @AfterEach
    fun afterEach() {
    }

    companion object {
        @BeforeAll
        fun beforeAll() {
        }
    }
}