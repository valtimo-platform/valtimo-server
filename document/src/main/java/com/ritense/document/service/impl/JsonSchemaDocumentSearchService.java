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

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

import com.ritense.document.domain.impl.JsonSchemaDocument;
import com.ritense.document.domain.impl.JsonSchemaDocumentDefinitionRole;
import com.ritense.document.service.DocumentSearchService;
import com.ritense.valtimo.contract.database.QueryDialectHelper;
import com.ritense.valtimo.contract.utils.SecurityUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;
import javax.transaction.Transactional;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.query.criteria.internal.OrderImpl;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@Transactional
public class JsonSchemaDocumentSearchService implements DocumentSearchService {

    private final EntityManager entityManager;

    private final QueryDialectHelper queryDialectHelper;

    public JsonSchemaDocumentSearchService(EntityManager entityManager, QueryDialectHelper queryDialectHelper) {
        this.entityManager = entityManager;
        this.queryDialectHelper = queryDialectHelper;
    }

    @Override
    public Page<JsonSchemaDocument> search(
        final SearchRequest searchRequest,
        final Pageable pageable
    ) {
        return search(searchRequest, pageable, true);
    }

    @Override
    public Page<JsonSchemaDocument> searchWithoutAuthorization(SearchRequest searchRequest, Pageable pageable) {
        return search(searchRequest, pageable, false);
    }

    private Page<JsonSchemaDocument> search(SearchRequest searchRequest, Pageable pageable, boolean withAuthorization) {
        final CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        final CriteriaQuery<JsonSchemaDocument> query = cb.createQuery(JsonSchemaDocument.class);
        final Root<JsonSchemaDocument> selectRoot = query.from(JsonSchemaDocument.class);

        query.select(selectRoot);
        query.where(createPredicates(searchRequest, cb, query, selectRoot, withAuthorization));
        query.orderBy(getOrderBy(cb, selectRoot, pageable.getSort()));

        final TypedQuery<JsonSchemaDocument> typedQuery = entityManager
            .createQuery(query)
            .setFirstResult((int) pageable.getOffset())
            .setMaxResults(pageable.getPageSize());

        final CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<JsonSchemaDocument> countRoot = countQuery.from(JsonSchemaDocument.class);
        countQuery.select(cb.count(countRoot));
        countQuery.where(createPredicates(searchRequest, cb, countQuery, countRoot, withAuthorization));

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

}