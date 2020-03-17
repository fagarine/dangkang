package cn.laoshini.dk.config.center.service;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import cn.laoshini.dk.config.center.mapper.VersionHistoryMapper;

/**
 * @author fagarine
 */
@Service
public class VersionService {

    @Resource
    private VersionHistoryMapper versionHistoryMapper;

    int nextVersionIterations(String name) {
        Integer iterations = versionHistoryMapper.selectIterations(name);
        if (iterations == null) {
            versionHistoryMapper.insert(name);
            return 1;
        } else {
            versionHistoryMapper.versionIncrement(name);
            return iterations + 1;
        }
    }

    public int currentVersionIterations(String name) {
        return versionHistoryMapper.selectIterations(name);
    }

}
