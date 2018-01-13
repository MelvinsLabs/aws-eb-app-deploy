/*
 *
 */

package me.melvins.labs.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.MessageFormatMessageFactory;

/**
 * @author Melvins
 */
public class TimeUtils {

    private static final Logger LOGGER =
            LogManager.getLogger(TimeUtils.class, new MessageFormatMessageFactory());

    public static void sleeper(long sleepTime) {

        try {
            Thread.sleep(sleepTime);

        } catch (InterruptedException ex) {
            LOGGER.warn("Exception While Sleeping Thread", ex);
        }
    }

}
