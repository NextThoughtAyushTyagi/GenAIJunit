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

    List<ROIStructureVO> fetchProductBasedInterestRateCode(List<String> allowedInterestRateCodeUuidList, List<ROIStructureVO> roiStructureVOS) {
        List<ROIStructureVO> productBasedROIStructureVOList = []
        if (allowedInterestRateCodeUuidList && roiStructureVOS) {
            for (String interestRateCodeUuid : allowedInterestRateCodeUuidList) {
                for (ROIStructureVO roiStructureVO : roiStructureVOS) {
                    if (interestRateCodeUuid.equals(roiStructureVO.getUuid())) {
                        productBasedROIStructureVOList.add(roiStructureVO)
                    }
                }
            }
        }
        return productBasedROIStructureVOList
    }

    boolean checkAssessmentYear(Long tenantId, String itrJson, String itrCutOffDateStr) {
        Date currentDate = new Date()
        int currentYear = Year.now().getValue()
        // log.info(tenantId, "ITR cut off date TSV :::::::::::::: ${itrCutOffDateStr}")
//        if (itrCutOffDateStr) {
//            StringBuilder sBCutOffDate = new StringBuilder(itrCutOffDateStr)
//            itrCutOffDateStr = sBCutOffDate.replace(0, 4, currentYear as String)
//        }
        Date itrCutOffDate
        if (itrCutOffDateStr) {
            try{
                itrCutOffDate = new SimpleDateFormat("dd/MM/yyyy")?.parse(itrCutOffDateStr)
            } catch(Exception e){
                itrCutOffDate = currentDate
            }
        }
        
        // log.info(tenantId, "ITR cut off date :::::::::::::: ${itrCutOffDate}")
        String[] validAssessmentYear = new String[2]
        if (itrCutOffDate < currentDate) {
            StringBuilder assessmentYear = new StringBuilder("${currentYear}-${currentYear + 1}")
            assessmentYear.replace(5, 7, '')
            validAssessmentYear[0] = assessmentYear.toString()
        } else {
            StringBuilder assessmentYear = new StringBuilder("${currentYear - 1}-${currentYear}")
            assessmentYear.replace(5, 7, '')
            validAssessmentYear[0] = assessmentYear.toString()
            assessmentYear = new StringBuilder("${currentYear}-${currentYear + 1}")
            assessmentYear.replace(5, 7, '')
            println("assessmentYear two ::::::::::::  " + assessmentYear)
            validAssessmentYear[1] = assessmentYear.toString()
        }
        // log.info(tenantId, "validAssessmentYear ::::::::::::::::::::: ${validAssessmentYear}")
        JSONObject jsonObject = new JSONObject(itrJson)
        boolean isAssessmentYearValid = false
        if (jsonObject.has("itrvDetails")) {
            JSONObject itrDetails = jsonObject.getJSONObject("itrvDetails")
            JSONArray itr = itrDetails.getJSONArray("itrvInfoList")
            if (itr && itr.length()) {
                for (int index = 0; index < itr.length(); index++) {
                    JSONObject itrObject = itr.getJSONObject(index)
                    if (itrObject && !isAssessmentYearValid) {
                        String assessmentYear = itrObject.isNull("assessmentYear") ? null : itrObject.getString("assessmentYear")
                        // log.info(tenantId, "assessmentYear ::::::::::: ${assessmentYear}")
                        if (assessmentYear in validAssessmentYear) {
                            isAssessmentYearValid = true
                        }
                    }
                }
            }
        } else {
            JSONObject itrDetails = jsonObject.getJSONObject("itrDetails")
            JSONObject itrInfoList = itrDetails.getJSONObject("itrInfoList")
            JSONArray itr = itrInfoList.isNull("itr") ? null : itrInfoList.getJSONArray("itr")
            if (itr && itr.length()) {
                for (int index = 0; index < itr.length(); index++) {
                    JSONObject itrObject = itr.getJSONObject(index)
                    if (itrObject && !isAssessmentYearValid) {
                        String assessmentYear = itrObject.isNull("ay") ? null : itrObject.getString("ay")
                        // log.info(tenantId, "assessmentYear ::::::::::: ${assessmentYear}")
                        if (assessmentYear in validAssessmentYear) {
                            isAssessmentYearValid = true
                        }
                    }
                }
            }
        }
        return isAssessmentYearValid
    }

    Map documentList(Map params, Long tenantId) {
        Map result = [:]
        Map map = [:]
        List mapList = []
        LoanApplication loanApplication
        List<SupportingDocument> supportingDocuments
        CoApplicant coApplicant
        try {
            Integer offset = params.offset ? Integer.parseInt("${params.offset}") : 0
            Integer max = params.max ? Integer.parseInt("${params.max}") : 20
            String tabName = params?.tabName
            String serviceTypeUuid = params?.serviceTypeUuid
            Boolean fetchPerfiosReport = Boolean.parseBoolean("${params.fetchPerfiosReport}")
            log.debug("fetchPerfiosReport : ${fetchPerfiosReport}")
            if (params?.loanUuid) {
                loanApplication = loanApplicationRepository.findByUuidAndTenantId(params?.loanUuid, tenantId)
            }
            if (!loanApplication) {
                return result.put('error','loan.application.not.found') as Map
            }
            if ((params.referenceUuid && params.coapplicantUuid) || (params.referenceUuid && params.companyRepresentativeUuid) ||
                    (params.companyRepresentativeUuid && params.coapplicantUuid) || (params.referenceUuid && params.coapplicantUuid && params.companyRepresentativeUuid)) {
                return result.put('error','invalid.input') as Map
            }

            if (params.coapplicantUuid) {
                coApplicant = coApplicantRepository.findByTenantIdAndUuid(tenantId, params.coapplicantUuid)
                if (!coApplicant) {
                    return result.put('error','coapplicant.not.found') as Map
                }
            }

            if (params.documentFor == 'property') {
                List<PropertyDetail> propertyDetailList = propertyDetailRepository.findAllByLoanUuidAndTenantId(loanApplication.uuid, tenant?.id)
                supportingDocuments = loanApplicationRepository.fetchSupportingDocumentListForProperty(loanApplication, tenantId )?: []
                Map propertyDocumentsMap = [:]
                propertyDetailList.eachWithIndex { PropertyDetail propertyDetail, int i ->
                    List propertyDocumentsMapList = []
                    Map propertyMap = [:]
                    propertyMap.put('propertyUuid', propertyDetail.uuid)
                    propertyMap.put('name', i + 1)
                    List<SupportingDocument> propertyDocuments = supportingDocuments.findAll { it.propertyDetail.id == propertyDetail.id }
                    propertyDocuments.each {
                        propertyDocumentsMap.put('documentUploadedSuccessfully', true)
                        JSONObject checkForReportForDocUploadStatus = loanPurposeDocumentCategory ? (loanPurposeDocumentCategory.jsonValidation?.contains("checkForReportForDocUploadStatus") ? new JSONObject(loanPurposeDocumentCategory.jsonValidation)?.getJSONObject("checkForReportForDocUploadStatus") : null) : null
                        if (!fetchPerfiosReport && (it.clienttransactionId || it.perfiosTransactionId) &&
                                (checkForReportForDocUploadStatus ?
                                        (checkForReportForDocUploadStatus.getBoolean("toCheckIfReportsAvailable") && loanApplication.originationSource.toString() in checkForReportForDocUploadStatus.getString("originationSources").split(",")) : true)) {
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
                        propertyDocumentsMap.put('ReferenceUuid', it.reference?.uuid)
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
            } else {
                supportingDocuments = loanApplicationRepository.fetchSupportingDocumentListForProperty(loanApplication, tenantId) ?: []
                supportingDocuments.each() {
                    map.put('documentUploadedSuccessfully', true)
                    if ("FINANCIAL_STATEMENTS" == it.contentType && loanApplication.productType == "WEB_JOURNEY") {
                        map.put('documentUploadedSuccessfully', true)
                    }
                    map.put('Name', it.name)
                    map.put('documentUuid', it.uuid)
                    map.put('ReferenceUuid', it.reference?.uuid)
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
                        propertyDocumentsMap.put('size', "fileSize")
                        propertyDocumentsMap.put('fileUploadedOn', it?.lastUpdated)
                    } else {
                        propertyDocumentsMap.put('fileUploadedOn', null)
                        propertyDocumentsMap.put('size', 0)
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
            result = 'rest.api.ERROR'
        }
        return result
    }
} 