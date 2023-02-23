package com.example.subversionMavenIncrement;

import com.example.subversionMavenIncrement.ui.SvnFormDialog;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

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

        try {
            SvnFormDialog svnFormDialog = new SvnFormDialog(project, dataContext);
            //是否允许用户通过拖拽的方式扩大或缩小你的表单框，我这里定义为true，表示允许
            svnFormDialog.setResizable(true);
            svnFormDialog.show();
        } catch (IOException | InterruptedException ex) {
            Messages.showMessageDialog(project, "出错了！请求SVN错误", "提示", Messages.getInformationIcon());
            throw new RuntimeException(ex);
        }
    }
}
