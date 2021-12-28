/*
 * Copyright 2015-2021 Ritense BV, the Netherlands.
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

package com.ritense.document.export.domain

import org.hibernate.annotations.Type
import javax.persistence.Column
import javax.persistence.Embeddable

@Embeddable
data class Tree(
    @Type(type = "com.vladmihalcea.hibernate.type.json.JsonStringType")
    @Column(name = "nodes", columnDefinition = "json")
    val nodes: List<Node>
) {

    companion object {

        fun fromDocumentDefinition(definition: String): Tree {
            return Tree(emptyList())
        }

    }

}