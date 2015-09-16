package com.msagi.flashbus.generator.dao;

import com.msagi.flashbus.generator.Subscriber;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

/**
 * Subscriber data access object implementation.
 *
 * @author msagi (miklos.sagi@gmail.com)
 */
public class SubscriberDaoImpl implements SubscriberDao {

    @Override
    public String getMetaDataFilePath(final String projectId) {
        final String tempDirectory = System.getProperty("java.io.tmpdir");
        final String metaDataFilePath = tempDirectory + File.separator + projectId + ".metadata";
        return metaDataFilePath;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Subscriber> loadSubscribers(final String projectId) throws IOException {
        final FileInputStream fileInputStream = new FileInputStream(getMetaDataFilePath(projectId));
        final ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
        final List<Subscriber> subscribers;
        try {
            subscribers = (List<Subscriber>) objectInputStream.readObject();
        } catch (Exception e) {
            throw new IOException(e);
        }
        fileInputStream.close();

        return subscribers;
    }

    @Override
    public void saveSubscribers(final String projectId, final List<Subscriber> subscribers) throws IOException {
        final FileOutputStream fileOutputStream = new FileOutputStream(getMetaDataFilePath(projectId));
        final ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
        objectOutputStream.writeObject(subscribers);
        objectOutputStream.flush();
        fileOutputStream.close();
    }
}
