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

package com.ritense.document.service.impl;

import java.util.List;
import java.util.Objects;

public class SearchRequest {
    private String documentDefinitionName;
    private String createdBy;
    private String globalSearchFilter;
    private Long sequence;
    private String assigneeId;
    private List<SearchCriteria> otherFilters;

    public SearchRequest() {
        //Default constructor
    }

    public String getDocumentDefinitionName() {
        return this.documentDefinitionName;
    }

    public String getCreatedBy() {
        return this.createdBy;
    }

    public String getGlobalSearchFilter() {
        return this.globalSearchFilter;
    }

    public Long getSequence() {
        return this.sequence;
    }

    public String getAssigneeId() {
        return assigneeId;
    }

    public void setAssigneeId(String assigneeId) {
        this.assigneeId = assigneeId;
    }

    public List<SearchCriteria> getOtherFilters() {
        return this.otherFilters;
    }

    public void setDocumentDefinitionName(String documentDefinitionName) {
        this.documentDefinitionName = documentDefinitionName;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public void setGlobalSearchFilter(String globalSearchFilter) {
        this.globalSearchFilter = globalSearchFilter;
    }

    public void setSequence(Long sequence) {
        this.sequence = sequence;
    }

    public void setOtherFilters(List<SearchCriteria> otherFilters) {
        this.otherFilters = otherFilters;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SearchRequest that = (SearchRequest) o;
        return Objects.equals(getDocumentDefinitionName(), that.getDocumentDefinitionName())
            && Objects.equals(getCreatedBy(), that.getCreatedBy())
            && Objects.equals(getGlobalSearchFilter(), that.getGlobalSearchFilter())
            && Objects.equals(getSequence(), that.getSequence())
            && Objects.equals(getAssigneeId(), that.getAssigneeId())
            && Objects.equals(getOtherFilters(), that.getOtherFilters());
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            getDocumentDefinitionName(),
            getCreatedBy(),
            getGlobalSearchFilter(),
            getSequence(),
            getAssigneeId(),
            getOtherFilters()
        );
    }

    @Override
    public String toString() {
        return "SearchRequest{" +
            "documentDefinitionName='" + documentDefinitionName + '\'' +
            ", createdBy='" + createdBy + '\'' +
            ", globalSearchFilter='" + globalSearchFilter + '\'' +
            ", sequence=" + sequence +
            ", assigneeId=" + assigneeId +
            ", otherFilters=" + otherFilters +
            '}';
    }
}
