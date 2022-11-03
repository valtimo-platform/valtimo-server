package com.ritense.tenancy.jpa

import com.fasterxml.jackson.annotation.JsonIgnore
import com.ritense.tenancy.TenantAware
import org.hibernate.annotations.Filter
import org.hibernate.annotations.FilterDef
import org.hibernate.annotations.ParamDef
import org.springframework.data.domain.AbstractAggregateRoot
import javax.persistence.Column
import javax.persistence.EntityListeners
import javax.persistence.MappedSuperclass

@MappedSuperclass
@FilterDef(
    name = AbstractTenantAwareAggregateRoot.TENANT_FILTER_NAME,
    defaultCondition = AbstractTenantAwareAggregateRoot.TENANT_COLUMN + " = :" + AbstractTenantAwareAggregateRoot.TENANT_PARAMETER_NAME,
    parameters = [ParamDef(name = AbstractTenantAwareAggregateRoot.TENANT_PARAMETER_NAME, type = "string")]
)
@Filter(name = AbstractTenantAwareAggregateRoot.TENANT_FILTER_NAME)
@EntityListeners(
    TenantAwareListener::class
)
open class AbstractTenantAwareAggregateRoot<A : AbstractTenantAwareAggregateRoot<A>?> :
    AbstractAggregateRoot<A>(), TenantAware {
    @JsonIgnore
    @Column(name = TENANT_COLUMN, columnDefinition = "VARCHAR(256)", nullable = false)
    override var tenantId: String? = null

    companion object {
        const val TENANT_FILTER_NAME = "tenantFilter"
        const val TENANT_PARAMETER_NAME = "tenantId"
        const val TENANT_COLUMN = "tenant_id"
    }
}