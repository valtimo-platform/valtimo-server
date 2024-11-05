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

// Q1:  In de CaseDefintion entity andere entities linken? Willen we zgw loskoppelen.
// A:   Optie A: Core properties zijn in de CaseDefinition.
//      Optie B: Alle addons zijn een losse entity.

// Q2:  Niet fijn dat we 1 CaseCamundaProcessDefinition class hebben. Ref op Core
// A:   Optie A: Case is core, move it, Dus Case module komt dan te vervallen?
//      Optie B: Camunda wordt een losse module alle moven
//      Optie C: CaseCamundaProcessDefinition wordt een losse entity

// Q3   Wat is de strategie, rondom? Er is een grote kans dat een reference naar CaseDefinition wordt gemaakt.
//      Is er een behoefte aan feasability study voor een van de opties? Om D te valideren wel.
//      Risico is dependency hell. We willen geen cyclic dependencies. Kunnen we dit hervalideren.
// A:   Optie A (OPEN): New RelatedCaseDefinitionId interface maken, locale entity impl. findBy<Type>(caseDefId)
//      Optie B (): Elke entity maak een reference naar CaseDefinition. (JPA supported)
//      Optie C (): Elke een entity maakt een FK naar CaseDefinition middels een column id (Buiten jpa om)
//      Optie D (Toms choice combined with e but breaking) : CaseDefinition die alle relaties heeft.
//      Optie E (Afgeschoten): CaseDefinition relaties expliciet maken aan beide kanten.
//      Archictectuur improvement options:
//         - Sommige module samenvoegen
//         - Restrukturering van de modules
//              - Core module + ZGW module = Monolith = Modulith
//      We zijn op naar 2 dingen: Hoe gaan we relaties leggen? En de dependencies structuur in the monolith

// Q4:  Wat gaan we functioneel maken? rondom CaseDefinition
// A:   Lijst van functionele requirements:
//      Randvoorwaarden:
//      - Need to scale and decreased dependecies between processes
//      Must haves:
//      - Configuratie kunnen koppelen aan CaseDefinition zowel op DB/JPA als op File niveau.
//      - Import / export van CaseDefinition supported, nieuwe meta file met version.
//          - Bestaande config's moeten linken aan CaseDefinition zoals in de feasability study
//          - 1 manier van autodeployment middels de Import/export aanpak (Technical debt wegnemen)
//      - Forms, DocumentDefinition, ProcessDefinitions zijn gekoppeld aan 1 CaseDefinition. Geen hergebruik.
//      - Migration = Custom vs Tooling 1 liquibase so isolate and create case-definitions on upgrade.
//          - Even creating the new config files in the right place. For an impl team to commit and use.
//      Really nice to have:
//      - PBAC supported ideaal maar lijkt niet acuut nodig maar is feasable
//      - Herzien van config bestanden in structuur. Sommige moeten op een vaste plek staan.
//      V2:
//      - What about changelog? Verbeteren of houden zoals het is?

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