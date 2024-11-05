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

/*
Q1: Linking other entities to the CaseDefinition entity? We want to decouple ZGW.

Answer:

• Option A: Core properties are within the CaseDefinition.
• Option B: All add-ons become separate entities.

Q2: It’s problematic that we only have one CaseCamundaProcessDefinition class. Reference to Core.

Answer:

• Option A: Case is core, move it, which would mean the Case module would no longer exist.
• Option B: Camunda becomes a separate module; move everything there.
• Option C: CaseCamundaProcessDefinition becomes a standalone entity.

Q3: What is the overall strategy? There is a significant likelihood of creating a reference to CaseDefinition.

Is there a need for a feasibility study for any of the options? To validate Option D, yes. The risk is dependency hell; we want to avoid cyclic dependencies. Can we re-evaluate this?

Answer:

• Option A (OPEN): Create a new RelatedCaseDefinitionId interface, local entity implementation with findBy<Type>(caseDefId).
• Option B: Each entity references CaseDefinition (JPA-supported).
• Option C: Each entity creates an FK to CaseDefinition using a column ID (outside JPA).
• Option D (Tom’s preferred choice, combined with E but disruptive): CaseDefinition holds all relationships.
• Option E (Rejected): Explicitly define relationships in CaseDefinition on both ends.

Architectural improvement options:

• Merge certain modules.
• Restructure modules:
• Core module + ZGW module = Monolith (Modulith).
    Find a impl project to verify.

Goals:

• Establish relationship structures.
• Define dependency structure within the monolith.

Q4: What functional aspects do we need to develop regarding CaseDefinition?

Answer:
List of functional requirements:
Prerequisites:

• Ability to scale and reduce dependencies between processes.

Must-haves:

• Ability to link configurations to CaseDefinition at both DB/JPA and file levels.
• Support import/export of CaseDefinition with a new versioned meta file.
• Existing configurations must link to CaseDefinition as outlined in the feasibility study.
• One method for auto-deployment using the import/export approach to remove technical debt.
• Forms, DocumentDefinition, and ProcessDefinitions are linked to a single CaseDefinition with no reuse.
• Migration strategy = custom vs. tooling (using one Liquibase instance) to isolate and create case-definitions during upgrades.
• Include creating new configuration files in the correct location for the implementation team to commit and use.

Nice-to-haves:

• PBAC support is ideal but not urgently needed and feasible.
• Revising the structure of configuration files, ensuring some are stored in fixed locations.

Version 2 (V2):

• What about the changelog? Should it be improved or kept as is?*/

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