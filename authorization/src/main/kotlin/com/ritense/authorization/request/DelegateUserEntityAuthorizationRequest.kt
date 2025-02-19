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

package com.ritense.authorization.request

import com.ritense.authorization.Action

class DelegateUserEntityAuthorizationRequest<T>(
    resourceType: Class<T>,
    action: Action<T>,
    override val user: String?,
    entities: List<T>
) : EntityAuthorizationRequest<T>(
    resourceType,
    action,
    entities
) {
    constructor(resourceType: Class<T>, action: Action<T>, user: String?, vararg entities: T) : this(
        resourceType,
        action,
        user,
        if (entities.any { it == null }) emptyList() else entities.filterNotNull().toList()
    )
}