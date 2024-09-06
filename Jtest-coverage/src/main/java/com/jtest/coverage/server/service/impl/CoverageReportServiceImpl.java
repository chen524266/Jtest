package com.jtest.coverage.server.service.impl;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cn.hutool.core.io.FileUtil;
import com.Jtest.common.core.text.Convert;
import com.Jtest.common.utils.DateUtils;
import com.Jtest.common.utils.IpUtils;
import com.Jtest.common.utils.ShiroUtils;
import com.Jtest.common.utils.StringUtils;
import com.Jtest.common.utils.uuid.UUID;
import com.jtest.coverage.exception.BaseException;
import com.jtest.coverage.server.domain.CoverageEnvConfig;
import com.jtest.coverage.server.domain.CoverageReport;
import com.jtest.coverage.server.mapper.CoverageEnvConfigMapper;
import com.jtest.coverage.vercontrol.ProcessHandle;
import com.jtest.coverage.vercontrol.handle.RepositoryCloneHandle;
import com.jtest.coverage.vercontrol.util.GitRepoUtil;
import com.jtest.coverage.vercontrol.util.JacocoCommandUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.stereotype.Service;
import com.jtest.coverage.server.mapper.CoverageReportMapper;
import com.jtest.coverage.server.service.ICoverageReportService;

/**
 * 代码覆盖率服务Service业务层处理
 *
 * @author Jtest
 * @date 2024-09-02
 */
@Service
public class CoverageReportServiceImpl implements ICoverageReportService {
    @Autowired
    private CoverageReportMapper coverageReportMapper;

    @Autowired
    private CoverageEnvConfigMapper envConfigMapper;

    @Autowired
    private ServletWebServerApplicationContext context;


    private static final String spilt = "/";

    /**
     * 查询代码覆盖率服务
     *
     * @param reportId 代码覆盖率服务主键
     * @return 代码覆盖率服务
     */
    @Override
    public CoverageReport selectCoverageReportByReportId(int reportId) {
        return coverageReportMapper.selectCoverageReportByReportId(reportId);
    }

    /**
     * 查询代码覆盖率服务列表
     *
     * @param coverageReport 代码覆盖率服务
     * @return 代码覆盖率服务
     */
    @Override
    public List<CoverageReport> selectCoverageReportList(CoverageReport coverageReport) {
        return coverageReportMapper.selectCoverageReportList(coverageReport);
    }

    /**
     * 新增代码覆盖率服务
     *
     * @param coverageReport 代码覆盖率服务
     * @return 结果
     */
    @Override
    public int insertCoverageReport(CoverageReport coverageReport) {
        coverageReport.setCreateTime(DateUtils.getNowDate());
        return coverageReportMapper.insertCoverageReport(coverageReport);
    }

    /**
     * 修改代码覆盖率服务
     *
     * @param coverageReport 代码覆盖率服务
     * @return 结果
     */
    @Override
    public int updateCoverageReport(CoverageReport coverageReport) {
        coverageReport.setUpdateTime(DateUtils.getNowDate());
        return coverageReportMapper.updateCoverageReport(coverageReport);
    }

    /**
     * 批量删除代码覆盖率服务
     *
     * @param reportIds 需要删除的代码覆盖率服务主键
     * @return 结果
     */
    @Override
    public int deleteCoverageReportByReportIds(String reportIds) {
        //删除报告需要把关联的报告文件夹、exec文件和diff-code都清理掉
        for(String reportId:reportIds.split(",")){
            CoverageReport report=coverageReportMapper.selectCoverageReportByReportId(Integer.parseInt(reportId));
            FileUtil.del(report.getReportDir());
            FileUtil.del(report.getExec());
            FileUtil.del(report.getDiffCodeFiles());
        }
        return coverageReportMapper.deleteCoverageReportByReportIds(Convert.toStrArray(reportIds));
    }

    /**
     * 删除代码覆盖率服务信息
     *
     * @param reportId 代码覆盖率服务主键
     * @return 结果
     */
    @Override
    public int deleteCoverageReportByReportId(int reportId) {
        return coverageReportMapper.deleteCoverageReportByReportId(reportId);
    }

    @Override
    public CoverageReport report(CoverageEnvConfig env) {
        ProcessHandle gitAndSvnCloneHandle = new RepositoryCloneHandle(env);
        gitAndSvnCloneHandle.process(env);
        String codeFile = env.getNowCodeFile() + File.separator;
        String classFiles = appendPrefix(env.getClassFiles().split(","), codeFile);
        String sourceFiles = appendPrefix(env.getSourceFiles().split(","), codeFile);
        CoverageReport report = CoverageReport.builder().reportDir(env.getLastReportDir()).exec(env.getExecFile())
                .appName(env.getAppName()).configId(env.getConfigId()).classFiles(classFiles).sourceFiles(sourceFiles)
                .repositoryUrl(env.getRepositoryUrl()).versionStart(env.getVersionStart()).versionNow(env.getVersionNow())
                .diffCodeFiles(env.getDiffCodeFile())
                .build();
        report.setCreateTime(new Date());
        report.setUpdateBy(ShiroUtils.getLoginName());
        String indexHtml = env.getLastReportDir() + File.separator + "index.html";
        try {
            analySumerReport(indexHtml, report);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String reportDir = new File(report.getReportDir()).getName();
        report.setReportHref("http://" + IpUtils.getHostIp() + ":" + context.getWebServer().getPort() + "/jacoco_report/" + reportDir + "/index.html");
        report.setCreateBy(ShiroUtils.getLoginName());
        coverageReportMapper.insertCoverageReport(report);
        env.setLastReportId(report.getReportId());
        env.setLastReportDir(report.getReportHref());
        envConfigMapper.updateCoverageEnvConfig(env);
        return report;
    }

    private String appendPrefix(String[] str, String prefix) {
        if (str.length == 1) {
            return prefix + str[0];
        }
        StringBuilder sb = new StringBuilder();
        for (String s : str) {
            sb.append(prefix).append(s).append(","); // 假设你想在每个元素后加一个空格
        }
        String result = sb.toString();
        return result.substring(0, result.length() - 1);
    }

    @Override
    public CoverageReport mergerReports(String ids) throws Exception {
        String[] reportIds = ids.split(",");
        if (reportIds.length < 2) {
            throw new BaseException(500, "合并报告数量不能少于两个！");
        }
        CoverageReport mergeReport = coverageReportMapper.selectCoverageReportByReportId(Integer.parseInt(reportIds[0]));
        CoverageEnvConfig mergeConfig = envConfigMapper.selectCoverageEnvConfigByConfigId(mergeReport.getConfigId());
        final boolean isGit = mergeReport.getRepositoryUrl().trim().toLowerCase().endsWith(".git") ? true : false;
        List<String> mergeTempFiles = new ArrayList<>();
        for (int i = 1; i < reportIds.length; i++) {
            CoverageReport report = coverageReportMapper.selectCoverageReportByReportId(Integer.parseInt((reportIds[i])));
            CoverageEnvConfig reportConfig = envConfigMapper.selectCoverageEnvConfigByConfigId(report.getConfigId());
            String reportDir = ProcessHandle.baseReportDir + UUID.fastUUID();
            String mergeExec = ProcessHandle.baseExecDir + UUID.fastUUID() + ".exec";
            List<String> args = new ArrayList<>();
            boolean afterBranch = false;
            if (isGit) {
                int commitTime = GitRepoUtil.getCommitTime(report, reportConfig.getName(), reportConfig.getPassword(), reportConfig.getGitSshPrivateKey());
                int mergeCommitTime = GitRepoUtil.getCommitTime(mergeReport, mergeConfig.getName(), mergeConfig.getPassword(), mergeConfig.getGitSshPrivateKey());
                afterBranch = mergeCommitTime > commitTime;
            } else {
                afterBranch = Long.parseLong(mergeReport.getVersionNow()) > Long.parseLong(report.getVersionNow());
            }
            String execFiles = afterBranch ? mergeReport.getExec() : report.getExec();
            String sourcefiles = afterBranch ? mergeReport.getSourceFiles() : report.getSourceFiles();
            String classFiles = afterBranch ? mergeReport.getClassFiles() : report.getClassFiles();
            String mergeClassfilepath = afterBranch ? report.getClassFiles() : mergeReport.getClassFiles();
            String mergeExecfilepath = afterBranch ? report.getExec() : mergeReport.getExec();
            args.add("report");
            args.add(execFiles);
            args.add("--html");
            args.add(reportDir);
            args.add("--mergeExecfilepath");
            args.add(mergeExecfilepath);
            args.add("--onlyMergeExec");
            args.add("true");
            args.add("--mergeExec");
            args.add(mergeExec);
            addpend(args, sourcefiles, "sourcefiles");
            addpend(args, classFiles, "classfiles");
            addpend(args, mergeClassfilepath, "mergeClassfilepath");
            String[] array = args.stream().toArray(String[]::new);
            JacocoCommandUtil.execute(array);
            if (i != reportIds.length - 1) {
                mergeTempFiles.add(mergeExec);
            }
            CoverageReport temp = afterBranch ? mergeReport : report;
            temp.setSourceFiles(sourcefiles);
            temp.setClassFiles(classFiles);
            temp.setExec(mergeExec);
            mergeReport = temp;
        }
        //生成最终报告
        String reportDir = ProcessHandle.baseReportDir + UUID.fastUUID();
        List<String> argss = new ArrayList<>();
        argss.add("report");
        argss.add(mergeReport.getExec());
        argss.add("--html");
        argss.add(reportDir);
        //如果最新的报告是增量的，就生成增量报告覆盖率
        if (StringUtils.isNotEmpty(mergeReport.getDiffCodeFiles())) {
            argss.add("--diffCodeFiles");
            argss.add(mergeReport.getDiffCodeFiles());
        }
        addpend(argss, mergeReport.getSourceFiles(), "sourcefiles");
        addpend(argss, mergeReport.getClassFiles(), "classfiles");
        String[] array = argss.stream().toArray(String[]::new);
        JacocoCommandUtil.execute(array);
        //删除掉合并过程的文件
        mergeTempFiles.forEach(i -> {
            FileUtil.del(i);
        });
        mergeReport.setReportId(null);
        analySumerReport(mergeReport.getReportDir()+File.separator+"index.html", mergeReport);
        coverageReportMapper.insertCoverageReport(mergeReport);
        return mergeReport;
    }

    public static void addpend(List<String> args, String source, String paramterName) {
        String[] sources = source.split(",");
        for (String str : sources) {
            args.add("--" + paramterName);
            args.add(str);
        }
    }


    public static void analySumerReport(String indexHtml, CoverageReport report) throws IOException {
        Document document = Jsoup.parse(new File(indexHtml), "UTF-8");
        Elements elements = document.select("tfoot tr td");
        String instructionMiss = elements.get(1).text().split("of")[0].trim();
        String instructionTotal = elements.get(1).text().split("of")[1].trim();
        String brachesMiss = elements.get(3).text().split("of")[0].trim();
        String brachesTotal = elements.get(3).text().split("of")[1].trim();
        String lineMiss = elements.get(7).text().trim();
        String lineTotal = elements.get(8).text().trim();
        String methodMiss = elements.get(9).text().trim();
        String methodTotal = elements.get(10).text().trim();
        String classMiss = elements.get(11).text().trim();
        String classTotal = elements.get(12).text().trim();
        String instructionCounter = instructionMiss + spilt + instructionTotal + rangePersent(instructionMiss, instructionTotal);
        String brachesCount = brachesTotal.trim().equals("0") ? "n/a" : brachesMiss + spilt + brachesTotal + rangePersent(brachesMiss, brachesTotal);
        String lineCounter = lineMiss + spilt + lineTotal + rangePersent(instructionMiss, instructionTotal);
        String methodCount = methodMiss + spilt + methodTotal + rangePersent(methodMiss, methodTotal);
        String classCount = classMiss + spilt + classTotal + rangePersent(classMiss, classTotal);
        report.setInstructionCounter(instructionCounter);
        report.setBranchCounter(brachesCount);
        report.setLineCounter(lineCounter);
        report.setClassCounter(classCount);
    }

    public static String rangePersent(String miss, String total) {
        if (total.trim().equals("0")) {
            return "n/a";
        }
        double ratio = 1 - (double) Integer.parseInt(miss.replaceAll(",", "")) / Integer.parseInt(total.replaceAll(",", ""));
        DecimalFormat df = new DecimalFormat("#.00"); // 保留两位小数
        return "(" + df.format(ratio * 100) + "%" + ")";
    }
}
