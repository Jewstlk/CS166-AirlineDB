/*
 * Template JAVA User Interface
 * =============================
 *
 * Database Management Systems
 * Department of Computer Science &amp; Engineering
 * University of California - Riverside
 *
 * Target DBMS: 'Postgres'
 *
 */
//ssh jmira006@bolt.cs.ucr.edu

import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Random;

/**
 * This class defines a simple embedded SQL utility class that is designed to
 * work with PostgreSQL JDBC drivers.
 *
 */

public class AirBooking{
	//reference to physical database connection
	private Connection _connection = null;
	static BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
	
	public AirBooking(String dbname, String dbport, String user, String passwd) throws SQLException {
		System.out.print("Connecting to database...");
		try{
			// constructs the connection URL
			String url = "jdbc:postgresql://localhost:" + dbport + "/" + dbname;
			System.out.println ("Connection URL: " + url + "\n");
			
			// obtain a physical connection
	        this._connection = DriverManager.getConnection(url, user, passwd);
	        System.out.println("Done");
		}catch(Exception e){
			System.err.println("Error - Unable to Connect to Database: " + e.getMessage());
	        System.out.println("Make sure you started postgres on this machine");
	        System.exit(-1);
		}
	}
	
	/**
	 * Method to execute an update SQL statement.  Update SQL instructions
	 * includes CREATE, INSERT, UPDATE, DELETE, and DROP.
	 * 
	 * @param sql the input SQL string
	 * @throws java.sql.SQLException when update failed
	 * */
	public void executeUpdate (String sql) throws SQLException { 
		// creates a statement object
		Statement stmt = this._connection.createStatement ();

		// issues the update instruction
		stmt.executeUpdate (sql);

		// close the instruction
	    stmt.close ();
	}//end executeUpdate

	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and outputs the results to
	 * standard out.
	 * 
	 * @param query the input query string
	 * @return the number of rows returned
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public int executeQueryAndPrintResult (String query) throws SQLException {
		//creates a statement object
		Statement stmt = this._connection.createStatement ();


		//issues the query instruction
		ResultSet rs = stmt.executeQuery (query);

		/*
		 *  obtains the metadata object for the returned result set.  The metadata
		 *  contains row and column info.
		 */
		ResultSetMetaData rsmd = rs.getMetaData ();
		int numCol = rsmd.getColumnCount ();
		int rowCount = 0;
		
		//iterates through the result set and output them to standard out.
		boolean outputHeader = true;
		while (rs.next()){
			if(outputHeader){
				for(int i = 1; i <= numCol; i++){
					System.out.format("%-25s", rsmd.getColumnName(i));
			    }
			    System.out.println();
			    outputHeader = false;
			}
			for (int i=1; i<=numCol; ++i)
				System.out.format("%-25s", rs.getString(i));
			System.out.println ();
			++rowCount;
		}//end while
		stmt.close ();
		return rowCount;
	}
	
	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and returns the results as
	 * a list of records. Each record in turn is a list of attribute values
	 * 
	 * @param query the input query string
	 * @return the query result as a list of records
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public List<List<String>> executeQueryAndReturnResult (String query) throws SQLException { 
		//creates a statement object 
		Statement stmt = this._connection.createStatement (); 
		
		//issues the query instruction 
		ResultSet rs = stmt.executeQuery (query); 
	 
		/*
		 * obtains the metadata object for the returned result set.  The metadata 
		 * contains row and column info. 
		*/ 
		ResultSetMetaData rsmd = rs.getMetaData (); 
		int numCol = rsmd.getColumnCount (); 
		int rowCount = 0; 
	 
		//iterates through the result set and saves the data returned by the query. 
		boolean outputHeader = false;
		List<List<String>> result  = new ArrayList<List<String>>(); 
		while (rs.next()){
			List<String> record = new ArrayList<String>(); 
			for (int i=1; i<=numCol; ++i) 
				record.add(rs.getString (i)); 
			result.add(record); 
		}//end while 
		stmt.close (); 
		return result; 
	}//end executeQueryAndReturnResult
	
	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and returns the number of results
	 * 
	 * @param query the input query string
	 * @return the number of rows returned
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public int executeQuery (String query) throws SQLException {
		//creates a statement object
		Statement stmt = this._connection.createStatement ();

		//issues the query instruction
		ResultSet rs = stmt.executeQuery (query);

		int rowCount = 0;

		//iterates through the result set and count nuber of results.
		if(rs.next()){
			rowCount++;
		}//end while
		stmt.close ();
		return rowCount;
	}
	
	/**
	 * Method to fetch the last value from sequence. This
	 * method issues the query to the DBMS and returns the current 
	 * value of sequence used for autogenerated keys
	 * 
	 * @param sequence name of the DB sequence
	 * @return current value of a sequence
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	
	public int getCurrSeqVal(String sequence) throws SQLException {
		Statement stmt = this._connection.createStatement ();
		
		ResultSet rs = stmt.executeQuery (String.format("Select currval('%s')", sequence));
		if (rs.next()) return rs.getInt(1);
		return -1;
	}

	/**
	 * Method to close the physical connection if it is open.
	 */
	public void cleanup(){
		try{
			if (this._connection != null){
				this._connection.close ();
			}//end if
		}catch (SQLException e){
	         // ignored.
		}//end try
	}//end cleanup

	/**
	 * The main execution method
	 * 
	 * @param args the command line arguments this inclues the <mysql|pgsql> <login file>
	 */
	public static void main (String[] args) {
		if (args.length != 3) {
			System.err.println (
				"Usage: " + "java [-classpath <classpath>] " + AirBooking.class.getName () +
		            " <dbname> <port> <user>");
			return;
		}//end if
		
		AirBooking esql = null;
		
		try{
			
			try {
				Class.forName("org.postgresql.Driver");
			}catch(Exception e){

				System.out.println("Where is your PostgreSQL JDBC Driver? " + "Include in your library path!");
				e.printStackTrace();
				return;
			}
			
			String dbname = args[0];
			String dbport = args[1];
			String user = args[2];
			
			esql = new AirBooking (dbname, dbport, user, "");
			
			boolean keepon = true;
			while(keepon){
				System.out.println("MAIN MENU");
				System.out.println("---------");
				System.out.println("1. Add Passenger");
				System.out.println("2. Book Flight");
				System.out.println("3. Review Flight");
				System.out.println("4. Insert or Update Flight");
				System.out.println("5. List Flights From Origin to Destination");
				System.out.println("6. List Most Popular Destinations");
				System.out.println("7. List Highest Rated Destinations");
				System.out.println("8. List Flights to Destination in order of Duration");
				System.out.println("9. Find Number of Available Seats on a given Flight");
				System.out.println("10. < EXIT");
				
				switch (readChoice()){
					case 1: AddPassenger(esql); break;
					case 2: BookFlight(esql); break;
					case 3: TakeCustomerReview(esql); break;
					case 4: InsertOrUpdateRouteForAirline(esql); break;
					case 5: ListAvailableFlightsBetweenOriginAndDestination(esql); break;
					case 6: ListMostPopularDestinations(esql); break;
					case 7: ListHighestRatedRoutes(esql); break;
					case 8: ListFlightFromOriginToDestinationInOrderOfDuration(esql); break;
					case 9: FindNumberOfAvailableSeatsForFlight(esql); break;
					case 10: keepon = false; break;
				}
			}
		}catch(Exception e){
			System.err.println (e.getMessage ());
		}finally{
			try{
				if(esql != null) {
					System.out.print("Disconnecting from database...");
					esql.cleanup ();
					System.out.println("Done\n\nBye !");
				}//end if				
			}catch(Exception e){
				// ignored.
			}
		}
	}

	public static int readChoice() {
		int input;
		// returns only if a correct value is given.
		do {
			System.out.print("Please make your choice: ");
			try { // read the integer, parse it and break.
				input = Integer.parseInt(in.readLine());
				break;
			}catch (Exception e) {
				System.out.println("Your input is invalid!");
				continue;
			}//end try
		}while (true);
		return input;
	}//end readChoice
	
	public static void AddPassenger(AirBooking esql){//1
		//Add a new passenger to the database
		//we can check if each input is valid or check everything at the end
		boolean pass = true;
		String passportnum= "he";
		String query;
		String pname;
		String pbday;
		String country; //where the passenger is from
		Scanner user_input = new Scanner(System.in);
		try
		{
			
			//passport number
			while(pass)
			{	System.out.println("Please enter your passport number: ");
				passportnum = user_input.next();
				query ="SELECT p.passNum " +
					   "FROM Passenger p " +
					   "WHERE p.passNum = '" + passportnum+"'";
				
				if(passportnum.length() !=10)
				{
					System.out.println("Insufficient characters");
				}
				else if(	esql.executeQuery(query) >= 1)
				{
					System.out.println("Invalid Number/Already Exists");
				}
				else
				{
					pass= false;
				}   
			}	
			user_input.nextLine();
			//passenger's Full name
			System.out.println("Please enter your Full Name: ");
			pname = user_input.nextLine();
			
			
			//passenger's Birth date //check if its valid probably do not need to check
			System.out.println("Please enter your Birth Date (year/month/day): ");
			pbday = user_input.next();
			//if(string
			
			//passenger's country
			System.out.println("Please enter your Country: ");
			country = user_input.next();
			
			query = "SELECT MAX(p.pID) FROM Passenger p";
			List<List<String>> list = esql.executeQueryAndReturnResult(query);
	
			int result = Integer.parseInt(list.get(0).get(0));
			result++;
			
			
			//inserting into the passenger table
			//System.out.println(list.get(0).get(0));
			query ="INSERT INTO Passenger(pID, passNum, fullName, bdate, country) " +
					"VALUES ( '" + result + "', " + "'" + passportnum +"', " + "'"+ pname +"', " + "'"+ pbday +"', " + "'"+ country +"')";               
					//"VALUES ( '400', 'asb', 'la', '123456', 'ca' )";
			esql.executeUpdate(query);		
			
		}
		catch (Exception e)
		{
			System.err.println(e.getMessage());
		}
	//user_input.close(); // closing scanner, to prevent bugs
		
	}
	
	public static void BookFlight(AirBooking esql){//2
		//Book Flight for an existing customer
		boolean pass = true;
		String passportnum= "he";
		String flightNum = "XD";
		String query1 = "he";
		String query2;
		String query3;
		String departure = "he";
		String pID;
		String queryT;
		String queryD;
		String bookRef = "he";
		String restart = "n";
		boolean repeat = true;
		int result = 0;
		Scanner user_input = new Scanner(System.in);
		
		try{
			while(repeat) {
				repeat = false;
				restart = "n";
				while(pass)
				{	
					System.out.println("Please enter your passport number: ");
					passportnum = user_input.next();
						
					query1 ="SELECT p.pID " +
							"FROM Passenger p " +
							"WHERE p.passNum = '" + passportnum+"'";
						
					if(passportnum.length() !=10)
					{
						System.out.println("Insufficient characters");
					}
					else if(esql.executeQuery(query1) < 1)
					{
						System.out.println("Passport Does not exist in DB");
					}
					else
					{
						pass= false;
					}   
				}
				pass = true;
				ListAvailableFlightsBetweenOriginAndDestination(esql);
				
				while(pass){
					while(pass)
					{
						System.out.println("Please enter the Flight Number you wish to book");
						flightNum = user_input.next();
						
						query2 ="SELECT f.flightNum " +
								"FROM Flight f " +
								"WHERE f.flightNum = '" + flightNum+"'";
						
						if (esql.executeQuery(query2) < 1)
						{
							System.out.println("Flight Number is Invalid/DNE");
						}		
						else
						{
							pass = false;
						}
					}
					
					
						System.out.println("Please Enter your desired departure date - mm/dd/yyyy");
						departure = user_input.next();
						
						queryD = "SELECT (f.seats - COUNT(b.pid)) AS RemainingSeats " +
								 "FROM Booking b, Flight f " +
								 "WHERE f.flightNum = '" + flightNum+ "' AND b.flightNum = '" + flightNum+"' AND b.departure = '" + departure+"'" +
								 "GROUP BY f.seats ";
								 
						if (esql.executeQuery(queryD) >= 1) {
							List<List<String>> listD = esql.executeQueryAndReturnResult(queryD);
							result = Integer.parseInt(listD.get(0).get(0));
							if ( result < 0 ) {
								System.out.println("Flight is full - please enter in a new date/flight heading to the same destination");
							}
							else {
							pass = false;
							}
						}
						else {
						pass = false;	
						}
				}
				pass = true;
				
				List<List<String>> list = esql.executeQueryAndReturnResult(query1);
				pID = list.get(0).get(0);
				
				while(pass)
				{
					String candidateChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
					StringBuilder sb = new StringBuilder();
					Random random = new Random();
					for (int i = 0; i < 10; i++) {
						sb.append(candidateChars.charAt(random.nextInt(candidateChars.length())));
					}
					bookRef = sb.toString();
					queryT ="SELECT b.bookRef " +
							"FROM Booking b " +
							"WHERE b.bookRef = '" + bookRef+"'";
					if (esql.executeQuery(queryT) < 1) {
						pass = false;
				
					}
				}
				String queryR = "SELECT * " +
								"FROM Booking b " +
								"WHERE B.flightNum = '" + flightNum+ "' AND b.pID = '" + pID+"' AND b.departure = '" + departure+"'";
				
				if (esql.executeQuery(queryR) >= 1) {
					System.out.println("You have already booked this flight!");
					System.out.println("Would you like to restart y/n: ");
					restart = user_input.next();
				}
				else {
					query3 ="INSERT INTO Booking(bookRef, departure, flightNum, pID) " +
						   "VALUES ( '" + bookRef + "', " + "'" + departure +"', " + "'"+ flightNum + "', " + "'" + pID + "')";
					esql.executeUpdate(query3);
				}
				
				if (restart.equals("y")) {
					repeat = true;
					pass = true;
				}
				else {
					repeat = false;
				}
				
		}
	}
	catch (Exception e)
	{
		System.err.println(e.getMessage());
	}
		
	}
	
	
	
	
	public static void TakeCustomerReview(AirBooking esql){//3
		//Insert customer review into the ratings table 
		//the score takes in values from 1-5. not including decimal numbers like 2.3
		int score=0;
		int pid=0;
		String passnum;
		String query;
		String flightNum="00";
		String comment="00";
		boolean inner =true;
		boolean pass = true;
		boolean com =false;
		Scanner userInput = new Scanner(System.in);
		//the user does not know their passenger id
		try
		{
			while(inner)
			{
				while(pass)
				{	
					//FIXME: If user inputs a letter the program goes into a infinite loop
					System.out.println("Please enter your Passport number: ");
					passnum = userInput.next();
				
					query ="SELECT p.pID " +
					   	    "FROM Passenger p " +
					  	 	"WHERE p.passNum = '" + passnum+"'";
					//want to check if the passenger is real
					
					if(passportnum.length() !=10)
					{
						System.out.println("Insufficient characters");
					}
					else if(esql.executeQuery(query) != 1)
					{
						System.out.println("Passenger number does not exist");
					}
					else
					{
						//need to store value of pID to pid
						//work in progress
						List<List<String>> list = esql.executeQueryAndReturnResult(query);
						pid = Integer.parseInt(list.get(0).get(0));
						pass= false;
					}   
				}
				pass = true;	
				//checking for Flight number
				while(pass)
				{	

					System.out.println("Please enter your Flight Number: ");
					flightNum = userInput.next();
				
			
					query ="SELECT b.flightNum " +
						   "FROM Booking b " +
						   "WHERE b.flightNum = '" + flightNum+"'";
				
					if(	esql.executeQuery(query) < 1)
					{
						System.out.println("Invalid flight Number/You did not book this flight");
					}
					else
					{
						pass= false;
					}   
				}
				
				query="SELECT r.pID, r.flightNum " +
					  "FROM Ratings r "+
					  "WHERE r.pID = '" + pid+ "' AND r.flightNum = '" + flightNum+"'";
				//this checks if the passengers has a done a review for a filghts	  
				if(	esql.executeQuery(query) >= 1)
				{
					System.out.println("Rating for this flight has been done, Try again");
				}
				else
				{
					inner = false;
				}	  	
				pass= true;
				
			}
			pass= true;
			
			//Score
			//what does _SCORE mean in the create.sql file
			// System.out.println("Please enter you Flight's score from 0-5\n");
// 			score = user_input.nextInt();
			while(pass)
			{	
				System.out.println("Please enter you Flight's score from 0-5\n");
				score = userInput.nextInt();
				
				if(	score < 0 || score > 5)
				{
					System.out.println("Invalid Score, Not between 0-5. Try again \n");
				}
				else
				{
					pass= false;
				}   
			}
			//passenger's comment
			System.out.println("Please enter you comment, type NA if you want to skip this");
			userInput.nextLine();
			comment = userInput.nextLine();
			
			
			query = "SELECT MAX(r.rID) FROM Ratings r";
			List<List<String>> list = esql.executeQueryAndReturnResult(query);
	
			int result = Integer.parseInt(list.get(0).get(0));
			result++;
			
			//String na ="NA";
			
			if(comment.equals("NA"))
			{
				//System.out.println("TEST1");
				com =false;
			}
			else
			{
				//System.out.println("TEST2");
				com=true;
			}
			
			//two different queries one with a comment and one without a comment
			if(com)
			{
				//System.out.println("TEST3");
				query = "INSERT INTO Ratings( rID, pID, flightNum, score, comment) " +
						"VALUES ('"+result+"', '"+pid+"', '"+flightNum+"', '"+score+"', '"+comment+"')";
			}
			else
			{
				//System.out.println("TEST4");
				query = "INSERT INTO Ratings( rID, pID, flightNum, score) " +
						"VALUES ('"+result+"', '"+pid+"', '"+flightNum+"', '"+score+"')";
			}
						
			esql.executeUpdate(query);
											
		}
		catch (Exception e)
		{
			System.err.println(e.getMessage());
		}
		
		//userInput.close();
	}
	

	public static void InsertOrUpdateRouteForAirline(AirBooking esql){//4
		//Insert a new route for the airline
		Scanner userInput = new Scanner(System.in);
		String query="test";
		String in="00";
		int rowcount=0;
		int airid=0;
		String origin;
		String destination;
		String plane;
		int seats;
		int duration;
		boolean pass =true;
		boolean inner =true;
		String up = "hello";
		String flightnum="00";
		String choose="hoi";
		String change ="chan";
		try
		{
			while(pass)
			{
				System.out.println(" 'Insert' or 'Update' Route For Airline. Press q to quit "); 
				in = userInput.next(); 
				
				//probably another while loop
				//manager will insert the airline, flightNum, origin, destination, seats, and duration
				//we will check each step by step so the user will know which value is incorrect
				if(in.equals("Insert") || in.equals("insert"))
				{
					while(inner)
					{
						System.out.println("Enter flightNum");
						flightnum = userInput.next();
						query = "SELECT f.flightNum " +
								"FROM Flight f " +
								"WHERE f.flightNum = '" + flightnum+"'";
						if(	esql.executeQuery(query) >= 1)
						{
							System.out.println("Flight Number already exists. Try again ");
						}
						else
						{
							inner =false;
						}	
					}
					//inner=true;
					
					//START HERE AGAIN
					System.out.println("Enter airId");
					airid = userInput.nextInt();
					
					userInput.nextLine();
					
					System.out.println("Enter origin");
					origin = userInput.nextLine();
					
					System.out.println("Enter destination");
					destination = userInput.nextLine();
					
					System.out.println("Enter plane");
					plane = userInput.nextLine();
					
					System.out.println("Enter seats");
					seats = userInput.nextInt();
					
					System.out.println("Enter duration");
					duration = userInput.nextInt();
					
					
					query =	"INSERT INTO Flight( airID, flightNum, origin, destination, plane, seats, duration) " +
							"VALUES ('"+airid+"', '"+flightnum+"', '"+origin+"', '"+destination+"', '"+plane+"', '"+seats+"', '"+duration+"')";
					
					esql.executeUpdate(query);
					
					//checking for airId
					// while(inner)
// 					{
// 						System.out.println("Enter airId")
// 						airid = userInput.nextInt();
// 						query = "SELECT f.flightNum " +
// 								"FROM Flight f " +
// 								"WHERE f.airId = " + airid +"  AND f.flightNum = "+flightnum+ " ";
// 						if(	esql.executeQuery(query) >= 1)
// 						{
// 							System.out.println("airId already exists. Try again ");
// 						}
// 						else
// 						{
// 							inner =false;
// 						}			
// 					
// 					}
// 					
					inner =true;
					
					//checking for origin and destination
					// while(inner)
// 					{
// 						System.out.println("Enter origin")
// 						origin = userInput.next();
// 						query = "SELECT f.flightNum " +
// 								"FROM Flight f " +
// 								"WHERE f.origin = " + origin +"  AND f.flightNum = "+flightnum+ " ";
// 						if(	esql.executeQuery(query) >= 1)
// 						{
// 							System.out.println(" already exists. Try again ");
// 						}
// 						else
// 						{
// 							inner =false;
// 						}			
// 					
// 					}
					inner=true;
					// while(inner)
// 					{
// 						System.out.println("Enter destination")
// 						destination = userInput.next();
// 						query = "SELECT f.flightNum " +
// 								"FROM Flight f " +
// 								"WHERE f.origin = " + origin +" ";
// 						if(	esql.executeQuery(query) >= 1)
// 						{
// 							System.out.println("airId already exists. Try again ");
// 						}
// 						else
// 						{
// 							inner =false;
// 						}			
// 					}
// 					
					
					pass = false;
				}
				else if(in.equals("Update") || in.equals("update"))
				{
					//SHOULD WE PROMPT THE USER TO SEE IF THEY ARE DONE UPDATING
					inner=true;
					//checking if the flight number is valid
					while(inner)
					{
						System.out.println("Enter the flight Number to update that route");
						flightnum = userInput.next();
						query = "SELECT f.flightNum " +
								"FROM Flight f " +
								"WHERE f.flightNum = '" + flightnum +"'";
						if(	esql.executeQuery(query) < 1)
						{
							System.out.println("Flight Number does not exists. Try again ");
						}
						else
						{
							inner=false;
						}	
					}
					
					inner =true;
					while(inner)
					{
						//this only allows to make change only one at a time
						System.out.println("What would you like to update");
						System.out.println("Type one of the following choice to update");
						System.out.println("'origin', 'destination', 'plane', 'seats', or 'duration'");
						choose = userInput.next();
						
						if(choose.equals("Origin") || choose.equals("origin"))
						{
							System.out.println("What is the new origin");
							change = userInput.next();
							query = "UPDATE Flight " +
									"SET origin = '" + change +"'"+
									"WHERE flightNum = '"+flightnum+"'";
							
							esql.executeUpdate(query);
							//should this be necessary
							System.out.println("Would you like to update another attribute. Enter Yes or No");
							up = userInput.next();
							
							if(up.equals("Yes") || up.equals("yes"))
							{
								inner = true;
							}
							else
							{
								inner = false;
							}
						}
						else if(choose.equals("Destination") || choose.equals("destination"))
						{
							System.out.println("What is the new Destination");
							change =userInput.next();
							query = "UPDATE Flight " +
									"SET destination = '" + change +"'"+
									"WHERE flightNum = '"+flightnum+"'";
							esql.executeUpdate(query);

							System.out.println("Would you like to update another attribute. Enter Yes or No");
							up = userInput.next();
							
							if(up.equals("Yes") || up.equals("yes"))
							{
								inner = true;
							}
							else
							{
								inner =false;
							}
						}
						else if(choose.equals("Plane") || choose.equals("plane"))
						{
							System.out.println("What is the new Plane");
							userInput.nextLine();
							change =userInput.nextLine();
							query = "UPDATE Flight " +
									"SET plane = '" + change + "'"+
									"WHERE flightNum = '"+flightnum+"'";
							System.out.println("Would you like to update another attribute. Enter Yes or No");
							up = userInput.next();
							esql.executeUpdate(query);

							if(up.equals("Yes") || up.equals("yes"))
							{
								inner = true;
							}
							else
							{
								inner =false;
							}
						}
						else if(choose.equals("seats") || choose.equals("Seats"))
						{
							System.out.println("What is the new number of seats");
							change =userInput.next();
							query = "UPDATE Flight " +
									"SET seats = '" + change + "'"+
									"WHERE flightNum = '"+flightnum+"'";
							System.out.println("Would you like to update another attribute. Enter Yes or No");
							up = userInput.next();
							esql.executeUpdate(query);
							
							if(up.equals("Yes") || up.equals("yes"))
							{
								inner = true;
							}
							else
							{
								inner =false;
							}
						}
						else if(choose.equals("Duration") || choose.equals("duration"))
						{
							System.out.println("What is the new Duration of the trip");
							change =userInput.next();
							query = "UPDATE Flight " +
									"SET duration = '" + change + "'"+
									"WHERE flightNum = '"+flightnum+"'";
							System.out.println("Would you like to update another attribute. Enter Yes or No");
							up = userInput.next();
							esql.executeUpdate(query);
							
							if(up.equals("Yes") || up.equals("yes"))
							{
								inner = true;
							}
							else
							{
								inner =false;
							}
						}
						else
						{
							System.out.println("invalid option");
						}
					
					}
					
					pass =false;
				}
				else if(in.equals("q"))
				{
					pass =false;
				}
				else
				{
					System.out.print("Invalid input");
				}
					
			}
			
		}
		catch(Exception e)
		{
		
		}
	}
	
	public static void ListAvailableFlightsBetweenOriginAndDestination(AirBooking esql) throws Exception{//5
		//List all flights between origin and distination (i.e. flightNum,origin,destination,plane,duration)
		String Origin;
		String Destination;
		String query = "he";
		Scanner userInput = new Scanner(System.in);
		boolean pass = true;
		try
		{
		while (pass)
		{
		System.out.println("Please Enter the Origin of the flight: ");
		Origin = userInput.nextLine();
		
		System.out.println("Please Enter your Destination: ");
		Destination = userInput.nextLine();
		
		query = "SELECT f.flightNum, f.origin, f.destination, f.plane, f.duration " +
				"FROM Flight f " +
				"WHERE f.origin = '" + Origin+ "' AND f.destination = '" + Destination+"'";
		//want to check if there is a flight from origin to destination
		if(	esql.executeQuery(query) < 1)
		{
			System.out.println("There are no flights offered: ");
		}
		else
		{
			pass = false;
		}
		}
		
		
		esql.executeQueryAndPrintResult(query);
		}
				
		catch (Exception e)
		{
			System.err.println(e.getMessage());
		} 
	}
	
	public static void ListMostPopularDestinations(AirBooking esql){//6
		//Print the k most popular destinations based on the number of flights offered to them (i.e. destination, choices)
		String query;
		int k=0;
		Scanner userInput = new Scanner(System.in);
		try
		{
			System.out.print("How many popular destination would you like to see (enter a number)\n");
			k = userInput.nextInt();
			query = "SELECT DISTINCT f.destination, COUNT(f.destination) as choices " +
					"FROM Flight f " +
					"GROUP BY f.destination " +
					"ORDER BY choices DESC " +
					"LIMIT " +k+ " "; 
			esql.executeQueryAndPrintResult(query);
		}
		catch (Exception e)
		{
			System.err.println(e.getMessage());
		}
		
	}
	
//probably need to check the number of ratings there are to make sure we do not have some type of overflow
	//there is no overflow, if you input 100000 the program does not crash.
	//but something still feels off.
	public static void ListHighestRatedRoutes(AirBooking esql){//7
		//List the k highest rated Routes (i.e. Airline Name, flightNum, Avg_Score)
		String query;
		int k=0;
		Scanner userInput = new Scanner(System.in);
		try
		{
			System.out.println("How many top rated routes will you like to see (enter a number)\n");
			k = userInput.nextInt();
			query = "SELECT a.name, f.flightNum, f.origin, f.destination, f.plane, AVG(r.score) as Avg_Score "+
					"FROM Airline a, Flight f, Ratings r " +
					"WHERE r.flightNum = f.flightNum AND f.airId = a.airId " +
					"GROUP BY a.name, f.flightNum, f.origin, f.destination, f.plane "+
					"ORDER By Avg_Score DESC "+
					"LIMIT " +k+ " ";
					
					
			// query = "SELECT TOP 4 * "+
// 					"FROM( "+
// 					"SELECT a.name, f.flightNum, f.origin, f.destination, f.plane, AVG(r.score) as ascore "+
// 					"FROM Airline a, Flight f, Ratings r " +
// 					"WHERE r.flightNum = f.flightNum AND f.airId = a.airId " +
// 					"GROUP BY a.name, f.flightNum, f.origin, f.destination, f.plane "+
// 					"ORDER By ascore)";
							
					
			esql.executeQueryAndPrintResult(query);			
		}
		catch (Exception e)
		{
			System.err.println(e.getMessage());
		}
	}
	
public static void ListFlightFromOriginToDestinationInOrderOfDuration(AirBooking esql){//8
        //List flight to destination in order of duration (i.e. Airline name, flightNum, origin, destination, duration, plane)
        String query;
        int k=0;
        String origin="hello";
        String destination;
        Scanner userInput = new Scanner(System.in);
        try
        {
            System.out.println("How Many flights would you like to see");
            k = userInput.nextInt();
            System.out.println("type in the origin");
            userInput.nextLine();
            origin = userInput.nextLine();
           
            System.out.println("type in the destination");
            //userInput.nextLine();
            //userInput.nextLine();
            destination = userInput.nextLine();
           
            System.out.println(origin);
            System.out.println(destination);
                       
            query = "SELECT a.name, f.flightNum, f.origin, f.destination, f.duration, f.plane " +
                    "FROM Airline a, Flight f " +
                    "WHERE a.airId = f.airId  AND f.origin = '"+ origin+ "' AND f.destination = '" +destination+ "' " +
                    "ORDER By f.duration " +
                    "LIMIT " +k+ " ";  
            esql.executeQueryAndPrintResult(query);    
        }
        catch (Exception e)
        {
            System.err.println(e.getMessage());
        }
       
       
    }
	//Implement to check the given date
	public static void FindNumberOfAvailableSeatsForFlight(AirBooking esql){//9
		//
		String flightNum = "he";
		String query = "he";
		Scanner userInput = new Scanner(System.in);
		boolean pass = true;
	try{
		while(pass)
		{
			System.out.println("Please Enter your Flight Number: ");
			flightNum = userInput.next();
			
			query = "SELECT f.flightNum " +
					"FROM Flight f " +
					"WHERE f.flightNum = '" + flightNum+ "'";
			if( esql.executeQuery(query) < 1)
			{
				System.out.println("FlightNumber Does Not Exist: ");
			}
			else
			{
				pass = false;
			}
			
			
		}
		
		System.out.println("Please Enter Departure Date");
		String departure = userInput.next();
		
		query =	"SELECT b.flightNum " +
				"FROM Booking b " +
				"WHERE b.flightNum = '" + flightNum+ "' AND b.departure = '"+ departure+"'";;
		
			
		if(	esql.executeQuery(query) < 1)
		{
			System.out.println("~No one has booked this flight, all seats availabe for: ");
			query =	"SELECT f.flightNum, f.origin, f.destination, '" + departure+ "' AS Departure, f.seats AS TotalSeats, 0 AS BookedSeats, f.seats AS RemainingSeats " +
					"FROM Flight f " +
					"WHERE f.flightNum = '" + flightNum+ "'";
			esql.executeQueryAndPrintResult(query);
		}
		
		query = "SELECT f.flightNum, f.origin, f.destination, b.departure, COUNT(b.pID) AS BookedSeats, f.seats, (f.seats - COUNT(b.pid)) AS RemainingSeats " +
		"FROM Booking b, Flight f " +
		"WHERE b.flightNum = '" + flightNum+ "' AND f.flightNum = '" + flightNum+"' AND b.departure = '" + departure+"'" +
		"GROUP BY f.flightNum, b.departure ";
		
		esql.executeQueryAndPrintResult(query);
	}
	catch (Exception e){
			System.err.println(e.getMessage());
	} 	
	}
	
}
