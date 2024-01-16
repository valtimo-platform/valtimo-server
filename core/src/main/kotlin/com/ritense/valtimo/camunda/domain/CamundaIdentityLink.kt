/*
 *  Copyright 2015-2023 Ritense BV, the Netherlands.
 *
 *  Licensed under EUPL, Version 1.2 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" basis,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.ritense.valtimo.camunda.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(name = "ACT_RU_IDENTITYLINK")
class CamundaIdentityLink(

    @Id
    @Column(name = "ID_")
    val id: String,

    @Column(name = "REV_")
    val revision: Int,

    @Column(name = "GROUP_ID_")
    val groupId: String?,

    @Column(name = "TYPE_")
    val type: String?,

    @Column(name = "USER_ID_")
    val userId: String?,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TASK_ID_")
    val task: CamundaTask?,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PROC_DEF_ID_")
    val processDefinition: CamundaProcessDefinition?,

    @Column(name = "TENANT_ID_")
    val tenantId: String?

)