package com.junit.demo.entity

import lombok.Data

import java.text.DateFormat
import java.text.SimpleDateFormat

@Data
class CoApplicant implements Serializable {

    Long id
    Long tenantId
    Date dateCreated
    Date lastUpdated
    String uuid = UUID.randomUUID()
    String firstName
    String middleName
    String lastName
    String coApplicantEmploymentType
    String mobileNumber
    String email
    String fbLink
    String gmailLink
    Date dateOfBirth
    String relationshipStatus
    String purposeOfOutStandingEmi
    String residentOwnershipType
    Integer noOfDependent
    BigDecimal monthlyRentalIncome
    BigDecimal monthlyRentalPayable
    BigDecimal monthlyOutStandingEmi

    String category
    String fatherOrHusbandName
    String motherName
    String spouseName
    String citizenship
    String alternativeNumber
    String occupation
    String maritalStatus
    String gender
    String employmentStatusOfSpouse
    BigDecimal creditBureauScore
    String mobileNumberCountryCode
    String alternativeNumberCountryCode
    String casteCategory
    String educationType
    String title
    Date identityNumberOneIssueDate
    Date identityNumberTwoIssueDate
    Date identityNumberThreeIssueDate
    Date identityNumberFourIssueDate
    Date identityNumberFiveIssueDate
    Date identityNumberSixIssueDate
    Double shareholdingPercentage

    Date identityNumberOneExpiryDate
    Date identityNumberTwoExpiryDate
    Date identityNumberThreeExpiryDate
    Date identityNumberFourExpiryDate
    Date identityNumberFiveExpiryDate
    Date identityNumberSixExpiryDate
    Boolean isGuarantor

    String identityNumberOneType
    String identityNumberTwoType
    String identityNumberThreeType
    String identityNumberFourType
    String identityNumberFiveType
    String identityNumberSixType
    Boolean isIdentityNumberOneVerified
    Boolean isIdentityNumberOnePhysicallyVerified
    Boolean isIdentityNumberTwoVerified
    Boolean isIdentityNumberTwoPhysicallyVerified
    Boolean isIdentityNumberFiveVerified
    Boolean isIdentityNumberFivePhysicallyVerified
    Boolean isMobileNumberVerified

    String disabledFields
    String nationality

    Boolean isExistingUser



    public String getFullName() {
        return (this.firstName ? "${this.firstName}" : '') + (this.middleName ? " ${this.middleName}" : '') + (this.lastName ? " ${this.lastName}" : '')
    }


    CoApplicant() {}



    public String getFullNameWithTitle() {
        if (this.title) {
            return (this.title ? "${this.title}" : '') + (this.firstName ? " ${this.firstName}" : '') + (this.middleName ? " ${this.middleName}" : '') + (this.lastName ? " ${this.lastName}" : '')
        } else {
            return this.fullName
        }
    }

    String getCompleteMobileNumber() {
        (this.mobileNumberCountryCode ?: '') + (this.mobileNumber ?: '')
    }
}
