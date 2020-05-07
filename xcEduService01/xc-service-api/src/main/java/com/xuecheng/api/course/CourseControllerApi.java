package com.xuecheng.api.course;

import com.xuecheng.framework.domain.course.CourseBase;
import com.xuecheng.framework.domain.course.Teachplan;
import com.xuecheng.framework.domain.course.ext.CourseView;
import com.xuecheng.framework.domain.course.ext.TeachplanNode;
import com.xuecheng.framework.domain.course.request.CourseListRequest;

import com.xuecheng.framework.domain.course.response.CoursePublicResult;
import com.xuecheng.framework.domain.course.response.CoursePublishResult;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.ResponseResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(value = "课程管理接口",description = "课程管理接口，提供课程的增删改查")
public interface CourseControllerApi {
    @ApiOperation("数据字典的增删改查")
    public QueryResponseResult getDirectory(String dType);

    @ApiOperation("课程计划查询")
    public TeachplanNode findTeachplanList(String courseId);

    @ApiOperation("添加课程计划")
    public ResponseResult addTeachPlan(Teachplan teachplan);

    @ApiOperation("查询我的课程")
    public QueryResponseResult findCourseList(int page, int size, CourseListRequest courseListRequest);

    @ApiOperation("添加课程基础信息")
    public ResponseResult addCourseBase(CourseBase courseBase);

    @ApiOperation("根据id查询课程基本信息")
    public CourseBase findCouseBaseById(String courseId);

    @ApiOperation("更新课程信息")
    public ResponseResult updateCourseInfo(String id,CourseBase courseBase);

    @ApiOperation("课程视图查询")
    public CourseView courseView(String id);

    @ApiOperation("课程预览")
    public CoursePublicResult preview(String id);

    @ApiOperation("课程发布")
    public CoursePublishResult publish(String id);
}
