package com.junit.demo.util

import org.json.JSONObject

class ROIStructureVO {
    String interestCode
    Float interestValue
    Long tenantId
    String uuid
    Float baseRate

    ROIStructureVO() {}

    ROIStructureVO(JSONObject jsonObject) {
        this.interestCode = jsonObject.isNull("code") ? null : jsonObject.getString("code")
        this.interestValue = jsonObject.isNull("value") ? null : jsonObject.getDouble("value")
        this.tenantId = jsonObject.isNull("tenantId") ? null : jsonObject.getLong("tenantId")
        this.uuid = jsonObject.isNull("uuid") ? null : jsonObject.getString("uuid")
        this.baseRate = jsonObject.isNull("baseRate") ? null : jsonObject.getDouble("baseRate")
    }
}
