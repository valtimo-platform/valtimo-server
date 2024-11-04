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

package com.ritense.case.domain.casedefinition

import com.ritense.authorization.AuthorizationEntityMapper
import com.ritense.authorization.AuthorizationEntityMapperResult
import com.ritense.case.repository.CaseDefinitionRepository
import com.ritense.valtimo.camunda.domain.CamundaProcessDefinition
import jakarta.persistence.criteria.AbstractQuery
import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.Root

class ProcessDefinitionCaseDefinitionMapper(
    private val caseDefinitionRepository: CaseDefinitionRepository
) : AuthorizationEntityMapper<CamundaProcessDefinition, CaseDefinition> {

    override fun mapRelated(entity: CamundaProcessDefinition): List<CaseDefinition> {
        return listOf(caseDefinitionRepository.findByProcessDefinitionIn(entity.id))
    }

    override fun mapQuery(
        root: Root<CamundaProcessDefinition>,
        query: AbstractQuery<*>,
        criteriaBuilder: CriteriaBuilder
    ): AuthorizationEntityMapperResult<CaseDefinition> {
        val caseRoot: Root<CaseDefinition> = query.from(CaseDefinition::class.java)
        // ROOT CamundaProcessDefinition to CaseDefinition
        return AuthorizationEntityMapperResult(
            root = caseRoot,
            query = query,
            joinPredicate = criteriaBuilder.`in`(
                root.get<String>("id")
            ).value(
                caseRoot.get<Set<CaseCamundaProcessDefinition>>("processDefinitions").get("id")
            )
        )
    }

    override fun supports(fromClass: Class<*>, toClass: Class<*>): Boolean {
        return fromClass == CamundaProcessDefinition::class.java && toClass == CaseDefinition::class.java
    }

}