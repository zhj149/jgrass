package eu.hydrologis.jgrass.ui.actions.persistence;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "SIM_DATA")
public class SimulationData {

    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false)
    private Long id;

    @Lob
    @Column(name = "parameters", nullable = false)
    private String modelParameters;

    @Lob
    @Column(name = "simresult", nullable = false)
    private String simulationResult;

    @Lob
    @Column(name = "simdata", nullable = true)
    private byte[] simulationData;

    @ManyToOne
    @JoinColumn(name = "simulationid", referencedColumnName = "id", nullable = false)
    private SimulationDescription simulationDescription;

    public Long getId() {
        return id;
    }

    public void setId( Long id ) {
        this.id = id;
    }

    public String getModelParameters() {
        return modelParameters;
    }

    public void setModelParameters( String modelParameters ) {
        this.modelParameters = modelParameters;
    }

    public String getSimulationResult() {
        return simulationResult;
    }

    public void setSimulationResult( String simulationResult ) {
        this.simulationResult = simulationResult;
    }

    public byte[] getSimulationData() {
        return simulationData;
    }

    public void setSimulationData( byte[] simulationData ) {
        this.simulationData = simulationData;
    }

    public void dumpSimulationDataToFile( File file ) {
        FileOutputStream fos = null;
        byte[] simulationData2 = getSimulationData();
        try {
            fos = new FileOutputStream(file);
            fos.write(simulationData2);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (fos != null)
                    fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public void setSimulationDataFromFile( File file ) {
        FileInputStream fis = null;
        try {

            fis = new FileInputStream(file);
            int byteNum = fis.available();

            byte[] bytes = new byte[byteNum];

            int read = fis.read(bytes);

            if (read == 1) {
                return;
            }

            setSimulationData(bytes);
        } catch (Exception ex) {
            System.out.println(ex);
        } finally {
            try {
                if (null != fis)
                    fis.close();
            } catch (Exception ex) {
                //
            }
        }
    }

    public void setSimulationDescription( SimulationDescription simulationDescription ) {
        this.simulationDescription = simulationDescription;
    }

    public SimulationDescription getSimulationDescription() {
        return simulationDescription;
    }

}
