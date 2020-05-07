package com.xuecheng.manage_cms_client.mq;

import com.alibaba.fastjson.JSON;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.manage_cms_client.service.PageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ConsumerPostPage {
    private static final Logger LOGGER = LoggerFactory.getLogger(PageService.class);
    @Autowired
    PageService pageService;
    @RabbitListener(queues = {"queue_cms_postpage_01"})
    public void postPage(String msg){
        Map map = JSON.parseObject(msg, Map.class);
        String pageId = (String)map.get("pageId");
        CmsPage cmspage = pageService.findCmspageById(pageId);
        if (cmspage == null){
            LOGGER.error("receive postpage msg,cmsPage is null,pageId:{}",pageId);
            return;
        }
        pageService.savePageToServerPath(pageId);
    }
}
