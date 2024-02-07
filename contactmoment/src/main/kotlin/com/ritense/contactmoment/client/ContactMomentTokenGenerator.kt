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

package com.ritense.contactmoment.client

import com.ritense.valtimo.contract.utils.SecurityUtils
import io.jsonwebtoken.JwtBuilder
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import java.nio.charset.Charset
import java.util.Date

class ContactMomentTokenGenerator {

    fun generateToken(secretKey: String, clientId: String): String {
        if (secretKey.length < 32) {
            throw IllegalStateException("SecretKey needs to be at least 32 in length")
        }
        val signingKey = Keys.hmacShaKeyFor(secretKey.toByteArray(Charset.forName("UTF-8")))

        val jwtBuilder = Jwts.builder()
        jwtBuilder
            .setIssuer(clientId)
            .setIssuedAt(Date())
            .claim("client_id", clientId)

        appendUserInfo(jwtBuilder)
        return jwtBuilder
            .signWith(signingKey, SignatureAlgorithm.HS256)
            .compact()
    }

    private fun appendUserInfo(jwtBuilder: JwtBuilder): JwtBuilder {
        val userLogin = SecurityUtils.getCurrentUserLogin()
        val userId = userLogin ?: "Valtimo"
        return jwtBuilder
            .claim("user_id", userId)
            .claim("user_representation", userId)
    }
}