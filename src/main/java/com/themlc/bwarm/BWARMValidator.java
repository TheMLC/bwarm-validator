package com.themlc.bwarm;

import java.io.BufferedWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

public class BWARMValidator {

	private String BASE_LOCATION = "";
	private HashMap<String, Integer> recurringMessages = new HashMap<String, Integer>();
	BufferedWriter logger = null;
	BufferedWriter summary = null;

	AVSHelper PartyRoles = null;
	AVSHelper RightShareTypes = null;
	AVSHelper RightTypes = null;
	AVSHelper Territories = null;
	AVSHelper TitleTypes = null;
	AVSHelper UseTypes = null;

	private final Logger LOGGER = LogManager.getLogger();

	private String[][] Works = {
			{ "FeedProvidersWorkId", "true", "string" },
			{ "ISWC", "false", "string" },
			{ "Workitle", "true", "string" },
			{ "OpusNumber", "false", "string" },
			{ "ComposerCatalogNumber", "false", "string" },
			{ "NominalDuration", "false", "duration" },
			{ "HasRightsInDispute", "true", "boolean" },
			{ "TerritoryOfPublicDomain", "false", "avs:Territories", "true" },
			{ "IsArrangementOfTraditionalWork", "true", "boolean" },
			{ "AlternativeWorkForUsStatutoryReversion", "false", "string" },
			{ "UsStatutoryReversionDate", "false", "date" }
	};
	private String[][] AlternativeWorkTitles = {
			{ "FeedProvidersWorkAlternativeTitleId", "true", "string" },
			{ "FeedProvidersWorkId", "true", "string" },
			{ "AlternativeTitle", "true", "string" },
			{ "LanguageAndScriptCode", "false", "string" },
			{ "TitleType", "false", "avs:TitleTypes" }
	};

	private String[][] WorkIdentifiers = {
			{ "FeedProvidersWorkProprietaryIdentifierId", "true", "string" },
			{ "FeedProvidersWorkId", "true", "string" },
			{ "Identifier", "true", "string" },
			{ "FeedProvidersAllocatingPartyId", "true", "string" }
	};

	private String[][] Parties = {
			{ "FeedProvidersPartyId", "true", "string" },
			{ "ISNI", "false", "string" },
			{ "IpiNameNumber", "false", "string" },
			{ "CisacSocietyId", "false", "string" },
			{ "DPID", "false", "string" },
			{ "FullName", "true", "string" },
			{ "NamesBeforeKeyName", "false", "string" },
			{ "KeyName", "false", "string" },
			{ "NamesAfterKeyName", "false", "string" },
			{ "ContactName", "false", "string" },
			{ "ContactEmail", "false", "string" },
			{ "ContactPhone", "false", "string" },
			{ "ContactAddress", "false", "string" },
			{ "NoValidContactInformationAvailable", "false", "boolean" }
	};

	private String[][] WorkRightShares = {
			{ "FeedProvidersWorkRightShareId", "true", "string" },
			{ "FeedProvidersWorkId", "true", "string" },
			{ "FeedProvidersPartyId", "true", "string" },
			{ "PartyRole", "false", "avs:PartyRoles" },
			{ "RightSharePercentage", "false", "number" },
			{ "RightShareType", "false", "avs:RightShareTypes", "true" },
			{ "RightsType", "false", "avs:RightTypes", "true" },
			{ "ValidityStartDate", "false", "date" },
			{ "ValidityEndDate", "false", "date" },
			{ "FeedProvidersParentWorkRightShareId", "false", "string" },
			{ "TerritoryCode", "false", "avs:TerritoryCodes", "true" },
			{ "UseType", "false", "avs:UseTypes", "true" }
	};

	private String[][] Recordings = {
			{ "FeedProvidersRecordingId", "true", "string" },
			{ "ISRC", "false", "string" },
			{ "RecordingTitle", "true", "string" },
			{ "RecordingSubTitle", "false", "string" },
			{ "DisplayArtistName", "true", "string" },
			{ "DisplayArtistISNI", "false", "string" },
			{ "PLine", "false", "string" },
			{ "Duration ", "false", "duration" },
			{ "FeedProvidersReleaseId", "false", "string" },
			{ "StudioProducerName", "false", "string" },
			{ "StudioProducerId", "false", "string" },
			{ "OriginalDataProviderName", "false", "string" },
			{ "OriginalDataProviderDPID", "false", "string" },
			{ "IsDataProvidedAsReceived", "false", "boolean" }
	};

	private String[][] AlternativeRecordingTitles = {
			{ "FeedProvidersRecordingAlternativeTitleId", "true", "string" },
			{ "FeedProvidersRecordingId", "true", "string" },
			{ "AlternativeTitle", "true", "string" },
			{ "LanguageAndScriptCode", "false", "string" },
			{ "TitleType", "false", "avs:TitleTypes" }
	};

	private String[][] RecordingIdentifiers = {
			{ "FeedProvidersRecordingProprietaryIdentifierId", "true", "string" },
			{ "FeedProvidersRecordingId", "true", "string" },
			{ "Identifier", "true", "string" },
			{ "FeedProvidersAllocatingPartyId", "true", "string" }
	};

	private String[][] Releases = {
			{ "FeedProvidersReleaseId", "true", "string" },
			{ "ICPN", "false", "string" },
			{ "ReleaseTitle", "false", "string" },
			{ "ReleaseSubTitle", "false", "string" },
			{ "DisplayArtistName", "false", "string" },
			{ "DisplayArtistISNI", "false", "string" },
			{ "LabelName", "false", "string" },
			{ "ReleaseDate", "false", "date" },
			{ "OriginalDataProviderName", "false", "string" },
			{ "OriginalDataProviderDPID", "false", "string" },
			{ "IsDataProvidedAsReceived", "false", "boolean" }
	};

	private String[][] ReleaseIdentifiers = {
			{ "FeedProvidersReleaseProprietaryIdentifierId", "true", "string" },
			{ "FeedProvidersReleaseId", "true", "string" },
			{ "Identifier", "true", "string" },
			{ "FeedProvidersAllocatingPartyId", "true", "string" }
	};

	private String[][] WorkRecordings = {
			{ "FeedProvidersLinkId", "true", "string" },
			{ "FeedProvidersWorkId", "true", "string" },
			{ "FeedProvidersRecordingId", "true", "string" }
	};

	private String[][] UnclaimedWorks = {
			{ "FeedProvidersRightShareId", "true", "string" },
			{ "FeedProvidersRecordingId", "false", "string" },
			{ "FeedProvidersWorkId", "false", "string" },
			{ "ISRC ", "false", "string" },
			{ "DspRecordingId ", "true", "string" },
			{ "RecordingTitle", "false", "string" },
			{ "RecordingSubTitle", "false", "string" },
			{ "AlternativeRecordingTitle", "false", "string" },
			{ "DisplayArtistName", "false", "string" },
			{ "DisplayArtistISNI", "false", "string" },
			{ "Duration", "false", "duration" },
			{ "UnclaimedPercentage", "true", "number" },
			{ "PercentileForPrioritisation", "false", "number" }
	};

	public BWARMValidator(String base) {
		BASE_LOCATION = base;
		PartyRoles = new AVSHelper("PartyRoles.tsv");
		RightShareTypes = new AVSHelper("RightShareTypes.tsv");
		RightTypes = new AVSHelper("RightTypes.tsv");
		Territories = new AVSHelper("Territories.tsv");
		TitleTypes = new AVSHelper("TitleTypes.tsv");
		UseTypes = new AVSHelper("UseTypes.tsv");
	}

	private void initLogger(String snapshot) throws IOException {
		String loggerLocation = BASE_LOCATION + System.getProperty("file.separator") + snapshot + System.getProperty("file.separator") + "validator.tsv";
		Path p = Paths.get(loggerLocation);
		if (p.toFile().exists()) {
			logger = Files.newBufferedWriter(p, StandardCharsets.UTF_8, StandardOpenOption.TRUNCATE_EXISTING);
		} else {
			logger = Files.newBufferedWriter(p, StandardCharsets.UTF_8, StandardOpenOption.CREATE);
		}
		log("Snapshot", "File", "Record Id", "Line Number", "Error Message");

		String summaryLocation = BASE_LOCATION + System.getProperty("file.separator") + snapshot + System.getProperty("file.separator") + "validator_summary.tsv";
		p = Paths.get(summaryLocation);
		if (p.toFile().exists()) {
			summary = Files.newBufferedWriter(p, StandardCharsets.UTF_8, StandardOpenOption.TRUNCATE_EXISTING);
		} else {
			summary = Files.newBufferedWriter(p, StandardCharsets.UTF_8, StandardOpenOption.CREATE);
		}

		summary.write("Snapshot");
		summary.write("\t");
		summary.write("File");
		summary.write("\t");
		summary.write("Record Id");
		summary.write("\t");
		summary.write("Error Message");
		summary.write("\t");
		summary.write("Count");
		summary.write("\n");

	}

	public void finish() throws IOException {
		logger.flush();
		logger.close();
		summary.flush();
		summary.close();
	}

	private synchronized void log(String snapshot, String logType, String id, String lineNumber, String msg) {
		try {

			logger.write(snapshot);
			logger.write("\t");
			logger.write(logType);
			logger.write("\t");
			logger.write(id);
			logger.write("\t");
			logger.write(lineNumber);
			logger.write("\t");
			logger.write(msg);
			logger.write("\n");
		} catch (IOException e) {
			LOGGER.error(e);
			LOGGER.error("200={} 202={} 204={} 206={}", snapshot, logType, lineNumber, msg);
			throw new RuntimeException(e);
		}

		if (!"Snapshot".equals(snapshot)) {
			String key = snapshot + "\t" + logType + "\t" + msg;
			int count = 0;
			if (recurringMessages.containsKey(key)) {
				count = recurringMessages.get(key);
			}
			count++;
			recurringMessages.put(key, count);
		}

	}

	private boolean checMandatory(String data, boolean isMandatory) {
		if (!isMandatory)
			return true;

		if (data == null)
			return false;
		if (data.equals(""))
			return false;

		return true;
	}

	private boolean checkBoolean(String data) {
		if (!data.toLowerCase().equals("true") && !data.toLowerCase().equals("false"))
			return false;

		return true;
	}

	private boolean checNumber(String data) {
		try {
			new BigDecimal(data);
		} catch (NumberFormatException nfe) {
			LOGGER.debug(nfe);
			return false;
		}
		return true;
	}

	private boolean checkDuration(String data) {
		try {
			Duration.parse(data);
		} catch (DateTimeParseException | IllegalArgumentException e) {
			return false;
		}
		return true;

	}

	private boolean checkDate(String data) {
		try {
			DateTimeFormatter parser2 = ISODateTimeFormat.localDateOptionalTimeParser();
			parser2.parseDateTime(data);
		} catch (UnsupportedOperationException | IllegalArgumentException e) {
			LOGGER.debug(e);
			return false;
		}
		return true;

	}

	private boolean checkAVS(String value, AVSHelper helper) {
		return helper.contains(value);
	}

	private DateTime getDate(String data) {
		try {
			DateTimeFormatter parser2 = ISODateTimeFormat.dateTimeNoMillis();
			return parser2.parseDateTime(data);
		} catch (UnsupportedOperationException | IllegalArgumentException e) {
			return null;
		}

	}

	private boolean validateAll(String snapshot, int lineNumber, String recrd[], String logType, String[][] schema) {
		boolean valid = true;
		if (recrd.length != schema.length) {

			log(snapshot, logType, recrd[0], "" + lineNumber, "Incorrect Number of records: expected " + schema.length + " found " + recrd.length);
			valid = false;
		} else {
			for (int i = 0; i < schema.length; i++) {

				String[] field = schema[i];

				if (!checMandatory(recrd[i], field[1].equals("true"))) {
					log(snapshot, logType, recrd[0], "" + lineNumber, "Missing Mandatory Field " + field[0]);
				} else {
					if (!"".equals(recrd[i])) {
						switch (field[2]) {
						case "boolean":
							if (!checkBoolean(recrd[i])) {
								log(snapshot, logType, recrd[0], "" + lineNumber, "Invalid boolean field " + field[0]);
								LOGGER.debug("{}   {}  i={}  value={}  fields={}", field[2], logType, i, recrd[i], field);
								valid = false;
							}
							break;
						case "number":
							if (!checNumber(recrd[i])) {
								log(snapshot, logType, recrd[0], "" + lineNumber, "Invalid number field " + field[0]);
								LOGGER.debug("{}   {}  i={}  value={}  fields={}", field[2], logType, i, recrd[i], field);
								valid = false;
							}
							break;
						case "duration":
							if (!checkDuration(recrd[i])) {
								log(snapshot, logType, recrd[0], "" + lineNumber, "Invalid duration field " + field[0]);
								LOGGER.debug("{}   {}  i={}  value={}  fields={}", field[2], logType, i, recrd[i], field);
								valid = false;
							}
							break;
						case "date":
							if (!checkDate(recrd[i])) {
								log(snapshot, logType, recrd[0], "" + lineNumber, "Invalid date field " + field[0]);
								LOGGER.debug("{}   {}  i={}  value={}  fields={}", field[2], logType, i, recrd[i], field);
								valid = false;
							}
							break;

						case "avs:PartyRoles":
							if (field.length == 4 && field[3].equals("true")) {
								String[] values = recrd[i].split("\\|");
								boolean invalid = false;
								for (int valIdx = 0; valIdx < values.length && !invalid; valIdx++) {
									if (!checkAVS(values[valIdx], PartyRoles)) {
										invalid = true;
									}
								}
								if (invalid) {
									log(snapshot, logType, recrd[0], "" + lineNumber, "Invalid AVS PartyRole Value field '" + recrd[i] + "'");
									LOGGER.debug("{}   {}  i={}  value={}  fields={}", field[2], logType, i, recrd[i], field);
									valid = false;
								}
							} else {
								if (!checkAVS(recrd[i], PartyRoles)) {
									log(snapshot, logType, recrd[0], "" + lineNumber, "Invalid AVS PartyRole Value field '" + recrd[i] + "'");
									LOGGER.debug("{}   {}  i={}  value={}  fields={}", field[2], logType, i, recrd[i], field);
									valid = false;
								}
							}
							break;

						case "avs:RightShareTypes":
							if (field.length == 4 && field[3].equals("true")) {
								String[] values = recrd[i].split("\\|");
								boolean invalid = false;
								for (int valIdx = 0; valIdx < values.length && !invalid; valIdx++) {
									if (!checkAVS(values[valIdx], RightShareTypes)) {
										invalid = true;
									}
								}
								if (invalid) {
									log(snapshot, logType, recrd[0], "" + lineNumber, "Invalid AVS RightShareType Value field '" + recrd[i] + "'");
									LOGGER.debug("{}   {}  i={}  value=( {} --> {} ) fields={}", field[2], logType, i, recrd[i], values, field);
									valid = false;
								}
							} else {
								if (!checkAVS(recrd[i], RightShareTypes)) {
									log(snapshot, logType, recrd[0], "" + lineNumber, "Invalid AVS RightShareType Value field  '" + recrd[i] + "'");
									LOGGER.debug("{}   {}  i={}  value={}  fields={}", field[2], logType, i, recrd[i], field);
									valid = false;
								}
							}
							break;

						case "avs:RightTypes":
							if (field.length == 4 && field[3].equals("true")) {
								String[] values = recrd[i].split("\\|");
								boolean invalid = false;
								for (int valIdx = 0; valIdx < values.length && !invalid; valIdx++) {
									if (!checkAVS(values[valIdx], RightTypes)) {
										invalid = true;
									}
								}
								if (invalid) {
									log(snapshot, logType, recrd[0], "" + lineNumber, "Invalid AVS RightType Value field '" + recrd[i] + "'");
									LOGGER.debug("{}   {}  i={}  value=( {} --> {} ) fields={}", field[2], logType, i, recrd[i], values, field);
									valid = false;
								}
							} else {
								if (!checkAVS(recrd[i], RightTypes)) {
									log(snapshot, logType, recrd[0], "" + lineNumber, "Invalid AVS RightType Value field '" + recrd[i] + "'");
									LOGGER.debug("{}   {}  i={}  value={}  fields={}", field[2], logType, i, recrd[i], field);
									valid = false;
								}
							}
							break;

						case "avs:Territories":
							if (field.length == 4 && field[3].equals("true")) {
								String[] values = recrd[i].split("\\|");
								boolean invalid = false;
								for (int valIdx = 0; valIdx < values.length && !invalid; valIdx++) {
									if (!checkAVS(values[valIdx], Territories)) {
										invalid = true;
									}
								}
								if (invalid) {
									log(snapshot, logType, recrd[0], "" + lineNumber, "Invalid AVS Territory Value field '" + recrd[i] + "'");
									LOGGER.debug("{}   {}  i={}  value=( {} --> {} ) fields={}", field[2], logType, i, recrd[i], values, field);
									valid = false;
								}
							} else {
								if (!checkAVS(recrd[i], Territories)) {
									log(snapshot, logType, recrd[0], "" + lineNumber, "Invalid AVS Territory Value field '" + recrd[i] + "'");
									LOGGER.debug("{}   {}  i={}  value={}  fields={}", field[2], logType, i, recrd[i], field);
									valid = false;
								}
							}
							break;

						case "avs:TitleTypes":
							if (field.length == 4 && field[3].equals("true")) {
								String[] values = recrd[i].split("\\|");
								boolean invalid = false;
								for (int valIdx = 0; valIdx < values.length && !invalid; valIdx++) {
									if (!checkAVS(values[valIdx], TitleTypes)) {
										invalid = true;
									}
								}
								if (invalid) {
									log(snapshot, logType, recrd[0], "" + lineNumber, "Invalid AVS TitleType Value field '" + recrd[i] + "'");
									LOGGER.debug("{}   {}  i={}  value=( {} --> {} ) fields={}", field[2], logType, i, recrd[i], values, field);
									valid = false;
								}
							} else {
								if (!checkAVS(recrd[i], TitleTypes)) {
									log(snapshot, logType, recrd[0], "" + lineNumber, "Invalid AVS TitleType Value field '" + recrd[i] + "'");
									LOGGER.debug("{}   {}  i={}  value={}  fields={}", field[2], logType, i, recrd[i], field);
									valid = false;
								}
							}
							break;

						case "avs:UseTypes":
							if (field.length == 4 && field[3].equals("true")) {
								String[] values = recrd[i].split("\\|");
								boolean invalid = false;
								for (int valIdx = 0; valIdx < values.length && !invalid; valIdx++) {
									if (!checkAVS(values[valIdx], UseTypes)) {
										invalid = true;
									}
								}
								if (invalid) {
									log(snapshot, logType, recrd[0], "" + lineNumber, "Invalid AVS UseType Value field '" + recrd[i] + "'");
									LOGGER.debug("{}   {}  i={}  value=( {} --> {} ) fields={}", field[2], logType, i, recrd[i], values, field);
									valid = false;
								}
							} else {
								if (!checkAVS(recrd[i], UseTypes)) {
									log(snapshot, logType, recrd[0], "" + lineNumber, "Invalid AVS UseType Value field '" + recrd[i] + "'");
									LOGGER.debug("{}   {}  i={}  value={}  fields={}", field[2], logType, i, recrd[i], field);
									valid = false;
								}
							}
							break;

						}
					}
				}
			}

		}
		return valid;

	}

	int workLineNumber = 0;

	private void validateWorks(String snapshot) {
		LOGGER.debug("Start {} {} ", snapshot, "works");
		try (Stream<String> lines = Files.lines(Paths.get(BASE_LOCATION + snapshot + "/works.tsv"), StandardCharsets.UTF_8)) {

			workLineNumber = 0;

			lines.forEach(line ->
				{
					String rec[] = line.split("\t", -1);
					workLineNumber++;
					boolean valid = validateAll(snapshot, workLineNumber, rec, "works", Works);
					if (valid) {
						// check conditional fields
						if (!rec[9].equals("") && rec[10].equals("")) {
							log(snapshot, "works", rec[0], "" + workLineNumber, "Condition not fulfilled for " + Works[10][0]);
						}
					}
				});
		} catch (IOException e) {
			LOGGER.error("Error {} {} {}", snapshot, "works", e);
		} finally {

			LOGGER.debug("End {} {} processed={}", snapshot, "works", workLineNumber);
			size--;
		}
	}

	int alternativeworktitlesLineNumber = 0;

	private void validateAlternativeWorkTitles(String snapshot) {
		LOGGER.debug("Start {} {} ", snapshot, "alternativeworktitles");
		try (Stream<String> lines = Files.lines(Paths.get(BASE_LOCATION + snapshot + "/workalternativetitles.tsv"), StandardCharsets.UTF_8)) {

			alternativeworktitlesLineNumber = 0;

			lines.forEach(line ->
				{
					String rec[] = line.split("\t", -1);
					alternativeworktitlesLineNumber++;
					validateAll(snapshot, alternativeworktitlesLineNumber, rec, "workalternativetitles", AlternativeWorkTitles);
				});
		} catch (IOException e) {
			LOGGER.error("Error {} {} {}", snapshot, "alternativeworktitles", e);
		} finally {

			LOGGER.debug("End {} {} processed={}", snapshot, "alternativeworktitles", alternativeworktitlesLineNumber);
			size--;
		}
	}

	int workidentifiersLineNumber = 0;

	private void validateWorkIdentifiers(String snapshot) {
		LOGGER.debug("Start {} {} ", snapshot, "workidentifiers");
		try (Stream<String> lines = Files.lines(Paths.get(BASE_LOCATION + snapshot + "/workidentifiers.tsv"), StandardCharsets.UTF_8)) {

			workidentifiersLineNumber = 0;

			lines.forEach(line ->
				{
					String rec[] = line.split("\t", -1);

					workidentifiersLineNumber++;
					validateAll(snapshot, workidentifiersLineNumber, rec, "workidentifiers", WorkIdentifiers);
				});
		} catch (IOException e) {
			LOGGER.error("Error {} {} {}", snapshot, "workidentifiers", e);
		} finally {

			LOGGER.debug("End {} {} processed={}", snapshot, "workidentifiers", workidentifiersLineNumber);
			size--;
		}
	}

	int partiesLineNumber = 0;

	private void validateParties(String snapshot) {
		LOGGER.debug("Start {} {} ", snapshot, "parties");
		try (Stream<String> lines = Files.lines(Paths.get(BASE_LOCATION + snapshot + "/parties.tsv"), StandardCharsets.UTF_8)) {

			partiesLineNumber = 0;

			lines.forEach(line ->
				{
					String rec[] = line.split("\t", -1);

					partiesLineNumber++;
					boolean valid = validateAll(snapshot, partiesLineNumber, rec, "parties", Parties);

					if (valid) {
						// check conditional fields
						if (rec[9].equals("") && rec[10].equals("") && rec[11].equals("") && !rec[13].equals("")) {
							log(snapshot, "parties", rec[0], "" + partiesLineNumber, "Condition not fulfilled for " + Parties[13][0]);
						}
					}
				});
		} catch (IOException e) {
			LOGGER.error("Error {} {} {}", snapshot, "parties", e);
		} finally {

			LOGGER.debug("End {} {} processed={}", snapshot, "parties", partiesLineNumber);
			size--;
		}
	}

	int workrightsharesLineNumber = 0;

	private void validateWorkRightShares(String snapshot) {
		LOGGER.debug("Start {} {} ", snapshot, "workrightshares");
		try (Stream<String> lines = Files.lines(Paths.get(BASE_LOCATION + snapshot + "/workrightshares.tsv"), StandardCharsets.UTF_8)) {

			workrightsharesLineNumber = 0;

			lines.forEach(line ->
				{
					String rec[] = line.split("\t", -1);

					workrightsharesLineNumber++;
					boolean valid = validateAll(snapshot, workrightsharesLineNumber, rec, "workrightshares", WorkRightShares);
					if (valid) {
						// check conditional fields
						if (rec[7].equals("") && rec[8].equals("")) {
							log(snapshot, "workrightshares", rec[0], "" + workrightsharesLineNumber, "Condition not fulfilled for " + WorkRightShares[7][0] + " and " + WorkRightShares[8][0]);
							// String key = snapshot + "\t" + rec[0] + "\t" + "\t" + "Condition not fulfilled for " + WorkRightShares[7][0] + " and " + WorkRightShares[8][0];
							// int count = 0;
							// if (recurringMessages.containsKey(key)) {
							// count = recurringMessages.get(key);
							// } else {
							// LOGGER.debug("LENGTH linenumber:{} {} recrd={}", workrightsharesLineNumber, rec[0], rec);
							// LOGGER.debug("LENGTH linenumber:{} {} schema={}", rec[0], WorkRightShares.length);
							// }
							// recurringMessages.put(key, count);

						}
						if (!rec[7].equals("") && !rec[8].equals("")) {
							DateTime start = getDate(rec[7]);
							DateTime end = getDate(rec[7]);
							if (!end.isAfter(start)) {
								log(snapshot, "workrightshares", rec[0], "" + workrightsharesLineNumber, WorkRightShares[8][0] + " before " + WorkRightShares[7][0]);
							}
						}
					}
				});
		} catch (IOException e) {
			LOGGER.error("Error {} {} {}", snapshot, "workrightshares", e);
		} finally {

			LOGGER.debug("End {} {} processed={}", snapshot, "workrightshares", workrightsharesLineNumber);
			size--;
		}
	}

	int recordingsLineNumber = 0;

	private void validateRecordings(String snapshot) {
		LOGGER.debug("Start {} {} ", snapshot, "recordings");
		try (Stream<String> lines = Files.lines(Paths.get(BASE_LOCATION + snapshot + "/recordings.tsv"), StandardCharsets.UTF_8)) {

			recordingsLineNumber = 0;

			lines.forEach(line ->
				{
					String rec[] = line.split("\t", -1);

					recordingsLineNumber++;
					boolean valid = validateAll(snapshot, recordingsLineNumber, rec, "recordings", Recordings);
					if (valid) {
						// check conditional fields
						if ((!rec[10].equals("") || rec[11].equals("")) && rec[13].equals("")) {
							log(snapshot, "recordings", rec[0], "" + recordingsLineNumber, "Condition not fulfilled for " + Recordings[13][0]);
						}
					}
				});
		} catch (IOException e) {
			LOGGER.error("Error {} {} {}", snapshot, "recordings", e);
		} finally {

			LOGGER.debug("End {} {} processed={}", snapshot, "recordings", recordingsLineNumber);
			size--;
		}
	}

	int alternativerecordingtitlesLineNumber = 0;

	private void validateAlternativeRecordingTitles(String snapshot) {
		LOGGER.debug("Start {} {} ", snapshot, "recordingalternativetitles");
		try (Stream<String> lines = Files.lines(Paths.get(BASE_LOCATION + snapshot + "/recordingalternativetitles.tsv"), StandardCharsets.UTF_8)) {

			alternativerecordingtitlesLineNumber = 0;

			lines.forEach(line ->
				{
					String rec[] = line.split("\t", -1);

					alternativerecordingtitlesLineNumber++;
					validateAll(snapshot, alternativerecordingtitlesLineNumber, rec, "recordingalternativetitles", AlternativeRecordingTitles);
				});
		} catch (IOException e) {
			LOGGER.error("Error {} {} {}", snapshot, "recordingalternativetitles", e);
		} finally {

			LOGGER.debug("End {} {} processed={}", snapshot, "recordingalternativetitles", alternativerecordingtitlesLineNumber);
			size--;
		}
	}

	int recordingidentifiersLineNumber = 0;

	private void validateRecordingIdentifiers(String snapshot) {
		LOGGER.debug("Start {} {} ", snapshot, "recordingidentifiers");
		try (Stream<String> lines = Files.lines(Paths.get(BASE_LOCATION + snapshot + "/recordingidentifiers.tsv"), StandardCharsets.UTF_8)) {

			recordingidentifiersLineNumber = 0;

			lines.forEach(line ->
				{
					String rec[] = line.split("\t", -1);

					recordingidentifiersLineNumber++;
					validateAll(snapshot, recordingidentifiersLineNumber, rec, "recordingidentifiers", RecordingIdentifiers);
				});
		} catch (IOException e) {
			LOGGER.error("Error {} {} {}", snapshot, "recordingidentifiers", e);
		} finally {
			LOGGER.debug("End {} {} processed={}", snapshot, "recordingidentifiers", recordingidentifiersLineNumber);
			size--;
		}
	}

	int releasesLineNumber = 0;

	private void validateReleases(String snapshot) {
		LOGGER.debug("Start {} {} ", snapshot, "releases");
		try (Stream<String> lines = Files.lines(Paths.get(BASE_LOCATION + snapshot + "/releases.tsv"), StandardCharsets.UTF_8)) {

			releasesLineNumber = 0;

			lines.forEach(line ->
				{
					String rec[] = line.split("\t", -1);

					releasesLineNumber++;
					boolean valid = validateAll(snapshot, releasesLineNumber, rec, "releases", Releases);
					if (valid) {
						// check conditional fields
						if ((!rec[8].equals("") || rec[9].equals("")) && rec[10].equals("")) {
							log(snapshot, "releases", rec[0], "" + releasesLineNumber, "Condition not fulfilled for " + Releases[10][0]);
						}
					}
				});
		} catch (IOException e) {
			LOGGER.error("Error {} {} {}", snapshot, "releases", e);
		} finally {

			LOGGER.debug("End {} {} processed={}", snapshot, "releases", releasesLineNumber);
			size--;
		}
	}

	int releaseidentifiersLineNumber = 0;

	private void validateReleaseIdentifiers(String snapshot) {
		LOGGER.debug("Start {} {} ", snapshot, "releaseidentifiers");
		try (Stream<String> lines = Files.lines(Paths.get(BASE_LOCATION + snapshot + "/releaseidentifiers.tsv"), StandardCharsets.UTF_8)) {

			releaseidentifiersLineNumber = 0;

			lines.forEach(line ->
				{
					String rec[] = line.split("\t", -1);

					releaseidentifiersLineNumber++;
					validateAll(snapshot, releaseidentifiersLineNumber, rec, "releaseidentifiers", ReleaseIdentifiers);
				});
		} catch (IOException e) {
			LOGGER.error("Error {} {} {}", snapshot, "releaseidentifiers", e);
		} finally {

			LOGGER.debug("End {} {} processed={}", snapshot, "releaseidentifiers", releaseidentifiersLineNumber);
			size--;
		}
	}

	int worksrecordingsLineNumber = 0;

	private void validateWorkRecordings(String snapshot) {
		LOGGER.debug("Start {} {} ", snapshot, "worksrecordings");
		try (Stream<String> lines = Files.lines(Paths.get(BASE_LOCATION + snapshot + "/worksrecordings.tsv"), StandardCharsets.UTF_8)) {

			worksrecordingsLineNumber = 0;

			lines.forEach(line ->
				{
					String rec[] = line.split("\t", -1);

					worksrecordingsLineNumber++;
					validateAll(snapshot, worksrecordingsLineNumber, rec, "worksrecordings", WorkRecordings);
				});
		} catch (IOException e) {
			LOGGER.error("Error {} {} {}", snapshot, "worksrecordings", e);
		} finally {

			LOGGER.debug("End {} {} processed={}", snapshot, "worksrecordings", worksrecordingsLineNumber);
			size--;
		}
	}

	int unclaimedworksLineNumber = 0;

	private void validateAUnclaimedWorks(String snapshot) {
		LOGGER.debug("Start {} {} ", snapshot, "unclaimedworkrightshares");
		try (Stream<String> lines = Files.lines(Paths.get(BASE_LOCATION + snapshot + "/unclaimedworkrightshares.tsv"), StandardCharsets.UTF_8)) {
			// List<String[]> records = lines.map(line -> pattern.split(line)).collect(Collectors.toList());
			unclaimedworksLineNumber = 0;

			lines.forEach(line ->
				{
					String rec[] = line.split("\t", -1);

					unclaimedworksLineNumber++;
					boolean valid = validateAll(snapshot, unclaimedworksLineNumber, rec, "unclaimedworkrightshares", UnclaimedWorks);
					if (valid) {
						// check conditional fields
						if (rec[1].equals("") && rec[5].equals("") && rec[8].equals("")) {
							log(snapshot, "unclaimedworkrightshares", rec[0], "" + unclaimedworksLineNumber, "Condition not fulfilled for (" + UnclaimedWorks[1][0] + ", " + UnclaimedWorks[5][0] + ", " + UnclaimedWorks[8][0] + ")");
						}
					}
				});
		} catch (IOException e) {
			LOGGER.error("Error {} {} {}", snapshot, "unclaimedworkrightshares", e);
		} finally {
			LOGGER.debug("End {} {} processed={}", snapshot, "unclaimedworkrightshares", unclaimedworksLineNumber);
			size--;
		}
	}

	int size = 0;

	public void validate(String snapshot) throws IOException {
		initLogger(snapshot);
		LOGGER.info("Start Validation of Snapshot {}", snapshot);

		ArrayList<Thread> workers = new ArrayList<Thread>();

		Runnable validateWorks = new Runnable() {
			public void run() {
				validateWorks(snapshot);
			}
		};
		workers.add(new Thread(validateWorks));

		Runnable validateAlternativeWorkTitles = new Runnable() {
			public void run() {
				validateAlternativeWorkTitles(snapshot);
			}
		};
		workers.add(new Thread(validateAlternativeWorkTitles));

		Runnable validateWorkIdentifiers = new Runnable() {
			public void run() {
				validateWorkIdentifiers(snapshot);
			}
		};
		workers.add(new Thread(validateWorkIdentifiers));

		Runnable validateParties = new Runnable() {
			public void run() {
				validateParties(snapshot);
			}
		};
		workers.add(new Thread(validateParties));

		Runnable validateWorkRightShares = new Runnable() {
			public void run() {
				validateWorkRightShares(snapshot);
			}
		};
		workers.add(new Thread(validateWorkRightShares));

		Runnable validateRecordings = new Runnable() {
			public void run() {
				validateRecordings(snapshot);
			}
		};
		workers.add(new Thread(validateRecordings));

		Runnable validateAlternativeRecordingTitles = new Runnable() {
			public void run() {
				validateAlternativeRecordingTitles(snapshot);
			}
		};
		workers.add(new Thread(validateAlternativeRecordingTitles));

		Runnable validateRecordingIdentifiers = new Runnable() {
			public void run() {
				validateRecordingIdentifiers(snapshot);
			}
		};
		workers.add(new Thread(validateRecordingIdentifiers));

		Runnable validateReleases = new Runnable() {
			public void run() {
				validateReleases(snapshot);
			}
		};
		workers.add(new Thread(validateReleases));

		Runnable validateReleaseIdentifiers = new Runnable() {
			public void run() {
				validateReleaseIdentifiers(snapshot);
			}
		};
		workers.add(new Thread(validateReleaseIdentifiers));

		Runnable validateWorkRecordings = new Runnable() {
			public void run() {
				validateWorkRecordings(snapshot);
			}
		};
		workers.add(new Thread(validateWorkRecordings));

		Runnable validateAUnclaimedWorks = new Runnable() {
			public void run() {
				validateAUnclaimedWorks(snapshot);
			}
		};
		workers.add(new Thread(validateAUnclaimedWorks));

		size = workers.size();

		int initSize = size;

		for (Thread worker : workers) {
			worker.start();
		}

		boolean isAlive = false;
		do {
			try {
				Thread.sleep(10 * 1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			int count = 0;
			for (Thread worker : workers) {
				isAlive = isAlive || worker.isAlive();
				if (isAlive) {
					count++;
				}
			}

			LOGGER.debug("Main {}/{} Running - isAlive={} countAlive={}", size, initSize, isAlive, count);

		} while (size > 0);

		Set<String> keys = recurringMessages.keySet();

		for (String key : keys) {
			int count = recurringMessages.get(key);
			// logger.write(key + "(Found " + count + " times)\n");
			summary.write(key + "\t" + count);
		}

		finish();
		LOGGER.info("End Validation of Snapshot  {}", snapshot);

	}

	private static CommandLine parseCommandLine(String[] args) throws ParseException {
		Options options = new Options();
		options.addOption(Option.builder("d").required(true).longOpt("snapshot-directory").desc("BWARM Snapshot Base Folder").hasArg().build());
		options.addOption(Option.builder("s").required(true).longOpt("snapshot").desc("Snapshot Reference").hasArg().build());

		CommandLineParser parser = new DefaultParser();

		CommandLine line;
		try {
			line = parser.parse(options, args);

			return line;
		} catch (ParseException e) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("BWARMValidator", options);
			throw e;
		}

	}

	public static void main(String a[]) {

		try {
			CommandLine cmd = BWARMValidator.parseCommandLine(a);

			BWARMValidator validator = new BWARMValidator(cmd.getOptionValue("snapshot-directory"));
			validator.validate(cmd.getOptionValue("snapshot"));

		} catch (ParseException | IOException e1) {
			e1.printStackTrace();
			System.exit(99);
		}
	}

}
