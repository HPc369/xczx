package com.xuecheng.manage_course.dao;

import com.xuecheng.framework.domain.course.DirectoryInfo;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface DirectoryRepository extends MongoRepository<DirectoryInfo, String> {
    public DirectoryInfo findByDType(String dType);
}
