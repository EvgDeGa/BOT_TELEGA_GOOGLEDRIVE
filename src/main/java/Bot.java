import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.io.FileUtils;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.mortbay.util.ajax.JSON;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.polls.SendPoll;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.MessageEntity;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.*;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.ArrayList;



public class Bot extends TelegramLongPollingBot {

    Data data;//Класс с информацией о пользователях и чатах

    //Конструктор создаёт класс с данными о всех пользователях
    Bot() throws IOException, ParseException {
        data = import_JSON();
        System.out.println(Check());
    }


    public static void main(String... args) {

    }

    //Импорт данных о пользователях и чатах из JSON
    public Data import_JSON() throws IOException, ParseException {
        JSONParser parser = new JSONParser();
        Object obj = parser.parse(new FileReader("C:\\Users\\JoJug\\Desktop\\Универ\\Научная работа\\BOT_TELEGA_GOOGLEDRIVE\\src\\main\\JSON\\CHATS.json"));

        System.out.println(JSON.toString(obj)); ;
        Gson g = new Gson();
        Data data = g.fromJson(JSON.toString(obj), Data.class);

        return data;
    }

    //Экспорт данных о пользователях и чатах в JSON
    public void export_JSON(Data data){
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();

        try (FileWriter file = new FileWriter("C:\\Users\\JoJug\\Desktop\\Универ\\Научная работа\\BOT_TELEGA_GOOGLEDRIVE\\src\\main\\JSON\\CHATS.json")) {

            file.write(gson.toJson(data));
            file.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Проверка на наличие папки TELEGA_DRIVE в которую будут загружаться файлы
    //Если папки не сущесвтует, то она создаётся
    //Функция возвращает id папки
    public String Check() throws IOException {
        String Create_Folder = "0";

        try {
            Create_Folder = GetSubFolders.find_name();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(Create_Folder.equals("0")){
            Create_Folder = CreateFolder.Create_TG();
        }

        return Create_Folder;
    }



    //Проверка от кого пришло сообщение от авторизированного пользователя или нет
    //Возвращает значение инф. о авт. пользователе и номер авториз. пользователя
    public ArrayList<Integer> search_auth_user(Message message){
        String user_id = message.getFrom().getId().toString();
        int flag = 0;
        int i = -1;
        for(UserData userData : data.userData){
            i++;
            if(user_id.equals(userData.self_user_id)){
                flag = 1;
                break;
            }
        }
        ArrayList<Integer> ret = new ArrayList<>();
        ret.add(i);
        ret.add(flag);
        export_JSON(data);
        return ret;
    }

    //Поиск и добавление пользователя в списке чатов
    //Возвращает номер пользователя, номер чата
    public ArrayList<Integer> search_chat_user(Message message){
        String user_id = message.getFrom().getId().toString();
        String chat_id = message.getChatId().toString();
        String name;
        if(message.getFrom().getUserName() == null){
            name = message.getFrom().getFirstName();
        }else{
            name = message.getFrom().getUserName();
        }
        int flag = 0;
        int Users_num = -1;
        int Chat_num = -1;
        for(ChatData chatData : data.chatData){
            String а = chatData.chat_id;
            Chat_num++;
            if(chat_id.equals(chatData.chat_id)){
                for(Users users: chatData.users){
                    Users_num++;
                    if(user_id.equals(users.user_id_s)){
                        flag = 1;
                        break;
                    }
                }
                if(flag == 0){
                    chatData.Set_user_id(new Users(user_id, "0", name));
                    export_JSON(data);
                    Users_num++;
                    break;
                }
            }
        }
        ArrayList<Integer> ret = new ArrayList<>();
        ret.add(Users_num);
        ret.add(Chat_num);
        export_JSON(data);
        return ret;
    }

    //Поиск и добавление чата
    //Возвращает номер чата
    public int search_auth_chat(String chat_id, String name){
        int i = -1;
        int flag = 0;

        for(ChatData chatData : data.chatData){
            i++;
            if(chatData.chat_id.equals(chat_id)){
                flag = 1;
                break;
            };
        }
        if(flag == 0){
            data.Set_chatData(new ChatData(chat_id ,"",name,false,false, new Users("0","0","0")));
            i++;
        }
        export_JSON(data);
        return i;
    }


    //Добавление чата авторизированному пользователю
    public void add_chat(int num_of_user, Message message, String folder_id){
        String chat_id = message.getChatId().toString();
        String name = message.getChat().getTitle();
        int length = data.userData.get(num_of_user).chats.toArray().length;
        data.userData.get(num_of_user).Set_chats(data.chatData.get(search_auth_chat(chat_id, name)));
        data.userData.get(num_of_user).chats.get(length).chat_folder_id = folder_id;
        export_JSON(data);
    }

    //Поиск полльзователя в чатах, у авторизированного пользователя
    //Возвразает номер пользователя, номер чата
    public ArrayList<Integer> search_user_chat(String user_id, String chat_id, String name){
        int num_of_user = -1;
        int num_of_chat = -1;
        int flag = 0;
        for(UserData userData : data.userData){
            num_of_user++;
            if(userData.self_user_id.equals(user_id)) {
                for(ChatData chatData : userData.chats){
                    num_of_chat++;
                    if (chatData.chat_id.equals(chat_id)) {
                        flag = 1;
                        break;
                    }
                }
            }
        }
        ArrayList<Integer> ret = new ArrayList<>();
        ret.add(num_of_user);
        ret.add(num_of_chat);
        ret.add(flag);
        export_JSON(data);
        return ret;
    }


    //Добавление пользователя в список чатов авторизированного пользователя
    public void add_users_id(Message message, String chat_id){
        String user_id = message.getFrom().getId().toString();
        String name;
        if(message.getFrom().getUserName() == null){
            name = message.getFrom().getFirstName();
        }else{
            name = message.getFrom().getUserName();
        }

        int flag = 0;
        for(UserData userData : data.userData){
            for(ChatData chatData : userData.chats){
                if(chatData.chat_id.equals(chat_id)){
                    for(Users users : chatData.users) {
                        if (user_id.equals(users.user_id_s)) {
                            flag = 1;
                            break;
                        }
                    }
                    if(flag == 0){
                        chatData.Set_user_id(new Users(user_id,"0",name));
                        break;
                    }
                }
            }
        }
        export_JSON(data);
    }

    //Функция создает папку для чата
    public ArrayList<String> create_chat_folder(String folderIdParent, Message message) throws IOException {
        String name = message.getChat().getTitle();
        return CreateFolder.Create_chat(folderIdParent, name);
    }

    //Функция создает папку для пользователя
    public ArrayList<String> create_user_folder(String folderIdParent, String name) throws IOException {
        return CreateFolder.Create_chat(folderIdParent, name);
    }

    //Выбор чата
    public String choice_chat(int n_user){
        String list = "Ваши чаты\n";
        for(ChatData chatData: data.userData.get(n_user).chats){
            list = list + chatData.chat_name + "\n";
        }
        return list;
    }

    //Включение параметра fill и filter  в выбранных чатах
    public void parse_chat(String list, int n_user) throws TelegramApiException {
        String[] buffer = list.split(",");
        String mode = data.userData.get(n_user).mode;
        String mes = "Натсройка " + mode + " в чатах\n";
        for(String buf : buffer ){
            int i = -1;
            for(ChatData chatData: data.userData.get(n_user).chats) {
                if (buf.equals(chatData.chat_name)){
                    mes += chatData.chat_name + "\n";
                    if(mode.equals("full")){
                        chatData.full = true;
                        chatData.filter = false;
                    }
                    if(mode.equals("filter")){
                        chatData.filter = true;
                        chatData.full = false;
                    }
                }
            }
        }
        mes += "Включена";
        execute(new SendMessage().setChatId(data.userData.get(n_user).self_user_id).setText(mes));
        data.userData.get(n_user).mode = "";
        data.userData.get(n_user).poll = false;
        export_JSON(data);
    }

    //Отключение функций full filter
    public void stop(int n_user) throws TelegramApiException {
        for(ChatData chatData: data.userData.get(n_user).chats){
            chatData.filter = false;
            chatData.full = false;
        }
        export_JSON(data);
        execute(new SendMessage().setChatId(data.userData.get(n_user).self_user_id).setText("Настройки изменены"));
    }

    //Добавление папки для пользователся с помощью функции add
    public void add(Message message,int n_user,int n_chat){
        if(message.getEntities() != null){
            for(MessageEntity messageEntity : message.getEntities()){
                if(messageEntity.getUser() != null){
                    int flag = 0;
                    int i = -1;
                    String id = messageEntity.getUser().getId().toString();
                    String name;
                    if(messageEntity.getUser().getUserName() == null){
                        name = messageEntity.getUser().getFirstName();
                    }else{
                        name = messageEntity.getUser().getUserName();
                    }
                    for(Users users: data.userData.get(n_user).chats.get(n_chat).users){
                        i++;
                        if(users.user_id_s.equals(id)){
                            flag =1;
                            break;
                        }
                    }
                    if (flag == 1){
                        int flag_1 = 0;
                        for(Integer integer: data.userData.get(n_user).chats.get(n_chat).user_choose){
                            if(i == integer){
                                flag_1 = 1;
                                break;
                            }
                        }
                        if(flag_1 == 0){
                            data.userData.get(n_user).chats.get(n_chat).Set_user_choose(i);
                        }

                    }else{
                        data.userData.get(n_user).chats.get(n_chat).Set_user_id(new Users(id,"0",name));
                        int length = data.userData.get(n_user).chats.get(n_chat).users.toArray().length;
                        data.userData.get(n_user).chats.get(n_chat).Set_user_choose(length-1);
                    }
                }
            }
        }
        export_JSON(data);
    }

    //Изменение параметров full и filter
    public void set_full_filter(int n_user) throws TelegramApiException {
        String list = choice_chat(n_user);
        String buf = data.userData.get(n_user).mode;
        execute(new SendMessage().setChatId(data.userData.get(n_user).self_user_id).setText("Выберите чаты для которых изменить настройку - " + buf + "\nНапишите нужные чаты через запятую и без пробелов\n\n" + list));
        data.userData.get(n_user).poll = true;
        export_JSON(data);
    }


    //Создание голосования за выбор чата и за выбор пользователя
    public void onUpdateReceived(Update update) {

        Message message = update.getMessage();
        SendMessage sendMessage = new SendMessage();

        String chat_id = "";
        String chat_name;
        String user_id = "";
        ArrayList<Integer> _search_auth_user;
        ArrayList<Integer> _search_chat_user;
        int n_auth_chat = 0;
        int flag = 0;
        int n_user_chat = 0;
        int n_user = 0;
        int chat_flag = 0;
        ArrayList<Integer> _search_user_chat;
        Boolean filter;
        Boolean full;

        if (update.getMessage() != null) {

            sendMessage.setChatId(message.getFrom().getId().toString());

            chat_id = message.getChatId().toString();
            chat_name = message.getChat().getTitle();
            user_id = message.getFrom().getId().toString();


            _search_auth_user = search_auth_user(message);
            _search_chat_user = search_chat_user(message);
            n_auth_chat = search_auth_chat(chat_id, chat_name);
            flag = _search_auth_user.get(1);

            //Проверка на авторизированного пользователя
            if (flag == 1) {

                _search_user_chat = search_user_chat(user_id, chat_id, chat_name);
                n_user = _search_user_chat.get(0);
                n_user_chat = _search_user_chat.get(1);
                chat_flag = _search_user_chat.get(2);

                if (data.userData.get(n_user).folder_id.equals("0")) {
                    try {
                        data.userData.get(n_user).folder_id = Check();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }


            add_users_id(message, chat_id);
            filter = false;
            full = false;



            //Команды

            //Вывод списка диалогов, для выбора пользоватлей, для котрых нужно создать папки
            if (message.hasText()) {
//
                //Команда на добавление пользователя вручную
                String buf = message.getText().split(" ")[0];
                if(buf.equals("/add")){
                        if(flag == 1) {
                            data.userData.get(n_user).mode = "";
                            data.userData.get(n_user).poll = false;
                            add(message, n_user, n_user_chat);
                        }else {
                            try {
                                execute(sendMessage.setText("Нужен доступ к Google-Drive\nПредоставить доступ - /reg"));
                            } catch (TelegramApiException e) {
                                e.printStackTrace();
                            }
                        }
                }

                //команды
                switch (message.getText()) {
                    //список комнад
                    case "/command":
                        if(flag == 1){
                            data.userData.get(n_user).mode = "";
                            data.userData.get(n_user).poll = false;
                        }

                        try {
                            execute(sendMessage.setText("Создать отдельные папки для пользователей  /folder\nЗагружать фалы только для тех пользователей у которых есть папки - /filter\nЗагружать файлы в папки для всех пользователей - /full\nОстановить действие настроек full и filter - /stop "));
                        } catch (TelegramApiException e) {
                            e.printStackTrace();
                        }
                        break;
                        //Выбор режима создания папок
                    case "/folder":
                        if (flag == 1) {
                            data.userData.get(n_user).mode = "";
                            data.userData.get(n_user).poll = false;
                            try {
                                execute(sendMessage.setText("/full - папки для каждого участника чата будут добавляться автоматически. \n\nЕсли хотите добавить пользователя вручную то напишиет в чате, где находиться пользователь /add @%user-name%"));
                            } catch (TelegramApiException e) {
                                e.printStackTrace();
                            }
                        } else {
                            try {
                                execute(sendMessage.setText("Нужен доступ к Google-Drive\nПредоставить доступ - /reg"));
                            } catch (TelegramApiException e) {
                                e.printStackTrace();
                            }
                        }
                        break;

                    //Папки буду создаваться автоматически для всех пользователей чата
                    case "/full":

                        if (flag == 1) {
                            data.userData.get(n_user).mode = "";
                            data.userData.get(n_user).poll = false;
                            data.userData.get(n_user).mode = "full";
                            try {
                                set_full_filter(n_user);
                            } catch (TelegramApiException e) {
                                e.printStackTrace();
                            }
                        } else {
                            try {
                                execute(sendMessage.setText("Нужен доступ к Google-Drive\nПредоставить доступ - /reg"));
                            } catch (TelegramApiException e) {
                                e.printStackTrace();
                            }
                        }
                        break;
                    //Документы будут загружаться только для пользователей у которых есть папки
                    case "/filter":

                        if ((flag == 1)) {
                            data.userData.get(n_user).mode = "";
                            data.userData.get(n_user).poll = false;
                            data.userData.get(n_user).mode = "filter";
                            try {
                                set_full_filter(n_user);
                            } catch (TelegramApiException e) {
                                e.printStackTrace();
                            }
                        } else {
                            try {
                                execute(sendMessage.setText("Нужен доступ к Google-Drive\nПредоставить доступ - /reg"));
                            } catch (TelegramApiException e) {
                                e.printStackTrace();
                            }
                        }
                        break;
                    //команда для добавления чата из которого нужно загружать папки
                    case "/start":

                        if ((flag == 1)) {
                            data.userData.get(n_user).mode = "";
                            data.userData.get(n_user).poll = false;
                            if (message.getFrom().getId().toString().equals(message.getChatId().toString())) {
                                try {
                                    execute(sendMessage.setText("Здесь этого сделать нельзя"));
                                } catch (TelegramApiException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                if (chat_flag == 0) {
                                    ArrayList<String> id_name = new ArrayList<>();
                                    try {
                                        id_name = create_chat_folder(data.userData.get(n_user).folder_id, message);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    add_chat(n_user, message, id_name.get(0));
                                    export_JSON(data);
                                }
                            }
                        } else {
                            try {
                                execute(sendMessage.setText("Нужен доступ к Google-Drive\nПредоставить доступ - /reg"));
                            } catch (TelegramApiException e) {
                                e.printStackTrace();
                            }
                        }

                        break;
                    //регистрация
                    case "/reg":

                        if(flag == 1){
                            data.userData.get(n_user).mode = "";
                            data.userData.get(n_user).poll = false;
                        }
                        try {
                           DriveQuickstart.main();
                        } catch ( IOException | GeneralSecurityException e) {
                            e.printStackTrace();
                        }
                        break;
                    case "/stop":
                        if (flag == 1) {
                            data.userData.get(n_user).mode = "";
                            data.userData.get(n_user).poll = false;
                            try {
                                stop(n_user);
                            } catch (TelegramApiException e) {
                                e.printStackTrace();
                            }
                        } else {
                            try {
                                execute(sendMessage.setText("Нужен доступ к Google-Drive\nПредоставить доступ - /reg"));
                            } catch (TelegramApiException e) {
                                e.printStackTrace();
                            }
                        }
                        break;
                    //информационное сообщение
                    default:
                        if(flag == 1){
                            if(data.userData.get(n_user).poll){//ввод выбранных чатов для full filter
                                try {
                                    parse_chat(message.getText(), n_user);
                                } catch (TelegramApiException e) {
                                    e.printStackTrace();
                                }
                            }else {
                                data.userData.get(n_user).mode = "";
                                data.userData.get(n_user).poll = false;
                                if ((message.getFrom().getId().toString().equals(message.getChatId().toString())) && (!buf.equals("/add"))) {
                                    try {
                                        execute(sendMessage.setText("Привет, если меня добавить в чат и написать в чате /start, то я начну загружать файлы отправленные пользователми к вам на гугл диск. Если вас интересует мой функционал напишите /command "));
                                    } catch (TelegramApiException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }else {
                            if (message.getFrom().getId().toString().equals(message.getChatId().toString())) {
                                try {
                                    execute(sendMessage.setText("Привет, если меня добавить в чат и написать в чате /start, то я начну загружать файлы отправленные участниками чата к вам на гугл диск. Если вас интересует мой функционал напишите /command "));
                                } catch (TelegramApiException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        break;
                }
            }
            //Если сообщение документ
            if (message.hasDocument()) {
                for (UserData userData : data.userData) {
                    for (ChatData chatData : userData.chats) {
                        filter = chatData.filter;
                        full = chatData.full;
                        if (chat_id.equals(chatData.chat_id)) {
                            //загружает файл в папку пользователся если она существует
                            //если папки не существует, то создаёт папку и загружает
                            if (full) {
                                int full_flag = 0;
                                for (Users users : chatData.users) {
                                    if (users.user_id_s.equals(user_id)) {
                                        if (users.folder_id_s.equals("0")) {
                                            try {
                                                ArrayList<String> buf = create_user_folder(chatData.chat_folder_id, users.user_name_s);
                                                users.folder_id_s = buf.get(0);

                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                            export_JSON(data);
                                        }
                                        down_up(message, users.folder_id_s);
                                        break;
                                    }
                                }
                            }
                            //Загружает файлы только для выбранных пользователей
                            //если папки не существует, то создаёт папку и загружает
                            if (filter) {
                                int n = 0;
                                for (Users users : chatData.users) {
                                    n++;
                                    if (users.user_id_s.equals(user_id)) {
                                        for (Integer i : chatData.user_choose) {
                                            if (n == i) {
                                                if (users.folder_id_s.equals("0")) {
                                                    try {
                                                        ArrayList<String> buf = create_user_folder(chatData.chat_folder_id, users.user_name_s);
                                                        users.folder_id_s = buf.get(0);

                                                    } catch (IOException e) {
                                                        e.printStackTrace();
                                                    }
                                                    export_JSON(data);
                                                }
                                                down_up(message, users.folder_id_s);
                                                break;
                                            }
                                        }
                                    }
                                }
                            }
                            //если у пользователя существует папка загружает туда
                            //если её нет то загружает в папку чата
                            if (!filter && !full) {
                                int n = 0;
                                int check = 0;
                                for (Users users : chatData.users) {
                                    n++;
                                    if (users.user_id_s.equals(user_id)) {
                                        for (Integer i : chatData.user_choose) {
                                            if (n == i) {
                                                if (users.folder_id_s.equals("0")) {
                                                    try {
                                                        ArrayList<String> buf = create_user_folder(chatData.chat_folder_id, users.user_name_s);
                                                        users.folder_id_s = buf.get(0);

                                                    } catch (IOException e) {
                                                        e.printStackTrace();
                                                    }
                                                    export_JSON(data);
                                                }
                                                down_up(message, users.folder_id_s);
                                                check = 1;
                                                break;
                                            }
                                        }
                                    }
                                }
                                if (check == 0) {
                                    down_up(message, chatData.chat_folder_id);
                                }
                            }
                        }
                    }
                }
            }
        }
    }




    //Скачивание и загрузка файла на гугл диск
    public void down_up(Message message, String folder_id){
        String file_id = message.getDocument().getFileId();
        GetFile file = new GetFile().setFileId(file_id);
        String file_path_id = "";
        try {
            file_path_id = execute(file).getFilePath();
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
        File local_file = new File(file_path_id);
        InputStream down_file = null;
        try {
            down_file = new URL("https://api.telegram.org/file/bot" + getBotToken() + "/" + file_path_id).openStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            assert down_file != null;
            FileUtils.copyInputStreamToFile(down_file, local_file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            String path_buf = folder_id + "/" + file_path_id;
            CreateGoogleFile.main(path_buf);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getBotUsername() {
        return "Dorokhov_Google_Drive_bot";
    }

    public String getBotToken() {
        return "1398352830:AAHEHFlLE8XomFRAGHOV6TcDJmSOZgVJk5c";
    }
}

