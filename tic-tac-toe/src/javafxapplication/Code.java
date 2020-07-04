
package javafxapplication;

/**
 *
 * @author Michael Beno
 */
interface Code {
    int DEFAULT = -1;
    int USER_OFFLINE = 0;
    int USER_PLAYING = -3;
    int USER_BUSY = 1;
    int USER_LOGGED_IN = 2;
    int USERTYPE_REGISTERED = 101;
  
    int ALL_PLAYERS = 234;
    int ALL_OPPONENTS = 200;
    int ONE_OPPONENT = 201;
    int CALL_NONE = 20;
    int CALL_I_AM_DIALING = 25; 
    int CALL_REJECT = 21;
    int CALL_INCOMMING = 22;
    int CALL_RECEIVING = 24;
    
    int GAME_NOT_EXIST = 30;
    int GAME_RUNNING = 31;
    int GAME_FINISH = 32;
    int GAME_YOU_WIN = 41;
    int GAME_YOU_LOSE = 42;
    int GAME_DRAW = 43;
    
}
