package com.example.subversionMavenIncrement.ui;

import com.example.subversionMavenIncrement.service.CheckUpService;
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
public class ComAddressFormDialog extends DialogWrapper {

    private final CompleteAddress completeAddress;

    public ComAddressFormDialog(String path, @Nullable Project project, DataContext dataContext, CheckUpService checkUpService) throws IOException, InterruptedException {
        super(true);

        completeAddress = new CompleteAddress(path, project, dataContext, checkUpService);

        //设置会话框标题
        setTitle("获取地址(Obtain Address)");
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
     * 开始任务
     */
    public void okTaskStart(){
        completeAddress.ok();
    }

    /**
     * 定义表单的主题，放置到IDEA会话框的中央位置
     *
     * @return
     */
    @Override
    protected JComponent createCenterPanel() {
        return completeAddress.getContent();
    }
}
