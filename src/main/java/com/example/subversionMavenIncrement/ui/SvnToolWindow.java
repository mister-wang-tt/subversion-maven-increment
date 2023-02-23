package com.example.subversionMavenIncrement.ui;

import com.example.subversionMavenIncrement.service.ChoiceActionService;
import com.example.subversionMavenIncrement.service.NotifyUtil;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.project.Project;

import javax.swing.*;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author wht
 */
public class SvnToolWindow {
    private JPanel panel;
    private JLabel isMvnPke;
    private JRadioButton isMvnRadioButton;
    private JButton okButton;
    private JTable table;
    private JScrollPane scrollPane;
    private JRadioButton isMvnRadioButtonNo;
    private JButton cancel;

    private SvnFormDialog svnFormDialog;

    MyDefaultTableModel dataModel = new MyDefaultTableModel();

    private Project project;

    private DataContext dataContext;

    /**
     * 创建线程池
     */
    private final static ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(2, 3, 2,
            TimeUnit.SECONDS,
            // 队列
            new ArrayBlockingQueue<Runnable>(5),
            // CallerRunsPolicy策略确保竞争失败的线程也能被执行
            new ThreadPoolExecutor.CallerRunsPolicy());

    public SvnToolWindow(SvnFormDialog svnFormDialog) {

        // 确定监听
        okButton.addActionListener(e -> {

            // 选中需要打包的数据
            List<String> list = new ArrayList<String>();
            for(int i=0;i<table.getRowCount();i++){
                if((Boolean)table.getValueAt(i, 0)){
                    list.add((String)table.getValueAt(i,1));
                }
            }

            threadPoolExecutor.submit(() -> {
                ChoiceActionService.backEnd(project, dataContext, list, isMvnRadioButton.isSelected());
            });
            svnFormDialog.disposeIfNeeded();
            NotifyUtil.notifyInfo(project, "Subversion Maven Increment 开始在异步打包,请稍后,打包完成后提示！");
        });

        // 取消监听
        cancel.addActionListener(e -> svnFormDialog.disposeIfNeeded());
    }

    public void currentDateTime(List<String> list, Project project, DataContext dataContext) {

        this.project = project;
        this.dataContext = dataContext;

        dataModel.setRowData(list);
        table.setModel(dataModel);
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
