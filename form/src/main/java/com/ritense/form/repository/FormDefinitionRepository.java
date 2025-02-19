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

package com.ritense.form.repository;

import com.ritense.form.domain.FormDefinition;
import com.ritense.form.domain.FormIoFormDefinition;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FormDefinitionRepository extends JpaRepository<FormIoFormDefinition, UUID> {

    List<FormIoFormDefinition> findAllByOrderByNameAsc();

    Optional<FormIoFormDefinition> findByName(String name);

    Optional<FormIoFormDefinition> findByNameIgnoreCase(String name);

    @Query("SELECT f FROM FormIoFormDefinition f WHERE upper(f.name) LIKE upper(concat('%', :name, '%'))")
    Page<FormDefinition> findAllByNameContainingIgnoreCase(@Param("name") String name, Pageable pageable);
}