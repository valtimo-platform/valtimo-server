package com.ritense.case.domain.casedefinition

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable

@Immutable
@Entity
@Table(name = "ACT_RE_PROCDEF")
class CaseCamundaProcessDefinition(
    @Id
    @Column(name = "ID_", insertable = false, updatable = false)
    val id: String
)