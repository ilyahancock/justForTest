package com.haulmont.testtask;

import com.haulmont.testtask.dbService.DBService;
import org.hibernate.cfg.Configuration;

import java.io.*;
import java.sql.*;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class theDB {
    public static class ScriptRunner {

        private static final String DEFAULT_DELIMITER = ";";
        private static final String DELIMITER_LINE_REGEX = "(?i)DELIMITER.+";
        private static final String DELIMITER_LINE_SPLIT_REGEX = "(?i)DELIMITER";

        private final Connection connection;
        private final boolean stopOnError;
        private final boolean autoCommit;
        private PrintWriter logWriter = new PrintWriter(System.out);
        private PrintWriter errorLogWriter = new PrintWriter(System.err);
        private String delimiter = DEFAULT_DELIMITER;
        private boolean fullLineDelimiter = false;

        /**
         * Default constructor.
         *
         * @param connection
         * @param autoCommit
         * @param stopOnError
         */
        public ScriptRunner(Connection connection, boolean autoCommit, boolean stopOnError) {
            this.connection = connection;
            this.autoCommit = autoCommit;
            this.stopOnError = stopOnError;
        }

        /**
         * @param delimiter
         * @param fullLineDelimiter
         */
        public void setDelimiter(String delimiter, boolean fullLineDelimiter) {
            this.delimiter = delimiter;
            this.fullLineDelimiter = fullLineDelimiter;
        }

        /**
         * Setter for logWriter property.
         *
         * @param logWriter
         *        - the new value of the logWriter property
         */
        public void setLogWriter(PrintWriter logWriter) {
            this.logWriter = logWriter;
        }

        /**
         * Setter for errorLogWriter property.
         *
         * @param errorLogWriter
         *        - the new value of the errorLogWriter property
         */
        public void setErrorLogWriter(PrintWriter errorLogWriter) {
            this.errorLogWriter = errorLogWriter;
        }

        /**
         * Runs an SQL script (read in using the Reader parameter).
         *
         * @param reader
         *        - the source of the script
         * @throws SQLException
         *         if any SQL errors occur
         * @throws IOException
         *         if there is an error reading from the Reader
         */
        public void runScript(Reader reader) throws IOException, SQLException {
            try {
                boolean originalAutoCommit = connection.getAutoCommit();
                try {
                    if (originalAutoCommit != autoCommit) {
                        connection.setAutoCommit(autoCommit);
                    }
                    runScript(connection, reader);
                } finally {
                    connection.setAutoCommit(originalAutoCommit);
                }
            } catch (IOException e) {
                throw e;
            } catch (SQLException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException("Error running script.  Cause: " + e, e);
            }
        }

        /**
         * Runs an SQL script (read in using the Reader parameter) using the connection passed in.
         *
         * @param conn
         *        - the connection to use for the script
         * @param reader
         *        - the source of the script
         * @throws SQLException
         *         if any SQL errors occur
         * @throws IOException
         *         if there is an error reading from the Reader
         */
        private void runScript(Connection conn, Reader reader) throws IOException, SQLException {
            StringBuffer command = null;
            try {
                LineNumberReader lineReader = new LineNumberReader(reader);
                String line = null;
                while ((line = lineReader.readLine()) != null) {
                    if (command == null) {
                        command = new StringBuffer();
                    }
                    String trimmedLine = line.trim();
                    if (trimmedLine.startsWith("--")) {
                        println(trimmedLine);
                    } else if (trimmedLine.length() < 1 || trimmedLine.startsWith("//")) {
                        // Do nothing
                    } else if (trimmedLine.length() < 1 || trimmedLine.startsWith("--")) {
                        // Do nothing
                    } else if (!fullLineDelimiter && trimmedLine.endsWith(getDelimiter())
                            || fullLineDelimiter && trimmedLine.equals(getDelimiter())) {

                        Pattern pattern = Pattern.compile(DELIMITER_LINE_REGEX);
                        Matcher matcher = pattern.matcher(trimmedLine);
                        if (matcher.matches()) {
                            setDelimiter(trimmedLine.split(DELIMITER_LINE_SPLIT_REGEX)[1].trim(),
                                    fullLineDelimiter);
                            line = lineReader.readLine();
                            if (line == null) {
                                break;
                            }
                            trimmedLine = line.trim();
                        }

                        command.append(line.substring(0, line.lastIndexOf(getDelimiter())));
                        command.append(" ");
                        Statement statement = conn.createStatement();

                        println(command);

                        boolean hasResults = false;
                        if (stopOnError) {
                            hasResults = statement.execute(command.toString());
                        } else {
                            try {
                                statement.execute(command.toString());
                            } catch (SQLException e) {
                                e.fillInStackTrace();
                                printlnError("Error executing: " + command);
                                printlnError(e);
                            }
                        }

                        if (autoCommit && !conn.getAutoCommit()) {
                            conn.commit();
                        }

                        ResultSet rs = statement.getResultSet();
                        if (hasResults && rs != null) {
                            ResultSetMetaData md = rs.getMetaData();
                            int cols = md.getColumnCount();
                            for (int i = 0; i < cols; i++) {
                                String name = md.getColumnLabel(i);
                                print(name + "\t");
                            }
                            println("");
                            while (rs.next()) {
                                for (int i = 1; i <= cols; i++) {
                                    String value = rs.getString(i);
                                    print(value + "\t");
                                }
                                println("");
                            }
                        }

                        command = null;
                        try {
                            if (rs != null) {
                                rs.close();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        try {
                            if (statement != null) {
                                statement.close();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            // Ignore to workaround a bug in Jakarta DBCP
                        }
                    } else {
                        Pattern pattern = Pattern.compile(DELIMITER_LINE_REGEX);
                        Matcher matcher = pattern.matcher(trimmedLine);
                        if (matcher.matches()) {
                            setDelimiter(trimmedLine.split(DELIMITER_LINE_SPLIT_REGEX)[1].trim(),
                                    fullLineDelimiter);
                            line = lineReader.readLine();
                            if (line == null) {
                                break;
                            }
                            trimmedLine = line.trim();
                        }
                        command.append(line);
                        command.append(" ");
                    }
                }
                if (!autoCommit) {
                    conn.commit();
                }
            } catch (SQLException e) {
                e.fillInStackTrace();
                printlnError("Error executing: " + command);
                printlnError(e);
                throw e;
            } catch (IOException e) {
                e.fillInStackTrace();
                printlnError("Error executing: " + command);
                printlnError(e);
                throw e;
            } finally {
                conn.rollback();
                flush();
            }
        }

        private String getDelimiter() {
            return delimiter;
        }

        private void print(Object o) {
            if (logWriter != null) {
                logWriter.print(o);
            }
        }

        private void println(Object o) {
            if (logWriter != null) {
                logWriter.println(o);
            }
        }

        private void printlnError(Object o) {
            if (errorLogWriter != null) {
                errorLogWriter.println(o);
            }
        }

        private void flush() {
            if (logWriter != null) {
                logWriter.flush();
            }
            if (errorLogWriter != null) {
                errorLogWriter.flush();
            }
        }
    }

    private static Properties getH2Properies() {
        Configuration h2 = DBService.getH2Configuration();
        Properties configuration = new Properties();
        configuration.setProperty("driver.class", h2.getProperty("hibernate.connection.driver_class"));// , "org.h2.Driver");
        configuration.setProperty("driver.url", h2.getProperty("hibernate.connection.url"));// , "jdbc:h2:./h2db");
        configuration.setProperty("user", h2.getProperty("hibernate.connection.username"));// , "tully");
        configuration.setProperty("password", h2.getProperty("hibernate.connection.password"));//, "tully");
        return configuration;
    }

    private static Properties getHSQLDBProperies() {
        Configuration hsqldb = DBService.getHSQLDBConfiguration();
        Properties configuration = new Properties();
        configuration.setProperty("driver.class", hsqldb.getProperty("hibernate.connection.driver_class"));// "org.hsqldb.jdbcDriver");
        configuration.setProperty("driver.url", hsqldb.getProperty("hibernate.connection.url"));// , "jdbc:hsqldb:file:testdb");
        configuration.setProperty("user", hsqldb.getProperty("hibernate.connection.username"));// , "sa");
        configuration.setProperty("password", hsqldb.getProperty("hibernate.connection.password"));// , "");
        return configuration;
    }

    public static void execSQL(String aSQLScriptFilePath) throws ClassNotFoundException,SQLException {
        Properties props = getHSQLDBProperies();
        String driverClassName = props.getProperty("driver.class");
        String driverURL = props.getProperty("driver.url");

         Connection dbConn = null;
        try {
            Class.forName(driverClassName);
            dbConn = DriverManager.getConnection(driverURL, props);
        }
        catch( Exception e ) {
            System.err.println("Unable to connect to database: "+e);
        }

        try {
            ScriptRunner sr = new ScriptRunner(dbConn,false,false);
            Reader reader = new BufferedReader( new FileReader(aSQLScriptFilePath) );
            sr.runScript(dbConn, reader);
        } catch (Exception e) {
            System.err.println("Failed to Execute" + aSQLScriptFilePath
                    + " The error is " + e.getMessage());
        }
    }

    /** Dump the whole database to an SQL string */
    public static String dumpDB() {
        Properties props = getHSQLDBProperies();
        String driverClassName = props.getProperty("driver.class");
        String driverURL = props.getProperty("driver.url");
        // Default to not having a quote character
        String columnNameQuote = props.getProperty("columnName.quoteChar", "");
        DatabaseMetaData dbMetaData = null;
        Connection dbConn = null;
        try {
            Class.forName(driverClassName);
            dbConn = DriverManager.getConnection(driverURL, props);
            dbMetaData = dbConn.getMetaData();
        }
        catch( Exception e ) {
            System.err.println("Unable to connect to database: "+e);
            return null;
        }

        try {
            StringBuffer result = new StringBuffer();
            String catalog = props.getProperty("catalog");
            String schema = props.getProperty("schemaPattern");
            String tables = props.getProperty("tableName");
            ResultSet rs = dbMetaData.getTables(catalog, schema, tables, null);
            if (! rs.next()) {
                System.err.println("Unable to find any tables matching: catalog="+catalog+" schema="+schema+" tables="+tables);
                rs.close();
            } else {
                // Right, we have some tables, so we can go to work.
                // the details we have are
                // TABLE_CAT String => table catalog (may be null)
                // TABLE_SCHEM String => table schema (may be null)
                // TABLE_NAME String => table name
                // TABLE_TYPE String => table type. Typical types are "TABLE", "VIEW", "SYSTEM TABLE", "GLOBAL TEMPORARY", "LOCAL TEMPORARY", "ALIAS", "SYNONYM".
                // REMARKS String => explanatory comment on the table
                // TYPE_CAT String => the types catalog (may be null)
                // TYPE_SCHEM String => the types schema (may be null)
                // TYPE_NAME String => type name (may be null)
                // SELF_REFERENCING_COL_NAME String => name of the designated "identifier" column of a typed table (may be null)
                // REF_GENERATION String => specifies how values in SELF_REFERENCING_COL_NAME are created. Values are "SYSTEM", "USER", "DERIVED". (may be null)
                // We will ignore the schema and stuff, because people might want to import it somewhere else
                // We will also ignore any tables that aren't of type TABLE for now.
                // We use a do-while because we've already caled rs.next to see if there are any rows
                do {
                    String tableName = rs.getString("TABLE_NAME");
                    String tableType = rs.getString("TABLE_TYPE");
                    if ("TABLE".equalsIgnoreCase(tableType)) {
                        result.append("\n\n-- "+tableName);
                        result.append("\nCREATE TABLE "+tableName+" (\n");
                        ResultSet tableMetaData = dbMetaData.getColumns(null, null, tableName, "%");
                        boolean firstLine = true;
                        while (tableMetaData.next()) {
                            if (firstLine) {
                                firstLine = false;
                            } else {
                                // If we're not the first line, then finish the previous line with a comma
                                result.append(",\n");
                            }
                            String columnName = tableMetaData.getString("COLUMN_NAME");
                            String columnType = tableMetaData.getString("TYPE_NAME");
                            // WARNING: this may give daft answers for some types on some databases (eg JDBC-ODBC link)
                            int columnSize = tableMetaData.getInt("COLUMN_SIZE");
                            String nullable = tableMetaData.getString("IS_NULLABLE");
                            String nullString = "NULL";
                            if ("NO".equalsIgnoreCase(nullable)) {
                                nullString = "NOT NULL";
                            }
                            result.append("    "+columnNameQuote+columnName+columnNameQuote+" "+columnType+" ("+columnSize+")"+" "+nullString);
                        }
                        tableMetaData.close();

                        // Now we need to put the primary key constraint
                        try {
                            ResultSet primaryKeys = dbMetaData.getPrimaryKeys(catalog, schema, tableName);
                            // What we might get:
                            // TABLE_CAT String => table catalog (may be null)
                            // TABLE_SCHEM String => table schema (may be null)
                            // TABLE_NAME String => table name
                            // COLUMN_NAME String => column name
                            // KEY_SEQ short => sequence number within primary key
                            // PK_NAME String => primary key name (may be null)
                            String primaryKeyName = null;
                            StringBuffer primaryKeyColumns = new StringBuffer();
                            while (primaryKeys.next()) {
                                String thisKeyName = primaryKeys.getString("PK_NAME");
                                if ((thisKeyName != null && primaryKeyName == null)
                                        || (thisKeyName == null && primaryKeyName != null)
                                        || (thisKeyName != null && ! thisKeyName.equals(primaryKeyName))
                                        || (primaryKeyName != null && ! primaryKeyName.equals(thisKeyName))) {
                                    // the keynames aren't the same, so output all that we have so far (if anything)
                                    // and start a new primary key entry
                                    if (primaryKeyColumns.length() > 0) {
                                        // There's something to output
                                        result.append(",\n    PRIMARY KEY ");
                                        if (primaryKeyName != null) { result.append(primaryKeyName); }
                                        result.append("("+primaryKeyColumns.toString()+")");
                                    }
                                    // Start again with the new name
                                    primaryKeyColumns = new StringBuffer();
                                    primaryKeyName = thisKeyName;
                                }
                                // Now append the column
                                if (primaryKeyColumns.length() > 0) {
                                    primaryKeyColumns.append(", ");
                                }
                                primaryKeyColumns.append(primaryKeys.getString("COLUMN_NAME"));
                            }
                            if (primaryKeyColumns.length() > 0) {
                                // There's something to output
                                result.append(",\n    PRIMARY KEY ");
                                if (primaryKeyName != null) { result.append(primaryKeyName); }
                                result.append(" ("+primaryKeyColumns.toString()+")");
                            }
                        } catch (SQLException e) {
                            // NB you will get this exception with the JDBC-ODBC link because it says
                            // [Microsoft][ODBC Driver Manager] Driver does not support this function
                            System.err.println("Unable to get primary keys for table "+tableName+" because "+e);
                        }

                        result.append("\n);\n");

                        // Right, we have a table, so we can go and dump it
                        dumpTable(dbConn, result, tableName);
                    }
                } while (rs.next());
                rs.close();
            }
            dbConn.close();
            return result.toString();
        } catch (SQLException e) {
            e.printStackTrace();  //To change body of catch statement use Options | File Templates.
        }
        return null;
    }

    /** dump this particular table to the string buffer */
    private static void dumpTable(Connection dbConn, StringBuffer result, String tableName) {
        try {
            // First we output the create table stuff
            PreparedStatement stmt = dbConn.prepareStatement("SELECT * FROM "+tableName);
            ResultSet rs = stmt.executeQuery();
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            // Now we can output the actual data
            result.append("\n\n-- Data for "+tableName+"\n");
            while (rs.next()) {
                result.append("INSERT INTO "+tableName+" VALUES (");
                for (int i=0; i<columnCount; i++) {
                    if (i > 0) {
                        result.append(", ");
                    }
                    Object value = rs.getObject(i+1);
                    if (value == null) {
                        result.append("NULL");
                    } else {
                        String outputValue = value.toString();
                        outputValue = outputValue.replaceAll("'","\\'");
                        result.append("'"+outputValue+"'");
                    }
                }
                result.append(");\n");
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            System.err.println("Unable to dump table "+tableName+" because: "+e);
        }
    }
}
