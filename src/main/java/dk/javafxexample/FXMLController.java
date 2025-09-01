package dk.javafxexample;

import com.google.gson.Gson;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import dk.entitycore.entity.student.Student;
import dk.entitycore.packet.Packet;
import dk.javafxexample.database.RestClient;
import dk.javafxexample.database.websocket.PacketProcessor;
import dk.javafxexample.database.websocket.WebsocketClient;

public class FXMLController implements Initializable
{

    private static final Logger LOG = Logger.getLogger(FXMLController.class.getSimpleName());
  
    // Поля формы "Карточка студента"
    @FXML
    private Label databaseStudentID;    // Поле для размещения идентификатора студента
    @FXML
    private Label lastName_card;             // Поле для размещения фамилии студента
    
    
    // Поля формы "Регистрация студента"
    @FXML
    private TextField lastNameTF;       // Текстовое поле "Фамилия студента"
    @FXML
    private TextField firstNameTF;      // Текстовое поле 
    @FXML
    private TextField middleNameTF;     // Текстовое поле 
    @FXML
    private TextField yearOfstudyTF;    // Текстовое поле 
    @FXML
    private TextField studentGroupTF;   // Текстовое поле 
    
    // Работа с фото студентов
    @FXML
    private ImageView studentPhoto;
    
    @FXML
    private GridPane studentCardGrid;
    
    @FXML
    private Button addStudentPhoto;        // Кнопка добавления фото студента 
    
    
    //  Кнопки формы
    @FXML
    private Button addStudentToDatabase;        // Кнопка добавления студента 
    @FXML
    private Button deleteStudentFromDatabase;   // Кнопка очистки приветствия
    @FXML
    private Button updateStudentInDatabase;     // Кнопка обновления таблицы студентов
    
   
    // Описание  модели данных 
    // 
    // Таблица для отображения студентов группы
    //------------------------------------------------------------------------------ 
    // Представление модели базы студнтов
    @FXML
    //         хранятся экземпляры класса Student
    //                   |
    private TableView<Student>   studentTable = new TableView<>();  // Таблица студентов
    // Добавление столбцов
    //       класс столбца   класс Student  поле типа экземпляр поля
    //            |             |             |                 |
    private TableColumn     <Student,       String>         id;             // Столбец "Идентификатор"
    private TableColumn     <Student,       String>         firstName;      // Столбец "Имя"
    private TableColumn     <Student,       String>         lastName;       // Столбец "Фамилия"  
    private TableColumn     <Student,       String>         middleName;     // Столбец "Отчество"  
    private TableColumn     <Student,       String>         yearOfstudy;    // Столбец "Год обучения"  
    private TableColumn     <Student,       String>         studentGroup;   // Столбец "Группа"  
    // Список студентов (модель базы студентов в памяти) 
    // 
    private ObservableList<Student> studentData  = FXCollections.observableArrayList();
    
    
    
    
    private RestClient databaseManager = new RestClient();
     // Клиент Websocket
    private WebsocketClient  websocketClient = new WebsocketClient();
        
    // Блокирующая очередь для приема экземпляров {Packet}
    private  final BlockingQueue <Packet> queue = new LinkedBlockingQueue<>(); ;
    
    // Класс для разбора пакетов
    private PacketProcessor packetProcessor = new PacketProcessor();
    
    
    /**
     * 
     * Функция инициализации компонентов графического  интерфейса FX
     * - выполняется автоматически при кажом 
     * 
     * @param url
     * @param rb 
     */
    @Override
    public void initialize(URL url, ResourceBundle rb)
    {
       
        websocketClient.setQueue(queue);    // Передача экземпляра Queue в websocketClient
        websocketClient.init();             // Инициализация клиента Websocket
        websocketClient.connect();          // Подключение клиента Websocket
        databaseManager.init();             // Создание менеджера базы данных на основе клиента REST
        
        // Конфигурирование поля фотографии студента
        configureStudentPhoto();
        configureAddStudentPhotoButton();
        
        //  Конфигурации кнопки графического интерфейса
        configureLabel();                           //  предварительная конфигурация текстовых меток
        configureAddStudentToDatabaseButton();      //  кнопка добавления студента в БД
        configureDeleteStudentButton();             //  кнопка удаления  студента из БД
        configureUpdateStudentInDatabase();         //  кнопка обновления студента в БД
        
        // Настройка таблицы студентов 
        configureStudentTable();    // Настройка таблицы
                                    //  1. Конфигурирование столбцов 
                                    //  2. Добавление столбцов в таблицу отображения студентов
                                    //  3. Обовление таблицы на основе списка студентов (модели студентов)
                                    
        configureStudentList();     // Формирование списка студентов
        
        // Запуск чтения из очереди сообщений из блокирующей очереди
        this.readFromQueue();
        
    }
    
    /**
     * Настройка кнопки добавления студента в базу данных 
     */
    private void configureAddStudentToDatabaseButton()
    {
        
        addStudentToDatabase.setOnAction((ActionEvent event) ->
        {
            // Вызов функции readStudentForm  и принятие подготовленного 
            // экземпляра Student в 
            //
            
            // Вызов функции чтения формы студента
            Student student = readStudentForm(); 
            System.out.println("Класс student: "+student.toString());
             
            // Создание экземпляра класса JSON
            Gson gson = new Gson();
            // Размещение экземпляра класса {Student} 
            String jsonStudent = gson.toJson(student); 
            System.out.println("Класс student JSON: "+jsonStudent);
            
            // Упаковка класса Student в экземпляр класса Packet
            Packet packet = new Packet();
            packet.setCommand("Добавление студента в БД");
            // В поле type размещена строка, указывающая на полное имя класса
            //
            // type = ru.hiik.entitycore.entity.student.Student
            System.out.println("Размещение экземляра класса Student внутри экземпляра Packet");
            packet.setType(Student.class.getCanonicalName());
            packet.setBody(jsonStudent);
            String jsonPacket = gson.toJson(packet);
            
            
            System.out.println("Отправка экземпляра Student {"+jsonStudent+"} на сервер");
            // Пересылка экземпляра класса {Student} на сервер
            //            получили доступ к экз  WebsocketEndpoint   вы
            //                   |                                   /       
            websocketClient.getWebsocketEndpoint().sendMessageToServer(jsonPacket);
          
            
        }
        );
    }
    
    
    
    /**
     * Настройка кнопки удаления студента из базы данных 
     */ 
    private void configureDeleteStudentButton()
    {
        // Код, срабатывающий после нажатия кнопки удаления студента
        deleteStudentFromDatabase.setOnAction((ActionEvent event) ->
        {
            // Фунция работы с текущим выбором из таблицы студентов 
            //      студент, выбранный в таблице
            //        |
            Student student = studentTable.getSelectionModel().getSelectedItem();
              if (student != null)
            {
                LOG.log(Level.INFO, "Выбран студент для удаления {" + student.toString() + "}");
                // Показ диалогового окна и чтение результата, выбранного пользователем
                boolean result = showConfirmation_DeleteStudent(student);
                if (result)
                {
                    
                    System.out.println("Класс student: " + student.toString());
                    // Создание экземпляра класса JSON
                    Gson gson = new Gson();
                    // Размещение экземпляра класса {Student} 
                    String jsonStudent = gson.toJson(student);
                    System.out.println("Класс student JSON: " + jsonStudent);
                    // Упаковка класса Student в экземпляр класса Packet
                    Packet packet = new Packet();
                    packet.setCommand("Удаление из БД");
                    // В поле type размещена строка, указывающая на полное имя класса
                    //
                    // type = ru.hiik.entitycore.entity.student.Student
                    packet.setType(Student.class.getCanonicalName());
                    packet.setBody(jsonStudent);
                    String jsonPacket = gson.toJson(packet);
                    System.out.println("Передача на сервер jsonPacket: " + jsonPacket);
                    websocketClient.getWebsocketEndpoint().sendMessageToServer(jsonPacket);
                }
            }
            else
            {
                showErrorDialog_NoStudentSelection();
            }
            

        }
        );
    }
    
     private void showErrorDialog_NoStudentSelection()
    {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка операции удаления");
        alert.setHeaderText("Не выбран студент из списка");
        alert.showAndWait();
    }

    
    
    /**
     * Диалоговое окно подтвреждения удаления студента из БД
     * @return 
     */
    private boolean showConfirmation_DeleteStudent(Student studentForDelete)
    {
        // Прихнак подтверждения false - запрет удаления, true - подтверждение удаления
        boolean result = false;
        
        ButtonType yes  = new ButtonType("Да",  ButtonBar.ButtonData.YES);  // Кнопка Да
        ButtonType no   = new ButtonType("Нет", ButtonBar.ButtonData.NO);   // Кнопка Нет
        
        // Создание экземпляра диалогового окна Alert
        //                            Тип окна = окно подтверждения (CONFIRMATION)
        //                               |                         кнопки с русскми буквами   
        //                               |                              |
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,"",        no,yes);
        // Закголовок окна
        alert.setTitle("Удалить студента из БД?");
        // Построитель строк
        StringBuilder sb = new StringBuilder();
        sb.append("["+studentForDelete.getId()+"] ");
        sb.append(studentForDelete.getLastName()+" ");
        sb.append(studentForDelete.getFirstName()+" ");
        sb.append(studentForDelete.getMiddleName()+" ");
        sb.append("курс: "+studentForDelete.getYearOfstudy()+", ");
        sb.append("группа: "+studentForDelete.getYearOfstudy());
        
        // Формирование содержимого панели диалога
        alert.setHeaderText(sb.toString());
        
        //                   выбор пользователя (ДА или НЕТ boolean)
        //                      |               показ диалога
        //                      |                 |
         Optional<ButtonType> responce = alert.showAndWait();
        
        // Анализуем кнопку, нажатую в диалоге удаления студента 
         if (responce.isPresent())
        {
            LOG.log(Level.INFO, "Ответ диалога удаления студента: {" + responce.get().getText() + "}");
            if (responce.get().getText().trim().equalsIgnoreCase("Да"))
            {
                result = true;
            }
        }
        
        return result;
    }        
    
    
    
    /*
     *   private boolean showConfirmation_DeleteImportTask(String operation, List<IoTask> tasklList)
    {
        boolean result = false;
        ButtonType yes  = new ButtonType("Да", ButtonBar.ButtonData.YES);
        ButtonType no   = new ButtonType("Нет", ButtonBar.ButtonData.NO);
        
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,"",no,yes);
        alert.setTitle(operation);
        alert.setHeaderText("Удалить выбранные задания импорта");

        int inset = 30;
        
        GridPane grid = new GridPane();
        grid.getColumnConstraints().add(new ColumnConstraints(210));
        grid.getColumnConstraints().add(new ColumnConstraints(410));
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(inset, inset, inset, inset));
       
        
        Color c = Color.web("0x284b82");
        Label headerTextLabel = new Label("Выбрано для удаления:");
        headerTextLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        headerTextLabel.setTextFill(c);
        
        Label countLabel = new Label("["+tasklList.size()+"] записей");
        countLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        countLabel.setTextFill(c);
       
       
       rowIndex = 0;
       int size = 0;
       int actualSize = 0;
       
        if (tasklList != null && tasklList.size() > 0)
        {
            size = tasklList.size();
            actualSize = tasklList.size();
            grid.add(headerTextLabel, 0, rowIndex);
            grid.add(countLabel, 1, rowIndex);
            if (size > 10)
            {

                size = 10;
                actualSize = actualSize - 10;
                for (int i = 0; i < 10; i++)
                {
                    rowIndex++;
                    IoTask t = tasklList.get(i);
                    grid.add(new Label(t.getName()), 0, rowIndex);
                    grid.add(new Label("тип: [" + t.getType() + "]"), 1, rowIndex);
                }
                grid.add(new Label("..."), 0, rowIndex);
                grid.add(new Label("... [" + actualSize + "] записей"), 1, rowIndex);

            } else
            {
                tasklList.stream().forEach(task ->
                {
                    rowIndex++;
                    grid.add(new Label(task.getName()), 0, rowIndex);
                    grid.add(new Label("тип: [" + task.getType() + "]"), 1, rowIndex);
                });
            }

        }
        alert.getDialogPane().setContent(grid);
        alert.setResizable(true);
        //                           ширина окна      высота окна
        //                                 |             |
        alert.getDialogPane().setPrefSize(700,          190+30*size);  
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);

        Optional<ButtonType> responce = alert.showAndWait();
        if (responce.isPresent())
        {
            LOG.log(Level.INFO, String.format("%-40s %-70s", "Ответ диалога удаления заданий импорта:", "{" + responce.get().getText() + "}"));
            if (responce.get().getText().trim().equalsIgnoreCase("Да"))
            {
                result = true;
            }
        }
        return result;
    }

    
     */
    
    
    
    /**
     * Настройка кнопки обновления студента в базе данных 
     */
      private void configureUpdateStudentInDatabase()
    {
        
           this.updateStudentInDatabase.setOnAction((ActionEvent event) ->
        {
            
            Student st = readStudentForm(); 
            System.out.println("Класс student: "+st.toString());
            // Создание экземпляра класса JSON
            Gson gson = new Gson();
            // Размещение экземпляра класса {Student} 
            String jsonText = gson.toJson(st); 
            System.out.println("Класс student JSON: "+jsonText);
            
            // Пересылка текста на сервер
            
            
        }
        );
            
    }

      
    /**
     * Метод чтения формы студента
     *
     *
     * @return возвращает экзмпляр класса {Student}, созданный из полей,
     * заполненных в форме
     */
    private Student readStudentForm()
    {

        // Считывание полей, заполненных оператором
        String last_name = lastNameTF.getText();
        String fist_name = firstNameTF.getText();
        String m_name = middleNameTF.getText();
        String course = yearOfstudyTF.getText();
        String group = studentGroupTF.getText();

        // Создание экземпляра класса Student путем заполнения полей
        Student student = new Student();

        student.setLastName(last_name);
        student.setFirstName(fist_name);
        student.setMiddleName(m_name);
        student.setStudentGroup(group);
        //     число   Преобразование строки в числов      строка
        //      |            |                              /
        Integer year = Integer.parseInt(course);
        // Запись числа, соответстсующего  строке course
        student.setYearOfstudy(year);

        return student;
    }
    
    
    /**
     * Проверка полноты заполнения полей
     *
     * @return
     */
    private boolean isCorrectStudentForm()
    {
        boolean result = false;
        if (this.studentGroupTF.getText().isEmpty())
        {
            Alert alert = new Alert(AlertType.WARNING);
            alert.setTitle("Ошибка заполнения полей");
            alert.setHeaderText("Не заполнены обязательные поля");
            alert.setContentText("[Группа]");
            alert.showAndWait();

        } else
        {
            boolean resul = true;
        }
        return result;
    }
    
    
    
    
    
   
    
    

    private void configureLabel()
    {
    }

    
    /**
     * Настройка полей таблицы студентов 
     * 
     */
    private void configureStudentTable()
    {
       
        // Формирование номера из класса Student
        id =  new TableColumn("Номер");
        id.setMinWidth(25);
        id.setCellValueFactory(
                student -> // Содержимое ячейки соотвествует экземп
        { // Код фабрики ячейки 
            
            SimpleStringProperty property = new SimpleStringProperty();  // Создание экземпляра для хранения свойств
            //           получить содержимое ячейки  полчить из экземпляла Student значение поля fistName
            //                    |                  /
            Long id = student.getValue().getId();
            
            property.setValue("БД= "+id); // В переменной property формируется отображение столбца
            return property;
        });
        
        
        // Код формирования столбца firstName
        //
        firstName = new TableColumn("Имя");
        firstName.setMinWidth(70);
        // Отображает значение,записанное в экземпляре класса SimpleStringProperty
        firstName.setCellValueFactory(
                value -> // Содержимое ячейки
        {
            
            SimpleStringProperty property = new SimpleStringProperty();  // Создание экземпляра для хранения свойств
            //           получить содержимое ячейки  полчить из экземпляла Student значение поля fistName
            //                    |                  /
            String str = value.getValue().getFirstName();    
            property.setValue(str); // Переписать значение поля fistName в экземляр класса 
            return property;
        });

        
        
        
        //  Код формирования поля  sureName
        //
        lastName = new TableColumn("Фамилия");  // Создание экземпляра класса столбца, подписанного фразой "Фамилия"
        lastName.setMinWidth(40);               // Задать минимальную ширину столбца
        //  Применить значение поля sureName для формирования столбца класса Student
        //                                                       название поле соотвествует полю класса Student
        //                                                                        |
        lastName.setCellValueFactory(new PropertyValueFactory<Student, String>("lastName"));

        middleName = new TableColumn("Отчество");
        middleName.setMinWidth(30);
        middleName.setCellValueFactory(new PropertyValueFactory<Student, String>("middleName"));
        
        studentGroup = new TableColumn("Группа");
        studentGroup.setCellValueFactory(new PropertyValueFactory<Student, String>("studentGroup"));
        
        yearOfstudy = new TableColumn("Год обучения");     // Столбец "Год обучения"  
        yearOfstudy.setMinWidth(12);
        yearOfstudy.setCellValueFactory(
                value -> // Содержимое ячейки
        {
            
            SimpleStringProperty property = new SimpleStringProperty();  // Создание экземпляра для хранения свойств
            //           получить содержимое ячейки  полчить из экземпляла Student значение поля fistName
            //                    |                  /
            String str = ""+ value.getValue().getYearOfstudy();    
            property.setValue(str); // Переписать значение поля fistName в экземляр класса 
            return property;
        });
        
        
        // Размещение списка студентов (модели) в представлении (таблице) студентов
        //      разместить экземпляры      список студентов  
        //              |                     |
        studentTable.setItems           (studentData);

        //  Добавление столбцов в таблицу 
        studentTable.getColumns().setAll(id, lastName, firstName, middleName, studentGroup,yearOfstudy);

        //           сообщение в пустой таблице          создание экземпляра класса Label
        //                 |                               /    
        studentTable.setPlaceholder                 (new Label("Нет данных о студентах"));
        studentTable.refresh();  //  отображение таблицы

        
        // Слушатель перемещения по строкам таблицы "Студент"
        studentTable.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Student>()
        { 
            // Слушатель событий перемещения по строкам
            @Override
            public void changed(ObservableValue<? extends Student> ov, Student value, Student new_student)
            { // Обработка события выбора пункта списка
                if (new_student != null)
                {
                    LOG.log(Level.INFO, "[Список студентов]: выбор строки --> {" + new_student.getLastName() + " " +new_student.getFirstName()+"}");
                    
                    //updateStudentDatabaseID("" + new_student.getId());
                    updateStudentLastName (new_student.getLastName()); 
                }
            }
        }
        );
    }
    
    
    /**
     * Функция обновления идентификаторв студента  в карточке Студента
     * @param text 
     */
       public void updateStudentDatabaseID(String studentId)
    {
        // Выполнение проверки текущей задачи процессора 
        if (Platform.isFxApplicationThread())
        {
            // Процессор работает с обновлением графики
            databaseStudentID.setText(""+studentId);
            
        } else
        {
             // Процессор занаимется задачами не связанными с обновлением графики
            Platform.runLater(() ->
            {
                databaseStudentID.setText(""+studentId);
            });
        }
    }
    
       
    //   
       
       public void updateStudentLastName(String lastName)
    {
        // Выполнение проверки текущей задачи процессора 
        if (Platform.isFxApplicationThread())
        {
            // Процессор работает с обновлением графики
            lastName_card.setText(lastName);
            
        } else
        {
             // Процессор занаимется задачами не связанными с обновлением графики
            Platform.runLater(() ->
            {
                lastName_card.setText(lastName);
            });
        }
    }   
       
    
    
    
      public void updatePhotoArea(String text)
    {
       
        if (Platform.isFxApplicationThread())
        {
            
        } else
        {
            Platform.runLater(() ->
            {
                
            });
        }
    }

      
    /**
     * Чтение экземпляров класса {Packet}, полученных от PacketProcessor
     */
    public void readFromQueue()
    {
        
        Runnable readQueue = new Runnable() // Создание отдельной нити (потока) для непрерывного чтения
        {                                   // сообщений из очереди queue
            @Override
            public void run()
            {
                while (true) // Бесконечный цикл получения сообщений  от класса  PacketProcessor
                {
                    try
                    {
                        //  Извлечение из очереди экземпляра класса {Packet}
                        Packet packet = queue.take();   // Возврат очередного пакета из очереди
                                                        // Блокировка выполнения нити 
                        if (packet != null)
                        {
                            LOG.log(Level.INFO, "Получен пакет из очереди: {" + packet.getCommand() + "}");
                            if (packetProcessor.getStudentFromPacket(packet).isPresent())
                            {    
                                // Извлечение эксземпляра студента из пакета
                                Student student = packetProcessor.getStudentFromPacket(packet).get();
                                
                                LOG.log(Level.INFO, "Извлечен студент из пакета: {" +  packetProcessor.getStudentFromPacket(packet).get().toString()+ "}"); 
                               
                                if (packet.getCommand().equalsIgnoreCase("Обновление списка на клиенте")
                                        || packet.getCommand().equalsIgnoreCase("Добавление студента в БД"))
                                {     
                                    boolean isFoundStudent = false; // Признак присутвия студента в БД
                                    for (int i = 0; i < studentData.size(); i++)
                                    {
                                        // current - очередной студент из списка модели (studentData)
                                        Student current  = studentData.get(i);
                                        if (current.getId().equals(student.getId()))
                                        {
                                            // Студент найден 
                                            isFoundStudent = true; break;
                                        }
                                        else
                                        {
                                            // Студент не найдент
                                            isFoundStudent = false;
                                        }
                                    }
                                  
                                    if (!isFoundStudent)
                                    {
                                        // Добавление студента в список studentData
                                        //      
                                        studentData.add(student);
                                    }

                                    // studentData является моделью для studentTable
                                    // Обновление таблицы студентов 
                                    //             |
                                    studentTable.refresh();
                                }
                                
                                // Код удаления студента
                                // Получение содержимого поля command
                                // equalsIgnoreCase - сравнение без учета регистра
                                if (packet.getCommand().equalsIgnoreCase("Удаление из БД"))
                                {
                                    LOG.log(Level.INFO, "Попытка удаления студента: {"+student.toString()+"}..." );
                                    boolean isRemoved = false;  // Признак удаления 
                                                                // false - запись не удалилась
                                                                // true - запись удалилась
                                    // Просмотр в цикле всех записей модели 
                                    //   начальное значение 0
                                    for (int i = 0; i < studentData.size(); i++)
                                    {
                                        // current - очередной студент из списка модели (studentData)
                                        Student current  = studentData.get(i);
                                        // Метод equls сравнивает текущую id с id в запросе
                                        //                       номер удаленного студента в ЮД
                                        //                            |
                                        if (current.getId().equals(student.getId()))
                                        {
                                            studentData.remove(i);  // Удаление студента с индексом i из модели
                                            isRemoved  = true;     
                                            break;
                                        }   
                                    }
                                    
                                    if (isRemoved)
                                    {
                                        LOG.log(Level.INFO, "Удален студент: {"+student.toString()+"}");
                                    }
                                    else
                                    {
                                        LOG.log(Level.SEVERE, "Ошибка удаления  студента: {"+student.toString()+"}");
                                    }
                                    studentTable.refresh();
                                }       
                              
                            }
                            
                        }
                    } catch (InterruptedException ex)
                    {
                        Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        };
        Thread thread = new Thread(readQueue);
        thread.start();

    }
      
     
     /**
     * Обновление списка студентов
     *
     * @param pipelinePart
     */
    private void updateStudentTable(List<Student> studentList)
    {
        LOG.log(Level.INFO, String.format("%-40s %-70s", "Обновление интерфейса таблицы", "{studentTable}..."));
        Task task = new Task<Void>()
        {
            @Override
            public Void call() throws Exception
            {
                Platform.runLater(() ->
                {
                    studentData.clear();
                    studentTable.getItems().clear();
                    if (studentList != null && studentList.size() > 0)
                    {
                        studentData.addAll(studentList);
                    }
                    
                    studentTable.refresh();
                    LOG.log(Level.INFO, String.format("%-40s %-70s", "Таблица обновлена", "{studentTable}"));
                });
                return null;
            }
        };
        Thread th = new Thread(task);
        th.setDaemon(true);
        th.start();
    }

    
    /**
     * Формирование списка студентов
     */
    private void configureStudentList()
    {
        
        
    }

    private void configureStudentPhoto()
    {
        // Создание экземпляра класса Image из файла 
        //Image image = new Image("image/Intel_C4004.jpg");
        //byte [] studentBytePhoto;
       
        //studentPhoto =  new ImageView();
        // Размащение изображения в ImageView
        //studentPhoto.setImage(image);
    }

    private void configureAddStudentPhotoButton()
    {
        this.addStudentPhoto.setOnAction((ActionEvent event) ->
        {

            // Формирование диалога выбора файла
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Выбор фото студента");
            LOG.log(Level.INFO, "Загрузка фото студента...");
            try
            {
                File file = fileChooser.showOpenDialog(null);
              
                if (file != null)
                {
                      LOG.log(Level.INFO, "Выбран файл: {" + file.getCanonicalPath() + "}");
                    
                    if (file.exists())
                    {
                        // Открытие потока к файлу
                        InputStream is = new FileInputStream(file);
                        // Преобразование потока в класс Image
                        Image image = new Image(is);
                        if (image != null)
                        {
                            LOG.log(Level.INFO, "Загружено изображение из файла: {" + file.getCanonicalPath() + "}");
                            ImageView studentPhoto = new ImageView();                            
                            studentPhoto.setImage(image);
                            studentPhoto.setFitWidth(279);
                            studentPhoto.setPreserveRatio(true);
                            studentPhoto.setSmooth(true);
                            readBytesFromFile(is);
                            
                            studentCardGrid.getChildren().clear();
                            studentCardGrid.getChildren().add(studentPhoto);
                            
                        } 
                        else
                        {
                            LOG.log(Level.SEVERE, "Ошибка загрузки  изображения из файла: {" + file.getCanonicalPath() + "}");
                        }    
                                
                    }    
                }   
            } catch (IOException ex)
            {
                LOG.log(Level.SEVERE, "Ошибка выбора файла: {" + ex.toString()+ "}");
            }

        });
    }

   
    
    private void readBytesFromFile(InputStream is) 
    {
       // Байтовый массив для чтения файла
        byte[] fileBuffer;
        try
        {
            
            // Чтение размера файла - переменная bytes
            int bytes = is.available();
            System.out.println("Достпуно в потоке: {" + bytes + "} байт");
            // Выделение памяти под файл в оперативной памяти 
            fileBuffer = new byte[bytes];

            // Чтение содержимого диска в массив в памяти
            // 
            is.read(fileBuffer, 0, bytes);
            System.out.println("Прочитано и размещено в памяти: " + fileBuffer.length);

        } catch (IOException ex)
        {
            LOG.log(Level.SEVERE, "Ошибка чтения файла изображения: {" + ex.toString()+ "}");
        }


    }        
  
      
    
}
