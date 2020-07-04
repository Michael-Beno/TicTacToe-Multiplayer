package javafxapplication;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Michael Beno
 */
public class DBConnect {

    private Connection con;
    private Statement s;

    public DBConnect() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
        con = DriverManager.getConnection("jdbc:mysql://<IP>:3306/<table>?user=<username>&password=<password>" );
            System.out.println("Database connection established");
            s = con.createStatement();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | SQLException ex) {
            System.out.println("SQLException: " + ex.getMessage());
        }
    }

    public void createUser(String name, String code) {
        try {
            s = con.createStatement();
            String insertSQL = "INSERT INTO `Players` (`pName`, `sid` , `pPass`) VALUES ('" + name + "', '0' , '" + code + "');";

            int res = s.executeUpdate(insertSQL);
            System.out.println("The Number or records inserted is      " + res);
        } catch (Exception io) {
            System.err.println("createUser error " + io);
        }
    }//insert
     
    public void setUserStatus(String myID, int status) {
        try {
            s = con.createStatement();
            String updateSQL = " Update Players set sid = " + status + " where pid ='" + myID + "'";
            int res = s.executeUpdate(updateSQL);
            System.out.println("The Number or records updated is      " + res);
        } catch (Exception io) {
            System.err.println("stUserStatus error " + io);
            Logger.getLogger(DBConnect.class.getName()).log(Level.SEVERE, null, io);
        }
    }//update
    
    public boolean isNameExist(String name) {
        boolean id = false;
        try {
            ResultSet rs = s.executeQuery("select * from Players WHERE pName = '" + name + "';");
            id = rs.next();
        } catch (SQLException io) {
            System.err.println("isNameExist error " + io);
            Logger.getLogger(DBConnect.class.getName()).log(Level.SEVERE, null, io);
        }
        System.out.println("is exist: " + id);
        return id;
    }
     
    public String getPlayersId(String name, String code) {
        String id = "";
        try {
            ResultSet rs = s.executeQuery("select * from Players WHERE pPass = '" + code + "' and pName = '" + name + "';");
            while (rs.next()) {
                id = rs.getString("pid");
            }
        } catch (SQLException ex) {
            System.err.println("getPlayersID error " + ex);
            Logger.getLogger(DBConnect.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("my id is: " + id);
        return id;
    }

    public String getUserName(String myID) {
        String name = "";
        try {
            ResultSet rs = s.executeQuery("select * from Players WHERE pid = '" + myID + "'");
            while (rs.next()) {
                name = rs.getString("pName");
            }
        } catch (SQLException io) {
            System.err.println("getUserName error " + io);
            Logger.getLogger(DBConnect.class.getName()).log(Level.SEVERE, null, io);
        }
        if (name.equals(Text.HOST)) {
            name = name + myID;
        }
        return name;
    }
    
    public int getMyPlayerStatus(String pid) {
        int status = Code.USER_OFFLINE;
        try {
            ResultSet rs = s.executeQuery("select * from Players WHERE pid = '" + pid + "'");
            if (rs.next()) {
                status = Integer.parseInt(rs.getString("sid"));
            }
        } catch (SQLException io) {
            System.err.println("getMyPlayerStatus error " + io);
            Logger.getLogger(DBConnect.class.getName()).log(Level.SEVERE, null, io);
        }
        return status;
    }
    
    public ArrayList<User> getListPlayers(int code) {
        String players = "";
        ArrayList<User> list = new ArrayList();
        String query = "";
        if (code == Code.ALL_PLAYERS) {
            query = "select * from Players ORDER BY sid DESC";
        } else if (code == Code.ALL_OPPONENTS) {
            query = "select * from Players where sid = '" + Code.USER_LOGGED_IN + "' and pid != '" + JavaFXApplication.myID + "' order by tid desc;";
        }
        ResultSet rs;
        try {
            rs = s.executeQuery(query);
            while (rs.next()) {
                players += "id:" + rs.getString("pid")
                        + " " + rs.getString("pName")
                        + " " + rs.getString("pPass")
                        + " " + rs.getString("sid") + "\n";
                String user = rs.getString("pName");
                if (user.compareTo(Text.HOST) == 0) {
                    user += rs.getString("pid");
                }
                list.add(new User(rs.getString("pid"), user, rs.getString("sid")));
            }
        } catch (SQLException ex) {
            System.out.println("SQLException: " + ex.getMessage());
        }
        return list;
    }

    public ArrayList<User> getArrOpponent(String myID, String opponentID, int code) {
        String players = "";
        ArrayList<User> list = new ArrayList();
        String query = "";
        if (code == Code.CALL_I_AM_DIALING) {
            query = "SELECT * FROM Connect c ,Players p where p.pid = calledId and c.pid =" + myID + ";";
        } else if (code == Code.CALL_RECEIVING) {
            query = "SELECT * FROM Connect c, Players p  where calledId = " + myID + " and p.pid = " + opponentID + ";";
        } else if (code == Code.USER_PLAYING) {
            query = "SELECT * FROM Players p where pid = " + opponentID + ";";
        }

        ResultSet rs;
        try {
            rs = s.executeQuery(query);
            while (rs.next()) {
                players += "id:" + rs.getString("pid")
                        + " " + rs.getString("pName")
                        + " " + rs.getString("pPass")
                        + " " + rs.getString("sid") + "\n";
                String user = rs.getString("pName");
                if (user.compareTo(Text.HOST) == 0) {
                    user += rs.getString("pid");
                }
                list.add(new User(rs.getString("pid"), user, rs.getString("sid")));
            }
        } catch (SQLException io) {
            System.err.println("getArrOpponent error " + io);
            Logger.getLogger(DBConnect.class.getName()).log(Level.SEVERE, null, io);
        }
        return list;
    }
   
    public User getOutgoingCall(String myID) {
        ResultSet rs;
        User u = new User("", "", "" + Code.CALL_NONE);
        try {
            rs = s.executeQuery("SELECT * FROM Connect c, Players p where p.pid = calledId and c.pid = '" + myID + "';");
            if (rs.next()) {
                String user = rs.getString("pName");
                if (user.compareTo(Text.HOST) == 0) {
                    user += rs.getString("p.pid");
                }
                u = new User(rs.getString("calledId"), user, rs.getString("c.sid"));
            }
        } catch (SQLException io) {
            System.err.println("getListeningUser error " + io);
            Logger.getLogger(DBConnect.class.getName()).log(Level.SEVERE, null, io);
        }
        return u;
    }

    public User getIncommingCall(String myID) {
        ResultSet rs;
        User u = new User("", "", "" + Code.CALL_NONE);
        String str="";
        try {
            
            rs = s.executeQuery("SELECT * FROM Connect c where calledId = '" + myID + "' or pid = '" + myID + "';");
             if (rs.next()) {
                 str = rs.getString("calledId");
             }else return u;
            
             if(myID.equals(str)){
            
            rs = s.executeQuery("SELECT * FROM Connect c, Players p where calledId = '" + myID + "' and c.pid = p.pid;");
            if (rs.next()) {
                String user = rs.getString("pName");
                if (user.compareTo(Text.HOST) == 0) {
                    user += rs.getString("p.pid");
                }
                
               u = new User(rs.getString("pid"), user , ""+Code.CALL_RECEIVING);
            }
            }else{
                 u = getOutgoingCall(myID);
             u.status = ""+Code.CALL_I_AM_DIALING;
            }
        } catch (SQLException io) {
            System.err.println("getListeningUser error " + io);
            Logger.getLogger(DBConnect.class.getName()).log(Level.SEVERE, null, io);
        }
        return u;
    }
   
    public void setCallOpponent(String myID, String id, int code) {
        try {
            s = con.createStatement();
            String insertSQL = "INSERT INTO Connect (pid, calledId, sid) VALUES (" + myID + ", " + id + ", " + code + "); ";
            int res = s.executeUpdate(insertSQL);
            System.out.println("The Number or records inserted is      " + res);
        } catch (Exception io) {
            System.err.println("setCallOpponent error " + io);
            Logger.getLogger(DBConnect.class.getName()).log(Level.SEVERE, null, io);
        }
    }//insert

    public void rejectCall(String opponentID) {
        try {
            s = con.createStatement();
            String deleteSQL = " Delete from Connect where pid = " + opponentID + " ;";
            int res = s.executeUpdate(deleteSQL);
            System.out.println("The Number or records deleted is      " + res);
        } catch (Exception io) {
            System.err.println("rejectCall error " + io);
            Logger.getLogger(DBConnect.class.getName()).log(Level.SEVERE, null, io);
        }
    }
    
    public void startGame(String myID, String opponentID, String mySymbol, String oppSymbol, int code) {
        try {
            s = con.createStatement();
            String insertSQL = "INSERT INTO `Game` (`pid1`, `pid2`, `Symbol1`, `Symbol2`, `playboard`, `lastPlayedId`, gameCreated, sid)"
                    + " VALUES ('" + myID + "', '" + opponentID + "', '" + mySymbol + "', '" + oppSymbol + "', '---------', '" + myID + "', now(), 31 );";

            int res = s.executeUpdate(insertSQL);
            System.out.println("The Number or records inserted is      " + res);
        } catch (Exception io) {
            System.err.println("startGame error " + io);
            Logger.getLogger(DBConnect.class.getName()).log(Level.SEVERE, null, io);
        }
                
    }//insert

    public GamePlay getGamePlay(String myID) {
        String gid = "", pid1 = "", pid2 = "", playBoard = "", playedID = "", sid = "", oppName = "", pPid = "";
        char symbol1 = 0, symbol2 = 0;
        GamePlay u;// = new User(pid, name, name);
        try {
            ResultSet rs = s.executeQuery(
                    "SELECT * FROM Game g, Players p where (pid1 = pid or pid2 = pid) and g.sid = '" + Code.GAME_RUNNING + "' and pid = '" + myID + "';");
            while (rs.next()) {
                gid = rs.getString("gid");
                pid1 = rs.getString("pid1");
                pid2 = rs.getString("pid2");
                symbol1 = rs.getString("Symbol1").charAt(0);
                symbol2 = rs.getString("Symbol2").charAt(0);
                oppName = rs.getString("pName");
                playBoard = rs.getString("playboard");
                playedID = rs.getString("lastPlayedId");
                sid = rs.getString("sid");
                pPid = rs.getString("p.pid");
            }
        } catch (SQLException io) {
            System.err.println("getGamePlay error " + io);
            //Logger.getLogger(DBConnect.class.getName()).log(Level.SEVERE, null, io);
        }

        if (oppName.equals(Text.HOST)) {
            oppName = oppName + pPid;
        }
        u = new GamePlay(gid, pid1, pid2, oppName, symbol1, symbol2, playBoard, playedID, sid);
        JavaFXApplication.gameID = gid;
        return u;
    }

    public void updateGame(String gid, String myID, String sBoard,
            int setCode, String idWin, String idLost) {
        try {
            s = con.createStatement();
            String updateSQL = "update Game set sid = " + setCode + ", lastPlayedId = " + myID + ", playboard = '" + sBoard + "', idWin = " + idWin + " ,idLost = " + idLost + " "
                    + "where gid = '" + gid + "';";
            int res = s.executeUpdate(updateSQL);
        } catch (Exception io) {
            System.err.println("updateGame error " + io);
            Logger.getLogger(DBConnect.class.getName()).log(Level.SEVERE, null, io);
        }

    }//update
    
    public void updateGame(String pid1, String pid2, String myID, String sBoard,
            int setCode, String idWin, String idLost) {
        try {
            s = con.createStatement();
            String updateSQL = "update Game set sid = " + setCode + ", lastPlayedId = " + myID + ", playboard = '" + sBoard + "', idWin = " + idWin + " ,idLost = " + idLost + " "
                    + "where pid1 = " + pid1 + " or pid2 = " + pid2 + " and sid = " + 31 + ";";
            int res = s.executeUpdate(updateSQL);
        } catch (Exception io) {
            System.err.println("updateGame error " + io);
            Logger.getLogger(DBConnect.class.getName()).log(Level.SEVERE, null, io);
        }

    }//update

    public String getLeaderboard() {
        ResultSet rs;
        String text = "";
        try {
            rs = s.executeQuery("select pName , SUM(idWin = p.pid) as wins, SUM(idLost = p.pid) as lost , SUM(idLost = 0 AND idWin = 0) as draw ,count(gid) AS Games_Played\n" +
                                "FROM Players p, Game g where (pid1 = p.pid or pid2 = p.pid) and g.sid = 32\n" +
                                "group by p.pid\n" +
                                "ORDER by wins DESC, draw DESC, lost LIMIT 10;");
           
            while (rs.next()) {
                String user = rs.getString("pName");
                String wins = rs.getString("wins");
                String lost = rs.getString("lost");
                String draw = rs.getString("draw");
                String play = rs.getString("Games_Played");
                int iAdd = 25-user.length();
                String padded = String.format("%-"+(user.length()+iAdd)+"s", user);
                text+=padded+" wins: "+wins+"\t losts: "+lost+"\t draws: "+draw+"\t games played: "+play+"\n";
            }
        } catch (SQLException io) {
            System.err.println("getListeningUser error " + io);
            Logger.getLogger(DBConnect.class.getName()).log(Level.SEVERE, null, io);
        }
        return text;
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    public void clean(String myID, String oppID) {
        try {
            s = con.createStatement();
            //SET SQL_SAFE_UPDATES = 0;
            
            String updateSQL ="UPDATE Game SET sid = '"+Code.GAME_FINISH+"', lastPlayedId = '"+JavaFXApplication.myID+ "' , idWin = '"+JavaFXApplication.oppID+"', idLost = '"+JavaFXApplication.myID+"'"
                               + "where gid = '"+JavaFXApplication.gameID+"';";
            int res = s.executeUpdate(updateSQL);
            System.out.println("The Number or records deleted is      " + res);
        } catch (Exception io) {
            System.err.println("clean (delete user)error " + io);
            Logger.getLogger(DBConnect.class.getName()).log(Level.SEVERE, null, io);
        }

        try {
            s = con.createStatement();
            String deleteSQL = " Delete from Players where pid ='" + myID + "' and pName = '"+Text.HOST+"';";
            int res = s.executeUpdate(deleteSQL);
            System.out.println("The Number or records deleted is      " + res);
        } catch (Exception io) {
            System.err.println("clean (delete user)error " + io);
            Logger.getLogger(DBConnect.class.getName()).log(Level.SEVERE, null, io);
        }
        try {
            s = con.createStatement();
            String deleteSQL = " Delete from Connect where pid = '" + myID + "' ;";
            int res = s.executeUpdate(deleteSQL);
            System.out.println("The Number or records deleted is      " + res);
        } catch (Exception io) {
            System.err.println("clean (delete call)error " + io);
            Logger.getLogger(DBConnect.class.getName()).log(Level.SEVERE, null, io);
        }
    }
    
    public void close() {
        try {
            s.close();
        } catch (SQLException ex) {
            System.err.println("close connection error " + ex);
            Logger.getLogger(DBConnect.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}//end of DBC class

/**
 * Help class
 */
class GamePlay {

    final String gid, pid1, pid2, playedID, sid, oppName, playBoard;
    char symbol1, symbol2;

    GamePlay(String gid, String pid1, String pid2, String oppName,
            char symbol1, char symbol2, String playBoard, String playedID, String sid) {
        this.gid = gid;
        this.pid1 = pid1;
        this.pid2 = pid2;
        this.oppName = oppName;
        this.symbol1 = symbol1;
        this.symbol2 = symbol2;
        this.playBoard = playBoard;
        this.playedID = playedID;
        this.sid = sid;
    }
}

/**
 * Help class
 */
class User {

    String pid, name, status;

    public User(String pid, String name, String status) {
        this.pid = pid;
        this.name = name;
        this.status = status;
    }
}