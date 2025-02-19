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

package com.ritense.valtimo.security.config;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

import com.ritense.valtimo.contract.security.config.HttpConfigurerConfigurationException;
import com.ritense.valtimo.contract.security.config.HttpSecurityConfigurer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

public class TaskHttpSecurityConfigurer implements HttpSecurityConfigurer {


    @Override
    public void configure(HttpSecurity http) {
        try {
            http.authorizeHttpRequests(requests ->
                requests.requestMatchers(antMatcher(GET, "/api/v1/task")).authenticated()
                .requestMatchers(antMatcher(GET, "/api/v2/task")).authenticated()
                .requestMatchers(antMatcher(POST, "/api/v1/task/assign/batch-assign")).authenticated()
                .requestMatchers(antMatcher(POST, "/api/v1/task/batch-complete")).authenticated()
                .requestMatchers(antMatcher(GET, "/api/v1/task/{taskId}")).authenticated()
                .requestMatchers(antMatcher(POST, "/api/v1/task/{taskId}/assign")).authenticated()
                .requestMatchers(antMatcher(GET, "/api/v1/task/{taskId}/comments")).authenticated()
                .requestMatchers(antMatcher(POST, "/api/v1/task/{taskId}/complete")).authenticated()
                .requestMatchers(antMatcher(POST, "/api/v1/task/{taskId}/unassign")).authenticated()
                .requestMatchers(antMatcher(GET, "/api/v1/task/{taskId}/candidate-user")).authenticated()
                .requestMatchers(antMatcher(GET, "/api/v2/task/{taskId}/candidate-user")).authenticated()
            );
        } catch (Exception e) {
            throw new HttpConfigurerConfigurationException(e);
        }
    }

}