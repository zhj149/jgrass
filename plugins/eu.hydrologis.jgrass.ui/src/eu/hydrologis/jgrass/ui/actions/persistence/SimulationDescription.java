package eu.hydrologis.jgrass.ui.actions.persistence;

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "SIM_DESCR")
public class SimulationDescription {

    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "model", nullable = false)
    private String model;

    @Column(name = "modeluser", nullable = false)
    private String user;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", nullable = true)
    private String description;

    @Column(name = "insertdate", nullable = false)
    private Timestamp insertDate;

    @Column(name = "startdate", nullable = false)
    private Timestamp startDate;

    @Column(name = "enddate", nullable = false)
    private Timestamp endDate;

    @Column(name = "refmodel", nullable = true)
    private Long referenceModelId;

    public Long getId() {
        return id;
    }

    public void setId( Long id ) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle( String title ) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription( String description ) {
        this.description = description;
    }

    public Timestamp getStartDate() {
        return startDate;
    }

    public void setStartDate( Timestamp startDate ) {
        this.startDate = startDate;
    }

    public Timestamp getEndDate() {
        return endDate;
    }

    public void setEndDate( Timestamp endDate ) {
        this.endDate = endDate;
    }

    public void setUser( String user ) {
        this.user = user;
    }

    public String getUser() {
        return user;
    }

    public void setInsertDate( Timestamp insertDate ) {
        this.insertDate = insertDate;
    }

    public Timestamp getInsertDate() {
        return insertDate;
    }

    public void setModel( String model ) {
        this.model = model;
    }

    public String getModel() {
        return model;
    }

    public void setReferenceModel( Long referenceModelId ) {
        this.referenceModelId = referenceModelId;
    }

    public Long getReferenceModel() {
        return referenceModelId;
    }
}
