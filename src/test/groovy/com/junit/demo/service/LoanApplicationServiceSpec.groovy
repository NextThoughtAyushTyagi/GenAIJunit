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

    // --- checkAssessmentYear tests ---
    def "should return true for valid assessmentYear in itrvDetails"() {
        given:
        def year = getCurrentYear()
        def validYear = String.format("%d-%02d", year, (year + 1) % 100)
        def itrJson = """
        {
          "itrvDetails": {
            "itrvInfoList": [
              {"assessmentYear": "${validYear}"}
            ]
          }
        }
        """
        def cutoff = "01/04/${year}"

        expect:
        service.checkAssessmentYear(tenantId, itrJson, cutoff)
    }

    def "should return true for valid ay in itrDetails"() {
        given:
        def year = getCurrentYear()
        def validYear = String.format("%d-%02d", year, (year + 1) % 100)
        def itrJson = """
        {
          "itrDetails": {
            "itrInfoList": {
              "itr": [
                {"ay": "${validYear}"}
              ]
            }
          }
        }
        """
        def cutoff = "01/04/${year}"

        expect:
        service.checkAssessmentYear(tenantId, itrJson, cutoff)
    }

    def "should return false when no valid assessment year present"() {
        given:
        def year = getCurrentYear()
        def invalidYear = "2000-01"
        def itrJson = """
        {
          "itrvDetails": {
            "itrvInfoList": [
              {"assessmentYear": "${invalidYear}"}
            ]
          }
        }
        """
        def cutoff = "01/04/${year}"

        expect:
        !service.checkAssessmentYear(tenantId, itrJson, cutoff)
    }

    def "should return false for malformed JSON"() {
        given:
        def year = getCurrentYear()
        def itrJson = "not a json"
        def cutoff = "01/04/${year}"

        when:
        def result = false
        try {
            result = service.checkAssessmentYear(tenantId, itrJson, cutoff)
        } catch(Exception e) {
            result = false
        }

        then:
        !result
    }

    def "should handle cutoff date after current date (future cutoff)"() {
        given:
        def year = getCurrentYear()
        def validYear = String.format("%d-%02d", year - 1, year % 100)
        def itrJson = """
        {
          "itrvDetails": {
            "itrvInfoList": [
              {"assessmentYear": "${validYear}"}
            ]
          }
        }
        """
        def cutoff = "01/04/${year + 1}"

        expect:
        service.checkAssessmentYear(tenantId, itrJson, cutoff)
    }

    def "should handle cutoff date before current date (past cutoff)"() {
        given:
        def year = getCurrentYear()
        def validYear = String.format("%d-%02d", year, (year + 1) % 100)
        def itrJson = """
        {
          "itrvDetails": {
            "itrvInfoList": [
              {"assessmentYear": "${validYear}"}
            ]
          }
        }
        """
        def cutoff = "01/04/${year - 1}"

        expect:
        service.checkAssessmentYear(tenantId, itrJson, cutoff)
    }

    def "should return true if any entry in array is valid"() {
        given:
        def year = getCurrentYear()
        def validYear = String.format("%d-%02d", year, (year + 1) % 100)
        def invalidYear = "2000-01"
        def itrJson = """
        {
          "itrvDetails": {
            "itrvInfoList": [
              {"assessmentYear": "${invalidYear}"},
              {"assessmentYear": "${validYear}"}
            ]
          }
        }
        """
        def cutoff = "01/04/${year}"

        expect:
        service.checkAssessmentYear(tenantId, itrJson, cutoff)
    }
} 