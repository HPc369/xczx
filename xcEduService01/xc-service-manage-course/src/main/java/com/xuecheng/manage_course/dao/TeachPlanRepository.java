package com.xuecheng.manage_course.dao;

import com.xuecheng.framework.domain.course.Teachplan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Created by Administrator.
 */
public interface TeachPlanRepository extends JpaRepository<Teachplan,String> {
    //根据课程id和parentid查询teachplan
    public List<Teachplan> findByCourseidAndParentid(String courseid,String parentid);
}

