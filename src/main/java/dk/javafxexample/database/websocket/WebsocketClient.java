
package dk.javafxexample.database.websocket;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.websocket.ClientEndpointConfig;
import javax.websocket.DeploymentException;
import org.glassfish.tyrus.client.ClientManager;
import dk.entitycore.packet.Packet;


public class WebsocketClient
{

    private static final Logger LOG = Logger.getLogger(WebsocketClient.class.getSimpleName());
    
    
    // Экземпляр класса клиента Websocket
    //                            |
    private static ClientManager client;
    
    // Оконечная точка протокола Websocket - реализует все функции обмена между клиентом WS и сервером WS
    //
    private WebsocketEndpoint websocketEndpoint;
    
    // Настройки протокола Websocket
    // 
    private final ClientEndpointConfig config = ClientEndpointConfig.Builder.create().build();
    
    // Точка подключения
    //       название класса    название экземпляра класса
    //       |                  /
    private URI                server;
   
    
    // Блокирующая очередь для передачи сообщений {Packet} в графический интерфейс пользователя
    private   BlockingQueue<Packet> queue ;
    

    /**
     * Метод инициализации (подготовительные мероприятия перед использованием  клиента Websocket)
     */
    public void init()
    {
        // Создание экземпляра класса ClientManager
        client = ClientManager.createClient();
        try
        {
            // Создание строки подключения к Websocket серверу
            //    Вызов конструктора класса URI со строковым параметром конструктора
            //
            server = new URI("ws://localhost:8080/WebSocketServer-0.0.1/websocket");
            //server = new URI("ws://cwlab.hiik.ru:8080/WebSocketServer-0.0.1/websocket");
            
        //         класс ошибки синтаксиса класса URI   экземпляр класса ошибки
        //            |                                |
        } catch (URISyntaxException                   ex)
        {
            LOG.log(Level.SEVERE, String.format("%-40s %-120s", "Ошибка формирования строки:", "{" + server.toString() + "}"));
            LOG.log(Level.SEVERE, String.format("%-40s %-120s", "Описание ошибки:", "{" + ex.getMessage() + "}"));
        }
    }
    
    
    
    /**
     * Реализует подключение к серверу
     * 
     */
    public void connect() 
    {
        try
        {
            // Создание точки подключения Websocket
            websocketEndpoint = new WebsocketEndpoint();
            
            if (this.queue != null )
            {
                websocketEndpoint.setQueue(queue);
            }    
            
            // Вызов метода подключения {connectToServer}
            //                        экземпляр  класса WebsocketEndpoint  конфигурация 
            //                              |                                |
            client.connectToServer (websocketEndpoint,               config, server);
            
            // Проверка подключения 
            if (client != null)
            {
                LOG.log(Level.INFO, "Подключено к серверу: {"+server+"}");
            }   
        } 
          // Ошибка развертывания 
          //    
          catch (DeploymentException error)
        {
             LOG.log(Level.SEVERE, "Ошибка подключения к серверу: {"+server+"}");
             LOG.log(Level.SEVERE, "Описание ошибки: {"+error.getMessage()+"}");
         
        // Ошибка ввода-вывода
        //
        } catch (IOException ex)
        {
             LOG.log(Level.SEVERE, "Ошибка вводв-вывода к серверу: {"+server+"}");
        }
    }
    
    
    
    /**
     * @return the websocketEndpoint
     */
    public WebsocketEndpoint getWebsocketEndpoint()
    {
        return websocketEndpoint;
    }
    
    
    
    
    /**
     * @param queue the queue to set
     */
    public void setQueue(BlockingQueue<Packet> queue)
    {
        this.queue = queue;
    }


    
}

