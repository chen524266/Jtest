package com.jtest.coverage.vercontrol.handle;

import com.Jtest.common.utils.spring.SpringUtils;
import com.jtest.coverage.server.domain.CoverageEnvConfig;
import com.jtest.coverage.vercontrol.ProcessHandle;
import com.jtest.coverage.vercontrol.VersionControlHandlerFactory;
import com.jtest.coverage.vercontrol.dto.MethodInvokeDto;
import com.jtest.coverage.vercontrol.enums.CodeManageTypeEnum;
import com.jtest.coverage.vercontrol.service.MavenCmdInvokeService;
import com.jtest.coverage.vercontrol.util.FileUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
public class RepositoryCloneHandle extends ProcessHandle {

    public RepositoryCloneHandle(CoverageEnvConfig envConfig) {
        super(envConfig);
        this.setNext(new DiffCodeHandle(envConfig));
    }

    @Override
    public void process(CoverageEnvConfig envConfig) {
        List<String> versions = Arrays.asList(new String[]{envConfig.getVersionStart(), envConfig.getVersionNow()});
        List<CompletableFuture<String>> futureList = versions.stream()
                .map(item -> CompletableFuture.supplyAsync(() -> VersionControlHandlerFactory.downloadCode(build(envConfig, item))))
                .collect(Collectors.toList());
        CompletableFuture.allOf(futureList.toArray(new CompletableFuture[0])).join();
        envConfig.setOldCodeFile(VersionControlHandlerFactory.downloadCode(build(envConfig, envConfig.getVersionStart())));
        envConfig.setNowCodeFile(VersionControlHandlerFactory.downloadCode(build(envConfig, envConfig.getVersionNow())));
        complieCode(envConfig.getOldCodeFile());
        complieCode(envConfig.getNowCodeFile());
        this.getNext().process(envConfig);
    }

    private MethodInvokeDto build(CoverageEnvConfig envConfig, String branchName) {
        final CodeManageTypeEnum codeManageTypeEnum = envConfig.getRepositoryUrl().trim().toLowerCase().endsWith(".git") ? CodeManageTypeEnum.GIT : CodeManageTypeEnum.SVN;
        MethodInvokeDto methodInvokeDto = MethodInvokeDto.builder().repoUrl(envConfig.getRepositoryUrl()).branchName(branchName)
                .gitSshPrivateKey(envConfig.getGitSshPrivateKey())
                .baseLocalDir(baseCodeCloneDir).userName(envConfig.getName()).passWord(envConfig.getPassword()).codeManageTypeEnum(codeManageTypeEnum).build();
        return methodInvokeDto;
    }

    private void complieCode(String filePath) {
        MavenCmdInvokeService mavenCmdInvokeService = SpringUtils.getBean(MavenCmdInvokeService.class);
        boolean compileFlag = FileUtils.searchFile(new File(filePath), ".class");
        if (compileFlag) {
            log.info(filePath + "代码已经编译，直接使用");
        } else {
            //编译代码
            mavenCmdInvokeService.compileCode(filePath);
        }
    }
}
