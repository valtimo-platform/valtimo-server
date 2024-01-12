/*
 * Copyright 2015-2023 Ritense BV, the Netherlands.
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

package com.ritense.document.domain.impl.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.ritense.valtimo.contract.resource.Resource;
import jakarta.validation.constraints.NotNull;
import java.util.Collections;
import java.util.Set;

public class NewDocumentRequest {

    @JsonProperty("definition")
    @NotNull
    private final String documentDefinitionName;

    @JsonProperty
    @NotNull
    private final JsonNode content;

    private DocumentRelationRequest documentRelation;

    private Set<Resource> resources = Collections.emptySet();

    @JsonCreator
    public NewDocumentRequest(
        @JsonProperty(value = "definition", required = true) String documentDefinitionName,
        @JsonProperty(value = "content", required = true) JsonNode content
    ) {
        this.documentDefinitionName = documentDefinitionName;
        this.content = content;
    }

    public String documentDefinitionName() {
        return documentDefinitionName;
    }

    public JsonNode content() {
        return content;
    }

    public NewDocumentRequest withDocumentRelation(DocumentRelationRequest documentRelation) {
        this.documentRelation = documentRelation;
        return this;
    }

    public NewDocumentRequest withResources(Set<Resource> resources) {
        this.resources = resources;
        return this;
    }

    public DocumentRelationRequest documentRelation() {
        return documentRelation;
    }

    public Set<Resource> getResources() {
        return this.resources;
    }
}