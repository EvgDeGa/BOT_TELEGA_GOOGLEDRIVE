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
        int i = 0;
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
        String name = message.getFrom().getUserName() + message.getFrom().getFirstName();
        int flag = 0;
        int Users_num = 0;
        int Chat_num = 0;
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
        int i = 0;
        int flag = 0;
        for(ChatData chatData : data.chatData){
            i++;
            if(chatData.chat_id.equals(chat_id)){
                flag = 1;
                break;
            };
        }
        if(flag == 0){
            data.Set_chatData(new ChatData(chat_id, 0,"",name,false,false, new Users("0","0","0")));
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
        data.userData.get(num_of_user).chats.get(length-1).chat_folder_id = folder_id;
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

    //Создание голосования за выбор чата и за выбор пользователя
    public void chat_choice(int n_user, int  n_users_chat, Message message, int mode) throws TelegramApiException {
        int i = 0;
        data.userData.get(n_user).n_user = n_user;
        data.userData.get(n_user).n_users_chat = n_users_chat;
        String question = "";
        String question_2 = "";
        ArrayList<String> options = new ArrayList<>();
        //Выбор чата
        if(mode == 0 ){
            data.userData.get(n_user).U_S = 0;
            question = "Выбирите чат";
            question_2 = "Чат";
            String list = "Ваши чаты:\n";
            for (ChatData chatData : data.userData.get(n_user).chats) {
                list = list + chatData.chat_name + "\n";
            }
            list = list +  "\n" + "Выберите один.";
            try {
                execute(new SendMessage().setChatId(message.getFrom().getId().toString()).setText(list));
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
        //Выбор пользователя
        if(mode == 1 ) {
            String list = "";
            for (Integer integer: data.userData.get(n_user).chats.get(n_users_chat).user_choose){
                list = list + data.userData.get(n_user).chats.get(n_users_chat).users.get(integer).user_name_s + "\n";
            }
            execute(new SendMessage().setChatId(data.userData.get(n_user).self_user_id).setText("Уже добавленные пользователи:\n" + list));

            data.userData.get(n_user).U_S = 1;
            export_JSON(data);
            question = "Кого добавить?";
            question_2 = "Пользователь";
            data.userData.get(n_user).poll_us_chat = data.userData.get(n_user).chats.get(n_users_chat).chat_id;
            for (Users users : data.userData.get(n_user).chats.get(n_users_chat).users) {
                i++;
                int flag = 0;
                for(Integer integer : data.userData.get(n_user).chats.get(n_users_chat).user_choose){
                    if(users.user_name_s.equals(data.userData.get(n_user).chats.get(n_users_chat).users.get(integer).user_name_s)) {
                        flag = 1;
                        i--;
                        break;
                    }
                }
                if(flag == 0){
                    options.add(users.user_name_s);
                }
                if (i == 10) {
                    try {
                        execute(new SendPoll().setChatId(message.getFrom().getId().toString()).setQuestion(question).setOptions(options).setAllowMultipleAnswers(true).setAnonymous(false));
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                    i = 0;
                    options.clear();
                }
            }
        }
        if((i < 10)&&(i != 0)&&(i != 1)){
            try {
                execute(new SendPoll().setChatId(message.getFrom().getId().toString()).setQuestion(question).setOptions(options).setAllowMultipleAnswers(true).setAnonymous(false));
            }catch (TelegramApiException e){
                e.printStackTrace();
            }
        }
        if(i == 1){
            ArrayList<String> op = new ArrayList<>();
            op.add("Да");
            op.add("Нет");
            try {
                execute(new SendPoll().setChatId(message.getFrom().getId().toString()).setQuestion("У вас всего 1" + question_2 + ": " + options.get(0) + "\nИзменить его").setOptions(op).setAllowMultipleAnswers(false).setAnonymous(false));
            }catch (TelegramApiException e){
                e.printStackTrace();
            }
        }
      //  export_JSON(data);
    }

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
        int n_users_chat = 0;
        int n_users = 0;
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
                n_users = _search_user_chat.get(0);
                n_users_chat = _search_user_chat.get(1);
                chat_flag = _search_user_chat.get(2);

                if (data.userData.get(n_users).folder_id.equals("0")) {
                    try {
                        data.userData.get(n_users).folder_id = Check();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }


            add_users_id(message, chat_id);
            filter = false;
            full = false;

            int gl_m = data.userData.get(n_users).U_S;

            //Команды

            //Вывод списка диалогов, для выбора пользоватлей, для котрых нужно создать папки
            if (message.hasText()) {
                if (gl_m == 0) {
                    int i = -1;
                    for (ChatData chatData : data.userData.get(n_users).chats) {
                        i++;
                        if (chatData.chat_name.equals(message.getText())) {
                            try {
                                chat_choice(n_users, i, message, 1);//Вывод участников чата
                            } catch (TelegramApiException e) {
                                e.printStackTrace();
                            }
                            data.userData.get(n_users).poll_ch_chat = chatData.chat_id;
                            data.userData.get(n_users).U_S = 1;
                            export_JSON(data);
                            break;
                        }
                    }
                }

                //команда на добавление пользователя вручную
                String buf = message.getText().split(" ")[0];
                if(buf.equals("/add")){
                        if(flag == 1) {
                            data.userData.get(n_users).U_S = -1;
                            add(message, n_users, n_users_chat);
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
                            data.userData.get(n_users).U_S = -1;
                        }

                        try {
                            execute(sendMessage.setText("Создать отдельные папки для пользователей  /folder\nЗагружать фалы только для тех пользователей у которых есть папки - /filter\nАвтоматически создавать папки для всех пользователей - /full\nОстановить действие настроек full и filter - /stop "));
                        } catch (TelegramApiException e) {
                            e.printStackTrace();
                        }
                        break;
                        //Выбор режима создания папок
                    case "/folder":
                        if (flag == 1) {
                            data.userData.get(n_users).U_S = -1;
                            try {
                                execute(sendMessage.setText("Если хотите создать отдельные папки, для определённых пользователей напишите - /choice .\n\n Учтите, что по началу создание папок для определённых пользователей может быть недоступно.Список людей будет пополняться после того как они напишут сообщение в чат, после этого нужно заново вызвать команду /choice и добавить недостающих людей. Если выбрана функция /full, то папки будут добавляться автоматически. \n\nЕсли хотите добавить пользователся вручную то напишиет в чате, где находиться пользователь /add @%user-name%"));
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
                    //Выбор папок для пользователей через голосование
                    case "/choice":
                        if(flag == 1) {
                            data.userData.get(n_users).U_S = -1;
                            if (gl_m == 1) {
                                try {
                                    chat_choice(n_users, n_users_chat, message, 1);
                                } catch (TelegramApiException e) {
                                    e.printStackTrace();
                                }
                            }
                            if (gl_m == -1) {
                                try {
                                    chat_choice(n_users, n_users_chat, message, 0);
                                } catch (TelegramApiException e) {
                                    e.printStackTrace();
                                }

                            } else {
                                data.userData.get(n_users).U_S = -1;
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
                            data.userData.get(n_users).U_S = -1;
                            data.userData.get(n_users).mode = "full";
                            set_full_filter(n_users, message);
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
                            data.userData.get(n_users).U_S = -1;
                            data.userData.get(n_users).mode = "filter";
                            set_full_filter(n_users, message);
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
                            data.userData.get(n_users).U_S = -1;
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
                                        id_name = create_chat_folder(data.userData.get(n_users).folder_id, message);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    add_chat(n_users, message, id_name.get(0));
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
                            data.userData.get(n_users).U_S = -1;
                        }
                        try {
                           DriveQuickstart.main();
                        } catch ( IOException | GeneralSecurityException e) {
                            e.printStackTrace();
                        }
                        break;
                    case "/stop":
                        if (flag == 1) {
                            data.userData.get(n_users).U_S = -1;
                            stop(n_users);
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
                        if(flag ==1 ){
                            if ((message.getFrom().getId().toString().equals(message.getChatId().toString())) && (gl_m == -1) && (!buf.equals("/add"))) {
                                try {
                                    execute(sendMessage.setText("Привет, если меня добавить в чат и написать в чате /start, то я начну загружать файлы отправленные пользователми к вам на гугл диск. Если вас интересует мой функционал напишите /command "));
                                } catch (TelegramApiException e) {
                                    e.printStackTrace();
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

        //Проверка данных голосования о выборе пользователей
        if ((update.getPollAnswer() != null)) {
            int j = -1;
            for (UserData userData : data.userData) {
                j++;
                if (userData.self_user_id.equals(update.getPollAnswer().getUser().getId().toString())) {
                    break;
                }
            }
            int gl_ch = data.userData.get(j).n_users_chat;
            sendMessage.setChatId(data.userData.get(j).self_user_id);
            String list = "";
            int U_S = data.userData.get(j).U_S;

            switch (U_S) {
                //Для добавления пользователя
                case 1:
                    if (data.userData.get(j).poll_us_chat.equals(data.userData.get(j).poll_ch_chat)) {
                        try {

                            for (Integer option : update.getPollAnswer().getOptionIds()) {

                                data.userData.get(j).chats.get(gl_ch).Set_user_choose(option);

                                 list = list + data.userData.get(j).chats.get(gl_ch).users.get(option).user_name_s + "\n";

                            }
                            execute(sendMessage.setText("Выбран пользователь:\n" + list));
                        } catch (TelegramApiException e) {
                            e.printStackTrace();
                        }
                        data.userData.get(j).poll_us_chat = "a";
                        data.userData.get(j).poll_ch_chat = "";

                    } else {
                        try {
                            execute(sendMessage.setText("Что-то пошло не так, попробуйде еще раз."));
                        } catch (TelegramApiException e) {
                            e.printStackTrace();
                        }
                    }

                break;
                    //Для изменения настроек full filter
            case 2:
                if (data.userData.get(j).U_S == 2) {
                    String mode = data.userData.get(j).mode;
                    try {
                        if (mode.equals("filter")) {
                            for (Integer option : update.getPollAnswer().getOptionIds()) {
                                data.userData.get(j).chats.get(option).filter = true;
                            }
                            execute(sendMessage.setText("Настройки изменены"));
                        }
                        if (mode.equals("full")) {
                            for (Integer option : update.getPollAnswer().getOptionIds()) {
                                data.userData.get(j).chats.get(option).full = true;
                            }
                            execute(sendMessage.setText("Настройки изменены"));
                        }
                        data.userData.get(j).mode = "";
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                }
                break;
            default:
                try {
                    execute(sendMessage.setText("Ничего не произошло."));
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }

        }

            data.userData.get(j).U_S = -1;
            export_JSON(data);
        }

    }


    //Отключение функций full filter
    public void stop(int n_user){
        for(ChatData chatData: data.userData.get(n_user).chats){
            chatData.filter = false;
            chatData.full = false;
        }
        export_JSON(data);
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
    public void set_full_filter(int n_user, Message message) {
        ArrayList<String> options = new ArrayList<>();
        int i =0;
        for (ChatData chatData : data.userData.get(n_user).chats) {
            i++;
            options.add(chatData.chat_name);
            if (i == 10) {
                try {
                    execute(new SendPoll().setChatId(message.getFrom().getId().toString()).setQuestion("Для каких чатов изменить настройки").setOptions(options).setAllowMultipleAnswers(true).setAnonymous(false));
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
                i = 0;
                options.clear();
            }
        }
        if((i < 10)&&(i != 0)&&(i != 1)){
            try {
                execute(new SendPoll().setChatId(message.getFrom().getId().toString()).setQuestion("Для каких чатов изменить настройки").setOptions(options).setAllowMultipleAnswers(true).setAnonymous(false));
            }catch (TelegramApiException e){
                e.printStackTrace();
            }
        }
        if(i == 1){
            ArrayList<String> op = new ArrayList<>();
            op.add("Да");
            op.add("Нет");
            try {
                execute(new SendPoll().setChatId(message.getFrom().getId().toString()).setQuestion("Изменить настройки для чата: " + options.get(0)).setOptions(op).setAllowMultipleAnswers(false).setAnonymous(false));
            }catch (TelegramApiException e){
                e.printStackTrace();
            }
        }
        data.userData.get(n_user).U_S = 2;
        export_JSON(data);
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

