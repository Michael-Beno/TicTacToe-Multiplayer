/*
 * To change this license header, choose License Headers in Project 
 *  Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javafxapplication;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import static javafxapplication.JavaFXApplication.myID;

/**
 *
 * @author Mick
 */
public class MyGUI implements Initializable {

    private DBConnect dbc;
    private GamePlay gp;
    private User u = null;
    private final int TICK = 1000;
    private final int TIME_OUT = 10;
    private int countdown = TIME_OUT;
    private String opponentID = "-1";
    private String opponentName = "";
    private int gameStatus = Code.GAME_NOT_EXIST;
    private char symbol;
    private String played = "";
    private String pid1 = "", pid2 = "";
    private String playboard = "         ";
    private String opponentStatus = "";
    
    @FXML private Button btnGuestLogin;
    @FXML private Label lblStatus;
    @FXML private Label names;
    @FXML private TextField textFieldNameLogin;
    @FXML private PasswordField textFieldPasswordLogin;
    @FXML private Button btnLogin;
    @FXML private AnchorPane gameLay;
    @FXML private Button btn0;
    @FXML private Button btn1;
    @FXML private Button btn2;
    @FXML private Button btn3;
    @FXML private Button btn4;
    @FXML private Button btn5;
    @FXML private Button btn6;
    @FXML private Button btn7;
    @FXML private Button btn8;
    @FXML private VBox box;
    @FXML private Button btnLogOut;
    @FXML private Label lblLoginIfo;
    @FXML private RadioButton radioX;
    @FXML private ToggleGroup tgSymbol;
    @FXML private RadioButton radioO;
    @FXML private Pane challenge;
    @FXML private Button btnReject;
    @FXML private Button btnAccept;
    @FXML private Label lblChallenge;
    @FXML private Label lblInfoConnect;
    @FXML private AnchorPane loginLay;
    @FXML private Button btnOk;
    @FXML private Label leaderBoard;
    
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
        lblStatus.setText(Text.CONNECTING);
        dbc = new DBConnect();

        Timeline time = new Timeline();
        time.setCycleCount(Timeline.INDEFINITE);
        KeyFrame kf = new KeyFrame(Duration.millis(TICK), (ActionEvent event) -> {
            timeTasks();
        });
        time.getKeyFrames().add(kf);
        time.playFromStart();
        //time.play();
    }

    private void timeTasks() {
        
        int line = Code.CALL_NONE;
        int myStatus = dbc.getMyPlayerStatus(myID);//myStatus;

        if (myStatus == Code.USER_BUSY && gameStatus == Code.GAME_RUNNING) {
            dbc.setUserStatus(myID, Code.USER_PLAYING);
        }
        
        leaderBoard.setText(dbc.getLeaderboard());
        
            u = dbc.getIncommingCall(myID);
        if (!"".equals(u.name)) {
                //dbc.setUserStatus(myID, Code.USER_BUSY);
                //line = Code.CALL_RECEIVING;
                 opponentID = u.pid;
                 opponentName = u.name;
                 opponentStatus = u.status;
                 line = Integer.parseInt(u.status);
         }else {opponentName = "none"; opponentID = "-1"; opponentStatus = "none";
            dbc.setUserStatus(myID, Code.USER_LOGGED_IN);
        }
  
//*******************GAME
         gp = dbc.getGamePlay(myID);

        if (!"".equals(gp.gid)) {
            gameStatus = Integer.parseInt(gp.sid);
            if (gameStatus == Code.GAME_RUNNING) {
                dbc.setUserStatus(myID, Code.USER_PLAYING);
            }
            if (myID.equals(gp.pid1)) {
                opponentID = gp.pid2;
                symbol = gp.symbol2;
            } else {
                opponentID = gp.pid1;
                symbol = gp.symbol1;
            }
            played = gp.playedID;
            pid1 = gp.pid1;
            pid2 = gp.pid2;
            playboard = gp.playBoard;
        } 
        else {  //return to online when game finish
          if(myStatus != Code.USER_BUSY)
            dbc.setUserStatus(myID, Code.USER_LOGGED_IN);
            gameStatus = Code.GAME_NOT_EXIST;
        }
        
        playersListTask(myStatus, line, u);
        gameplay();
        statusInfo(myStatus, line, gameResult());
//         lblStatus.setText("Game: MyID:"+myID+" pid1:"+gp.pid1+" symbol:"+gp.symbol1+
//             " pid2:"+gp.pid2+" symbol:"+gp.symbol2+" opponent:"+opponentID+" sid"+gp.sid+" "+gp.gid+
//              "\nmystatus:"+myStatus+" line "+line + " jsem "+myID+ " "+opponentName+" "+opponentID+" "+opponentStatus+
//                "\n gameStatus "+gameStatus +" "+gp.playBoard+
//                      "");
    }
    
    private boolean isMyTurn = true;
    private void statusInfo(int myStatus, int line, int gameResult) {
         btnOk.setVisible(false);
        switch (myStatus) {
            case Code.USER_OFFLINE:
                lblStatus.setText(Text.CONNECTED + displayActivity());
                names.setText(Text.REGISTERED_USERS);
                break;
            case Code.USER_LOGGED_IN:
                lblStatus.setText(Text.WELCOME + " " + dbc.getUserName(myID) + displayActivity());
                names.setText(Text.AVAILABLE_USERS);
                lblInfoConnect.setText(Text.CHOOSE_OPPONENT);
               // btnOk.setVisible(false);
                break;
            case Code.USER_BUSY:
                switch (line) {
                    case Code.CALL_NONE:
                        lblStatus.setText(Text.WELCOME + " " + dbc.getUserName(myID) + ". Call Canceled" + displayActivity());
                        names.setText("You are calling");
                        lblInfoConnect.setText("Challenge Canceled");
                        challenge.setVisible(false);
                        countdown = TIME_OUT;
                        break;
                    case Code.CALL_I_AM_DIALING:
                        lblStatus.setText(Text.WELCOME + " " + dbc.getUserName(myID) + ". You are calling: " + opponentName + " " + displayActivity());
                        names.setText("You are calling");
                        lblInfoConnect.setText("You are calling: " + opponentName + ". " + "Wait for respond " + --countdown);
                        if (countdown <= 0 && isCountdownRunning) {
                            dbc.rejectCall(myID);
                        }
                        break;
                    case Code.CALL_RECEIVING:
                        lblStatus.setText(Text.WELCOME + " " + dbc.getUserName(myID) + ". It is ringing: " + opponentName + " " + displayActivity());
                        names.setText("Incomming Call");
                        lblChallenge.setText(opponentName + "\nWants play");
                        lblInfoConnect.setText("Incomming call: " + opponentName + ". " + "Please respond " + --countdown);
                        challenge.setVisible(true);
                        break;
                }
                break;
            case Code.USER_PLAYING:
                 lblStatus.setText(Text.WELCOME + " " + dbc.getUserName(myID) + displayActivity());
                names.setText("Your opponent");
                String play = "";
                if (!myID.equals(played)) {
                    isMyTurn = false;
                    play = "YOUR TURN";
                } else {
                    isMyTurn = true;
                    play = "Wait, " + opponentName + " playing" + " ";
                }
                lblInfoConnect.setText("Opponent: " + opponentName + " Your symbol:" + symbol + " " + play);
                switch (gameResult) {
                    case Code.GAME_YOU_WIN:
                        lblInfoConnect.setText("YOU WIN");
                        isMyTurn = true;
                         btnOk.setVisible(true);
                        break;
                    case Code.GAME_YOU_LOSE:
                        lblInfoConnect.setText("YOU LOSE");
                        isMyTurn = true;
                         btnOk.setVisible(true);
                        break;
                    case Code.GAME_DRAW:
                        lblInfoConnect.setText("DRAW");
                        isMyTurn = true;
                         btnOk.setVisible(true);
                        break;
                }
                break;
        }
    }

    private int playerCount;
    private void playersListTask(int myStatus, int line, User u) {
        ArrayList<User> ar = new ArrayList<>();
     
        if (myStatus == Code.USER_OFFLINE) {
            ar = dbc.getListPlayers(Code.ALL_PLAYERS);
        } else if (myStatus == Code.USER_LOGGED_IN) {
            ar = dbc.getListPlayers(Code.ALL_OPPONENTS);
        } 
        else if (myStatus == Code.USER_PLAYING) //ar = dbc.getListPlayers(Code.ALL_PLAYERS);
        {
            ar = dbc.getArrOpponent(myID, opponentID, Code.USER_PLAYING);
        }
        else if(line == Code.CALL_RECEIVING){
            u.status = Code.CALL_RECEIVING+"";
            ar.add(u);
        } else if(line == Code.CALL_I_AM_DIALING){
            u.status = Code.CALL_I_AM_DIALING+"";
            ar.add(u);
        }
         
        if (playerCount != ar.size()) { //Ivalidate only if size changed
            box.getChildren().clear();
            for (int i = 0; i < ar.size(); i++) {
                Button btnPlayers = new Button(ar.get(i).name);
                btnPlayers.setOnAction((ActionEvent event) -> {
                    for (int j = 0; j < box.getChildren().size(); j++) {
                        box.getChildren().get(j).setDisable(true);
                    }
                    //call player
                    callTheOpponent(btnPlayers);
                });
                btnPlayers.setId(ar.get(i).pid);
                btnPlayers.setMaxWidth(150);
                btnPlayers.setAlignment(Pos.BASELINE_LEFT);
                btnPlayers.setDisable(true);
                box.getChildren().add(btnPlayers);
            }
        }
        for (int i = 0; i < box.getChildren().size(); i++) {

            if (myStatus == Code.USER_LOGGED_IN) {
                box.getChildren().get(i).setDisable(false);
            }
            if (ar.get(i).status.equals(Code.USER_LOGGED_IN + "")) {
                box.getChildren().get(i).setStyle("-fx-font: 12 arial; -fx-base: #b6e7c9;");//green
            } else if (ar.get(i).status.equals(Code.USER_BUSY + "")
                    || ar.get(i).status.equals(Code.CALL_I_AM_DIALING + "")
                    || ar.get(i).status.equals(Code.CALL_RECEIVING + "")) {
                box.getChildren().get(i).setDisable(true);
                box.getChildren().get(i).setStyle("-fx-font: 12 arial; -fx-base: #e7e7c9;");//yellow
            } else if (ar.get(i).status.equals(Code.USER_PLAYING + "")) {
                box.getChildren().get(i).setDisable(true);
                opponentName = ar.get(i).name;
                box.getChildren().get(i).setStyle("-fx-font: 12 arial; -fx-base: #ffe7b6;");//orange
            } else {
                box.getChildren().get(i).setStyle(""); //USER_OFFLINE    
            }
             if(!box.getChildren().get(i).getId().equals(ar.get(i).pid))
                box.getChildren().clear();
        }
        playerCount = box.getChildren().size();
    }
 
    boolean isCountdownRunning = false;
    private void callTheOpponent(Button b) {
        //create call record
        dbc.setUserStatus(myID, Code.USER_BUSY);
        dbc.setUserStatus(b.getId(), Code.USER_BUSY);
        dbc.setCallOpponent(myID, b.getId(), Code.CALL_I_AM_DIALING);
        
        isCountdownRunning = true;
        countdown = TIME_OUT;
    }
    
    @FXML private void textfieldNameOnKeyRelease(KeyEvent event) {
        String inputName = textFieldNameLogin.getText();
        lblLoginIfo.setText("");
        textFieldPasswordLogin.setText("");
        btnLogin.setText("LogIN");
        if (!inputName.equals("")) {
            btnGuestLogin.setDisable(true);
            btnLogin.setDisable(false);
        } else {
            btnLogin.setDisable(true);
            btnGuestLogin.setDisable(false);
        }
    }

    @FXML private void handleLoginTextField(MouseEvent event) {
        btnLogin.setText("LogIN");
        isExistingUser = false;
        lblLoginIfo.setText("");
        textFieldPasswordLogin.setText("");
    }

    boolean isExistingUser = false;
    @FXML private void handlePasswordField(MouseEvent event) {
        String inputName = textFieldNameLogin.getText();
        inputName = inputName.toUpperCase();
        textFieldNameLogin.setText(inputName);
        if (dbc.isNameExist(inputName)) {
            btnLogin.setText("Existing User Login");
            isExistingUser = true;
            lblLoginIfo.setText("Enter Your Pasword");
        } else if (inputName.isEmpty()) {

        } else {
            btnLogin.setText("Create User");
            isExistingUser = false;
            lblLoginIfo.setText("Create New Pasword");
        }
    }  

    @FXML private void handleButtonGuestLogin(ActionEvent event) {
        String passcode = Long.toHexString(Double.doubleToLongBits(Math.random()));
        dbc.createUser(Text.HOST, passcode);          //create user
        myID = dbc.getPlayersId(Text.HOST, passcode);  //obtaing myID
        //myStatus = 2;
        dbc.setUserStatus(myID, Code.USER_LOGGED_IN); //set status 2
        btnLogOut.setVisible(true);
        lblStatus.setText("You Are ONLINE As \"" + Text.HOST + "" + myID + "\"");
        gameLay.setVisible(true);
        loginLay.setVisible(false);
    }

    @FXML private void handleButtonUserLogin(ActionEvent event) {
        if (isExistingUser) {
            String myName = textFieldNameLogin.getText();
            myID = dbc.getPlayersId(myName, textFieldPasswordLogin.getText());
            if (!"".equals(myID)) {
                //myStatus = 2;
                dbc.setUserStatus(myID, Code.USER_LOGGED_IN); //set status 2
                btnLogOut.setVisible(true);
                lblStatus.setText("You are logged in as: "
                        + textFieldNameLogin.getText());
                lblLoginIfo.setText("");
                gameLay.setVisible(true);
                loginLay.setVisible(false);
            } else {
                lblLoginIfo.setText("Wrong Password");
            }
        } else {
            lblLoginIfo.setText("password is mandatory");
            if (!textFieldPasswordLogin.getText().isEmpty()) {
                dbc.createUser(textFieldNameLogin.getText(),
                        textFieldPasswordLogin.getText());
                textFieldPasswordLogin.setText("");
                btnLogin.setText("LogIN");
                lblLoginIfo.setText("");
            }
        }
    }

    @FXML private void handelBtnLogOut(ActionEvent event) {
        //myStatus = 0;
        dbc.setUserStatus(myID, Code.USER_OFFLINE);
        dbc.clean(myID,opponentID);
        myID = "";
        btnLogOut.setVisible(false);
        gameLay.setVisible(false);
        loginLay.setVisible(true);
        lblStatus.setText("");
        names.setText("Registered users");
        //wipeButtons();
    }
    
    private char mySymbol = 'O';
    @FXML private void handelBtnAccept(ActionEvent event) {
        char oppSymbol = 'X';
        if (mySymbol == 'O') {
            oppSymbol = 'X';
        } else {
            oppSymbol = 'O';
        }
        dbc.startGame(myID, opponentID, "O", "X", Code.GAME_RUNNING);
        dbc.setUserStatus(myID, Code.USER_PLAYING);
        challenge.setVisible(false);
        dbc.rejectCall(opponentID);
        isCountdownRunning = false;
        JavaFXApplication.oppID = opponentID;
    }

    @FXML private void handleBtnReject(ActionEvent event) {
        //for canceling from my side use myID
        dbc.rejectCall(opponentID);
    }

    @FXML private void handleOkButton(ActionEvent event) {
         //store game result
        if (result == Code.GAME_YOU_WIN) {
            dbc.updateGame(JavaFXApplication.gameID, myID, playboard, Code.GAME_FINISH, myID, opponentID);
        } else if (result == Code.GAME_YOU_LOSE) {
            dbc.updateGame(JavaFXApplication.gameID, myID, playboard, Code.GAME_FINISH, opponentID, myID);
        } else if (result == Code.GAME_DRAW) {
            dbc.updateGame(JavaFXApplication.gameID, myID, playboard, Code.GAME_FINISH, "0", "0");
        }
        isMyTurn = true;
    }
    
// @FXML private void handelCancelButton(ActionEvent event) { }
// @FXML private void handleRadioX(ActionEvent event) { mySymbol = 'X'; }
// @FXML private void handleRadioO(ActionEvent event) { mySymbol = 'O'; }

    @FXML private void handleBtn0(ActionEvent event) { setPllayboard(0); }
    @FXML private void handleBtn1(ActionEvent event) { setPllayboard(1); }
    @FXML private void handleBtn2(ActionEvent event) { setPllayboard(2); }
    @FXML private void handleBtn3(ActionEvent event) { setPllayboard(3); }
    @FXML private void handleBtn4(ActionEvent event) { setPllayboard(4); }
    @FXML private void handleBtn5(ActionEvent event) { setPllayboard(5); }
    @FXML private void handleBtn6(ActionEvent event) { setPllayboard(6); }
    @FXML private void handleBtn7(ActionEvent event) { setPllayboard(7); }
    @FXML private void handleBtn8(ActionEvent event) { setPllayboard(8); }
    
 
    private void setPllayboard(int btn){
         StringBuilder myNewBoard = new StringBuilder(playboard);
        myNewBoard.setCharAt(btn, symbol);
        dbc.updateGame(pid1, pid2, myID, myNewBoard.toString(), Code.GAME_RUNNING, "0", "0"); //dodat sid misto 0
        isMyTurn = true;
        btn0.setDisable(true);
        btn1.setDisable(true);
        btn2.setDisable(true);
        btn3.setDisable(true);
        btn4.setDisable(true);
        btn5.setDisable(true);
        btn6.setDisable(true);
        btn7.setDisable(true);
        btn8.setDisable(true);  
    }
    
    private void gameplay() {
        if ("-".equals(playboard.charAt(0) + "") || Code.GAME_RUNNING != gameStatus) {
            btn0.setDisable(isMyTurn);
            btn0.setText("");
        } else {
            btn0.setDisable(true);
            btn0.setText(playboard.charAt(0) + "");
        }

        if ("-".equals(playboard.charAt(1) + "") || Code.GAME_RUNNING != gameStatus) {
            btn1.setDisable(isMyTurn);
            btn1.setText("");
        } else {
            btn1.setDisable(true);
            btn1.setText(playboard.charAt(1) + "");
        }

        if ("-".equals(playboard.charAt(2) + "") || Code.GAME_RUNNING != gameStatus) {
            btn2.setDisable(isMyTurn);
            btn2.setText("");
        } else {
            btn2.setDisable(true);
            btn2.setText(playboard.charAt(2) + "");
        }

        if ("-".equals(playboard.charAt(3) + "") || Code.GAME_RUNNING != gameStatus) {
            btn3.setDisable(isMyTurn);
            btn3.setText("");
        } else {
            btn3.setDisable(true);
            btn3.setText(playboard.charAt(3) + "");
        }

        if ("-".equals(playboard.charAt(4) + "") || Code.GAME_RUNNING != gameStatus) {
            btn4.setDisable(isMyTurn);
            btn4.setText("");
        } else {
            btn4.setDisable(true);
            btn4.setText(playboard.charAt(4) + "");
        }

        if ("-".equals(playboard.charAt(5) + "") || Code.GAME_RUNNING != gameStatus) {
            btn5.setDisable(isMyTurn);
            btn5.setText("");
        } else {
            btn5.setDisable(true);
            btn5.setText(playboard.charAt(5) + "");
        }

        if ("-".equals(playboard.charAt(6) + "") || Code.GAME_RUNNING != gameStatus) {
            btn6.setDisable(isMyTurn);
            btn6.setText("");
        } else {
            btn6.setDisable(true);
            btn6.setText(playboard.charAt(6) + "");
        }

        if ("-".equals(playboard.charAt(7) + "") || Code.GAME_RUNNING != gameStatus) {
            btn7.setDisable(isMyTurn);
            btn7.setText("");
        } else {
            btn7.setDisable(true);
            btn7.setText(playboard.charAt(7) + "");
        }

        if ("-".equals(playboard.charAt(8) + "") || Code.GAME_RUNNING != gameStatus) {
            btn8.setDisable(isMyTurn);
            btn8.setText("");
        } else {
            btn8.setDisable(true);
            btn8.setText(playboard.charAt(8) + "");
        }

    }
    
    private int result = Code.GAME_RUNNING;
    private int gameResult() {

        if (playboard.charAt(0) == 'X' && playboard.charAt(1) == 'X' && playboard.charAt(2) == 'X'
                || playboard.charAt(3) == 'X' && playboard.charAt(4) == 'X' && playboard.charAt(5) == 'X'
                || playboard.charAt(6) == 'X' && playboard.charAt(7) == 'X' && playboard.charAt(8) == 'X'
                || playboard.charAt(0) == 'X' && playboard.charAt(3) == 'X' && playboard.charAt(6) == 'X'
                || playboard.charAt(1) == 'X' && playboard.charAt(4) == 'X' && playboard.charAt(7) == 'X'
                || playboard.charAt(2) == 'X' && playboard.charAt(5) == 'X' && playboard.charAt(8) == 'X'
                || playboard.charAt(0) == 'X' && playboard.charAt(4) == 'X' && playboard.charAt(8) == 'X'
                || playboard.charAt(2) == 'X' && playboard.charAt(4) == 'X' && playboard.charAt(6) == 'X') {

            if (symbol == 'X') {
                result = Code.GAME_YOU_WIN;
            } else {
                result = Code.GAME_YOU_LOSE;
            }
            btnOk.setVisible(true);
        } else if (playboard.charAt(0) == 'O' && playboard.charAt(1) == 'O' && playboard.charAt(2) == 'O'
                || playboard.charAt(3) == 'O' && playboard.charAt(4) == 'O' && playboard.charAt(5) == 'O'
                || playboard.charAt(6) == 'O' && playboard.charAt(7) == 'O' && playboard.charAt(8) == 'O'
                || playboard.charAt(0) == 'O' && playboard.charAt(3) == 'O' && playboard.charAt(6) == 'O'
                || playboard.charAt(1) == 'O' && playboard.charAt(4) == 'O' && playboard.charAt(7) == 'O'
                || playboard.charAt(2) == 'O' && playboard.charAt(5) == 'O' && playboard.charAt(8) == 'O'
                || playboard.charAt(0) == 'O' && playboard.charAt(4) == 'O' && playboard.charAt(8) == 'O'
                || playboard.charAt(2) == 'O' && playboard.charAt(4) == 'O' && playboard.charAt(6) == 'O') {

            if (symbol == 'O') {
                result = Code.GAME_YOU_WIN;
            } else {
                result = Code.GAME_YOU_LOSE;
            }
            btnOk.setVisible(true);
        } else {
            result = Code.GAME_DRAW;
            for (int i = 0; i < playboard.length(); i++) {
                if (playboard.charAt(i) == '-') {
                    result = Code.GAME_RUNNING;
                }
            }
        }
        if (result == Code.GAME_DRAW) {
            btnOk.setVisible(true);
        }
        return result;
    }
    
     private boolean b;
    private String displayActivity() {
        String act;
        if (b) {
            act = "***";
        } else {
            act = "....";
        }
        b = !b;
        return act;
    }

    @FXML
    private void handleRadioX(ActionEvent event) {
    }

    @FXML
    private void handleRadioO(ActionEvent event) {
    }

    @FXML
    private void handelCancelButton(ActionEvent event) {
    }
}
