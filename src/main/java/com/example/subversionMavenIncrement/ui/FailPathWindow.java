package com.example.subversionMavenIncrement.ui;

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.project.Project;

import javax.swing.*;
import java.util.List;

/**
 * 错误提示实现类
 *
 * @author 蚕豆的生活
 */
public class FailPathWindow {
    private JTable table1;
    private JPanel panel;

    MyFailPathTableModel myFailPathTableModel = new MyFailPathTableModel();

    /**
     * 初始化数据
     *
     * @param failPaths
     */
    public void currentDateTime(List<String> failPaths) {


        myFailPathTableModel.setRowData(failPaths);
        table1.setModel(myFailPathTableModel);

    }

    public JPanel getContent() {
        return this.panel;
    }

}
