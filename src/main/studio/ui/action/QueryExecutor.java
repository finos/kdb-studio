package studio.ui.action;

import kx.ProgressCallback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import studio.kdb.*;
import studio.ui.EditorTab;
import studio.ui.StudioPanel;

import javax.swing.*;
import java.util.concurrent.atomic.AtomicInteger;

public class QueryExecutor implements ProgressCallback {

    private static final Logger log = LogManager.getLogger();
    private static final Logger queryLog = LogManager.getLogger("Query");
    private static final AtomicInteger queryIndex = new AtomicInteger(1);

    private Worker worker = null;
    private final EditorTab editor;

    private volatile ProgressMonitor pm;
    private boolean compressed;
    private int msgLength;

    private static final String[] suffix = {"B", "K", "M"};
    private static final double[] factor = {1, 1024, 1024*1024};
    private int progressNoteIndex = 0;

    public QueryExecutor(EditorTab editor) {
        this.editor = editor;
    }

    public void execute(K.KBase query) {
        worker = new Worker(queryIndex.getAndIncrement(), editor.getServer(), query);
        worker.execute();
    }

    public void execute(String query) {
        worker = new Worker(queryIndex.getAndIncrement(), editor.getServer(), query);
        worker.execute();
    }

    public void cancel() {
        if (worker == null) return;
        if (worker.isDone()) return;
        worker.closeConnection();
        worker.cancel(true);
        worker = null;
    }

    public boolean running() {
        return worker != null && ! worker.isDone();
    }

    @Override
    public void setCompressed(boolean compressed) {
        this.compressed = compressed;
    }

    private String formatProgressNote(int total) {
        if (progressNoteIndex == 0) {
            return String.format("%,d of %,d B", total, msgLength);
        } else {
            return String.format("%,.1f of %,.1f %s", total / factor[progressNoteIndex], msgLength / factor[progressNoteIndex], suffix[progressNoteIndex]);
        }
    }

    @Override
    public void setMsgLength(int msgLength) {
        this.msgLength = msgLength;

        if (msgLength < 2*1024) progressNoteIndex = 0;
        else if (msgLength < 2*1024*1024) progressNoteIndex = 1;
        else progressNoteIndex = 2;

        UIManager.put("ProgressMonitor.progressText", "Studio for kdb+");
        pm = new ProgressMonitor(editor.getTextArea(), "Receiving " + (compressed ? "compressed " : "") + "data ...",
                                    formatProgressNote(0), 0, msgLength);
        SwingUtilities.invokeLater( () -> {
            pm.setMillisToDecideToPopup(300);
            pm.setMillisToPopup(100);
            pm.setProgress(0);
        });
    }

    @Override
    public void setCurrentProgress(int total) {
        SwingUtilities.invokeLater( () -> {
            pm.setProgress(total);
            pm.setNote(formatProgressNote(total));
        });

        if (pm.isCanceled()) {
            cancel();
        }
    }

    private class Worker extends SwingWorker<QueryResult, Integer> {

        private volatile Server server;
        private volatile K.KBase query;
        private volatile String queryText;
        private volatile kx.c c = null;
        private final int queryIndex;

        public Worker(int queryIndex, Server server, K.KBase query) {
            this.queryIndex = queryIndex;
            this.server = server;
            this.query = query;
            this.queryText = "<upload to server>";
        }

        public Worker(int queryIndex, Server server, String query) {
            this.queryIndex = queryIndex;
            this.server = server;
            this.query = new K.KCharacterVector(query);
            this.queryText = query;
        }

        void closeConnection() {
            if (c!=null) {
                c.close();
            }
        }

        @Override
        protected QueryResult doInBackground() {
            QueryResult result = new QueryResult(server, queryText);
            queryLog.info("#{}: query {}({})\n{}",queryIndex, server.getFullName(), server.getConnectionString(), queryText);
            long startTime = System.currentTimeMillis();
            try {
                c = ConnectionPool.getInstance().leaseConnection(server);
                K.KBase response = c.k(query, QueryExecutor.this);
                result.setResult(response);
            } catch (Throwable e) {
                if (! (e instanceof kx.c.K4Exception)) {
                    log.error("Error occurred during query execution",e);
                    if (c != null) c.close();
                }
                result.setError(e);
            } finally {
                if (c!=null) {
                    ConnectionPool.getInstance().freeConnection(server, c);
                }
            }
            result.setExecutionTime(System.currentTimeMillis() - startTime);
            if (result.getError() != null) {
                if (result.getError() instanceof kx.c.K4Exception) {
                    queryLog.info("#{}: server returns error {}", queryIndex, result.getError().getMessage());
                } else {
                    queryLog.info("#{}: error during execution {} {}", queryIndex, result.getError().getClass().getName(), result.getError().getMessage());
                }
            } else {
                queryLog.info("#{}: type={}, count={}, time={}", queryIndex, result.getResult().getType(), result.getResult().count(), result.getExecutionTime());
            }
            return result;
        }

        @Override
        protected void done() {
            if (pm != null) {
                pm.close();
            }
            try {
                QueryResult result;
                if (isCancelled()) {
                    result = new QueryResult(server, queryText);
                    queryLog.info("#{}: Cancelled", queryIndex);
                } else {
                    result = get();
                }
                StudioPanel.queryExecutionComplete(editor, result);
            } catch (Exception e) {
                log.error("Ops... It wasn't expected", e);
            }
        }
    }
}
