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

package com.ritense.valtimo.contract.mail;

import com.ritense.valtimo.contract.mail.model.MailMessageStatus;
import com.ritense.valtimo.contract.mail.model.RawMailMessage;
import com.ritense.valtimo.contract.mail.model.TemplatedMailMessage;
import java.util.List;
import java.util.Optional;

public interface MailSender {
    Optional<List<MailMessageStatus>> send(RawMailMessage rawMailMessage);

    Optional<List<MailMessageStatus>> send(TemplatedMailMessage templatedMailMessage);

    int getMaximumSizeAttachments();
}
