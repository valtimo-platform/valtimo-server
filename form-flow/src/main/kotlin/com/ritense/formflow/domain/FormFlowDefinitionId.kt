/*
 * Copyright 2015-2022 Ritense BV, the Netherlands.
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

package com.ritense.formflow.domain

import com.fasterxml.jackson.annotation.JsonCreator
import lombok.EqualsAndHashCode
import javax.persistence.Column
import javax.persistence.Embeddable

@Embeddable
@EqualsAndHashCode(callSuper = false)
data class FormFlowDefinitionId(

    @Column(name = "key")
    val key: String,

    @Column(name = "version")
    var version: Long? = null

) : AbstractId<FormFlowDefinitionId>() {

    override fun toString(): String {
        return "$key:$version"
    }

    companion object {
        @JvmStatic
        @JsonCreator
        fun create(value: String) = newId(value)

        fun newId(key: String): FormFlowDefinitionId {
            return FormFlowDefinitionId(key, 1).newIdentity()
        }

        fun nextVersion(id: FormFlowDefinitionId): FormFlowDefinitionId {
            return FormFlowDefinitionId(id.key, id.version!! + 1).newIdentity()
        }

        fun existingId(id: FormFlowDefinitionId): FormFlowDefinitionId {
            return FormFlowDefinitionId(id.key, id.version)
        }
    }
}