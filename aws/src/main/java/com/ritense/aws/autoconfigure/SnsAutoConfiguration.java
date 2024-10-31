/*
 * Copyright 2015-2020 Ritense BV, the Netherlands.
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

package com.ritense.aws.autoconfigure;

import com.ritense.aws.config.AwsProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProviderChain;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsAsyncClient;

@Configuration
@EnableConfigurationProperties(AwsProperties.class)
@ConditionalOnProperty(value = {"aws.sns.topic"})
public class SnsAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public SnsAsyncClient snsAsyncClient(
        AwsProperties awsProperties,
        AwsCredentialsProviderChain valtimoAwsCredentialsProviderChain
    ) {
        return SnsAsyncClient.builder()
            .credentialsProvider(valtimoAwsCredentialsProviderChain)
            .region(Region.of(awsProperties.getRegion()))
            .build();
    }

}
