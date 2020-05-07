package com.xuecheng.manage_course.service;

import com.xuecheng.framework.domain.course.DirectoryInfo;
import com.xuecheng.framework.domain.course.ext.DirectoryChildren;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import com.xuecheng.manage_course.dao.DirectoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DirectoryService {
    @Autowired
    DirectoryRepository directoryRepository;

    public QueryResponseResult getDirectory(String dType) {
        DirectoryInfo byDType = directoryRepository.findByDType(dType);
        if (byDType == null){
            ExceptionCast.cast(CommonCode.FAIL);
        }
        List<DirectoryChildren> dValue = byDType.getDValue();
        QueryResult<DirectoryChildren> queryResult = new QueryResult<>();
        queryResult.setList(dValue);
        QueryResponseResult queryResponseResult = new QueryResponseResult(CommonCode.SUCCESS,queryResult);
        return queryResponseResult;
    }
}
