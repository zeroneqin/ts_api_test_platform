package io.zeroneqin.base.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class ApiTest implements Serializable {
    private String id;

    private String projectId;

    private String name;

    private String description;

    private String status;

    private String userId;

    private String gitlab;

    private String gitbranch;

    private Long createTime;

    private Long updateTime;

    private String scenarioDefinition;

    private static final long serialVersionUID = 1L;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getGitlab() {
        return gitlab;
    }

    public void setGitlab(String gitlab) {
        this.gitlab = gitlab;
    }

    public String getGitbranch() {
        return gitbranch;
    }

    public void setGitbranch(String gitbranch) {
        this.gitbranch = gitbranch;
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

    public String getScenarioDefinition() {
        return scenarioDefinition;
    }

    public void setScenarioDefinition(String scenarioDefinition) {
        this.scenarioDefinition = scenarioDefinition;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }
}