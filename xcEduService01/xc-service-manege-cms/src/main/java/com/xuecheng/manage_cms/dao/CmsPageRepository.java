package com.xuecheng.manage_cms.dao;

import java.util.List;
import com.xuecheng.framework.domain.cms.CmsPage;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface CmsPageRepository extends MongoRepository<CmsPage,String> {
    CmsPage findByPageName(String pageName);
    CmsPage findByPageNameAndSiteIdAndPageWebPath(String pageName,String siteId,String pageWebPath);

}
