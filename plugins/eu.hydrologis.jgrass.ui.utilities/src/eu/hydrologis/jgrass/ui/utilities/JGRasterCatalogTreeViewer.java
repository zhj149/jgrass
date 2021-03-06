/*
 * JGrass - Free Open Source Java GIS http://www.jgrass.org 
 * (C) HydroloGIS - www.hydrologis.com 
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package eu.hydrologis.jgrass.ui.utilities;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

import net.refractions.udig.catalog.CatalogPlugin;
import net.refractions.udig.catalog.ICatalog;
import net.refractions.udig.catalog.IGeoResource;
import net.refractions.udig.catalog.IResolve;
import net.refractions.udig.catalog.ui.CatalogUIPlugin;
import net.refractions.udig.catalog.ui.ISharedImages;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.dialogs.PatternFilter;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import eu.hydrologis.jgrass.libs.utils.JGrassConstants;
import eu.hydrologis.jgrass.ui.utilities.messages.Messages;
import eu.udig.catalog.jgrass.JGrassPlugin;
import eu.udig.catalog.jgrass.core.JGrassMapGeoResource;
import eu.udig.catalog.jgrass.core.JGrassMapsetGeoResource;
import eu.udig.catalog.jgrass.core.JGrassService;

/**
 * <p>
 * This class supplies a tree viewer containing the JGrass raster maps that are in the catalog.
 * </p>
 * 
 * @author Andrea Antonello - www.hydrologis.com
 */
public class JGRasterCatalogTreeViewer extends Composite
        implements
            ISelectionChangedListener,
            IResourcesSelector {

    private final HashMap<String, JGrassMapGeoResource> itemsMap = new HashMap<String, JGrassMapGeoResource>();
    private LabelProvider labelProvider = null;

    private List<IGeoResource> itemLayers;
    private String mapset;

    /**
     * @param parent
     * @param style
     * @param selectionStyle the tree selection style (single or multiple)
     * @param mapset mapset path on which to limit the view
     */
    public JGRasterCatalogTreeViewer( Composite parent, int style, int selectionStyle, String mapset ) {
        super(parent, style);
        if (mapset != null)
            this.mapset = mapset;
        setLayout(new GridLayout(1, false));
        GridData gridData = new GridData();
        gridData.grabExcessHorizontalSpace = true;
        gridData.horizontalAlignment = org.eclipse.swt.layout.GridData.FILL;
        gridData.verticalAlignment = org.eclipse.swt.layout.GridData.FILL;
        gridData.grabExcessVerticalSpace = true;
        setLayoutData(gridData);

        // Create the tree viewer to display the file tree
        PatternFilter patternFilter = new PatternFilter();
        final FilteredTree filter = new FilteredTree(this, selectionStyle, patternFilter);
        final TreeViewer tv = filter.getViewer();
        tv.getTree().setLayoutData(new GridData(GridData.FILL_BOTH));
        tv.setContentProvider(new ContentProvider());
        labelProvider = new LabelProvider();
        tv.setLabelProvider(labelProvider);
        tv.setInput("dummy2"); // pass a non-null that will be ignored //$NON-NLS-1$
        tv.addSelectionChangedListener(this);
    }

    public void selectionChanged( SelectionChangedEvent event ) {
        // if the selection is empty clear the label
        if (event.getSelection().isEmpty()) {
            return;
        }
        if (event.getSelection() instanceof IStructuredSelection) {
            IStructuredSelection selection = (IStructuredSelection) event.getSelection();
            Vector<String> itemNames = new Vector<String>();
            for( Iterator< ? > iterator = selection.iterator(); iterator.hasNext(); ) {
                Object domain = iterator.next();
                String value = labelProvider.getText(domain);
                itemNames.add(value);
            }
            itemLayers = new ArrayList<IGeoResource>();
            for( String name : itemNames ) {
                JGrassMapGeoResource tmpLayer = itemsMap.get(name);
                if (tmpLayer != null) {
                    itemLayers.add(tmpLayer);
                }
            }
        }
    }

    /**
     * This class provides the content for the tree in FileTree
     */

    private class ContentProvider implements ITreeContentProvider {
        /**
         * Gets the children of the specified object
         * 
         * @param arg0 the parent object
         * @return Object[]
         */
        public Object[] getChildren( Object arg0 ) {

            if (arg0 instanceof JGrassMapsetGeoResource) {
                JGrassMapsetGeoResource map = (JGrassMapsetGeoResource) arg0;
                List<IResolve> layers = map.members(null);
                if (layers == null)
                    return null;
                return filteredLayers(layers);
            } else if (arg0 instanceof JGrassMapGeoResource) {
                return null;
            }

            return null;
        }

        private Object[] filteredLayers( List<IResolve> layers ) {
            try {
                Vector<JGrassMapGeoResource> filteredLayers = new Vector<JGrassMapGeoResource>();
                for( IResolve layer : layers ) {
                    if (layer instanceof JGrassMapGeoResource
                            &&

                            (((JGrassMapGeoResource) layer).getType().equals(
                                    JGrassConstants.GRASSBINARYRASTERMAP)
                                    || ((JGrassMapGeoResource) layer).getType().equals(
                                            JGrassConstants.GRASSASCIIRASTERMAP)
                                    || ((JGrassMapGeoResource) layer).getType().equals(
                                            JGrassConstants.ESRIRASTERMAP) || ((JGrassMapGeoResource) layer)
                                    .getType().equals(JGrassConstants.FTRASTERMAP))

                    ) {

                        filteredLayers.add((JGrassMapGeoResource) layer);
                        itemsMap.put(((JGrassMapGeoResource) layer).getInfo(null).getTitle(),
                                (JGrassMapGeoResource) layer);
                    }

                }

                /*
                 * now let's sort them for nice visualization
                 */
                HashMap<String, JGrassMapGeoResource> tmp = new HashMap<String, JGrassMapGeoResource>();
                for( JGrassMapGeoResource resource : filteredLayers ) {
                    tmp.put(resource.getInfo(null).getTitle(), resource);
                }
                Map<String, JGrassMapGeoResource> sortedMap = new TreeMap<String, JGrassMapGeoResource>(
                        tmp);
                filteredLayers.removeAllElements();
                for( String key : sortedMap.keySet() ) {
                    filteredLayers.add(sortedMap.get(key));
                }

                return filteredLayers.toArray();
            } catch (IOException e) {
                UiUtilitiesPlugin.log("UiUtilitiesPlugin problem", e); //$NON-NLS-1$
                e.printStackTrace();
                return null;
            }
        }

        /**
         * Gets the parent of the specified object
         * 
         * @param arg0 the object
         * @return Object
         */
        public Object getParent( Object arg0 ) {
            if (arg0 instanceof JGrassMapsetGeoResource) {
                return null;
            } else if (arg0 instanceof JGrassMapGeoResource) {
                try {
                    return ((JGrassMapGeoResource) arg0).parent(null);
                } catch (IOException e) {
                    UiUtilitiesPlugin.log("UiUtilitiesPlugin problem", e); //$NON-NLS-1$
                    e.printStackTrace();
                }
            }
            return null;
        }

        /**
         * Returns whether the passed object has children
         * 
         * @param arg0 the parent object
         * @return boolean
         */
        public boolean hasChildren( Object arg0 ) {
            if (arg0 instanceof JGrassMapsetGeoResource) {
                return true;
            } else if (arg0 instanceof JGrassMapGeoResource) {
                return false;
            }
            return false;
        }

        /**
         * Gets the root element(s) of the tree
         * 
         * @param arg0 the input data
         * @return Object[]
         */
        public Object[] getElements( Object arg0 ) {
            // add the service to the catalog
            ICatalog catalog = CatalogPlugin.getDefault().getLocalCatalog();
            ArrayList<IResolve> neededCatalogMembers = null;
            try {
                List< ? extends IResolve> allCatalogMembers = catalog.members(null);
                // for jgrass locations we extract the mapset out of the
                // locations = from
                // JGrassService
                neededCatalogMembers = new ArrayList<IResolve>();
                for( IResolve catalogMember : allCatalogMembers ) {
                    if (catalogMember instanceof JGrassService) {
                        List<IResolve> layers = ((JGrassService) catalogMember).members(null);
                        for( IResolve resource : layers ) {
                            if (mapset != null && resource instanceof JGrassMapsetGeoResource) {
                                JGrassMapsetGeoResource map = (JGrassMapsetGeoResource) resource;
                                File refFile = map.getFile();
                                File mapsetFile = new File(mapset);

                                if (refFile.getAbsolutePath().equals(mapsetFile.getAbsolutePath())) {
                                    neededCatalogMembers.add(resource);
                                }
                            } else {
                                neededCatalogMembers.add(resource);
                            }
                        }
                    }
                }
            } catch (IOException e) {
                UiUtilitiesPlugin.log("UiUtilitiesPlugin problem", e); //$NON-NLS-1$
                e.printStackTrace();
            }

            if (neededCatalogMembers != null) {
                return neededCatalogMembers.toArray();
            } else {
                return null;
            }
        }

        /**
         * Disposes any created resources
         */
        public void dispose() {
            // Nothing to dispose
        }

        /**
         * Called when the input changes
         * 
         * @param arg0 the viewer
         * @param arg1 the old input
         * @param arg2 the new input
         */
        public void inputChanged( Viewer arg0, Object arg1, Object arg2 ) {
            // Nothing to change
        }
    }

    /**
     * This class provides the labels for the file tree
     */

    private class LabelProvider implements ILabelProvider {
        // The listeners
        private final List<ILabelProviderListener> listeners;

        // Images for tree nodes
        private final Image rasterMaps;
        private final Image mainRasterMaps;
        private final Image grassasciiRasterMaps;
        private final Image esriasciiRasterMaps;
        private final Image fluidturtleRasterMaps;
        private final Image problemRasterMaps;

        // Label provider state: preserve case of file names/directories
        boolean preserveCase = true;

        /**
         * Constructs a FileTreeLabelProvider
         */
        public LabelProvider() {
            // Create the list to hold the listeners
            listeners = new ArrayList<ILabelProviderListener>();

            // Create the images
            rasterMaps = CatalogUIPlugin.getDefault().getImages().getImageDescriptor(
                    ISharedImages.GRID_OBJ).createImage();
            mainRasterMaps = AbstractUIPlugin.imageDescriptorFromPlugin(JGrassPlugin.PLUGIN_ID,
                    "icons/obj16/jgrass_obj.gif").createImage(); //$NON-NLS-1$
            grassasciiRasterMaps = AbstractUIPlugin.imageDescriptorFromPlugin(
                    JGrassPlugin.PLUGIN_ID, "icons/obj16/grassascii.gif").createImage(); //$NON-NLS-1$
            esriasciiRasterMaps = AbstractUIPlugin.imageDescriptorFromPlugin(
                    JGrassPlugin.PLUGIN_ID, "icons/obj16/esrigrid.gif").createImage(); //$NON-NLS-1$
            fluidturtleRasterMaps = AbstractUIPlugin.imageDescriptorFromPlugin(
                    JGrassPlugin.PLUGIN_ID, "icons/obj16/ftraster.gif").createImage(); //$NON-NLS-1$
            problemRasterMaps = AbstractUIPlugin.imageDescriptorFromPlugin(JGrassPlugin.PLUGIN_ID,
                    "icons/obj16/problem.gif").createImage(); //$NON-NLS-1$
        }

        /**
         * Sets the preserve case attribute
         * 
         * @param preserveCase the preserve case attribute
         */
        public void setPreserveCase( boolean preserveCase ) {
            this.preserveCase = preserveCase;

            // Since this attribute affects how the labels are computed,
            // notify all the listeners of the change.
            LabelProviderChangedEvent event = new LabelProviderChangedEvent(this);
            for( int i = 0, n = listeners.size(); i < n; i++ ) {
                ILabelProviderListener ilpl = listeners.get(i);
                ilpl.labelProviderChanged(event);
            }
        }

        /**
         * Gets the image to display for a node in the tree
         * 
         * @param arg0 the node
         * @return Image
         */
        public Image getImage( Object arg0 ) {
            if (arg0 instanceof JGrassMapsetGeoResource) {
                return mainRasterMaps;
            } else if (arg0 instanceof JGrassMapGeoResource) {
                // support all raster types known
                if (((JGrassMapGeoResource) arg0).getType().equals(
                        JGrassConstants.GRASSBINARYRASTERMAP)) {

                    return rasterMaps;
                } else if (((JGrassMapGeoResource) arg0).getType().equals(
                        JGrassConstants.GRASSASCIIRASTERMAP)) {
                    return grassasciiRasterMaps;
                } else if (((JGrassMapGeoResource) arg0).getType().equals(
                        JGrassConstants.ESRIRASTERMAP)) {
                    return esriasciiRasterMaps;
                } else if (((JGrassMapGeoResource) arg0).getType().equals(
                        JGrassConstants.FTRASTERMAP)) {
                    return fluidturtleRasterMaps;
                } else {
                    return problemRasterMaps;
                }

            } else {
                return null;
            }
        }

        /**
         * Gets the text to display for a node in the tree
         * 
         * @param arg0 the node
         * @return String
         */
        public String getText( Object arg0 ) {

            String text = null;
            try {
                if (arg0 instanceof JGrassMapsetGeoResource) {
                    String locationName = ((JGrassService) ((JGrassMapsetGeoResource) arg0)
                            .parent(null)).getInfo(null).getTitle();
                    String mapsetName;
                    mapsetName = ((JGrassMapsetGeoResource) arg0).getTitle();

                    text = locationName
                            + Messages
                                    .getString("JGRasterCatalogTreeViewer.loc-mapset-name-delimiter") + mapsetName; //$NON-NLS-1$
                } else if (arg0 instanceof JGrassMapGeoResource) {
                    text = ((JGrassMapGeoResource) arg0).getInfo(null).getTitle();
                }
            } catch (IOException e) {
                UiUtilitiesPlugin.log("UiUtilitiesPlugin problem", e); //$NON-NLS-1$
                e.printStackTrace();
            }

            return text;
        }

        /**
         * Adds a listener to this label provider
         * 
         * @param arg0 the listener
         */
        public void addListener( ILabelProviderListener arg0 ) {
            listeners.add(arg0);
        }

        /**
         * Called when this LabelProvider is being disposed
         */
        public void dispose() {
            // Dispose the images
            if (rasterMaps != null)
                rasterMaps.dispose();
        }

        /**
         * Returns whether changes to the specified property on the specified element would affect
         * the label for the element
         * 
         * @param arg0 the element
         * @param arg1 the property
         * @return boolean
         */
        public boolean isLabelProperty( Object arg0, String arg1 ) {
            return false;
        }

        /**
         * Removes the listener
         * 
         * @param arg0 the listener to remove
         */
        public void removeListener( ILabelProviderListener arg0 ) {
            listeners.remove(arg0);
        }
    }

    /*
     * (non-Javadoc)
     * @see eu.hydrologis.jgrass.ui.utilities.ResourcesSelector#getSelectedLayers()
     */
    public List<IGeoResource> getSelectedLayers() {
        return itemLayers;
    }

}
