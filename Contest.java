import java.util.LinkedList;
import java.util.List;

public class Contest {

	/**The amount of money that will be given to the players that won the contest*/
	private double prizeMoney;
	/**The player or players that owned the winning team(s) and will be receiving the prize money*/
	private List<PlayerInterface> players;
	/**The team or teams that won the contest*/
	private List<TeamInterface> teams;
	
	//private PrizeType prizeType;
	
	public Contest(double prizeMoney) {
		this.prizeMoney = prizeMoney;
		players = new LinkedList<PlayerInterface>();
		teams = new LinkedList<TeamInterface>();
	}
	
	public int instancesOf(PlayerInterface player) {
		int total = 0;
		for(PlayerInterface winner: players)
			if(player.equals(winner))
				total++;
		return total;
	}

	public double getPrizeMoney() {
		return prizeMoney;
	}

	public void setPrizeMoney(double prizeMoney) {
		if(prizeMoney > 0)
			this.prizeMoney = prizeMoney;
	}

	public List<PlayerInterface> getPlayers() {
		return players;
	}

	public void setPlayers(List<PlayerInterface> players) {
		if(players != null)
			this.players = players;
	}

	public List<TeamInterface> getTeams() {
		return teams;
	}

	public void setTeams(List<TeamInterface> teams) {
		if(teams != null)
			this.teams = teams;
	}
	
	
	
}

