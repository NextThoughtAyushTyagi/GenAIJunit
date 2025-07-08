package com.junit.demo.entity


public class PropertyDetail implements Serializable {

    Long id
    Long tenantId
    Date dateCreated
    Date lastUpdated
    String uuid = UUID.randomUUID()
    String loanUuid

}
