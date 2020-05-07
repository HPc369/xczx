package com.xuecheng.manage_course.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "course")
public class CourseInfoConfiguration {

    public  String dataUrlPre;

    public  String pagePhysicalPath;

    public  String pageWebPath;

    public  String siteId;

    public  String templateId;

    public  String previewUrl;

    public String getDataUrlPre() {
        return dataUrlPre;
    }

    public void setDataUrlPre(String dataUrlPre) {
        this.dataUrlPre = dataUrlPre;
    }

    public String getPagePhysicalPath() {
        return pagePhysicalPath;
    }

    public void setPagePhysicalPath(String pagePhysicalPath) {
        this.pagePhysicalPath = pagePhysicalPath;
    }

    public String getPageWebPath() {
        return pageWebPath;
    }

    public void setPageWebPath(String pageWebPath) {
        this.pageWebPath = pageWebPath;
    }

    public String getSiteId() {
        return siteId;
    }

    public void setSiteId(String siteId) {
        this.siteId = siteId;
    }

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    public String getPreviewUrl() {
        return previewUrl;
    }

    public void setPreviewUrl(String previewUrl) {
        this.previewUrl = previewUrl;
    }
}
