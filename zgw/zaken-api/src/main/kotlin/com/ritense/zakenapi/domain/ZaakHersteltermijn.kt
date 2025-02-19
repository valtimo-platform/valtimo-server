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

package com.ritense.zakenapi.domain

import com.ritense.valtimo.contract.repository.UriAttributeConverter
import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.net.URI
import java.time.LocalDate
import java.util.UUID

@Entity
@Table(name = "zaak_hersteltermijn")
data class ZaakHersteltermijn(

    @Id
    @Column(name = "id")
    val id: UUID = UUID.randomUUID(),

    @Convert(converter = UriAttributeConverter::class)
    @Column(name = "zaak_url")
    val zaakUrl: URI,

    @Column(name = "start_date")
    var startDate: LocalDate,

    @Column(name = "end_date")
    var endDate: LocalDate? = null,

    @Column(name = "max_duration_in_days")
    var maxDurationInDays: Int

)