package com.jtest.coverage.vercontrol.svn;

import com.jtest.coverage.log.LoggerUtil;
import com.jtest.coverage.vercontrol.AbstractVersionControl;
import com.jtest.coverage.vercontrol.ProcessHandle;
import com.jtest.coverage.vercontrol.dto.MethodInvokeDto;
import com.jtest.coverage.vercontrol.enums.CodeManageTypeEnum;
import com.jtest.coverage.vercontrol.util.PathUtils;
import com.jtest.coverage.vercontrol.util.SvnRepoUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.wc.SVNDiffClient;
import org.tmatesoft.svn.core.wc.SVNRevision;


/**
 * @ProjectName: code-diff-parent
 * @Package: com.dr.code.diff.vercontrol
 * @Description: svn差异代码获取
 * @Author: duanrui
 * @CreateDate: 2021/4/5 9:56
 * @Version: 1.0
 * <p>
 * Copyright: Copyright (c) 2021
 */
@Slf4j
@Component
public class SvnAbstractVersionControl extends AbstractVersionControl {

    /**
     * 获取操作类型
     */
    @Override
    public CodeManageTypeEnum getType() {
        return CodeManageTypeEnum.SVN;
    }

    @Override
    public void getDiffCodeClasses() {
        try {
            MySVNDiffStatusHandler.list.clear();
            String nowSvnUrl = super.versionControlDto.getRepoUrl();
            if(StringUtils.isNotBlank(super.versionControlDto.getSvnRepoUrl())){
                nowSvnUrl = super.versionControlDto.getSvnRepoUrl();
            }
            SVNRevision oldVersion = null;
            SVNRevision newVersion = null;
            //不同reversion的比较和最新reversion的比较
            if (StringUtils.isNotBlank(super.versionControlDto.getNowVersion()) && StringUtils.isNotBlank(super.versionControlDto.getBaseVersion())) {
                oldVersion = SVNRevision.create(Long.parseLong(super.versionControlDto.getBaseVersion()));
                newVersion = SVNRevision.create(Long.parseLong(super.versionControlDto.getNowVersion()));
            } else {
                oldVersion = SVNRevision.HEAD;
                newVersion = SVNRevision.HEAD;
            }
            String localBaseRepoDir = SvnRepoUtil.getSvnLocalDir(super.versionControlDto.getRepoUrl(), ProcessHandle.baseCodeCloneDir, oldVersion.toString());
            String localNowRepoDir = SvnRepoUtil.getSvnLocalDir(nowSvnUrl, ProcessHandle.baseCodeCloneDir, newVersion.toString());
            LoggerUtil.info(log,"旧版本本地地址",localBaseRepoDir);
            LoggerUtil.info(log,"新版本本地地址",localNowRepoDir);
            super.versionControlDto.setNewLocalBasePath(localNowRepoDir);
            super.versionControlDto.setOldLocalBasePath(localBaseRepoDir);
            SvnRepoUtil.cloneRepository(super.versionControlDto.getRepoUrl(), localBaseRepoDir, oldVersion, versionControlDto.getUserName(), versionControlDto.getPassWord());
            SvnRepoUtil.cloneRepository(nowSvnUrl, localNowRepoDir, newVersion, versionControlDto.getUserName(), versionControlDto.getPassWord());
            SVNDiffClient svnDiffClient = SvnRepoUtil.getSvnDiffClient( versionControlDto.getUserName(),versionControlDto.getPassWord());
            String rootPath= StringUtils.isEmpty(versionControlDto.getRootPath())?"src/main/java/":versionControlDto.getRootPath();
            versionControlDto.setRootPath(rootPath);
            svnDiffClient.doDiffStatus(SVNURL.parseURIEncoded(super.versionControlDto.getRepoUrl()), oldVersion, SVNURL.parseURIEncoded(nowSvnUrl), newVersion, SVNDepth.INFINITY, true, new MySVNDiffStatusHandler(rootPath));
            //将差异代码设置进集合
            super.versionControlDto.setDiffClasses(MySVNDiffStatusHandler.list);
        } catch (SVNException e) {
            e.printStackTrace();
        }
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
        String localDir = SvnRepoUtil.getSvnLocalDir(super.versionControlDto.getRepoUrl(), ProcessHandle.baseCodeCloneDir, "");
        return PathUtils.getClassFilePath(super.versionControlDto.getOldLocalBasePath(), filePackage);
    }

    /**
     * 下载代码
     *
     * @param methodInvokeDto 方法调用dto
     * @return {@link String}
     */
    @Override
    public String downloadCode(MethodInvokeDto methodInvokeDto) {
        SVNRevision oldVersion = SVNRevision.create(Long.parseLong(methodInvokeDto.getBranchName()));
        String localBaseRepoDir = SvnRepoUtil.getSvnLocalDir(methodInvokeDto.getRepoUrl(), methodInvokeDto.getBaseLocalDir(), oldVersion.toString());
        SvnRepoUtil.cloneRepository(methodInvokeDto.getRepoUrl(), localBaseRepoDir, oldVersion, methodInvokeDto.getUserName(), methodInvokeDto.getPassWord());
        return localBaseRepoDir;
    }
}
