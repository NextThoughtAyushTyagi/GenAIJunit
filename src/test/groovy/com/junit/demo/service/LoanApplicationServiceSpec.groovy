package com.junit.demo.service

import spock.lang.Specification
import com.junit.demo.service.LoanApplicationService
import com.junit.demo.util.ROIStructureVO
import java.time.Year

class LoanApplicationServiceSpec extends Specification {
    def service = new LoanApplicationService()
    def tenantId = 1L
    static int getCurrentYear() { java.time.Year.now().value }

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