package edu.cmu.inmind.multiuser.socialreasoner.control.emulators;

import edu.cmu.inmind.multiuser.socialreasoner.model.Constants;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by oscarr on 12/13/16.
 */
public class ExcelReaderWriter {
    private XSSFWorkbook workbook;
    private int rowOffset = 7;
    private int colOffset = 6;
    private int currentSheetIdx = 0;
    private String docName = "";
    private int maxNumRows = 0;


    public void openWorkbook(String fileName){
        try{
            if( workbook == null || !docName.equals(fileName)) {
                FileInputStream inputStream = new FileInputStream(fileName);
                workbook = new XSSFWorkbook(inputStream);
                inputStream.close();
                SXSSFWorkbook wb = new SXSSFWorkbook(workbook);
                wb.setCompressTempFiles(true);
                docName = fileName;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public List<SystemIntentStep> readSheet(){
        Sheet sheet = workbook.getSheetAt( currentSheetIdx );
        Row row;
        List<SystemIntentStep>  intents = new ArrayList<>();
        if( !sheet.getSheetName().equals("Comparison") ) {
            boolean isFarewell = false;
            int rowPos = rowOffset;
            int lastRow = sheet.getLastRowNum();
            for ( ; rowPos < lastRow+1 && !isFarewell; rowPos++) {
                row = sheet.getRow(rowPos);
                if (row != null && row.getCell(0) != null ) {
                    SystemIntentStep intent = new SystemIntentStep(
                            row.getCell(0) == null? "" : row.getCell(0).getStringCellValue(), "",
                            row.getCell(1) == null? 0 : row.getCell(1).getNumericCellValue(),
                            row.getCell(2) == null? "" : row.getCell(2).getStringCellValue(),
                            row.getCell(3) == null? false :row.getCell(3).getStringCellValue().equals("SMILE") ? true : false,
                            row.getCell(4) == null? false :row.getCell(4).getStringCellValue().equals("GAZE_PARTNER") ? true : false,
                            colOffset > 5? row.getCell(4) == null? false : row.getCell(4).getStringCellValue().equals("AVAILABLE") : false);
                    intents.add(intent);
                    if( intent.getIntent().equals(Constants.FAREWELL) ){
                        isFarewell = true;
                    }
                }
            }
            if( rowPos > maxNumRows ){
                maxNumRows = rowPos;
            }
        }
        return intents;
    }


    public int getNumSheets(){
        return workbook.getNumberOfSheets();
    }

    public void setColOffset(int colOffset){
        this.colOffset = colOffset;
    }

    public void writeSheet(String results){
        try {
            String[] rows = results.split("\\n");
            Sheet sheet = workbook.getSheetAt(currentSheetIdx);
            Row row;
            int numRows = sheet.getLastRowNum();
            for (int rowPos = rowOffset; rowPos < numRows + 1 && (rowPos - rowOffset) < rows.length; rowPos++) {
                row = sheet.getRow(rowPos);
                if (row != null) {
                    String[] columns = rows[rowPos - rowOffset].split("\\t");
                    for (int colPos = colOffset; colPos < columns.length; colPos++) {
                        if (row.getCell(colPos) != null) {
                            row.getCell(colPos).setCellValue(columns[colPos]);
                        } else {
                            row.createCell(colPos).setCellValue(columns[colPos]);
                        }
                    }
                }
            }
            currentSheetIdx++;
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public boolean checkFinished(){
        if( currentSheetIdx >= getNumSheets() ){
            return true;
        }
        return false;
    }

    public void writeComparison() {
        Sheet sheetComparison = workbook.getSheetAt( currentSheetIdx );
        if( sheetComparison.getSheetName().equals("Comparison") ) {
            CellStyle cs = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            cs.setFont(font);
            for( int sheetIdx = 0; sheetIdx < getNumSheets() - 1; sheetIdx++ ){
                Sheet sheet = workbook.getSheetAt( sheetIdx );
                //headers
                Row rowHeader = sheetComparison.getRow( rowOffset - 1 );
                rowHeader.createCell( sheetIdx + 1).setCellValue( sheet.getSheetName() );
                rowHeader.getCell( sheetIdx + 1).setCellStyle(cs);
                //values
                boolean isFarewell = false;
                for( int rowIdx = rowOffset; rowIdx < maxNumRows + 1 && !isFarewell; rowIdx++ ){
                    Row rowComparison = sheetComparison.getRow( rowIdx );
                    Row row = sheet.getRow( rowIdx );
                    if( row == null ){
                        row = sheet.createRow( rowIdx );
                    }
                    if( row.getCell(0) != null && row.getCell(0).getStringCellValue().equals(Constants.FAREWELL)){
                        isFarewell = true;
                    }
                    if (rowComparison.getCell(0) == null || rowComparison.getCell(0).getStringCellValue().equals("")) {
                        rowComparison.createCell(0).setCellValue(row.getCell(0) == null ? "" :
                                row.getCell(0).getStringCellValue());
                    }
                    rowComparison.createCell(sheetIdx + 1).setCellValue(row.getCell( colOffset ) == null ? "" :
                            row.getCell( colOffset ).getStringCellValue());
                }
            }
            writeAndClose();
        }
    }

    public void writeAndClose(){
        try {
            FileOutputStream fileOut = new FileOutputStream(docName);
            workbook.write(fileOut);
            fileOut.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public String getScenarioName() {
        return workbook.getSheetAt( currentSheetIdx ).getSheetName();
    }
}
