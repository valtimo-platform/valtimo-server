package com.ritense.zaakdetails.service

import com.ritense.zaakdetails.domain.ZaakdetailsObject
import com.ritense.zaakdetails.repository.ZaakdetailsObjectRepository
import java.util.*

class ZaakdetailsObjectService(
    private val zaakdetailsObjectRepository: ZaakdetailsObjectRepository
) {
    fun findById(id: UUID): Optional<ZaakdetailsObject> {
        return zaakdetailsObjectRepository.findById(id)
    }

    fun save(zaakdetailsObject: ZaakdetailsObject) {
        zaakdetailsObjectRepository.save(zaakdetailsObject)
    }
}