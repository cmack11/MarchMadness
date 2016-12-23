public class Team implements TeamInterface, Comparable<TeamInterface>
{
	private String name;
	private int seed;
	private Region region;
	private int wins;
	private boolean alive;
	
	public Team(String name, int seed, Region region)
	{
		this(name,seed,region,true,0);
	}
	
	public Team(String name, int seed, Region region, boolean alive)
	{
		this(name,seed,region,alive,0);
	}
	
	public Team(String name, int seed, Region region, boolean alive, int wins)
	{
		this.name = name;
		this.seed = seed;
		this.region = region;
		this.alive = alive;
		this.wins = wins;
	}
	
	public String getName() 
	{
		return name;
	}
	
	public void setName(String name)
	{
		if(name == null || name.trim().length() < 1)
			throw new IllegalArgumentException();
		this.name = name;
	}

	public Region getRegion() 
	{
		return region;
	}

	public int getSeed() 
	{
		return seed;
	}
	
	public int getWins() 
	{
		return wins;
	}
	
	public boolean is_alive() 
	{
		return alive;
	}
	
	public void loses() 
	{
		alive = false;
	}
	
	public void wins() 
	{
		if(!alive)
			throw new IllegalArgumentException();
		wins++;
	}
	
	public String toString()
	{
		return "#"+seed+" "+name;
	}

	@Override
	public int compareTo(TeamInterface o) 
	{
		if(o == null)
			throw new NullPointerException();
		return this.getName().compareTo(o.getName());
	}

	public void setWins(int wins) 
	{
		this.wins = wins;
	}

	public void setStatus(boolean status) 
	{
		this.alive = status;
	}
	
	
}
