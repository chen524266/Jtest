package com.jtest.coverage.server.mapper;

import java.util.List;
import com.jtest.coverage.server.domain.CoverageEnvConfig;

/**
 * 环境配置Mapper接口
 *
 * @author Jtest
 * @date 2024-09-02
 */
public interface CoverageEnvConfigMapper
{
    /**
     * 查询环境配置
     *
     * @param configId 环境配置主键
     * @return 环境配置
     */
    public CoverageEnvConfig selectCoverageEnvConfigByConfigId(int configId);

    /**
     * 查询环境配置列表
     *
     * @param coverageEnvConfig 环境配置
     * @return 环境配置集合
     */
    public List<CoverageEnvConfig> selectCoverageEnvConfigList(CoverageEnvConfig coverageEnvConfig);

    /**
     * 新增环境配置
     *
     * @param coverageEnvConfig 环境配置
     * @return 结果
     */
    public int insertCoverageEnvConfig(CoverageEnvConfig coverageEnvConfig);

    /**
     * 修改环境配置
     *
     * @param coverageEnvConfig 环境配置
     * @return 结果
     */
    public int updateCoverageEnvConfig(CoverageEnvConfig coverageEnvConfig);

    /**
     * 删除环境配置
     *
     * @param configId 环境配置主键
     * @return 结果
     */
    public int deleteCoverageEnvConfigByConfigId(int configId);

    /**
     * 批量删除环境配置
     *
     * @param configIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteCoverageEnvConfigByConfigIds(String[] configIds);
}
