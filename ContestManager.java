import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class ContestManager {

	private static Contest[] contests;
	private static MarchMadnessDB database;
	
	public static void initialize(MarchMadnessDB db) {
		database = db;
		
		contests = new Contest[PrizeType.getNumPrizes()];
		
		contests[PrizeType.CHOSEN_WINNER.getPrizeNum()] =
				new Contest(20);
		contests[PrizeType.CINDERELLA_TEAM.getPrizeNum()] =
				new Contest(10);
		contests[PrizeType.FIRST_OVERTIME_LOSS.getPrizeNum()] =
				new Contest(10);
		contests[PrizeType.FIRST_ROUND_BLOWOUT.getPrizeNum()] =
				new Contest(20);
		contests[PrizeType.WON_FIRST_GAME.getPrizeNum()] =
				new Contest(5);
		
		contests[PrizeType.FINAL_FOUR_TEAM.getPrizeNum()] =
				new Contest(20);
		contests[PrizeType.CHAMPION_TEAM.getPrizeNum()] =
				new Contest(80);
		
		contests[PrizeType.PICK_FOUR_CHAMPION.getPrizeNum()] =
				new Contest(30);
		contests[PrizeType.PICK_FOUR_RUNNERUP.getPrizeNum()] =
				new Contest(10);
	}
	
	public static boolean setPrizeMoney(PrizeType prizeType, double prizeMoney) {
		if(prizeType == null)
			return false;
		contests[prizeType.getPrizeNum()].setPrizeMoney(prizeMoney);
		return true;
	}
	
	public static double getPrizeMoney(PrizeType prizeType) {
		if(prizeType == null)
			return 0;
		return contests[prizeType.getPrizeNum()].getPrizeMoney();
	}
	
	public static List<PlayerInterface> getWinningPlayers(PrizeType prizeType) {
		if(prizeType == null)
			return null;
		return contests[prizeType.getPrizeNum()].getPlayers();
	}
	
	public static List<TeamInterface> getWinningTeams(PrizeType prizeType) {
		if(prizeType == null)
			return null;
		return contests[prizeType.getPrizeNum()].getTeams();
	}
	
	//Check if first round is done (for some) and if all scores are entered (others) save time and don't declare winners early
	public static void update()
	{
		if(database == null)
			return;
		
		List<TeamInterface> finalFourTeams = database.getFinalFour();
		List<TeamInterface> finalFourContestTeams = new LinkedList<TeamInterface>();
		List<PlayerInterface> finalFourContestWinners = new LinkedList<PlayerInterface>();

		TeamInterface champion = database.getChampionTeam();
		List<TeamInterface> chosenWinnerTeam = new LinkedList<TeamInterface>();
		List<PlayerInterface> chosenWinnerWinners = new LinkedList<PlayerInterface>();

		TeamInterface otLoser = database.getOTloser();
		List<TeamInterface> otLoserTeam = new LinkedList<TeamInterface>();
		List<PlayerInterface> otLoserWinner = new LinkedList<PlayerInterface>();

		TeamInterface firstRoundBlowout = database.getFirstRoundBlowout();
		List<TeamInterface> firstRoundBlowoutTeam = new LinkedList<TeamInterface>();
		List<PlayerInterface> firstRoundBlowoutWinner = new LinkedList<PlayerInterface>();

		List<TeamInterface>  championTeam = new LinkedList<TeamInterface>();
		List<PlayerInterface>  championWinner = new LinkedList<PlayerInterface>();

		int mostWins = 0;
		List<TeamInterface> cinderellaTeams = new LinkedList<TeamInterface>();
		List<PlayerInterface> cinderellaWinners = new LinkedList<PlayerInterface>();

		List<TeamInterface> wonFirstGameTeams = new LinkedList<TeamInterface>();
		List<PlayerInterface> wonFirstGameWinners = new LinkedList<PlayerInterface>();
		
		boolean allScoresEntered = champion != null;
		
		for(PlayerInterface player: database.getPlayers()) {								//Each player in the database
			if(!player.hasPaid())
				player.setBalance(0-database.getPricing().get(player.getNumSpots()));	  	//Player's balance is set to
			else																			//initial if player has not
				player.setBalance(0);														//paid
			
			for(TeamInterface ownedTeam: player.getTeams()) {								//Each team the player owns
				Iterator<TeamInterface> ffItr = finalFourTeams.iterator();
				while(ffItr.hasNext()) {													//Each of the final four 
					TeamInterface finalFourTeam = ffItr.next();								//teams
					if(ownedTeam.equals(finalFourTeam)) {									//If player owns a final 
						ffItr.remove();														//four team
						finalFourContestTeams.add(ownedTeam);
						finalFourContestWinners.add(player);
					}
				}

				if(allScoresEntered && championTeam.isEmpty() && ownedTeam.equals(champion)) { 	//If player owns 
					championTeam.add(ownedTeam);												//the champion
					championWinner.add(player);
				}

				if(otLoser != null && otLoserTeam.isEmpty() && ownedTeam.equals(otLoser)) {		//If player owns 
					otLoserTeam.add(ownedTeam);													//the OT loser
					otLoserWinner.add(player);
				}
				if(firstRoundBlowout != null && firstRoundBlowoutTeam.isEmpty() && 				//If player owns the
						ownedTeam.equals(firstRoundBlowout)) {									//first round blowout
					firstRoundBlowoutTeam.add(ownedTeam); 										//team
					firstRoundBlowoutWinner.add(player);												
				}

				if(ownedTeam.getWins() > 0) {													//Team won first game
					wonFirstGameTeams.add(ownedTeam);											
					wonFirstGameWinners.add(player);
				}

				if(ownedTeam.getSeed() > 8) {													//Farthest advancing
																								//9-16 seeded team
					if(ownedTeam.getWins() > mostWins) {
						cinderellaTeams = new LinkedList<TeamInterface>();
						cinderellaWinners = new LinkedList<PlayerInterface>();
						cinderellaWinners.add(player);
						cinderellaTeams.add(ownedTeam);
						mostWins = ownedTeam.getWins();
					}
					else if(mostWins != 0 && ownedTeam.getWins() == mostWins) {					
						if(ownedTeam.getSeed() > cinderellaTeams.get(0).getSeed()) {			//Lower seed wins
							cinderellaWinners = new LinkedList<PlayerInterface>();				//Tiebreaker
							cinderellaTeams = new LinkedList<TeamInterface>();
							cinderellaWinners.add(player);
							cinderellaTeams.add(ownedTeam);
						}
						else if(ownedTeam.getSeed() == cinderellaTeams.get(0).getSeed())		//True tie
						{
							cinderellaWinners.add(player);
							cinderellaTeams.add(ownedTeam);
						}
					}
				}
			}

			if(allScoresEntered && player.getChosenWinner() != null &&
					player.getChosenWinner().equals(champion)) {								//If the player chose
				chosenWinnerTeam.add(champion);													//the correct champion
				chosenWinnerWinners.add(player);												
			}
		}
		double earnings;
		Contest curr;
		
		if(!firstRoundBlowoutTeam.isEmpty() && !firstRoundBlowoutWinner.isEmpty()) {
			curr = contests[PrizeType.FIRST_ROUND_BLOWOUT.getPrizeNum()];
			curr.setPlayers(firstRoundBlowoutWinner);
			curr.setTeams(firstRoundBlowoutTeam);
		}
		if(!championTeam.isEmpty() && !championWinner.isEmpty()) {
			curr = contests[PrizeType.CHAMPION_TEAM.getPrizeNum()];
			curr.setPlayers(championWinner);
			curr.setTeams(championTeam);
		}
		if(!chosenWinnerTeam.isEmpty() && !chosenWinnerWinners.isEmpty()) {
			curr = contests[PrizeType.CHOSEN_WINNER.getPrizeNum()];
			curr.setPlayers(chosenWinnerWinners);
			curr.setTeams(chosenWinnerTeam);
		}
		if(!cinderellaWinners.isEmpty() && !cinderellaTeams.isEmpty()) {
			curr = contests[PrizeType.CINDERELLA_TEAM.getPrizeNum()];
			curr.setPlayers(cinderellaWinners);
			curr.setTeams(cinderellaTeams);
		}
		if(!finalFourContestTeams.isEmpty() && !finalFourContestWinners.isEmpty()) {
			curr = contests[PrizeType.FINAL_FOUR_TEAM.getPrizeNum()];
			curr.setPlayers(finalFourContestWinners);
			curr.setTeams(finalFourContestTeams);
		}
		if(!otLoserTeam.isEmpty() && !otLoserWinner.isEmpty()) {
			curr = contests[PrizeType.FIRST_OVERTIME_LOSS.getPrizeNum()];
			curr.setPlayers(otLoserWinner);
			curr.setTeams(otLoserTeam);
		}
		if(!wonFirstGameTeams.isEmpty() && !wonFirstGameWinners.isEmpty()) {
			curr = contests[PrizeType.WON_FIRST_GAME.getPrizeNum()];
			curr.setPlayers(wonFirstGameWinners);
			curr.setTeams(wonFirstGameTeams);
		}
		List<PlayerInterface> pickFourLeaderboard = database.getPickFourLeaderBoard(database.getPlayers().size());		//NEED TO ACCOUNT FOR TIES
		if(allScoresEntered && !pickFourLeaderboard.isEmpty()) {
			List<PlayerInterface> winner = new LinkedList<PlayerInterface>();
			winner.add(pickFourLeaderboard.remove(0));
			while(!pickFourLeaderboard.isEmpty() && 
					winner.get(0).getPickFourPoints() == pickFourLeaderboard.get(0).getPickFourPoints())
				winner.add(pickFourLeaderboard.remove(0));
			curr = contests[PrizeType.PICK_FOUR_CHAMPION.getPrizeNum()];
			curr.setPlayers(winner);
			curr.setTeams(winner.get(0).getPickFourTeams());
		}
		if(allScoresEntered && !pickFourLeaderboard.isEmpty()) {
			List<PlayerInterface> runnerUp = new LinkedList<PlayerInterface>();
			runnerUp.add(pickFourLeaderboard.remove(0));
			while(!pickFourLeaderboard.isEmpty() && 
					runnerUp.get(0).getPickFourPoints() == pickFourLeaderboard.get(0).getPickFourPoints())
				runnerUp.add(pickFourLeaderboard.remove(0));
			curr = contests[PrizeType.PICK_FOUR_RUNNERUP.getPrizeNum()];
			curr.setPlayers(runnerUp);
			curr.setTeams(runnerUp.get(0).getPickFourTeams());
		}
		
		for(Contest contest: contests) {
			if(contest.getPlayers().size() > 0) {
				if(contests[PrizeType.PICK_FOUR_CHAMPION.getPrizeNum()].equals(contest) &&
						contest.getPlayers().size() > 1) {
					earnings = (contest.getPrizeMoney() + 
							contests[PrizeType.PICK_FOUR_RUNNERUP.getPrizeNum()].getPrizeMoney()) /
							contest.getPlayers().size();

					for(PlayerInterface player: contest.getPlayers())
						player.setBalance(player.getBalance()+earnings);
					contests[PrizeType.PICK_FOUR_RUNNERUP.getPrizeNum()].setPlayers(new LinkedList<PlayerInterface>());
					contests[PrizeType.PICK_FOUR_RUNNERUP.getPrizeNum()].setTeams(new LinkedList<TeamInterface>());
				}
				else if(contests[PrizeType.WON_FIRST_GAME.getPrizeNum()].equals(contest)) {
					for(PlayerInterface player: contest.getPlayers())
						player.setBalance(player.getBalance()+contest.getPrizeMoney());
				}
				else {
				earnings = contest.getPrizeMoney() / contest.getPlayers().size();
				for(PlayerInterface player: contest.getPlayers())
					player.setBalance(player.getBalance()+earnings);
				}
			}
		}
	}
}
