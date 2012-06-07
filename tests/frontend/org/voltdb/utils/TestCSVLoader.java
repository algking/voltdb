package org.voltdb.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;

import org.voltdb.ServerThread;
import org.voltdb.VoltDB;
import org.voltdb.VoltTable;
import org.voltdb.VoltDB.Configuration;
import org.voltdb.VoltType;
import org.voltdb.client.Client;
import org.voltdb.client.ClientFactory;
import org.voltdb.compiler.VoltProjectBuilder;

import junit.framework.TestCase;

public class TestCSVLoader extends TestCase {
	
	private String simpleSchema;
	private String pathToCatalog = Configuration.getPathToCatalogForTest("csv.jar");
    private String pathToDeployment = Configuration.getPathToCatalogForTest("csv.xml");
    private ServerThread localServer;
    private VoltDB.Configuration config;
    private VoltProjectBuilder builder;
    private Client client;
    
    private String userHome = System.getProperty("user.home"); 
    private String reportdir = userHome + "/";
    String path_csv = userHome + "test.csv";
        
    private String []myData_Blank = {"1","","2"};
    private String []myData;
    
    private String []params = {
    		//userHome + "/testdb.csv",
    		"--inputfile=" + userHome + "/test.csv", 
    		//"--procedurename=BLAH.insert",
    		"--reportdir=" + reportdir,
    		"--tablename=BLAH",
    		"--abortfailurecount=50",
    		//"--skipemptyrecords=false",
    		"--trimwhitespace=true"
    		};

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        
        simpleSchema =
                "create table BLAH (" +
                "clm_integer integer default 0 not null, " + // column that is partitioned on

                //"clm_tinyint tinyint default 0, " +
                //"clm_smallint smallint default 0, " +
                //"clm_bigint bigint default 0, " +
                
               // "clm_string varchar(10) default null, " +
                //"clm_decimal decimal default null, " +
                //"clm_float float default 1.0, "+ // for later
                //"clm_timestamp timestamp default null, " + // for later
                //"clm_varinary varbinary default null" + // for later
                "); ";
        
        pathToCatalog = Configuration.getPathToCatalogForTest("csv.jar");
        pathToDeployment = Configuration.getPathToCatalogForTest("csv.xml");
        builder = new VoltProjectBuilder();
        builder.addLiteralSchema(simpleSchema);
        builder.addPartitionInfo("BLAH", "clm_integer");
        //builder.addStmtProcedure("Insert", "insert into blah values (?, ?, ?);", null);
        //builder.addStmtProcedure("InsertWithDate", "INSERT INTO BLAH VALUES (974599638818488300, 5, 'nullchar');");
        boolean success = builder.compile(pathToCatalog, 2, 1, 0);
        assertTrue(success);
        MiscUtils.copyFile(builder.getPathToDeployment(), pathToDeployment);

        config = new VoltDB.Configuration();
        config.m_pathToCatalog = pathToCatalog;
        config.m_pathToDeployment = pathToDeployment;
        localServer = new ServerThread(config);
        client = null;
    }

	public void testSimple() throws Exception {
		 String []params_simple = {
	    		//userHome + "/testdb.csv",
	    		"--inputfile=" + userHome + "/testdb.csv", 
	    		//"--procedurename=BLAH.insert",
	    		"--reportdir=" + reportdir,
	    		"--tablename=BLAH",
	    		"--abortfailurecount=50",
	    		//"--skipemptyrecords=false",
	    		"--trimwhitespace=true"
	    		};
        try {
            localServer.start();
            localServer.waitForInitialization();
           
            CSVLoader.main(params_simple);
            // do the test
            client = ClientFactory.createClient();
            client.createConnection("localhost");
            
            VoltTable modCount;
            modCount = client.callProcedure("@AdHoc", "SELECT * FROM BLAH;").getResults()[0];
            System.out.println("data inserted to table BLAH:\n" + modCount);
            
            modCount = client.callProcedure("@AdHoc", "SELECT COUNT(*) FROM BLAH;").getResults()[0];
            int rowct = modCount.getRowCount();
                        
            BufferedReader csvreport = new BufferedReader(new FileReader(new String(reportdir + CSVLoader.reportfile)));
            int lineCount = 0;
            String line = "";
            String promptMsg = "Number of acknowledged tuples:";
            
            while ((line = csvreport.readLine()) != null) {
            	if (line.startsWith(promptMsg)) {
            		String num = line.substring(promptMsg.length());
            		lineCount = Integer.parseInt(num.replaceAll("\\s",""));
            		break;
            	}
            }
            System.out.println(String.format("The rows infected: (%d,%s)", lineCount, rowct));
            assertEquals(lineCount, rowct);
        }
        finally {
            if (client != null) client.close();
            client = null;

            if (localServer != null) {
                localServer.shutdown();
                localServer.join();
            }
            localServer = null;

            // no clue how helpful this is
            System.gc();
        }
	}
	
	public void testBlankLine() throws Exception {
		try{
			BufferedWriter out_csv = new BufferedWriter( new FileWriter( path_csv ) );
			for( int i = 0; i < myData_Blank.length; i++ )
				out_csv.write( myData_Blank[ i ]+"\n" );
		}
		catch( Exception e) {
		}
		
		try {
            localServer.start();
            localServer.waitForInitialization();
           
            CSVLoader.main(params);
            // do the test
            client = ClientFactory.createClient();
            client.createConnection("localhost");
            
            VoltTable modCount;
            modCount = client.callProcedure("@AdHoc", "SELECT * FROM BLAH;").getResults()[0];
            System.out.println("data inserted to table BLAH:\n" + modCount);
            
            modCount = client.callProcedure("@AdHoc", "SELECT COUNT(*) FROM BLAH;").getResults()[0];
            int rowct = modCount.getRowCount();
                        
            BufferedReader csvreport = new BufferedReader(new FileReader(new String(reportdir + CSVLoader.reportfile)));
            int lineCount = 0;
            String line = "";
            String promptMsg = "Number of acknowledged tuples:";
            
            while ((line = csvreport.readLine()) != null) {
            	if (line.startsWith(promptMsg)) {
            		String num = line.substring(promptMsg.length());
            		lineCount = Integer.parseInt(num.replaceAll("\\s",""));
            		break;
            	}
            }
            System.out.println(String.format("The rows infected: (%d,%s)", lineCount, rowct));
            assertEquals(lineCount, rowct);
        }
        finally {
            if (client != null) client.close();
            client = null;

            if (localServer != null) {
                localServer.shutdown();
                localServer.join();
            }
            localServer = null;

            // no clue how helpful this is
            System.gc();
        }
	}
}