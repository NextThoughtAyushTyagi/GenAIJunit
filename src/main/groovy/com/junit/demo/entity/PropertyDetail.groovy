package com.junit.demo.entity

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener

public class PropertyDetail implements Serializable {

    Long id
    Long tenantId
    @CreatedDate
    Date dateCreated
    @LastModifiedDate
    Date lastUpdated
    String uuid = UUID.randomUUID()
    String loanUuid

}
