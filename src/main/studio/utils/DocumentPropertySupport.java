package studio.utils;

import javax.swing.text.AbstractDocument;
import javax.swing.text.Document;
import java.util.*;

public class DocumentPropertySupport {

    public static void addPropertyChangeListener(Document document, PropertyChangeListener listener) {
        getDocumentProperties(document).addPropertyChangeListener(listener);
    }

    public static void removePropertyChangeListener(Document document, PropertyChangeListener listener) {
        getDocumentProperties(document).removePropertyChangeListener(listener);
    }

    private static HashtableWithPropertySupport<Object,Object> getDocumentProperties(Document document) {
        AbstractDocument doc = (AbstractDocument) document;
        Dictionary<Object,Object> props = doc.getDocumentProperties();
        if (props instanceof HashtableWithPropertySupport) {
            return (HashtableWithPropertySupport<Object, Object>) props;
        }

        HashtableWithPropertySupport<Object, Object> newProps = new HashtableWithPropertySupport<>(props, document);
        doc.setDocumentProperties(newProps);
        return newProps;
    }

    private static class HashtableWithPropertySupport<K,V> extends Hashtable<K,V> {
        private final Document source;
        private final List<PropertyChangeListener> listeners = Collections.synchronizedList(new ArrayList<>());

        HashtableWithPropertySupport(Dictionary<K,V> dictionary, Document source) {
            super(dictionary.size());
            this.source = source;
            Enumeration<K> keys = dictionary.keys();
            while(keys.hasMoreElements()) {
                K key = keys.nextElement();
                V value = dictionary.get(key);
                super.put(key, value);
            }
        }

        public void addPropertyChangeListener(PropertyChangeListener listener) {
            listeners.add(listener);
        }

        public void removePropertyChangeListener(PropertyChangeListener listener) {
            listeners.remove(listener);
        }

        private void fireEvent(Object key, V oldValue, V newValue) {
            if (Objects.equals(oldValue, newValue)) return;

            PropertyEvent event = new PropertyEvent(source, key, oldValue, newValue);
            PropertyChangeListener[] list = listeners.toArray(new PropertyChangeListener[0]);
            for (PropertyChangeListener aListener: list) {
                aListener.propertyChanged(event);
            }
        }

        @Override
        public synchronized V put(K key, V value) {
            V oldValue = super.put(key, value);
            fireEvent(key, oldValue, value);
            return oldValue;
        }

        @Override
        public synchronized V remove(Object key) {
            V oldValue = super.remove(key);
            fireEvent(key, oldValue, null);
            return oldValue;
        }

    }

    public static class PropertyEvent extends EventObject {
        private Object key, oldValue, newValue;
        public PropertyEvent(Object source, Object key, Object oldValue, Object newValue) {
            super(source);
            this.key = key;
            this.oldValue = oldValue;
            this.newValue = newValue;
        }
        public Object getKey() {
            return key;
        }
        public Object getOldValue() {
            return oldValue;
        }
        public Object getNewValue() {
            return newValue;
        }
    }

    public interface PropertyChangeListener extends EventListener {
        void propertyChanged(PropertyEvent event);
    }
}
