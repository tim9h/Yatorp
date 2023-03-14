package dev.tim9h.swtor.parser;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import javax.swing.filechooser.FileSystemView;

import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.inject.Inject;

import dev.tim9h.swtor.parser.bean.CombatLog;

public class CombatLogWatcher {

	private static final Pattern PATTERN = Pattern
			.compile("^\\[(.+)\\] \\[(.*)\\] \\[(.*)\\] \\[(.*)\\] \\[(.*)\\](?: \\((.*)\\))?(?: <(.*)>)?$");

	private static final Logger logger = LogManager.getLogger(CombatLogWatcher.class);

	private FileAlterationMonitor monitor;

	@Inject
	private CombatlogAlterationListener listener;

	private Consumer<CombatLog> consumer;

	public void startWatching(Consumer<CombatLog> consumer) {
		this.consumer = consumer;
		logger.debug(() -> "Start combatlog watcher");
		listener.setParser(this::parse);
		try {
			var path = getCombatlogsPath();
			logger.debug(() -> "Starting combatlog watcher");
			FileAlterationObserver observer = new FileAlterationObserver(path.toFile());
			observer.addListener(listener);
			monitor = new FileAlterationMonitor(100);
			monitor.addObserver(observer);
			startMonitor();
		} catch (InvalidPathException e) {
			logger.warn(() -> "Unable to start combatlog watcher: Combatlog directory not found");
		}
	}

	private void startMonitor() {
		try {
			monitor.start();
		} catch (Exception e) {
			logger.error(() -> "Unable to start combatlog monitor");
		}
	}

	public void stopWatching() {
		logger.debug(() -> "Shutting down combatlog watcher");
		try {
			monitor.stop();
			listener.stop();
		} catch (Exception e) {
			logger.warn(() -> "Unable to stop combatlog monitor");
		}
	}

	private static Path getCombatlogsPath() throws InvalidPathException {
		return Path.of(FileSystemView.getFileSystemView().getDefaultDirectory().toString(),
				"Star Wars - The Old Republic", "CombatLogs"); // CombatLogs
	}

	private void parse(String combatlog) {
		var matcher = PATTERN.matcher(combatlog);
		if (matcher.find() && matcher.groupCount() >= 7) {
			var log = new CombatLog(matcher.group(1), matcher.group(2), matcher.group(3), matcher.group(4),
					matcher.group(5), matcher.group(6), matcher.group(7));
			consumer.accept(log);
		} else {
			listener.onParseFail();
		}
	}

}
