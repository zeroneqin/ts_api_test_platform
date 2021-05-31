package io.zeroneqin.base.domain;

import lombok.Data;

import java.io.Serializable;

@Data
public class TestResource implements Serializable {
    private String id;

    private String testResourcePoolId;

    private String status;

    private Long createTime;

    private Long updateTime;

    private String configuration;

    private static final long serialVersionUID = 1L;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTestResourcePoolId() {
        return testResourcePoolId;
    }

    public void setTestResourcePoolId(String testResourcePoolId) {
        this.testResourcePoolId = testResourcePoolId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    public Long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Long updateTime) {
        this.updateTime = updateTime;
    }

    public String getConfiguration() {
        return configuration;
    }

    public void setConfiguration(String configuration) {
        this.configuration = configuration;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }
}