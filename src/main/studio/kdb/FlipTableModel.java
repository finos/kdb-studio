package studio.kdb;

public class FlipTableModel extends KTableModel {

    private final K.Flip flip;

    public FlipTableModel(K.Flip obj) {
        super(obj.count());
        flip = obj;
    }

    public boolean isKey(int column) {
        return false;
    }

    public int getColumnCount() {
        return flip.x.getLength();
    }

    public String getColumnName(int i) {
        return flip.x.at(i).s;
    }

    public K.KBaseVector<? extends K.KBase> getColumn(int col) {
        return (K.KBaseVector<? extends K.KBase>) flip.y.at(col);
    }
};
