package com.junit.demo.service

import spock.lang.Specification
import com.junit.demo.service.LoanApplicationService
import com.junit.demo.util.ROIStructureVO
import java.time.Year

class LoanApplicationServiceSpec extends Specification {
    def service = new LoanApplicationService()
    def tenantId = 1L
    static int getCurrentYear() { java.time.Year.now().value }

    // --- fetchProductBasedInterestRateCode tests ---
    def "should return empty list when allowedInterestRateCodeUuidList is empty"() {
        given:
        def roiList = [new ROIStructureVO(uuid: "uuid-1"), new ROIStructureVO(uuid: "uuid-2")]
        def allowedList = []

        when:
        def result = service.fetchProductBasedInterestRateCode(allowedList, roiList)

        then:
        result.isEmpty()
    }

    def "should return empty list when no uuids match"() {
        given:
        def roiList = [new ROIStructureVO(uuid: "uuid-1"), new ROIStructureVO(uuid: "uuid-2")]
        def allowedList = ["uuid-3", "uuid-4"]

        when:
        def result = service.fetchProductBasedInterestRateCode(allowedList, roiList)

        then:
        result.isEmpty()
    }

    def "should return only matching ROIStructureVOs"() {
        given:
        def roi1 = new ROIStructureVO(uuid: "uuid-1")
        def roi2 = new ROIStructureVO(uuid: "uuid-2")
        def roi3 = new ROIStructureVO(uuid: "uuid-3")
        def roiList = [roi1, roi2, roi3]
        def allowedList = ["uuid-2", "uuid-3"]

        when:
        def result = service.fetchProductBasedInterestRateCode(allowedList, roiList)

        then:
        result.size() == 2
        result.containsAll([roi2, roi3])
    }

    def "should return empty list when both allowedInterestRateCodeUuidList and ROI list are empty"() {
        given:
        def roiList = []
        def allowedList = []

        when:
        def result = service.fetchProductBasedInterestRateCode(allowedList, roiList)

        then:
        result.isEmpty()
    }
} 