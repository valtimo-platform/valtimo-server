/*
 * Copyright 2015-2023 Ritense BV, the Netherlands.
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

package com.ritense.authorization.permission.condition

import com.fasterxml.jackson.annotation.JsonTypeName
import com.fasterxml.jackson.annotation.JsonView
import com.ritense.authorization.Action
import com.ritense.authorization.AuthorizationEntityMapper
import com.ritense.authorization.AuthorizationServiceHolder
import com.ritense.authorization.permission.ConditionContainer
import com.ritense.authorization.permission.Permission
import com.ritense.authorization.permission.PermissionView
import com.ritense.authorization.permission.condition.ContainerPermissionCondition.Companion.CONTAINER
import com.ritense.authorization.request.EntityAuthorizationRequest
import com.ritense.authorization.role.Role
import com.ritense.authorization.specification.AuthorizationSpecification
import com.ritense.valtimo.contract.database.QueryDialectHelper
import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.CriteriaQuery
import javax.persistence.criteria.Predicate
import javax.persistence.criteria.Root

@JsonTypeName(CONTAINER)
data class ContainerPermissionCondition<TO : Any>(
    @field:JsonView(value = [PermissionView.RoleManagement::class, PermissionView.PermissionManagement::class])
    val resourceType: Class<TO>,
    @field:JsonView(value = [PermissionView.RoleManagement::class, PermissionView.PermissionManagement::class])
    val conditions: List<PermissionCondition>
) : PermissionCondition(PermissionConditionType.CONTAINER) {
    override fun <FROM: Any> isValid(entity: FROM): Boolean {
        val mapper = findMapper(entity::class.java) as AuthorizationEntityMapper<FROM, TO>
        val relatedEntities = mapper.mapRelated(entity)
        return relatedEntities.any { relatedEntity ->
            val spec = findChildSpecification(relatedEntity)
            spec.isAuthorized()
        }
    }

    override fun <T : Any> toPredicate(
        root: Root<T>,
        query: CriteriaQuery<*>,
        criteriaBuilder: CriteriaBuilder,
        resourceType: Class<T>,
        queryDialectHelper: QueryDialectHelper
    ): Predicate {
        val authorizationEntityMapperResult = findMapper(resourceType).mapQuery(root, query, criteriaBuilder)
        val spec = findChildSpecification()

        return criteriaBuilder.and(
            authorizationEntityMapperResult.joinPredicate,
            spec.toPredicate(
                authorizationEntityMapperResult.root,
                authorizationEntityMapperResult.query,
                criteriaBuilder
            )
        )
    }

    private fun findChildSpecification(entity: TO? = null): AuthorizationSpecification<TO> {
        return AuthorizationServiceHolder.currentInstance.getAuthorizationSpecification(
            EntityAuthorizationRequest(this.resourceType, Action(Action.IGNORE), entity),
            listOf(
                Permission(
                    resourceType = resourceType,
                    action = Action<Any>(Action.IGNORE),
                    conditionContainer = ConditionContainer(conditions),
                    role = Role(key = "")
                )
            )
        )
    }

    private fun <FROM: Any> findMapper(fromType: Class<FROM>): AuthorizationEntityMapper<FROM, TO> {
        return AuthorizationServiceHolder.currentInstance.getMapper(fromType, this.resourceType)
    }

    companion object {
        const val CONTAINER = "container"
    }
}