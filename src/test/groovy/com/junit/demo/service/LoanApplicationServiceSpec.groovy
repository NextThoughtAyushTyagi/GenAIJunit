package com.junit.demo.service

import spock.lang.Specification
import com.junit.demo.service.LoanApplicationService
import com.junit.demo.util.ROIStructureVO
import com.junit.demo.entity.LoanApplication
import com.junit.demo.entity.CoApplicant
import com.junit.demo.entity.PropertyDetail
import com.junit.demo.entity.SupportingDocument
import com.junit.demo.repository.LoanApplicationRepository
import com.junit.demo.repository.CoApplicantRepository
import com.junit.demo.repository.PropertyDetailRepository
import org.springframework.test.util.ReflectionTestUtils
import java.time.Year

class LoanApplicationServiceSpec extends Specification {
    def service = new LoanApplicationService()
    def loanApplicationRepository = Mock(LoanApplicationRepository)
    def coApplicantRepository = Mock(CoApplicantRepository)
    def propertyDetailRepository = Mock(PropertyDetailRepository)
    def tenantId = 1L
    
    def setup() {
        ReflectionTestUtils.setField(service, 'loanApplicationRepository', loanApplicationRepository)
        ReflectionTestUtils.setField(service, 'coApplicantRepository', coApplicantRepository)
        ReflectionTestUtils.setField(service, 'propertyDetailRepository', propertyDetailRepository)
    }
    
    static int getCurrentYear() { java.time.Year.now().value }

    def "test documentList with null loanUuid should return error"() {
        given:
        def params = [:]
        
        when:
        def result = service.documentList(params, tenantId)
        
        then:
        result instanceof Map
        result.error == 'loan.application.not.found'
    }

    def "test documentList with invalid loanUuid should return error"() {
        given:
        def params = [loanUuid: 'invalid-uuid']
        loanApplicationRepository.findByUuidAndTenantId('invalid-uuid', tenantId) >> null
        
        when:
        def result = service.documentList(params, tenantId)
        
        then:
        result instanceof Map
        result.error == 'loan.application.not.found'
    }

    def "test documentList with valid loanUuid but loan not found should return error"() {
        given:
        def params = [loanUuid: 'valid-uuid']
        loanApplicationRepository.findByUuidAndTenantId('valid-uuid', tenantId) >> null
        
        when:
        def result = service.documentList(params, tenantId)
        
        then:
        result instanceof Map
        result.error == 'loan.application.not.found'
    }

    def "test documentList with invalid input parameters should return error"() {
        given:
        def params = [
            loanUuid: 'valid-uuid',
            referenceUuid: 'ref-uuid',
            coapplicantUuid: 'coapp-uuid'
        ]
        def loanApplication = new LoanApplication(uuid: 'valid-uuid')
        loanApplicationRepository.findByUuidAndTenantId('valid-uuid', tenantId) >> loanApplication
        
        when:
        def result = service.documentList(params, tenantId)
        
        then:
        result instanceof Map
        result.error == 'invalid.input'
    }

    def "test documentList with invalid input parameters - referenceUuid and companyRepresentativeUuid"() {
        given:
        def params = [
            loanUuid: 'valid-uuid',
            referenceUuid: 'ref-uuid',
            companyRepresentativeUuid: 'company-uuid'
        ]
        def loanApplication = new LoanApplication(uuid: 'valid-uuid')
        loanApplicationRepository.findByUuidAndTenantId('valid-uuid', tenantId) >> loanApplication
        
        when:
        def result = service.documentList(params, tenantId)
        
        then:
        result instanceof Map
        result.error == 'invalid.input'
    }

    def "test documentList with invalid input parameters - companyRepresentativeUuid and coapplicantUuid"() {
        given:
        def params = [
            loanUuid: 'valid-uuid',
            companyRepresentativeUuid: 'company-uuid',
            coapplicantUuid: 'coapp-uuid'
        ]
        def loanApplication = new LoanApplication(uuid: 'valid-uuid')
        loanApplicationRepository.findByUuidAndTenantId('valid-uuid', tenantId) >> loanApplication
        
        when:
        def result = service.documentList(params, tenantId)
        
        then:
        result instanceof Map
        result.error == 'invalid.input'
    }

    def "test documentList with invalid input parameters - all three uuids"() {
        given:
        def params = [
            loanUuid: 'valid-uuid',
            referenceUuid: 'ref-uuid',
            companyRepresentativeUuid: 'company-uuid',
            coapplicantUuid: 'coapp-uuid'
        ]
        def loanApplication = new LoanApplication(uuid: 'valid-uuid')
        loanApplicationRepository.findByUuidAndTenantId('valid-uuid', tenantId) >> loanApplication
        
        when:
        def result = service.documentList(params, tenantId)
        
        then:
        result instanceof Map
        result.error == 'invalid.input'
    }

    def "test documentList with invalid coapplicantUuid should return error"() {
        given:
        def params = [
            loanUuid: 'valid-uuid',
            coapplicantUuid: 'invalid-coapp-uuid'
        ]
        def loanApplication = new LoanApplication(uuid: 'valid-uuid')
        loanApplicationRepository.findByUuidAndTenantId('valid-uuid', tenantId) >> loanApplication
        coApplicantRepository.findByTenantIdAndUuid('invalid-coapp-uuid', tenantId) >> null
        
        when:
        def result = service.documentList(params, tenantId)
        
        then:
        result instanceof Map
        result.error == 'coapplicant.not.found'
    }

    def "test documentList for supporting documents non-property"() {
        given:
        def params = [
            loanUuid: 'valid-uuid',
            offset: '0',
            max: '10'
        ]
        def loanApplication = new LoanApplication(uuid: 'valid-uuid')
        def supportingDocument = new SupportingDocument(
            uuid: 'doc-uuid',
            name: 'supporting-doc.pdf',
            contentType: 'PDF',
            perfiosCategoryName: 'VERIFIED',
            perfiosResponse: 'success',
            dateCreated: new Date(),
            lastUpdated: new Date(),
            isItrAssessmentYearValid: true,
            isGstValidForAssessment: true
        )
        
        loanApplicationRepository.findByUuidAndTenantId('valid-uuid', tenantId) >> loanApplication
        loanApplicationRepository.fetchSupportingDocumentListForProperty(loanApplication, tenantId) >> [supportingDocument]
        
        when:
        def result = service.documentList(params, tenantId)
        
        then:
        result instanceof Map
        result.totalSupportingDocumentCount == 1
        result.supportingDocuments.size() == 1
        result.supportingDocuments[0].Name == 'supporting-doc.pdf'
        result.supportingDocuments[0].documentUuid == 'doc-uuid'
        result.supportingDocuments[0].documentUploadedSuccessfully == true
    }

    def "test documentList for supporting documents with FINANCIAL_STATEMENTS and WEB_JOURNEY"() {
        given:
        def params = [
            loanUuid: 'valid-uuid'
        ]
        def loanApplication = new LoanApplication(uuid: 'valid-uuid', productType: 'WEB_JOURNEY')
        def supportingDocument = new SupportingDocument(
            uuid: 'doc-uuid',
            name: 'financial-statement.pdf',
            contentType: 'FINANCIAL_STATEMENTS'
        )
        
        loanApplicationRepository.findByUuidAndTenantId('valid-uuid', tenantId) >> loanApplication
        loanApplicationRepository.fetchSupportingDocumentListForProperty(loanApplication, tenantId) >> [supportingDocument]
        
        when:
        def result = service.documentList(params, tenantId)
        
        then:
        result instanceof Map
        result.supportingDocuments[0].documentUploadedSuccessfully == true
    }

    def "test documentList with pagination - offset and max"() {
        given:
        def params = [
            loanUuid: 'valid-uuid',
            offset: '10',
            max: '5'
        ]
        def loanApplication = new LoanApplication(uuid: 'valid-uuid')
        def supportingDocuments = (1..5).collect { i ->
            new SupportingDocument(
                uuid: "doc-uuid-${i}",
                name: "doc-${i}.pdf"
            )
        }
        
        loanApplicationRepository.findByUuidAndTenantId('valid-uuid', tenantId) >> loanApplication
        loanApplicationRepository.fetchSupportingDocumentListForProperty(loanApplication, tenantId) >> supportingDocuments
        
        when:
        def result = service.documentList(params, tenantId)
        
        then:
        result instanceof Map
        result.totalSupportingDocumentCount == 5
        result.supportingDocuments.size() == 5
        result.offset == '15'
    }

    def "test documentList with default pagination values"() {
        given:
        def params = [
            loanUuid: 'valid-uuid'
        ]
        def loanApplication = new LoanApplication(uuid: 'valid-uuid')
        def supportingDocuments = (1..20).collect { i ->
            new SupportingDocument(
                uuid: "doc-uuid-${i}",
                name: "doc-${i}.pdf"
            )
        }
        
        loanApplicationRepository.findByUuidAndTenantId('valid-uuid', tenantId) >> loanApplication
        loanApplicationRepository.fetchSupportingDocumentListForProperty(loanApplication, tenantId) >> supportingDocuments
        
        when:
        def result = service.documentList(params, tenantId)
        
        then:
        result instanceof Map
        result.totalSupportingDocumentCount == 20
        result.supportingDocuments.size() == 20
        result.offset == '20'
    }

    def "test documentList with empty supporting documents"() {
        given:
        def params = [
            loanUuid: 'valid-uuid'
        ]
        def loanApplication = new LoanApplication(uuid: 'valid-uuid')
        
        loanApplicationRepository.findByUuidAndTenantId('valid-uuid', tenantId) >> loanApplication
        loanApplicationRepository.fetchSupportingDocumentListForProperty(loanApplication, tenantId) >> []
        
        when:
        def result = service.documentList(params, tenantId)
        
        then:
        result instanceof Map
        result.totalSupportingDocumentCount == 0
        result.supportingDocuments.size() == 0
    }

    def "test documentList with valid coapplicantUuid"() {
        given:
        def params = [
            loanUuid: 'valid-uuid',
            coapplicantUuid: 'valid-coapp-uuid'
        ]
        def loanApplication = new LoanApplication(uuid: 'valid-uuid')
        def coApplicant = new CoApplicant(uuid: 'valid-coapp-uuid')
        def supportingDocument = new SupportingDocument(
            uuid: 'doc-uuid',
            name: 'coapp-doc.pdf'
        )
        loanApplicationRepository.findByUuidAndTenantId('valid-uuid', tenantId) >> loanApplication
        coApplicantRepository.findByTenantIdAndUuid('valid-coapp-uuid', tenantId) >> coApplicant
        loanApplicationRepository.fetchSupportingDocumentListForProperty(loanApplication, tenantId) >> [supportingDocument]
        
        when:
        def result = service.documentList(params, tenantId)
        
        then:
        result instanceof Map
        result.totalSupportingDocumentCount == 1
        result.supportingDocuments.size() == 1
    }

    def "test documentList with exception handling"() {
        given:
        def params = [loanUuid: 'valid-uuid']
        loanApplicationRepository.findByUuidAndTenantId('valid-uuid', tenantId) >> { throw new RuntimeException("Database error") }
        
        when:
        def result = service.documentList(params, tenantId)
        
        then:
        result instanceof Map
        result.error == 'rest.api.ERROR'
    }

    def "test documentList with null params"() {
        given:
        def params = null
        
        when:
        def result = service.documentList(params, tenantId)
        
        then:
        result instanceof Map
        result.error == 'rest.api.ERROR'
    }

    def "test documentList with invalid offset parameter"() {
        given:
        def params = [
            loanUuid: 'valid-uuid',
            offset: 'invalid-offset'
        ]
        def loanApplication = new LoanApplication(uuid: 'valid-uuid')
        
        loanApplicationRepository.findByUuidAndTenantId('valid-uuid', tenantId) >> loanApplication
        loanApplicationRepository.fetchSupportingDocumentListForProperty(loanApplication, tenantId) >> []
        
        when:
        def result = service.documentList(params, tenantId)
        
        then:
        result instanceof Map
        result.error == 'rest.api.ERROR'
    }

    def "test documentList with invalid max parameter"() {
        given:
        def params = [
            loanUuid: 'valid-uuid',
            max: 'invalid-max'
        ]
        def loanApplication = new LoanApplication(uuid: 'valid-uuid')
        
        loanApplicationRepository.findByUuidAndTenantId('valid-uuid', tenantId) >> loanApplication
        loanApplicationRepository.fetchSupportingDocumentListForProperty(loanApplication, tenantId) >> []
        
        when:
        def result = service.documentList(params, tenantId)
        
        then:
        result instanceof Map
        result.error == 'rest.api.ERROR'
    }

    def "test documentList with invalid fetchPerfiosReport parameter"() {
        given:
        def params = [
            loanUuid: 'valid-uuid',
            fetchPerfiosReport: 'invalid-boolean'
        ]
        def loanApplication = new LoanApplication(uuid: 'valid-uuid')
        
        loanApplicationRepository.findByUuidAndTenantId('valid-uuid', tenantId) >> loanApplication
        loanApplicationRepository.fetchSupportingDocumentListForProperty(loanApplication, tenantId) >> []
        
        when:
        def result = service.documentList(params, tenantId)
        
        then:
        result instanceof Map
        result.error == 'rest.api.ERROR'
    }

    def "test documentList with ITR validation error in supporting documents"() {
        given:
        def params = [
            loanUuid: 'valid-uuid'
        ]
        def loanApplication = new LoanApplication(uuid: 'valid-uuid')
        def supportingDocument = new SupportingDocument(
            uuid: 'doc-uuid',
            name: 'itr-doc.pdf',
            isItrAssessmentYearValid: false
        )
        
        loanApplicationRepository.findByUuidAndTenantId('valid-uuid', tenantId) >> loanApplication
        loanApplicationRepository.fetchSupportingDocumentListForProperty(loanApplication, tenantId) >> [supportingDocument]
        
        when:
        def result = service.documentList(params, tenantId)
        
        then:
        result instanceof Map
        result.supportingDocuments[0].errorMessage == 'itr.assessment.year.not.valid'
    }

    def "test documentList with GST validation error in supporting documents"() {
        given:
        def params = [
            loanUuid: 'valid-uuid'
        ]
        def loanApplication = new LoanApplication(uuid: 'valid-uuid')
        def supportingDocument = new SupportingDocument(
            uuid: 'doc-uuid',
            name: 'gst-doc.pdf',
            isGstValidForAssessment: false
        )
        
        loanApplicationRepository.findByUuidAndTenantId('valid-uuid', tenantId) >> loanApplication
        loanApplicationRepository.fetchSupportingDocumentListForProperty(loanApplication, tenantId) >> [supportingDocument]
        
        when:
        def result = service.documentList(params, tenantId)
        
        then:
        result instanceof Map
        result.supportingDocuments[0].gstValidationError == 'gst.assessment.year.not.valid'
    }
} 