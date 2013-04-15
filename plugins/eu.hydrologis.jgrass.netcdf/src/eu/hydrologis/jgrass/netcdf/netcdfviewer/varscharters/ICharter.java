package eu.hydrologis.jgrass.netcdf.netcdfviewer.varscharters;

import org.eclipse.swt.widgets.Composite;

public interface ICharter {

    /**
     * Puts the chart on a composite and retruns it to be used in a stacklayout.
     * 
     * @param indexes the indexes to order axes.
     * @return the chart composite.
     * @throws Exception
     */
    public Composite dochart(Integer... indexes) throws Exception;

}