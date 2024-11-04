package com.ritense.case.domain.casedefinition

import jakarta.persistence.Column
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.JoinTable
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "case_definition_2")
data class CaseDefinition(
    @Id
    val id: UUID,

    @Column(name = "case_definition_name")
    val name: String,

    @Embedded
    val version: SemVer,

    @OneToMany
    @JoinTable(
        name = "case_definition_process_definition",
        joinColumns = [JoinColumn(name = "case_definition_id")],
        inverseJoinColumns = [JoinColumn(name = "camunda_process_definition_id")]
    )
    var processDefinitions: MutableSet<CaseCamundaProcessDefinition> = mutableSetOf()
) {
    init {
        require(name.isNotBlank()) { "CaseDefinition name must not be blank" }
    }
}