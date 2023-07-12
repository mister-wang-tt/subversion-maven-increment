package com.example.subversionMavenIncrement.service;

import com.example.subversionMavenIncrement.StaticValue;
import com.example.subversionMavenIncrement.run.ExecuteCommand;
import com.example.subversionMavenIncrement.util.FileUtil;
import com.example.subversionMavenIncrement.util.NotifyUtil;
import com.example.subversionMavenIncrement.util.WarUtil;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vcs.RepositoryLocation;
import com.intellij.openapi.vcs.history.VcsFileRevision;
import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import com.intellij.openapi.vcs.vfs.VcsVirtualFolder;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.lang.StringUtils;
import org.dom4j.*;
import org.dom4j.io.SAXReader;

import java.awt.*;
import java.io.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
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

    private static final String CLASSES = File.separator + "classes";

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

        // 获取选中的svn相关数据 VCS_FILE_REVISIONS -> {VcsFileRevision[4]@54552} VCS_REVISION_NUMBERS -> {VcsRevisionNumber[7]@50905}
//        VcsRevisionNumber[] vcsRevisionNumbers = (VcsRevisionNumber[]) dataContext.getData("VCS_REVISION_NUMBERS");
        VcsFileRevision[] vcsFileRevisions = (VcsFileRevision[]) dataContext.getData("VCS_FILE_REVISIONS");
//        VcsFileRevision vcsFileRevision = (VcsFileRevision) dataContext.getData("VCS_FILE_REVISION");
//        RepositoryLocation changedRepositoryPath = vcsFileRevision.getChangedRepositoryPath();

        Set<String> svnRecords = new HashSet<>();

        for(VcsFileRevision vcs : vcsFileRevisions) {
            // 获取svn提交的历史记录
            String s = ExecuteCommand.carrOut(null, "svn log",
                    vcs.getChangedRepositoryPath().toPresentableString(),
                    "-r", vcs.getRevisionNumber().asString(),
                    "-v");
            List<String> strings = svnDataHandling(s, " [M|A] /(.*?)\r\n");
            svnRecords.addAll(strings);
        }

        return new ArrayList<>(svnRecords);
    }

    /**
     * 生成更新包后续处理 war包获取
     *
     * @param project
     * @param dataContext
     * @param svnSubmitData
     * @param isMvnRadioButton
     */
    public static void backEndWar(Project project, DataContext dataContext, List<String> svnSubmitData, Boolean isMvnRadioButton, Boolean packButtonYes) {

        VcsVirtualFolder filePathOn = (VcsVirtualFolder)dataContext.getData("VCS_VIRTUAL_FILE");

        // 是否使用maven打包
        if (isMvnRadioButton) {
            try {

                // 先找本机 maven，没有找到在使用环境变量 mvn 命令
                String mavenPath = obtainMaven(filePathOn.getPath());
                if(StringUtils.isEmpty(mavenPath)) {
                    mavenPath = "mvn";
                }else {
                    mavenPath += "mvn";
                    String os = System.getProperty("os.name");
                    if (os != null && os.toUpperCase().contains(StaticValue.WINDOWS.toString())) {
                        mavenPath += ".cmd";
                    }
                }

                ExecuteCommand.carrOut(new File(filePathOn.getPath()), mavenPath + " clean package",
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
            FileUtil.deleteDirectoryStream(tempFile.getPath());

            WarUtil.unCompressWar(warList.get(0), temporaryFile + CO_WAR_PATH);
        } catch (IOException | ArchiveException e) {
            NotifyUtil.notifyError(project, "解压war包失败！" + Messages.getInformationIcon());
            throw new RuntimeException(e);
        }

        String dist = temporaryFile + File.separator + "dist";

        FileUtil.deleteDirectoryStream(dist);

        try {
            // 只移动 resources 和 src
            for (String path : svnSubmitData) {

                for (String fold : PACKAGING_FOLDER) {
                    String[] paths = path.split(fold);

                    // 文件打包
                    if(!packButtonYes) {
                        if (path.contains(fold) && path.contains(".")) {
                            paths[1] = paths[1].replace(".java", ".class");

                            if (!new File(dist + WEB_INF_CASS_PATH + paths[1]).exists()) {
                                Path parent = Path.of(temporaryFile + CO_WAR_PATH + WEB_INF_CASS_PATH + paths[1]).getParent();
                                String pathParent = Path.of(dist + WEB_INF_CASS_PATH + paths[1]).getParent().toString();
                                Files.createDirectories(Path.of(dist + WEB_INF_CASS_PATH + paths[1]).getParent());
                                Files.copy(Path.of(temporaryFile + CO_WAR_PATH + WEB_INF_CASS_PATH + paths[1]),
                                        Path.of(dist + WEB_INF_CASS_PATH + paths[1]));

                                // 查找子类文件也加入打包中
                                subClass(paths, parent, pathParent);
                            }
                        }
                    }
                    // 文件夹打包
                    else {
                        if (path.contains(fold)) {
                            FileUtil.copyFolder(Paths.get(temporaryFile + CO_WAR_PATH + WEB_INF_CASS_PATH + paths[1]).getParent().toFile(), Paths.get(dist + WEB_INF_CASS_PATH + paths[1]).getParent().toFile());
                        }
                    }
                }
            }

            // 生成完后打开文件夹
            Desktop.getDesktop().open(new File(dist));
        } catch (IOException e) {
            NotifyUtil.notifyError(project, "生成更新包失败！" + Messages.getInformationIcon());
            throw new RuntimeException(e);
        }

        NotifyUtil.notifyInfo(project, "打包成功,请查看 target/dist 文件夹");
    }

    /**
     * 生成更新包后续处理 classes文件夹获取
     *
     * @param project
     * @param dataContext
     * @param svnSubmitData
     */
    public static void backEndClasses(Project project, DataContext dataContext, List<String> svnSubmitData, Boolean packButtonYes) {

        // 先创建临时时文件夹
        String temporaryFile = CheckUpService.RESOLVE_ADDRESS + File.separator + "target";

        String dist = temporaryFile + File.separator + "dist";

        FileUtil.deleteDirectoryStream(dist);

        try {
            // 只移动 resources 和 src
            for (String path : svnSubmitData) {

                for (String fold : PACKAGING_FOLDER) {
                    String[] paths = path.split(fold);

                    // 文件打包
                    if(!packButtonYes) {
                        if (path.contains(fold) && path.contains(".")) {
                            paths[1] = paths[1].replace(".java", ".class");

                            if (!new File(dist + CLASSES + paths[1]).exists()) {
                                Path parent = Path.of(temporaryFile + CLASSES + paths[1]).getParent();
                                String pathParent = Path.of(dist + CLASSES + paths[1]).getParent().toString();
                                Files.createDirectories(Path.of(dist + CLASSES + paths[1]).getParent());
                                Files.copy(Path.of(temporaryFile + CLASSES + paths[1]),
                                        Path.of(dist + CLASSES + paths[1]));

                                // 查找子类文件也加入打包中 只争对.class文件
                                subClass(paths, parent, pathParent);

                            }
                        }
                    }
                    // 文件夹打包
                    else {
                        if (path.contains(fold)) {
                            FileUtil.copyFolder(Paths.get(temporaryFile + CLASSES + paths[1]).getParent().toFile(),
                                    Paths.get(dist + CLASSES + paths[1]).getParent().toFile());
                        }
                    }
                }
            }

            // 生成完后打开文件夹
            Desktop.getDesktop().open(new File(dist));
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

            if(substring.contains("(")) {
                substring = substring.replaceAll("\\s", "");
                substring = substring.split("\\(")[0];
            }

            list.add(substring);
        }
        return list;
    }

    /**
     * 查找子类文件也加入打包中 只争对.class文件
     *
     * @param paths
     * @param parent
     * @param pathParent
     * @throws IOException
     */
    private static void subClass(String[] paths, Path parent, String pathParent) throws IOException {
        // 查找子类文件也加入打包中 只争对.class文件
        String s1 = Path.of(paths[1]).getFileName().toString();
        if(s1.contains(".class")) {
            String pathsFileName = s1.replace(".class", "");
            Files.list(parent)
                    .filter(Files::isRegularFile)
                    .forEach(subPath -> {
                        String s = subPath.getFileName().toString();
                        if(s.contains(".class")) {
                            String replace = s.replace(".class", "");
                            if(replace.contains(pathsFileName)
                                    && !replace.equals(pathsFileName)) {
                                try {
                                    Files.copy(subPath, Path.of(pathParent + File.separator + s));
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    });
        }
    }

    /**
     * 获取 idea配置的本机Maven 地址
     *
     * @param filePathOn
     * @return
     * @throws DocumentException
     */
    private static String obtainMaven(String filePathOn) {

        String mavenPath = null;
        String userSettingsFile = null;

        try {
            SAXReader reader = new SAXReader();
            // 加载xml文件
            Document dc= reader.read(new File(filePathOn + File.separator + ".idea" + File.separator + "workspace.xml"));

            // 获取根节点
            Element e = dc.getRootElement();

            Element mavenImportPreferences = null;

            // 查找 MavenImportPreferences
            mi:
            for (Iterator<Element> i = e.elementIterator("component"); i.hasNext();) {
                Element works = (Element) i.next();
                for (Attribute att : works.attributes()) {
                    if("MavenImportPreferences".equals(att.getValue())){
                        mavenImportPreferences = works;
                        break mi;
                    }
                }
            }


            if(null == mavenImportPreferences){
                return mavenPath;
            }

            Element generalSettings = null;
            mi:
            for (Iterator<Element> i = mavenImportPreferences.elementIterator("option"); i.hasNext();) {
                Element option = (Element) i.next();
                for (Attribute att : option.attributes()) {
                    if("generalSettings".equals(att.getValue())){
                        generalSettings = option;
                        break mi;
                    }
                }
            }

            if(null == generalSettings){
                return mavenPath;
            }

            Element mavenGeneralSettings = generalSettings.element("MavenGeneralSettings");
            mi:
            for (Iterator<Element> i = mavenGeneralSettings.elementIterator("option"); i.hasNext();) {
                Element option = (Element) i.next();

                for (int j= 0; j < option.attributes().size(); j++){
                    if("userSettingsFile".equals(option.attributes().get(j).getValue())){
                        userSettingsFile = option.attributes().get(j + 1).getValue();
                        break mi;
                    }
                }
            }

            if(StringUtils.isNotEmpty(userSettingsFile) && userSettingsFile.contains("conf")) {
                mavenPath = userSettingsFile.split("conf")[0] + "bin" + File.separator;
            }

        }catch (Exception e){
            e.printStackTrace();
        }

        return mavenPath;
    }

}
