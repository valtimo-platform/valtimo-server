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

package com.ritense.resource.web.rest

import com.ritense.resource.web.ObjectUrlDTO
import com.ritense.resource.web.ResourceDTO
import com.ritense.valtimo.contract.annotation.SkipComponentScan
import com.ritense.valtimo.contract.domain.ValtimoMediaType.APPLICATION_JSON_UTF8_VALUE
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@SkipComponentScan
@RequestMapping("/api", produces = [APPLICATION_JSON_UTF8_VALUE])
interface ResourceResource {

    @GetMapping("/v1/resource/{resourceId}")
    fun get(@PathVariable(name = "resourceId") resourceId: String): ResponseEntity<ObjectUrlDTO>

    @GetMapping("/v1/resource/{resourceId}/download")
    fun getContent(@PathVariable(name = "resourceId") resourceId: String): ResponseEntity<ByteArray>

    @PutMapping("/v1/resource", consumes = [APPLICATION_JSON_UTF8_VALUE])
    fun register(@RequestBody resourceDTO: ResourceDTO): ResponseEntity<ResourceDTO>

    @DeleteMapping("/v1/resource/{resourceId}")
    fun delete(@PathVariable(name = "resourceId") resourceId: String): ResponseEntity<Void>

}
