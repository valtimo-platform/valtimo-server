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

package com.ritense.processdocument.domain.impl;

import static com.ritense.valtimo.contract.utils.AssertionConcern.assertArgumentNotNull;

import com.ritense.document.domain.impl.JsonSchemaDocumentDefinitionId;
import com.ritense.processdocument.domain.ProcessDocumentDefinitionId;
import com.ritense.valtimo.contract.domain.AbstractId;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import java.util.Objects;

@Embeddable
public class OperatonProcessJsonSchemaDocumentDefinitionId extends AbstractId<OperatonProcessJsonSchemaDocumentDefinitionId>
    implements ProcessDocumentDefinitionId {

    @Embedded
    private OperatonProcessDefinitionKey processDefinitionKey;

    @Embedded
    private JsonSchemaDocumentDefinitionId documentDefinitionId;

    private OperatonProcessJsonSchemaDocumentDefinitionId(
        final OperatonProcessDefinitionKey processDefinitionKey,
        final JsonSchemaDocumentDefinitionId documentDefinitionId
    ) {
        assertArgumentNotNull(processDefinitionKey, "processDefinitionKey is required");
        assertArgumentNotNull(documentDefinitionId, "documentDefinitionId is required");
        this.processDefinitionKey = processDefinitionKey;
        this.documentDefinitionId = documentDefinitionId;
    }

    private OperatonProcessJsonSchemaDocumentDefinitionId() {
    }

    public static OperatonProcessJsonSchemaDocumentDefinitionId existingId(
        OperatonProcessDefinitionKey processDefinitionKey,
        JsonSchemaDocumentDefinitionId documentDefinitionId
    ) {
        return new OperatonProcessJsonSchemaDocumentDefinitionId(processDefinitionKey, documentDefinitionId);
    }

    public static OperatonProcessJsonSchemaDocumentDefinitionId newId(
        OperatonProcessDefinitionKey processDefinitionKey,
        JsonSchemaDocumentDefinitionId documentDefinitionId
    ) {
        return new OperatonProcessJsonSchemaDocumentDefinitionId(processDefinitionKey, documentDefinitionId).newIdentity();
    }

    @Override
    public OperatonProcessDefinitionKey processDefinitionKey() {
        return processDefinitionKey;
    }

    @Override
    public JsonSchemaDocumentDefinitionId documentDefinitionId() {
        return documentDefinitionId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof OperatonProcessJsonSchemaDocumentDefinitionId that)) {
            return false;
        }
        return processDefinitionKey.equals(that.processDefinitionKey)
            && documentDefinitionId.equals(that.documentDefinitionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(processDefinitionKey, documentDefinitionId);
    }

}