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

package com.ritense.valtimo.formflow.autoconfigure

import com.ritense.form.service.FormLoaderService
import com.ritense.formflow.service.FormFlowService
import com.ritense.formlink.service.FormAssociationService
import com.ritense.valtimo.formflow.ValtimoFormFlowHttpSecurityConfigurer
import com.ritense.valtimo.formflow.interceptor.FormFlowCreateTaskCommandInterceptor
import com.ritense.valtimo.formflow.interceptor.FormFlowProcessPlugin
import com.ritense.valtimo.formflow.web.rest.FormFlowDemoResource
import com.ritense.valtimo.formflow.web.rest.ProcessLinkFormFlowDefinitionResource
import org.camunda.bpm.engine.RuntimeService
import org.camunda.bpm.engine.TaskService
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order

@Configuration
class FormFlowValtimoAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(FormFlowProcessPlugin::class)
    fun formFlowProcessPlugin(
        formFlowCreateTaskCommandInterceptor: FormFlowCreateTaskCommandInterceptor
    ): FormFlowProcessPlugin {
        return FormFlowProcessPlugin(formFlowCreateTaskCommandInterceptor)
    }

    @Bean
    @ConditionalOnMissingBean(ProcessLinkFormFlowDefinitionResource::class)
    fun processLinkFormFlowDefinitionResource(formFlowService: FormFlowService): ProcessLinkFormFlowDefinitionResource {
        return ProcessLinkFormFlowDefinitionResource(formFlowService)
    }

    @Bean
    @ConditionalOnMissingBean(FormFlowDemoResource::class)
    fun formFlowDemoResource(formFlowService: FormFlowService,
        formLoaderService: FormLoaderService): FormFlowDemoResource {
        return FormFlowDemoResource(formFlowService, formLoaderService)
    }

    @Bean
    @Order(270)
    @ConditionalOnMissingBean(ValtimoFormFlowHttpSecurityConfigurer::class)
    fun valtimoFormFlowHttpSecurityConfigurer(): ValtimoFormFlowHttpSecurityConfigurer {
        return ValtimoFormFlowHttpSecurityConfigurer()
    }
}