package com.msagi.flashbus.generator.dao;

import com.msagi.flashbus.generator.Subscriber;

import java.io.IOException;
import java.util.List;

/**
 * Data access object for subscriber value objects.
 *
 * @author msagi (miklos.sagi@gmail.com)
 */
public interface SubscriberDao {

    /**
     * Get the meta data file path for given unique project identifier.
     * @param projectId The unique identifier of the project the subscriber list belongs to.
     * @return The meta data file path for the given project identifier.
     */
    String getMetaDataFilePath(String projectId);

    /**
     * Load the list of subscribers.
     * @param projectId The unique identifier of the project the subscriber list belongs to.
     * @return The unmodifiable list of subscribers or an empty list if no subscribers available.
     */
    List<Subscriber> loadSubscribers(String projectId) throws IOException;

    /**
     * Save the list of subscribers.
     * @param projectId The unique identifier of the project the subscriber list belongs to.
     * @param subscribers The list of subscribers to save.
     */
    void saveSubscribers(String projectId, List<Subscriber> subscribers) throws IOException;
}
