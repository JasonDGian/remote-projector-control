package es.iesjandula.reaktor_projector_server.parsers;

import java.util.Optional;
import java.util.Scanner;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.iesjandula.reaktor_projector_server.entities.Classroom;
import es.iesjandula.reaktor_projector_server.entities.Floor;
import es.iesjandula.reaktor_projector_server.parsers.interfaces.IClassroomParser;
import es.iesjandula.reaktor_projector_server.repositories.IClassroomRepository;
import es.iesjandula.reaktor_projector_server.repositories.IFloorRepository;
import es.iesjandula.reaktor_projector_server.utils.Constants;
import es.iesjandula.reaktor_projector_server.utils.ProjectorServerException;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of the {@link IClassroomParser} interface for parsing classroom data
 * from CSV files. This service is responsible for reading a CSV file containing
 * classroom and floor information, checking for existing records in the
 * database, and saving new records.
 * 
 * @see IClassroomParser
 * 
 * <p>
 * Author: David Jason Gianmoena
 * (<a href="https://github.com/JasonDGian">GitHub</a>) 
 * Version: 1.0
 * </p>
 */
@Slf4j
@Service
public class IClassroomParserImpl implements IClassroomParser
{
	/**
	 * Autowired repository for classroom data access.
	 */
	@Autowired
	private IClassroomRepository classroomRepository;

	/**
	 * Autowired repository for floor data access.
	 */
	@Autowired
	private IFloorRepository floorRepository;

	/**
	 * Parses the classroom and floor information from the provided CSV file (using
	 * a scanner). It reads the data, validates the records, checks the database for
	 * existing entries, and saves new ones.
	 * 
	 * @param scanner Scanner object containing the CSV file data.
	 * @return A string summarizing the number of records saved and skipped.
	 * @throws ProjectorServerException If there is an error during the parsing
	 *                                  process.
	 */
	@Override
	public String parseClassroom(Scanner scanner) throws ProjectorServerException
	{
		// Logging the start of the parsing process
		log.debug("Classroom & Floors parsing process initiated.");

		// Check if the CSV file is empty and throw an exception if it is.
		if (!scanner.hasNextLine())
		{
			log.error("The received file is empty. No classrooms or floors to parse.");
			throw new ProjectorServerException(493, "Empty CSV file received in classroom & floors parse method.");
		}

		String message;
		// Counters for tracking the number of records saved and skipped
		int recordsSkipped = 0;
		int recordsSaved = 0;

		// Ignore the first line of the CSV file (column headers)
		scanner.nextLine();

		// Iterate through each line in the CSV file
		while (scanner.hasNextLine())
		{
			// File fields should always be -> floor, classroom
			int expectedNumberOfFields = 2;

			// Split the current line by the CSV delimiter to get individual fields
			String[] csvFields = scanner.nextLine().split(Constants.CSV_DELIMITER);

			// If there are not enough fields in the CSV line, skip it
			if (csvFields.length < expectedNumberOfFields)
			{
				log.error("Skipping malformed or empty CSV line.");
				recordsSkipped++;
				continue;
			}

			// Extract floor name and classroom name from the CSV fields
			String floorName = csvFields[0];
			String classroomName = csvFields[1];

			// Try to find the floor in the database using the floor name
			Optional<Floor> floorOptional = this.floorRepository.findById(floorName);

			// If the floor does not exist, create a new floor entity and save it
			Floor floorEntity = floorOptional.orElseGet(() ->
			{
				log.debug("Floor " + floorName + " does not exist in DB, saving it now.");
				Floor newFloorEntity = new Floor();
				newFloorEntity.setFloorName(floorName);
				return this.floorRepository.save(newFloorEntity);
			});

			// Try to find the classroom in the database using the classroom name
			Optional<Classroom> classroomOptional = this.classroomRepository.findById(classroomName);

			// If the classroom already exists in the database, skip the current record
			if (classroomOptional.isPresent())
			{
				log.debug("Classroom '{}' already exists in the database. Skipping record.", classroomName);
				recordsSkipped++;
				continue;
			}

			// If the classroom does not exist, create a new classroom entity and save it
			log.debug("Classroom '{}' not present in DB, saving now.", classroomName);
			Classroom currentClassroom = new Classroom();
			currentClassroom.setClassroomName(classroomName);
			currentClassroom.setFloor(floorEntity);

			this.classroomRepository.save(currentClassroom);

			recordsSaved++;
		}

		// Return a summary of the number of records saved and skipped
		message = "CLASSROOMS: Records saved: " + recordsSaved + " - Records skipped: " + recordsSkipped;
		log.info(message);
		return message;
	}
}
