package table;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import app.IMediator;

public class ProgressTableModel extends AbstractTableModel {

	private static final long serialVersionUID = -1440410634037827179L;

	private List<RowData> rows;
	private IMediator med;
	
	public ProgressTableModel(IMediator med) {
        rows = new ArrayList<>(100);
        
        this.med = med;
        this.med.registerProgressTableModel(this);
    }
	
	@Override
	public int getRowCount() {
		// TODO Auto-generated method stub
		return rows.size();
	}

	@Override
	public int getColumnCount() {
		// TODO Auto-generated method stub
		return 5;
	}
	
	 @Override
     public String getColumnName(int column) {
         String name = "??";
         switch (column) {
             case 0:
                 name = "Source";
                 break;
             case 1:
                 name = "Destination";
                 break;
             case 2:
                 name = "File Name";
                 break;
             case 3:
                 name = "Progress";
                 break;
             case 4:
                 name = "Status";
                 break;
         }
         return name;
     }
	
	 @Override
     public Object getValueAt(int rowIndex, int columnIndex) {
         RowData rowData = rows.get(rowIndex);
         Object value = null;
         switch (columnIndex) {
             case 0:
                 value = rowData.getSource();
                 break;
             case 1:
                 value = rowData.getDest();
                 break;
             case 2:
                 value = rowData.getFileName();
                 break;
             case 3:
                 value = rowData.getProgress();
                 break;
             case 4:
                 value = rowData.getStatus();
                 break;
         }
         return value;
     }

     @Override
     public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
         RowData rowData = rows.get(rowIndex);
         switch (columnIndex) {
         	case 3:
                 rowData.setProgress((int) aValue);
	             break;
             case 4:
                 rowData.setStatus((String) aValue);
                 break;
         }
     }
     
     public void addRow(RowData rowData) {
         rows.add(rowData);
         fireTableRowsInserted(rows.size() - 1, rows.size() - 1);
     }
     
     public void updateStatus(int index, int progress) {
    	 if (index < rows.size() && index >= 0) {
    		 setValueAt(progress, index, 3);
    		 fireTableCellUpdated(index, 3);
    		 if (progress == 100) {
    			 setValueAt("Complete", index, 4);
    			 fireTableCellUpdated(index, 4);
    		 }
    	 }
     }
     
     public void updateMessage(int index, String msg) {
    	 setValueAt(msg, index, 4);
    	 fireTableCellUpdated(index, 4);
     }
}
