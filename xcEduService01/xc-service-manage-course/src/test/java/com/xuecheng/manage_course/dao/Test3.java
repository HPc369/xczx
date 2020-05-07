package com.xuecheng.manage_course.dao;

import com.xuecheng.manage_course.config.CourseInfoConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
public class Test3 {

    @Autowired
    CourseInfoConfiguration courseInfoConfiguration;

    @Test
    public void test(){
        String a = courseInfoConfiguration.getDataUrlPre();
        String b = courseInfoConfiguration.getPagePhysicalPath();
        String c = courseInfoConfiguration.getPageWebPath();
        System.out.println(a+b+c);
    }

}
