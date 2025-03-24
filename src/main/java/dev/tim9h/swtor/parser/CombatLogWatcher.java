package dev.tim9h.swtor.parser;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.function.Consumer;

import javax.swing.filechooser.FileSystemView;

import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.inject.Inject;

import dev.tim9h.swtor.parser.bean.CombatLog;
import dev.tim9h.swtor.parser.utils.CombatlogParserException;
import dev.tim9h.swtor.parser.utils.CombatlogParserException.Type;

public class CombatLogWatcher {

	private static final Logger LOGGER = LogManager.getLogger(CombatLogWatcher.class);

	private FileAlterationMonitor monitor;

	@Inject
	private CombatlogAlterationListener listener;

	private Consumer<CombatLog> consumer;

	private Path combatLogsPath;

	public void startWatching(Consumer<CombatLog> logConsumer) {
		consumer = logConsumer;
		listener.setParser(this::parse);
		try {
			var path = getCombatlogsPath();
			LOGGER.debug(() -> "Starting combatlog watcher");
			var observer = new FileAlterationObserver(path.toFile());
			observer.addListener(listener);
			monitor = new FileAlterationMonitor(100);
			monitor.addObserver(observer);
			startMonitor();
		} catch (InvalidPathException e) {
			LOGGER.warn(() -> "Unable to start combatlog watcher: Combatlog directory not found");
		}
	}

	public void startWatching(Consumer<CombatLog> logConsumer, Path path) {
		this.combatLogsPath = path;
		startWatching(logConsumer);
	}

	private void startMonitor() {
		try {
			monitor.start();
		} catch (Exception e) {
			LOGGER.error(() -> "Unable to start combatlog monitor");
		}
	}

	public void stopWatching() {
		LOGGER.debug(() -> "Shutting down combatlog watcher");
		try {
			if (monitor != null) {
				monitor.stop();
			}
			listener.stop();
		} catch (Exception e) {
			LOGGER.warn(() -> "Unable to stop combatlog monitor");
		}
	}

	private Path getCombatlogsPath() throws InvalidPathException {
		return combatLogsPath == null
				? Path.of(FileSystemView.getFileSystemView().getDefaultDirectory().toString(),
						"Star Wars - The Old Republic", "CombatLogs")
				: combatLogsPath;
	}

	private void parse(String combatlog) {
		try {
			var log = new CombatLog(combatlog);
			consumer.accept(log);
		} catch (CombatlogParserException e) {
			if (e.getType() == Type.UNPARSABLE_COMBATLOG) {
				listener.onParseFail();
			} else {
				LOGGER.warn(() -> String.format("Exception while parsing combatlog: %n%s", combatlog));
			}
		}
	}

}
