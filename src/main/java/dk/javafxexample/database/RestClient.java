package dk.javafxexample.database;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.client.Client;         // Пакет REST клиента
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import dk.entitycore.entity.student.Student;


public class RestClient
{

    private static final Logger LOG = Logger.getLogger(RestClient.class.getSimpleName());

    // Классы протокола REST
    private Client client;                          // Клиент протокола REST
    private WebTarget webTarget;                    // Настройки пути к точке REST
    private Invocation.Builder invocationBuilder;   // Построитель запросов (GET, PUT, DELETE, POST)
    
    // Путь к точке REST  на сервере
    //            указание Http узел(IP) порт   путь к приложениям REST  
    //                        |      |    |      /
    private String target  ="http://hs:8080/database/";

    public RestClient()
    {
    }

    /**
     * Инициализация клиента 
     */
    public void init()
    {
        // Создание клиента REST
        client = ClientBuilder.newClient();
    }

    
    /**
     * Отправка REST запроса к точке REST 
     * полный путь = /database/students/connected
     * 
     * @return 
     */
    public boolean connect()
    {
        
        boolean result = false;
        // Создание полного пути к точке REST
        webTarget = client.target(target).path("students").path("connected");
        
        // Создание экземпляра построителя запросов        JSON
        //                                                  |
        invocationBuilder =  webTarget.request(MediaType.APPLICATION_JSON);
        
        // Сформировать запроси и отправить
        //        ответ от севера                  отправка запроса (Http GET)
        //          |                                      /
        Response response =             invocationBuilder.get();
        
        // Действия с ответом REST
        if (response != null)
        {
            LOG.log(Level.INFO, "Информация об ответе REST ==> {"+response.getStatusInfo()+"}");
            //       строка ответа  чтение содержимого контейнера
            //       /                 |       класс String
            //       |                 |          |
            String resp = response.readEntity(String.class);
            LOG.log(Level.INFO, "Ответ REST ==> {"+resp+"}");
        }
        else
        {
            LOG.log(Level.SEVERE, "Ошибка: ответ REST не получен");
        }
        
        return result;
    }       
    
    
    /**
     * Вызов удаленного 
     * 
     * @param student
     * @return 
     */
    public Student addStudent(Student student)
    {
       Student studentFromDb = null;
        
        webTarget = client.target(target).path("students").path("addStudent");
        Invocation.Builder invocationBuilder =  webTarget.request(MediaType.APPLICATION_JSON);
        
        //                                  Вызов метода http {PUT}
        //                                     |          экземпляр класса Student
        //                                     |                 |
        Response response = invocationBuilder.put(Entity.entity(student, MediaType.APPLICATION_JSON));
        
        if (response != null)
        {
            studentFromDb  = response.readEntity(Student.class);
            if (student != null)
            {
                LOG.log(Level.INFO, "Ответ сервера ==> Добавлен студент {"+studentFromDb.getLastName()+"}");
                
            }   
            else
            {
                LOG.log(Level.INFO, "Ответ сервера ==> Ошибка добавления студента");
            }
            
        }    
        
        return studentFromDb;
    }       
    
    
     public boolean deleteStudent(Student student)
    {
        
        webTarget = client.target(target).path("students").path("deleteStudent").path("5");
        
        Invocation.Builder invocationBuilder =  webTarget.request(MediaType.APPLICATION_JSON);
      
        Response response = invocationBuilder.delete();
        
        return false;
    
    }       
            
    
}
