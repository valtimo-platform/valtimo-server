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

package com.ritense.valtimo.contract.authorization

import com.ritense.authorization.UserManagementServiceHolder

object CurrentUserExpressionHandler {

    fun <V> resolveValue(value: V?): Any? {
        if (value is String) {
            return when (value) {
                PermissionConditionKey.CURRENT_USER_ID.key -> UserManagementServiceHolder.currentInstance.currentUser.id
                PermissionConditionKey.CURRENT_USER_EMAIL.key -> UserManagementServiceHolder.currentInstance.currentUser.email
                PermissionConditionKey.CURRENT_USER_ROLES.key -> UserManagementServiceHolder.currentInstance.currentUser.roles
                PermissionConditionKey.CURRENT_USER_IDENTIFIER.key -> UserManagementServiceHolder.currentInstance.currentUser.userIdentifier
                else -> value
            }
        }
        return value
    }

}
