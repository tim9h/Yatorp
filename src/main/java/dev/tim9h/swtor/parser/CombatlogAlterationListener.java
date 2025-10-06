package dev.tim9h.swtor.parser;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.util.function.Consumer;

import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.lang3.Strings;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.inject.Inject;

import dev.tim9h.swtor.parser.utils.TextFileUtils;

class CombatlogAlterationListener extends FileAlterationListenerAdaptor {

	private static final Logger LOGGER = LogManager.getLogger(CombatlogAlterationListener.class);

	private static final Charset CHARSET = Charset.forName("WINDOWS-1252");

	private File currentFile;

	private Consumer<String> parser;

	private long linecount;

	@Inject
	private TextFileUtils textUtils;

	void setParser(Consumer<String> parser) {
		this.parser = parser;
	}

	@Override
	public void onFileCreate(File file) {
		this.currentFile = file;
	}

	@Override
	public void onFileChange(File file) {
		try {
			handleLogChanged(file);
		} catch (IOException e) {
			LOGGER.error(() -> "Unable to read combat log " + file, e);
		}
	}

	private void handleLogChanged(File file) throws IOException, IllegalArgumentException {
		if (currentFile == null) {
			currentFile = file;
			LOGGER.debug(() -> "Parsing started");
		} else if (!Strings.CS.equals(currentFile.toString(), file.toString())) {
			currentFile = file;
			linecount = 0;
			LOGGER.debug(() -> "New combatlog started: " + file.toString());
		}

		long newlines = 0;
		try (var totallines = Files.lines(file.toPath(), CHARSET)) {
			if (linecount == 0) {
				linecount = totallines.count();
				newlines = 1;
			} else {
				newlines = totallines.count() - linecount;
				linecount += newlines;
			}
		} catch (NoSuchFileException e) {
			// file was deleted (e.g. by CombatLogPurger)
			LOGGER.debug(() -> "File " + file + " was deleted");
			return;
		}

		var tail = textUtils.tail(currentFile, newlines, CHARSET);
		tail.forEach(parser::accept);
	}

	public void stop() {
		currentFile = null;
		linecount = 0;
		textUtils.close();
	}

	public void onParseFail() {
		linecount--;
	}

}