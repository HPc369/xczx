package com.xuecheng.manage_course.dao;

import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.response.CmsPageResult;
import com.xuecheng.manage_course.client.CmsPageClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
public class TestFeign {

    @Autowired
    CmsPageClient cmsPageClient;

    @Test
    public void testFeign(){
        CmsPage cmsPage = new CmsPage();
        cmsPage.setSiteId("4028e581617f945f01617f9dabc40000");
        cmsPage.setDataUrl("http://localhost:31200/course/courseview/4028e581617f945f01617f9dabc40000");
        cmsPage.setPageName("4028e581617f945f01617f9dabc40000.html");
        cmsPage.setPageAliase("bootstrap开发框架");
        cmsPage.setPagePhysicalPath("/course/detail/");
        cmsPage.setPageWebPath("/course/detail/");
        cmsPage.setTemplateId("5eae7b56fe83b513bc0fb436");

        CmsPageResult cmsPageResult = cmsPageClient.saveCmsPage(cmsPage);
        System.out.println(cmsPageResult);
    }

}
