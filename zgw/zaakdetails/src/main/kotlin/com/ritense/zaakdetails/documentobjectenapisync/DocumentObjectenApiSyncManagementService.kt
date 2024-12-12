package com.ritense.zaakdetails.documentobjectenapisync

import com.ritense.document.domain.impl.JsonSchemaDocumentDefinition
import com.ritense.logging.LoggableResource
import com.ritense.objectsapi.service.ObjectSyncService
import com.ritense.valtimo.contract.annotation.SkipComponentScan
import com.ritense.zaakdetails.documentobjectenapisync.DocumentObjectenApiSyncService.Companion.logger
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Transactional
@Service
@SkipComponentScan
class DocumentObjectenApiSyncManagementService(
    private val documentObjectenApiSyncRepository: DocumentObjectenApiSyncRepository,
    private val objectSyncService: ObjectSyncService,

    ) {
    fun getSyncConfiguration(
        @LoggableResource(resourceType = JsonSchemaDocumentDefinition::class) documentDefinitionName: String,
        documentDefinitionVersion: Long
    ): DocumentObjectenApiSync? {
        logger.debug { "Get sync configuration documentDefinitionName=$documentDefinitionName" }
        return documentObjectenApiSyncRepository.findByDocumentDefinitionNameAndDocumentDefinitionVersion(
            documentDefinitionName,
            documentDefinitionVersion
        )
    }

    fun saveSyncConfiguration(sync: DocumentObjectenApiSync) {
        logger.info { "Save sync configuration documentDefinitionName=${sync.documentDefinitionName}" }
        val modifiedSync = getSyncConfiguration(sync.documentDefinitionName, sync.documentDefinitionVersion)
            ?.copy(
                objectManagementConfigurationId = sync.objectManagementConfigurationId,
                enabled = sync.enabled
            )
            ?: sync

        // Remove old connector configuration
        objectSyncService.getObjectSyncConfig(sync.documentDefinitionName).content
            .forEach { objectSyncService.removeObjectSyncConfig(it.id.id) }

        documentObjectenApiSyncRepository.save(modifiedSync)
    }

    fun deleteSyncConfigurationByDocumentDefinition(
        @LoggableResource(resourceType = JsonSchemaDocumentDefinition::class) documentDefinitionName: String,
        documentDefinitionVersion: Long
    ) {
        logger.info {
            """Delete sync configuration documentDefinitionName=$documentDefinitionName
                documentDefinitionVersion=$documentDefinitionVersion"""
        }
        documentObjectenApiSyncRepository.deleteByDocumentDefinitionNameAndDocumentDefinitionVersion(
            documentDefinitionName,
            documentDefinitionVersion
        )
    }
}