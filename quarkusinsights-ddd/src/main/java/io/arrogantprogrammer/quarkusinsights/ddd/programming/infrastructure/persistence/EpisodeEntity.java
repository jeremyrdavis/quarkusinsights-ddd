package io.arrogantprogrammer.quarkusinsights.ddd.programming.infrastructure.persistence;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;

import java.sql.Date;

@Entity
public class EpisodeEntity extends PanacheEntity {

    String title;

    String description;

    Date scheduledDate;

    protected EpisodeEntity() {
    }

    protected EpisodeEntity(String title, String description, Date scheduledDate) {
        this.title = title;
        this.description = description;
        this.scheduledDate = scheduledDate;
    }

    public String getTitle() {
        return title;
    }

    void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    void setDescription(String description) {
        this.description = description;
    }

    public Date getScheduledDate() {
        return scheduledDate;
    }

    void setScheduledDate(Date scheduledDate) {
        this.scheduledDate = scheduledDate;
    }
}
