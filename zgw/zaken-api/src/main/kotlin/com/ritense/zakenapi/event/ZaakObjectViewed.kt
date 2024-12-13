package com.ritense.zakenapi.event

import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.ritense.outbox.domain.BaseEvent

class ZaakObjectViewed (zaakobject: ObjectNode) : BaseEvent(
    type = "com.ritense.gzac.zrc.zaakobject.listed",
    resultType = "com.ritense.zakenapi.domain.ZaakObject",
    resultId = null,
    result = zaakobject
)