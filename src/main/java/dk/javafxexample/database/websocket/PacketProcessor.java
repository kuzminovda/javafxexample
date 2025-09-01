package dk.javafxexample.database.websocket;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import dk.entitycore.entity.student.Student;
import dk.entitycore.packet.Packet;


public class PacketProcessor
{

    private static final Logger LOG = Logger.getLogger(PacketProcessor.class.getSimpleName());
    private Gson gson = new Gson();
    
   
    
     
    // Блокирующая очередь для передачи сообщений {Packet} в графический интерфейс пользователя
    private   BlockingQueue<Packet> queue ;
    
     /**
     * @param queue the queue to set
     */
    public void setQueue(BlockingQueue<Packet> queue)
    {
        this.queue = queue;
    }

    
    
    /**
     * Функция обработки пакетов, поступивших от клиентов
     * - извлечение класса Packet из строки
     *
     * @param inputJson
     * @return
     */    
    //                             строка, полученная от  WebSocketClient             
    //                                    |
    public Packet processPacket(String inputJson)
    {
        Packet responcePacket = new Packet();  // Создание пустого ответного пакета
        Packet inputPacket = null;             // Создание входного пусто пакета
        try
        {
            
            // Восстановливает экземпляр класс Packet из строки
            inputPacket = gson.fromJson(inputJson, Packet.class);
            if (inputPacket != null)
            {
                // Извлекаем класс Student из поля body Packet
                if (extractCommand(inputPacket).isPresent())
                {
                    Optional<Packet> opt = extractClass(inputPacket);
                    if (opt.isPresent())
                    {
                        // Передача экземпляра класса Packet на обработку 
                        //
                        // routePacket(opt.get());
                        //
                        if (queue != null)
                        {
                            try
                            {
                                // Размещение экземпляра {Packet} в очереди 
                                queue.put(opt.get());
                                LOG.log(Level.INFO, "Пакет отправлен в GUI : {"+opt.get().getCommand()+"}");
                                
                            } catch (InterruptedException ex)
                            {
                                Logger.getLogger(PacketProcessor.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }   
                    }
                    else
                    {
                    
                    }    
                }
            } else
            {

            }
        } catch (JsonParseException ex)
        {

            LOG.log(Level.SEVERE, "Ошибка извлечения пакета {Packet} - json не содержит экземпляра класса {Packet}");
        }
        return responcePacket;
    }

    
      /**
     * Функция извлечения класса
     *
     * @param packet
     */
    private Optional<Packet> extractClass(Packet packet)
    {
        Optional<Packet> opt = Optional.empty();
        if (packet.getType() != null && !packet.getType().isEmpty())
        {
            if (packet.getType() != null && !packet.getType().isEmpty())
            {
                try
                {
                    Class cls = Class.forName(packet.getType());
                    if (cls != null)
                    {
                        if (packet.getBody() != null && !packet.getBody().isEmpty())
                        {
                            opt = Optional.of(packet);
                        } else
                        {
                            LOG.log(Level.SEVERE, "Ошибка извлечения класса {"+packet.getType()+"}");
                        }
                    }
                } catch (ClassNotFoundException ex)
                {
                    System.out.println(ex.toString());
                }
            }
        } else
        {
            LOG.log(Level.SEVERE, "Ошибка извлечения пакета: не указан тип данных");
        }
        return opt;
    }

    
     /**
     * Фукнция извлечения пакетов
     *
     * @param packet
     * @return
     */
    private Optional<String> extractCommand(Packet packet)
    {
        Optional<String> opt = Optional.empty();
        if (packet.getCommand() != null && !packet.getCommand().isEmpty())
        {
            opt = Optional.of(packet.getCommand());
            LOG.log(Level.INFO, "Извлечен пакет c командой: {" + packet.getCommand() + "}");
        } else
        {
            LOG.log(Level.SEVERE, "Ошибка извлечения команды пакета {Packet}: получен пакет с пустой командой");
        }
        return opt;
    }
    
    
    /**
     * Функция извлечения экземпляра {Student} из экземпляра {Packet}
     *
     * @param obj
     * @param className
     */
    public Optional<Student>  getStudentFromPacket(Packet packet)
    {
        LOG.log(Level.INFO, "Извлечение экземпляра {Student} ");
        // Формирование пустого экземпляра класса Optional
        Optional<Student> studentOpt = Optional.empty();
        try
        {
            switch (packet.getType())
            {
                case "ru.hiik.entitycore.entity.student.Student":
                {
                    
                    // Из поля Body извлекается экземпляр класса Student
                    Student student = gson.fromJson(packet.getBody(), Student.class);
                    if (student != null)
                    {
                        LOG.log(Level.INFO, "Извлечен экземпляр {Student}: " + student.toString());
                        studentOpt = Optional.of(student);
                    }
                }
                break;

                default:
                {
                    LOG.log(Level.SEVERE, "Неизвестный класс для обработки: " + packet.getType());
                }
            }

        } catch (JsonParseException ex)
        {
            LOG.log(Level.SEVERE, "Ошибка GSON извлечения класса из пакета {Packet} : {" + ex.toString() + "}");
        }
        return studentOpt;
    }

    
    
}
