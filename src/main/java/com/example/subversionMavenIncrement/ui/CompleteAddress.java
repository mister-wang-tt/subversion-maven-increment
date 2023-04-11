package com.example.subversionMavenIncrement.ui;

import com.example.subversionMavenIncrement.service.CheckUpService;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.project.Project;

import javax.swing.*;

/**
 * 地址收集表单（有maven子项目时用）
 *
 * @author 蚕豆的生活
 */
public class CompleteAddress {

    private JTextField textField1;
    private JPanel panel;

    private Project project;

    private DataContext dataContext;

    private CheckUpService checkUpService;

    /**
     * 初始化
     */
    public CompleteAddress(String path, Project project, DataContext dataContext, CheckUpService checkUpService) {
        textField1.setText(path);
        this.project = project;
        this.dataContext = dataContext;
        this.checkUpService = checkUpService;
    }

    /**
     * 保存地址
     */
    public void ok(){
        checkUpService.setXml(project, dataContext, textField1.getText());
    }

    public JPanel getContent() {
        return panel;
    }
}
