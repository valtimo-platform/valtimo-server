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
import com.ritense.processdocument.service.result.ModifyDocumentAndCompleteTaskResult;
import com.ritense.valtimo.contract.result.OperationError;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class ModifyDocumentAndCompleteTaskResultSucceeded implements ModifyDocumentAndCompleteTaskResult {

    private final Document document;

    public ModifyDocumentAndCompleteTaskResultSucceeded(Document document) {
        assertArgumentNotNull(document, "document is required");
        this.document = document;
    }

    @Override
    public Optional<Document> resultingDocument() {
        return Optional.of(document);
    }

    @Override
    public List<OperationError> errors() {
        return Collections.emptyList();
    }

}