package studio.kdb;

public class ListModel extends KTableModel {
    private final K.KBaseVector<? extends K.KBase> list;

    public ListModel(K.KBaseVector<? extends K.KBase> list) {
        super(list.count());
        this.list = list;
    }

    @Override
    public boolean isKey(int column) {
        return false;
    }

    @Override
    public K.KBaseVector<? extends K.KBase> getColumn(int col) {
        return list;
    }

    @Override
    public String getColumnName(int col) {
        return "value";
    }

    @Override
    public int getColumnCount() {
        return 1;
    }
}
