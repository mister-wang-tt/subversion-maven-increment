package com.example.subversionMavenIncrement.ui;

import com.example.subversionMavenIncrement.service.ChoiceActionService;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.IOException;

/**
 * 表单弹出框
 *
 * @author 蚕豆的生活
 */
public class SvnFormDialog extends DialogWrapper {

    private final SvnToolWindow svnToolWindow = new SvnToolWindow(this);

    public SvnFormDialog(@Nullable Project project, DataContext dataContext) throws IOException, InterruptedException {
        super(true);

        svnToolWindow.currentDateTime(ChoiceActionService.findSvnNotes(dataContext), project, dataContext);

        //设置会话框标题
        setTitle("选择svn数据生成");
        //触发一下init方法，否则swing样式将无法展示在会话框
        init();
    }

    /**
     * 返回位于会话框north位置的swing样式
     *
     * @return
     */
    @Override
    protected JComponent createNorthPanel() {
        return null;
    }

    /**
     * 特别说明：不需要展示SouthPanel要重写返回null，否则IDEA将展示默认的"Cancel"和"OK"按钮
     *
     * @return
     */
    @Override
    protected JComponent createSouthPanel() {
        return null;
    }

    /**
     * 定义表单的主题，放置到IDEA会话框的中央位置
     *
     * @return
     */
    @Override
    protected JComponent createCenterPanel() {
        return svnToolWindow.getContent();
    }
}
