/*
 * Copyright 2015-2020 Ritense BV, the Netherlands.
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

import com.ritense.document.domain.impl.JsonSchemaDocument;
import com.ritense.document.domain.impl.JsonSchemaDocumentDefinitionRole;
import com.ritense.document.domain.impl.searchfield.SearchField;
import com.ritense.document.domain.search.SearchOperator;
import com.ritense.document.domain.search.SearchRequest2;
import com.ritense.document.domain.search.SearchRequestMapper;
import com.ritense.document.domain.search.SearchWithConfigRequest;
import com.ritense.document.service.DocumentSearchService;
import com.ritense.document.service.SearchFieldService;
import com.ritense.valtimo.contract.database.QueryDialectHelper;
import com.ritense.valtimo.contract.utils.SecurityUtils;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.query.criteria.internal.OrderImpl;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

@Transactional
public class JsonSchemaDocumentSearchService implements DocumentSearchService {

    private final EntityManager entityManager;

    private final QueryDialectHelper queryDialectHelper;

    private final SearchFieldService searchFieldService;

    public JsonSchemaDocumentSearchService(EntityManager entityManager, QueryDialectHelper queryDialectHelper, SearchFieldService searchFieldService) {
        this.entityManager = entityManager;
        this.queryDialectHelper = queryDialectHelper;
        this.searchFieldService = searchFieldService;
    }

    @Override
    public Page<JsonSchemaDocument> search(
        final SearchRequest searchRequest,
        final Pageable pageable
    ) {
        return search((cb, query, documentRoot) -> createPredicates(searchRequest, cb, query, documentRoot, true), pageable);
    }

    @Override
    public Page<JsonSchemaDocument> search(String documentDefinitionName, SearchWithConfigRequest searchWithConfigRequest, Pageable pageable) {
        var searchFieldMap = searchFieldService.getSearchFields(documentDefinitionName).stream()
            .collect(toMap(SearchField::getKey, searchField -> searchField));

        var searchCriteria = searchWithConfigRequest.getOtherFilters().stream()
            .map(otherFilter -> SearchRequestMapper.toSearchCriteria2(otherFilter, searchFieldMap.get(otherFilter.getKey())))
            .collect(toList());

        var searchRequest2 = SearchRequestMapper.toSearchRequest2(searchWithConfigRequest, searchCriteria);

        return search(documentDefinitionName, searchRequest2, pageable);
    }

    @Override
    public Page<JsonSchemaDocument> search(String documentDefinitionName, SearchRequest2 searchRequest, Pageable pageable) {
        return search((cb, query, documentRoot) -> {
            final List<Predicate> predicates = new ArrayList<>();

            if (!StringUtils.isEmpty(documentDefinitionName)) {
                predicates.add(cb.equal(documentRoot.get("documentDefinitionId").get("name"), documentDefinitionName));
            }

            if (!StringUtils.isEmpty(searchRequest.getCreatedBy())) {
                predicates.add(cb.equal(documentRoot.get("createdBy"), searchRequest.getCreatedBy()));
            }

            if (searchRequest.getSequence() != null) {
                predicates.add(cb.equal(documentRoot.get("sequence"), searchRequest.getSequence()));
            }

            addJsonFieldPredicates(cb, documentRoot, searchRequest, predicates);
            return predicates.toArray(new Predicate[0]);
        }, pageable);
    }

    @Override
    public Page<JsonSchemaDocument> searchWithoutAuthorization(SearchRequest searchRequest, Pageable pageable) {
        return search((cb, query, documentRoot) -> createPredicates(searchRequest, cb, query, documentRoot, false), pageable);
    }

    private Page<JsonSchemaDocument> search(PredicateBuilder predicateBuilder, Pageable pageable) {
        final CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        final CriteriaQuery<JsonSchemaDocument> query = cb.createQuery(JsonSchemaDocument.class);
        final Root<JsonSchemaDocument> selectRoot = query.from(JsonSchemaDocument.class);

        query.select(selectRoot);
        query.where(predicateBuilder.apply(cb, query, selectRoot));
        query.orderBy(getOrderBy(cb, selectRoot, pageable.getSort()));

        final TypedQuery<JsonSchemaDocument> typedQuery = entityManager
            .createQuery(query)
            .setFirstResult((int) pageable.getOffset())
            .setMaxResults(pageable.getPageSize());

        final CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<JsonSchemaDocument> countRoot = countQuery.from(JsonSchemaDocument.class);
        countQuery.select(cb.count(countRoot));
        countQuery.where(predicateBuilder.apply(cb, countQuery, countRoot));

        return new PageImpl<>(typedQuery.getResultList(), pageable, entityManager.createQuery(countQuery).getSingleResult());
    }

    @NotNull
    private Predicate[] createPredicates(SearchRequest searchRequest, CriteriaBuilder cb, CriteriaQuery<?> query, Root<JsonSchemaDocument> documentRoot, boolean withAuthorization) {
        final List<Predicate> predicates = new ArrayList<>();

        addNonJsonFieldPredicates(cb, documentRoot, searchRequest, predicates);
        addJsonFieldPredicates(cb, documentRoot, searchRequest, predicates);
        if (withAuthorization) {
            addUserRolePredicate(cb, query, documentRoot, predicates);
        }


        return predicates.toArray(Predicate[]::new);
    }

    private void addNonJsonFieldPredicates(CriteriaBuilder cb, Root<JsonSchemaDocument> root,
                                           SearchRequest searchRequest, List<Predicate> predicates) {

        if (!StringUtils.isEmpty(searchRequest.getDocumentDefinitionName())) {
            predicates.add(cb.equal(root.get("documentDefinitionId").get("name"),
                searchRequest.getDocumentDefinitionName()));
        }

        if (!StringUtils.isEmpty(searchRequest.getCreatedBy())) {
            predicates.add(cb.equal(root.get("createdBy"), searchRequest.getCreatedBy()));
        }

        if (searchRequest.getSequence() != null) {
            predicates.add(cb.equal(root.get("sequence"), searchRequest.getSequence()));
        }

        if (!StringUtils.isEmpty(searchRequest.getGlobalSearchFilter())) {
            predicates.add(findJsonValue(cb, root, searchRequest.getGlobalSearchFilter()));
        }
    }

    private void addJsonFieldPredicates(CriteriaBuilder cb, Root<JsonSchemaDocument> root,
                                        SearchRequest searchRequest, List<Predicate> predicates) {

        if (searchRequest.getOtherFilters() != null && !searchRequest.getOtherFilters().isEmpty()) {
            Map<String, List<SearchCriteria>> criteriaPerPath = searchRequest.getOtherFilters()
                .stream()
                .collect(groupingBy(SearchCriteria::getPath));

            List<Predicate> criteriaPredicates = criteriaPerPath.entrySet()
                .stream()
                .flatMap(pathEntry -> {
                    if (pathEntry.getValue().size() == 1) {
                        return Stream.of(
                            findJsonPathValue(cb, root, pathEntry.getKey(), pathEntry.getValue().get(0).getValue()));
                    } else {
                        return Stream.of(cb.or(
                            pathEntry.getValue().stream()
                                .map(currentCriteria -> findJsonPathValue(cb, root, currentCriteria.getPath(),
                                    currentCriteria.getValue()))
                                .collect(toList())
                                .toArray(Predicate[]::new)
                        ));
                    }
                })
                .collect(toList());

            predicates.add(cb.and(criteriaPredicates.toArray(Predicate[]::new)));
        }
    }

    private void addJsonFieldPredicates(CriteriaBuilder cb, Root<JsonSchemaDocument> root,
                                        SearchRequest2 searchRequest, List<Predicate> predicates) {

        if (searchRequest.getOtherFilters() != null && !searchRequest.getOtherFilters().isEmpty()) {
            if (searchRequest.getOtherFilters().size() == 1) {
                predicates.add(findJsonValue(cb, root, searchRequest.getOtherFilters().get(0)));
            } else {
                var jsonPredicates = searchRequest.getOtherFilters().stream()
                    .map(currentCriteria -> findJsonValue(cb, root, currentCriteria))
                    .collect(toList())
                    .toArray(Predicate[]::new);

                if (searchRequest.getSearchOperator() == SearchOperator.AND) {
                    predicates.add(cb.and(jsonPredicates));
                } else {
                    predicates.add(cb.or(jsonPredicates));
                }
            }
        }
    }

    private void addUserRolePredicate(CriteriaBuilder cb,
                                      CriteriaQuery<?> query,
                                      Root<JsonSchemaDocument> documentRoot,
                                      List<Predicate> predicates) {
        List<String> roles = SecurityUtils.getCurrentUserRoles();

        Subquery<String> sub = query.subquery(String.class);
        Root<JsonSchemaDocumentDefinitionRole> subRoot = sub.from(JsonSchemaDocumentDefinitionRole.class);
        sub.select(subRoot.get("id").get("documentDefinitionName"));
        sub.where(subRoot.get("id").get("role").in(roles));

        predicates.add(
            documentRoot.get("documentDefinitionId").get("name").in(sub)
        );
    }

    private Predicate findJsonPathValue(CriteriaBuilder cb, Root<JsonSchemaDocument> root, String path, String value) {
        return queryDialectHelper.getJsonValueExistsInPathExpression(cb, root.get("content").get("content"), path, value);
    }

    private Predicate findJsonValue(CriteriaBuilder cb, Root<JsonSchemaDocument> root, String value) {
        return queryDialectHelper.getJsonValueExistsExpression(cb, root.get("content").get("content"), value);
    }

    private Predicate findJsonValue(CriteriaBuilder cb, Root<JsonSchemaDocument> root, SearchRequest2.SearchCriteria2 searchCriteria) {
        var dataType = searchCriteria.getDataType();
        var jsonValue = queryDialectHelper.getJsonValueExpression(cb, root.get("content").get("content"), searchCriteria.getPath(), dataType);

        if (String.class.isAssignableFrom(dataType)) {
            switch (searchCriteria.getSearchType()) {
                case LIKE:
                    return cb.like(cb.lower((Expression<String>) jsonValue), "%" + searchCriteria.getValues().get(0).toString().trim().toLowerCase() + "%");
                case EXACT:
                    return cb.equal(cb.lower((Expression<String>) jsonValue), searchCriteria.getValues().get(0).toString().trim().toLowerCase());
            }
        }

        switch (searchCriteria.getSearchType()) {
            case EXACT:
                return cb.equal(jsonValue, searchCriteria.getValues().get(0));
            case IN:
                return cb.literal(jsonValue).in(searchCriteria.getValues());
            case FROM:
                return cb.greaterThanOrEqualTo((Expression) jsonValue, (Comparable) searchCriteria.getRangeFrom());
            case TO:
                return cb.lessThanOrEqualTo((Expression) jsonValue, (Comparable) searchCriteria.getRangeTo());
            case BETWEEN:
                return cb.between(((Expression<String>) jsonValue), cb.literal((Comparable) searchCriteria.getRangeFrom()), cb.literal((Comparable) searchCriteria.getRangeTo()));
        }

        throw new NotImplementedException("Searching for data type '" + dataType + "' with search type '" + searchCriteria.getSearchType() + "' hasn't been implemented.");
    }

    private List<Order> getOrderBy(CriteriaBuilder cb, Root<JsonSchemaDocument> root, Sort sort) {
        return sort.stream()
            .map(order -> {
                if (order.getProperty().startsWith("$.")) {
                    return new OrderImpl(
                        queryDialectHelper.getJsonValueExpression(cb, root.get("content"), order.getProperty()),
                        order.getDirection().isAscending());
                } else {
                    return new OrderImpl(
                        root.get(order.getProperty()),
                        order.getDirection().isAscending());
                }
            })
            .collect(Collectors.toList());
    }

    @FunctionalInterface
    public interface PredicateBuilder {
        Predicate[] apply(
            CriteriaBuilder criteriaBuilder,
            CriteriaQuery<?> query,
            Root<JsonSchemaDocument> documentRoot
        );
    }
}
