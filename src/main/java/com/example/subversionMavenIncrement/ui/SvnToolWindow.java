package com.example.subversionMavenIncrement.ui;

import com.example.subversionMavenIncrement.run.MyThreadPoolExecutor;
import com.example.subversionMavenIncrement.service.ChoiceActionService;
import com.example.subversionMavenIncrement.util.NotifyUtil;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.project.Project;
import com.intellij.ui.JBColor;

import javax.swing.*;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * GUI实现类
 *
 * @author 蚕豆的生活
 */
public class SvnToolWindow {
    private JPanel panel;
    private JLabel isMvnPke;
    private JRadioButton isMvnRadioButton;
    private JTable table;
    private JScrollPane scrollPane;
    private JRadioButton isMvnRadioButtonNo;

    MyDefaultTableModel dataModel = new MyDefaultTableModel();

    private Project project;

    private DataContext dataContext;

    /**
     * 初始化
     */
    public SvnToolWindow() {

    }

    /**
     * 开始任务
     */
    public void okTaskStart(){
        // 选中需要打包的数据
        List<String> list = new ArrayList<String>();
        for(int i=0;i<table.getRowCount();i++){
            if((Boolean)table.getValueAt(i, 0)){
                list.add((String)table.getValueAt(i,1));
            }
        }

        MyThreadPoolExecutor.INSTANCE.getThreadPoolExecutor().submit(() -> {
            ChoiceActionService.backEnd(project, dataContext, list, isMvnRadioButton.isSelected());
        });

        NotifyUtil.notifyInfo(project, "开始在异步打包,请稍后,打包完成后提示！");
    }

    /**
     * 初始化表单数据
     *
     * @param list
     * @param project
     * @param dataContext
     */
    public void currentDateTime(List<String> list, Project project, DataContext dataContext) {

        this.project = project;
        this.dataContext = dataContext;

        dataModel.setRowData(list);
        table.setModel(dataModel);

        // 设置列头宽度
        TableColumn column = table.getColumnModel().getColumn(0);
        column.setPreferredWidth(80);
        column.setMaxWidth(80);
        column.setMinWidth(80);

        // 默认选中maven打包
        isMvnRadioButton.setSelected(true);
    }

    public JPanel getContent() {
        return panel;
    }
}
