package com.junit.demo.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.SequenceGenerator
import jakarta.persistence.Table
import jakarta.persistence.Version
import lombok.Data
import org.hibernate.annotations.NotFound
import org.hibernate.annotations.NotFoundAction
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import org.springframework.web.multipart.MultipartFile

@Entity
@Table(name = "SUPPORTING_DOCUMENT")
@Data
class SupportingDocument implements Serializable {


    private static final long serialVersionUID = 2L;
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_SUPPORTING_DOCUMENT")
    @SequenceGenerator(name = "SEQ_SUPPORTING_DOCUMENT", sequenceName = "SEQ_SUPPORTING_DOCUMENT", allocationSize = 1)
    Long id
    Long tenantId
    @CreatedDate
    Date dateCreated
    @LastModifiedDate
    Date lastUpdated
    String uuid = UUID.randomUUID()
    String name
    String note
    String path
    @Column(name = "passcode")
    String password
    String contentType
    String mediumImagePath
    String originalImagePath
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "LOAN_APPLICATION_ID", referencedColumnName = "ID")
    LoanApplication loanApplication
    Boolean visibleToDSA
    Boolean visibleToBorrower
    Boolean visibleToInvestor
    Boolean visibleToFia = false
    Boolean isPublic = false
    Boolean collateral = false
    Boolean isSuccessfullyVerifiedByPerfios = false
    String perfiosTransactionId
    String perfiosResponse
    String clienttransactionId
    String perfiosCategoryName
    String fin360AccountUID

    Reference reference
    String documentStatus
    String ocrVerificationStatus
    Boolean fileDoesNotExist
    Date documentUploadedExternalStorage

    Date encryptionDate = new Date()
    Long loanProgramId
    String perfiosGeneratedLinkId
    String loanQuestionUuid
    String assetUuid
    String groupMemberDetailsUuid
    String dmsDocumentIndex
    String perfiosErrorResponse
    @Version
    Long version
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "LOAN_DISBURSEMENT_ID", referencedColumnName = "id")
//    LoanDisbursement loanDisbursement
//    @OneToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "BID_ID", referencedColumnName = "id")
//    Bid bid

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "AGENT_ID", referencedColumnName = "ID", insertable = false, updatable = false)
//    DirectSellingAssociate directSellingAssociate
    Boolean isItrAssessmentYearValid
    Boolean isGstValidForAssessment


    String toString() {
        return this.id?.toString()
    }

    SupportingDocument() {

    }


    String existingDocsNameByApplicantAndCategory() {
        List<SupportingDocument> supportingDocumentList = this.relatedSupportingDocuments
        return (supportingDocumentList ? supportingDocumentList*.name?.join(", ") : "")
    }

    /** fetch all loan documents for same applicant and category as {this}*/
    List<SupportingDocument> getRelatedSupportingDocuments() {
        List<SupportingDocument> supportingDocumentList = fetchSupportingDocumentsByApplicantAndCategory(
                this.loanApplication, this.documentCategory, this.companyRepresentative, this.coApplicant, this.reference
        )
        return supportingDocumentList
    }

    SupportingDocument(String fileName, SupportingDocument supportingDocument, Map params, String perfiosTransactionId = null) {
        this.name = fileName
        this.loanApplication = supportingDocument?.loanApplication
        this.tenantId = supportingDocument?.tenantId
        this.documentCategory = supportingDocument?.documentCategory
        this.password = params?.password as String ?: null
        this.perfiosTransactionId = supportingDocument?.perfiosTransactionId ?: perfiosTransactionId
        this.visibleToBorrower = params?.visibleToBorrower as Boolean ?: false
        this.visibleToInvestor = params?.visibleToInvestor as Boolean ?: false
        this.visibleToDSA = params?.visibleToDSA as Boolean ?: false
        this.perfiosResponse = supportingDocument?.perfiosResponse

        if (supportingDocument?.companyRepresentative) {
            this.companyRepresentative = supportingDocument?.companyRepresentative
        } else if (supportingDocument?.coApplicant) {
            this.coApplicant = supportingDocument?.coApplicant
        } else {
            this.user = supportingDocument?.user
            this.agent = supportingDocument?.agent
        }

        this.documentType = Enums.DocumentType.REPORT_ANALYSIS
        this.aliasId = supportingDocument?.aliasId
    }

    SupportingDocument(MultipartFile multipartFile, String docFileSystemPath, Boolean isPublic, LoanApplication loanApplication) {
        this.name = multipartFile.originalFilename
        this.contentType = multipartFile.contentType
        this.user = loanApplication?.borrower?.fetchUser()
        this.agent = AppUtil.currentAgentFromSession()
        this.loanApplication = loanApplication
        this.tenantId = loanApplication?.tenantId
        this.path = this.uuid
        this.isPublic = isPublic ?: false
        File file = new File(docFileSystemPath)
        if (!file.exists())
            file.mkdirs()
        def clt = ApplicationContextProvider.getApplicationContext().getBean(DocumentService.class)
        if (!clt.uploadDocument(filePathForUpload: "${docFileSystemPath}", fileName: this.path, multipartFile: multipartFile)) {
            this.name = null
            this.encryptionDate = null
        }
        if (!TenantConfiguration.fetchObjectPropertyValue(TenantConfigurationType.ENABLE_FILE_ENCRYPTION, this.tenant)) {
            this.encryptionDate = null
        }
    }
}
