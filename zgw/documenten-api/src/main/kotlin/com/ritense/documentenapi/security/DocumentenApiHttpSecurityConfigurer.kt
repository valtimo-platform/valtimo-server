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

package com.ritense.documentenapi.security

import com.ritense.valtimo.contract.authentication.AuthoritiesConstants.ADMIN
import com.ritense.valtimo.contract.security.config.HttpConfigurerConfigurationException
import com.ritense.valtimo.contract.security.config.HttpSecurityConfigurer
import org.springframework.http.HttpMethod.DELETE
import org.springframework.http.HttpMethod.GET
import org.springframework.http.HttpMethod.POST
import org.springframework.http.HttpMethod.PUT
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher

class DocumentenApiHttpSecurityConfigurer : HttpSecurityConfigurer {

    override fun configure(http: HttpSecurity) {
        try {
            http.authorizeHttpRequests { requests ->
                requests
                    .requestMatchers(antMatcher(GET, "/api/v1/documenten-api/{pluginConfigurationId}/files/{documentId}/download")).authenticated()
                    .requestMatchers(antMatcher(PUT, "/api/v1/documenten-api/{pluginConfigurationId}/files/{documentId}")).authenticated()
                    .requestMatchers(antMatcher(DELETE, "/api/v1/documenten-api/{pluginConfigurationId}/files/{documentId}")).authenticated()
                    .requestMatchers(antMatcher(GET, "/api/v1/case-definition/{caseDefinitionName}/zgw-document-column")).authenticated()
                    .requestMatchers(antMatcher(GET, "/api/v1/case-definition/{caseDefinitionName}/documenten-api/version")).authenticated()
                    .requestMatchers(antMatcher(GET, "/api/v1/case-definition/{caseDefinitionName}/zgw-document/trefwoord")).authenticated()
                    .requestMatchers(antMatcher(GET, "/api/v1/document/{documentId}/zgw-document/upload-field")).authenticated()

                    .requestMatchers(antMatcher(GET, "/api/management/v1/case-definition/{caseDefinitionName}/zgw-document/trefwoord")).hasAuthority(ADMIN)
                    .requestMatchers(antMatcher(POST, "/api/management/v1/case-definition/{caseDefinitionName}/zgw-document/trefwoord/{trefwoord}")).hasAuthority(ADMIN)
                    .requestMatchers(antMatcher(DELETE, "/api/management/v1/case-definition/{caseDefinitionName}/zgw-document/trefwoord/{trefwoord}")).hasAuthority(ADMIN)
                    .requestMatchers(antMatcher(DELETE, "/api/management/v1/case-definition/{caseDefinitionName}/zgw-document/trefwoord")).hasAuthority(ADMIN)

                    .requestMatchers(antMatcher(GET, "/api/management/v1/case-definition/{caseDefinitionName}/zgw-document-column-key")).hasAuthority(ADMIN)
                    .requestMatchers(antMatcher(GET, "/api/management/v1/case-definition/{caseDefinitionName}/zgw-document-column")).hasAuthority(ADMIN)
                    .requestMatchers(antMatcher(PUT, "/api/management/v1/case-definition/{caseDefinitionName}/zgw-document-column")).hasAuthority(ADMIN)
                    .requestMatchers(antMatcher(PUT, "/api/management/v1/case-definition/{caseDefinitionName}/zgw-document-column/{key}")).hasAuthority(ADMIN)
                    .requestMatchers(antMatcher(DELETE, "/api/management/v1/case-definition/{caseDefinitionName}/zgw-document-column/{key}")).hasAuthority(ADMIN)

                    .requestMatchers(antMatcher(GET, "/api/management/v1/case-definition/{caseDefinitionName}/documenten-api/version")).hasAuthority(ADMIN)
                    .requestMatchers(antMatcher(GET, "/api/management/v1/documenten-api/versions")).hasAuthority(ADMIN)

                    .requestMatchers(antMatcher(GET, "/api/management/v1/case-definition/{caseDefinitionName}/zgw-document/upload-field")).hasAuthority(ADMIN)
                    .requestMatchers(antMatcher(PUT, "/api/management/v1/case-definition/{caseDefinitionName}/zgw-document/upload-field")).hasAuthority(ADMIN)
            }
        } catch (e: Exception) {
            throw HttpConfigurerConfigurationException(e)
        }
    }
}
