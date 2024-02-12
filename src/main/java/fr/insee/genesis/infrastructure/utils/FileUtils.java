package fr.insee.genesis.infrastructure.utils;

import fr.insee.genesis.configuration.Config;
import fr.insee.genesis.domain.dtos.SurveyUnitUpdateDto;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONArray;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@Slf4j
@Component
public class FileUtils {

	@Getter
	private final String dataFolderSource;

	private final String specFolderSource;

	public FileUtils(Config config) {
		this.dataFolderSource = config.getDataFolderSource();
		this.specFolderSource = config.getSpecFolderSource();
	}

	/**
	 * Move a file from a directory to another. It creates the destination directory if it does not exist.
	 * @param from Path of the source directory.
	 *                Example: /home/genesis/data/in/2021/2021-01-01
	 * @param destination Path of the destination directory.
	 *                Example: /home/genesis/data/done/2021/2021-01-01
	 * @param filename Name of the file to move.
	 */
	public void moveFiles(String from, String destination ,String filename) throws IOException {
		if (!isFolderPresent(destination)) {
			Files.createDirectories(Path.of(destination));
		}
		Files.move(Path.of(from+"/"+filename),Path.of(destination+"/"+filename));
		log.info("File {} moved from {} to {}", filename, from, destination);
	}

	/**
	 * Move a data file to the folder done
	 * @param campaign Name of the campaign (also folder name)
	 * @param dataSource Application the data came from
	 * @param filename Data file to move
	 * @throws IOException
	 */
	public void moveDataFile(String campaign, String dataSource, String filename) throws IOException {
		String from = getDataFolder(campaign, dataSource);
		String destination = getDoneFolder(campaign, dataSource);
		moveFiles(from, destination, filename);
	}

	/**
	 * Check if a file exists
	 * @param path
	 * @return true if the file exists, false otherwise
	 */
	public boolean isFilePresent(String path) {
		return Files.exists(Path.of(path));
	}

	/**
	 * Checks if a folder exists
	 * @param path
	 * @return true if the folder exists, false otherwise
	 */
	public boolean isFolderPresent(String path) {
		return Files.exists(Path.of(path));
	}

	/**
	 * List all files in a folder
	 * @param dir
	 * @return List of files, empty list if the folder does not exist
	 */
	public List<String> listFiles(String dir) {
		if (isFolderPresent(dir)) {
			return Stream.of(Objects.requireNonNull(new File(dir).listFiles()))
					.filter(file -> !file.isDirectory())
					.map(File::getName)
					.toList();
		}
		return List.of();
	}

	/**
	 * List all folders in a folder
	 * @param dir
	 * @return List of folders, empty list if the folder does not exist
	 */
	public List<String> listFolders(String dir) {
		if (isFolderPresent(dir)) {
			List<String> folders =new ArrayList<>();
			File[] objs = new File(dir).listFiles();
			if (objs == null) {
				return List.of();
			}
			for (File file : objs) {
				if (file.isDirectory()) {
					folders.add(file.getName());
				}
			}
			return folders;
		}
		return List.of();
	}

	/**
	 * Find the DDI file in the folder of a campaign
	 * @param campaign
	 * @param mode
	 * @return Path of the DDI file
	 * @throws IOException
	 */
	public Path findDDIFile(String campaign, String mode) throws IOException {
		try (Stream<Path> files = Files.find(Path.of(String.format("%s/%s",getSpecFolder(campaign),mode)), 1, (path, basicFileAttributes) -> path.toFile().getName().matches("ddi[\\w,\\s-]+\\.xml"))) {
			return files.findFirst()
					.orElseThrow(() -> new RuntimeException("No DDI file found in " + String.format("%s/%s",getSpecFolder(campaign),mode)));
		}
	}

	/**
	 * Get the path of the folder where the data files are stored
	 * @param campaign
	 * @param dataSource
	 * @return Path of the data folder
	 */
	public String getDataFolder(String campaign, String dataSource) {
		return  String.format("%s/%s/%s/%s", dataFolderSource, "IN", dataSource, campaign);
	}

	/**
	 * Get the path of the folder where the specifications files are stored
	 * @param campaign
	 * @return Path of the specifications folder
	 */
	public String getSpecFolder(String campaign) {
		return  String.format("%s/%s/%s", specFolderSource, "specs", campaign);
	}

	/**
	 * Get the path of the folder where the files are stored after processing
	 * @param campaign
	 * @param dataSource
	 * @return Path of the done folder
	 */
	public String getDoneFolder(String campaign, String dataSource) {
		return  String.format("%s/%s/%s/%s", dataFolderSource, "DONE", dataSource, campaign);
	}

	/**
	 * Write a text file.
	 * @param filePath Path to the file.
	 * @param fileContent Content of the text file.
	 */
	public void writeFile(Path filePath, String fileContent) {
		Path path = Path.of(dataFolderSource, filePath.toString());
		boolean fileCreated = false;
		File myFile = null;
		try {
			Files.createDirectories(path.getParent());
			myFile = path.toFile();
			fileCreated = myFile.createNewFile();
		} catch (IOException e) {
			log.error("Permission refused to create folder: " + path.getParent(), e);
		}
		if (!fileCreated){return ;}
		try (FileWriter myWriter = new FileWriter(myFile)) {
			myWriter.write(fileContent);
			log.info(String.format("Text file: %s successfully written", filePath));
		} catch (IOException e) {
			log.warn(String.format("Error occurred when trying to write file: %s", filePath), e);
		}
	}

	/**
	 * Appends a JSON object array into file.
	 * Creates the files if it doesn't exist
	 * @param filePath Path to the file.
	 * @param jsonArray JSON objects to write
	 */
	public void appendJSONFile(Path filePath, JSONArray jsonArray) {
		String content = jsonArray.toJSONString();
        File myFile;
		try {
			Files.createDirectories(filePath.getParent());
			myFile = filePath.toFile();

			try (RandomAccessFile raf = new RandomAccessFile(myFile, "rw")) {
				if(myFile.length() == 0) {
					raf.write("[]".getBytes(StandardCharsets.UTF_8));
				}
				raf.seek(myFile.length()-1);

				if(myFile.length() != 2) {
					raf.write(",".getBytes(StandardCharsets.UTF_8));
				}
				raf.write(content.substring(1).getBytes(StandardCharsets.UTF_8));
			}
		}catch (IOException e) {
			log.warn(String.format("Error occurred when trying to append into file: %s", filePath), e);
		}
	}

	/**
	 * Appends a JSON object array into file.
	 * Creates the files if it doesn't exist
	 * @param filePath Path to the file.
	 * @param responsesStream Stream of SurveyUnitUpdateDto to write
	 */
	public void writeSuUpdatesInFile(Path filePath, Stream<SurveyUnitUpdateDto> responsesStream) throws IOException {
		Files.createDirectories(filePath.getParent());
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath.toFile(), true))) {
			writer.write("[");
			responsesStream.forEach(response -> {
				try {
					writer.write(response.toJSONObject().toJSONString());
					writer.write(",");
				} catch (IOException e) {
					throw new UncheckedIOException(e);
				}
			});
			writer.write("{}]");
		} catch (UncheckedIOException e) {
			throw e.getCause();
		}
	}
}
