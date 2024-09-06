package com.jtest.coverage.server.mapper;

import java.util.List;
import com.jtest.coverage.server.domain.CoverageReport;

/**
 * 代码覆盖率服务Mapper接口
 *
 * @author Jtest
 * @date 2024-09-02
 */
public interface CoverageReportMapper
{
    /**
     * 查询代码覆盖率服务
     *
     * @param reportId 代码覆盖率服务主键
     * @return 代码覆盖率服务
     */
    public CoverageReport selectCoverageReportByReportId(int reportId);

    /**
     * 查询代码覆盖率服务列表
     *
     * @param coverageReport 代码覆盖率服务
     * @return 代码覆盖率服务集合
     */
    public List<CoverageReport> selectCoverageReportList(CoverageReport coverageReport);

    /**
     * 新增代码覆盖率服务
     *
     * @param coverageReport 代码覆盖率服务
     * @return 结果
     */
    public int insertCoverageReport(CoverageReport coverageReport);

    /**
     * 修改代码覆盖率服务
     *
     * @param coverageReport 代码覆盖率服务
     * @return 结果
     */
    public int updateCoverageReport(CoverageReport coverageReport);

    /**
     * 删除代码覆盖率服务
     *
     * @param reportId 代码覆盖率服务主键
     * @return 结果
     */
    public int deleteCoverageReportByReportId(int reportId);

    /**
     * 批量删除代码覆盖率服务
     *
     * @param reportIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteCoverageReportByReportIds(String[] reportIds);
}
