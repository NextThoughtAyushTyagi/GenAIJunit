package com.junit.demo.repository

import com.junit.demo.entity.CoApplicant
import com.junit.demo.entity.PropertyDetail
import org.springframework.stereotype.Service

@Service
class CoApplicantRepository {

    CoApplicant findByTenantIdAndUuid(String loanUuid, Long tenantId){
        return new CoApplicant()
    }
}
