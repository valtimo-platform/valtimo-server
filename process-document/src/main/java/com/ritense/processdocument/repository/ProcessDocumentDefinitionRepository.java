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

package com.ritense.processdocument.repository;

import com.ritense.processdocument.domain.ProcessDefinitionKey;
import com.ritense.processdocument.domain.ProcessDocumentDefinitionId;
import com.ritense.processdocument.domain.impl.OperatonProcessJsonSchemaDocumentDefinition;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;

@Repository
public interface ProcessDocumentDefinitionRepository extends
    JpaRepository<OperatonProcessJsonSchemaDocumentDefinition, ProcessDocumentDefinitionId> {

    @Query(
        "SELECT  pdd " +
        "FROM    OperatonProcessJsonSchemaDocumentDefinition pdd " +
        "WHERE   pdd.processDocumentDefinitionId.documentDefinitionId.version = ( " +
        "   SELECT  MAX(dd.id.version) " +
        "   FROM    JsonSchemaDocumentDefinition dd " +
        "   WHERE   dd.id.name = pdd.id.documentDefinitionId.name " +
        ")"
    )
    Page<OperatonProcessJsonSchemaDocumentDefinition> findAllByLatestDocumentDefinitionVersion(Pageable pageable);

    @Query(
        "SELECT  pdd " +
        "FROM    OperatonProcessJsonSchemaDocumentDefinition pdd " +
        "WHERE   pdd.processDocumentDefinitionId.documentDefinitionId.name = :documentDefinitionName " +
        "AND     pdd.processDocumentDefinitionId.documentDefinitionId.version = ( " +
        "   SELECT  MAX(dd.id.version) " +
        "   FROM    JsonSchemaDocumentDefinition dd " +
        "   WHERE   dd.id.name = pdd.id.documentDefinitionId.name " +
        ") " +
        "AND (:startableByUser IS NULL OR pdd.startableByUser = :startableByUser)" +
        "AND (:canInitializeDocument IS NULL OR pdd.canInitializeDocument = :canInitializeDocument)"
    )
    List<OperatonProcessJsonSchemaDocumentDefinition> findAll(
        @Param("documentDefinitionName") String documentDefinitionName,
        @Nullable @Param("startableByUser") Boolean startableByUser,
        @Nullable @Param("canInitializeDocument") Boolean canInitializeDocument
    );

    @Query(
        "SELECT  pdd " +
        "FROM    OperatonProcessJsonSchemaDocumentDefinition pdd " +
        "WHERE   pdd.processDocumentDefinitionId.documentDefinitionId.name = :documentDefinitionName " +
        "AND     pdd.processDocumentDefinitionId.documentDefinitionId.version = :documentDefinitionVersion"
    )
    List<OperatonProcessJsonSchemaDocumentDefinition> findAllByDocumentDefinitionNameAndVersion(
        @Param("documentDefinitionName") String documentDefinitionName,
        @Param("documentDefinitionVersion") long documentDefinitionVersion
    );

    @Query(
        "SELECT  pdd " +
        "FROM    OperatonProcessJsonSchemaDocumentDefinition pdd " +
        "WHERE   pdd.processDocumentDefinitionId.processDefinitionKey.key = :processDefinitionKey " +
        "AND     pdd.processDocumentDefinitionId.documentDefinitionId.version = ( " +
        "   SELECT  MAX(dd.id.version) " +
        "   FROM    JsonSchemaDocumentDefinition dd " +
        "   WHERE   dd.id.name = pdd.id.documentDefinitionId.name " +
        ")"
    )
    List<OperatonProcessJsonSchemaDocumentDefinition> findAllByProcessDefinitionKeyAndLatestDocumentDefinitionVersion(
        @Param("processDefinitionKey") String processDefinitionKey
    );

    @Query(
        "SELECT  pdd " +
        "FROM    OperatonProcessJsonSchemaDocumentDefinition pdd " +
        "WHERE   pdd.processDocumentDefinitionId.documentDefinitionId.name = :documentDefinitionName"
    )
    Optional<OperatonProcessJsonSchemaDocumentDefinition> findByDocumentDefinitionName(
        @Param("documentDefinitionName") String documentDefinitionName
    );

    @Query(
        "SELECT  pdd " +
        "FROM    OperatonProcessJsonSchemaDocumentDefinition pdd " +
        "WHERE   pdd.processDocumentDefinitionId.processDefinitionKey = :processDefinitionKey " +
        "AND     pdd.processDocumentDefinitionId.documentDefinitionId.version = ( " +
        "   SELECT  MAX(dd.id.version) " +
        "   FROM    JsonSchemaDocumentDefinition dd " +
        "   WHERE   dd.id.name = pdd.id.documentDefinitionId.name " +
        ")"
    )
    Optional<OperatonProcessJsonSchemaDocumentDefinition> findByProcessDefinitionKeyAndLatestDocumentDefinitionVersion(
        @Param("processDefinitionKey") ProcessDefinitionKey processDefinitionKey
    );

    @Query(
        "SELECT  pdd " +
        "FROM    OperatonProcessJsonSchemaDocumentDefinition pdd " +
        "WHERE   pdd.processDocumentDefinitionId.processDefinitionKey = :processDefinitionKey " +
        "AND     pdd.processDocumentDefinitionId.documentDefinitionId.version = ( " +
        "   SELECT MAX(dd.id.version) " +
        "   FROM   JsonSchemaDocumentDefinition dd " +
        "   WHERE  dd.id.name = pdd.id.documentDefinitionId.name " +
        ")"
    )
    List<OperatonProcessJsonSchemaDocumentDefinition> findAllByProcessDefinitionKeyAndLatestDocumentDefinitionVersion(
        @Param("processDefinitionKey") ProcessDefinitionKey processDefinitionKey
    );

    @Query(
        "SELECT  pdd " +
        "FROM    OperatonProcessJsonSchemaDocumentDefinition pdd " +
        "WHERE   pdd.processDocumentDefinitionId.processDefinitionKey = :processDefinitionKey " +
        "AND     pdd.processDocumentDefinitionId.documentDefinitionId.version = :documentDefinitionVersion "
    )
    Optional<OperatonProcessJsonSchemaDocumentDefinition> findByProcessDefinitionKeyAndDocumentDefinitionVersion(
        @Param("processDefinitionKey") ProcessDefinitionKey processDefinitionKey,
        @Param("documentDefinitionVersion") long documentDefinitionVersion
    );

    @Modifying
    @Query(
        "DELETE " +
        "FROM    OperatonProcessJsonSchemaDocumentDefinition pdd " +
        "WHERE   pdd.processDocumentDefinitionId.documentDefinitionId.name = :documentDefinitionName"
    )
    void deleteByDocumentDefinition(@Param("documentDefinitionName") String documentDefinitionName);

}