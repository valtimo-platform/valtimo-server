/*
 * Copyright 2015-2024 Ritense BV, the Netherlands.
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
 *
 */

package com.ritense.valtimo.autoconfiguration

import com.ritense.authorization.AuthorizationService
import com.ritense.valtimo.ValtimoApplicationPropertyService
import com.ritense.valtimo.operaton.authorization.OperatonExecutionProcessDefinitionMapper
import com.ritense.valtimo.operaton.authorization.OperatonExecutionSpecificationFactory
import com.ritense.valtimo.operaton.authorization.OperatonIdentityLinkSpecificationFactory
import com.ritense.valtimo.operaton.authorization.OperatonProcessDefinitionSpecificationFactory
import com.ritense.valtimo.operaton.authorization.OperatonTaskSpecificationFactory
import com.ritense.valtimo.operaton.repository.OperatonBytearrayRepository
import com.ritense.valtimo.operaton.repository.OperatonExecutionRepository
import com.ritense.valtimo.operaton.repository.OperatonHistoricProcessInstanceRepository
import com.ritense.valtimo.operaton.repository.OperatonHistoricTaskInstanceRepository
import com.ritense.valtimo.operaton.repository.OperatonHistoricVariableInstanceRepository
import com.ritense.valtimo.operaton.repository.OperatonIdentityLinkRepository
import com.ritense.valtimo.operaton.repository.OperatonProcessDefinitionRepository
import com.ritense.valtimo.operaton.repository.OperatonTaskIdentityLinkMapper
import com.ritense.valtimo.operaton.repository.OperatonTaskRepository
import com.ritense.valtimo.operaton.repository.OperatonVariableInstanceRepository
import com.ritense.valtimo.operaton.service.OperatonContextService
import com.ritense.valtimo.operaton.service.OperatonHistoryService
import com.ritense.valtimo.operaton.service.OperatonRepositoryService
import com.ritense.valtimo.operaton.service.OperatonRuntimeService
import com.ritense.valtimo.contract.config.ValtimoProperties
import com.ritense.valtimo.contract.database.QueryDialectHelper
import com.ritense.valtimo.repository.ValtimoApplicationPropertyRepository
import com.ritense.valtimo.service.OperatonTaskService
import org.operaton.bpm.engine.HistoryService
import org.operaton.bpm.engine.RepositoryService
import org.operaton.bpm.engine.RuntimeService
import org.operaton.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Lazy
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@AutoConfiguration
@EnableJpaRepositories(
    basePackageClasses = [
        OperatonBytearrayRepository::class,
        OperatonExecutionRepository::class,
        OperatonHistoricProcessInstanceRepository::class,
        OperatonHistoricTaskInstanceRepository::class,
        OperatonHistoricVariableInstanceRepository::class,
        OperatonIdentityLinkRepository::class,
        OperatonProcessDefinitionRepository::class,
        OperatonTaskRepository::class,
        OperatonVariableInstanceRepository::class
    ]
)
@EntityScan(
    basePackages = [
        "com.ritense.valtimo.operaton.domain",
        "com.ritense.valtimo.domain"
    ]
)
class ValtimoOperatonAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(OperatonContextService::class)
    fun operatonContextService(
        processEngineConfiguration: ProcessEngineConfigurationImpl,
    ): OperatonContextService {
        return OperatonContextService(processEngineConfiguration)
    }

    @Bean
    @ConditionalOnMissingBean(OperatonHistoryService::class)
    fun operatonHistoryService(
        historyService: HistoryService,
        operatonHistoricProcessInstanceRepository: OperatonHistoricProcessInstanceRepository,
        authorizationService: AuthorizationService
    ): OperatonHistoryService {
        return OperatonHistoryService(historyService, operatonHistoricProcessInstanceRepository, authorizationService)
    }

    @Bean
    @ConditionalOnMissingBean(OperatonRepositoryService::class)
    fun operatonRepositoryService(
        operatonProcessDefinitionRepository: OperatonProcessDefinitionRepository,
        authorizationService: AuthorizationService,
        repositoryService: RepositoryService,
    ): OperatonRepositoryService {
        return OperatonRepositoryService(
            operatonProcessDefinitionRepository,
            authorizationService,
            repositoryService
        )
    }

    @Bean
    @ConditionalOnMissingBean(OperatonRuntimeService::class)
    fun operatonRuntimeService(
        runtimeService: RuntimeService,
        operatonVariableInstanceRepository: OperatonVariableInstanceRepository,
        operatonIdentityLinkRepository: OperatonIdentityLinkRepository,
        authorizationService: AuthorizationService
    ): OperatonRuntimeService {
        return OperatonRuntimeService(
            runtimeService,
            operatonVariableInstanceRepository,
            operatonIdentityLinkRepository,
            authorizationService
        )
    }

    @Bean
    @ConditionalOnMissingBean(OperatonTaskSpecificationFactory::class)
    @ConditionalOnBean(AuthorizationService::class)
    fun operatonTaskSpecificationFactory(
        @Lazy operatonTaskService: OperatonTaskService,
        queryDialectHelper: QueryDialectHelper
    ): OperatonTaskSpecificationFactory {
        return OperatonTaskSpecificationFactory(operatonTaskService, queryDialectHelper)
    }

    @Bean
    @ConditionalOnMissingBean(OperatonIdentityLinkSpecificationFactory::class)
    @ConditionalOnBean(AuthorizationService::class)
    fun operatonIdentityLinkSpecificationFactory(
        @Lazy operatonRuntimeService: OperatonRuntimeService,
        queryDialectHelper: QueryDialectHelper
    ): OperatonIdentityLinkSpecificationFactory {
        return OperatonIdentityLinkSpecificationFactory(operatonRuntimeService, queryDialectHelper)
    }

    @Bean
    @ConditionalOnMissingBean(OperatonExecutionSpecificationFactory::class)
    @ConditionalOnBean(AuthorizationService::class)
    fun operatonExecutionSpecificationFactory(
        repository: OperatonExecutionRepository,
        queryDialectHelper: QueryDialectHelper
    ): OperatonExecutionSpecificationFactory {
        return OperatonExecutionSpecificationFactory(repository, queryDialectHelper)
    }

    @Bean
    @ConditionalOnMissingBean(OperatonProcessDefinitionSpecificationFactory::class)
    @ConditionalOnBean(AuthorizationService::class)
    fun operatonProcessDefinitionSpecificationFactory(
        repository: OperatonProcessDefinitionRepository,
        queryDialectHelper: QueryDialectHelper
    ): OperatonProcessDefinitionSpecificationFactory {
        return OperatonProcessDefinitionSpecificationFactory(repository, queryDialectHelper)
    }

    @Bean
    @ConditionalOnMissingBean(OperatonExecutionProcessDefinitionMapper::class)
    @ConditionalOnBean(AuthorizationService::class)
    fun operatonExecutionProcessDefinitionMapper() = OperatonExecutionProcessDefinitionMapper()


    @Bean
    @ConditionalOnMissingBean(OperatonTaskIdentityLinkMapper::class)
    fun operatonTaskIdentityLinkMapper(): OperatonTaskIdentityLinkMapper {
        return OperatonTaskIdentityLinkMapper()
    }

    @Bean
    fun valtimoApplicationPropertyService(
        repository: ValtimoApplicationPropertyRepository,
        valtimoProperties: ValtimoProperties
    ): ValtimoApplicationPropertyService = ValtimoApplicationPropertyService(repository, valtimoProperties)

}