package com.xuecheng.framework.domain.cms.request;

import lombok.Data;

@Data
public class QueryPageRequest {
    //站点Id
    private String siteId;
    //页面Id
    private String pageId;
    //模板Id
    private String templateId;
    //页面名称
    private String pageName;
    //页面别名
    private String pageAliase;
}
