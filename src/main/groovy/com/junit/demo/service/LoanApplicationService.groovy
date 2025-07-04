package com.junit.demo.service

import org.springframework.stereotype.Service
import com.junit.demo.util.ROIStructureVO
import org.json.JSONObject
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.time.Year

@Service
class LoanApplicationService {
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
} 