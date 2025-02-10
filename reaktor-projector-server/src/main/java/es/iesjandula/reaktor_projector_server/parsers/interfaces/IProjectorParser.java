package es.iesjandula.reaktor_projector_server.parsers.interfaces;

import java.util.Scanner;

import es.iesjandula.reaktor_projector_server.utils.ProjectorServerException;

/**
 * This interface defines the contract for parsing Projector PHYSICAL UNITS 
 * data from a given input source.
 * 
 * <p>Implementations of this interface should process Projectors data 
 * from a {@link Scanner} and handle validation, persistence, or any 
 * necessary operations.</p>
 * 
 * <p>If an error occurs during processing, a {@link ProjectorServerException} is thrown.</p>
 * 
 * @author David Jason Gianmoena (<a href="https://github.com/JasonDGian">GitHub</a>)
 * @version 1.0
 */
public interface IProjectorParser
{
    /**
     * Parses Projector data from the provided {@link Scanner} input.
     * 
     * @param scanner The {@link Scanner} containing the projector data to parse.
     * @throws ProjectorServerException If an error occurs during parsing or processing.
     */
    public void parseProjectors(Scanner scanner) throws ProjectorServerException;
}
