package studio.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.*;
import java.sql.Time;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static java.nio.file.StandardWatchEventKinds.*;

public class FileWatcher implements Runnable {

    public interface Listener {
        void fileModified(Path path);
    }

    private static final Logger log = LogManager.getLogger();
    private static WatchService watchService = null;
    private static final Map<Path, WatchKey> watchKeys = new HashMap<>();
    private static final Map<Listener, Path> listeners = new HashMap<>();

    public static synchronized void addListener(Path path, Listener listener) {
        if (watchService == null) {
            log.warn("Failed to watch {} as watchService is not initialized", path);
            return;
        }
        if (! Files.exists(path) ) {
            log.warn("File " + path + " is not exist");
        }

        Path folder = path.getParent();
        if(! Files.exists(folder)) {
            log.error("Folder {} doesn't exist. File {} will not be monitored", folder, path);
            return;
        }

        if (watchKeys.get(folder) == null) {
            try {
                WatchKey watchKey = folder.register(watchService, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE);
                watchKeys.put(folder, watchKey);
            } catch (IOException e) {
                log.error("Failed to start watching folder {}", folder, e);
                return;
            }
        }

        Path oldPath = listeners.get(listener);
        if (oldPath !=null) {
            log.error("Ops... That's not expected. A listener found in FileWatcher.addListener for {}.", path);
            removeListener(listener);
        }
        listeners.put(listener, path);
        log.info("Start watching {}", path);
    }

    public static synchronized void removeListener(Listener listener) {
        Path path = listeners.remove(listener);
        if (path == null) {
            log.error("Ops... That's not expected. A listener not found in FileWatch.removeListener");
            return;
        }

        Path folder = path.getParent();
        boolean cancelWatch = true;
        for (Path p: listeners.values()) {
            if (folder.equals(p.getParent())) {
                cancelWatch = false;
                break;
            }
        }

        if (cancelWatch) {
            WatchKey watchKey = watchKeys.remove(folder);
            if (watchKey == null) {
                log.error("Ops... That's not expected. There is no watchKey for folder {}", folder);
            } else {
                watchKey.cancel();
                log.error("Stop watching folder {}", folder);
            }
        }
        log.info("Stop watching file {}", path);
    }

    private static synchronized void notify(Path filename, WatchKey watchKey) {
        for (Map.Entry<Path, WatchKey> entry: watchKeys.entrySet()) {
            if (! entry.getValue().equals(watchKey)) continue;
            Path path = entry.getKey().resolve(filename);

            for (Map.Entry<Listener,Path> listenerEntry: listeners.entrySet()) {
                if (! listenerEntry.getValue().equals(path)) continue;

                Listener listener = listenerEntry.getKey();
                SwingUtilities.invokeLater(() -> listener.fileModified(path));
            }
        }
    }

    public static synchronized void start() {
        if (watchService != null) {
            log.error("Ups.. watchService was already started");
            return;
        }
        try {
            watchService = FileSystems.getDefault().newWatchService();
            new Thread(new FileWatcher(), "File watcher").start();
        } catch (IOException e) {
            log.error("Failed to initialized file system watcher", e);
            watchService = null;
        }
    }

    public void run() {
        try {
            while (true) {
                WatchKey watchKey = watchService.take();
                for(WatchEvent<?> event: watchKey.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();
                    if (kind == OVERFLOW) continue;
                    Path filename = (Path) event.context();
                    notify(filename, watchKey);
                }
                watchKey.reset();
            }
        } catch (InterruptedException e) {
            log.info("File watcher was interrupted. Stop watching file changes");
            try {
                watchService.close();
            } catch (IOException ioe) {
                log.error("Error on closing watch service");
            }
        }
        watchService = null;
    }
}
