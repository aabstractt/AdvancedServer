package us.advancedserver.provider;

import us.advancedserver.extension.BanEntry;
import us.advancedserver.extension.Rank;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class MysqlProvider implements Provider {

    private final Map<String, Object> data;

    private Connection connection;

    @SuppressWarnings("unchecked")
    public MysqlProvider(Map<String, Object> data, Map<String, Object> defaultRanks) throws SQLException {
        try {
            Class.forName("com.mysql.jdbc.Driver");

            this.intentConnect(data);

            PreparedStatement preparedStatement;

            preparedStatement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS players (id INT AUTO_INCREMENT PRIMARY KEY, username VARCHAR(70), address VARCHAR(60), lastAddress VARCHAR(60), auth_date VARCHAR(70))");

            preparedStatement.executeUpdate();

            preparedStatement.close();

            preparedStatement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS offline_players (id INT AUTO_INCREMENT PRIMARY KEY, username VARCHAR(70), original_username VARCHAR(70), address VARCHAR(60), auth_date VARCHAR(70))");

            preparedStatement.executeUpdate();

            preparedStatement.close();

            preparedStatement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS ranks(name VARCHAR(32), isDefault BOOLEAN DEFAULT 0 NOT NULL, inheritance TEXT NOT NULL, chat_format TEXT NOT NULL, nametag_format TEXT NOT NULL, format TEXT NOT NULL, permissions TEXT NOT NULL)");

            preparedStatement.executeUpdate();

            preparedStatement.close();

            preparedStatement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS users_rank(username VARCHAR(70), rankName VARCHAR(40))");

            preparedStatement.executeUpdate();

            preparedStatement.close();

            preparedStatement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS users_permission(username VARCHAR(70), permission VARCHAR(80))");

            preparedStatement.executeUpdate();

            preparedStatement.close();

            preparedStatement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS auth(id INT AUTO_INCREMENT PRIMARY KEY, username VARCHAR(70), password VARCHAR(200), address_registered VARCHAR(70), lastAddress VARCHAR(70))");

            preparedStatement.executeUpdate();

            preparedStatement.close();

            preparedStatement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS ban(type INT AUTO_INCREMENT PRIMARY KEY, username VARCHAR(70), createdAt VARCHAR(70), finishAt INT, author VARCHAR(70), reason VARCHAR(80), address VARCHAR(80), finished BOOLEAN DEFAULT FALSE)");

            preparedStatement.executeUpdate();

            preparedStatement.close();

        } catch (SQLException e) {
            throw new SQLException(e.getMessage(), e);
        } catch (ClassNotFoundException e) {
            throw new SQLException("Can't load JDBC Driver", e);
        }

        this.data = data;

        defaultRanks.forEach((name, rankData) -> this.createOrUpdateRank(new Rank(name, (Map<String, Object>) rankData)));
    }

    public void intentConnect(Map<String, Object> data) throws SQLException {
        this.connection = DriverManager.getConnection("jdbc:mysql://" + data.get("host") + ":" + data.get("port") + "/" + data.get("dbname") + "?serverTimezone=UTC", (String) data.get("username"), (String) data.get("password"));
    }

    @Override
    public String getName() {
        return "mysql";
    }

    @Override
    public void setTargetData(Map<String, Object> data) {
        try {
            if(this.isClosed()) this.intentConnect(this.data);

            boolean isTargetOffline = this.getTargetData((String) data.get("username")) != null;

            PreparedStatement preparedStatement;

            if(!isTargetOffline) {
                preparedStatement = connection.prepareStatement("INSERT INTO players(username, address, lastAddress, auth_date) VALUES (?, ?, ?, ?)");
            } else {
                preparedStatement = connection.prepareStatement("UPDATE players SET username = ?, address = ?, lastAddress = ?, auth_date = ? WHERE username = ?");
            }

            preparedStatement.setString(1, (String) data.get("username"));

            preparedStatement.setString(2, (String) data.get("address"));

            preparedStatement.setString(3, (String) data.get("lastAddress"));

            preparedStatement.setString(4, (String) data.get("auth_date"));

            if(!isTargetOffline) {
                preparedStatement.execute();
            } else {
                preparedStatement.setString(5, (String) data.get("username"));

                preparedStatement.executeUpdate();
            }

            preparedStatement.close();

        } catch(SQLException exception) {
            exception.printStackTrace();
        }
    }

    public void setTargetAuth(Map<String, String> data) {
        try {
            if(this.isClosed()) this.intentConnect(this.data);

            PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO auth(username, password, auth_date) VALUES (?, ?, ?)");

            preparedStatement.setString(1, data.get("username"));

            preparedStatement.setString(2, data.get("password"));

            preparedStatement.setString(3, data.get("auth_date"));

            preparedStatement.execute();

            preparedStatement.close();

        } catch(SQLException exception) {
            exception.printStackTrace();
        }
    }

    @Override
    public HashMap<String, Object> getTargetData(String username) {
        HashMap<String, Object> targetData = new HashMap<>();

        try {
            if(this.isClosed()) this.intentConnect(data);

            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM players WHERE username = ?");

            preparedStatement.setString(1, username);

            ResultSet rs = preparedStatement.executeQuery();

            while(rs.next()) {
                ResultSetMetaData md = rs.getMetaData();

                for(int $i = 1; $i <= md.getColumnCount(); $i++) {
                    targetData.put(md.getColumnName($i), rs.getObject($i));
                }
            }

            rs.close();

            preparedStatement.close();

        } catch(SQLException e) {
            e.printStackTrace();
        }

        return targetData.size() <= 0 ? null : targetData;
    }

    @Override
    public Map<String, Object> getTargetAuth(String username) {
        Map<String, Object> offlineData = new HashMap<>();

        try {
            if(this.isClosed()) this.intentConnect(data);

            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM auth WHERE username = ?");

            preparedStatement.setString(1, username);

            ResultSet rs = preparedStatement.executeQuery();

            while(rs.next()) {
                ResultSetMetaData md = rs.getMetaData();

                for(int $i = 1; $i <= md.getColumnCount(); $i++) {
                    offlineData.put(md.getColumnName($i), rs.getObject($i));
                }
            }

            rs.close();

            preparedStatement.close();

        } catch(SQLException e) {
            e.printStackTrace();
        }

        return offlineData.size() <= 0 ? null : offlineData;
    }

    @Override
    public void createOrUpdateRank(Rank rank) {
        try {
            if(this.isClosed()) this.intentConnect(this.data);

            PreparedStatement preparedStatement;

            boolean exists = this.getRank(rank.getName()) != null;

            if(exists) {
                preparedStatement = this.connection.prepareStatement("UPDATE ranks SET isDefault = ?, inheritance = ?, format = ?, chat_format = ?, nametag_format = ?, permissions = ? WHERE name = ?");
            } else {
                preparedStatement = this.connection.prepareStatement("INSERT ranks(isDefault, inheritance, format, chat_format, nametag_format, permissions, name) VALUES (?, ?, ?, ?, ?, ?, ?)");
            }

            preparedStatement.setBoolean(1, rank.isDefault());

            preparedStatement.setString(2, rank.getInheritedRanksString());

            preparedStatement.setString(3, rank.getFormat());

            preparedStatement.setString(4, rank.getOriginalChatFormat());

            preparedStatement.setString(5, rank.getOriginalNametagFormat(null));

            preparedStatement.setString(6, String.join(":", rank.getPermissionsWithoutInherited()));

            preparedStatement.setString(7, rank.getName());

            if(exists) {
                preparedStatement.executeUpdate();
            } else {
                preparedStatement.execute();
            }

            rank.updatePlayersPermissions();

            preparedStatement.close();

        } catch(SQLException exception) {
            exception.printStackTrace();
        }
    }

    @Override
    public void deleteRank(String rankName) {
        try {
            if(this.isClosed()) this.intentConnect(this.data);

            PreparedStatement preparedStatement = this.connection.prepareStatement("DELETE FROM ranks WHERE name = ?");

            preparedStatement.setString(1, rankName);

            preparedStatement.execute();

            preparedStatement.close();

        } catch(SQLException exception) {
            exception.printStackTrace();
        }
    }

    @Override
    public Rank getRank(String rankName) {
        Rank rank = null;

        try {
            if(this.isClosed()) this.intentConnect(this.data);

            PreparedStatement preparedStatement = this.connection.prepareStatement("SELECT * FROM ranks WHERE name = ?");

            preparedStatement.setString(1, rankName);

            Map<String, Object> data = new HashMap<>();

            ResultSet rs = preparedStatement.executeQuery();

            while(rs.next()) {
                ResultSetMetaData md = rs.getMetaData();

                for(int $i = 1; $i <= md.getColumnCount(); $i++) {
                    data.put(md.getColumnName($i), rs.getObject($i));
                }

                rank = new Rank((String) data.get("name"), data);
            }

            rs.close();

            preparedStatement.close();

        } catch(SQLException exception) {
            exception.printStackTrace();
        }

        return rank;
    }

    public List<Rank> getRanks() {
        List<Rank> ranks = new ArrayList<>();

        try {
            if(this.isClosed()) this.intentConnect(this.data);

            PreparedStatement preparedStatement = this.connection.prepareStatement("SELECT * FROM ranks");

            ResultSet rs = preparedStatement.executeQuery();

            while(rs.next()) {
                ranks.add(this.getRank(rs.getString("name")));
            }

            rs.close();

            preparedStatement.close();

        } catch(SQLException exception) {
            exception.printStackTrace();
        }

        return ranks;
    }

    @Override
    public Rank getDefaultRank() {
        Rank rank = null;

        try {
            if(this.isClosed()) this.intentConnect(this.data);

            PreparedStatement preparedStatement = this.connection.prepareStatement("SELECT name FROM ranks WHERE isDefault = ?");

            preparedStatement.setBoolean(1, true);

            ResultSet rs = preparedStatement.executeQuery();

            while(rs.next()) {
                rank = this.getRank(rs.getString("name"));
            }

            rs.close();

            preparedStatement.close();

        } catch(SQLException exception) {
            exception.printStackTrace();
        }

        return rank;
    }

    @Override
    public void setTargetRank(String username, String rankName) {
        try {
            if(this.isClosed()) this.intentConnect(this.data);

            PreparedStatement preparedStatement;

            boolean hasRank = this.getTargetRank(username) != null;

            if(hasRank) {
                preparedStatement = this.connection.prepareStatement("UPDATE users_rank SET rankName ? WHERE username = ?");
            } else {
                preparedStatement = this.connection.prepareStatement("INSERT users_rank(rankName, username) VALUES (?, ?)");
            }

            preparedStatement.setString(1, rankName);

            preparedStatement.setString(2, username);

            if(hasRank) {
                preparedStatement.executeUpdate();
            } else {
                preparedStatement.execute();
            }

            preparedStatement.close();

        } catch(SQLException exception) {
            exception.printStackTrace();
        }
    }

    @Override
    public Rank getTargetRank(String username) {
        Rank rank = null;

        try {
            if(this.isClosed()) this.intentConnect(this.data);

            PreparedStatement preparedStatement = this.connection.prepareStatement("SELECT rankName FROM users_rank WHERE username = ?");

            preparedStatement.setString(1, username);

            ResultSet rs = preparedStatement.executeQuery();

            while(rs.next()) {
                rank = this.getRank(rs.getString("rankName"));
            }

            rs.close();

            preparedStatement.close();

        } catch(SQLException exception) {
            exception.printStackTrace();
        }

        return rank;
    }

    @Override
    public void setPlayerPermission(String username, String permission) {
        try {
            if(this.isClosed()) this.intentConnect(this.data);

            PreparedStatement preparedStatement = this.connection.prepareStatement("INSERT INTO users_permission(username, permission) VALUES (?, ?)");

            preparedStatement.setString(1, username);

            preparedStatement.setString(2, permission);

            preparedStatement.execute();

            preparedStatement.close();

        } catch(SQLException exception) {
            exception.printStackTrace();
        }
    }

    @Override
    public List<String> getPlayerPermissions(String username) {
        List<String> permissions = new ArrayList<>();

        try {
            if(this.isClosed()) this.intentConnect(this.data);

            PreparedStatement preparedStatement = this.connection.prepareStatement("SELECT * FROM users_permission WHERE username = ?");

            preparedStatement.setString(1, username);

            ResultSet rs = preparedStatement.executeQuery();

            while(rs.next()) {
                permissions.add(rs.getString("permission"));
            }

            rs.close();

            preparedStatement.close();

        } catch(SQLException exception) {
            exception.printStackTrace();
        }

        return permissions;
    }

    public boolean hasPlayerPermission(String username, String permission) {
        for(String perm : this.getPlayerPermissions(username)) {
            if(perm.equals(permission)) return true;
        }

        return false;
    }

    @Override
    public void deletePlayerPermission(String username, String permission) {
        try {
            if(this.isClosed()) this.intentConnect(this.data);

            PreparedStatement preparedStatement = this.connection.prepareStatement("DELETE FROM users_permission WHERE username = ? AND permission = ?");

            preparedStatement.setString(1, username);

            preparedStatement.setString(2, permission);

            preparedStatement.executeUpdate();

        } catch(SQLException exception) {
            exception.printStackTrace();
        }
    }

    public void addBan(BanEntry entry) {
        try {
            if(this.isClosed()) this.intentConnect(this.data);

            PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO ban (type, username, createdAt, finishAt, author, reason, address, finished) VALUES (?, ?, ?, ?, ?, ?, ?, ?)");

            preparedStatement.setInt(1, entry.getType());

            preparedStatement.setString(2, entry.getName());

            preparedStatement.setString(3, entry.getCreatedAt());

            preparedStatement.setInt(4, (int) TimeUnit.MILLISECONDS.toMinutes(entry.getFinishAt()));

            preparedStatement.setString(5, entry.getAuthor());

            preparedStatement.setString(6, entry.getReason());

            preparedStatement.setString(7, entry.getAddress());

            preparedStatement.setBoolean(8, false);

            preparedStatement.execute();

            preparedStatement.close();

        } catch(SQLException exception) {
            exception.printStackTrace();
        }
    }

    public BanEntry getBanActiveByUsername(String username) {
        BanEntry entry = this.getBanActiveByUsername(username, false);

        if(entry == null) {
            return this.getBanActiveByUsername(username, true);
        }

        return entry;
    }

    @Override
    public BanEntry getBanActiveByUsername(String username, boolean permanent) {
        BanEntry entry = null;

        try {
            if(this.isClosed()) this.intentConnect(this.data);

            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM ban WHERE type = ? AND username = ? AND finished = ?");

            preparedStatement.setInt(1, permanent ? 1 : 2);

            preparedStatement.setString(2, username);

            preparedStatement.setBoolean(3, false);

            ResultSet rs = preparedStatement.executeQuery();

            while(rs.next()) {
                entry = new BanEntry(rs.getInt("type"), rs.getString("username"), rs.getString("createdAt"), TimeUnit.MINUTES.toMillis(rs.getInt("finishAt")), rs.getString("author"), rs.getString("reason"), rs.getString("address"), rs.getBoolean("finished"), rs.getInt("id"));
            }

            rs.close();

            preparedStatement.close();

        } catch(SQLException exception) {
            exception.printStackTrace();
        }

        return entry;
    }

    @Override
    public BanEntry getMuteActiveByUsername(String username) {
        BanEntry entry = this.getMuteActiveByUsername(username, false);

        if(entry == null) {
            return this.getMuteActiveByUsername(username, true);
        }

        return entry;
    }

    @Override
    public BanEntry getMuteActiveByUsername(String username, boolean permanent) {
        BanEntry entry = null;

        try {
            if(this.isClosed()) this.intentConnect(this.data);

            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM ban WHERE type = ? AND username = ? AND finished = ?");

            preparedStatement.setInt(1, permanent ? 3 : 4);

            preparedStatement.setString(2, username);

            preparedStatement.setBoolean(3, false);

            ResultSet rs = preparedStatement.executeQuery();

            while(rs.next()) {
                entry = new BanEntry(rs.getInt("type"), rs.getString("username"), rs.getString("createdAt"), TimeUnit.MINUTES.toMillis(rs.getInt("finishAt")), rs.getString("author"), rs.getString("reason"), rs.getString("address"), rs.getBoolean("finished"), rs.getInt("id"));
            }

            rs.close();

            preparedStatement.close();

        } catch(SQLException exception) {
            exception.printStackTrace();
        }

        return entry;
    }

    @Override
    public List<BanEntry> getAllDeleteByUsername(String username) {
        ArrayList<BanEntry> entrys = new ArrayList<>();

        try {
            if(this.isClosed()) this.intentConnect(this.data);

            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM ban WHERE (type > 4 AND type < 7) AND username = ?");

            preparedStatement.setString(1, username);

            ResultSet rs = preparedStatement.executeQuery();

            while(rs.next()) {
                entrys.add(new BanEntry(rs.getInt("type"), rs.getString("username"), rs.getString("createdAt"), TimeUnit.MINUTES.toMillis(rs.getInt("finishAt")), rs.getString("author"), rs.getString("reason"), rs.getString("address"), rs.getBoolean("finished"), rs.getInt("id")));
            }

            rs.close();

            preparedStatement.close();

        } catch(SQLException exception) {
            exception.printStackTrace();
        }

        return entrys;
    }

    @Override
    public List<BanEntry> getAllActiveByUsername(String username) {
        List<BanEntry> entrys = new ArrayList<>();

        try {
            if(this.isClosed()) this.intentConnect(this.data);

            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM ban WHERE username = ?");

            preparedStatement.setString(1, username);

            ResultSet rs = preparedStatement.executeQuery();

            while(rs.next()) {
                entrys.add(new BanEntry(rs.getInt("type"), rs.getString("username"), rs.getString("createdAt"), TimeUnit.MINUTES.toMillis(rs.getInt("finishAt")), rs.getString("author"), rs.getString("reason"), rs.getString("address"), rs.getBoolean("finished"), rs.getInt("id")));
            }

            rs.close();

            preparedStatement.close();

        } catch(SQLException exception) {
            exception.printStackTrace();
        }

        return entrys.size() <= 0 ? null : entrys;
    }

    @Override
    public void deleteEntry(BanEntry entry) {
        try {
            if(this.isClosed()) this.intentConnect(this.data);

            PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM ban WHERE id = ?");

            preparedStatement.setInt(1, entry.getId());

            preparedStatement.execute();

            preparedStatement.close();

        } catch(SQLException exception) {
            exception.printStackTrace();
        }
    }

    @Override
    public void updateEntry(BanEntry entry) {
        try {
            if(this.isClosed()) this.intentConnect(this.data);

            PreparedStatement preparedStatement = connection.prepareStatement("UPDATE ban SET type = ?, createdAt = ?, finishAt = ?, author = ?, reason = ?, address = ?, finished = ? WHERE id = ?");

            preparedStatement.setInt(1, entry.getId());

            preparedStatement.setString(2, entry.getCreatedAt());

            preparedStatement.setInt(3, (int) TimeUnit.MILLISECONDS.toMinutes(entry.getFinishAt()));

            preparedStatement.setString(4, entry.getAuthor());

            preparedStatement.setString(5, entry.getReason());

            preparedStatement.setString(6, entry.getAddress());

            preparedStatement.setBoolean(7, entry.isFinished());

            preparedStatement.setString(8, entry.getName());

            preparedStatement.executeUpdate();

            preparedStatement.close();

        } catch(SQLException exception) {
            exception.printStackTrace();
        }
    }

    /**
     * @return bool
     */
    private boolean isClosed() throws SQLException {
        return this.connection != null && this.connection.isClosed();
    }
}