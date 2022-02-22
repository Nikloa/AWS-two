package ru.vironit.service;

import org.hibernate.Session;
import ru.vironit.connection.HibernateSessionFactory;
import ru.vironit.model.MetadataEntity;

public class MetadataService {

    public static void save(MetadataEntity metadata) {
        Session session = HibernateSessionFactory.getSessionFactory().openSession();
        session.beginTransaction();
        session.saveOrUpdate(metadata);
        session.getTransaction().commit();
        session.close();
    }
}
