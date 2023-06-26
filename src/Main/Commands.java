package Main;

import java.awt.Color;
import java.util.List;
import java.util.concurrent.TimeUnit;

import model.dao.playerDAO;
import model.vo.playerVO;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Button;

public class Commands extends ListenerAdapter {
	public final String PREFIX = "fc";

	public static List<Member> defendersList;
	public static List<Member> challengerList;
	public final static String MATCHESCHANNEL = "988800427627790356";
	public final static String COMMANDSCHANNEL = "988888289782165574";
	public final static String BOTSPAMCHANNEL = "989684276578648095";

	public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
		String[] args = event.getMessage().getContentRaw().split(" ");

		if (args[0].equalsIgnoreCase(PREFIX + "help")) {
			event.getGuild().getTextChannelById(COMMANDSCHANNEL).deleteMessageById(event.getMessageId()).queue();
			EmbedBuilder api = new EmbedBuilder();

			api.setAuthor("FC Duel Bot Commands", event.getJDA().getSelfUser().getAvatarUrl());
			api.setThumbnail(event.getGuild().getIconUrl());
			api.setColor(Color.MAGENTA);
			api.addField("fcstartMatch @challenger1 @challenger2", "Starts a 2v2 Match (can only be used by"
					+ event.getGuild().getRolesByName("2v2 Champ", false).get(0).getAsMention()+")", false);
			api.addField("fcrank","Displays user's current rank and stats",false);
			api.addField("fcrankof @player","Displays the rank and stats of the tagged player",false);
			api.addField("fcshowrank (number of player)","Displays the rank and stats of the player",false);
			api.addField("fcadminhelp","Only admins can use this command",false);


			event.getGuild().getTextChannelById(COMMANDSCHANNEL).sendMessage(api.build()).queue();
		}

		if (args[0].equalsIgnoreCase(PREFIX + "adminhelp")){
			Role role = event.getGuild().getRolesByName("Bot Admin", false).get(0);
			if (event.getMember().getRoles().contains(role)){
				EmbedBuilder adminApi = new EmbedBuilder();

				adminApi.setAuthor("FC Duel Bot Admin Commands",event.getJDA().getSelfUser().getAvatarUrl());
				adminApi.setThumbnail(event.getGuild().getIconUrl());
				adminApi.setColor(Color.magenta);
				adminApi.addField("fceditwins @player (wins)","Changes current wins of the tagged player for the number introduced",false);
				adminApi.addField("fceditloses @player (loses)","Changes current loses of the tagged player for the number introduced",false);
				adminApi.addField("fceditwinstreak @player (winstreak)","Changes current winstreak of the tagged player for the number introduced",false);
				adminApi.addField("fcedithgwinstreak @player (hgwinstreak)","Changes current highest winstreak of the tagged player for the number introduced",false);
				adminApi.addField("fc+Win @player","Adds a win to the tagged player",false);
				adminApi.addField("fc+Lose @player","Adds a lose to the tagged player",false);
				adminApi.addField("fc-Win @player","Subtracts a win from the tagged player",false);
				adminApi.addField("fc-Lose @player","Substracts a lose from the tagged player",false);
				adminApi.addField("addelo @player (elo)","Add Elo to the tagged player",false);
				adminApi.addField("fc-elo @player (elo)","Substracts elo from the tagged player",false);
				adminApi.addField("fccancelmatch @player","Cancels the current match from the player tagged",false);
				adminApi.addField("fccqm","Creates queue message",false);
				adminApi.addField("fccld","Creates leaderboard message",false);
				adminApi.addField("fcprime","Increases rating gains by 1,5 and losing ratings by 1,25",false);
				adminApi.addField("fcprimestop","Restores by default rating gains",false);
				adminApi.addField("fcsavedb","Saves the database in an external table",false);
				adminApi.addField("fcforcematch @Team1Member1 @Team1Member2 @Team2Member1 @Team2Member2","Gives a win to the first two players mentioned, like if they would have played",false);

				event.getGuild().getTextChannelById(BOTSPAMCHANNEL).sendMessage(event.getMember().getAsMention()).queue();
				event.getGuild().getTextChannelById(BOTSPAMCHANNEL).sendMessage(adminApi.build()).queue();
			}
			else{
				event.getGuild().getTextChannelById(COMMANDSCHANNEL).sendMessage("Only people with " + role.getAsMention()
						+ " can use this command").queue(m -> m.delete().queueAfter(5, TimeUnit.SECONDS));
			}
		}

		if (args[0].equalsIgnoreCase(PREFIX + "savedb")){
			Role role = event.getGuild().getRolesByName("Bot Admin", false).get(0);
			if (event.getMember().getRoles().contains(role)){
				playerDAO playerDAO = new playerDAO();

				playerDAO.savedatabase();
			}
			else{
				event.getGuild().getTextChannelById(COMMANDSCHANNEL).sendMessage("Only people with " + role.getAsMention()
						+ " can use this command").queue(m -> m.delete().queueAfter(5, TimeUnit.SECONDS));
			}
		}
		if (args[0].equalsIgnoreCase(PREFIX + "startMatch")) {
			event.getGuild().getTextChannelById(MATCHESCHANNEL).deleteMessageById(event.getMessageId()).queue();
			Role role = event.getGuild().getRolesByName("2v2 Champ", false).get(0);
			challengerList = event.getMessage().getMentionedMembers();
			defendersList = event.getGuild().getMembersWithRoles(role);
			Button defendersWon = Button.success("defenders-button", "Title Defenders");
			Button challengersWon = Button.danger("challengers-button", "Challengers");

			if (event.getMember().getRoles().contains(role)) {

				for (Member defender : defendersList) {
					playerDAO playerDAO = new playerDAO();
					playerVO player = new playerVO(Long.parseLong(defender.getId()));

					if (playerDAO.searchPlayer(player.getPlayerID()) == null) {
						playerDAO.insertPlayer(player);
					}
				}

				for (Member attacker : challengerList) {
					playerDAO playerDAO = new playerDAO();
					playerVO player = new playerVO(Long.parseLong(attacker.getId()));

					if (playerDAO.searchPlayer(player.getPlayerID()) == null) {
						playerDAO.insertPlayer(player);
					}
				}

				Message msg = new MessageBuilder().append("Select who won the match")
						.setActionRows(ActionRow.of(defendersWon, challengersWon)).build();
				event.getChannel().sendMessage(msg).queue();

			}
			else {
				event.getGuild().getTextChannelById(MATCHESCHANNEL).sendMessage("Only people with " + role.getAsMention()
				+ " can use this command").queue(m -> m.delete().queueAfter(10, TimeUnit.SECONDS));
			}
		}

		if (args[0].equalsIgnoreCase(PREFIX + "addelo")){
			Role role = event.getGuild().getRolesByName("Bot Admin",false).get(0);
			playerDAO playerDAO = new playerDAO();
			Member player = event.getMessage().getMentionedMembers().get(0);

			if (event.getMember().getRoles().contains(role)){
				if (args.length == 3){
					playerDAO.addElo(player.getIdLong(),Integer.parseInt(args[2]));
				}
				else{
					event.getGuild().getTextChannelById(BOTSPAMCHANNEL).sendMessage(event.getMember().getAsMention()).queue();
					event.getGuild().getTextChannelById(BOTSPAMCHANNEL).sendMessage("Command has to follow the next syntax:\n **fcaddelo @player (elo to add)**").queue();
				}
			}
			else{
				event.getChannel().sendMessage(event.getMember().getAsMention() + "only admins can use this command").queue(message -> message.delete().queueAfter(5,TimeUnit.SECONDS));
			}
		}

		if (args[0].equalsIgnoreCase(PREFIX + "-elo")){
			Role role = event.getGuild().getRolesByName("Bot Admin",false).get(0);
			playerDAO playerDAO = new playerDAO();
			Member player = event.getMessage().getMentionedMembers().get(0);

			if (event.getMember().getRoles().contains(role)){
				if (args.length == 3){
					playerDAO.subtractElo(player.getIdLong(),Integer.parseInt(args[2]));
				}
				else{
					event.getGuild().getTextChannelById(BOTSPAMCHANNEL).sendMessage(event.getMember().getAsMention()).queue();
					event.getGuild().getTextChannelById(BOTSPAMCHANNEL).sendMessage("Command has to follow the next syntax:\n **fcsubtract @player (elo to remove)**").queue();
				}
			}
			else{
				event.getChannel().sendMessage(event.getMember().getAsMention() + "only admins can use this command").queue(message -> message.delete().queueAfter(5,TimeUnit.SECONDS));
			}
		}

		if (args[0].equalsIgnoreCase(PREFIX + "+Win")){
			Role role = event.getGuild().getRolesByName("Bot Admin",false).get(0);
			playerDAO playerDAO = new playerDAO();
			Member player = event.getMessage().getMentionedMembers().get(0);

			if (event.getMember().getRoles().contains(role)){
				playerDAO.incrementWin(event.getMember().getIdLong());
			}
			else{
				event.getChannel().sendMessage(event.getMember().getAsMention() + "only admins can use this command").queue(message -> message.delete().queueAfter(5,TimeUnit.SECONDS));
			}
		}

		if (args[0].equalsIgnoreCase(PREFIX + "-Win")){
			Role role = event.getGuild().getRolesByName("Bot Admin",false).get(0);
			playerDAO playerDAO = new playerDAO();
			Member player = event.getMessage().getMentionedMembers().get(0);

			if (event.getMember().getRoles().contains(role)){
				playerDAO.subtractWin(event.getMember().getIdLong());
			}
			else{
				event.getChannel().sendMessage(event.getMember().getAsMention() + "only admins can use this command").queue(message -> message.delete().queueAfter(5,TimeUnit.SECONDS));
			}
		}

		if (args[0].equalsIgnoreCase(PREFIX + "+Lose")){
			Role role = event.getGuild().getRolesByName("Bot Admin",false).get(0);
			playerDAO playerDAO = new playerDAO();
			Member player = event.getMessage().getMentionedMembers().get(0);

			if (event.getMember().getRoles().contains(role)){
				playerDAO.incrementLose(event.getMember().getIdLong());
			}
			else{
				event.getChannel().sendMessage(event.getMember().getAsMention() + "only admins can use this command").queue(message -> message.delete().queueAfter(5,TimeUnit.SECONDS));
			}
		}

		if (args[0].equalsIgnoreCase(PREFIX + "-Lose")){
			Role role = event.getGuild().getRolesByName("Bot Admin",false).get(0);
			playerDAO playerDAO = new playerDAO();
			Member player = event.getMessage().getMentionedMembers().get(0);

			if (event.getMember().getRoles().contains(role)){
				playerDAO.subtractLose(event.getMember().getIdLong());
			}
			else{
				event.getChannel().sendMessage(event.getMember().getAsMention() + "only admins can use this command").queue(message -> message.delete().queueAfter(5,TimeUnit.SECONDS));
			}
		}

		if (args[0].equalsIgnoreCase(PREFIX + "editwins")){
			Role role = event.getGuild().getRolesByName("Bot Admin",false).get(0);
			playerDAO playerDAO = new playerDAO();
			Member player = event.getMessage().getMentionedMembers().get(0);

			if (event.getMember().getRoles().contains(role)){
				if (args.length == 3){
					playerDAO.editWins(player.getIdLong(),args[2]);
				}
				else{
					event.getGuild().getTextChannelById(BOTSPAMCHANNEL).sendMessage(event.getMember().getAsMention()).queue();
					event.getGuild().getTextChannelById(BOTSPAMCHANNEL).sendMessage("Command has to follow the next syntax:\n **fceditwins @player (number of wins)**").queue();
				}
			}
			else{
				event.getChannel().sendMessage(event.getMember().getAsMention() + "only admins can use this command").queue(message -> message.delete().queueAfter(5,TimeUnit.SECONDS));
			}
		}

		if (args[0].equalsIgnoreCase(PREFIX + "editloses")){
			Role role = event.getGuild().getRolesByName("Bot Admin",false).get(0);
			playerDAO playerDAO = new playerDAO();
			Member player = event.getMessage().getMentionedMembers().get(0);

			if (event.getMember().getRoles().contains(role)){
				if (args.length == 3){
					playerDAO.editLoses(player.getIdLong(),args[2]);
				}
				else{
					event.getGuild().getTextChannelById(BOTSPAMCHANNEL).sendMessage(event.getMember().getAsMention()).queue();
					event.getGuild().getTextChannelById(BOTSPAMCHANNEL).sendMessage("Command has to follow the next syntax:\n **fceditloses @player (number of wins)**").queue();
				}
			}
			else{
				event.getChannel().sendMessage(event.getMember().getAsMention() + "only admins can use this command").queue(message -> message.delete().queueAfter(5,TimeUnit.SECONDS));
			}
		}

		if (args[0].equalsIgnoreCase(PREFIX + "editwinstreak")){
			Role role = event.getGuild().getRolesByName("Bot Admin",false).get(0);
			playerDAO playerDAO = new playerDAO();
			Member player = event.getMessage().getMentionedMembers().get(0);

			if (event.getMember().getRoles().contains(role)){
				if (args.length == 3){
					playerDAO.editWinstreak(player.getIdLong(),args[2]);
				}
				else{
					event.getGuild().getTextChannelById(BOTSPAMCHANNEL).sendMessage(event.getMember().getAsMention()).queue();
					event.getGuild().getTextChannelById(BOTSPAMCHANNEL).sendMessage("Command has to follow the next syntax:\n **fceditwinstreak @player (number of wins)**").queue();
				}
			}
			else{
				event.getChannel().sendMessage(event.getMember().getAsMention() + "only admins can use this command").queue(message -> message.delete().queueAfter(5,TimeUnit.SECONDS));
			}
		}

		if (args[0].equalsIgnoreCase(PREFIX + "edithgwinstreak")){
			Role role = event.getGuild().getRolesByName("Bot Admin",false).get(0);
			playerDAO playerDAO = new playerDAO();
			Member player = event.getMessage().getMentionedMembers().get(0);

			if (event.getMember().getRoles().contains(role)){
				if (args.length == 3){
					playerDAO.editHighestWinstreak(player.getIdLong(),args[2]);
				}
				else{
					event.getGuild().getTextChannelById(BOTSPAMCHANNEL).sendMessage(event.getMember().getAsMention()).queue();
					event.getGuild().getTextChannelById(BOTSPAMCHANNEL).sendMessage("Command has to follow the next syntax:\n **fcedithgwinstreak @player (number of wins)**").queue();
				}
			}
			else{
				event.getChannel().sendMessage(event.getMember().getAsMention() + "only admins can use this command").queue(message -> message.delete().queueAfter(5,TimeUnit.SECONDS));
			}
		}

		if (args[0].equalsIgnoreCase(PREFIX + "rank")) {
			event.getGuild().getTextChannelById(COMMANDSCHANNEL).deleteMessageById(event.getMessageId()).queue();
			playerDAO playerDAO = new playerDAO();
			List<playerVO> players = playerDAO.getPlayers(100);
			playerVO player = null;
			Member memberTag = null;

			for (playerVO playerAux : players) {
				if (playerAux.getPlayerID() == event.getMember().getIdLong()){
					player = playerAux;
					memberTag = event.getGuild().getMemberById(player.getPlayerID());
				}
			}

			EmbedBuilder rank = new EmbedBuilder();

			rank.setAuthor("2v2 Rank", event.getJDA().getSelfUser().getAvatarUrl());
			rank.setThumbnail(event.getAuthor().getAvatarUrl());
			rank.setColor(Color.MAGENTA);
			rank.addField("Rank " + player.getRanking() +"/"+ players.size() +  " | ELO: " + player.getElo(),memberTag.getAsMention() + player.toString(), false);
			event.getGuild().getTextChannelById(COMMANDSCHANNEL).sendMessage(rank.build()).queue();
		}

		if (args[0].equalsIgnoreCase(PREFIX + "divisions")){
			event.getGuild().getTextChannelById(BOTSPAMCHANNEL).deleteMessageById(event.getMessageId()).queue();
			Role role = event.getGuild().getRolesByName("Bot Admin",false).get(0);
			String ironIds = "",ceramicIds = "",boneIds = "";
			playerDAO playerDAO = new playerDAO();
			List<playerVO> players = playerDAO.getPlayers(1000);

			if (event.getMember().getRoles().contains(role)){
				EmbedBuilder divisions = new EmbedBuilder();

				for (int i = 0;i <= 11;i++){
					ironIds += "<@!" + players.get(i).getPlayerID() + "> "+ players.get(i).getElo();
				}

				for (int i = 12;i <= 24;i++){
					ceramicIds += "<@!" + players.get(i).getPlayerID() + "> "+ players.get(i).getElo();
				}

				for (int i = 25;i < players.size();i++){
					boneIds += "<@!" + players.get(i).getPlayerID() + "> "+ players.get(i).getElo();
				}

				divisions.setAuthor("2v2 Tourney Divisions");
				divisions.setThumbnail(event.getGuild().getIconUrl());
				divisions.setColor(Color.MAGENTA);
				divisions.addField("**Iron Division**","" + ironIds,false);
				divisions.addField("**Ceramic Division**","" + ceramicIds,false);
				divisions.addField("**Bone Division**","" + boneIds,false);

				event.getGuild().getTextChannelById(BOTSPAMCHANNEL).sendMessage(divisions.build()).queue();
			}
			else{
				event.getChannel().sendMessage(event.getMember().getAsMention() + "only admins can use this command").queue(message -> message.delete().queueAfter(5,TimeUnit.SECONDS));
			}
		}

		if (args[0].equalsIgnoreCase(PREFIX + "showrank")){
			try {
				int rankToShow = Integer.parseInt(args[1]);
				event.getGuild().getTextChannelById(COMMANDSCHANNEL).deleteMessageById(event.getMessageId()).queue();
				playerDAO playerDAO = new playerDAO();
				List<playerVO> players = playerDAO.getPlayers(1000);
				playerVO player = playerDAO.searchPlayer(players.get(rankToShow - 1).getPlayerID());
				Member memberTag = event.getGuild().getMemberById(player.getPlayerID());

				EmbedBuilder rank = new EmbedBuilder();

				rank.setAuthor("2v2 Rank", event.getJDA().getSelfUser().getAvatarUrl());
				rank.setThumbnail(event.getMessage().getMentionedUsers().get(0).getAvatarUrl());
				rank.setColor(Color.MAGENTA);
				rank.addField("Rank " + player.getRanking() +"/"+ players.size() +  " | ELO: " + player.getElo(),memberTag.getAsMention() + player.toString(), false);

				event.getGuild().getTextChannelById(COMMANDSCHANNEL).sendMessage(rank.build()).queue();
			}catch (Exception e){
				event.getGuild().getTextChannelById(COMMANDSCHANNEL).sendMessage("Command has to follow the next syntax:\\n **fcshowrank (rank)**\"");
			}
		}

		if (args[0].equalsIgnoreCase(PREFIX + "rankof")) {
			event.getGuild().getTextChannelById(COMMANDSCHANNEL).deleteMessageById(event.getMessageId()).queue();
			playerDAO playerDAO = new playerDAO();
			List<playerVO> players = playerDAO.getPlayers(100);
			playerVO player = null;
			Member memberTag = null;

			for (playerVO playerAux : players) {
				if (playerAux.getPlayerID() == event.getMessage().getMentionedMembers().get(0).getIdLong()){
					player = playerAux;
					memberTag = event.getGuild().getMemberById(player.getPlayerID());
				}

			}

			EmbedBuilder rank = new EmbedBuilder();

			rank.setAuthor("2v2 Rank", event.getJDA().getSelfUser().getAvatarUrl());
			rank.setThumbnail(event.getMessage().getMentionedUsers().get(0).getAvatarUrl());
			rank.setColor(Color.MAGENTA);
			rank.addField("Rank " + player.getRanking() +"/"+ players.size() +  " | ELO: " + player.getElo(),memberTag.getAsMention() + player.toString(), false);

			event.getGuild().getTextChannelById(COMMANDSCHANNEL).sendMessage(rank.build()).queue();
		}

		if (args[0].equalsIgnoreCase(PREFIX + "adminInsertTest")){
			Role role = event.getGuild().getRolesByName("Bot Admin",false).get(0);
			playerDAO playerDAO = new playerDAO();
			if (event.getMember().getRoles().contains(role)){
				playerVO playerVO = new playerVO(event.getMember().getIdLong());
				playerDAO.insertPlayer(playerVO);
			}
			else{
				event.getChannel().sendMessage(event.getMember().getAsMention() + " you can't use this command").queue(message -> message.delete().queueAfter(5,TimeUnit.SECONDS));
			}
		}

	}

	public void onButtonClick(ButtonClickEvent event) {
		Role role = event.getGuild().getRolesByName("2v2 Champ", false).get(0);

			if (event.getButton().getId().equals("defenders-button")) {
				if (event.getMember().getRoles().contains(role)) {

					event.getGuild().getTextChannelById(MATCHESCHANNEL).deleteMessageById(event.getMessageIdLong()).queue();

				for (Member defender : defendersList) {
					playerDAO playerDAO = new playerDAO();

					playerDAO.incrementWin(Long.parseLong(defender.getId()));
					playerDAO.incrementCurrentWinstreak(Long.parseLong(defender.getId()));
					playerVO player = playerDAO.searchPlayer(Long.parseLong(defender.getId()));
					if (player.getCurrentwinstreak() > player.getHighestwinstreak()) {
						playerDAO.incrementHighestWinstreak(Long.parseLong(defender.getId()));
					}

				}

				for (Member attacker : challengerList) {
					playerDAO playerDAO = new playerDAO();

					playerDAO.incrementLose(Long.parseLong(attacker.getId()));
				}


				EmbedBuilder matchInvitation = new EmbedBuilder();

				matchInvitation.setAuthor("2v2 Champion Match Info", event.getJDA().getSelfUser().getAvatarUrl());
				matchInvitation.setThumbnail(event.getGuild().getIconUrl());
				matchInvitation.setColor(Color.MAGENTA);
				matchInvitation.addField("Defenders won",
						defendersList.get(0).getAsMention() + " || " + defendersList.get(1).getAsMention(), false);
				matchInvitation.addField("Challengers lost",
						challengerList.get(0).getAsMention() + " || " + challengerList.get(1).getAsMention(), false);
				event.getGuild().getTextChannelById(MATCHESCHANNEL).sendMessage(matchInvitation.build()).queue();
				}
			}

			if (event.getButton().getId().equals("challengers-button")) {
				if (event.getMember().getRoles().contains(role)) {
					event.getGuild().getTextChannelById(MATCHESCHANNEL).deleteMessageById(event.getMessageIdLong()).queue();

					for (Member attacker : challengerList) {
						playerDAO playerDAO = new playerDAO();

						playerDAO.incrementWin(Long.parseLong(attacker.getId()));
						playerDAO.incrementCurrentWinstreak(Long.parseLong(attacker.getId()));
						playerVO player = playerDAO.searchPlayer(Long.parseLong(attacker.getId()));
						if (player.getCurrentwinstreak() > player.getHighestwinstreak()) {
							playerDAO.incrementHighestWinstreak(Long.parseLong(attacker.getId()));
						}

					}

					for (Member defender : defendersList) {
						playerDAO playerDAO = new playerDAO();

						playerDAO.incrementLose(Long.parseLong(defender.getId()));
						playerDAO.resetWinstreak(Long.parseLong(defender.getId()));
					}

					EmbedBuilder matchInvitation = new EmbedBuilder();

					matchInvitation.setAuthor("2v2 Champion Match Info", event.getJDA().getSelfUser().getAvatarUrl());
					matchInvitation.setThumbnail(event.getGuild().getIconUrl());
					matchInvitation.setColor(Color.MAGENTA);
					matchInvitation.addField("Defenders lost",
							defendersList.get(0).getAsMention() + " || " + defendersList.get(1).getAsMention(), false);
					matchInvitation.addField("Challengers won",
							challengerList.get(0).getAsMention() + " || " + challengerList.get(1).getAsMention(), false);
					event.getGuild().getTextChannelById(MATCHESCHANNEL).sendMessage(matchInvitation.build()).queue();

					for (Member memberWithRole : defendersList) {
						event.getGuild().removeRoleFromMember(memberWithRole, role).queue();
					}

					for (Member mentionedMember : challengerList) {
						event.getGuild().addRoleToMember(mentionedMember, role).queue();
					}
				} else {
					event.reply("Only people with " + role.getAsMention() + " can use these buttons").setEphemeral(true)
					.queue();
				}

				}
	}

}
