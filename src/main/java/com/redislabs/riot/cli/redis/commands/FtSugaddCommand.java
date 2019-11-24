package com.redislabs.riot.cli.redis.commands;

import com.redislabs.riot.batch.redis.writer.AbstractKeyRedisWriter;
import com.redislabs.riot.batch.redisearch.writer.FtSugadd;
import com.redislabs.riot.batch.redisearch.writer.FtSugaddPayload;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "ftsugadd", description = "Add suggestion strings to an auto-complete suggestion dictionary")
public class FtSugaddCommand extends AbstractKeyRedisCommand {

	@Option(names = "--payload", description = "Name of the field containing the payload", paramLabel = "<field>")
	private String payload;
	@Option(names = "--score", description = "Name of the field to use for scores", paramLabel = "<field>")
	private String score;
	@Option(names = "--default-score", description = "Score when field not present (default: ${DEFAULT-VALUE})", paramLabel = "<num>")
	private double defaultScore = 1d;
	@Option(names = "--string", description = "Field containing the suggestion", paramLabel = "<field>")
	private String suggest;
	@Option(names = "--increment", description = "Use increment to set value")
	private boolean increment;

	@Override
	protected boolean isRediSearch() {
		return true;
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected AbstractKeyRedisWriter keyWriter() {
		FtSugadd writer = payload == null ? new FtSugadd() : new FtSugaddPayload().payload(payload);
		writer.field(suggest);
		writer.increment(increment);
		writer.defaultScore(defaultScore);
		writer.scoreField(score);
		return writer;
	}

}
