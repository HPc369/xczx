package com.xuecheng.manage_cms.service;

import com.alibaba.fastjson.JSON;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.CmsSite;
import com.xuecheng.framework.domain.cms.CmsTemplate;
import com.xuecheng.framework.domain.cms.request.QueryPageRequest;
import com.xuecheng.framework.domain.cms.response.CmsCode;
import com.xuecheng.framework.domain.cms.response.CmsPageResult;
import com.xuecheng.framework.domain.cms.response.CmsPostPageResult;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.*;
import com.xuecheng.manage_cms.config.RabbitmqConfig;
import com.xuecheng.manage_cms.dao.CmsConfigRepository;
import com.xuecheng.manage_cms.dao.CmsPageRepository;
import com.xuecheng.manage_cms.dao.CmsSiteRepository;
import com.xuecheng.manage_cms.dao.CmsTemplateRepository;
import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.client.RestTemplate;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@Service
public class PageService {
    @Autowired
    private CmsPageRepository repository;

    @Autowired
    private CmsConfigRepository cmsConfigRepository;

    @Autowired
    private CmsTemplateRepository templateRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    GridFsTemplate gridFsTemplate;

    @Autowired
    GridFSBucket gridFSBucket;

    @Autowired
    private CmsPageRepository cmsPageRepository;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private CmsSiteRepository cmsSiteRepository;

    public QueryResponseResult findList(int page, int size, QueryPageRequest pageRequest) {
        if (pageRequest == null){
            pageRequest = new QueryPageRequest();
        }
        CmsPage cmsPage = new CmsPage();
        ExampleMatcher exampleMatcher = ExampleMatcher.matching();
        if (StringUtils.isNoneEmpty(pageRequest.getSiteId())){
            cmsPage.setSiteId(pageRequest.getSiteId());
        }
        if (StringUtils.isNoneEmpty(pageRequest.getPageAliase())){
            cmsPage.setPageAliase(pageRequest.getPageAliase());
            exampleMatcher = exampleMatcher.withMatcher("pageAliase", ExampleMatcher.GenericPropertyMatchers.contains());
        }
        if (StringUtils.isNoneEmpty(pageRequest.getPageId())){
            cmsPage.setPageId(pageRequest.getPageId());
        }
        if (StringUtils.isNoneEmpty(pageRequest.getPageName())){
            cmsPage.setPageName(pageRequest.getPageName());
            exampleMatcher = exampleMatcher.withMatcher("pageName", ExampleMatcher.GenericPropertyMatchers.contains());
        }
        if (StringUtils.isNoneEmpty(pageRequest.getTemplateId())){
            cmsPage.setTemplateId(pageRequest.getTemplateId());
        }

        Example<CmsPage> example = Example.of(cmsPage,exampleMatcher);
        if (page <= 0){
            page = 1;
        }
        page = page - 1;
        if (size <= 0){
            size = 10;
        }
        Pageable pageable = PageRequest.of(page,size);
        Page<CmsPage> all = repository.findAll(example,pageable);
        QueryResult result = new QueryResult();
        result.setList(all.getContent());
        result.setTotal(all.getTotalElements());
        QueryResponseResult queryResponseResult = new QueryResponseResult(CommonCode.SUCCESS,result);
        return queryResponseResult;
    }


    public CmsPageResult add(CmsPage cmsPage) {
        if (cmsPage == null){
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_DATAURLISNULL);
        }
        CmsPage cmsPage1 = repository.findByPageNameAndSiteIdAndPageWebPath(cmsPage.getPageName(), cmsPage.getSiteId(), cmsPage.getPageWebPath());
        if (cmsPage1 != null){
            ExceptionCast.cast(CmsCode.CMS_ADDPAGE_EXISTSNAME);
        }

        cmsPage.setPageId(null);
        repository.save(cmsPage);
        return new CmsPageResult(CommonCode.SUCCESS,cmsPage);

       // return new CmsPageResult(CommonCode.FAIL,null);

    }

    public CmsPage findById(String id) {
        Optional<CmsPage> pageOptional = repository.findById(id);
        CmsPage cmsPage = null;
        if (pageOptional.isPresent()){
            cmsPage = pageOptional.get();
        }
        return cmsPage;
    }

    public CmsPageResult edit(String id, CmsPage cmsPage1) {
        CmsPage queryPage = findById(id);
        if (queryPage == null){
            ExceptionCast.cast(CmsCode.CMS_FINDPAGE_FAILER);
        }
        queryPage.setPageAliase(cmsPage1.getPageAliase());
        queryPage.setPageName(cmsPage1.getPageName());
        queryPage.setTemplateId(cmsPage1.getTemplateId());
        queryPage.setSiteId(cmsPage1.getSiteId());
        queryPage.setDataUrl(cmsPage1.getDataUrl());
        queryPage.setPageWebPath(cmsPage1.getPageWebPath());
        queryPage.setPagePhysicalPath(cmsPage1.getPagePhysicalPath());
        CmsPage save = repository.save(queryPage);
        if (save == null){
            ExceptionCast.cast(CmsCode.CMS_ADDPAGE_FAILER);
        }
        return new CmsPageResult(CommonCode.SUCCESS, save);
    }

    public ResponseResult delete(String id) {
        Optional<CmsPage> byId = repository.findById(id);
        if (byId.isPresent()){
            CmsPage cmsPage = byId.get();
            repository.deleteById(id);
            return new ResponseResult(CommonCode.SUCCESS);
        }else {
            ExceptionCast.cast(CmsCode.CMS_FINDPAGE_FAILER);
        }
        return new ResponseResult(CommonCode.FAIL);
    }

    //页面静态化方法
    public  String getPageHtml(String pageId){
        Map model = getModelByPageId(pageId);
        if (model == null){
            //数据模型获取失败
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_DATAISNULL);
        }
        //获取页面模板
        String template = getTemplateByPageId(pageId);
        if (StringUtils.isEmpty(template)){
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_TEMPLATEISNULL);
        }
        String html = generateHtml(template, model);
        return html;

    }

    //执行静态化
    private String generateHtml(String templateContent,Map model){
        Configuration configuration = new Configuration(Configuration.getVersion());
        StringTemplateLoader stringTemplateLoader = new StringTemplateLoader();
        stringTemplateLoader.putTemplate("template",templateContent);
        configuration.setTemplateLoader(stringTemplateLoader);
        try {
            Template template = configuration.getTemplate("template");
            String content = FreeMarkerTemplateUtils.processTemplateIntoString(template, model);
            return content;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    //获取数据模型信息
    private Map getModelByPageId(String pageId){
        CmsPage cmsPage = this.findById(pageId);
        if (cmsPage == null){
            ExceptionCast.cast(CmsCode.CMS_FINDPAGE_FAILER);
        }
        String dataUrl = cmsPage.getDataUrl();
        if (StringUtils.isEmpty(dataUrl)){
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_DATAURLISNULL);
        }
        ResponseEntity<Map> forEntity = restTemplate.getForEntity(dataUrl, Map.class);
        Map body = forEntity.getBody();
        return body;


    }

    //获取模板信息
    private String getTemplateByPageId(String pageId){
        CmsPage cmsPage = this.findById(pageId);
        if (cmsPage == null){
            ExceptionCast.cast(CmsCode.CMS_FINDPAGE_FAILER);
        }
        String templateId = cmsPage.getTemplateId();
        if (StringUtils.isEmpty(templateId)){
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_TEMPLATEISNULL);
        }
        //获取模板信息
        Optional<CmsTemplate> byId = templateRepository.findById(templateId);
        if (byId.isPresent()){
            CmsTemplate cmsTemplate = byId.get();
            //获取模板文件id
            String templateFileId = cmsTemplate.getTemplateFileId();
            //从GridFS中取模板文件的内容
            GridFSFile gridFSFile = gridFsTemplate.findOne(Query.query(Criteria.where("_id").is(templateFileId)));
            GridFSDownloadStream gridFSDownloadStream = gridFSBucket.openDownloadStream(gridFSFile.getObjectId());
            GridFsResource gridFsResource = new GridFsResource(gridFSFile,gridFSDownloadStream);
            try {
                String content = IOUtils.toString(gridFsResource.getInputStream(), "utf-8");
                return content;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    //保存静态页面内容到gridFS
    private CmsPage saveHtml(String pageId,String htmlContent){
        //得到页面信息
        CmsPage cmsPage = this.findById(pageId);
        if (cmsPage == null){
            ExceptionCast.cast(CommonCode.INVALID_PARAM);
        }
        ObjectId objectId = null;
        //将htmlContent内容转成输入流
        try {
            InputStream inputStream = IOUtils.toInputStream(htmlContent, "utf-8");
            //将html文件内容保存到gridFS中
            objectId = gridFsTemplate.store(inputStream, cmsPage.getPageName());
        } catch (IOException e) {
            e.printStackTrace();
        }
        //将html文件id更新到cmsPage
        cmsPage.setHtmlFileId(objectId.toHexString());
        repository.save(cmsPage);
        return cmsPage;
    }

    //向mq发送消息
    private void sendPostPage(String pageId){
        CmsPage cmsPage = this.findById(pageId);
        if (cmsPage == null){
            ExceptionCast.cast(CmsCode.CMS_FINDPAGE_FAILER);
        }
        //创建对象
        Map<String,String> msg = new HashMap<>();
        msg.put("pageId",pageId);
        String jsonString = JSON.toJSONString(msg);
        //将json对象发送给mq
        //参数：交换机，RoutingKey(页面所属站点id)，消息
        rabbitTemplate.convertAndSend(RabbitmqConfig.EX_ROUTING_CMS_POSTPAGE,cmsPage.getSiteId(),jsonString);
    }

    public ResponseResult post(String pageId){
        String pageHtml = this.getPageHtml(pageId);
        CmsPage cmsPage = saveHtml(pageId, pageHtml);
        sendPostPage(pageId);
        return new ResponseResult(CommonCode.SUCCESS);
    }

    public CmsPageResult save(CmsPage cmsPage) {
        CmsPage one = cmsPageRepository.findByPageNameAndSiteIdAndPageWebPath(cmsPage.getPageName(), cmsPage.getSiteId(), cmsPage.getPageWebPath());
        if(one!=null){
            //进行更新
            return this.edit(one.getPageId(),cmsPage);
        }
        return this.add(cmsPage);
    }

    public CmsPostPageResult postPageQuick(CmsPage cmsPage) {

        //将页面信息存储到cms_page 集合中
        CmsPageResult save = this.save(cmsPage);
        if(!save.isSuccess()){
            ExceptionCast.cast(CommonCode.FAIL);
        }
        //得到页面的id
        CmsPage cmsPageSave = save.getCmsPage();
        String pageId = cmsPageSave.getPageId();

        //执行页面发布（先静态化、保存GridFS，向MQ发送消息）
        ResponseResult post = this.post(pageId);
        if(!post.isSuccess()){
            ExceptionCast.cast(CommonCode.FAIL);
        }
        //拼接页面Url= cmsSite.siteDomain+cmsSite.siteWebPath+ cmsPage.pageWebPath + cmsPage.pageName
        //取出站点id
        String siteId = cmsPageSave.getSiteId();
        CmsSite cmsSite = this.findCmsSiteById(siteId);
        //页面url
        String pageUrl =cmsSite.getSiteDomain() + cmsSite.getSiteWebPath() + cmsPageSave.getPageWebPath() + cmsPageSave.getPageName();
        return new CmsPostPageResult(CommonCode.SUCCESS,pageUrl);
    }

    private CmsSite findCmsSiteById(String siteId) {
        Optional<CmsSite> optional = cmsSiteRepository.findById(siteId);
        if(optional.isPresent()){
            return optional.get();
        }
        return null;
    }
}
