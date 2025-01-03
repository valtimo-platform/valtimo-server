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

package com.ritense.openzaak.service

import org.operaton.bpm.engine.delegate.DelegateExecution

@Deprecated("Since 12.0.0. Replace with the Zaken API plugin")
interface ZaakProcessService {
    @Deprecated("Since 12.0.0.", ReplaceWith("com.ritense.zakenapi.ZakenApiClient.setZaakStatus"))
    fun setStatus(execution: DelegateExecution, status: String)
}