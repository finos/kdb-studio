package studio.kdb;

import javax.swing.table.AbstractTableModel;

public abstract class KTableModel extends AbstractTableModel {

    public abstract boolean isKey(int column);
    public abstract K.KBaseVector<? extends K.KBase> getColumn(int col);
    public abstract String getColumnName(int col) ;

    public static KTableModel getModel(K.KBase obj) {
        if (obj instanceof K.Flip) {
            return new FlipTableModel((K.Flip) obj);
        }

        if (obj instanceof K.Dict) {
            K.Dict dict = (K.Dict) obj;
            if ( (dict.x instanceof K.KBaseVector || dict.x instanceof K.Flip) &&
                 (dict.y instanceof K.KBaseVector || dict.y instanceof K.Flip) ) {
                return new DictTableModel(dict);
            } else {
                return null;
            }
        }

        if ((obj instanceof K.KBaseVector) && obj.getType() != 10 && obj.getType() != 4) {
            return new ListModel((K.KBaseVector<? extends K.KBase>)obj);
        }
        return null;
    }

    protected int[] index;
    protected boolean ascSorted;
    protected int sortedByColumn;

    protected KTableModel(int rowCount) {
        index = new int[rowCount];
        ascSorted = true;
        initIndex();
    }

    private void initIndex() {
        int k = ascSorted ? 1 : -1;
        int b = ascSorted ? 0 : index.length - 1;
        for (int i = 0; i< index.length; i++) {
            index[i] = b + k*i;
        }
        sortedByColumn = -1;
    }

    public int[] getIndex() {
        return index;
    }

    public void sort(int col) {
        if (sortedByColumn == col) {
            if (ascSorted) {
                ascSorted = false;
            } else {
                ascSorted = true;
                col = -1;
            }
        } else {
            ascSorted = true;
        }
        if (col == -1) {
            initIndex();
        } else {
            K.KBaseVector<? extends K.KBase> array = getColumn(col);
            if (sortedByColumn == col) {
                index = Sorter.reverse(array, index);
            } else {
                index = Sorter.sort(array, index);
            }
        }
        sortedByColumn = col;

        fireTableDataChanged();
    }

    public boolean isSortedAsc(int column) {
        return ascSorted && sortedByColumn == column;
    }

    public boolean isSortedDesc(int column) {
        return !ascSorted && sortedByColumn == column;
    }

    public Class getColumnClass(int col) {
        return getColumn(col).getClass();
    }
    //@TODO: add separate method which return K.KBase
    public Object getValueAt(int row,int col) {
        row = index[row];
        K.KBaseVector<? extends K.KBase> v = getColumn(col);
        return v.at(row);
    }

    public int getRowCount() {
        return getColumn(0).getLength();
    }

}
