package com.junit.demo.repository

import com.junit.demo.entity.LoanApplication
import com.junit.demo.entity.PropertyDetail
import org.springframework.stereotype.Service

@Service
class PropertyDetailRepository {

    PropertyDetail findByUuidAndTenantId(String loanUuid, Long tenantId){
        return new PropertyDetail()
    }
}
