
import java.util.List;
import java.util.Scanner;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.LinkedList;

/**
* [0] Implement 
* 	A) a method to read in commands from a file 
*	B) a way to check if program is respond to commands correctly 
* 	C) a way to get results from the test and see where program failed (if ever) 
* [1] See where remove methods are at
* [2] COMMENT COMMENT COMMENT
* [3] Clean up bracket and database classes
* [3] Touch up commands and style of console print outs
*/
public class MarchMadness 
{
	private static Scanner scanner;
	private static InputStream is;
	private static BufferedReader reader;
	private static MarchMadnessDB db;
	//private static boolean unsavedChanges;//Needs to be set to true every time something is changed, prompt when quitting
	private static String teamFile = "/teamFile.txt";
	private static String playerFile = "/playerFile.txt";
	private static String configFile = "/config.txt";
	private static String logFile;
	private static String instructionFile;
	private static PrintWriter logger;
	private static DateFormat dateFormat;
	private static Calendar calendar;
	private static PrintWriter writer;
	
	private final static String addCommands = 
			"\t[score] <Team Name> <Score> = add a score\n"+
			"\t[player] = add a player\n"+
			"\t[playerinfo] = add player's chosen winner and/or pickfour teams\n"+
			"\t[team] = add a team\n"+
			"\t[teams] = add in multiple teams at once in region and seed order\n"+
			"\t[pick4team] = add pick four team to a player\n"+
			"\t[chosenwinner] = add chosen winner to a player\n";
	private final static String viewCommands = 
			"\t[player] <Player Name> = view a player's information\n"+
			"\t[team] <Team Name> = view a team's information\n"+
			"\t[pick4leaderboard] = view the pick four leaderboard\n"+
			"\t[playerleaderboard] = view the player leaderboard (players with team alive)\n"+
			"\t[winners] = view winners of the various mini-games\n"+
			"\t[missing] = view what information missing from the database (teams, players, and player information)\n";
	private final static String listCommands = 
			"\t[players]  = prints a list of players\n"+
			"\t[teams]  = prints a list of teams\n";
	private final static String editCommands = 
			"\t[scores] = edit the scores for a specific team\n"+
			"\t[team] = edit a team's information\n"+
			"\t[player] = edit a player's information\n";
	private final static String removeCommands = 
			"\t[player] = remove player\n"+
			"\t[team] = remove team\n"+
			"\t[scores] = remove ALL scores\n"+
			"\t[assigned] = remove every player's assigned teams\n"+
			"\t[pick4] = remove every player's pick four teams\n";
	
	private final static String menuBar = "===================================";

	
	
	public static void main(String[] args) 
	{		
		if(args.length == 0) {
			//teamFile = "teamFile.txt";
			//playerFile = "playerFile.txt";
			//configFile = "config.txt";
			logFile = "log.txt";
		} else {
			//teamFile = args[0];
			//playerFile = args[1];
			//configFile = args[2];
			logFile = args[3];
			if(args.length == 5)
				instructionFile = args[4];
		}
		
		if(!initialize())
			;
		
		processUserCommands();
		
		System.exit(0);
		
	
	}
	
	
	private static boolean initialize() {
		db = new MarchMadnessDB();
		try {
			return readConfigFile() && readTeamFile() && readPlayerFile();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e1) {
			e1.printStackTrace();
			return false;
		} 
	}
	
	private static boolean readTeamFile() throws FileNotFoundException, IOException {
		//scanner = new Scanner(new File(teamFile));
		is = MarchMadness.class.getResourceAsStream(teamFile);
		reader = new BufferedReader(new InputStreamReader(is));
		logger = new PrintWriter(new FileWriter(logFile,true));
		dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		calendar = Calendar.getInstance();
		
		String line = reader.readLine();
		String region;
		while(line != null && line.contains("Region:")) {
			region = line.substring(line.indexOf(":")+1, line.length()).trim();
			line = reader.readLine();
			
			while(line != null && !line.contains("Region:")) {
				int seed;
				String name = "";
				List<Integer> scores = new LinkedList<Integer>();
				String[] pieces = line.split(" ");
				seed = Integer.parseInt(pieces[0]);
				
				for(int i = 1; i < pieces.length-1; i++) {
					name += pieces[i];
					if(i+1 < pieces.length-1)
						name+=" ";
				}
				name = formatTeam(name);
				if(pieces[pieces.length-1].length() > 2) {
					pieces[pieces.length-1] = pieces[pieces.length-1].substring(1, pieces[pieces.length-1].length()-1);//remove the end brackets
					pieces = pieces[pieces.length-1].split(",");//fill pieces with scores
					for(int i = 0; i < pieces.length; i++)
					{
						scores.add(Integer.parseInt(pieces[i]));
					}
				}
				try {
					if(db.addTeam(name,seed,region,scores) != 0) {
						logger.println(dateFormat.format(calendar.getTime())+":TEAM ALREADY EXISTS_Could not ADD TEAM: \""+seed+"\" \""+name+"\" in the \""+region+"\"");
						logger.close();
					}
				} catch(IllegalArgumentException e) {
					logger.println(dateFormat.format(calendar.getTime())+":ILLEGAL ARG_Could not ADD TEAM: \""+seed+"\" \""+name+"\" in the \""+region+"\"");
					logger.close();
				}
				
				line = reader.readLine();
			}
		}
		reader.close();
		return true;
	}

	private static boolean readPlayerFile() throws FileNotFoundException, IOException {
		//scanner = new Scanner(new File(playerFile));
		is = MarchMadness.class.getResourceAsStream(playerFile);
		reader = new BufferedReader(new InputStreamReader(is));
		logger = new PrintWriter(new FileWriter(logFile,true));
		dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		calendar = Calendar.getInstance();
		
		String line = reader.readLine();
		while(line != null) {
			if(!line.contains("Player:"))
				line = reader.readLine();
			String name = "";
			PlayerInterface player;
			while(line != null && line.contains("Player:")) {
				line = line.substring(line.indexOf(":")+1, line.length()).trim(); //<Name> <Spots Purhcased>
				String[] pieces = line.split(" ");
				int spotsPurchased = Integer.parseInt(pieces[pieces.length-1]);
				
				/** Reconstructs name */
				name = "";
				for(int i = 0; i < pieces.length-1; i++) {
					name += pieces[i];
					if(i+1 < pieces.length-1)
						name+=" ";
				}
				/** Attempts to add player to database */
				try {
					if(db.addPlayer(name, spotsPurchased) != 0) {
						logger.println(dateFormat.format(calendar.getTime())+
								":PLAYER ALREADY EXISTS_Could not ADD PLAYER: "
								+ "\""+name+"\", "+spotsPurchased+" sp");
						logger.close();
					}
				} catch(IllegalArgumentException e) {
					logger.println(dateFormat.format(calendar.getTime())+
							":ILLEGAL ARG_Could not ADD PLAYER: "
							+ "\""+name+"\", "+spotsPurchased+" sp");
					logger.close();
				}
				
				line = reader.readLine();//NEXT PLAYER OR "#CHOSEN TEAM"
			}
			/** Find player to add data to */
			player = db.findPlayer(name);
			if(line != null && player != null) {
			/** Reads in player's chosen team */
				if(line.contains("Chosen Team:")) {
					line = reader.readLine();
					if(line != null && !line.contains("Owned Teams:")) {
						try{
						db.addChosenWinner(player, db.findTeam(formatTeam(line)));
						}catch (IllegalArgumentException e2){}
						
						line = reader.readLine();
					}	
				}
				while(line != null && line.contains("Owned Teams:")) {	
					line = reader.readLine();
					while(line != null && !line.contains("Pick Four Teams:")) {
						try {
							db.addOwnedTeam(player, db.findTeam(formatTeam(line)));
						} catch (IllegalArgumentException e2){}
						
						line = reader.readLine();
					}
				}
				while(line != null && line.contains("Pick Four Teams:")) {
					line = reader.readLine();
					while(line != null && !line.contains("Player:")) {
						try {
							db.addPickFourTeam(player, db.findTeam(formatTeam(line)));
						} catch (IllegalArgumentException e2){}
						line = reader.readLine();
					}
				}
			}
		}
		reader.close();
		return true;
	}
	
	private static boolean readConfigFile() throws FileNotFoundException, IOException {
		//scanner = new Scanner(new File(configFile));
		is = MarchMadness.class.getResourceAsStream(configFile);
		reader = new BufferedReader(
				new InputStreamReader(
						is));
		
		String line = reader.readLine();
		while(line != null) {
			if(line.contains("#Pricing:")) {
				line = reader.readLine();
				while(line != null && !line.contains("#")) {
					if(line.contains("Spots Pricing:")) {
						List<Integer> pricing = new LinkedList<Integer>();
						line = line.substring(line.indexOf(":")+1).trim();
						line = line.replace("[", "").replace("]", "");
						String[] pieces = line.split(",");
						for(int i = 0; i < pieces.length; i++) {
							try {
								pricing.add(Integer.parseInt(pieces[i].trim()));
							} catch(NumberFormatException numExcep) {
								return false;
							}
						}
						db.setPricing(pricing);
						line = reader.readLine();
					}
				}
			}
			if(line != null && line.contains("#Prizes:")) {
				line = reader.readLine();
				double prize;
				List<Double> pickFourPrizes = new LinkedList<Double>();
				while(line != null && !line.contains("#")) {
					if(line.contains("Blowout Loss Prize:")) {
						line = line.substring(line.indexOf(":")+1).trim();
						try {
							prize = Double.parseDouble(line);
							db.setPrizeMoney(PrizeType.FIRST_ROUND_BLOWOUT,prize);
						}
						catch(NumberFormatException numExcep){}
					}
					else if(line.contains("Cinderella Prize:")) {
						line = line.substring(line.indexOf(":")+1).trim();
						try {
							prize = Double.parseDouble(line);
							db.setPrizeMoney(PrizeType.CINDERELLA_TEAM,prize);
						}
						catch(NumberFormatException numExcep){}
					}
					else if(line.contains("First Overtime Loss Prize:")) {
						line = line.substring(line.indexOf(":")+1).trim();
						try {
							prize = Double.parseDouble(line);
							db.setPrizeMoney(PrizeType.FIRST_OVERTIME_LOSS,prize);
						}
						catch(NumberFormatException numExcep){}
					}
					else if(line.contains("Chosen Winner Prize:")) {
						line = line.substring(line.indexOf(":")+1).trim();
						try
						{
							prize = Double.parseDouble(line);
							db.setPrizeMoney(PrizeType.CHOSEN_WINNER,prize);
						}
						catch(NumberFormatException numExcep){}
					}
					else if(line.contains("Won First Game Prize:")) {
						line = line.substring(line.indexOf(":")+1).trim();
						try
						{
							prize = Double.parseDouble(line);
							db.setPrizeMoney(PrizeType.WON_FIRST_GAME,prize);
						}
						catch(NumberFormatException numExcep){}
					}
					else if(line.contains("Final Four Team:")) {
						line = line.substring(line.indexOf(":")+1).trim();
						try {
							prize = Double.parseDouble(line);
							db.setPrizeMoney(PrizeType.FINAL_FOUR_TEAM,prize);
						}
						catch(NumberFormatException numExcep){}
					}
					else if(line.contains("Champion Team:")) {
						line = line.substring(line.indexOf(":")+1).trim();
						try
						{
							prize = Double.parseDouble(line);
							db.setPrizeMoney(PrizeType.CHAMPION_TEAM,prize);
						}
						catch(NumberFormatException numExcep){}
					}
					else if(line.contains("Pick Four Prize:")) {
						line = line.substring(line.indexOf(":")+1).replace("[","").replace("]", "").trim();
						String[] pieces = line.split(",");
						for(int i = 0; i < pieces.length; i++) {
							try{pickFourPrizes.add(Double.parseDouble(pieces[i]));}
							catch(NumberFormatException numExcep){}
						}
						db.setPrizeMoney(PrizeType.PICK_FOUR_CHAMPION,pickFourPrizes.remove(0));
						db.setPrizeMoney(PrizeType.PICK_FOUR_RUNNERUP,pickFourPrizes.remove(0));
					}
					
					line = reader.readLine();
				}
			}
			if(line.contains("#Region Matchups:")) {
				line = reader.readLine();
				Region[] regionMatchups = new Region[db.getRegionMatchups().length];
				int index = 0;
				while(line != null && !line.contains("#")) {
					String[] pieces = line.split("vs");
					int i = 0;
					while(index < regionMatchups.length && i < pieces.length) {
						regionMatchups[index] = Region.determineRegion(pieces[i]);
						i++; index++;
					}
					line = reader.readLine();
				}
				db.setRegionMatchups(regionMatchups);
			}
			if(line != null && line.contains("#Other:")) {
				line = reader.readLine();
				int size;
				while(line != null && !line.contains("#")) {
					if(line.contains("Leaderboard Size:")) {
						line = line.substring(line.indexOf(":")+1).trim();
						try {
							size = Integer.parseInt(line);
							db.setLeaderboardSize(size);
						}
						catch(NumberFormatException numExcep){}
					}
					line = reader.readLine();
				}
			}
			line = reader.readLine();
		}
		
		reader.close();
		return true;
	}
	
	/**
	 * Not fully tested?
	 *
	 * @return
	 */
	private static boolean saveToFile()
	{
		/** Write out team information to team file */
		try {
			File output = new File(teamFile);
			OutputStream os = new FileOutputStream(output);
			writer = new PrintWriter(os);
		} catch (FileNotFoundException e) {e.printStackTrace();return false;} 
		  //catch (URISyntaxException e2) {e2.printStackTrace(); return false;}
		Iterator<TeamInterface> itr = db.getTeams().iterator();
		Region r  = null;
		while(itr.hasNext()) {
			TeamInterface team = itr.next();
			if(r == null || r != team.getRegion()) {
				r = team.getRegion();
				writer.println("#Region: " + team.getRegion());
			}
			writer.print(team.getSeed() + " " + team.getName() + " ");
			String scores = db.getScores(team).toString().replaceAll(" ","");
			writer.println(scores);
		}
		writer.close();
		
		/** Write out player information to player file */
		try {
			File output = new File(playerFile);
                        OutputStream os = new FileOutputStream(output);
                        writer = new PrintWriter(os);
		} catch (FileNotFoundException e) {e.printStackTrace();return false;} 
		  //catch (URISyntaxException e2) {e2.printStackTrace(); return false;}
		
		Iterator<PlayerInterface> itr2 = db.getPlayers().iterator();
		while(itr2.hasNext())
		{
			PlayerInterface player = itr2.next();
			writer.println("#Player: "+player.getName() + " "+player.getNumSpots());
			writer.println("\t#Chosen Team:");
			if(player.getChosenWinner() != null)
				writer.println("\t\t"+player.getChosenWinner().getName());
			writer.println("\t#Owned Teams:");
			itr = player.getTeams().iterator();
			while(itr.hasNext())
				writer.println("\t\t"+itr.next().getName());
			writer.println("\t#Pick Four Teams:");
			itr = player.getPickFourTeams().iterator();
			while(itr.hasNext())
				writer.println("\t\t"+itr.next().getName());
			writer.flush();
		}
		writer.close();
		
		/** Write out rules and settings to configFile */
		try {
			File output = new File(configFile);
                        OutputStream os = new FileOutputStream(output);
                        writer = new PrintWriter(os);
		} catch (FileNotFoundException e) {e.printStackTrace();return false;} 
		  //catch (URISyntaxException e2) {e2.printStackTrace(); return false;}
		
		writer.println("#Pricing:");
		writer.println("Spots Pricing: "+db.getPricing());
		writer.println("#Prizes:");
		writer.println("Blowout Loss Prize: "+db.getPrizeMoney(PrizeType.FIRST_ROUND_BLOWOUT));
		writer.println("Cinderella Prize: "+db.getPrizeMoney(PrizeType.CINDERELLA_TEAM));
		writer.println("First Overtime Loss Prize: "+db.getPrizeMoney(PrizeType.FIRST_OVERTIME_LOSS));
		writer.println("Pick Four Prize: ["+db.getPrizeMoney(PrizeType.PICK_FOUR_CHAMPION)+", "+
				db.getPrizeMoney(PrizeType.PICK_FOUR_RUNNERUP)+"]");
		writer.println("Chosen Winner Prize: "+db.getPrizeMoney(PrizeType.CHOSEN_WINNER));
		writer.println("Won First Game Prize: "+db.getPrizeMoney(PrizeType.WON_FIRST_GAME));
		writer.println("Finla Four Team: "+db.getPrizeMoney(PrizeType.FINAL_FOUR_TEAM));
		writer.println("Champion Team: "+db.getPrizeMoney(PrizeType.CHAMPION_TEAM));
		writer.println("#Region Matchups:");
		Region[] regionMatchups = db.getRegionMatchups();
		for(int i = 0; i < regionMatchups.length; i++) {
			writer.print(regionMatchups[i]+" vs ");
			i++;
			if(i < regionMatchups.length)
				writer.print(regionMatchups[i]);
			writer.println();
		}
		writer.println("#Other:");
		writer.println("Leaderboard Size: "+db.getLeaderboardSize());
		
		writer.close();
		
		return true;
	}
	
	private static void processUserCommands() {
		db.update();
		if(instructionFile == null)
			scanner = new Scanner(System.in);
		else {
			try {
				scanner = new Scanner(new File(instructionFile));
			} catch (IOException e2) {return;}
		}
		String command = "";
		do {
			System.out.print("\nPlease Enter a command ([help]):");
			command = scanner.next().toLowerCase().trim();
			/**Only when instructions are fed in will it print the instructions given*/
			if(instructionFile != null)
				System.out.print(command+" ");
			switch(command) {	
				case "add"://Add commands
					handleAddCommand();
					db.update();
					break;
				case "view"://View commands
					handleViewCommand();
					break;
				case "list":
					handleListCommand();
					break;
				case "edit":
					handleEditCommand();
					db.update();
					break;
				case "remove":
					handleRemoveCommand();
					db.update();
					break;
				case "assign":
					if(scanner.nextLine().trim().toLowerCase().equals("teams")) {
						assignTeams();
						db.update();
					}
					else
						System.out.println("\nUnrecognized Command");
					break;
				case "reset":
					scanner.nextLine();//clear scanner
					System.out.printf("%nThis will reset all team and player data but teams and players will not be removed.%n"
							+ "\tDo you wish to continue? (y/n) ");
					if(scanner.nextLine().trim().equalsIgnoreCase("y")) {
						resetAll();
						System.out.printf("Teams and Players have been reset.%n");
					}
					break;
				case "print":
					System.out.println(db);
					break;
				case "save":
					db.update();
					if(saveToFile())
						System.out.println("Saved successfully");
					else
						System.out.println("Error. Data was not saved");
					break;
				case "quit":
					System.out.println("Quit");
					break;
				case "help":
					System.out.println("\nBracket Information/Commands\n"+
							"[view]\n"+ viewCommands +
							"[add]\n"+ addCommands +
							"[list]\n"+ listCommands +
							"[edit]\n"+ editCommands +
							"[remove]\n"+ removeCommands +
							"[assign teams] = assign each player their teams \n\t(will prompt "
							+ "for \"y/n\" confirmation)\n"+
							"[quit]\n");
					break;
				default:
					String line = command+scanner.nextLine();
					if(line.contains("paid")) {
						line = line.replaceAll("paid", "");
						PlayerInterface player = db.findPlayer(line);
						if(player != null) {
							player.paid();
							db.update();
							System.out.println(player.getName()+"'s balance has been updated");
							break;
						}
					}
					System.out.println("Unrecognized Command!");
					break;
			}
			if(instructionFile != null && !scanner.hasNext()) {//switch to input after instruction sheet
				scanner = new Scanner(System.in);
			}
		} while (!command.equalsIgnoreCase("quit"));
		scanner.close();
	}
	
	private static void handleAddCommand() {
		String command = scanner.next().toLowerCase().trim();
		/**Only when instructions are fed in will it print the instructions given*/
		if(instructionFile != null)
			System.out.println(command);
		switch(command)
		{
			case "score":
				addScore();
				break;
			case "player":
				addPlayer();
				break;
			case "team":
				addTeam();
				break;
			case "teams":
				addTeams();
				break;
			case "pick4team":
				addPickFourTeam();
				break;
			case "chosenwinner":
				addChosenWinner();
				break;
			case "playerinfo":
				addPlayerInfo();
				break;
			default:
				scanner.nextLine();
				System.out.println("\""+command+"\" is not a valid [add] command. Valid [add] commands:\n"+addCommands);
				break;
		}
	}
	
	private static void addScore() {
				
		TeamInterface team = userEnterTeam();
		if(team == null)
			return;
		
		System.out.println("-Enter the score (0 if unknown)");
		int score = -1;
		while(score < 0) {
			System.out.print("\t"+team+": ");
			String line = scanner.nextLine();
			if(line.trim().equalsIgnoreCase("quit"))
				return;
			try {
				score = Integer.parseInt(line);
			} catch (NumberFormatException e) {
				System.out.println("\t *\""+line+"\" is not a number");
			}
			db.addScore(team,score);
			
			//Asks for opponent score if there is one
			List<TeamInterface> opponents = db.getOpponents(team);
			if(!opponents.isEmpty()) {
				TeamInterface opponent = opponents.get(opponents.size()-1);
				if(db.getScores(team).size() != db.getScores(opponent).size())
					return;
				System.out.print("\t"+opponent+": ");
				String scoreStr = scanner.nextLine();
				if(scoreStr.length() == 0 || scoreStr.equalsIgnoreCase("quit"))
					return;
				try {
					score = Integer.parseInt(scoreStr);
				} catch (NumberFormatException e) {
					System.out.println("Score not added. \""+scoreStr+"\" is not a number. Error Code: 2");
					return;
				}
				db.addScore(opponent, score);
			}
		}
	}
	
	private static void addPlayer() {
		scanner.nextLine();//Clear out line
		
		String name = "";
		int spotsPurchased = 0;
		
		System.out.println("Enter each line of information and press enter");
		
		while(name.equals("")) {
			System.out.print("\tName: ");
			name = scanner.nextLine().trim();
			if(name.toLowerCase().equals("quit"))
				return;
			if(name.equals(""))
				System.out.println("\t *INVALID NAME");
			else if(db.findPlayer(name) != null) {
				System.out.println("\t *USER ALREADY EXISTS ");
				name = "";
			}
		}
		
		while(spotsPurchased < 1) {
			System.out.print("\tSpots Purchased ("+db.spotsRemaining()+" remain): ");
			String numStr = scanner.nextLine().trim();
			if(numStr.toLowerCase().equals("quit"))
				return;
			try {
				spotsPurchased = Integer.parseInt(numStr);
				if(spotsPurchased < 1 || spotsPurchased > db.spotsRemaining() || spotsPurchased >= db.getPricing().size()) {
					spotsPurchased = 0;
					throw new Exception();
				}
			} catch(NumberFormatException e) {System.out.println("\t *INVALID NUMBER");}
			catch(Exception ex) {System.out.println("\t *INVALID SPOTS PURCHASED");}
		}
		
		int code = db.addPlayer(name, spotsPurchased);
		if(code == 1 || code == 2 || code == 4)
			System.out.println("Illegal Information. Error Code: " + code);
		else if(code == 3)
			System.out.println("Not enough availible spots to enter this player. Error Code: " + code);
	}
	
	private static void addTeam() {
		scanner.nextLine();//Clear out line
		
		int seed = 0;
		String name = "";
		Region region = null;
		
		System.out.println("Enter each line of information and press enter");
		
		while(seed == 0) {
			System.out.print("\tSeed: ");
			try {
				seed = Integer.parseInt(scanner.nextLine());
				if(seed < 1 || seed > 16) {//Change to final variables
					seed = 0;
					throw new Exception();
				}
			} catch(NumberFormatException e) {System.out.println("\t *INVALID SEED");}
			catch(Exception ex) {System.out.println("\t *INVALID SEED (Seed should be in range of 1-16)");}
		}
		
		while(name.equals("")) {
			System.out.print("\tName: ");
			name = scanner.nextLine();
			if(name.equals(""))
				System.out.println("\t *INVALID NAME");
		}
		
		while(region == null) {
			System.out.print("\tRegion: ");
			region = Region.determineRegion(scanner.nextLine());
			if(region == null)
				System.out.println("\t* INVALID REGION (Valid Regions are "+Region.EAST+" "+Region.MIDWEST+" "+Region.SOUTH
					+" "+Region.WEST+")");
		}
		
		int code = db.addTeam(name, seed, region);
		if(code == 1)
			System.out.println("Illegal team data entered. Error: 1");
		else if(code == 2)
			System.out.println("Error team name is a duplicate. Error 2");
		else if(code == 3)
			System.out.println("Error team in that location already exists. Error 3");
	}
	
	private static void addTeams() {
		scanner.nextLine(); //clear line
		
		if(db.bracketInitialized()) {
			System.out.printf("Bracket is already full%n");
			return;
		}
		
		//Creates an organized list of teams in order of seed in each region
		TeamInterface[] organizedTeams = new TeamInterface[65];
		int seed = 1;
		int regionNum = 0;
		Region[] regions = Region.values();
		//Organizes list
		for(TeamInterface team: db.getTeams()) {
			for(int i = 0; i < regions.length; i++) {
				if(regions[i].equals(team.getRegion()))
					organizedTeams[team.getSeed() + i*16] = team;
			}
			seed++;
			if(seed > 16)
				seed = 1;
		}
		
		System.out.println(regions[regionNum]);
		for(int i = 1; i < 65; i++) {
			if(organizedTeams[i] == null) {
				TeamInterface team = null;
				while(team == null) {
					seed = (i-regionNum*16);
					System.out.print(" #"+seed+" ");
				
					String line = scanner.nextLine().trim();
					if(line.trim().equalsIgnoreCase("quit"))
						return;
					team = db.findTeam(line);
					if(team == null) {
						db.addTeam(line,seed,regions[regionNum]);
						break;
					}
					else {
						System.out.println("  *TEAM ALREADY EXISTS");
						team = null;
					}
				}
			}
			else
				System.out.println(" "+organizedTeams[i]);
			
			if(i % 16 == 0) {
				regionNum = i/16;
				if(regionNum < regions.length)
					System.out.println(regions[regionNum]);
			}
		}
	}
	
	private static void addPickFourTeam()
	{
		scanner.nextLine();//Clear the line
		
		System.out.println("-Enter the name of the player");
		
		PlayerInterface player = userEnterPlayer();
		if(player == null)
			return;
		
		List<TeamInterface> teams = player.getPickFourTeams();
		Iterator<TeamInterface> itr = teams.iterator();
		System.out.println("\n-Enter Team Name(s) ("+(4-teams.size()+"/4 availible)"));
		
		int num;//Numbering for list of pick four teams
		for(num = 1; num <= 4 && itr.hasNext(); num++) {
			TeamInterface temp = itr.next();
			System.out.println("\t"+num+") "+temp);
		}
		while(player.getPickFourTeams().size() < 4) {
			TeamInterface team = null;
			while(team == null) {
				System.out.print("\t"+num+") ");
				String line = scanner.nextLine();
				if(line.trim().equalsIgnoreCase("quit"))
					return;
				team = db.findTeam(line);
				if(team != null) {
					player.addPickFourTeam(team);
					num++;
				}
				else
					System.out.println("\t *TEAM NOT FOUND");
			}
		}
	}
	
	private static void addChosenWinner() {				
		PlayerInterface player = userEnterPlayer();
		if(player == null)
			return;
		
		TeamInterface chosenWinner = player.getChosenWinner();
		if(chosenWinner != null) {
			System.out.println("\t"+player+" has already selected "+chosenWinner+" to win it all.");
			return;
		}
		
		System.out.println("-Enter Team Name (or quit to exit)");
		chosenWinner = userEnterTeam();
		if(chosenWinner == null)
			return;
		
		player.addChosenTeam(chosenWinner);
	}
	
	private static void addPlayerInfo()
	{
		PlayerInterface player = userEnterPlayer();
		if(player == null)
			return;
		
		if(player.getChosenWinner() == null) {
			System.out.print("Chosen Winner: ");
			String line = scanner.nextLine().trim();
			if(line.equalsIgnoreCase("quit"))
				return;
			TeamInterface team = db.findTeam(line);
			if(team != null)
				player.addChosenTeam(team);
			else
				System.out.println("NOT FOUND");
		}
		
		Iterator<TeamInterface> itr = player.getPickFourTeams().iterator();
		System.out.println("\nPick Four Teams:");
		int i = 1;
		while(itr.hasNext()) {
			System.out.println(i+") "+itr.next().getName());
			i++;
		}
		while(i <= 4) {//Number of pick-four teams
			System.out.print(i+") ");
			String line = scanner.nextLine().trim();
			if(line.equalsIgnoreCase("quit"))
				return;
			TeamInterface team = db.findTeam(line);
			if(team != null) {
				player.addPickFourTeam(team);
				i++;
			} else
				System.out.println("NOT FOUND");
		}
		
	}
	
	private static void handleViewCommand() {
		String command = "";
		
		command = scanner.next().toLowerCase().trim();
		/**Only when instructions are fed in will it print the instructions given*/
		if(instructionFile != null)
			System.out.println(command);
		switch(command) {
			case "team"://View team
				TeamInterface team = userEnterTeam();
				viewTeam(team);
				break;
			case "player"://View player
				PlayerInterface player = userEnterPlayer();
				viewPlayer(player);
				break;
			case "pick4leaderboard"://View pick four player leaderboard
				viewPickFourLeaderboard();
				break;
			case "playerleaderboard"://View player leaderboard
				viewPlayerLeaderboard();
				break;
			case "missing":
				viewMissing();
				break;
			case "winners":
				viewWinners();
				break;
			case "prizes":
				viewPrizes();
				break;
			case "settings":
				viewSettings();
				break;
			default:
				if(scanner.hasNextLine()) {
					command += scanner.nextLine();
					team = db.findTeam(command);
					if(team != null) {
						viewTeam(team);
						return;
					}
					player = db.findPlayer(command);
					if(player != null) {
						viewPlayer(player);
						return;
					}
				}
			System.out.println("\""+command+"\" is not a valid [view] command. Valid [view] commands:\n"+viewCommands);
		}
	}
	
	private static void viewTeam(TeamInterface team) {
		if(team == null) 
			return;
		
		String status = (team.is_alive() ? "Alive" : "Eliminated");
		System.out.printf("%n%s%s%nTeam: %-10s | Status: %s | Seed: #%-2d | Region: %-6s%n",
				menuBar,menuBar,team.getName(),status,team.getSeed(),team.getRegion());//Prints bar and basic team info
		
		System.out.printf("%s%s%nGames List: (%d wins)%n",
				menuBar,menuBar,team.getWins());//Section bar and games list header
		
		Iterator<TeamInterface> opItr = db.getOpponents(team).iterator();
		Iterator<Integer> scoreItr = db.getScores(team).iterator();
		for (int round = 1; scoreItr.hasNext() && opItr.hasNext(); round++) {
			TeamInterface opponent = opItr.next();
			System.out.printf("  -%s: %d vs %s: %d%n", 
					team,scoreItr.next(),opponent,db.getScores(opponent).get(round-1));//Each matchup opponent has had
		}
		System.out.printf("%s%s%n",menuBar,menuBar);
	}
	
	private static void viewPlayer(PlayerInterface p) {
		if(p == null) 
			return;
		
		/**Prints out one bar, player's name, ad balance*/
		System.out.printf("%n%s%n"
				+ "Player: %s | $% .2f | ",menuBar+menuBar,p.toString(),p.getBalance());
		
		/**Determines if player has chosen winner, prints, adds another bar"*/
		String chosenWinner;
		if (p.getChosenWinner() != null) {
			if(p.getChosenWinner().is_alive())
				chosenWinner = p.getChosenWinner().toString()+" (Alive)";
			else
				chosenWinner = p.getChosenWinner().toString()+" (Eliminated)";
		} else
			chosenWinner = "None Selected";
		System.out.printf("Chosen Winner: %s%n%s%n", chosenWinner,menuBar+menuBar);
		
		/**Pick-four points and potential points remaining (or none if bracket isn't ready*/ 
		String points = ((db.bracketInitialized()) ?
				("("+p.getPickFourPoints()+"/"+db.getPotentialPickFourPoints(p)+")") : "(0/0)");
		
		/**Prints a title and then the respective teams in two team rows*/
		String[] titles = {"Owned Teams:","Pick-Four: "+points};
		@SuppressWarnings("unchecked")
		Iterator<TeamInterface>[] itrs = (Iterator<TeamInterface>[]) new Iterator[2];
		itrs[0] = p.getTeams().iterator();
		itrs[1] = p.getPickFourTeams().iterator();
				
		for(int i = 0; i < titles.length && i < itrs.length; i++) {
			System.out.printf("%s%n   ", titles[i]);
			int count = 0;
			Iterator<TeamInterface> itr = itrs[i];
			while(itr.hasNext()) {
				TeamInterface t = itr.next();
				String status = ((t.is_alive()) ? " (Alive)" : " (Eliminated)");
				System.out.printf("%-20s%-15s", t,status);
				count++;
				if(itr.hasNext())
					System.out.printf((count % 2 == 0) ? "%n   " : " | ");
			}
			System.out.printf("%n");
		}
		
		/**Final bar to close out section of information*/
		System.out.printf("%s%n",menuBar+menuBar);
		
	}
	
	private static void viewPickFourLeaderboard() {
		Iterator<PlayerInterface> itr = db.getPickFourLeaderBoard().iterator();
		int rank = 1;
		System.out.printf("%s%n"
				+ "%-23s%-1s%n"
				+ "%s%n",menuBar,"**PLAYER**","**POINTS**",menuBar);
		while(itr.hasNext()) {
			PlayerInterface p1 = itr.next();
			int points = p1.getPickFourPoints();
			int potential = db.getPotentialPickFourPoints(p1);
			System.out.printf("#%-2d %-20s %d/%-10d%n", rank,p1.toString(),points,potential);
			rank++;
		}
	}
	
	private static void viewPlayerLeaderboard() {
		Iterator<PlayerInterface> itr = db.getPlayerLeaderboard().iterator();
		int rank = 1;
		System.out.printf("=================================%n"
				+ "%-18s%-1s%n"
				+ "=================================%n","**PLAYER**","**TEAMS ALIVE**");
		while(itr.hasNext()) {
			PlayerInterface player = itr.next();
			System.out.printf("#%-2d %-20s %d%n", rank,player.toString(),player.numTeamsAlive());
			rank++;
		}
	}
	
	//Look for missing scores
	private static void viewMissing() {
		String response = "";
		//Look for open bracket spots
		int numTeams = db.getTeams().size();
		if(numTeams != 64)
			response += "Open bracket spots: "+(64-numTeams)+"\n";
		
		//Look for open board spots
		int spotsRemaining = db.spotsRemaining();
		if(spotsRemaining != 0)
			response += "Open board spots: "+spotsRemaining+"\n";
		
		//Look for unowned teams
		List<TeamInterface> teams =  new LinkedList<TeamInterface>(db.getTeams());
		for(PlayerInterface player: db.getPlayers()) {
			for(TeamInterface team: player.getTeams())
				teams.remove(team);
		}
		if(!teams.isEmpty())
			response += "Unowned Teams: "+teams+"\n";
		
		//Look for player's missing fields
		if(response.length() != 0)
			response += "\n";
		for(PlayerInterface player: db.getPlayers())
		{
			String temp = "";
			if(player.getChosenWinner() == null)
				temp += "\t-Missing Chosen Winner\n";
			
			int numPickFourTeams = player.getPickFourTeams().size();
			if(numPickFourTeams != 4)
				temp += "\t-Missing "+(4-numPickFourTeams)+" Pick Four Team(s)\n";
			
			int numOwnedTeams = player.getTeams().size();
			int spotsPurchased = player.getNumSpots();
			if(numOwnedTeams != spotsPurchased)
				temp += "\t-Missing "+(spotsPurchased-numOwnedTeams)+" Purchased Team(s)\n";
			
			if(temp.length() != 0)
				response += "Player: "+player.getName()+"\n"+temp;
		}
		
		if(response.length() != 0)
			System.out.print(response);
		else
			System.out.println("No fields are missing!");
			
	}
	
	private static void viewWinners() {
		Iterator<PlayerInterface> winners;
		//BLOWOUTLOSS CONTEST
		winners = db.getPrizeWinner(PrizeType.FIRST_ROUND_BLOWOUT).iterator();//Need to be money not people... i fucked up
		String blowoutLossContest = "Blowout Loss Winner: $"+db.getPrizeMoney(PrizeType.FIRST_ROUND_BLOWOUT)+"\n";
		if(winners.hasNext()) {
			while(winners.hasNext()) {
				blowoutLossContest += " "+winners.next()+"\n";
			}
		}
		else {
			blowoutLossContest += " Contest is not complete.\n";
		}
		//OWNER OF CHAMPION TEAM
		winners = db.getPrizeWinner(PrizeType.CHAMPION_TEAM).iterator();
		String championTeamContest = "Champion Team Owner: $"+db.getPrizeMoney(PrizeType.CHAMPION_TEAM)+"\n";
		if(winners.hasNext()) {
			while(winners.hasNext()) {
				championTeamContest += " "+winners.next()+"\n";
			}
		}
		else {
			championTeamContest += " Contest is not complete.\n";
		}
		//U-PICK IT/CHOSEN WINNER CONTEST
		winners = db.getPrizeWinner(PrizeType.CHOSEN_WINNER).iterator();
		String chosenWinnerContest = "Chosen Winner Contest Winner(s): $"+db.getPrizeMoney(PrizeType.CHOSEN_WINNER)+"\n";
		if(winners.hasNext()) {
			while(winners.hasNext()) {
				chosenWinnerContest += " "+winners.next()+"\n";
			}
		}
		else if(db.getChampionTeam() != null) {
			chosenWinnerContest += "Nobody correctly selected "+db.getChampionTeam().getName()+".\n";
		}
		else {
			chosenWinnerContest += " Contest is not complete.\n";
		}
		//CINDERELLA CONTEST
		winners = db.getPrizeWinner(PrizeType.CINDERELLA_TEAM).iterator();
		String cinderellaContest = "Cinderella Contest Winner: $"+db.getPrizeMoney(PrizeType.CINDERELLA_TEAM)+"\n";
		if(winners.hasNext()) {
			while(winners.hasNext()) {
				cinderellaContest += " "+winners.next()+"\n";
			}
		}
		else {
			cinderellaContest += " Contest is not complete.\n";
		}
		//FINAL FOUR CONTEST
		winners = db.getPrizeWinner(PrizeType.FINAL_FOUR_TEAM).iterator();
		String finalFourContest = "Final Four Owners: $"+db.getPrizeMoney(PrizeType.FINAL_FOUR_TEAM)+"\n";
		if(winners.hasNext()) {
			while(winners.hasNext()) {
				finalFourContest += " "+winners.next()+"\n";
			}
		}
		else {
			finalFourContest += " Contest is not complete.\n";
		}
		//FIRST OVERTIME LOSS CONTEST
		winners = db.getPrizeWinner(PrizeType.FIRST_OVERTIME_LOSS).iterator();
		String firstOvertimeLossContest = "First Overtime Loss Contest Winner: $"+db.getPrizeMoney(PrizeType.FIRST_OVERTIME_LOSS)+"\n";
		if(winners.hasNext()) {
			while(winners.hasNext()) {
				firstOvertimeLossContest += " "+winners.next()+"\n";
			}
		}
		else {
			firstOvertimeLossContest += " Contest is not complete.\n";
		}
		//PICK FOUR CONTEST
		winners = db.getPrizeWinner(PrizeType.PICK_FOUR_CHAMPION).iterator();
		String pickFourContest = "Pick Four Contest Winners: $"+
				db.getPrizeMoney(PrizeType.PICK_FOUR_CHAMPION)+"/$"+db.getPrizeMoney(PrizeType.PICK_FOUR_RUNNERUP)+"\n";
		if(winners.hasNext()) {
			while(winners.hasNext()) {
				pickFourContest += " 1) "+winners.next()+"\n";
			}
			winners = db.getPrizeWinner(PrizeType.PICK_FOUR_RUNNERUP).iterator();
			if(winners.hasNext()) {
				while(winners.hasNext()) {
					pickFourContest += " 2) "+winners.next()+"\n";
				}
			}
		}
		else {
			pickFourContest += " Contest is not complete.\n";
		}
		//WIN FIRST GAME CONTEST
		winners = db.getPrizeWinner(PrizeType.WON_FIRST_GAME).iterator();
		String winFirstGameContest = "Win First Game Contest Winner(s): $"+db.getPrizeMoney(PrizeType.WON_FIRST_GAME)+" (each team)\n";
		if(winners.hasNext()) {
			PlayerInterface oldPlayer = winners.next();
			int count = 1;
			winFirstGameContest += " "+oldPlayer+" owns ";
			PlayerInterface newPlayer = null;
			while(winners.hasNext()) {
				newPlayer = winners.next();
				if(newPlayer.equals(oldPlayer))
					count++;
				else {
					winFirstGameContest += count+" team(s)\n "+newPlayer+" owns ";
					count = 1;
					oldPlayer = newPlayer;
				}
			}
			if(newPlayer != null)
				winFirstGameContest += count+" team(s)\n";
		}
		else {
			winFirstGameContest += " Contest is not complete.\n";
		}
		System.out.println(championTeamContest+"\n"+
							finalFourContest+"\n"+
							pickFourContest+"\n"+
							chosenWinnerContest+"\n"+
							blowoutLossContest+"\n"+
							firstOvertimeLossContest+"\n"+
							cinderellaContest+"\n"+
							winFirstGameContest);
	}
	
	private static void viewPrizes() {
		for(PrizeType prizeType: PrizeType.values()) {
			System.out.println("Contest: "+prizeType.getName()+"\nPrize: $"+ContestManager.getPrizeMoney(prizeType)
			+"\n  -"+prizeType.getDescription()+"\n");
		}
	}
	
	private static void viewSettings() {
		
	}
	
	private static void handleListCommand()
	{
		String command = scanner.next().toLowerCase();
		/**Only when instructions are fed in will it print the instructions given*/
		if(instructionFile != null)
			System.out.println(command);
		switch(command)
		{
			case "players":
				int i = 1;
				System.out.printf("%s%s%n",menuBar,menuBar);
				for(PlayerInterface player: db.getPlayers()) {
					System.out.printf("%-15s",player.getName());					
				if(i%4 == 0)//4 columns
					System.out.printf("%n");
				i++;
				}	
				if((i-1)%4 != 0)
					System.out.printf("%n");
				System.out.printf("%s%s%n",menuBar,menuBar);
				break;
			case "teams":
				i = 1;
				System.out.printf("%s%s%n",menuBar,menuBar);
				for(TeamInterface team: db.getTeams()) {
					System.out.printf("%-18s",team.getName());					
					if(i%4 == 0)//4 columns
						System.out.printf("%n");
					i++;
				}
				if((i-1)%4 != 0)
					System.out.printf("%n");
				System.out.printf("%s%s%n",menuBar,menuBar);
				break;
			default:
				scanner.nextLine();
				System.out.println("\""+command+"\" is not a valid [list] command. Valid [list] commands:\n"+listCommands);
		}
	}
	private static void handleEditCommand()
	{
		String command = "";
		if(scanner.hasNext()) //does this do anything??
			command = scanner.next().toLowerCase().trim();
		/**Only when instructions are fed in will it print the instructions given*/
		if(instructionFile != null)
			System.out.println(command);
		PlayerInterface player;
		TeamInterface team;
		switch(command)
		{
		case "scores"://Edit scores
			team = userEnterTeam();
			processEditScores(team);
			break;
		case "team"://Edit team
			team = userEnterTeam();
			processEditTeam(team);
			break;
		case "player"://Edit player
			player = userEnterPlayer();
			processEditPlayer(player);
			break;
		case "settings"://Edit settings
			processEditSettings();
			break;
		default:
			String rest = scanner.nextLine();
			player = db.findPlayer(command+rest);//See if user entered player name
			if(player != null) {
				processEditPlayer(player);
				return;
			}
			team = db.findTeam(command+rest);//See if user entered team name
			if(team != null) {
				processEditTeam(team);
				return;
			}
			else
				System.out.println("\""+command+"\" is not a valid [edit] command. Valid [edit] commands:\n"+editCommands);
			break;
		}
	}
	
	private static void processEditScores(TeamInterface team)
	{
		if(team == null) {
			System.out.println("Team was not found");
			return;
		}
		System.out.println("The scores will be shown in order of oldest to new. "
				+ "\n\t- Press return if the score is correct."
				+ "\n\t- Enter the correct score and press return if the score needs to be changed.");
		List<Integer> scores = db.getScores(team);
		List<Integer> newScores = new LinkedList<Integer>();
		int roundNum = 1;
		for(int score: scores) {
			System.out.print("Round "+roundNum+": "+score+" ");
			String response = scanner.nextLine();
			try {
			if(!response.equals(""))
				score = Integer.parseInt(response);
			}
			catch(NumberFormatException e) {
				System.out.println("Please enter only numbers");
			}
			newScores.add(score);
			roundNum++;
		}
		db.editScores(team, newScores);
	}
	
	private static void processEditTeam(TeamInterface team)
	{
		if(team == null) {
			System.out.println("Team was not found");
			return;
		}
		System.out.println("The traits of the Team will be shown line by line: "
				+ "\n\t- Press return if the trait is correct."
				+ "\n\t- Enter the correct trait and press return if the trait needs to be changed.");
		
		String name = team.getName();
		int seed = team.getSeed();
		Region region = team.getRegion();
		String response;
		
		//Read the name and update if necessary
		System.out.print("Name: "+name+" > ");
		response = scanner.nextLine();
		if(!response.trim().equals(""))
			name = response;
		
		//Read the seed and update if necessary
		System.out.print("Seed: "+seed+" > ");
		response = scanner.nextLine();
		try {
			int temp = Integer.parseInt(response);
			if(temp > 0 && temp < 17)
				seed = temp;
		} catch(NumberFormatException e){}
		
		//Read the name and update if necessary
		System.out.print("Region: "+region+" > ");
		response = scanner.nextLine();
		Region tempRegion = null;
		if(!response.trim().equals("")) {
				tempRegion = Region.determineRegion(response);
				if(tempRegion != null)
					region = tempRegion;
		}
		
		boolean nameUnchanged = team.getName().equals(name);
		boolean seedUnchanged = team.getSeed() == seed;
		boolean regionUnchanged = team.getRegion() == region;
		if(nameUnchanged && seedUnchanged && regionUnchanged)//Nothing was changed
			return;
		else if(!seedUnchanged || !regionUnchanged) {//New seed and/or new region
			if(!nameUnchanged) {//New place in bracket and also new name
				TeamInterface temp = db.findTeam(name);
				if(temp != null && temp != team) {//Team name is a duplicate
					System.out.println("Change was not made. \""+name+"\" already exists in the bracket");
					return;
				}
				team.setName(name);
			}
			//New place in bracket and name is set
			db.moveTeam(team, seed, region);
			return;
		}
		else {
			TeamInterface temp = db.findTeam(name);
			if(temp != null && temp != team) {
				System.out.println("Change was not made. \""+name+"\" already exists in the bracket");
				return;
			}
			team.setName(name);
		}
	}
	
	private static void processEditPlayer(PlayerInterface player) {
		if(player == null) {
			System.out.println("Player was not found");
			return;
		}
		System.out.println("The traits of the Team will be shown line by line: "
				+ "\n\t- Press return if the trait is correct."
				+ "\n\t- Enter the correct trait and press return if the trait needs to be changed.");
		//Name
		System.out.print("\nName: "+player.getName()+" > ");
		String line = scanner.nextLine().trim();
		
		if(line.length() > 0) {
			PlayerInterface temp = db.findPlayer(line);
			if(temp == null || temp == player)
				player.setName(line);
		}
		
		//Spots Purchased
		if(!db.teamsAssigned()) {
			System.out.print("Spots Purchased: "+player.getNumSpots()+" > ");
			line = scanner.nextLine().trim();
			try {
				if(line.length() > 0)
					player.setNumSpots(Integer.parseInt(line));
			} catch(NumberFormatException e){}
		}
		
		//Pick Four Teams
		System.out.println("Pick Four Teams: ");
		List<TeamInterface> pick4teams = new LinkedList<TeamInterface>();
		Iterator<TeamInterface> itr = player.getPickFourTeams().iterator();
		int num = 0;
		while(itr.hasNext() || num < 4) {
			TeamInterface team = null;
			if(itr.hasNext()) {
				team = itr.next();
				System.out.print("  -"+team.getName());
			}
			System.out.print(" > ");
			line = scanner.nextLine().trim();
			if(line.length() > 0) {
				TeamInterface temp = db.findTeam(line);
				if(temp != null)
					team = temp;
			}
			if(team != null)
				pick4teams.add(team);
			num++;
		}
		player.setPickFourTeams(pick4teams);

	}
	
	private static void processEditSettings() {
		scanner.nextLine();
		System.out.println("The settings of the program will be shown line by line: "
				+ "\n\t- Press return if the trait is correct."
				+ "\n\t- Enter the correct trait and press return if the trait needs to be changed.\n");
		
		//Edit Region Matchups
		Region[] regionMatchups = db.getRegionMatchups();
		System.out.println("Region Matchups:");
		for(int i = 0; i < regionMatchups.length; i++) {
			System.out.print("  "+regionMatchups[i]+" vs ");
			i++;
			if(i < regionMatchups.length)
				System.out.print(regionMatchups[i]);
			System.out.println();
		}
		
		System.out.print(" >");
		String line = scanner.nextLine().trim();
		if(line.equalsIgnoreCase("quit"))
			return;
		if(line.length() != 0 && line.contains(" vs ")) {
			Region[] newRegionMatchups = new Region[regionMatchups.length];
			String[] pieces = line.split(" vs ");
			for(int i = 0; i < 2; i++) {
				Region temp = Region.determineRegion(pieces[i]);
				if(temp != null)
					newRegionMatchups[i] = temp;
			}
			if(newRegionMatchups[0] != null && newRegionMatchups[1] != null && !newRegionMatchups[0].equals(newRegionMatchups[1])) {
				int index = 2;//Region's in spot 3 and 4 need to be determined
				for(int i = 0; i < regionMatchups.length; i++) {
					boolean found = false;
					for(int j = 0; j < newRegionMatchups.length; j++) {
						if(newRegionMatchups[j] != null && newRegionMatchups[j].equals(regionMatchups[i])) 
							found = true;
					}
					if(!found) {
						newRegionMatchups[index] = regionMatchups[i];
						index ++;
					}
				}
				List<TeamInterface> teams = new LinkedList<TeamInterface>(db.getTeams());
				while(!db.getTeams().isEmpty())
					db.getTeams().remove(0);
				
				List<List<Integer>> scoreLists = new LinkedList<List<Integer>>();
				for(TeamInterface team: teams)
					scoreLists.add(db.getScores(team));
				
				db = new MarchMadnessDB();
				db.setRegionMatchups(newRegionMatchups);
				
				Iterator<TeamInterface> teamItr = teams.iterator();
				Iterator<List<Integer>> scoreListItr = scoreLists.iterator();
				
				while(teamItr.hasNext() && scoreListItr.hasNext()) {
					TeamInterface team = teamItr.next();
					List<Integer> scores = scoreListItr.next();
					db.addTeam(team);
					db.editScores(team, scores);
				}
			}
			
		}
		
		//Edit Leaderboard Size
		System.out.print("\nLeaderboard Size: "+db.getLeaderboardSize()+" > ");
		line = scanner.nextLine().trim();
		if(line.equalsIgnoreCase("quit"))
			return;
		try {
			if(line.length() > 0)
				db.setLeaderboardSize(Integer.parseInt(line));
		} catch(NumberFormatException e){}
		
		//Edit prizes
		System.out.println("\nPrizes:");
		for(PrizeType prize: PrizeType.values()) {
			System.out.print("  "+prize.getName()+" $"+db.getPrizeMoney(prize)+" > ");
			line = scanner.nextLine().trim();
			if(line.equalsIgnoreCase("quit"))
				return;
			try {
				if(line.length() > 0)
					db.setPrizeMoney(prize,Double.parseDouble(line));
			} catch(NumberFormatException e){}
		}
		
		//Edit pricing
		System.out.println("\nPricing:");
		Iterator<Integer> itr = db.getPricing().iterator();
		List<Integer> pricing = new LinkedList<Integer>();
		int i = 0;
		while(itr.hasNext()) {
			int price = itr.next();
			if(i == 1)
				System.out.print("  Price of "+i+" Spot "+price+" > ");
			else if(i > 1)
				System.out.print("  Price of "+i+" Spots "+price+" > ");
			if(i > 0) {
				line = scanner.nextLine().trim();
				if(line.equalsIgnoreCase("quit"))
					return;
				try {
					if(line.length() > 0)
						price = Integer.parseInt(line);
				} catch(NumberFormatException e){}
			}
			pricing.add(price);
			i++;
		}
		db.setPricing(pricing);
	}
	
	private static void handleRemoveCommand()
	{
		String command = scanner.next().toLowerCase();
		/**Only when instructions are fed in will it print the instructions given*/
		if(instructionFile != null)
			System.out.println(command);
		switch(command) {
		case "player":		//Remove player
			PlayerInterface player = userEnterPlayer();
			removePlayer(player);
			break;
		case "assigned":
			removeAssignedTeams();
			break;
		case "pick4":
			removePickFourTeams();
			break;
		case "chosenteams":
			removeChosenTeams();
			break;
		case "team":		//Remove team
			TeamInterface team = userEnterTeam();
			removeTeam(team);
			break;
		case "scores":		//Remove all scores			Should question whether user is sure
			removeScores();
			System.out.printf("All scores removed.%n");
			break;
		default:
			command += scanner.nextLine();
			player = db.findPlayer(command);
			if(player != null) {
				removePlayer(player);
				return;
			}
			team = db.findTeam(command);
			if(team != null) {
				removeTeam(team);
				return;
			}
			System.out.println("\""+command+"\" is not a valid [remove] command. Valid [remove] commands:\n"+removeCommands);
		}
	}
	
	private static void removePlayer(PlayerInterface player) {
		if(player == null) {
			System.out.println("Player was not found");
			return;
		}
		PlayerInterface removedPlayer = db.removePlayer(player.getName()); 
		if(removedPlayer  == null)
			System.out.println("\""+player.getName()+"\""+" was not found");
		else
			System.out.println(removedPlayer+" was removed successfully");
	}
	
	private static void removeTeam(TeamInterface team) { 
		TeamInterface removedTeam = null;
		if(team == null) {
			System.out.println("Team was not found");
			return;
		}
		if(team.getName().length() > 0)
			removedTeam = db.removeTeam(team.getName());
		if(removedTeam == null)
			System.out.println("\""+team.getName()+"\""+"was not found");
		else
			System.out.println(team.getName()+" was removed successfully");
	}
	
	private static void removeScores() {
		List<Integer> zero = new ArrayList<Integer>(); 
		zero.add(0);
		for(TeamInterface t: db.getTeams()) {
			db.editScores(t, zero);
		}
	}
	
	private static void removeAssignedTeams() {//Warnings for removing during tournament
		for(PlayerInterface player: db.getPlayers()) {
			List<TeamInterface> teams = player.getTeams();
			while(!teams.isEmpty())
				teams.remove(0);
		}
	}
	
	private static void removePickFourTeams() {
		for(PlayerInterface player: db.getPlayers()) {
			List<TeamInterface> teams = player.getPickFourTeams();
			while(!teams.isEmpty())
				teams.remove(0);
		}
	}
	
	private static void removeChosenTeams() {
		for(PlayerInterface player: db.getPlayers()) {
			player.addChosenTeam(null);
		}
	}
	
	private static void assignTeams() {
		if(!db.bracketInitialized()) {
			System.out.println("Bracket is missing teams. Teams not assigned");
			return;
		}
		if(db.spotsRemaining() != 0) {
			System.out.println("Not all of the spots are filled. Teams not assigned");
			return;
		}
		
		boolean allAssigned = true;
		boolean someAssigned = false;
		for (PlayerInterface player : db.getPlayers()) {
			if(player.getTeams().size() != player.getNumSpots())
				allAssigned = false;
			if(player.getTeams().size() > 0)
				someAssigned = true;
		}
		if(allAssigned || someAssigned) {
			String prompt = "";
			if(allAssigned)
				prompt = String.format("All teams have already been assigned. Do you want to reassign? (y/n)");
			else if(someAssigned)
				prompt = String.format("Some teams have already been assigned.%n"
						+ "Assigning now will erase all previously assigned teams and reassign new teams.%n"
						+ "Do you wish to continue? (y/n) ");
			System.out.printf(prompt);
			String command = scanner.next().trim().toLowerCase();
			if(!command.equals("y")) {
				System.out.println("Assignment cancelled");
				return;
			}
		} else {
			System.out.print("Are you sure you are ready to assign teams? (y/n) ");
			String command = scanner.next().trim().toLowerCase();
			if(!command.equals("y")) {
				System.out.println("Assignment cancelled");
				return;
			}
		}
			db.assignTeams();
			System.out.println("Teams assigned successfully");
		
	}
	
	private static void resetAll() {
		removeScores();
		for(PlayerInterface player: db.getPlayers()) {
			List<TeamInterface> teams = player.getTeams();
			List<TeamInterface> pick4 = player.getPickFourTeams();
			while(!teams.isEmpty()) 
				teams.remove(0);
			while(!pick4.isEmpty())
				pick4.remove(0);
			player.addChosenTeam(null);
				
		}
	}
	
	private static TeamInterface userEnterTeam() {
		TeamInterface team = null;
		scanner.nextLine();//Clear out scanner
		while(team == null) {
			System.out.print("\tTeam Name: ");
			String line = scanner.nextLine();
			if(line.trim().equalsIgnoreCase("quit"))
				return null;
			team = db.findTeam(line);
			if(team == null) 
				System.out.println("\t *TEAM NOT FOUND");			
		}
		return team;
	}
	
	private static PlayerInterface userEnterPlayer() {
		PlayerInterface player = null;
		scanner.nextLine();//Clear out scanner
		while(player == null) {
			System.out.print("\tPlayer Name: ");
			String line = scanner.nextLine();
			if(line.trim().equalsIgnoreCase("quit"))
				return null;
			player = db.findPlayer(line);
			if(player == null) 
				System.out.println("\t *PLAYER NOT FOUND");
		}
		return player;
	}

	//Make the first word capitalized
	private static String formatTeam(String teamName)
	{
		if(teamName == null)
			return null;
		teamName = teamName.replace("State","St.");
		teamName = teamName.replace("St ", "St.");
		return teamName;
	}
}
