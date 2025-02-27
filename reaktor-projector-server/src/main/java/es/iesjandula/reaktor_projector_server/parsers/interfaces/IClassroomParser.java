package es.iesjandula.reaktor_projector_server.parsers.interfaces;

import java.util.Scanner;

import es.iesjandula.reaktor_projector_server.utils.ProjectorServerException;

/**
 * This interface defines the contract for parsing Classroom data from a given input source.
 * Implementations of this interface should process classroom data from a {@link Scanner} and 
 * handle any necessary validation or transformation.
 * 
 * <p>The parser reads Classroom data, processes it, and may use it for further operations such as 
 * assigning projectors or storing in a database. If an error occurs during processing, a 
 * {@link ProjectorServerException} is thrown.</p>
 * 
 * @author David Jason Gianmoena (<a href="https://github.com/JasonDGian">GitHub</a>)
 * @version 1.0
 */
public interface IClassroomParser
{
    /**
     * Parses Classroom data from the provided {@link Scanner} input.
     * 
     * @param scanner The {@link Scanner} containing the Classroom data to parse.
     * @throws ProjectorServerException If an error occurs during parsing or processing.
     */
    public String parseClassroom(Scanner scanner) throws ProjectorServerException;
}
