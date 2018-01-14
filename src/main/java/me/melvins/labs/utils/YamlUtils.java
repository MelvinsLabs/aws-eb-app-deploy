/*
 *
 */

package me.melvins.labs.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.MessageFormatMessageFactory;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Melvins
 */
public class YamlUtils {

    private static final Logger LOGGER =
            LogManager.getLogger(YamlUtils.class, new MessageFormatMessageFactory());

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper(new YAMLFactory());

    public static Map<String, Map<String, String>> readYamlFile(String fileName) {

        try {
            BufferedReader br = new BufferedReader(new FileReader(fileName));

            TypeReference<HashMap<String, HashMap<String, String>>> typeRef =
                    new TypeReference<HashMap<String, HashMap<String, String>>>() {
                    };

            return OBJECT_MAPPER.readValue(br, typeRef);

        } catch (FileNotFoundException ex) {
            LOGGER.error(ex);

        } catch (IOException ex) {
            LOGGER.error(ex);
        }

        return null;
    }


}
