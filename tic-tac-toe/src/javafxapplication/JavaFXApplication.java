
package javafxapplication;


import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 *
 * @author Michael Beno
 */
public class JavaFXApplication extends Application {
    public static String myID = "-1";
    public static String gameID = "-1";
    public static String oppID = "-1";
    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("FXMLDocument.fxml"));
        Scene scene = new Scene(root);
        stage.setTitle("My App");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
        stage.setOnCloseRequest((WindowEvent we) -> {
            System.out.println("Stage is closing, id: "+myID);
            DBConnect dbc =new DBConnect();
            dbc.setUserStatus(myID, Code.USER_OFFLINE);
            dbc.clean(myID,oppID);
            dbc.close();
        });      
    }
 
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
}
