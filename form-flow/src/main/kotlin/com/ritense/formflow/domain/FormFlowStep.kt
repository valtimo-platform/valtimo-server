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

import org.hibernate.annotations.Type
import javax.persistence.Column
import javax.persistence.EmbeddedId
import javax.persistence.Entity
import javax.persistence.Table
import javax.validation.constraints.NotBlank

@Entity
@Table(name = "form_flow_step")
data class FormFlowStep(

    @EmbeddedId
    val id: FormFlowStepId,

    @Column(name = "first_step")
    @field:NotBlank
    val firstStep: Boolean,

    @Type(type = "com.vladmihalcea.hibernate.type.json.JsonStringType")
    @Column(name = "next_steps", columnDefinition = "JSON")
    val nextSteps: List<FormFlowNextStep>
)
