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
} 