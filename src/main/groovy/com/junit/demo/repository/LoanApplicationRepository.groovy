package com.junit.demo.repository

import com.junit.demo.entity.LoanApplication
import com.junit.demo.entity.SupportingDocument
import org.springframework.stereotype.Service

@Service
class LoanApplicationRepository {

    LoanApplication findByUuidAndTenantId(String loanUuid, Long tenantId){
        return new LoanApplication()
    }

    List<SupportingDocument> fetchSupportingDocumentListForProperty(LoanApplication loanApplication, Long tenantId){
        return new SupportingDocument() as List
    }

    SupportingDocument fetchSupportingDocument(LoanApplication loanApplication, Long tenantId){
        return new SupportingDocument()
    }
}
