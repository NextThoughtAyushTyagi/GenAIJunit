package com.junit.demo.service

import spock.lang.Specification
import com.junit.demo.service.LoanApplicationService
import com.junit.demo.util.ROIStructureVO
import java.time.Year

class LoanApplicationServiceSpec extends Specification {
    def service = new LoanApplicationService()
    def tenantId = 1L
    static int getCurrentYear() { java.time.Year.now().value }

} 