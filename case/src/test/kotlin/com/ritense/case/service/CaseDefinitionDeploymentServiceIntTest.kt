package com.ritense.case.service

import com.ritense.case.BaseIntegrationTest
import com.ritense.case_.repository.CaseDefinitionRepository
import com.ritense.valtimo.contract.case_.CaseDefinitionId
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.transaction.annotation.Transactional
import kotlin.test.assertEquals

@Transactional
class CaseDefinitionDeploymentServiceIntTest @Autowired constructor(
    private val caseDefinitionDeploymentService: CaseDefinitionDeploymentService,
    private val caseDefinitionRepository: CaseDefinitionRepository
) : BaseIntegrationTest() {
    @Test
    fun `should deploy case definition`() {
        val key = "test"
        val version = "1.0.0"

        caseDefinitionDeploymentService.deploy(
            """
            {
                "key": "$key",
                "versionTag": "$version",
                "name": "Test",
                "canHaveAssignee": true,
                "autoAssignTasks": false
            }
        """.trimIndent()
        )

        val deployedCaseDefinition = caseDefinitionRepository.findByIdOrNull(CaseDefinitionId.of(key, version))

        assertEquals(deployedCaseDefinition?.id?.key, key)
        assertEquals(deployedCaseDefinition?.canHaveAssignee, true)
        assertEquals(deployedCaseDefinition?.autoAssignTasks, false)
    }

    @Test
    fun `should throw exception when case definition is invalid`() {
        val invalidCaseDefinition = """
        {
            "key": "key",
            "versionTag": "1.0.0",
            "canHaveAssignee": true,
            "autoAssignTasks": false
        }
    """.trimIndent()

        assertThrows<IllegalArgumentException> {
            caseDefinitionDeploymentService.deploy(invalidCaseDefinition)
        }
    }

    @Test
    fun `should create case settings with default values when settings are not defined`() {
        val key = "test"
        val version = "1.0.0"

        caseDefinitionDeploymentService.deploy(
            """
            {
                "key": "$key",
                "versionTag": "$version",
                "name": "Test"
            }
        """.trimIndent()
        )

        val deployedCaseDefinition = caseDefinitionRepository.findByIdOrNull(CaseDefinitionId.of(key, version))

        assertEquals(deployedCaseDefinition?.id?.key, key)
        assertEquals(deployedCaseDefinition?.canHaveAssignee, false)
        assertEquals(deployedCaseDefinition?.autoAssignTasks, false)
    }
}