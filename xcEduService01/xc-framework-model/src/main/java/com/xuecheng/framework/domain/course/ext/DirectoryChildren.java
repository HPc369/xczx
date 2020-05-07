package com.xuecheng.framework.domain.course.ext;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class DirectoryChildren {
    /*"sd_name" : "处理中",
            "sd_id" : "303001",
            "sd_status" : "1"*/
    private String sdName;
    private String sdId;
    private String sdStatus;
}
