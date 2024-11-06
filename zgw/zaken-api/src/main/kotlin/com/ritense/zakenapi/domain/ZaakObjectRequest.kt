package com.ritense.zakenapi.domain

import com.fasterxml.jackson.annotation.JsonProperty
import java.net.URI
import java.util.*

data class ZaakObjectRequest (
    @JsonProperty("zaak")
    val zaakUrl: URI,
    @JsonProperty("object")
    var objectUrl: URI,
    val objectType: String,
    val objectTypeOverige: String?
)