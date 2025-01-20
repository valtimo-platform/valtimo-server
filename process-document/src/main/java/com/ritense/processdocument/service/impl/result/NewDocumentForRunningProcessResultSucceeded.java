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

package com.ritense.processdocument.service.impl.result;

import static com.ritense.valtimo.contract.utils.AssertionConcern.assertArgumentNotNull;

import com.ritense.document.domain.Document;
import com.ritense.processdocument.domain.ProcessInstanceId;
import com.ritense.processdocument.domain.impl.OperatonProcessInstanceId;
import com.ritense.processdocument.service.result.NewDocumentForRunningProcessResult;
import com.ritense.valtimo.contract.result.OperationError;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class NewDocumentForRunningProcessResultSucceeded implements NewDocumentForRunningProcessResult {

    private final Document document;
    private final OperatonProcessInstanceId processInstanceId;

    public NewDocumentForRunningProcessResultSucceeded(Document document, OperatonProcessInstanceId processInstanceId) {
        assertArgumentNotNull(document, "document is required");
        assertArgumentNotNull(processInstanceId, "processInstanceId is required");
        this.document = document;
        this.processInstanceId = processInstanceId;
    }

    @Override
    public Optional<Document> resultingDocument() {
        return Optional.of(document);
    }

    @Override
    public ProcessInstanceId processInstanceId() {
        return processInstanceId;
    }

    @Override
    public List<OperationError> errors() {
        return Collections.emptyList();
    }

}