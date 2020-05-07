package com.xuecheng.framework.domain.course;

import java.util.List;
import com.xuecheng.framework.domain.course.ext.DirectoryChildren;
import lombok.Data;
import lombok.ToString;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.Id;

@Data
@ToString
@Document(collection = "sys_dictionary")
public class DirectoryInfo {
    /*
    "d_name" : "媒资视频处理状态",
    "d_type" : "303",
    "d_value" :*/
    @Id
    private String directoryId;
    private String dName;
    private String dType;
    private List<DirectoryChildren> dValue;
}
