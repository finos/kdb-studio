package studio.kdb;

public class DictTableModel extends KTableModel {
    private final K.Dict dict;

    private final boolean keyFlip;
    private final boolean valueFlip;
    private final int keyCount;
    private final int valueCount;

    public DictTableModel(K.Dict obj) {
        super(obj.count());
        this.dict = obj;
        keyFlip = dict.x instanceof K.Flip;
        valueFlip = dict.y instanceof K.Flip;
        keyCount = keyFlip ? ((K.Flip)dict.x).x.getLength() : 1;
        valueCount = valueFlip ? ((K.Flip)dict.y).x.getLength() : 1;
    }

    public boolean isKey(int column) {
        return column < keyCount;
    }

    public int getColumnCount() {
        return keyCount + valueCount;
    }

    public String getColumnName(int col) {
        boolean keyColumn = col < keyCount;
        K.KBase obj = keyColumn ? dict.x : dict.y;
        int index = keyColumn ? col : col - keyCount;

        if (obj instanceof K.Flip) {
            K.KSymbolVector v = ((K.Flip) obj).x;
            return v.at(index).s;
        } else { //list
            return keyColumn ? "key" : "value";
        }
    }

    public K.KBaseVector<? extends K.KBase> getColumn(int col) {
        boolean keyColumn = col < keyCount;
        K.KBase obj = keyColumn ? dict.x : dict.y;
        int index = keyColumn ? col : col - keyCount;
        if (obj instanceof K.Flip) {
            return (K.KBaseVector<? extends K.KBase>) ((K.Flip)obj).y.at(index);
        } else { //list
            return (K.KBaseVector<? extends K.KBase>)obj;
        }
    }
};
