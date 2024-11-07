package com.ritense.case.service

import com.ritense.BaseIntegrationTest
import com.ritense.authorization.Action
import com.ritense.authorization.AuthorizationContext.Companion.runWithoutAuthorization
import com.ritense.authorization.AuthorizationService
import com.ritense.authorization.request.EntityAuthorizationRequest
import com.ritense.case.domain.CaseTabType
import com.ritense.case.web.rest.dto.CaseTabDto
import com.ritense.valtimo.camunda.domain.CamundaProcessDefinition
import com.ritense.valtimo.camunda.repository.CamundaProcessDefinitionRepository
import com.ritense.valtimo.service.CamundaProcessService
import jakarta.transaction.Transactional
import org.assertj.core.api.Assertions.assertThat
import org.camunda.bpm.engine.RepositoryService
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.test.context.support.WithMockUser
import java.util.UUID

@Transactional
class CaseDefinitionServiceIntTest @Autowired constructor(
    private val caseDefinitionService: CaseDefinitionService,
    private val caseTabService: CaseTabService,
    private val repositoryService: RepositoryService,
    private val camundaProcessService: CamundaProcessService,
    private val camundaProcessDefinitionRepository: CamundaProcessDefinitionRepository,
    private val authorizationService: AuthorizationService
) : BaseIntegrationTest() {

    @Test
    fun `should deploy case-definition`() {
        val id = UUID.randomUUID()
        val name = "case-definition-name"
        val version = "1.0.0"
        val result = caseDefinitionService.deployCaseDefinition(
            id = id,
            name = name,
            version = version
        )
        assertThat(result.id).isEqualTo(id)
        assertThat(result.name).isEqualTo(name)
        assertThat(result.version.toString()).isEqualTo(version)
    }

    @Test
    @WithMockUser(username = "john.doe@ritense.com", authorities = ["ROLE_CUSTOM_TEST"])
    fun `should link case-definition to CaseTab`() {
        val id = UUID.randomUUID()
        val name = "definition-test"
        val version = "1.0.0"

        // Given
        runWithoutAuthorization {
            val caseDefinition = caseDefinitionService.deployCaseDefinition(
                id = id,
                name = name,
                version = version
            )
            // When
            val result = caseTabService.createCaseTab(
                caseDefinitionName = name, // Reuse the name here, if this is not f
                caseTabDto = CaseTabDto(
                    key = "case-tab-key",
                    type = CaseTabType.CUSTOM,
                    contentKey = "case-tab-content-key"
                ),
                caseDefinitionId = caseDefinition.id,
            )
            // Then
            assertThat(result.caseDefinitionId).isEqualTo(id)
        }

        // Retrieve the case tab
        val caseTab = caseTabService.getCaseTab(
            caseDefinitionName = "definition-test",
            key = "case-tab-key"
        )
        assertThat(caseTab.caseDefinitionId).isEqualTo(id)

        // Retrieve the case tab via list call
        val caseTabs = caseTabService.getCaseTabs(
            caseDefinitionName = "definition-test"
        )
        assertThat(caseTabs).hasSize(1)
        // next steps deployer
        // custom export for the case definition into a archive for deployer
        // or
        // (Skipped) make an archive manually
        // (Made prototype) build a mini ui that does this. As a discussion piece. using exiting importer and exporter.
        // build deployer that can deploy a case definition from an export.

        // (in progress) phase 2 case definition: connect the process definition to the case definition
        // processDefinition is from camunda cannot change the table with caseDefinitionId
        // create a new table that links the processDefinitionId to the caseDefinitionId
        // onDeployment catch the event ProcessDefinitionDeployedEvent link it.
        // 1 to many so list with many-many table and then in JPA.
    }

    @Test
    @WithMockUser(username = "john.doe@ritense.com", authorities = ["ROLE_PDTEST"])
    fun `should link case-definition to CamundaProcessDefinition`() {
        val id = UUID.randomUUID()
        val name = "definition-test"
        val version = "1.0.0"

        // Given
        runWithoutAuthorization {
            val caseDefinition = caseDefinitionService.deployCaseDefinition(
                id = id,
                name = name,
                version = version
            )
            // When
            val process =
                repositoryService.createProcessDefinitionQuery().processDefinitionKey("test-process-cd").singleResult()!!
            val result = caseDefinitionService.assignProcessDefinition(id, process.id)
            // Then
            assertThat(result.id).isEqualTo(id)
            assertThat(result.processDefinitions).hasSize(1)
        }

        // Start the process and hit pcab
        val processInstanceWithDefinition = camundaProcessService.startProcess("test-process-cd", "", emptyMap())
        assertThat(processInstanceWithDefinition).isNotNull

        // The list call is missing
        val spec = authorizationService.getAuthorizationSpecification(
            EntityAuthorizationRequest(
                CamundaProcessDefinition::class.java,
                Action("view_list")
            )
        )
        val camundaProcessDefinitions = camundaProcessDefinitionRepository.findAll(spec)
        assertThat(camundaProcessDefinitions).hasSize(1)
    }

    @Test
    @WithMockUser(username = "john.doe@ritense.com", authorities = ["ROLE_PDTEST"])
    fun `should link case-definition to CamundaProcessDefinition but not match on version`() {
        val id = UUID.randomUUID()
        val name = "definition-test"
        val version = "2.0.0"

        // Given
        runWithoutAuthorization {
            val caseDefinition = caseDefinitionService.deployCaseDefinition(
                id = id,
                name = name,
                version = version
            )
            // When
            val process = repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey("test-process")
                .singleResult()!!

            val result = caseDefinitionService.assignProcessDefinition(id, process.id)
            // Then
            assertThat(result.id).isEqualTo(id)
            assertThat(result.processDefinitions).hasSize(1)
        }

        assertThrows<AccessDeniedException> {
            val processInstanceWithDefinition = camundaProcessService.startProcess("test-process-cd", "", emptyMap())
        }

        // The list call is missing
        val spec = authorizationService.getAuthorizationSpecification(
            EntityAuthorizationRequest(
                CamundaProcessDefinition::class.java,
                Action("view_list")
            )
        )
        val camundaProcessDefinitions = camundaProcessDefinitionRepository.findAll(spec)
        assertThat(camundaProcessDefinitions).hasSize(0)
    }
}