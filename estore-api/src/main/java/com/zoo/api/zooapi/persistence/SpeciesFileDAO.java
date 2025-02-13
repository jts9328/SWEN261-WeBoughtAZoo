package com.zoo.api.zooapi.persistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zoo.api.zooapi.model.Species;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;

/**
 * Implements the functionality for JSON file-based peristance for Speciess
 *
 * {@literal @}Component Spring annotation instantiates a single instance of this
 * class and injects the instance into other classes as needed
 *
 * @author Group 6F
 */
@Component
public class SpeciesFileDAO implements SpeciesDAO {
    private static final Logger LOG = Logger.getLogger(SpeciesFileDAO.class.getName());
    Map<String, Species> speciesList;   // Provides a local cache of the species objects
                                   // so that we don't need to read from the file
                                   // each time
    private ObjectMapper objectMapper;  // Provides conversion between Species
                                        // objects and JSON text format written
                                        // to the file
    private String filename;    // Filename to read from and write to

    /**
     * Creates a Species File Data Access Object
     *
     * @param filename Filename to read from and write to
     * @param objectMapper Provides JSON Object to/from Java Object serialization and deserialization
     *
     * @throws IOException when file cannot be accessed or read from
     */
    public SpeciesFileDAO(@Value("data/species.json") String filename, ObjectMapper objectMapper) throws IOException {
        this.filename = filename;
        this.objectMapper = objectMapper;
        try {
            load();  // load the Animals from the file
        } catch (IOException e) {
            LOG.severe("Unable to load species info from file: " + filename + " " + e.getMessage());
            speciesList = new HashMap<>();
        }
    }

    /**
     * Loads the Speciess from the file
     *
     * @return true if the file was loaded successfully, false otherwise
     * @throws IOException when file cannot be accessed or read from
     */
    private boolean load() throws IOException {
        speciesList = new TreeMap<>();

        // Deserializes the JSON objects from the file into an array of Species
        // readValue will throw an IOException if there's an issue with the file
        // or reading from the file
        Species[] SpeciesArray = objectMapper.readValue(new File(filename), Species[].class);

        // Add each Species to the tree map
        for (Species Species : SpeciesArray) {
            speciesList.put(Species.getName(),Species);
        }
        return true;
    }

    /**
     * Generates an array of {@linkplain Species Speciess} from the tree map
     *
     * @return  The array of {@link Species Speciess}, may be empty
     */
    private Species[] getSpeciessArray() {
        return getSpeciessArray(null);
    }

    /**
     * Generates an array of {@linkplain Species Speciess} from the tree map for any
     * {@linkplain Species Speciess} that contains the text specified by containsText
     * <br>
     * If containsText is null, the array contains all of the {@linkplain Species Speciess}
     * in the tree map
     *
     * @return  The array of {@link Species Speciess}, may be empty
     */
    private Species[] getSpeciessArray(String containsText) { // if containsText == null, no filter
        ArrayList<Species> SpeciesArrayList = new ArrayList<>();

        for (Species species : speciesList.values()) {
            if (containsText == null || species.getName().toLowerCase().contains(containsText)) {
                SpeciesArrayList.add(species);
            }
        }

        Species[] SpeciesArray = new Species[SpeciesArrayList.size()];
        SpeciesArrayList.toArray(SpeciesArray);
        return SpeciesArray;
    }

    /**
     * Saves the {@linkplain Species Speciess} from the map into the file as an array of JSON objects
     *
     * @return true if the {@link Species Speciess} were written successfully
     *
     * @throws IOException when file cannot be accessed or written to
     */
    private boolean save() throws IOException {
        Species[] SpeciesArray = getSpeciessArray();

        // Serializes the Java Objects to JSON objects into the file
        // writeValue will thrown an IOException if there is an issue
        // with the file or reading from the file
        objectMapper.writeValue(new File(filename),SpeciesArray);
        return true;
    }

    /**
     * Loads {@linkplain Species Speciess} from the JSON file into the map
     * <br>
     * Also sets next id to one more than the greatest id found in the file
     *
     * @return true if the file was read successfully
     *
     * @throws IOException when file cannot be accessed or read from
     */
   

    /**
    ** {@inheritDoc}
     */
    @Override
    public Species[] getSpeciess() {
        synchronized(speciesList) {
            return getSpeciessArray();
        }
    }

    /**
    ** {@inheritDoc}
     */
    @Override
    public Species[] findSpeciess(String containsText) {
        synchronized(speciesList) {
            return getSpeciessArray(containsText);
        }
    }

    /**
    ** {@inheritDoc}
     */
    @Override
    public Species getSpecies(String name ) {
        synchronized(speciesList) {
            if (speciesList.containsKey(name))
                return speciesList.get(name);
            else
                return null;
        }
    }

    /**
    ** {@inheritDoc}
     */
    @Override
    public Species createSpecies(Species species) throws IOException {
        synchronized(speciesList) {
            // We create a new Species object because the id field is immutable
            // and we need to assign the next unique id
            Species newSpecies = new Species(species.getName(), species.getInfo());
            speciesList.put(newSpecies.getName(),newSpecies);
            save(); // may throw an IOException
            return newSpecies;
        }
    }

    /**
    ** {@inheritDoc}
     */
    @Override
    public Species updateSpecies(Species species) throws IOException {
        synchronized(speciesList) {
            if (speciesList.containsKey(species.getName()) == false)
                return null;  // Species does not exist

            speciesList.put(species.getName(), species);
            save(); // may throw an IOException
            return species;
        }
    }

    /**
    ** {@inheritDoc}
     */
    @Override
    public boolean deleteSpecies(String name) throws IOException {
        synchronized(speciesList) {
            if (speciesList.containsKey(name)) {
                speciesList.remove(name);
                return save();
            }
            else
                return false;
        }
    }
}
