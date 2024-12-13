package com.ritense.zaakdetails.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.net.URI
import java.util.UUID

@Entity
@Table(name = "zaakdetails_object")
data class ZaakdetailsObject(
    @Id
    @Column(name = "document_id")
    val documentId: UUID,

    @Column(name = "object_url")
    val objectURI: URI,

    @Column(name = "linked_to_zaak")
    var linkedToZaak: Boolean = false
)