package com.xuecheng.manage_cms.service;

import com.xuecheng.framework.domain.cms.CmsConfig;
import com.xuecheng.manage_cms.dao.CmsConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
@Service
public class ConfigService {
    @Autowired
    private CmsConfigRepository configRepository;

    public CmsConfig getConfigById(String id){
        Optional<CmsConfig> byId = configRepository.findById(id);
        CmsConfig cmsConfig = null;
        if (byId.isPresent()){
            cmsConfig = byId.get();
        }
        return cmsConfig;
    }
}
