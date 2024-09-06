package com.jtest.coverage.server.service.impl;

import java.util.List;

import com.Jtest.common.core.text.Convert;
import com.Jtest.common.utils.DateUtils;
import com.Jtest.common.utils.ShiroUtils;
import com.jtest.coverage.server.domain.CoverageEnvConfig;
import com.jtest.coverage.server.mapper.CoverageEnvConfigMapper;
import com.jtest.coverage.server.service.ICoverageReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.jtest.coverage.server.service.ICoverageEnvConfigService;

/**
 * 环境配置Service业务层处理
 *
 * @author Jtest
 * @date 2024-09-02
 */
@Service
public class CoverageEnvConfigServiceImpl implements ICoverageEnvConfigService {
    @Autowired
    private CoverageEnvConfigMapper coverageEnvConfigMapper;

    @Autowired
    private ICoverageReportService coverageReportService;

    /**
     * 查询环境配置
     *
     * @param configId 环境配置主键
     * @return 环境配置
     */
    @Override
    public CoverageEnvConfig selectCoverageEnvConfigByConfigId(int configId) {
        return coverageEnvConfigMapper.selectCoverageEnvConfigByConfigId(configId);
    }

    /**
     * 查询环境配置列表
     *
     * @param coverageEnvConfig 环境配置
     * @return 环境配置
     */
    @Override
    public List<CoverageEnvConfig> selectCoverageEnvConfigList(CoverageEnvConfig coverageEnvConfig) {
        return coverageEnvConfigMapper.selectCoverageEnvConfigList(coverageEnvConfig);
    }

    /**
     * 新增环境配置
     *
     * @param coverageEnvConfig 环境配置
     * @return 结果
     */
    @Override
    public int insertCoverageEnvConfig(CoverageEnvConfig coverageEnvConfig) {
        coverageEnvConfig.setCreateTime(DateUtils.getNowDate());
        coverageEnvConfig.setCreateBy(ShiroUtils.getLoginName());
        return coverageEnvConfigMapper.insertCoverageEnvConfig(coverageEnvConfig);
    }

    /**
     * 修改环境配置
     *
     * @param coverageEnvConfig 环境配置
     * @return 结果
     */
    @Override
    public int updateCoverageEnvConfig(CoverageEnvConfig coverageEnvConfig) {
        coverageEnvConfig.setUpdateTime(DateUtils.getNowDate());
        return coverageEnvConfigMapper.updateCoverageEnvConfig(coverageEnvConfig);
    }

    /**
     * 批量删除环境配置
     *
     * @param configIds 需要删除的环境配置主键
     * @return 结果
     */
    @Override
    public int deleteCoverageEnvConfigByConfigIds(String configIds) {
        return coverageEnvConfigMapper.deleteCoverageEnvConfigByConfigIds(Convert.toStrArray(configIds));
    }

    /**
     * 删除环境配置信息
     *
     * @param configId 环境配置主键
     * @return 结果
     */
    @Override
    public int deleteCoverageEnvConfigByConfigId(int configId) {
        return coverageEnvConfigMapper.deleteCoverageEnvConfigByConfigId(configId);
    }

    @Override
    public void report(int configId) {
        coverageReportService.report(coverageEnvConfigMapper.selectCoverageEnvConfigByConfigId(configId));
    }
}
