package com.reservation.batch.job.roomabailabilitysummary.writer;

import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

import org.springframework.stereotype.Component;

import com.univocity.parsers.csv.CsvWriter;
import com.univocity.parsers.csv.CsvWriterSettings;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class GenerateCsvRoomAvailabilitySummaryWriter {
	public void write(List<String[]> outputAvailabilitySummaries, Path csvPath) {
		if (outputAvailabilitySummaries.isEmpty()) {
			return;
		}

		CsvWriterSettings settings = new CsvWriterSettings();
		settings.getFormat().setDelimiter(',');

		try (Writer writer = Files.newBufferedWriter(csvPath, StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
			CsvWriter csvWriter = new CsvWriter(writer, settings);
			for (String[] row : outputAvailabilitySummaries) {
				csvWriter.writeRow(row);
			}
		} catch (Exception e) {
			log.info("Failed to generate CSV availabilities", e);
		}
	}
}
