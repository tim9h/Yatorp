package dev.tim9h.swtor.parser;

import com.google.inject.AbstractModule;

import dev.tim9h.swtor.parser.utils.TextFileUtils;
import dev.tim9h.swtor.parser.utils.TextFileUtilsCommons;

public class ParserModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(TextFileUtils.class).to(TextFileUtilsCommons.class);
	}

}
