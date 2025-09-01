package dk.javafxexample;

import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;


public class MainApp extends Application {

    private static final Logger LOG = Logger.getLogger(MainApp.class.getSimpleName());

     /**
     * Стартовая точка запуска программы
     * 
     * @param args 
     */
    public static void main(String[] args)
    {
        // Запуск приложение (вызывает функцию start()
        launch(args);
    }
    
    @Override
    public void start(Stage stage) throws Exception 
    {
        // Установка пути к файлу графического интерфейса Student.fxml
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/Student.fxml"));
        
        // Загрузка основной сцены для отображения графики
        Scene scene = new Scene(root);
        
        // Установка стилей отображения элементов
        scene.getStylesheets().add("/styles/Styles.css");
        
        // Установка имени приложения в заголовке графического окна
        stage.setTitle("Пример приложения JavaFX");
        stage.setScene(scene);
        
        // Отображения сцены приложения
        stage.show();
        
        
        // Обработка события закрытия окна
        stage.setOnCloseRequest(new EventHandler<WindowEvent>()
        {
            @Override
            public void handle(WindowEvent t)
            {
                LOG.log(Level.INFO, "Завершение работы программы");
                Platform.exit();
                System.exit(0);
            }
            
        });
        
    }

    
    
   

}
