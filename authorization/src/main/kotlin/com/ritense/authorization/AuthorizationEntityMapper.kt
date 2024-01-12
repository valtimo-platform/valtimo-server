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

package com.ritense.authorization

import jakarta.persistence.criteria.AbstractQuery
import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.Root

interface AuthorizationEntityMapper<FROM, TO> {
    fun mapRelated(entity: FROM): List<TO>

    fun mapQuery(root: Root<FROM>, query: AbstractQuery<*>, criteriaBuilder: CriteriaBuilder): AuthorizationEntityMapperResult<TO>

    fun supports(fromClass: Class<*>, toClass: Class<*>): Boolean
}