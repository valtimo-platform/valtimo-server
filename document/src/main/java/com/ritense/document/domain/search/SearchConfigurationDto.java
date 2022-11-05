/*
 * Copyright 2015-2022 Ritense BV, the Netherlands.
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

package com.ritense.document.domain.search;

import com.ritense.document.domain.impl.searchfield.SearchField;
import com.ritense.document.domain.impl.searchfield.SearchFieldDatatype;
import com.ritense.document.domain.impl.searchfield.SearchFieldFieldtype;
import com.ritense.document.domain.impl.searchfield.SearchFieldId;
import com.ritense.document.domain.impl.searchfield.SearchFieldMatchtype;

import java.util.List;
import java.util.stream.Collectors;

public class SearchConfigurationDto {

    private List<SearchConfigurationFieldJson> searchFields;

    public SearchConfigurationDto() {
        // Empty constructor needed for Jackson
    }

    public static class SearchConfigurationFieldJson {
        private String key;
        private String path;
        private SearchFieldDatatype dataType;
        private SearchFieldFieldtype fieldType;
        private SearchFieldMatchtype matchType;

        public SearchConfigurationFieldJson() {
            // Empty constructor needed for Jackson
        }

        public SearchField toEntity(String documentDefinitionName) {
            var searchField = new SearchField(key, path, dataType, fieldType, matchType);
            searchField.setId(SearchFieldId.newId(documentDefinitionName));
            return searchField;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public SearchFieldDatatype getDataType() {
            return dataType;
        }

        public void setDataType(SearchFieldDatatype dataType) {
            this.dataType = dataType;
        }

        public SearchFieldFieldtype getFieldType() {
            return fieldType;
        }

        public void setFieldType(SearchFieldFieldtype fieldType) {
            this.fieldType = fieldType;
        }

        public SearchFieldMatchtype getMatchType() {
            return matchType;
        }

        public void setMatchType(SearchFieldMatchtype matchType) {
            this.matchType = matchType;
        }
    }

    public List<SearchField> toEntity(String documentDefinitionName) {
        return searchFields.stream()
            .map(searchField -> searchField.toEntity(documentDefinitionName))
            .collect(Collectors.toList());
    }

    public List<SearchConfigurationFieldJson> getSearchFields() {
        return searchFields;
    }

    public void setSearchFields(List<SearchConfigurationFieldJson> searchFields) {
        this.searchFields = searchFields;
    }
}
