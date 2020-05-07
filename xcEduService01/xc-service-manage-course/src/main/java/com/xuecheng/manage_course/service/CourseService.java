package com.xuecheng.manage_course.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.response.CmsPageResult;
import com.xuecheng.framework.domain.cms.response.CmsPostPageResult;
import com.xuecheng.framework.domain.course.CourseBase;
import com.xuecheng.framework.domain.course.CourseMarket;
import com.xuecheng.framework.domain.course.CoursePic;
import com.xuecheng.framework.domain.course.Teachplan;
import com.xuecheng.framework.domain.course.ext.CourseInfo;
import com.xuecheng.framework.domain.course.ext.CourseView;
import com.xuecheng.framework.domain.course.ext.TeachplanNode;
import com.xuecheng.framework.domain.course.request.CourseListRequest;
import com.xuecheng.framework.domain.course.response.CourseCode;
import com.xuecheng.framework.domain.course.response.CoursePublicResult;
import com.xuecheng.framework.domain.course.response.CoursePublishResult;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_course.client.CmsPageClient;
import com.xuecheng.manage_course.config.CourseInfoConfiguration;
import com.xuecheng.manage_course.dao.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;


@Service
public class CourseService {
    @Autowired
    TeachplanMapper teachplanMapper;
    @Autowired
    CourseBaseRepository courseBaseRepository;
    @Autowired
    TeachPlanRepository teachPlanRepository;
    @Autowired
    CourseMapper courseMapper;
    @Autowired
    CoursePicRepository coursePicRepository;
    @Autowired
    CourseMarketRepository courseMarketRepository;
    @Autowired
    CmsPageClient cmsPageClient;
    @Autowired
    CourseInfoConfiguration courseInfoConfiguration;


    public TeachplanNode findTeachplanList(String courseId) {
        return teachplanMapper.findTeachplanList(courseId);
    }

    @Transactional
    public ResponseResult addTeachPlan(Teachplan teachplan) {
        if (teachplan == null ||
                StringUtils.isEmpty(teachplan.getCourseid())||
                StringUtils.isEmpty(teachplan.getPname())){
            ExceptionCast.cast(CommonCode.INVALID_PARAM);
        }
        String courseid = teachplan.getCourseid();
        String parentid = teachplan.getParentid();
        if (StringUtils.isEmpty(parentid)){
            Teachplan teachplanRoot = this.getTeachplanRoot(courseid);
            parentid = teachplanRoot.getId();
        }
        Teachplan teachplanNew = new Teachplan();
        BeanUtils.copyProperties(teachplan,teachplanNew);
        teachplanNew.setParentid(parentid);
        teachplanNew.setCourseid(courseid);
        String grade = this.getTeachplanRoot(courseid).getGrade();
        if (grade.equals("1")){
            teachplanNew.setGrade("2");
        }else {
            teachplanNew.setGrade("3");
        }
        teachPlanRepository.save(teachplanNew);
        return new ResponseResult(CommonCode.SUCCESS);

    }

    //查询课程根节点，查询不到则增加根节点
    private Teachplan getTeachplanRoot(String courseId){
        List<Teachplan> teachplanList = teachPlanRepository.findByCourseidAndParentid(courseId, "0");
        if (teachplanList == null || teachplanList.size() <= 0){
            Optional<CourseBase> optional = courseBaseRepository.findById(courseId);
            if (!optional.isPresent()){
                return null;
            }
            CourseBase courseBase = optional.get();
            //查询不到，自动添加
            Teachplan teachplan = new Teachplan();
            teachplan.setParentid("0");
            teachplan.setGrade("1");
            teachplan.setCourseid(courseId);
            teachplan.setStatus("0");
            teachplan.setPname(courseBase.getName());
            teachPlanRepository.save(teachplan);
            return teachplan;
        }
        return teachplanList.get(0);
    }

    public QueryResponseResult findCourseList(int page, int size, CourseListRequest courseListRequest) {
        if (courseListRequest == null){
            courseListRequest = new CourseListRequest();
        }
        CourseInfo courseInfo = new CourseInfo();
        ExampleMatcher exampleMatcher = ExampleMatcher.matching();
        if (StringUtils.isNoneEmpty(courseListRequest.getCompanyId())){
            courseInfo.setCompanyId(courseListRequest.getCompanyId());
        }
        Example<CourseInfo> example = Example.of(courseInfo,exampleMatcher);
        if (page <= 0){
            page = 1;
        }
        page = page - 1;
        if (size <= 0){
            size = 10;
        }
        PageHelper.startPage(page,size);
        Page<CourseInfo> courseListPage = courseMapper.findCourseListPage();
        QueryResult result = new QueryResult();
        result.setList(courseListPage);
        result.setTotal(courseListPage.getTotal());
        QueryResponseResult queryResponseResult = new QueryResponseResult(CommonCode.SUCCESS,result);
        return queryResponseResult;
    }

    public ResponseResult addCourseBase(CourseBase courseBase) {
        if (courseBase == null||
                StringUtils.isEmpty(courseBase.getName())||
                StringUtils.isEmpty(courseBase.getGrade())||
                StringUtils.isEmpty(courseBase.getStudymodel())||
                StringUtils.isEmpty(courseBase.getMt())||
                StringUtils.isEmpty(courseBase.getSt())){
            ExceptionCast.cast(CommonCode.INVALID_PARAM);
        }
        courseBase.setStatus("202001");
        CourseBase save = courseBaseRepository.save(courseBase);
        ResponseResult responseResult = new ResponseResult(CommonCode.SUCCESS);
        return responseResult;
    }

    public CourseBase findCouseBaseById(String courseId) {
        Optional<CourseBase> baseOptional = courseBaseRepository.findById(courseId);
        if(baseOptional.isPresent()){
            CourseBase courseBase = baseOptional.get();
            return courseBase;
        }
        ExceptionCast.cast(CourseCode.COURSE_DENIED_DELETE);
        return null;
    }

    public ResponseResult updateCourseInfo(String id, CourseBase courseBase) {
        if (courseBase == null){
            ExceptionCast.cast(CommonCode.INVALID_PARAM);
        }
        CourseBase courseBaseById = this.findCouseBaseById(id);
        if (courseBaseById.getName().equals(courseBase.getName())&&
                courseBaseById.getMt().equals(courseBase.getMt())&&
                courseBaseById.getSt().equals(courseBase.getSt())&&
                courseBaseById.getGrade().equals(courseBase.getGrade())&&
                courseBaseById.getStudymodel().equals(courseBase.getStudymodel())){
            if (courseBase.getUsers()!=null&&courseBase.getDescription()!=null){
                if (courseBase.getUsers().equals(courseBaseById.getUsers())&&
                        courseBase.getDescription().equals(courseBaseById.getDescription())){
                    return new ResponseResult(CommonCode.NOSUCHUPDATE);
                }
            }else if (courseBase.getUsers() != null){
                if (courseBase.getUsers().equals(courseBaseById.getUsers())){
                    return new ResponseResult(CommonCode.NOSUCHUPDATE);
                }
            }else if (courseBase.getDescription() != null){
                if (courseBase.getDescription().equals(courseBaseById.getDescription())){
                    return new ResponseResult(CommonCode.NOSUCHUPDATE);
                }
            }
        }
        courseBase.setId(courseBaseById.getId());
        courseBaseRepository.save(courseBase);
        return new ResponseResult(CommonCode.SUCCESS);
    }

    public CourseView getCoruseView(String id) {
        CourseView courseView = new CourseView();

        Optional<CourseBase> courseBaseOptional = courseBaseRepository.findById(id);
        if (courseBaseOptional.isPresent()){
            courseView.setCourseBase(courseBaseOptional.get());
        }
        Optional<CoursePic> coursePicOptional = coursePicRepository.findById(id);
        if (coursePicOptional.isPresent()){
            courseView.setCoursePic(coursePicOptional.get());
        }
        Optional<CourseMarket> courseMarketOptional = courseMarketRepository.findById(id);
        if (courseMarketOptional.isPresent()){
            courseView.setCourseMarket(courseMarketOptional.get());
        }
        TeachplanNode teachplanList = teachplanMapper.findTeachplanList(id);
        courseView.setTeachplanNode(teachplanList);
        return courseView;
    }

    /**
     * 课程预览
     * @param id
     * @return
     */

    public CoursePublicResult preview(String id) {
        CourseBase couseBaseById = findCouseBaseById(id);

        CmsPage cmsPage = new CmsPage();
        cmsPage.setSiteId(courseInfoConfiguration.getSiteId());
        cmsPage.setDataUrl(courseInfoConfiguration.getDataUrlPre()+id);
        cmsPage.setPageName(id+".html");
        cmsPage.setPageAliase(couseBaseById.getName());
        cmsPage.setPagePhysicalPath(courseInfoConfiguration.getPagePhysicalPath());
        cmsPage.setPageWebPath(courseInfoConfiguration.getPageWebPath());
        cmsPage.setTemplateId(courseInfoConfiguration.getTemplateId());

        CmsPageResult cmsPageResult = cmsPageClient.saveCmsPage(cmsPage);
        if (!cmsPageResult.isSuccess()){
            return new CoursePublicResult(CommonCode.FAIL,null);
        }
        CmsPage cmsPage1 = cmsPageResult.getCmsPage();
        String pageId = cmsPage1.getPageId();
        String preview = courseInfoConfiguration.getPreviewUrl()+pageId;
        return new CoursePublicResult(CommonCode.SUCCESS,preview);
    }

    @Transactional
    public CoursePublishResult publish(String id) {
        //查询课程
        CourseBase couseBaseById = findCouseBaseById(id);

        //准备页面信息
        CmsPage cmsPage = new CmsPage();
        cmsPage.setSiteId(courseInfoConfiguration.getSiteId());
        cmsPage.setDataUrl(courseInfoConfiguration.getDataUrlPre()+id);
        cmsPage.setPageName(id+".html");//页面名称
        cmsPage.setPageAliase(couseBaseById.getName());
        cmsPage.setPagePhysicalPath(courseInfoConfiguration.getPagePhysicalPath());
        cmsPage.setPageWebPath(courseInfoConfiguration.getPageWebPath());
        cmsPage.setTemplateId(courseInfoConfiguration.getTemplateId());
        //调用cms一键发布接口将课程详情页面发布到服务器
        CmsPostPageResult cmsPostPageResult = cmsPageClient.postPageQuick(cmsPage);
        if(!cmsPostPageResult.isSuccess()){
            return new CoursePublishResult(CommonCode.FAIL,null);
        }

        //保存课程的发布状态为“已发布”
        CourseBase courseBase = this.saveCoursePubState(id);
        if(courseBase == null){
            return new CoursePublishResult(CommonCode.FAIL,null);
        }

        //保存课程索引信息
        //...

        //缓存课程的信息
        //...
        //得到页面的url
        String pageUrl = cmsPostPageResult.getPageUrl();
        return new CoursePublishResult(CommonCode.SUCCESS,pageUrl);
    }

    private CourseBase  saveCoursePubState(String courseId){
        CourseBase courseBaseById = findCouseBaseById(courseId);
        courseBaseById.setStatus("202002");
        courseBaseRepository.save(courseBaseById);
        return courseBaseById;
    }
}
