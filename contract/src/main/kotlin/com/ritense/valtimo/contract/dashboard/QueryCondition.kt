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

package com.ritense.valtimo.contract.dashboard

import PermissionConditionKey
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.ritense.valtimo.contract.authorization.CurrentUserExpressionHandler
import com.ritense.valtimo.contract.repository.ExpressionOperator
import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.Expression
import jakarta.persistence.criteria.Predicate
import jakarta.persistence.criteria.Root
import org.springframework.expression.spel.standard.SpelExpressionParser
import org.springframework.expression.spel.support.StandardEvaluationContext
import java.time.LocalDateTime

data class QueryCondition<T : Comparable<T>>(
    val queryPath: String,
    val queryOperator: ExpressionOperator,
    @JsonDeserialize(using = ComparableDeserializer::class)
    val queryValue: T
) {
    private fun <T> queryValueIsNull(target: T): Boolean {
        return target == "\${null}"
    }

    private fun queryValueIsDateTimeSpelExpression(target: Any?): Boolean {
        return (target as? String)?.let {
            it.isNotEmpty() && it.startsWith("\${") && it.endsWith('}') && "localDateTimeNow" in it
        } ?: false
    }

    private fun getPredicateFromDateTimeSpelExpression(
        root: Root<*>,
        it: QueryCondition<String>,
        criteriaBuilder: CriteriaBuilder,
        pathExpressionFunction: (Class<T>, String, Root<*>, CriteriaBuilder) -> Expression<T>
    ): Predicate {
        val parser = SpelExpressionParser()
        val expressionWithoutPrefixSuffix = it.queryValue.substringAfter("\${").substringBefore("}")

        val spelEvaluationContext = WidgetDataSourceSpelEvaluationContext()
        val context = StandardEvaluationContext()

        context.setRootObject(spelEvaluationContext)

        val spelExpression: org.springframework.expression.Expression =
            parser.parseExpression(expressionWithoutPrefixSuffix)

        val valueClass = LocalDateTime::class.java

        val value = spelExpression.getValue(context, valueClass)

        val expression =
            pathExpressionFunction(valueClass as Class<T>, it.queryPath, root, criteriaBuilder)

        return it.queryOperator.toPredicate(
            criteriaBuilder,
            expression,
            value as T
        )
    }

    private fun <T> queryValueIsPermissionConditionValueExpression(target: T): Boolean {
        return (target as? String)?.let {
            it.isNotEmpty() &&
                PermissionConditionKey.isValidKey(it) &&
                // roles is a list and is currently not supported
                PermissionConditionKey.fromKey(it) != PermissionConditionKey.CURRENT_USER_ROLES
        } ?: false
    }

    private fun getPredicateFromPermissionConditionExpression(
        root: Root<*>,
        it: QueryCondition<String>,
        criteriaBuilder: CriteriaBuilder,
        pathExpressionFunction: (Class<T>, String, Root<*>, CriteriaBuilder) -> Expression<T>
    ): Predicate {
        val valueClass = String::class.java
        val expression = pathExpressionFunction(valueClass as Class<T>, it.queryPath, root, criteriaBuilder)
        val permissionConditionKey = PermissionConditionKey.fromKey(it.queryValue)?.key
        val resolvedValue = CurrentUserExpressionHandler.resolveValue(permissionConditionKey) as? String ?: ""

        return it.queryOperator.toPredicate(
            criteriaBuilder,
            expression,
            resolvedValue as T
        )
    }

    fun toPredicate(
        root: Root<*>,
        criteriaBuilder: CriteriaBuilder,
        pathExpressionFunction: (Class<T>, String, Root<*>, CriteriaBuilder) -> Expression<T>
    ): Predicate {
        if (queryValueIsDateTimeSpelExpression(queryValue)) {
            return getPredicateFromDateTimeSpelExpression(
                root,
                this as QueryCondition<String>,
                criteriaBuilder,
                pathExpressionFunction
            )
        }

        if (queryValueIsPermissionConditionValueExpression(queryValue)) {
            return getPredicateFromPermissionConditionExpression(
                root,
                this as QueryCondition<String>,
                criteriaBuilder,
                pathExpressionFunction
            )
        }

        val predicateQueryValue: T? = if (queryValueIsNull(queryValue)) {
            null
        } else {
            queryValue
        }

        val valueClass = queryValue::class.java as Class<T>

        val expression = pathExpressionFunction(valueClass, queryPath, root, criteriaBuilder)

        return queryOperator.toPredicate(
            criteriaBuilder,
            expression,
            predicateQueryValue
        )
    }
}