package us.advancedserver.provider;

import us.advancedserver.extension.Rank;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

            preparedStatement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS offline_players (id INT AUTO_INCREMENT PRIMARY KEY, username VARCHAR(70), guestName VARCHAR(70), address VARCHAR(60), auth_date VARCHAR(70))");

            preparedStatement.executeUpdate();

            preparedStatement.close();

            preparedStatement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS ranks(name VARCHAR(32), isDefault BOOLEAN DEFAULT 0 NOT NULL, inheritance TEXT NOT NULL, chat_format TEXT NOT NULL, nametag_format TEXT NOT NULL, format TEXT NOT NULL, permissions TEXT NOT NULL)");

            preparedStatement.executeUpdate();

            preparedStatement.close();

            preparedStatement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS users_rank(username VARCHAR(70), rankName VARCHAR(40))");

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
    public void setTargetData(HashMap<String, Object> data) {
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

    public void setOfflineData(HashMap<String, Object> data) {
        try {
            if(this.isClosed()) this.intentConnect(this.data);

            PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO offline_players(username, guestName, address, auth_date) VALUES (?, ?, ?, ?)");

            preparedStatement.setString(1, (String) data.get("username"));

            preparedStatement.setString(2, (String) data.get("guestName"));

            preparedStatement.setString(3, (String) data.get("address"));

            preparedStatement.setString(4, (String) data.get("auth_date"));

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
    public HashMap<String, Object> getOfflineData(String username) {
        HashMap<String, Object> offlineData = new HashMap<>();

        try {
            if(this.isClosed()) this.intentConnect(data);

            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM offline_players WHERE guestName = ?");

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

    public void deleteTargetOffline(String guestName) {
        this.deleteTargetOffline("guestName", guestName);
    }

    public void deleteTargetOffline(String columnName, String guestName) {
        try {
            if(this.isClosed()) this.intentConnect(this.data);

            PreparedStatement preparedStatement = this.connection.prepareStatement(String.format("DELETE FROM offline_players WHERE %s = ?", columnName));

            preparedStatement.setString(1, guestName);


        } catch(SQLException exception) {
            exception.printStackTrace();
        }
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

            PreparedStatement preparedStatement = this.connection.prepareStatement("DELETE FROM userS_permission WHERE username = ? AND permission = ?");

            preparedStatement.setString(1, username);

            preparedStatement.setString(2,permission);

            preparedStatement.executeUpdate();

        } catch(SQLException exception) {
            exception.printStackTrace();
        }
    }

    @Override
    public HashMap<String, Object> handleAuthData(String username) {
        HashMap<String, Object> authData = new HashMap<>();

        try {
            if(this.isClosed()) this.intentConnect(data);

            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM auth WHERE username = ?");

            preparedStatement.setString(1, username);

            ResultSet rs = preparedStatement.executeQuery();

            while(rs.next()) {
                ResultSetMetaData md = rs.getMetaData();

                for(int $i = 1; $i <= md.getColumnCount(); $i++) {
                    authData.put(md.getColumnName($i), rs.getObject($i));
                }
            }

            rs.close();

            preparedStatement.close();
        } catch(SQLException exception) {
            exception.printStackTrace();
        }

        return authData;
    }

    /**
     * @return bool
     */
    private boolean isClosed() throws SQLException {
        return this.connection != null && this.connection.isClosed();
    }
}
