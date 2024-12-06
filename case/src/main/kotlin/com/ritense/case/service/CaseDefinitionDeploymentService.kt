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

package com.ritense.case.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.ritense.case_.domain.definition.CaseDefinition
import com.ritense.case_.repository.CaseDefinitionRepository
import com.ritense.valtimo.contract.annotation.SkipComponentScan
import mu.KotlinLogging
import org.springframework.core.io.ResourceLoader
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Transactional
@Service
@SkipComponentScan
class CaseDefinitionDeploymentService(
    private val resourceLoader: ResourceLoader,
    private val objectMapper: ObjectMapper,
    private val caseDefinitionRepository: CaseDefinitionRepository
) {
    fun deploy(fileContent: String, forceDeploy: Boolean = false) {
        val caseDefinition = objectMapper.readValue<CaseDefinition>(fileContent)


        logger.debug { "Deploying case definition ${caseDefinition.name}" }
        val caseDefinitionSettings = caseDefinitionRepository.findByIdOrNull(caseDefinition.id)

        if (caseDefinitionSettings == null || forceDeploy) {
/*            val settingsToDeploy = objectMapper.readValue<ObjectNode>(settingsJson)
                .put("name", caseDefinitionName)
            val createdCaseDefinitionSettings: CaseDefinitionSettings = objectMapper.convertValue(settingsToDeploy)*/

            caseDefinitionRepository.save(caseDefinition)
            logger.debug { "Case definition ${caseDefinition.name} was created" }
        } else {
            logger.debug { "Attempted to update settings for case that already exist ${caseDefinition.name}" }
        }
    }

    companion object {
        val logger = KotlinLogging.logger {}
    }
}
