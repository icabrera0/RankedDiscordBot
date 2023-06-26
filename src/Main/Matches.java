package Main;

import java.util.ArrayList;

public class Matches {
	private ArrayList<Match> matches;
	
	public Matches() {
		matches = new ArrayList<Match>();
	}
	
	public void addMatch(Match m) {
		matches.add(m);
	}
	
	public void removeMatchByUserId(String id) {
		
		for (int i = 0;i < matches.size();i++) {
			Match match = matches.get(i);
			if (match.getTeam1().get(0).getId().equals(id))
				matches.remove(i);
			if (match.getTeam1().get(1).getId().equals(id))
				matches.remove(i);
			if (match.getTeam2().get(0).getId().equals(id))
				matches.remove(i);
			if (match.getTeam2().get(1).getId().equals(id))
				matches.remove(i);
		}
	}
	public Match get(int i){
		return matches.get(i);
	}

	public int size(){
		return matches.size();
	}

	public void clearMatches(){
		matches.clear();
	}
	
	public Match getMatchByUserId(String id) {
		for (int i = 0;i < matches.size();i++) {
			Match match = matches.get(i);
			if (match.getTeam1().get(0).getId().equals(id))
				return matches.get(i);
			if (match.getTeam1().get(1).getId().equals(id))
				return matches.get(i);
			if (match.getTeam2().get(0).getId().equals(id))
				return matches.get(i);
			if (match.getTeam2().get(1).getId().equals(id))
				return matches.get(i);
		}
		return null;
	}
	
	public boolean playerIsInAMatch(String id) {
		for (int i = 0;i < matches.size();i++) {
			Match match = matches.get(i);
			if (match.getTeam1().get(0).getId().equals(id))
				return true;
			if (match.getTeam1().get(1).getId().equals(id))
				return true;
			if (match.getTeam2().get(0).getId().equals(id))
				return true;
			if (match.getTeam2().get(1).getId().equals(id))
				return true;
		}
		return false;
	}

	private static float getProbability(float rating1,float rating2){
		return 1.0f / (1 + (float)(Math.pow(10,(rating1 - rating2) / 500)));

	}

	private static void getNewRating(float ra, float rb, int k, boolean d){
		// Team 2 chance of winning
		float team2Chance = getProbability(ra,rb);
		// Team 1 chance of winning
		float team1Chance = getProbability(rb,ra);

		// If team 1 wins
		if (d){
			ra = ra + k * (1 - team1Chance);
			System.out.println("team 1 new elo: "+ra);
			rb = rb + k * (0 - team2Chance);
			System.out.println("team 2 new elo: "+rb);
		}
		// If team 2 wins
		if (!d){
			rb = rb + k * (1 - team2Chance);
			System.out.println("team 2 new elo: "+rb);
			ra = ra + k * (0 - team1Chance);
			System.out.println("team 1 new elo: "+ra);
		}

	}

}
