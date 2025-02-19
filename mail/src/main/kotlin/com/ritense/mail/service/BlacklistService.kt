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

package com.ritense.mail.service

import com.ritense.mail.domain.blacklist.BlacklistedEmail
import com.ritense.mail.repository.BlacklistRepository
import com.ritense.valtimo.contract.annotation.SkipComponentScan
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
@SkipComponentScan
class BlacklistService(
    private val blacklistRepository: BlacklistRepository
) {

    fun blacklist(emailAddress: String, blacklistedOn: LocalDateTime, cause: String) {
        if (!isBlacklisted(emailAddress)) {
            blacklistRepository.save(BlacklistedEmail(emailAddress, blacklistedOn, cause))
        }
    }

    fun isBlacklisted(emailAddress: String): Boolean {
        return blacklistRepository.existsById(emailAddress)
    }

}