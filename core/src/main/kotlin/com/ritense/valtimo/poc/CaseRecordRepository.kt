package com.ritense.valtimo.poc

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface CaseRecordRepository: JpaRepository<CaseRecord, UUID> {
}