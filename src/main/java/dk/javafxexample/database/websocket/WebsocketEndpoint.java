
package dk.javafxexample.database.websocket;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.MessageHandler;
import javax.websocket.Session;
import dk.entitycore.packet.Packet;


/**
 * Получение  и передача websocket сообщений, обработка ошибок
 * 
 * 
 */
public class WebsocketEndpoint  extends Endpoint 
{
    private static final Logger LOG = Logger.getLogger(WebsocketEndpoint.class.getSimpleName());
     
    // Класс, представляющий текущий сеанс связи с сервером Websocket
    private Session session;
    private PacketProcessor packetProcessor = new PacketProcessor();
    
     
    // Блокирующая очередь для передачи сообщений {Packet} в графический интерфейс пользователя
    private   BlockingQueue<Packet> queue ;
    
    public WebsocketEndpoint()
    {
        
    }
    
    /**
     * Метод вызывается автоматически при подключении сервера к клиенту
     *
     * @param session
     * @param ec
     */
    @Override
    public void onOpen(Session session, EndpointConfig ec)
    {
        this.session = session;
        if (queue != null)
        {    
            this.packetProcessor.setQueue(queue);
        }
        this.session.addMessageHandler(new MessageHandler.Whole<String>()
        {
            
            @Override
            // Получает пакеты от сервера WebsocketServer
            public void onMessage(String message)
            {
                LOG.log(Level.INFO, "Получено сообщение ==> "+message);
                // Переправляет строку в класс PacketProcessor
                packetProcessor.processPacket(message);
                
            }
        });
    }
    // 
    //session.getBasicRemote().sendText("Привет лаборатория CWLAB ");

    /**
     * Метод для пересылки сообщений серверу Websocket
     */
    public void sendMessageToServer(String text)
    {
        try
        {
            // Из документации проекта Websocket
            this.session.getBasicRemote().sendText(text);
        } catch (IOException ex)
        {
            LOG.log(Level.SEVERE,"Ошибка отправки текста на сервер Websocket: "+ex.getMessage());
        }
        
    }
   
    
     /**
     * @param queue the queue to set
     */
    public void setQueue(BlockingQueue<Packet> queue)
    {
        this.queue = queue;
    }


    
}
