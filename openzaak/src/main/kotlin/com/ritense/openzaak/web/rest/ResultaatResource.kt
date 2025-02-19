/*
 * Copyright 2020 Dimpact.
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

package com.ritense.openzaak.web.rest

import com.ritense.openzaak.service.impl.model.catalogi.ResultaatType
import com.ritense.openzaak.web.rest.request.ZaakTypeRequest
import com.ritense.valtimo.contract.annotation.SkipComponentScan
import com.ritense.valtimo.contract.domain.ValtimoMediaType.APPLICATION_JSON_UTF8_VALUE
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Deprecated("Since 12.0.0")
@RestController
@SkipComponentScan
@RequestMapping("/api",
    produces = [APPLICATION_JSON_UTF8_VALUE],
    consumes = [APPLICATION_JSON_UTF8_VALUE]
)
interface ResultaatResource {

    @PostMapping ("/v1/openzaak/resultaat") // can be a get if body is moved to queryparam
    fun getResultaatTypes(@RequestBody zaakTypeRequest: ZaakTypeRequest): ResponseEntity<List<ResultaatType>>

}
