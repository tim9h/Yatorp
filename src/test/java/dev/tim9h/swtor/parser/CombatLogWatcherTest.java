package dev.tim9h.swtor.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.util.Comparator;

import org.apache.commons.rng.simple.RandomSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

import com.google.inject.Guice;
import com.google.inject.Inject;

@TestInstance(Lifecycle.PER_CLASS)
class CombatLogWatcherTest {

	private static final Logger LOGGER = LogManager.getLogger(CombatLogWatcherTest.class);

	@Inject
	private CombatLogWatcher watcher;

	private Path fakeLogDir;

	private int numberOfParsedLogs;

	@BeforeAll
	public void init() throws IOException {
		var injector = Guice.createInjector(new ParserModule());
		injector.injectMembers(this);
		fakeLogDir = Files.createTempDirectory("CombatLogs");
	}

	@AfterAll
	public void cleanUp() throws IOException {
		var logfiles = Files.walk(fakeLogDir).sorted(Comparator.reverseOrder()).toList();
		for (var file : logfiles) {
			Files.delete(file);
		}
		watcher.stopWatching();
	}

	@Test
	@Disabled("Disabled because it's not working in CI/CD pipeline")
	void testDirectoryWatcher() throws InterruptedException, IOException, URISyntaxException {
		var rng = RandomSource.L64_X256_MIX.create();

		numberOfParsedLogs = 0;
		watcher.startWatching(log -> {
			numberOfParsedLogs++;
			LOGGER.debug(() -> "Log " + numberOfParsedLogs + " parsed: " + log);
		}, fakeLogDir);

		var logfile = Files.createTempFile(fakeLogDir, "combat_", "txt");
		var lines = Files.readAllLines(Paths.get(getClass().getClassLoader().getResource("combatlogs.txt").toURI()));
		var maxDelay = 500;
		LOGGER.info(() -> "Attempting to parse " + lines.size() + " lines");
		Thread.sleep(maxDelay);

		for (var line : lines) {
			Files.write(logfile, (line + "\r\n").getBytes(), StandardOpenOption.APPEND);
			LOGGER.debug(() -> "Log written: " + line);
			Thread.sleep(rng.nextLong(1, maxDelay));
		}

		Awaitility.await().timeout(Duration.ofMillis(maxDelay * lines.size()))
				.untilAsserted(() -> assertEquals(lines.size(), numberOfParsedLogs));
		LOGGER.info(() -> "Successfully parsed " + lines.size() + " logs");
	}

}
