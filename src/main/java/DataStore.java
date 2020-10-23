import com.google.api.client.util.store.DataStoreFactory;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.Set;

public class DataStore implements com.google.api.client.util.store.DataStore {

    @Override
    public DataStoreFactory getDataStoreFactory() {
        return null;
    }

    @Override
    public String getId() {
        return null;
    }

    @Override
    public int size() throws IOException {
        return 0;
    }

    @Override
    public boolean isEmpty() throws IOException {
        return false;
    }

    @Override
    public boolean containsKey(String s) throws IOException {
        return false;
    }

    @Override
    public boolean containsValue(Serializable serializable) throws IOException {
        return false;
    }

    @Override
    public Set<String> keySet() throws IOException {
        return null;
    }

    @Override
    public Collection values() throws IOException {
        return null;
    }

    @Override
    public Serializable get(String s) throws IOException {
        return null;
    }

    @Override
    public com.google.api.client.util.store.DataStore set(String s, Serializable serializable) throws IOException {
        return null;
    }

    @Override
    public com.google.api.client.util.store.DataStore clear() throws IOException {
        return null;
    }

    @Override
    public com.google.api.client.util.store.DataStore delete(String s) throws IOException {
        return null;
    }
}
