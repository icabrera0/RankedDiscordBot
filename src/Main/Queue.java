package Main;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import model.dao.playerDAO;
import model.vo.playerVO;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Button;

import javax.crypto.ExemptionMechanismException;

public class Queue extends ListenerAdapter {

	public final String PREFIX = "fc";
	public static boolean queueOpened = false;
	public static List<Member> queueUsers = new ArrayList<>();
	public static Matches matches = new Matches();
	Timer timer;
	public final int MAXTRIES = 30;
	public int currentTries;

	public final static String QUEUECHANNEL = "993561598146904195";
	public final static String MATCHESCHANNEL = "988800427627790356";
	public final static String COMMANDSCHANNEL = "988888289782165574";
	public final static String BOTSPAMCHANNEL = "989684276578648095";

	public String adminPlayerIdForMatch;

	public MessageEmbed queueMsg;
	public EmbedBuilder queueMsgEmbed;
	public MessageEmbed leaderboardMsg;
	public EmbedBuilder leaderboardMsgEmbed;
	public long queueMsgID;
	public long leaderboardMsgID;
	public int KFORLOSS = 50;
	public int KFORWIN = 50;
	Guild guild = null;

	public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
		String[] args = event.getMessage().getContentRaw().split(" ");

		if (args[0].equalsIgnoreCase(PREFIX + "cqm")){
			Role role = event.getGuild().getRolesByName("Bot Admin", false).get(0);
			if (event.getMember().getRoles().contains(role)){
				Button joinQueue = Button.primary("joinQ-button","Join Queue");
				Button leaveQueue = Button.danger("leaveQ-button","Leave Queue");
				event.getGuild().getTextChannelById(QUEUECHANNEL).deleteMessageById(event.getMessageId()).queue();


				queueMsgEmbed = new EmbedBuilder();
				queueMsgEmbed.setThumbnail(event.getGuild().getIconUrl());
				queueMsgEmbed.setColor(Color.MAGENTA);

				queueMsg = queueMsgEmbed.build();
				Message queueMessage = new MessageBuilder()
						.setEmbed(queueMsg)
						.setActionRows(ActionRow.of(joinQueue,leaveQueue))
						.build();
				event.getGuild().getTextChannelById(QUEUECHANNEL).sendMessage(queueMessage).queue();
			}
			else {
				event.getGuild().getTextChannelById(COMMANDSCHANNEL).sendMessage("Only people with " + role.getAsMention()
						+ " can use this command").queue(m -> m.delete().queueAfter(5, TimeUnit.SECONDS));
			}
		}

		if (args[0].equalsIgnoreCase(PREFIX + "forcematch")){
			Role role = event.getGuild().getRolesByName("Bot Admin", false).get(0);
			if (event.getMember().getRoles().contains(role)){
				if (args.length == 5){
					List<Member> team1List = event.getMessage().getMentionedMembers().subList(0,1);
					List<Member> team2List = event.getMessage().getMentionedMembers().subList(2,3);

					Match match = new Match(team1List,team2List);

					for (Member team1Member : match.getTeam1()) {
						playerDAO playerDAO = new playerDAO();

						playerDAO.incrementWin(Long.parseLong(team1Member.getId()));
						playerDAO.incrementCurrentWinstreak(Long.parseLong(team1Member.getId()));
						playerDAO.addElo(team1Member.getIdLong(),match.getNewRatingTeam1(true,KFORWIN));
						playerVO player = playerDAO.searchPlayer(Long.parseLong(team1Member.getId()));
						if (player.getCurrentwinstreak() > player.getHighestwinstreak()) {
							playerDAO.incrementHighestWinstreak(Long.parseLong(team1Member.getId()));
						}
					}

					for (Member team2Member : match.getTeam2()) {
						playerDAO playerDAO = new playerDAO();

						playerDAO.subtractElo(team2Member.getIdLong(),match.getNewRatingTeam2(false,KFORLOSS));
						playerDAO.incrementLose(Long.parseLong(team2Member.getId()));
						playerDAO.resetWinstreak(team2Member.getIdLong());
					}

					EmbedBuilder matchInvitation = new EmbedBuilder();

					matchInvitation.setAuthor("2v2 Ranked Match Info", event.getJDA().getSelfUser().getAvatarUrl());
					matchInvitation.setThumbnail(event.getGuild().getIconUrl());
					matchInvitation.setColor(Color.MAGENTA);
					matchInvitation.addField("Team 1 won | ELO: +" + match.getNewRatingTeam1(true,KFORWIN),
							match.getTeam1().get(0).getAsMention() + " || " + match.getTeam1().get(1).getAsMention(), false);
					matchInvitation.addField("Team 2 lost | ELO: " + match.getNewRatingTeam2(false,KFORLOSS),
							match.getTeam2().get(0).getAsMention() + " || " + match.getTeam2().get(1).getAsMention(), false);
					event.getGuild().getTextChannelById(MATCHESCHANNEL).sendMessage(matchInvitation.build()).queue();

					matches.removeMatchByUserId(event.getMember().getId());
					event.getGuild().getTextChannelById(QUEUECHANNEL).editMessageById(queueMsgID,updateQueueEmbed().build()).queue();
					event.getGuild().getTextChannelById(QUEUECHANNEL).editMessageById(leaderboardMsgID,updateLeaderboardEmbed().build()).queue();
				}
				else {
					event.getGuild().getTextChannelById(COMMANDSCHANNEL).sendMessage("Command has to follow the next syntax:\n **forcematch @Team1Member1 @Team1Member2 @Team2Member1 @Team2Member2**").queue();
				}

			}
			else {
				event.getGuild().getTextChannelById(COMMANDSCHANNEL).sendMessage("Only people with " + role.getAsMention() +
						" can use this command").queue(message -> message.delete().queueAfter(5,TimeUnit.MINUTES));
			}
		}

		if (args[0].equalsIgnoreCase(PREFIX + "cld")){
			guild = event.getGuild();
			Role role = event.getGuild().getRolesByName("Bot Admin",false).get(0);
			event.getGuild().getTextChannelById(QUEUECHANNEL).deleteMessageById(event.getMessageId()).queue();
			Button nextPage = Button.primary("top12leaderboard",Emoji.fromUnicode("◀"));
			Button beforePage = Button.primary("bozosboard",Emoji.fromUnicode("▶"));
			if (event.getMember().getRoles().contains(role)){
				playerDAO playerDAO = new playerDAO();
				List<playerVO> players = playerDAO.getPlayersLeaderboard(12);
				List<playerVO> playersFooter = playerDAO.getPlayers(1000);


				leaderboardMsgEmbed = new EmbedBuilder();

				leaderboardMsgEmbed.setAuthor("2v2 Champion Leaderboard", event.getJDA().getSelfUser().getAvatarUrl());
				leaderboardMsgEmbed.setThumbnail(event.getGuild().getMemberById(players.get(0).getPlayerID()).getUser().getAvatarUrl());
				leaderboardMsgEmbed.setColor(Color.MAGENTA);
				leaderboardMsgEmbed.setFooter("Current players: " + playersFooter.size());

				for (int i = 0; i < players.size(); i++) {
					Member memberTag = event.getGuild().getMemberById(players.get(i).getPlayerID());
					leaderboardMsgEmbed.addField("Rank " + (i + 1) + " | ELO: " + players.get(i).getElo(),memberTag.getAsMention() + players.get(i).toString(), true);
				}
				leaderboardMsg = leaderboardMsgEmbed.build();
				Message leaderboardMessage = new MessageBuilder()
						.setEmbed(leaderboardMsg)
						.setActionRows(ActionRow.of(nextPage,beforePage))
						.build();
				event.getGuild().getTextChannelById(QUEUECHANNEL).sendMessage(leaderboardMessage).queue();
			}
			else {
				event.getGuild().getTextChannelById(QUEUECHANNEL).sendMessage("Only people with " + role.getAsMention()
						+ " can use this command").queue(m -> m.delete().queueAfter(5, TimeUnit.SECONDS));
			}
		}

		if (args[0].equalsIgnoreCase(PREFIX + "prime")){
			Role role = event.getGuild().getRolesByName("Bot Admin",false).get(0);
			if (event.getMember().getRoles().contains(role)){
				KFORLOSS = 65;
				KFORWIN = 75;
			}
			else{
				event.getGuild().getTextChannelById(COMMANDSCHANNEL).sendMessage("Only people with " + role.getAsMention()
						+ " can use this command").queue(m -> m.delete().queueAfter(5, TimeUnit.SECONDS));
			}
		}

		if (args[0].equalsIgnoreCase(PREFIX + "primestop")){
			Role role = event.getGuild().getRolesByName("Bot Admin",false).get(0);
			if (event.getMember().getRoles().contains(role)){
				KFORLOSS = 50;
				KFORWIN = 50;
			}
			else{
				event.getGuild().getTextChannelById(COMMANDSCHANNEL).sendMessage("Only people with " + role.getAsMention()
						+ " can use this command").queue(m -> m.delete().queueAfter(5, TimeUnit.SECONDS));
			}
		}

		if (args[0].equalsIgnoreCase(PREFIX + "openqueue")) {
			timer = new Timer();
			queueOpened = true;

			timer.schedule(new TimerTask() {
				public void run() {
					try {
						if (queueUsers.size() < 4) {
							System.out.println(queueUsers);
							currentTries++;
							System.out.println(currentTries);
							if (currentTries + 1 == MAXTRIES) {
								for (Member memberInQueue : queueUsers) {
									event.getGuild().getTextChannelById(QUEUECHANNEL).sendMessage(memberInQueue.getAsMention() + " you have been removed from the queue "
											+ "/ Not enough players").queue(m -> m.delete().queueAfter(2, TimeUnit.MINUTES));
								}
								queueUsers.clear();
								event.getGuild().getTextChannelById(QUEUECHANNEL).editMessageById(queueMsgID,updateQueueEmbed().build()).queue();
								event.getGuild().getTextChannelById(BOTSPAMCHANNEL).sendMessage("fcclosequeue").queue(m -> m.delete().queueAfter(5, TimeUnit.SECONDS));
							}
							return;
						}
					} catch (Exception e) {
						System.out.println(e);
					}

					System.out.println(queueUsers.toString());

					Match m1 = new Match(queueUsers);
					currentTries = 0;
					matches.addMatch(m1);
					event.getGuild().getTextChannelById(QUEUECHANNEL).editMessageById(queueMsgID,updateQueueEmbed().build()).queue();
					try{
						event.getGuild().getTextChannelById(MATCHESCHANNEL).sendMessage(m1.makeInvitationMessage()).queue();
					}catch (Exception e){
						System.out.println(e);
					}

				}
			}, 0, 30000);

		}

		if (args[0].equalsIgnoreCase(PREFIX + "closequeue")) {
			timer.cancel();
			currentTries = 0;
			queueOpened = false;
			matches.clearMatches();
			queueUsers.clear();
		}

		if (args[0].equalsIgnoreCase(PREFIX + "cancelmatch")){
			Role role = event.getGuild().getRolesByName("Bot Admin",true).get(0);
			if (event.getMember().getRoles().contains(role)){
				matches.removeMatchByUserId(event.getMessage().getMentionedMembers().get(0).getId());
			}
			else{
				event.getChannel().sendMessage("only " + role.getAsMention() + " can use this command");
			}
		}
	}
	private EmbedBuilder updateQueueEmbed(){
		//String ids = "";
		queueMsgEmbed.clearFields();
		queueMsgEmbed.addField("**Fightclub 2v2 Ranked Queue**",
				"``- Use buttons to join/leave queue\n" +
				"- Accept or decline the match invitation\n" +
				"- Every 2 dodges will result in a loss of elo\n" +
				"- Play the match on fightclub server (ft5 format)\n" +
				"- Select who won``",false);
		List<Member> usersInQueue = queueUsers;
		/*for (int i = 0;i < usersInQueue.size();i++){
			ids += "\n<@!"+ usersInQueue.get(i).getId() + ">";
		}*/
		queueMsgEmbed.addField("**Players in Queue**","``" + usersInQueue.size() + "``",false);

		String matchesPrint = "";
		for (int i = 0;i < matches.size();i++){
			matchesPrint += "<@!"+ matches.get(i).getTeam1().get(0).getId() + ">" + " & " + "<@!"+ matches.get(i).getTeam1().get(1).getId() + ">"
			+ "\nVS\n" + "<@!"+ matches.get(i).getTeam2().get(0).getId() + ">" + " & " + "<@!"+ matches.get(i).getTeam2().get(1).getId() + ">"
			+ "\n ------------------------ \n";
		}
		queueMsgEmbed.addField("**Current Matches**",matchesPrint,false);
		return queueMsgEmbed;
	}

	private EmbedBuilder updateLeaderboardEmbed(){
		playerDAO playerDAO = new playerDAO();
		List<playerVO> players = playerDAO.getPlayersLeaderboard(12);
		List<playerVO> playersFooter = playerDAO.getPlayers(1000);


		EmbedBuilder leaderboard = new EmbedBuilder();

		leaderboard.setAuthor("2v2 Champion Leaderboard");
		leaderboard.setThumbnail(guild.getMemberById(players.get(0).getPlayerID()).getUser().getAvatarUrl());
		leaderboard.setColor(Color.MAGENTA);
		leaderboard.setFooter("Current players: " + playersFooter.size());

		for (int i = 0; i < players.size(); i++) {
			User memberTag = null;
			memberTag = guild.getMemberById(players.get(i).getPlayerID()).getUser();
			if (memberTag != null){
				leaderboard.addField("Rank " + (i + 1) + " | ELO: " + players.get(i).getElo(),memberTag.getAsMention() + players.get(i).toString(), true);
			}
		}
		return leaderboard;
	}

	private EmbedBuilder updateBozoBoard(){
		playerDAO playerDAO = new playerDAO();
		List<playerVO> players = playerDAO.getPlayersLeaderboardBozos(12);
		List<playerVO> playersFooter = playerDAO.getPlayers(1000);

		EmbedBuilder leaderboard = new EmbedBuilder();

		leaderboard.setAuthor("2v2 Bozos Leaderboard");
		try{
			leaderboard.setThumbnail(guild.getMemberById(players.get(0).getPlayerID()).getUser().getAvatarUrl());
		}catch(Exception e) {
			playerDAO.deletePlayer(players.get(0).getPlayerID());
		}
		leaderboard.setColor(Color.MAGENTA);
		leaderboard.setFooter("Current players: " + playersFooter.size());

		for (int i = 0; i < players.size(); i++) {
			User memberTag = null;
			try{
				memberTag = guild.getMemberById(players.get(i).getPlayerID()).getUser();
			}catch(Exception e){
				playerDAO.deletePlayer(players.get(i).getPlayerID());
			}
			if (memberTag != null){
				leaderboard.addField("Rank " + (i + 1) + " | ELO: " + players.get(i).getElo(),memberTag.getAsMention() + players.get(i).toString(), true);
			}
			//memberTag = guild.getMemberById(players.get(i).getPlayerID());

		}
		return leaderboard;
	}


	public void onButtonClick(ButtonClickEvent event) {

		if (event.getButton().getId().equals("top12leaderboard")){
			leaderboardMsgID = event.getMessageIdLong();
			event.getGuild().getTextChannelById(QUEUECHANNEL).editMessageById(leaderboardMsgID,updateLeaderboardEmbed().build()).queue();
			event.deferEdit().queue();
		}

		if (event.getButton().getId().equals("bozosboard")){
			leaderboardMsgID = event.getMessageIdLong();
			event.getGuild().getTextChannelById(QUEUECHANNEL).editMessageById(leaderboardMsgID,updateBozoBoard().build()).queue();
			event.deferEdit().queue();
		}

		if (event.getButton().getId().equals("joinQ-button")){
			queueMsgID = event.getMessageIdLong();
			currentTries = 0;
			boolean isAlreadyInQueue = false;

			playerDAO playerDAO = new playerDAO();
			playerVO player = new playerVO(Long.parseLong(event.getMember().getId()));

			if (playerDAO.searchPlayer(player.getPlayerID()) == null) {
				playerDAO.insertPlayer(player);
			}

			if (!queueOpened) {
				event.getGuild().getTextChannelById(BOTSPAMCHANNEL).sendMessage("fcopenqueue")
						.queue(m -> m.delete().queueAfter(5, TimeUnit.SECONDS));
			}
			for (Member member : queueUsers) {
				if (member.getId().equals(event.getMember().getId())) {
					event.reply("You are already in queue").setEphemeral(true).queue();
					isAlreadyInQueue = true;
				}
			}
			if (!isAlreadyInQueue) {
				if (!matches.playerIsInAMatch(event.getMember().getId())){
					queueUsers.add(event.getMember());
					event.getGuild().getTextChannelById(QUEUECHANNEL).editMessageById(event.getMessageIdLong(),updateQueueEmbed().build()).queue();
					event.deferEdit().queue();
				}
				else{
					event.reply(event.getMember().getAsMention() + " you are already in a match bozo").setEphemeral(true).queue();
				}
			}
		}

		if (event.getButton().getId().equals("leaveQ-button")){
			Member memberToRemove = event.getMember();
			for (int i = 0;i < queueUsers.size();i++){
				if (queueUsers.get(i).getIdLong() == memberToRemove.getIdLong())
					queueUsers.remove(i);
			}
			event.getGuild().getTextChannelById(QUEUECHANNEL).editMessageById(event.getMessageIdLong(),updateQueueEmbed().build()).queue();
			event.deferEdit().queue();
		}

		if (event.getButton().getId().equals("accept-button")) {
			List<Member> playersMentioned = event.getMessage().getMentionedMembers();
			int matchID = matches.getMatchByUserId(playersMentioned.get(0).getId()).getMatchID();

			if (matches.playerIsInAMatch(event.getMember().getId()) && matchID == matches.getMatchByUserId(event.getMember().getId()).getMatchID()) {
				Match match = matches.getMatchByUserId(event.getMember().getId());

					if (match.getTeam1().get(0).getId().equals(event.getMember().getId())) {
						match.setPlayer1Ready();
						System.out.println("player 1 is ready");
						event.reply("Waiting for members to accept").setEphemeral(true).queue();
					}
					if (match.getTeam1().get(1).getId().equals(event.getMember().getId())) {
						match.setPlayer2Ready();
						System.out.println("player 2 is ready");
						event.reply("Waiting for members to accept").setEphemeral(true).queue();
					}
					if (match.getTeam2().get(0).getId().equals(event.getMember().getId())) {
						match.setPlayer3Ready();
						System.out.println("player 3 is ready");
						event.reply("Waiting for members to accept").setEphemeral(true).queue();
					}
					if (match.getTeam2().get(1).getId().equals(event.getMember().getId())) {
						match.setPlayer4Ready();
						System.out.println("player 4 is ready");
						event.reply("Waiting for members to accept").setEphemeral(true).queue();
					}
					if (match.checkIfMatchIsReady()) {
						event.getGuild().getTextChannelById(MATCHESCHANNEL).deleteMessageById(event.getMessageIdLong()).queue();
						event.getGuild().getTextChannelById(MATCHESCHANNEL).sendMessage(match.makeWhoWonMessage()).queue(m -> m.delete().queueAfter(20, TimeUnit.MINUTES));

					}
			}
			else {
				event.reply("You are not part of this match bozo").setEphemeral(true).queue();
			}
		}

		if (event.getButton().getId().equals("decline-button")) {
			playerDAO playerDAO = new playerDAO();
			playerVO playerVO = playerDAO.searchPlayer(event.getMember().getIdLong());

			List<Member> playersMentioned = event.getMessage().getMentionedMembers();
			int matchID = matches.getMatchByUserId(playersMentioned.get(0).getId()).getMatchID();
			if (matches.playerIsInAMatch(event.getMember().getId()) && matchID == matches.getMatchByUserId(event.getMember().getId()).getMatchID()) {
				Match match = matches.getMatchByUserId(event.getMember().getId());
				event.getGuild().getTextChannelById(MATCHESCHANNEL).sendMessage(match.getTeam1().get(0).getAsMention() + " " + match.getTeam1().get(1).getAsMention()
						+ match.getTeam2().get(0).getAsMention() + " " + match.getTeam2().get(1).getAsMention()
						+ "\n"
						+ "The match has been cancelled").queue(m -> m.delete().queueAfter(2, TimeUnit.MINUTES));
				event.getGuild().getTextChannelById(MATCHESCHANNEL).deleteMessageById(event.getMessageIdLong()).queue();

				playerDAO.just4FunIsInYourTeam(playerVO.getPlayerID());
				//playerDAO.subtractElo(playerVO.getPlayerID(),match.getNewRatingDodger(playerVO.getPlayerID(),KFORLOSS));

				if (playerVO.getDodges() % 2 == 0 && playerVO.getDodges() != 0){
					playerDAO.dodgeElo(playerVO.getPlayerID());
					playerDAO.incrementLose(playerVO.getPlayerID());
				}


				matches.removeMatchByUserId(event.getMember().getId());
				event.getGuild().getTextChannelById(QUEUECHANNEL).editMessageById(queueMsgID,updateQueueEmbed().build()).queue();
			}
			else {
				event.reply("You are not part of this match bozo").setEphemeral(true).queue();
			}
		}

		if (event.getButton().getId().equals("team1-button")) {
			List<Member> playersMentioned = event.getMessage().getMentionedMembers();
			int matchID = matches.getMatchByUserId(playersMentioned.get(0).getId()).getMatchID();
			if (matches.playerIsInAMatch(event.getMember().getId()) && matchID == matches.getMatchByUserId(event.getMember().getId()).getMatchID()) {
				Match match = matches.getMatchByUserId(event.getMember().getId());
				if (match.getsIfVoted(event.getMember().getId())){
					if (match.getWhoWon(1) == 1){
						event.getGuild().getTextChannelById(MATCHESCHANNEL).deleteMessageById(event.getMessageIdLong()).queue();

						for (Member team1Member : match.getTeam1()) {
							playerDAO playerDAO = new playerDAO();

							playerDAO.incrementWin(Long.parseLong(team1Member.getId()));
							playerDAO.incrementCurrentWinstreak(Long.parseLong(team1Member.getId()));
							playerDAO.addElo(team1Member.getIdLong(),match.getNewRatingTeam1(true,KFORWIN));
							playerVO player = playerDAO.searchPlayer(Long.parseLong(team1Member.getId()));
							if (player.getCurrentwinstreak() > player.getHighestwinstreak()) {
								playerDAO.incrementHighestWinstreak(Long.parseLong(team1Member.getId()));
							}
						}

						for (Member team2Member : match.getTeam2()) {
							playerDAO playerDAO = new playerDAO();

							playerDAO.subtractElo(team2Member.getIdLong(),match.getNewRatingTeam2(false,KFORLOSS));
							playerDAO.incrementLose(Long.parseLong(team2Member.getId()));
							playerDAO.resetWinstreak(team2Member.getIdLong());
						}

						EmbedBuilder matchInvitation = new EmbedBuilder();

						matchInvitation.setAuthor("2v2 Ranked Match Info", event.getJDA().getSelfUser().getAvatarUrl());
						matchInvitation.setThumbnail(event.getGuild().getIconUrl());
						matchInvitation.setColor(Color.MAGENTA);
						matchInvitation.addField("Team 1 won | ELO: +" + match.getNewRatingTeam1(true,KFORWIN),
								match.getTeam1().get(0).getAsMention() + " || " + match.getTeam1().get(1).getAsMention(), false);
						matchInvitation.addField("Team 2 lost | ELO: " + match.getNewRatingTeam2(false,KFORLOSS),
								match.getTeam2().get(0).getAsMention() + " || " + match.getTeam2().get(1).getAsMention(), false);
						event.getGuild().getTextChannelById(MATCHESCHANNEL).sendMessage(matchInvitation.build()).queue();

						matches.removeMatchByUserId(event.getMember().getId());
						event.getGuild().getTextChannelById(QUEUECHANNEL).editMessageById(queueMsgID,updateQueueEmbed().build()).queue();
						event.getGuild().getTextChannelById(QUEUECHANNEL).editMessageById(leaderboardMsgID,updateLeaderboardEmbed().build()).queue();
					}
					else if (match.getWhoWon(0) == 0){
						event.reply("Waiting for other players to select who won").setEphemeral(true).queue();
					}
					else if (match.getWhoWon(0) == 3){
						event.getGuild().getTextChannelById(MATCHESCHANNEL).deleteMessageById(event.getMessageIdLong()).queue();
						event.getGuild().getTextChannelById(MATCHESCHANNEL).sendMessage(match.getTeam1().get(0).getAsMention() + " " + match.getTeam1().get(1).getAsMention() +
								" " + match.getTeam2().get(0).getAsMention() + " " + match.getTeam2().get(1).getAsMention() + "\n ** Your match hasn't been resolved. One admin will do it ** ").queue();

						adminPlayerIdForMatch = event.getMember().getId();
						Button adminTeam1Won = Button.primary("adminTeam1Won",match.getTeam1().get(0).getUser().getName() + " & " + match.getTeam1().get(1).getUser().getName());
						Button adminTeam2Won = Button.danger("adminTeam2Won",match.getTeam2().get(0).getUser().getName() + " & " + match.getTeam2().get(1).getUser().getName());
						Button adminDraw = Button.secondary("adminDraw","Tie Match");

						Message msg = new MessageBuilder()
								.append(event.getGuild().getRolesByName("Bot Admin",false).get(0).getAsMention() + " Decide who won:")
								.append("\n team 1: " + match.getTeam1().get(0).getAsMention() + " & " + match.getTeam1().get(1).getAsMention())
								.append("\n team 2: " + match.getTeam2().get(0).getAsMention() + " & " + match.getTeam2().get(1).getAsMention())
								.setActionRows(ActionRow.of(adminTeam1Won,adminTeam2Won,adminDraw))
								.build();

						event.getGuild().getTextChannelById(BOTSPAMCHANNEL).sendMessage(msg).queue();
					}
				}
				else {
					event.reply("You already voted").setEphemeral(true).queue();
				}
			}
			else {
				event.reply("You are not part of this match bozo").setEphemeral(true).queue();
			}
		}

		if (event.getButton().getId().equals("team2-button")) {
			List<Member> playersMentioned = event.getMessage().getMentionedMembers();
			int matchID = matches.getMatchByUserId(playersMentioned.get(0).getId()).getMatchID();
			if (matches.playerIsInAMatch(event.getMember().getId()) && matchID == matches.getMatchByUserId(event.getMember().getId()).getMatchID()) {
				Match match = matches.getMatchByUserId(event.getMember().getId());
				if (match.getsIfVoted(event.getMember().getId()) == true){
					if (match.getWhoWon(2) == 2){
						event.getGuild().getTextChannelById(MATCHESCHANNEL).deleteMessageById(event.getMessageIdLong()).queue();

						for (Member team2Member : match.getTeam2()) {
							playerDAO playerDAO = new playerDAO();

							playerDAO.incrementWin(Long.parseLong(team2Member.getId()));
							playerDAO.incrementCurrentWinstreak(Long.parseLong(team2Member.getId()));
							playerDAO.addElo(team2Member.getIdLong(),match.getNewRatingTeam2(true,KFORWIN));
							playerVO player = playerDAO.searchPlayer(Long.parseLong(team2Member.getId()));
							if (player.getCurrentwinstreak() > player.getHighestwinstreak()) {
								playerDAO.incrementHighestWinstreak(Long.parseLong(team2Member.getId()));
							}
						}

						for (Member team1Member : match.getTeam1()) {
							playerDAO playerDAO = new playerDAO();
							playerDAO.subtractElo(team1Member.getIdLong(),match.getNewRatingTeam1(false,KFORLOSS));
							playerDAO.incrementLose(Long.parseLong(team1Member.getId()));
							playerDAO.resetWinstreak(team1Member.getIdLong());
						}

						EmbedBuilder matchInvitation = new EmbedBuilder();

						matchInvitation.setAuthor("2v2 Ranked Match Info", event.getJDA().getSelfUser().getAvatarUrl());
						matchInvitation.setThumbnail(event.getGuild().getIconUrl());
						matchInvitation.setColor(Color.MAGENTA);
						matchInvitation.addField("Team 2 won | ELO: +" + match.getNewRatingTeam2(true,KFORWIN),
								match.getTeam2().get(0).getAsMention() + " || " + match.getTeam2().get(1).getAsMention(), false);
						matchInvitation.addField("Team 1 lost | ELO: " + match.getNewRatingTeam1(false,KFORLOSS),
								match.getTeam1().get(0).getAsMention() + " || " + match.getTeam1().get(1).getAsMention(), false);
						event.getGuild().getTextChannelById(MATCHESCHANNEL).sendMessage(matchInvitation.build()).queue();

						matches.removeMatchByUserId(event.getMember().getId());
						event.getGuild().getTextChannelById(QUEUECHANNEL).editMessageById(queueMsgID,updateQueueEmbed().build()).queue();
						event.getGuild().getTextChannelById(QUEUECHANNEL).editMessageById(leaderboardMsgID,updateLeaderboardEmbed().build()).queue();
					}
					else if (match.getWhoWon(0) == 0){
						event.reply("Waiting for other players to select who won").setEphemeral(true).queue();
					}
					else if (match.getWhoWon(0) == 3){
						event.getGuild().getTextChannelById(MATCHESCHANNEL).deleteMessageById(event.getMessageIdLong()).queue();
						event.getGuild().getTextChannelById(MATCHESCHANNEL).sendMessage(match.getTeam1().get(0).getAsMention() + " " + match.getTeam1().get(1).getAsMention() +
								" " + match.getTeam2().get(0).getAsMention() + " " + match.getTeam2().get(1).getAsMention() + "\n ** Your match hasn't been resolved. One admin will do it ** ").queue();

						adminPlayerIdForMatch = event.getMember().getId();
						Button adminTeam1Won = Button.primary("adminTeam1Won",match.getTeam1().get(0).getUser().getName() + " & " + match.getTeam1().get(1).getUser().getName());
						Button adminTeam2Won = Button.danger("adminTeam2Won",match.getTeam2().get(0).getUser().getName() + " & " + match.getTeam2().get(1).getUser().getName());
						Button adminDraw = Button.secondary("adminDraw","Tie Match");

						Message msg = new MessageBuilder()
								.append(event.getGuild().getRolesByName("Bot Admin",false).get(0).getAsMention() + " Decide who won:")
								.append("\n team 1: " + match.getTeam1().get(0).getAsMention() + " & " + match.getTeam1().get(1).getAsMention())
								.append("\n team 2: " + match.getTeam2().get(0).getAsMention() + " & " + match.getTeam2().get(1).getAsMention())
								.setActionRows(ActionRow.of(adminTeam1Won,adminTeam2Won,adminDraw))
								.build();

						event.getGuild().getTextChannelById(BOTSPAMCHANNEL).sendMessage(msg).queue();
					}
				}
				else {
					event.reply("You already voted").setEphemeral(true).queue();
				}
			}
			else {
				event.reply("You are not part of this match bozo").setEphemeral(true).queue();
			}
		}

		if (event.getButton().getId().equals(("calladmin-button"))){
			List<Member> playersMentioned = event.getMessage().getMentionedMembers();
			int matchID = matches.getMatchByUserId(playersMentioned.get(0).getId()).getMatchID();
			if (matches.playerIsInAMatch(event.getMember().getId()) && matchID == matches.getMatchByUserId(event.getMember().getId()).getMatchID()) {
				Match match = matches.getMatchByUserId(event.getMember().getId());

				event.getGuild().getTextChannelById(MATCHESCHANNEL).deleteMessageById(event.getMessageIdLong()).queue();
				event.getGuild().getTextChannelById(MATCHESCHANNEL).sendMessage(match.getTeam1().get(0).getAsMention() + " " + match.getTeam1().get(1).getAsMention() +
						" " + match.getTeam2().get(0).getAsMention() + " " + match.getTeam2().get(1).getAsMention() + "\n ** Your match hasn't been resolved. One admin will do it ** ").queue();

				adminPlayerIdForMatch = event.getMember().getId();
				Button adminTeam1Won = Button.primary("adminTeam1Won",match.getTeam1().get(0).getUser().getName() + " & " + match.getTeam1().get(1).getUser().getName());
				Button adminTeam2Won = Button.danger("adminTeam2Won",match.getTeam2().get(0).getUser().getName() + " & " + match.getTeam2().get(1).getUser().getName());
				Button adminDraw = Button.secondary("adminDraw","Tie Match");

				Message msg = new MessageBuilder()
						.append(event.getGuild().getRolesByName("Bot Admin",false).get(0).getAsMention() + " Decide who won:")
						.append("\n team 1: " + match.getTeam1().get(0).getAsMention() + " & " + match.getTeam1().get(1).getAsMention())
						.append("\n team 2: " + match.getTeam2().get(0).getAsMention() + " & " + match.getTeam2().get(1).getAsMention())
						.setActionRows(ActionRow.of(adminTeam1Won,adminTeam2Won,adminDraw))
						.build();

				event.getGuild().getTextChannelById(BOTSPAMCHANNEL).sendMessage(msg).queue();
			}
			else {
				event.reply("You are not part of this match bozo").setEphemeral(true).queue();
			}

		}

		if (event.getButton().getId().equals("adminTeam1Won")){
			event.getGuild().getTextChannelById(BOTSPAMCHANNEL).deleteMessageById(event.getMessageIdLong()).queue();
			Match match = matches.getMatchByUserId(adminPlayerIdForMatch);

			for (Member team1Member : match.getTeam1()) {
				playerDAO playerDAO = new playerDAO();

				playerDAO.incrementWin(Long.parseLong(team1Member.getId()));
				playerDAO.incrementCurrentWinstreak(Long.parseLong(team1Member.getId()));
				playerDAO.addElo(team1Member.getIdLong(),match.getNewRatingTeam1(true,KFORWIN));
				playerVO player = playerDAO.searchPlayer(Long.parseLong(team1Member.getId()));
				if (player.getCurrentwinstreak() > player.getHighestwinstreak()) {
					playerDAO.incrementHighestWinstreak(Long.parseLong(team1Member.getId()));
				}
			}

			for (Member team2Member : match.getTeam2()) {
				playerDAO playerDAO = new playerDAO();

				playerDAO.subtractElo(team2Member.getIdLong(),match.getNewRatingTeam2(false,KFORLOSS));
				playerDAO.incrementLose(Long.parseLong(team2Member.getId()));
				playerDAO.resetWinstreak(team2Member.getIdLong());
			}

			EmbedBuilder matchInvitation = new EmbedBuilder();

			matchInvitation.setAuthor("2v2 Ranked Match Info", event.getJDA().getSelfUser().getAvatarUrl());
			matchInvitation.setThumbnail(event.getGuild().getIconUrl());
			matchInvitation.setColor(Color.MAGENTA);
			matchInvitation.addField("Team 1 won | ELO: +" + match.getNewRatingTeam1(true,KFORWIN),
					match.getTeam1().get(0).getAsMention() + " || " + match.getTeam1().get(1).getAsMention(), false);
			matchInvitation.addField("Team 2 lost | ELO: " + match.getNewRatingTeam2(false,KFORLOSS),
					match.getTeam2().get(0).getAsMention() + " || " + match.getTeam2().get(1).getAsMention(), false);
			event.getGuild().getTextChannelById(MATCHESCHANNEL).sendMessage(matchInvitation.build()).queue();

			matches.removeMatchByUserId(adminPlayerIdForMatch);
			event.getGuild().getTextChannelById(QUEUECHANNEL).editMessageById(queueMsgID,updateQueueEmbed().build()).queue();
			event.getGuild().getTextChannelById(QUEUECHANNEL).editMessageById(leaderboardMsgID,updateLeaderboardEmbed().build()).queue();
		}

		if (event.getButton().getId().equals("adminTeam2Won")){
			event.getGuild().getTextChannelById(BOTSPAMCHANNEL).deleteMessageById(event.getMessageIdLong()).queue();
			Match match = matches.getMatchByUserId(adminPlayerIdForMatch);


			for (Member team2Member : match.getTeam2()) {
				playerDAO playerDAO = new playerDAO();

				playerDAO.incrementWin(Long.parseLong(team2Member.getId()));
				playerDAO.incrementCurrentWinstreak(Long.parseLong(team2Member.getId()));
				playerDAO.addElo(team2Member.getIdLong(),match.getNewRatingTeam2(true,KFORWIN));
				playerVO player = playerDAO.searchPlayer(Long.parseLong(team2Member.getId()));
				if (player.getCurrentwinstreak() > player.getHighestwinstreak()) {
					playerDAO.incrementHighestWinstreak(Long.parseLong(team2Member.getId()));
				}
			}

			for (Member team1Member : match.getTeam1()) {
				playerDAO playerDAO = new playerDAO();

				playerDAO.subtractElo(team1Member.getIdLong(),match.getNewRatingTeam1(false,KFORLOSS));
				playerDAO.incrementLose(Long.parseLong(team1Member.getId()));
				playerDAO.resetWinstreak(team1Member.getIdLong());
			}

			EmbedBuilder matchInvitation = new EmbedBuilder();

			matchInvitation.setAuthor("2v2 Ranked Match Info", event.getJDA().getSelfUser().getAvatarUrl());
			matchInvitation.setThumbnail(event.getGuild().getIconUrl());
			matchInvitation.setColor(Color.MAGENTA);
			matchInvitation.addField("Team 2 won | ELO: +" + match.getNewRatingTeam2(true,KFORWIN),
					match.getTeam2().get(0).getAsMention() + " || " + match.getTeam2().get(1).getAsMention(), false);
			matchInvitation.addField("Team 1 lost | ELO " + match.getNewRatingTeam1(false,KFORLOSS),
					match.getTeam1().get(0).getAsMention() + " || " + match.getTeam1().get(1).getAsMention(), false);
			event.getGuild().getTextChannelById(MATCHESCHANNEL).sendMessage(matchInvitation.build()).queue();

			matches.removeMatchByUserId(adminPlayerIdForMatch);
			event.getGuild().getTextChannelById(QUEUECHANNEL).editMessageById(queueMsgID,updateQueueEmbed().build()).queue();
			event.getGuild().getTextChannelById(QUEUECHANNEL).editMessageById(leaderboardMsgID,updateLeaderboardEmbed().build()).queue();
		}

		if (event.getButton().getId().equals("adminDraw")){
			event.getGuild().getTextChannelById(BOTSPAMCHANNEL).deleteMessageById(event.getMessageIdLong()).queue();
			Match match = matches.getMatchByUserId(adminPlayerIdForMatch);
			event.getGuild().getTextChannelById(MATCHESCHANNEL).sendMessage(match.getTeam1().get(0).getAsMention() + " " + match.getTeam1().get(1).getAsMention() +
					" " + match.getTeam2().get(0).getAsMention() + " " + match.getTeam2().get(1).getAsMention() + "\n ** Your match has been tied ** ").queue();
			matches.removeMatchByUserId(adminPlayerIdForMatch);
			event.getGuild().getTextChannelById(QUEUECHANNEL).editMessageById(queueMsgID,updateQueueEmbed().build()).queue();
			event.getGuild().getTextChannelById(QUEUECHANNEL).editMessageById(leaderboardMsgID,updateLeaderboardEmbed().build()).queue();
		}
	}
}
