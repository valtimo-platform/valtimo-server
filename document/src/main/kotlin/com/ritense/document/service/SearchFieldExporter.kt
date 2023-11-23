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

package com.ritense.document.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.ritense.document.domain.search.SearchConfigurationDto
import com.ritense.export.ExportFile
import com.ritense.export.Exporter
import com.ritense.export.request.DocumentDefinitionExportRequest
import java.io.ByteArrayOutputStream
import org.springframework.transaction.annotation.Transactional

@Transactional(readOnly = true)
class SearchFieldExporter(
    private val objectMapper: ObjectMapper,
    private val searchFieldService: SearchFieldService,
) : Exporter<DocumentDefinitionExportRequest>{

    override fun supports() = DocumentDefinitionExportRequest::class.java

    override fun export(request: DocumentDefinitionExportRequest): Set<ExportFile> {
        val searchFields = searchFieldService.getSearchFields(request.name)

        if(searchFields.isEmpty()) {
            return setOf()
        }

        val exportFile = ByteArrayOutputStream().use {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(it, SearchConfigurationDto(searchFields))

            ExportFile(
                PATH.format(request.name),
                it.toByteArray()
            )
        }

        return setOf(exportFile)
    }

    companion object {
        private const val PATH = "config/search/%s.json"
    }
}