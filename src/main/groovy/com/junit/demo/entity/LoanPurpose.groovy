package com.junit.demo.entity

import lombok.Data


@Data
public class LoanPurpose implements Serializable {

    Long id
    private static final long serialVersionUID = 2L

    Date dateCreated
    Date lastUpdated
    //TODO : To be deleted, don't use it anywhere, instead use "repaymentTypesAllowed" var
    String description
    Double serviceFeesPercent //TODO : Deleted as we use fee structure now.
    String purpose
    Long minDurationInMonths
    Long maxDurationInMonths
    Long tenantId
    Long minimumScore
    Boolean forIndividual = true
    Boolean forCorporate = true
    Boolean forGroup = false
    Float minInterestRate = 5.00
    Float maxInterestRate = 36.00
    BigDecimal loanMinBid
    BigDecimal loanMaxBid
    Integer loanTenureIncrementalFactor
    Integer loanProductIndex
    String uuid = UUID.randomUUID().toString()
    Double creditScoringFee = 0
    BigDecimal minLoanAmount
    BigDecimal maxLoanAmount
    BigDecimal firstTimeMaxLoanAmount
    Boolean isActive = true
    Integer auctionPeriod = 7
    BigDecimal monthlyPenaltyPercentage = 0
    Integer gracePeriodInDays = null
    String alias
    String termsAndConditionContent
    Integer repaymentStartDate = 7
    Integer repaymentStartDate2
    Integer repaymentStartDate3
    BigDecimal baseInterestRate
    Boolean isInternal = false
    Integer delayedLoanThreshold
    Integer defaultedLoanThreshold
    Integer chargedOffLoanThreshold
    Boolean isGuarantorAllowed = false
    Boolean isBorrowerReferenceAllowed = false
    Boolean isGuarantorEmploymentDetailAllowed = false
    Boolean isOtherLoanAllowed = false
    String code
    Boolean allowMoreBidInterestRateThanLoanInterestRate = false
    Integer repaymentStartDateForMonthJump = 15
    Integer holidayRepaymentPeriod
    Boolean isBorrowerInsuranceDetailAllowed = false
    Boolean isBorrowerVehicleDetailsAllowed = false
    Boolean isBorrowerFamilyMembersDetailAllowed = false
    Boolean isTenureReducingByOneMonth = false
    Boolean isFakeInterestRateAllowed = false
    String creditBureauCode

    Integer nachToDate
    String nachPresentationAllowedLoanStatus
    BigDecimal nachMaxAmountPercentage
    Boolean isRepaymentAllowedWithoutInterestRate = false
    Boolean payRepaymentWithoutPenalty = false    // allow repayment to mark as {PAID} without paying penalty

    Boolean isPartialPaymentDisabled = false    // allow loan product wise setting to disable partial payment
    Integer minimumDaysBetweenDisbursedDateAndFirstEmi = 30

    String repaymentTypesAllowed
    Boolean isCompanyRepresentativeAllowed = false
    Integer advanceEmiNumber
    Boolean enableBorrowerOnboarding = false
    Boolean applyGracePeriodOnExistingCalculatedPenaltyRepayment = false
    Boolean sendNotificationForPaidDisbursedFee = true
    Boolean sendNotificationForPaidGapDay = true
    String introVideoUrl
    String processToFollowPath
    String requiredDocumentDetailsPath
    String eligibilityDetailsPath
    String iconPath
    String iconContentType

    String featuresPath
    String bannerPath
    String bannerContentType
    Integer numOfMonthsToIncludeInterestInForeClosure
    Integer repaymentStartDate4
    Integer repaymentStartDate5
    Integer noOfNominees = 0
    Integer maxNoOfPdcItems = 0
    String flowableBpmnId

    String landingUrlForExternalSource
    String finacleRequest
    Boolean enableLoanApplicationGroup
    String loanApplicationIdentifier
    String documentPath;
    String crmLeadCode

    String rateOfInterestCode                     //TODO : Need to remove this field
    String allowedAssetTypes

    String allowedRoiTypesForLoan
    Float defaultInterestRate
    Float interestRateWebJourney

    String allowedInterestRateCodes
    Integer noOfDaysAfterWhichNewLoanCanBeAppliedWithSameMobile
    String crmIds
    Boolean isLimitApprovalEnabled = false
    Integer minAddOnCardDetails
    Integer maxAddOnCardDetails
    String journeyType
    String allowedSource
    String assessmentMethods
    Integer maxVehicleDetails = 0
    Integer allowedMaximumFacility

    Boolean enableEsignOnBranchPortal = false

    Boolean enableParkingBranchFeature = false
    String svgIcon
    String loanApplicationCrmDropoutStatus
    Boolean fetchProgramPageSort = false

    Boolean fetchProgramTabSort = false
    Long version
    String eSignCharges
    Boolean maxEligibleAmountValidationFromBre = false
    Boolean isScoreCardDisabled = false
    String accountCoolingPeriod
    Boolean isNpaCheck
    Boolean isRenewalProduct
    Integer maxJointAccountDetails
    Integer minJointAccountDetails
    String hideAssignLoanToDsaForStatuses
    String hideAssignLoanToCheckerForStatuses
    String multipleEndStatusForWebJourney

    Boolean disableCustomerProfiling = false
    Integer tatDays = 0
    Integer maxAuthorizedSignatories


    String restrictPanAndAadharValidationForEntities
    Boolean allowComparison = false
    String schemeComparisonKeys
    String statusesToGenerateContractButton
    String statusesToHtmlApiTriggering

    String mandatoryParkingBranchStatus
    String limitApprovalValidationStatus       //multi select loan status
    String limitApprovalValidationAmountType  //amount type
    Integer maxNumberOfProperty = 0

    LoanPurpose() {
    }

    public LoanPurpose(String uuid, Boolean enableBorrowerOnboarding) {
        this.uuid = uuid
        this.enableBorrowerOnboarding = enableBorrowerOnboarding
    }

    List<String> repaymentTypesAllowedList() {
        return (this.repaymentTypesAllowed ? this.repaymentTypesAllowed.split(",") : [])
    }

    String toString() {
        return this.id?.toString()
    }

    List<String> selectedLimitApprovalValidationStatus() {
        List<String> selectedStatus = this?.limitApprovalValidationStatus?.split(",") ?: []
        return selectedStatus
    }

}
