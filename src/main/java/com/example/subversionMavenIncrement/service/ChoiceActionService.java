package com.example.subversionMavenIncrement.service;

import com.example.subversionMavenIncrement.run.ExecuteCommand;
import com.example.subversionMavenIncrement.util.NotifyUtil;
import com.example.subversionMavenIncrement.util.WarUtil;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.RepositoryLocation;
import com.intellij.openapi.vcs.actions.VcsContextUtil;
import com.intellij.openapi.vcs.history.VcsFileRevision;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.peer.impl.VcsContextFactoryImpl;
import org.apache.commons.compress.archivers.ArchiveException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * 逻辑处理
 *
 * @author 蚕豆的生活
 */
public class ChoiceActionService {

    private static Pattern pattern;

    private static final String CO_WAR_PATH = File.separator + "temporary-file";

    private static final String WEB_INF_CASS_PATH = File.separator + "WEB-INF" + File.separator + "classes";

    private static final String[] PACKAGING_FOLDER = {"resources", "src"};

    /**
     * 获取svn提交的历史记录
     *
     * @param dataContext
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    public static List<String> findSvnNotes(DataContext dataContext) throws IOException, InterruptedException {

        // 获取选中的svn相关数据
        VcsFileRevision vcsFileRevision = (VcsFileRevision) dataContext.getData("VCS_FILE_REVISION");
        RepositoryLocation changedRepositoryPath = vcsFileRevision.getChangedRepositoryPath();

        // 获取svn提交的历史记录
        String s = ExecuteCommand.carrOut(null, "svn log",
                changedRepositoryPath.toPresentableString(),
                "-r", vcsFileRevision.getRevisionNumber().asString(),
                "-v");

        return svnDataHandling(s, " [M|A] /(.*?)\r\n");
    }

    /**
     * 生成更新包后续处理
     *
     * @param project
     * @param dataContext
     * @param svnSubmitData
     * @param isMvnRadioButton
     */
    public static void backEnd(Project project, DataContext dataContext, List<String> svnSubmitData, Boolean isMvnRadioButton) {

        VirtualFile virtualFile = VcsContextUtil.selectedFile(dataContext);
        FilePath filePathOn = new VcsContextFactoryImpl().createFilePathOn(virtualFile);

        // 是否使用maven打包
        if (isMvnRadioButton) {
            try {
                ExecuteCommand.carrOut(filePathOn.getIOFile(), "mvn clean package",
                        "-Dmaven.test.skip=true");
                NotifyUtil.notifyInfo(project, "maven打包完成,请稍后！");
            } catch (IOException | InterruptedException e) {
                NotifyUtil.notifyError(project, "maven打包失败！请检查配置" + Messages.getInformationIcon());
                throw new RuntimeException(e);
            }
        }

        // 先创建临时时文件夹
        String temporaryFile = CheckUpService.RESOLVE_ADDRESS + File.separator + "target";
        File file = new File(temporaryFile + CO_WAR_PATH);
        file.mkdirs();

        // 搜索war包
        List<String> warList = new ArrayList<>();
        //按后缀名过滤
        try (Stream<Path> paths = Files.walk(Paths.get(temporaryFile), 2)) {
            paths.map(Path::toString).filter(f -> f.endsWith(".war"))
                    .forEach(warList::add);
        } catch (IOException e) {
            NotifyUtil.notifyError(project, "查找war包失败" + Messages.getInformationIcon());
            throw new RuntimeException(e);
        }

        if (warList.size() != 1) {
            NotifyUtil.notifyError(project, "出错了！maven打包失败" + Messages.getInformationIcon());
            return;
        }

        // 解压war包
        try {
            File tempFile = new File(temporaryFile + CO_WAR_PATH);
            tempFile.delete();

            WarUtil.unCompressWar(warList.get(0), temporaryFile + CO_WAR_PATH);
        } catch (IOException | ArchiveException e) {
            NotifyUtil.notifyError(project, "解压war包失败！" + Messages.getInformationIcon());
            throw new RuntimeException(e);
        }

        String dist = temporaryFile + File.separator + "dist";

        new File(dist).delete();

        try {
            // 只移动 resources 和 src
            for (String path : svnSubmitData) {

                for (String fold : PACKAGING_FOLDER) {
                    if (path.contains(fold) && path.contains(".")) {
                        String[] paths = path.split(fold);
                        paths[1] = paths[1].replace(".java", ".class");

                        if (!new File(dist + WEB_INF_CASS_PATH + paths[1]).exists()) {
                            Files.createDirectories(Path.of(dist + WEB_INF_CASS_PATH + paths[1]).getParent());
                            Files.copy(Path.of(temporaryFile + CO_WAR_PATH + WEB_INF_CASS_PATH + paths[1]),
                                    Path.of(dist + WEB_INF_CASS_PATH + paths[1]));
                        }
                    }
                }
            }
        } catch (IOException e) {
            NotifyUtil.notifyError(project, "生成更新包失败！" + Messages.getInformationIcon());
            throw new RuntimeException(e);
        }

        NotifyUtil.notifyInfo(project, "打包成功,请查看 target/dist 文件夹");
    }

    /**
     * SVN 数据处理分割
     *
     * @param data
     * @param canonical
     * @return
     */
    private static List<String> svnDataHandling(String data, String canonical) {

        List<String> list = new ArrayList<>();

        Pattern pattern = Pattern.compile(canonical);
        Matcher matcher = pattern.matcher(data);
        while (matcher.find()) {
            //截取出括号中的内容
            String substring = matcher.group(1);
            list.add(substring);
        }
        return list;
    }

}
