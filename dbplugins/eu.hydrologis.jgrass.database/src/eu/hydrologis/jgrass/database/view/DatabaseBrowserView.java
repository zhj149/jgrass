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
package eu.hydrologis.jgrass.database.view;

import i18n.database.Messages;

import java.io.IOException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import eu.hydrologis.jgrass.database.DatabasePlugin;
import eu.hydrologis.jgrass.database.core.ConnectionManager;
import eu.hydrologis.jgrass.database.core.DatabaseConnectionProperties;
import eu.hydrologis.jgrass.database.interfaces.IConnectionFactory;

/**
 * The browser view where the database H2 webserver browser is opened.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class DatabaseBrowserView extends ViewPart {
    
    public static final String ID = "eu.hydrologis.jgrass.database.browserview"; //$NON-NLS-1$

    private Browser browser;
    public DatabaseBrowserView() {
    }

    @Override
    public void createPartControl( Composite parent ) {
        GridData gridData = new GridData();
        gridData.horizontalAlignment = GridData.FILL;
        gridData.grabExcessHorizontalSpace = true;
        gridData.grabExcessVerticalSpace = true;
        gridData.verticalAlignment = GridData.FILL;
        browser = new Browser(parent, SWT.NONE);
        browser.setLayoutData(gridData);

        try {
            DatabaseConnectionProperties databaseConnectionProperties = DatabasePlugin.getDefault()
                    .getActiveDatabaseConnectionProperties();
            IConnectionFactory connectionFactory = ConnectionManager.getDatabaseConnectionFactory(databaseConnectionProperties);
            String webserverConnectionString = connectionFactory.generateWebserverConnectionString(databaseConnectionProperties);
            browser.setUrl(webserverConnectionString);
        } catch (IOException e) {
            e.printStackTrace();

            StringBuilder sB = new StringBuilder();
            sB.append("<html> <head> </head> <body> <div style=\"text-align: center; color: rgb(256, 0, 0);\"><big><big>"); //$NON-NLS-1$
            sB.append(Messages.DatabaseBrowserView__no_db_connetion_established);
            sB.append("<br> <br> </big></big></div> </body> </html>"); //$NON-NLS-1$
            browser.setText(sB.toString());
        }
    }

    public void setFocus() {
    }

}
