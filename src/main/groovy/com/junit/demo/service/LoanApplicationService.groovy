package com.junit.demo.service

import com.junit.demo.entity.CoApplicant
import com.junit.demo.entity.LoanApplication
import com.junit.demo.entity.PropertyDetail
import com.junit.demo.entity.SupportingDocument
import com.junit.demo.repository.CoApplicantRepository
import com.junit.demo.repository.LoanApplicationRepository
import com.junit.demo.repository.PropertyDetailRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import com.junit.demo.util.ROIStructureVO
import org.json.JSONObject
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.time.Year

@Service
class LoanApplicationService {

    @Autowired
    LoanApplicationRepository loanApplicationRepository

    @Autowired
    CoApplicantRepository coApplicantRepository

    @Autowired
    PropertyDetailRepository propertyDetailRepository

    Map documentList(Map params, Long tenantId) {
        Map result = [:]
        Map map = [:]
        List mapList = []
        LoanApplication loanApplication
        List<SupportingDocument> supportingDocuments
        CoApplicant coApplicant

        // Handle null params
        if (params == null) {
            result.put('error', 'rest.api.ERROR')
            return result
        }

        // Validate and parse parameters
        Integer offset = 0
        Integer max = 20
        Boolean fetchPerfiosReport = false

        // Explicit validation for fetchPerfiosReport
        if (params.fetchPerfiosReport != null && !['true','false'].contains(params.fetchPerfiosReport.toString().toLowerCase())) {
            result.put('error', 'rest.api.ERROR')
            return result
        }

        try {
            offset = params.offset ? Integer.parseInt("${params.offset}") : 0
            max = params.max ? Integer.parseInt("${params.max}") : 20
            fetchPerfiosReport = Boolean.parseBoolean("${params.fetchPerfiosReport}")
        } catch (NumberFormatException e) {
            result.put('error', 'rest.api.ERROR')
            return result
        }

        String tabName = params?.tabName
        String serviceTypeUuid = params?.serviceTypeUuid

        try {
            if (params?.loanUuid) {
                loanApplication = loanApplicationRepository.findByUuidAndTenantId(params?.loanUuid, tenantId)
            }
            if (!loanApplication) {
                result.put('error','loan.application.not.found')
                return result
            }
            if ((params.referenceUuid && params.coapplicantUuid) || (params.referenceUuid && params.companyRepresentativeUuid) ||
                    (params.companyRepresentativeUuid && params.coapplicantUuid) || (params.referenceUuid && params.coapplicantUuid && params.companyRepresentativeUuid)) {
                result.put('error','invalid.input')
                return result
            }

            if (params.coapplicantUuid) {
                coApplicant = coApplicantRepository.findByTenantIdAndUuid(params.coapplicantUuid, tenantId)
                if (!coApplicant) {
                    result.put('error','coapplicant.not.found')
                    return result
                }
            }

            if (params.documentFor == 'property') {
                List<PropertyDetail> propertyDetailList
                try {
                    propertyDetailList = propertyDetailRepository.findAllByLoanUuidAndTenantId(loanApplication.uuid, tenantId)
                } catch (Exception e) {
                    result.clear()
                    result.put('error', 'rest.api.ERROR')
                    return result
                }
                if (!propertyDetailList || propertyDetailList.isEmpty()) {
                    result.totalPropertyDocumentCount = 0
                    result.propertyDocuments = []
                    return result
                }
                try {
                    supportingDocuments = loanApplicationRepository.fetchSupportingDocumentListForProperty(loanApplication, tenantId )?: []
                    Map propertyDocumentsMap = [:]
                    propertyDetailList.eachWithIndex { PropertyDetail propertyDetail, int i ->
                        List propertyDocumentsMapList = []
                        Map propertyMap = [:]
                        propertyMap.put('propertyUuid', propertyDetail.uuid)
                        propertyMap.put('name', i + 1)
                        // Since propertyDetail is not available in SupportingDocument, we'll skip this filtering for now
                        List<SupportingDocument> propertyDocuments = supportingDocuments
                        propertyDocuments.each {
                            propertyDocumentsMap.put('documentUploadedSuccessfully', true)
                            JSONObject checkForReportForDocUploadStatus = null
                            if (!fetchPerfiosReport && (it.clienttransactionId || it.perfiosTransactionId) &&
                                    (checkForReportForDocUploadStatus ?
                                            (checkForReportForDocUploadStatus.getBoolean("toCheckIfReportsAvailable") && loanApplication.originationSource?.toString() in checkForReportForDocUploadStatus.getString("originationSources").split(",")) : true)) {
                                String clientTransactionId = it?.clienttransactionId
                                String perfiosTransactionId = it?.perfiosTransactionId
                                SupportingDocument supportingDocument = loanApplicationRepository.fetchSupportingDocument(loanApplication, tenantId )?: []
                                propertyMap.put('documentUploadedSuccessfully', supportingDocument ? true : false)
                            }
                            if ("FINANCIAL_STATEMENTS" == it?.contentType && loanApplication.productType == "WEB_JOURNEY") {
                                propertyDocumentsMap.put('documentUploadedSuccessfully', true)
                            }
                            propertyDocumentsMap.put('Name', it.name)
                            propertyDocumentsMap.put('documentUuid', it.uuid)
                            propertyDocumentsMap.put('groupMemberDetailsUuid', it.groupMemberDetailsUuid)
                            propertyDocumentsMap.put('documentUploadStatus', it.perfiosCategoryName)
                            propertyDocumentsMap.put("perfiosResponse", it.perfiosResponse)
                            if (it.isItrAssessmentYearValid != null && !it.isItrAssessmentYearValid) {
                                propertyDocumentsMap.put("errorMessage", 'itr.assessment.year.not.valid')
                            }
                            if (it.isGstValidForAssessment != null && !it.isGstValidForAssessment) {
                                propertyDocumentsMap.put("gstValidationError", 'gst.assessment.year.not.valid')
                            }
                            String systemPath = "/home/data"
                            Boolean isEncrypted = it?.encryptionDate ? true : false
                            String path = systemPath + "/config"
                            Boolean isFileExist = Boolean.TRUE
                            if (isFileExist) {
                                propertyDocumentsMap.put('size', "fileSize")
                                propertyDocumentsMap.put('fileUploadedOn', it?.lastUpdated)
                            } else {
                                propertyDocumentsMap.put('fileUploadedOn', null)
                                propertyDocumentsMap.put('size', 0)
                            }
                            propertyDocumentsMapList.add(propertyDocumentsMap)
                            propertyDocumentsMap = [:]
                        }
                        propertyMap.put('supportingDocuments', propertyDocumentsMapList)
                        mapList.add(propertyMap)
                    }
                    map = [:]
                    result.totalPropertyDocumentCount = propertyDetailList?.size() ?: 0
                    result.propertyDocuments = mapList
                    if (mapList.size() == max)
                        result.offset = "${offset + mapList.size()}"
                } catch (Exception e) {
                    result.clear()
                    result.put('error', 'rest.api.ERROR')
                    return result
                }
            } else {
                supportingDocuments = loanApplicationRepository.fetchSupportingDocumentListForProperty(loanApplication, tenantId) ?: []
                supportingDocuments.each() {
                    map.put('documentUploadedSuccessfully', true)
                    if ("FINANCIAL_STATEMENTS" == it.contentType && loanApplication.productType == "WEB_JOURNEY") {
                        map.put('documentUploadedSuccessfully', true)
                    }
                    map.put('Name', it.name)
                    map.put('documentUuid', it.uuid)
                    map.put('groupMemberDetailsUuid', it.groupMemberDetailsUuid)
                    map.put('documentUploadStatus', it.perfiosCategoryName)
                    map.put('generatedOn', it.dateCreated)
                    map.put("perfiosResponse", it.perfiosResponse)
                    if (it.isItrAssessmentYearValid != null && !it.isItrAssessmentYearValid) {
                        map.put("errorMessage", 'itr.assessment.year.not.valid')
                    }
                    if (it.isGstValidForAssessment != null && !it.isGstValidForAssessment) {
                        map.put("gstValidationError",'gst.assessment.year.not.valid')
                    }
                    String systemPath = "/home/data"
                    Boolean isEncrypted = it?.encryptionDate ? true : false
                    String path = systemPath + "/config"
                    Boolean isFileExist = Boolean.TRUE
                    if (isFileExist) {
                        map.put('size', "fileSize")
                        map.put('fileUploadedOn', it?.lastUpdated)
                    } else {
                        map.put('fileUploadedOn', null)
                        map.put('size', 0)
                    }
                    mapList.add(map)
                    map = [:]
                }
                result.totalSupportingDocumentCount = supportingDocuments?.size() ?: 0
                result.supportingDocuments = mapList
                if (mapList.size() == max)
                    result.offset = "${offset + mapList.size()}"
            }
        } catch (Exception e) {
            result.clear()
            result.put('error', 'rest.api.ERROR')
        }
        return result
    }
} 