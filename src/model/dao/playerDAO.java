package model.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import model.connection.DBConnection;
import model.vo.playerVO;

public class playerDAO {
	
	public void insertPlayer(playerVO player) {
		DBConnection conn = new DBConnection();
		
		try {
			Statement query = conn.getConnection().createStatement();
			query.executeUpdate("INSERT INTO players VALUES (" + player.getPlayerID() + "," + player.getWins() + "," 
					+ player.getLoses() + "," + player.getHighestwinstreak() + "," + player.getCurrentwinstreak() + ","
					+ player.getDodges()  + "," + player.getElo() + ")");
			query.close();
			conn.disconnect();
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
	}

	public void savedatabase(){
		DBConnection conn = new DBConnection();

		try {
			Statement query = conn.getConnection().createStatement();
			query.executeUpdate("DROP TABLE if exists season2leaderboard");
			query.executeUpdate("CREATE TABLE season2leaderboard (SELECT * FROM players)");
			query.close();
			conn.disconnect();
		} catch(SQLException e){
			System.out.println(e.getMessage());
		}
	}

	public void deletePlayer(long id){
		DBConnection conn = new DBConnection();
		try {
			Statement query = conn.getConnection().createStatement();
			query.executeUpdate("DELETE FROM players WHERE playerID = " + id);
			query.close();
			conn.disconnect();
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
	}
	
	public List<playerVO> getPlayers(int amount){
		DBConnection conn = new DBConnection();
		List<playerVO> players = new ArrayList<playerVO>();
		playerVO player = null;
		
		try {
			PreparedStatement query = conn.getConnection().prepareStatement("SELECT*,IFNULL(round((wins / (wins+loses))*100,2),0) AS winratio,ROW_NUMBER() OVER() AS ranking FROM players ORDER BY elo desc, winratio DESC,wins DESC");
			ResultSet res = query.executeQuery();
			
			while(res.next()) {
				player = new playerVO();
				player.setPlayerID(Long.parseLong(res.getString("playerID")));
				player.setWins(Integer.parseInt(res.getString("wins")));
				player.setLoses(Integer.parseInt(res.getString("loses")));
				player.setHighestwinstreak(Integer.parseInt(res.getString("highestwinstreak")));
				player.setCurrentwinstreak(Integer.parseInt(res.getString("currentwinstreak")));
				player.setDodges(Integer.parseInt(res.getString("dodges")));
				player.setWinRatio(Double.parseDouble(res.getString("winratio")));
				player.setRanking(Integer.parseInt(res.getString("ranking")));
				player.setElo(Integer.parseInt(res.getString("elo")));

				
				players.add(player);
			}
			res.close();
			conn.disconnect();
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
		
		if (!players.isEmpty()) {
			return players;
		}
		return null;
	}
	
	public List<playerVO> getPlayersLeaderboard(int amount){
		DBConnection conn = new DBConnection();
		List<playerVO> players = new ArrayList<playerVO>();
		playerVO player = null;
		
		try {
			PreparedStatement query = conn.getConnection().prepareStatement("SELECT*,IFNULL(round((wins / (wins+loses))*100,2),0) AS winratio,ROW_NUMBER() OVER() AS ranking FROM players where (wins + loses) >= 10 ORDER BY elo desc, winratio DESC,wins DESC"
					+ " limit " + amount);
			ResultSet res = query.executeQuery();
			
			while(res.next()) {
				player = new playerVO();
				player.setPlayerID(Long.parseLong(res.getString("playerID")));
				player.setWins(Integer.parseInt(res.getString("wins")));
				player.setLoses(Integer.parseInt(res.getString("loses")));
				player.setHighestwinstreak(Integer.parseInt(res.getString("highestwinstreak")));
				player.setCurrentwinstreak(Integer.parseInt(res.getString("currentwinstreak")));
				player.setWinRatio(Double.parseDouble(res.getString("winratio")));
				player.setDodges(Integer.parseInt(res.getString("dodges")));
				player.setElo(Integer.parseInt(res.getString("elo")));
				
				players.add(player);
			}
			res.close();
			conn.disconnect();
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
		
		if (!players.isEmpty()) {
			return players;
		}
		return null;
	}

	public List<playerVO> getPlayersLeaderboardBozos(int amount){
		DBConnection conn = new DBConnection();
		List<playerVO> players = new ArrayList<playerVO>();
		playerVO player = null;

		try {
			PreparedStatement query = conn.getConnection().prepareStatement("SELECT*,IFNULL(round((wins / (wins+loses))*100,2),0) AS winratio,ROW_NUMBER() OVER() AS ranking FROM players where (wins + loses) >= 10 ORDER BY elo ASC, winratio ASC,wins ASC"
					+ " limit " + amount);
			ResultSet res = query.executeQuery();

			while(res.next()) {
				player = new playerVO();
				player.setPlayerID(Long.parseLong(res.getString("playerID")));
				player.setWins(Integer.parseInt(res.getString("wins")));
				player.setLoses(Integer.parseInt(res.getString("loses")));
				player.setHighestwinstreak(Integer.parseInt(res.getString("highestwinstreak")));
				player.setCurrentwinstreak(Integer.parseInt(res.getString("currentwinstreak")));
				player.setWinRatio(Double.parseDouble(res.getString("winratio")));
				player.setDodges(Integer.parseInt(res.getString("dodges")));
				player.setElo(Integer.parseInt(res.getString("elo")));

				players.add(player);
			}
			res.close();
			conn.disconnect();
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}

		if (!players.isEmpty()) {
			return players;
		}
		return null;
	}
	
	
	public playerVO searchPlayer(long playerID) {
		DBConnection conn = new DBConnection();
		playerVO player = null;
		
		try {
			PreparedStatement query = conn.getConnection().prepareStatement("SELECT * FROM players WHERE playerID = ? ");
			query.setLong(1, playerID);
			ResultSet res = query.executeQuery();
			
			while(res.next()) {
				player = new playerVO();
				player.setPlayerID(Long.parseLong(res.getString("playerID")));
				player.setWins(Integer.parseInt(res.getString("wins")));
				player.setLoses(Integer.parseInt(res.getString("loses")));
				player.setHighestwinstreak(Integer.parseInt(res.getString("highestwinstreak")));
				player.setCurrentwinstreak(Integer.parseInt(res.getString("currentwinstreak")));
				player.setDodges(Integer.parseInt(res.getString("dodges")));
				player.setElo(Integer.parseInt(res.getString("elo")));
			}
			res.close();
			conn.disconnect();
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
		
		if (player != null) {
			return player;
		}
		return null;
	}

	public void addElo(long playerID,int elo) {
		DBConnection conn = new DBConnection();
		try {
			String query = "UPDATE players SET elo = (SELECT elo) + " + elo + " WHERE playerID = " + playerID;
			PreparedStatement statement = conn.getConnection().prepareStatement(query);
			statement.executeUpdate();
		} catch (SQLException e) {
			System.out.println(e);
		}
	}

	public void dodgeElo(long playerID){
		DBConnection conn = new DBConnection();
		try {
			String query = "UPDATE players SET elo = (SELECT elo) - 25 WHERE playerID = " + playerID;
			PreparedStatement statement = conn.getConnection().prepareStatement(query);
			statement.executeUpdate();
		} catch (SQLException e) {
			System.out.println(e);
		}
	}

	public void subtractElo(long playerID,int elo){
		DBConnection conn = new DBConnection();
		try {
			String query = "UPDATE players SET elo = (SELECT elo)  " + elo + " WHERE playerID = " + playerID;
			PreparedStatement statement = conn.getConnection().prepareStatement(query);
			statement.executeUpdate();
		} catch (SQLException e) {
			System.out.println(e);
		}
	}
	
	public void incrementWin(long playerID) {
		DBConnection conn = new DBConnection();
		try {
			String query = "UPDATE players SET wins = (SELECT wins) + 1 WHERE playerID = " + playerID;
			PreparedStatement statement = conn.getConnection().prepareStatement(query);
			statement.executeUpdate();
		} catch (SQLException e) {
			System.out.println(e);
		}
	}

	public void editWins(long playerID, String wins){
		DBConnection conn = new DBConnection();
		try {
			String query = "UPDATE players SET wins = " + wins + " WHERE playerID = " + playerID;
			PreparedStatement statement = conn.getConnection().prepareStatement(query);
			statement.executeUpdate();
		}catch (SQLException e){
			System.out.println(e);
		}
	}
	
	public void subtractWin(long playerID) {
		DBConnection conn = new DBConnection();
		try {
			String query = "UPDATE players SET wins = (SELECT wins) - 1 WHERE playerID = " + playerID;
			PreparedStatement statement = conn.getConnection().prepareStatement(query);
			statement.executeUpdate();
		} catch (SQLException e) {
			System.out.println(e);
		}
	}
	
	public void incrementLose(long playerID) {
		DBConnection conn = new DBConnection();
		try {
			String query = "UPDATE players SET loses = (SELECT loses) + 1 WHERE playerID = " + playerID;
			PreparedStatement statement = conn.getConnection().prepareStatement(query);
			statement.executeUpdate();
		} catch (SQLException e) {
			System.out.println(e);
		}
	}
	
	public void subtractLose(long playerID) {
		DBConnection conn = new DBConnection();
		try {
			String query = "UPDATE players SET loses = (SELECT loses) - 1 WHERE playerID = " + playerID;
			PreparedStatement statement = conn.getConnection().prepareStatement(query);
			statement.executeUpdate();
		} catch (SQLException e) {
			System.out.println(e);
		}
	}

	public void editLoses(long playerID, String loses){
		DBConnection conn = new DBConnection();
		try {
			String query = "UPDATE players SET loses = " + loses + " WHERE playerID = " + playerID;
			PreparedStatement statement = conn.getConnection().prepareStatement(query);
			statement.executeUpdate();
		}catch (SQLException e){
			System.out.println(e);
		}
	}
	
	public void incrementHighestWinstreak(long playerID) {
		DBConnection conn = new DBConnection();
		try {
			String query = "UPDATE players SET highestwinstreak = (SELECT highestwinstreak) + 1 WHERE playerID = " + playerID;
			PreparedStatement statement = conn.getConnection().prepareStatement(query);
			statement.executeUpdate();
		} catch (SQLException e) {
			System.out.println(e);
		}
	}
	
	
	public void subtractHighestWinstreak(long playerID) {
		DBConnection conn = new DBConnection();
		try {
			String query = "UPDATE players SET highestwinstreak = (SELECT highestwinstreak) - 1 WHERE playerID = " + playerID;
			PreparedStatement statement = conn.getConnection().prepareStatement(query);
			statement.executeUpdate();
		} catch (SQLException e) {
			System.out.println(e);
		}
	}

	public void editHighestWinstreak(long playerID, String highestwinstreak){
		DBConnection conn = new DBConnection();
		try {
			String query = "UPDATE players SET highestwinstreak = " + highestwinstreak + " WHERE playerID = " + playerID;
			PreparedStatement statement = conn.getConnection().prepareStatement(query);
			statement.executeUpdate();
		}catch (SQLException e){
			System.out.println(e);
		}
	}
	
	public void incrementCurrentWinstreak(long playerID) {
		DBConnection conn = new DBConnection();
		try {
			String query = "UPDATE players SET currentwinstreak = (SELECT currentwinstreak) + 1 WHERE playerID = " + playerID;
			PreparedStatement statement = conn.getConnection().prepareStatement(query);
			statement.executeUpdate();
		} catch (SQLException e) {
			System.out.println(e);
		}
	}
	
	public void subtractCurrentWinstreak(long playerID) {
		DBConnection conn = new DBConnection();
		try {
			String query = "UPDATE players SET currentwinstreak = (SELECT currentwinstreak) - 1 WHERE playerID = " + playerID;
			PreparedStatement statement = conn.getConnection().prepareStatement(query);
			statement.executeUpdate();
		} catch (SQLException e) {
			System.out.println(e);
		}
	}
	
	public void resetWinstreak(long playerID) {
		DBConnection conn = new DBConnection();
		try {
			String query = "UPDATE players SET currentwinstreak = 0 WHERE playerID = " + playerID;
			PreparedStatement statement = conn.getConnection().prepareStatement(query);
			statement.executeUpdate();
		} catch (SQLException e) {
			System.out.println(e);
		}
	}

	public void editWinstreak(long playerID, String winstreak){
		DBConnection conn = new DBConnection();
		try {
			String query = "UPDATE players SET currentwinstreak = " + winstreak + " WHERE playerID = " + playerID;
			PreparedStatement statement = conn.getConnection().prepareStatement(query);
			statement.executeUpdate();
		}catch (SQLException e){
			System.out.println(e);
		}
	}

	public void just4FunIsInYourTeam(long playerID){
		DBConnection conn =  new DBConnection();
		try{
			String query = "UPDATE players SET dodges = (SELECT dodges) + 1 WHERE playerID = " + playerID;
			PreparedStatement statement = conn.getConnection().prepareStatement(query);
			statement.executeUpdate();
		}catch (SQLException e ){
			System.out.println(e);
		}
	}

	public void setDodgeTo0(long playerID){
		DBConnection conn = new DBConnection();
		try{
			String query = "UPDATE players SET dodges = 0 WHERE playerID = " + playerID;
			PreparedStatement statement = conn.getConnection().prepareStatement(query);
			statement.executeUpdate();
		}catch (SQLException e) {
			System.out.println(e);
		}
	}
}
