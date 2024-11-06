package com.ritense.zaakdetails.repository

import com.ritense.zaakdetails.domain.ZaakdetailsObject
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface ZaakdetailsObjectRepository : JpaRepository<ZaakdetailsObject, UUID> {
}