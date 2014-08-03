package com.github.simpleexpress.populate;


import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;

public class POIUtil
{
    public static XSSFCell getOrCreateCell(XSSFRow row, int column)
    {
        XSSFCell cell = row.getCell(column);
        if (cell == null)
        {
            cell = row.createCell(column);
            cell.setCellType(XSSFCell.CELL_TYPE_STRING);
            cell.setCellValue("");
        }

        return cell;
    }

    public static String getCellText(XSSFCell cell)
    {
        cell.setCellType(XSSFCell.CELL_TYPE_STRING);
        return cell.getStringCellValue().trim();
    }
}
