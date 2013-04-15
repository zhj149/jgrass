package i18n.database;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
    private static final String BUNDLE_NAME = "i18n.database.messages"; //$NON-NLS-1$
    public static String ConnectionManager__unknown_db_type;
    public static String databaseplugin__activate_connection;
    public static String databaseplugin__connecting_db;
    public static String databaseplugin__connection_error;
    public static String databaseplugin__connection_name;
    public static String databaseplugin__database_folder;
    public static String databaseplugin__database_host;
    public static String databaseplugin__database_name;
    public static String databaseplugin__database_password;
    public static String databaseplugin__database_port;
    public static String databaseplugin__database_type;
    public static String databaseplugin__database_user;
    public static String databaseplugin__disconnect;
    public static String databaseplugin__disconnecting_db;
    public static String databaseplugin__errmsg_connection;
    public static String databaseplugin__open_db_location;
    public static String DatabaseBrowserView__no_db_connetion_established;
    public static String DatabaseConnectionPropertiesWidget__create_db_and_connect;
    public static String DatabaseConnectionPropertiesWidget__double_db_definition_warning;
    public static String DatabaseConnectionPropertiesWidget__warning;
    public static String DatabasePlugin__errmsg_connecting_db;
    public static String DatabasePlugin__errmsg_creating_db;
    public static String DatabaseView__connections;
    public static String DatabaseView__db_not_removed_warning;
    public static String DatabaseView__filter_active;
    public static String DatabaseView__filter_project;
    public static String DatabaseView__new_local_db;
    public static String DatabaseView__new_remote_db;
    public static String DatabaseView__no_item_selected;
    public static String ExportDatabaseAction__errmsg_db_export;
    public static String ExportDatabaseAction__export_db;
    public static String ExportDatabaseAction__select_zip_file;
    public static String H2ConnectionFactory__db_doesnt_exist;
    public static String H2ConnectionFactory__default_db;
    public static String ImportDatabaseAction__errmsg_db_import;
    public static String ImportDatabaseAction__import_db;
    public static String ImportDatabaseAction__select_folder;
    public static String ImportDatabaseAction__select_zip;
    public static String OpenDatabaseViewAction__errmsg_open_dbview;
    public static String OpenExistingLocalDatabaseAction__db_error;
    public static String OpenExistingLocalDatabaseAction__inserted_folder_not_dbfolder;
    public static String OpenExistingLocalDatabaseAction__inserted_folder_not_exist;
    public static String OpenInBrowserViewAction__errmsg_open_dbview;
    public static String RemoveDatabaseAction__remove_db_prompt;
    public static String RemoveDatabaseAction__remove_db_warning;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }
    private Messages() {
    }
}
