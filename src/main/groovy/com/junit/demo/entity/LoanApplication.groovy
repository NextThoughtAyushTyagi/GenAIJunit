package com.junit.demo.entity

import lombok.Data
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.time.LocalDate
import java.time.Month
import java.time.ZoneId
import java.time.temporal.ChronoUnit

@Data
class LoanApplication implements Serializable {

    private static final long serialVersionUID = 2L;

    Long id
    BigDecimal loanAmount
    BigDecimal appliedLoanAmount
    Integer loanTenureInMonths = null
    Integer loanTenureInDays = null
    Integer loanTenureInWeeks = null
    Integer appliedLoanTenureInMonths = null
    Integer appliedLoanTenureInDays = null
    Integer appliedLoanTenureInWeeks = null
    BigDecimal interestRate = 0.0
    BigDecimal fakeInterestRate = 0.0
    BigDecimal appliedInterestRate
    BigDecimal monthlyPaymentAmount
    BigDecimal periodicInterestRate
    BigDecimal maxInterestRate
    BigDecimal minLoanAmount
    BigDecimal gapDayInterest = 0.00
    Integer dayOfMonth // gap Day , changed repayment day
    Long auctionPeriod = 0
    Double dailyPenaltyInterest = 0.1
    String uuid = UUID.randomUUID()
    Date startDate = new Date()
    Date endDate
    Boolean sendReminderEmails = true
    Boolean sendReminderSmses = true
    Date dateCreated
    Date lastUpdated
    Long tenantId

//    Contract contract
    LoanPurpose purpose

    String description
    Boolean isDefaulted = Boolean.FALSE
    Boolean isDelayed = Boolean.FALSE
    Boolean isChargedOff = Boolean.FALSE
    Boolean repaymentsViaDsa = Boolean.FALSE
//    LoanSubPurpose loanSubPurpose
    Date dateOfAcceptPolicy
    String termsAcceptedFromIP

    Date applyDate = new Date()
    Date autoDebitApprovalDateByBank
    String repaymentReferenceNoByBank

    String loanDisbursementTransactionId
//    SupportingDocument loanDisbursementSupportingDocument
    Date loanDisbursementDate
    Boolean nationalityApproval = Boolean.FALSE
    Boolean declarationApproval = Boolean.FALSE
    Boolean notificationApproval = Boolean.FALSE
    Boolean profileAuthorizationApproval = Boolean.FALSE
    Boolean seekingApproval = Boolean.FALSE
    Boolean selfConfirmation = Boolean.FALSE
    Boolean isMetaDataSynced = Boolean.FALSE
    BigDecimal creditBureauScore
//    CommonProperty loanDisbursementPaymentMode
    BigDecimal valueOfCollateral
    String typeOfCollateral
    String collateralValuationBasis
    BigDecimal previousInterestRate
    BigDecimal collateralMultiplier
    BigDecimal collateralRate
    String collateralMultiplierType
    Float percentageOfFunding
    String dsaBranchName
    String applicationId
    BigDecimal dsaCommission
    String identityNumberOne
    String identityNumberTwo
    String identifierPrefix

    Boolean tatStatus
    String tatDescription

    //Following Fees are Deprecated please use LoanApplicationFees domain
    Double serviceFee = 0.0
    Double creditScoringFee = 0.00
    BigDecimal legalFee
    BigDecimal assetFee
    BigDecimal reservedFundFee
    BigDecimal additionalFeeOne
    BigDecimal additionalFeeTwo
    BigDecimal additionalFeeThree
    BigDecimal additionalFeeFour
    BigDecimal adminFee
    BigDecimal insuranceFee = 0
    BigDecimal serviceFeeTaxOne

    BigDecimal fullSettlementDiscount
    BigDecimal fullSettlementPenalty
    BigDecimal monthlyPenaltyPercentage
    String uniqueReferenceID
    Boolean repaymentsUploadedViaExcel = false
    Long buyerId
    Boolean isFldg = false
    Boolean isCreatedByDocument = false
    String idPerTenant
    Integer holidayRepaymentPeriodMonths
    Boolean isNachEnabled

    BigDecimal riskScore
    Date appointmentDateTime
    Date auctionStartDate
    Integer gracePeriod

    BigDecimal componentFromRepaymentInterest = 0.0

    BigDecimal registrationFee
    BigDecimal discountPercentage
    BigDecimal purchaseAmount
    BigDecimal interestChargedToBorrower
    Integer advanceEmiNumber = 0
    Integer repaymentStartDate
    String investorUuid
    Long loanProgramId
    BigDecimal discountOnProcessingFees = 0.00
    BigDecimal totalCalculatedFees = 0.00
    BigDecimal totalCalculatedTax = 0.00
    String adminUuid
    String userHierarchyUnitUuid          //processing branch
    String processingCentreUnitUuid       //processing centre
    String kycBranchUnitUuid              //kyc branch
    Integer ownershipIndicator
    Boolean isOverdue = Boolean.FALSE
    String loanApplicationGroupUuid
    String crmLeadId
    String nationalPortalId
    String nationalPortalText
    String borrowerEmploymentTypeUuid  // Containing CommonProperty Uuid

    String purposeOfAdvance

    Date crmLeadGenerationDate
    String parkingBranchUnitUuid
    Boolean isNomineeOpted = Boolean.FALSE

    Set<SupportingDocument> supportingDocuments

    String productType
    String originationName
    Boolean isAllDocumentUploaded = Boolean.FALSE


    public LoanApplication() {}

//
    BigDecimal serviceFeeWithTaxAfterDiscount() {
        this?.totalFeesWithTax() - (this?.discountOnProcessingFees ?: 0.00)
    }

    BigDecimal totalFeesWithTax() {
        (totalFees + totalTax1()) ?: 0.00
    }

    BigDecimal totalTax1() {
        return totalCalculatedTax ?: LoanApplicationFee.totalTax(this)
    }

//    String getLoanPurposeUuid() {
//        LoanApplicationService loanApplicationService=(LoanApplicationService) ApplicationContextProvider.getApplicationContext().getBean("loanApplicationService")
//        return loanApplicationService.getLoanPurposeUuid(this?.tenantId,this?.purpose?.id)
//    }

    Date getAuctionEndDate() {
        Date date = this?.auctionStartDate ? this.auctionStartDate : this.startDate
        if (date && this.auctionPeriod)
            date = DateUtil.plus(date, this.auctionPeriod as int)
        return date
    }

    int getAge() {
        (new Date() - this.getApplyDate()) as int
    }

    Date getApplyDate() {
        this.applyDate ?: this.dateCreated
    }

    String getLoanId() {
        return this.identityNumberOne ?: ''
    }

    String getLoanPurposeUuid() {
        return this?.purpose?.uuid
    }

    BigDecimal fetchUpdatedPurchaseAmount() {
        return (interestChargedToBorrower) ? ((interestChargedToBorrower / 100) * purchaseAmount + purchaseAmount) : purchaseAmount
    }

    BigDecimal fetchServiceFee(Boolean withTax = false) {
        return 0
    }

    Boolean isNpa() {
        return this.loanApplicationStatus in Enums.LoanApplicationStatus.npaLoanStatuses()
    }

    Boolean isBeyondAuctionStage() {
        (this.loanApplicationStatus in Enums.LoanApplicationStatus.fundedAndAfterWards())
    }

    def copyLoanDetail(LoanApplication loanApplication) {
        if (loanApplication) {
            this.creditScoringFee = loanApplication.creditScoringFee
            this.dailyPenaltyInterest = loanApplication.dailyPenaltyInterest
            this.dateCreated = new Date()
            this.lastUpdated = new Date()
            this.serviceFee = loanApplication.serviceFee
            this.startDate = loanApplication.startDate
            this.tenantId = loanApplication.tenantId
            this.loanAmount = loanApplication.loanAmount
            this.appliedLoanAmount = loanApplication.appliedLoanAmount
            this.loanTenureInMonths = loanApplication.loanTenureInMonths
            this.loanTenureInDays = loanApplication.loanTenureInDays
            this.loanTenureInWeeks = loanApplication.loanTenureInWeeks
            this.appliedLoanTenureInMonths = loanApplication.appliedLoanTenureInMonths
            this.appliedLoanTenureInDays = loanApplication.appliedLoanTenureInDays
            this.appliedLoanTenureInWeeks = loanApplication.appliedLoanTenureInWeeks
            this.interestRate = loanApplication.interestRate
            this.fakeInterestRate = loanApplication.fakeInterestRate
            this.appliedInterestRate = loanApplication.appliedInterestRate
            this.monthlyPaymentAmount = loanApplication.monthlyPaymentAmount
            this.periodicInterestRate = loanApplication.periodicInterestRate
            this.maxInterestRate = loanApplication.maxInterestRate
            this.minLoanAmount = loanApplication.minLoanAmount
            this.gapDayInterest = loanApplication.gapDayInterest
            this.dayOfMonth = loanApplication.dayOfMonth
            this.auctionPeriod = loanApplication.auctionPeriod
            this.endDate = loanApplication.endDate
            this.sendReminderEmails = loanApplication.sendReminderEmails
            this.sendReminderSmses = loanApplication.sendReminderSmses
            this.city = loanApplication.city
            this.dsaAgent = loanApplication.dsaAgent
            this.description = loanApplication.description
            this.isDefaulted = loanApplication.isDefaulted
            this.isDelayed = loanApplication.isDelayed
            this.isChargedOff = loanApplication.isChargedOff
            this.repaymentsViaDsa = loanApplication.repaymentsViaDsa
            this.dateOfAcceptPolicy = loanApplication.dateOfAcceptPolicy
            this.termsAcceptedFromIP = loanApplication.termsAcceptedFromIP
            this.termsAcceptedByAgent = loanApplication.termsAcceptedByAgent
            this.applyDate = loanApplication.applyDate
            this.autoDebitApprovalDateByBank = loanApplication.autoDebitApprovalDateByBank
            this.repaymentReferenceNoByBank = loanApplication.repaymentReferenceNoByBank
            this.loanDisbursementBankDetail = loanApplication.loanDisbursementBankDetail
            this.adminBankDetail = loanApplication.adminBankDetail
            this.loanDisbursementTransactionId = loanApplication.loanDisbursementTransactionId
            this.loanDisbursementDate = loanApplication.loanDisbursementDate
            this.nationalityApproval = loanApplication.nationalityApproval
            this.declarationApproval = loanApplication.declarationApproval
            this.notificationApproval = loanApplication.notificationApproval
            this.profileAuthorizationApproval = loanApplication.profileAuthorizationApproval
            this.seekingApproval = loanApplication.seekingApproval
            this.selfConfirmation = loanApplication.selfConfirmation
            this.isMetaDataSynced = loanApplication.isMetaDataSynced
            this.creditBureauScore = loanApplication.creditBureauScore
            this.valueOfCollateral = loanApplication.valueOfCollateral
            this.typeOfCollateral = loanApplication.typeOfCollateral
            this.collateralValuationBasis = loanApplication.collateralValuationBasis
            this.previousInterestRate = loanApplication.previousInterestRate
            this.collateralMultiplier = loanApplication.collateralMultiplier
            this.collateralRate = loanApplication.collateralRate
            this.collateralMultiplierType = loanApplication.collateralMultiplierType
            this.percentageOfFunding = loanApplication.percentageOfFunding
            this.dsaBranchName = loanApplication.dsaBranchName
            this.dsaCommission = loanApplication.dsaCommission
            this.identifierPrefix = loanApplication.identifierPrefix
            this.borrowerType = loanApplication.borrowerType
            this.legalFee = loanApplication.legalFee
            this.assetFee = loanApplication.assetFee
            this.reservedFundFee = loanApplication.reservedFundFee
            this.additionalFeeOne = loanApplication.additionalFeeOne
            this.additionalFeeTwo = loanApplication.additionalFeeTwo
            this.additionalFeeThree = loanApplication.additionalFeeThree
            this.additionalFeeFour = loanApplication.additionalFeeFour
            this.adminFee = loanApplication.adminFee
            this.insuranceFee = loanApplication.insuranceFee
            this.serviceFeeTaxOne = loanApplication.serviceFeeTaxOne
            this.fullSettlementDiscount = loanApplication.fullSettlementDiscount
            this.fullSettlementPenalty = loanApplication.fullSettlementPenalty
            this.monthlyPenaltyPercentage = loanApplication.monthlyPenaltyPercentage
            this.uniqueReferenceID = loanApplication.uniqueReferenceID
            this.repaymentsUploadedViaExcel = loanApplication.repaymentsUploadedViaExcel
            this.buyerId = loanApplication.buyerId
            this.isFldg = loanApplication.isFldg
            this.isCreatedByDocument = loanApplication.isCreatedByDocument
            this.idPerTenant = loanApplication.idPerTenant
            this.holidayRepaymentPeriodMonths = loanApplication.holidayRepaymentPeriodMonths
            this.isNachEnabled = loanApplication.isNachEnabled
            this.directSellingAssociate = loanApplication.directSellingAssociate
            this.riskScore = loanApplication.riskScore
            this.appointmentDateTime = loanApplication.appointmentDateTime
            this.auctionStartDate = loanApplication.auctionStartDate
            this.gracePeriod = loanApplication.gracePeriod
            this.repaymentType = loanApplication.repaymentType
            this.componentFromRepaymentPrincipal = loanApplication.componentFromRepaymentPrincipal
            this.componentFromRepaymentPrincipalType = loanApplication.componentFromRepaymentPrincipalType
            this.componentFromRepaymentInterest = loanApplication.componentFromRepaymentInterest
            this.componentFromRepaymentInterestType = loanApplication.componentFromRepaymentInterestType
            this.registrationFee = loanApplication.registrationFee
            this.discountPercentage = loanApplication.discountPercentage
            this.purchaseAmount = loanApplication.purchaseAmount
            this.interestChargedToBorrower = loanApplication.interestChargedToBorrower
            this.advanceEmiNumber = loanApplication.advanceEmiNumber
            this.repaymentStartDate = loanApplication.repaymentStartDate
            this.discountOnProcessingFees = loanApplication.discountOnProcessingFees
            this.totalCalculatedFees = loanApplication.totalCalculatedFees
            this.totalCalculatedTax = loanApplication.totalCalculatedTax
            this.adminUuid = loanApplication.adminUuid
            this.userHierarchyUnitUuid = loanApplication.userHierarchyUnitUuid
            this.processingCentreUnitUuid = loanApplication.processingCentreUnitUuid
            this.kycBranchUnitUuid = loanApplication.kycBranchUnitUuid
            this.ownershipIndicator = loanApplication.ownershipIndicator
            this.isOverdue = loanApplication.isOverdue
            this.loanApplicationGroupUuid = loanApplication.loanApplicationGroupUuid
            this.crmLeadId = loanApplication.crmLeadId
            this.nationalPortalId = loanApplication.nationalPortalId
            this.nationalPortalText = loanApplication.nationalPortalText
            this.borrowerEmploymentTypeUuid = loanApplication.borrowerEmploymentTypeUuid
            this.purposeOfLoanType = loanApplication.purposeOfLoanType
            this.purposeOfAdvance = loanApplication.purposeOfAdvance
            this.applicationSource = loanApplication.applicationSource
            this.originationSource = loanApplication.originationSource
            this.crmLeadGenerationDate = loanApplication.crmLeadGenerationDate
            this.parkingBranchUnitUuid = loanApplication.parkingBranchUnitUuid
            this.isNomineeOpted = loanApplication.isNomineeOpted
            this.rootUser = loanApplication.rootUser
            this.loanDisbursementPaymentMode = loanApplication.loanDisbursementPaymentMode
            this.productType = loanApplication.productType
        }
        return this
    }

    public Integer fetchRemainingTenureForInterestIncome(Date date) {
        LocalDate applicationCreatedDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
        LocalDate currentDate = LocalDate.now()
        LocalDate endDate
        Double difference
        endDate = LocalDate.of(currentDate.getYear(), Month.MARCH, 31);
        if (currentDate.isAfter(endDate)) {
            endDate = endDate.plusYears(1)
        }
        difference = ChronoUnit.DAYS.between(applicationCreatedDate, endDate)
        return difference >= 0 ? difference : 0
    }

    String getOriginationNameWhenValueNotPresent(String applicationSource) {
        if (!applicationSource)
            return null
        if (applicationSource == Enums.ApplicationSource.WEB_JOURNEY.toString()) {
            return "EPW"
        } else if (applicationSource == Enums.ApplicationSource.BRANCH_PORTAL.toString()) {
            return "EPB"
        } else if (applicationSource in Enums.ApplicationSource.getProcessingSourceNotApplicationSource()*.toString()) {
            return "EPB"
        } else {
            return applicationSource
        }
    }


}
