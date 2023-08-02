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

package com.ritense.verzoek

import com.ritense.catalogiapi.service.ZaaktypeUrlProvider
import com.ritense.document.domain.impl.JsonSchemaDocument
import com.ritense.document.service.DocumentService
import com.ritense.document.service.impl.JsonSchemaDocumentDefinitionService
import com.ritense.objectmanagement.service.ObjectManagementService
import com.ritense.plugin.service.PluginService
import com.ritense.processdocument.service.ProcessDocumentService
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class VerzoekAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(VerzoekPluginFactory::class)
    fun verzoekPluginFactory(
        pluginService: PluginService,
        documentDefinitionService: JsonSchemaDocumentDefinitionService,
    ): VerzoekPluginFactory {
        return VerzoekPluginFactory(pluginService, documentDefinitionService)
    }

    @Bean
    @ConditionalOnMissingBean(VerzoekPluginEventListener::class)
    fun verzoekPluginEventListener(
        pluginService: PluginService,
        objectManagementService: ObjectManagementService,
        documentService: DocumentService<JsonSchemaDocument>,
        zaaktypeUrlProvider: ZaaktypeUrlProvider,
        processDocumentService: ProcessDocumentService
    ): VerzoekPluginEventListener {
        return VerzoekPluginEventListener(
            pluginService,
            objectManagementService,
            documentService,
            zaaktypeUrlProvider,
            processDocumentService
        )
    }
}
