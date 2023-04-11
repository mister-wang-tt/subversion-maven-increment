package com.example.subversionMavenIncrement;

import com.example.subversionMavenIncrement.service.CheckUpService;
import com.example.subversionMavenIncrement.ui.SvnFormDialog;
import com.example.subversionMavenIncrement.util.NotifyUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

/**
 * 菜单选择
 *
 * @author 蚕豆的生活
 */
public class ChoiceAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {

        DataContext dataContext = e.getDataContext();
        Project project = e.getProject();

        // 先初始化数据
        CheckUpService.init(project, dataContext);

        if(StringUtils.isNotEmpty(CheckUpService.RESOLVE_ADDRESS)) {
            try {
                SvnFormDialog svnFormDialog = new SvnFormDialog(project, dataContext);
                //是否允许用户通过拖拽的方式扩大或缩小你的表单框，我这里定义为true，表示允许
                svnFormDialog.setResizable(true);
                if(svnFormDialog.showAndGet()){
                    svnFormDialog.okTaskStart();
                }
            } catch (IOException | InterruptedException ex) {
                NotifyUtil.notifyError(project, "出错了！请求SVN错误" + Messages.getInformationIcon());
                throw new RuntimeException(ex);
            }
        }
    }
}
