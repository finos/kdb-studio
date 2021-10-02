package studio.ui.action;

import studio.kdb.K;
import studio.kdb.Server;

public class QueryResult {

    private String query;
    private Server server;

    private K.KBase result = null;
    private Throwable error = null;
    private long executionTime = -1;
    private boolean complete = false;

    public QueryResult(Server server, String query) {
        this.server = server;
        this.query = query;
    }

    public void setResult(K.KBase result) {
        this.result = result;
        complete = true;
    }

    public void setError(Throwable error) {
        this.error = error;
        complete = true;
    }

    public void setExecutionTime(long executionTime) {
        this.executionTime = executionTime;
    }

    public boolean isComplete() {
        return complete;
    }

    public String getQuery() {
        return query;
    }

    public Server getServer() {
        return server;
    }

    public K.KBase getResult() {
        return result;
    }

    public Throwable getError() {
        return error;
    }

    public long getExecutionTime() {
        return executionTime;
    }
}
