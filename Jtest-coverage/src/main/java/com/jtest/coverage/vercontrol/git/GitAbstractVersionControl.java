package com.jtest.coverage.vercontrol.git;

import com.Jtest.common.utils.StringUtils;
import com.alibaba.fastjson.JSON;
import com.jtest.coverage.errorcode.BizCode;
import com.jtest.coverage.exception.BizException;
import com.jtest.coverage.log.LoggerUtil;
import com.jtest.coverage.vercontrol.AbstractVersionControl;
import com.jtest.coverage.vercontrol.ProcessHandle;
import com.jtest.coverage.vercontrol.dto.ChangeLine;
import com.jtest.coverage.vercontrol.dto.DiffEntryDto;
import com.jtest.coverage.vercontrol.dto.GitInfoDto;
import com.jtest.coverage.vercontrol.dto.MethodInvokeDto;
import com.jtest.coverage.vercontrol.enums.CodeManageTypeEnum;
import com.jtest.coverage.vercontrol.enums.GitUrlTypeEnum;
import com.jtest.coverage.vercontrol.util.GitRepoUtil;
import com.jtest.coverage.vercontrol.util.PathUtils;
import com.jtest.coverage.vercontrol.utils.mapper.OrikaMapperUtils;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.diff.EditList;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.util.io.DisabledOutputStream;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @ProjectName: code-diff-parent
 * @Package: com.dr.code.diff.vercontrol
 * @Description: 代码差异获取流程类定义
 * @Author: duanrui
 * @CreateDate: 2021/4/5 9:56
 * @Version: 1.0
 * <p>
 * Copyright: Copyright (c) 2021
 */
@Component
@Slf4j
public class GitAbstractVersionControl extends AbstractVersionControl {

    /**
     * 获取操作类型
     */
    @Override
    public CodeManageTypeEnum getType() {
        return CodeManageTypeEnum.GIT;
    }

    @Override
    public void getDiffCodeClasses() {
        try {
            GitInfoDto baseGitInfo = getGitInfo(super.versionControlDto.getRepoUrl(), super.versionControlDto.getBaseVersion());
            Git baseGit = baseGitInfo.getGit();
            GitInfoDto nowGitInfo = getGitInfo(super.versionControlDto.getRepoUrl(), super.versionControlDto.getNowVersion());
            Git nowGit = nowGitInfo.getGit();
            super.versionControlDto.setNewLocalBasePath(nowGitInfo.getLocalBaseRepoDir());
            super.versionControlDto.setOldLocalBasePath(baseGitInfo.getLocalBaseRepoDir());
            AbstractTreeIterator baseTree = GitRepoUtil.prepareTreeParser(baseGit.getRepository(), super.versionControlDto.getBaseVersion());
            AbstractTreeIterator nowTree = GitRepoUtil.prepareTreeParser(nowGit.getRepository(), super.versionControlDto.getNowVersion());
            //获取两个版本之间的差异代码
            List<DiffEntry> diff = null;
            String rootPath = StringUtils.isEmpty(versionControlDto.getRootPath()) ? "src/main/java/" : versionControlDto.getRootPath();
            versionControlDto.setRootPath(rootPath);
            diff = nowGit.diff().setOldTree(baseTree).setNewTree(nowTree).setShowNameAndStatusOnly(true).call();
            //过滤出有效的差异代码
            Collection<DiffEntry> validDiffList = diff.stream()
                    //只计算java文件和xml
                    .filter(e -> e.getNewPath().endsWith(".java") || e.getNewPath().endsWith(".xml"))
                    //排除测试文件
                    .filter(e -> {
                        if (e.getNewPath().endsWith(".java")) {
                            return e.getNewPath().contains(rootPath);
                        }
                        return Boolean.TRUE;
                    })
                    //只计算新增和变更文件
                    .filter(e -> DiffEntry.ChangeType.ADD.equals(e.getChangeType()) || DiffEntry.ChangeType.MODIFY.equals(e.getChangeType()))
                    .collect(Collectors.toList());
            //计算xml变更引起的mapper方法变更
            if (CollectionUtils.isEmpty(validDiffList)) {
                LoggerUtil.info(log, "没有需要对比的类");
                return;
            }
            List<DiffEntryDto> diffEntries = OrikaMapperUtils.mapList(validDiffList, DiffEntry.class, DiffEntryDto.class);

            Map<String, DiffEntryDto> diffMap = diffEntries.stream().collect(Collectors.toMap(DiffEntryDto::getNewPath, Function.identity()));
            LoggerUtil.info(log, "需要对比的差异类为：", JSON.toJSON(diffEntries));
            DiffFormatter diffFormatter = new DiffFormatter(DisabledOutputStream.INSTANCE);
            diffFormatter.setRepository(nowGit.getRepository());
            diffFormatter.setContext(0);
            //此处是获取变更行，有群友需求新增行或变更行要在类中打标记，此处忽略删除行
            for (DiffEntry diffClass : validDiffList) {
                //获取变更行
                EditList edits = diffFormatter.toFileHeader(diffClass).toEditList();
                if (CollectionUtils.isEmpty(edits)) {
                    continue;
                }
                //获取出新增行和变更行
                List<Edit> list = edits.stream().filter(e -> Edit.Type.INSERT.equals(e.getType()) || Edit.Type.REPLACE.equals(e.getType())).collect(Collectors.toList());
                List<ChangeLine> lines = new ArrayList<>(list.size());
                list.forEach(
                        edit -> {
                            ChangeLine build = ChangeLine.builder().startLineNum(edit.getBeginB()).endLineNum(edit.getEndB()).type(edit.getType().name()).build();
                            lines.add(build);
                        }
                );
                if (diffMap.containsKey(diffClass.getNewPath())) {
                    DiffEntryDto diffEntryDto = diffMap.get(diffClass.getNewPath());
                    diffEntryDto.setLines(lines);
                }
            }
            //设置变更行
            super.versionControlDto.setDiffClasses(new ArrayList<DiffEntryDto>(diffMap.values()));
        } catch (IOException |
                GitAPIException e) {
            e.printStackTrace();
            throw new BizException(BizCode.GET_DIFF_CLASS_ERROR);
        }

    }


    /**
     * 下载代码
     *
     * @param methodInvokeDto 方法调用dto
     * @return {@link String}
     */
    @Override
    public String downloadCode(MethodInvokeDto methodInvokeDto) {
        GitInfoDto gitInfo = getGitInfo(methodInvokeDto);
        return gitInfo.getLocalBaseRepoDir();
    }


    /**
     * git获取信息
     *
     * @param repoUrl    仓库url
     * @param branchName 分支机构名称
     * @return {@link GitInfoDto}
     */
    public GitInfoDto getGitInfo(String repoUrl, String branchName) {
        Git git = null;
        String localBaseRepoDir = GitRepoUtil.getLocalDir(repoUrl, ProcessHandle.baseCodeCloneDir, branchName);
        GitUrlTypeEnum gitUrlTypeEnum = GitRepoUtil.judgeUrlType(repoUrl);
        if (null == gitUrlTypeEnum) {
            throw new BizException(BizCode.UNKNOWN_REPOSITY_URL);
        }
        switch (Objects.requireNonNull(gitUrlTypeEnum)) {
            case HTTP: {
                //原有代码git对象
                git = GitRepoUtil.httpCloneRepository(repoUrl, localBaseRepoDir, branchName, versionControlDto.getUserName(), versionControlDto.getPassWord());
                break;
            }
            case SSH: {
                localBaseRepoDir += GitUrlTypeEnum.SSH.getValue();
                //原有代码git对象
                git = GitRepoUtil.sshCloneRepository(repoUrl, localBaseRepoDir, branchName, versionControlDto.getGitSshPrivateKey());
                break;
            }
            default: {
                LoggerUtil.error(log, "未知类型仓库地址");
                throw new BizException(BizCode.UNKNOWN_REPOSITY_URL);
            }
        }
        return GitInfoDto.builder().git(git).localBaseRepoDir(localBaseRepoDir).build();
    }

    public static GitInfoDto getGitInfo(String repoUrl, String branchName, String userName, String passWord, String gitSshPrivateKey) {
        Git git = null;
        String localBaseRepoDir = GitRepoUtil.getLocalDir(repoUrl, ProcessHandle.baseCodeCloneDir, branchName);
        GitUrlTypeEnum gitUrlTypeEnum = GitRepoUtil.judgeUrlType(repoUrl);
        if (null == gitUrlTypeEnum) {
            throw new BizException(BizCode.UNKNOWN_REPOSITY_URL);
        }
        switch (Objects.requireNonNull(gitUrlTypeEnum)) {
            case HTTP: {
                //原有代码git对象
                git = GitRepoUtil.httpCloneRepository(repoUrl, localBaseRepoDir, branchName, userName, passWord);
                break;
            }
            case SSH: {
                localBaseRepoDir += GitUrlTypeEnum.SSH.getValue();
                //原有代码git对象
                git = GitRepoUtil.sshCloneRepository(repoUrl, localBaseRepoDir, branchName, gitSshPrivateKey);
                break;
            }
            default: {
                LoggerUtil.error(log, "未知类型仓库地址");
                throw new BizException(BizCode.UNKNOWN_REPOSITY_URL);
            }
        }
        return GitInfoDto.builder().git(git).localBaseRepoDir(localBaseRepoDir).build();
    }

    public GitInfoDto getGitInfo(MethodInvokeDto methodInvokeDto) {
        String repoUrl = methodInvokeDto.getRepoUrl();
        String branchName = methodInvokeDto.getBranchName();
        String userName = methodInvokeDto.getUserName();
        String passWord = methodInvokeDto.getPassWord();
        Git git = null;
        String localBaseRepoDir = GitRepoUtil.getLocalDir(repoUrl, ProcessHandle.baseCodeCloneDir, branchName);
        GitUrlTypeEnum gitUrlTypeEnum = GitRepoUtil.judgeUrlType(repoUrl);
        if (null == gitUrlTypeEnum) {
            throw new BizException(BizCode.UNKNOWN_REPOSITY_URL);
        }
        switch (Objects.requireNonNull(gitUrlTypeEnum)) {
            case HTTP: {
                //原有代码git对象
                git = GitRepoUtil.httpCloneRepository(repoUrl, localBaseRepoDir, branchName, userName, passWord);
                break;
            }
            case SSH: {
                localBaseRepoDir += GitUrlTypeEnum.SSH.getValue();
                //原有代码git对象
                git = GitRepoUtil.sshCloneRepository(repoUrl, localBaseRepoDir, branchName, methodInvokeDto.getGitSshPrivateKey());
                break;
            }
            default: {
                LoggerUtil.error(log, "未知类型仓库地址");
                throw new BizException(BizCode.UNKNOWN_REPOSITY_URL);
            }
        }
        return GitInfoDto.builder().git(git).localBaseRepoDir(localBaseRepoDir).build();
    }

    /**
     * @param filePackage
     * @date:2021/4/24
     * @className:VersionControl
     * @author:Administrator
     * @description: 获取旧版本文件本地路径
     */
    @Override
    public String getLocalNewPath(String filePackage) {
        return PathUtils.getClassFilePath(super.versionControlDto.getNewLocalBasePath(), filePackage);
    }

    /**
     * @param filePackage
     * @date:2021/4/24
     * @className:VersionControl
     * @author:Administrator
     * @description: 获取新版本文件本地路径
     */
    @Override
    public String getLocalOldPath(String filePackage) {
        return PathUtils.getClassFilePath(super.versionControlDto.getOldLocalBasePath(), filePackage);
    }
}
