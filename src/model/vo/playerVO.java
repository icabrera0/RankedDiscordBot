package model.vo;

public class playerVO {
	private long playerID;
	private int wins;
	private int loses;
	private int highestwinstreak;
	private int currentwinstreak;
	private double winRatio;
	private Integer ranking;
	private int dodges;
	private int elo;
	
	public playerVO() {
		
	}
	
	public playerVO(long playerID) {
		this.playerID = playerID;
		wins = 0;
		loses = 0;
		highestwinstreak = 0;
		currentwinstreak = 0;
		dodges = 0;
		elo = 500;
	}
	
	public long getPlayerID() {
		return playerID;
	}
	public void setPlayerID(long playerID) {
		this.playerID = playerID;
	}
	public int getWins() {
		return wins;
	}
	public void setWins(int wins) {
		this.wins = wins;
	}
	public int getLoses() {
		return loses;
	}
	public void setLoses(int loses) {
		this.loses = loses;
	}
	public int getHighestwinstreak() {
		return highestwinstreak;
	}
	public void setHighestwinstreak(int highestwinstreak) {
		this.highestwinstreak = highestwinstreak;
	}
	public int getCurrentwinstreak() {
		return currentwinstreak;
	}
	public void setCurrentwinstreak(int currentwinstreak) {
		this.currentwinstreak = currentwinstreak;
	}
	public void setWinRatio(double winRatio) {
		this.winRatio = winRatio;
	}
	public double getWinRatio() {
		return this.winRatio;
	}

	public int getDodges() {
		return dodges;
	}

	public void setDodges(int dodges) {
		this.dodges = dodges;
	}

	public Integer getRanking() {
		return ranking;
	}

	public void setRanking(int ranking) {
		this.ranking = ranking;
	}

	public int getElo() {
		return elo;
	}

	public void setElo(int elo) {
		this.elo = elo;
	}

	public String toString() {
		return "\nWinratio: " + this.winRatio + "%" + "\n Wins: " + this.wins + " | Loses: " + this.loses + "\n Highest Winstreak: " + this.highestwinstreak + "\n Matches Dodged: " + this.dodges;
	}
	
	
}
