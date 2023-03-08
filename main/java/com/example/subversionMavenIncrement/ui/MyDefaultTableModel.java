package com.example.subversionMavenIncrement.ui;

import javax.swing.table.DefaultTableModel;
import java.util.List;

/**
 * JTable组件
 *
 * @author 蚕豆的生活
 */
public class MyDefaultTableModel extends DefaultTableModel {

    private final Class<?>[] typeArray = {Boolean.class, String.class};

    /**
     * 表头
     */
    private final Object[] columnNames = {"是否选择", "文件"};

    /**
     * 数据
     */
    private Object[][] rowData;

    /**
     * 处理数据
     */
    public void setRowData(List<String> list){

        // 过滤不和规则的数据
        list.removeIf(e -> !e.contains(".") || e.contains("pom.xml") || e.contains("pom.jar"));

        rowData = new Object[list.size()][];

        // 默认全部选中
        int i = 0;
        for(String da : list){
            rowData[i] = new Object[]{Boolean.TRUE, da};
            i++;
        }
        setDataVector(rowData, columnNames);
    }

    public Object[][] getRowData(){
        return rowData;
    }

    /**
     * 使表格具有可编辑性
     *
     * @param rowIndex             the row whose value is to be queried
     * @param columnIndex          the column whose value is to be queried
     * @return
     */
    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return true;
    }

    /**
     * 只要覆盖tablemodel的getColumnClass返回一个boolean的class
     *
     * @param columnIndex  the column being queried
     * @return
     */
    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return typeArray[columnIndex];
    }

}
