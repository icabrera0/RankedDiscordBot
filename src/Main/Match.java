package Main;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import model.dao.playerDAO;
import model.vo.playerVO;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Button;

public class Match extends ListenerAdapter{
	private int id;
	private List<Member> team1;
	private List<Member> team2;
	private boolean player1, player2, player3, player4 = false;
	private static int nextId = 0;
	private String messageID;

	private String[] playerIDs;

	private int team1Won;
	private int team2Won;
	private int team1Avg;
	private int team2Avg;
	private List<Member> playersVotes;

	public Match() {

	}

	public Match(List<Member> usersOnQueue) {
		playerIDs = new String[4];

		team1 = new ArrayList<>();
		team2 = new ArrayList<>();
		playersVotes = new ArrayList<>();
		Random ale = new Random();
		int randomPlayer = 0;
		id = nextId;
		nextId++;


		int k = 0;
		for (int i = 1;i <= 2;i++) {
			randomPlayer = ale.nextInt(usersOnQueue.size());
			playerIDs[k] = usersOnQueue.get(randomPlayer).getId();
			k++;
			team1.add(usersOnQueue.get(randomPlayer));
			usersOnQueue.remove(randomPlayer);
		}

		System.out.println(team1.get(0).getUser().getName());

		for (int i = 1;i <= 2;i++) {
			randomPlayer = ale.nextInt(usersOnQueue.size());
			playerIDs[k] = usersOnQueue.get(randomPlayer).getId();
			k++;
			team2.add(usersOnQueue.get(randomPlayer));
			usersOnQueue.remove(randomPlayer);
		}
		playersVotes.addAll(team1);
		playersVotes.addAll(team2);

		team1Avg = getAverageElo(1);
		team2Avg = getAverageElo(2);
	}

	public Match(List<Member> team1, List<Member> team2){
		this.team1 = team1;
		this.team2 = team2;

		team1Avg = getAverageElo(1);
		team2Avg = getAverageElo(2);
	}

	public Message makeInvitationMessage() {
		Button acceptMatch = Button.success("accept-button", "Accept Match");
		Button declineMatch = Button.danger("decline-button", "Decline Match");
		Message msg = new MessageBuilder()
				.append("**Match Found**\nTeam 1: " + team1.get(0).getAsMention() + " "
						+ team1.get(1).getAsMention() + " | AVG Elo: " + getAverageElo(1) + " \n" + "Team 2: " + team2.get(0).getAsMention() + " "
						+ team2.get(1).getAsMention() + " | AVG Elo: " + getAverageElo(2))
				.setActionRows(ActionRow.of(acceptMatch, declineMatch)).build();

		return msg;
	}

	public Message makeWhoWonMessage() {
		Button team1Button = Button.primary("team1-button", team1.get(0).getUser().getName() + " & " + team1.get(1).getUser().getName());
		Button team2Button = Button.danger("team2-button", team2.get(0).getUser().getName() + " & " + team2.get(1).getUser().getName());
		Button adminSolveMatch = Button.secondary("calladmin-button","Call an Admin");
		Message msg = new MessageBuilder().append("Select who won the match " + team1.get(0).getAsMention() + " " + team1.get(1).getAsMention() +
				" " + team2.get(0).getAsMention() + " " + team2.get(1).getAsMention())
				.setActionRows(ActionRow.of(team1Button,team2Button,adminSolveMatch)).build();

		return msg;
	}

	public Message makeInvitationMessageTest() {
		Button test = Button.success("test-button", "Accept Match");

		Message msg = new MessageBuilder().append("test").setActionRows(ActionRow.of(test)).build();
		return msg;
	}

	public List<Member> getTeam1() {
		return team1;
	}

	public List<Member> getTeam2() {
		return team2;
	}

	public int getMatchID() {
		return id;
	}

	public boolean checkIfMatchIsReady() {
		System.out.println(player1);
		System.out.println(player2);
		System.out.println(player3);
		System.out.println(player4);

		if (player1 && player2 && player3 && player4)
			return true;
		return false;
	}

	public int getAverageElo(int team){
		playerDAO playerDAO = new playerDAO();
		playerVO playerVO = new playerVO();
		if (team == 1){
			int avg;
			playerVO = playerDAO.searchPlayer(team1.get(0).getIdLong());
			avg = playerVO.getElo();
			playerVO = playerDAO.searchPlayer(team1.get(1).getIdLong());
			avg = (avg + playerVO.getElo()) / 2;
			return avg;
		}

		if (team == 2){
			int avg;
			playerVO = playerDAO.searchPlayer(team2.get(0).getIdLong());
			avg = playerVO.getElo();
			playerVO = playerDAO.searchPlayer(team2.get(1).getIdLong());
			avg = (avg + playerVO.getElo()) / 2;
			return avg;
		}
		return 0;
	}

	public float getProbability(float rating1,float rating2){
		return 1.0f / (1 + (float)(Math.pow(10,(rating1 - rating2) / 500)));
	}

	public int getNewRatingTeam1(boolean result,int k){
		float team1Chance = getProbability(team2Avg,team1Avg);

		int newRating = 0;
		if (result){
			newRating = (int) (team1Avg + k * (1 - team1Chance));
			return newRating - team1Avg;
		}
		newRating = (int) (team1Avg + k * (0 - team1Chance));
		return newRating - team1Avg;
	}

	public int getNewRatingTeam2(boolean result,int k){
		float team2Chance = getProbability(team1Avg,team2Avg);

		int newRating = 0;
		if (result){
			newRating = (int) (team2Avg + k * (1 - team2Chance));
			return newRating - team2Avg;
		}
		newRating = (int) (team2Avg + k * (0 - team2Chance));
		return newRating - team2Avg;
	}

	public int getNewRatingDodger(long playerID,int k){
		for (Member team1Member : team1){
			if (team1Member.getIdLong() == playerID)
				return getNewRatingTeam1(false,k);
		}
		for (Member team2Member : team2){
			if (team2Member.getIdLong() == playerID)
				return getNewRatingTeam1(false,k);
		}
		return 0;
	}

	public void setPlayer1Ready() {
		this.player1 = true;
	}
	public void setPlayer2Ready() {
		this.player2 = true;
	}
	public void setPlayer3Ready() {
		this.player3 = true;
	}
	public void setPlayer4Ready() {
		this.player4 = true;
	}
	public void setMessageId(String id) {
		this.messageID = id;
	}
	public String getMessageId() {
		return messageID;
	}
	public int getWhoWon(int vote){
		if (vote == 1)
			team1Won++;
		if (vote == 2)
			team2Won++;

		if (team1Won == 3)
			return 1;
		if (team2Won == 3)
			return 2;
		if (team1Won == 2 && team2Won == 2)
			return 3;

		return 0;
	}

	public boolean getsIfVoted(String id){
		for (int i = 0;i < playersVotes.size();i++){
			if (playersVotes.get(i).getId().equals(id)){
				playersVotes.remove(i);
				return true;
			}
		}
		return false;
	}
}
