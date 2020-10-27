import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.io.FileUtils;
import org.checkerframework.checker.units.qual.A;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.mortbay.util.ajax.JSON;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.polls.SendPoll;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.*;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.ArrayList;


public class Bot extends TelegramLongPollingBot {

    Data data;

    //Конструктор создаёт класс с данными о всех пользователях
    Bot() throws IOException, ParseException {
        data = import_JSON();
        System.out.println(Check());
    }

    //Импорт данных о пользователях и чатах из JSON
    public Data import_JSON() throws IOException, ParseException {
        JSONParser parser = new JSONParser();
        Object obj = parser.parse(new FileReader("C:\\Users\\JoJug\\Desktop\\Универ\\Научная работа\\BOT_TELEGA_GOOGLEDRIVE\\src\\main\\JSON\\CHATS.json"));

        System.out.println(JSON.toString(obj)); ;
        Gson g = new Gson();
        Data data = g.fromJson(JSON.toString(obj), Data.class);

        for (UserData userData : data.userData) {

        }
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
    //Если папки не сущёсвтует, то она создаёться
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

    //Инициализация бота
    public static void main(String... args) {

    }

    public void download_file(String file_id) throws TelegramApiException, IOException {
        GetFile get_file = new GetFile().setFileId(file_id);
        String file_path_id = execute(get_file).getFilePath();
        File local_file = new File(file_path_id);
        InputStream down_file =  new URL("https://api.telegram.org/file/bot" + "1398352830:AAHEHFlLE8XomFRAGHOV6TcDJmSOZgVJk5c" + "/" + file_path_id).openStream();
        FileUtils.copyInputStreamToFile(down_file, local_file);
    }

    //Создание опроса с целью узнать для каких участников создать отдельные папки
    public SendPoll user_poll(String setChatId, ArrayList<String> options) throws TelegramApiException, IOException {
        SendPoll sendPoll = new SendPoll();
        sendPoll.setChatId(setChatId);
        sendPoll.setQuestion("Каво");
        sendPoll.setOptions(options);
        return sendPoll;
    }

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
        ret.add(flag);
        export_JSON(data);
        return ret;
    }

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

    public void add_chat(int num_of_user, Message message, String folder_id){
        String chat_id = message.getChatId().toString();
        String name = message.getChat().getTitle();
        int length = data.userData.get(num_of_user).chats.toArray().length;
        data.userData.get(num_of_user).Set_chats(data.chatData.get(search_auth_chat(chat_id, name)));
        data.userData.get(num_of_user).chats.get(length-1).chat_folder_id = folder_id;
        export_JSON(data);
    }

    public ArrayList<Integer> search_user_chat(String user_id, String chat_id, String name){
        int num_of_user = 0;
        int num_of_chat = 0;
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

    public void add_users_id(Message message, String chat_id){
        String user_id = message.getFrom().getId().toString();
        String name;
        if(message.getFrom().getUserName() == null){
            name = message.getFrom().getFirstName();
        }else{
            name = message.getFrom().getFirstName();
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


    public ArrayList<String> create_chat_folder(String folderIdParent, Message message) throws IOException {
        String name = message.getChat().getTitle();
        return CreateFolder.Create_chat(folderIdParent, name);
    }

    public ArrayList<String> create_user_folder(String folderIdParent, String name) throws IOException {
        return CreateFolder.Create_chat(folderIdParent, name);
    }

    public void onUpdateReceived(Update update) {
        SendMessage sendMessage = new SendMessage();
        Message message = update.getMessage();
        sendMessage.setChatId(message.getChatId());

        String chat_id = message.getChatId().toString();
        String chat_name = message.getChat().getTitle();
        String user_id = message.getFrom().getId().toString();


        ArrayList<Integer> _search_auth_user = search_auth_user(message);
        ArrayList<Integer> _search_chat_user = search_chat_user(message);
        int n_auth_chat = search_auth_chat(chat_id,chat_name);
        int flag = _search_auth_user.get(1);
        int n_users_chat = 0;
        int n_users = 0;
        int chat_flag = 0;

        if(flag == 1) {
            ArrayList<Integer> _search_user_chat = search_user_chat(user_id, chat_id, chat_name);
            n_users = _search_user_chat.get(0);
            n_users_chat = _search_user_chat.get(1);
            chat_flag = _search_user_chat.get(2);
        }
        add_users_id(message, chat_id);

        Boolean filter = false;
        Boolean full = false;








        //Команды
        if (message.hasText()){
            switch (message.getText()){
                case "/help":
                    try {
                        DriveQuickstart.main();
                    } catch (IOException | GeneralSecurityException e) {
                        e.printStackTrace();
                    }
                    break;
                case "/stardt":
                    //проверка авторизации
                    if (flag != 1) {
                        try {
                            DriveQuickstart.main();
                        } catch (IOException | GeneralSecurityException e) {
                            e.printStackTrace();
                        }
//                        data.Set_userData();
                    }
                    try {
                        execute(sendMessage.setText("Создать отдельные папки для пользователей  /folder \nОстановить автоматическое добавление папок для пользователей - /stop \nЗагружать фалы только для выбранных пользователей - /filter"));
                    }catch (TelegramApiException e){
                        e.printStackTrace();
                    }
                    break;
                case "/folder":
                    try {
                        execute(sendMessage.setText("Если хотите создать отдельную папку, для определённого человека напишите   /choice .\nЕсли хотите создать отдельные папки, для всех пользователей чата напишите   /full .\n\n Учтите, что по началу создание папок для определённых пользователей может быть недоступно.Список людей будет пополняться после того как они напишут сообщение в чат, после этого нужно заново вызвать команду /choice и добавить недостающих людей. Если выбрана функция /full, то папки будут добавляться автоматически. "));
                    }catch (TelegramApiException e){
                        e.printStackTrace();
                    }
                    break;
                case "/choice":
                    int i = 0;
                    ArrayList<String> options = new ArrayList<>();
                    for(Users users: data.userData.get(n_users).chats.get(n_users_chat).users){
                        options.add(users.user_name_s);
                    };
                    try {
                       execute(new SendPoll().setChatId(message.getChatId().toString()).setQuestion("Кого добавить?").setOptions(options).setAllowMultipleAnswers(true));
                    }catch (TelegramApiException e){
                        e.printStackTrace();
                    }
                    break;
                case "/full":
                    if(flag == 1) {
                        data.userData.get(n_users_chat).chats.get(n_users_chat).full = !data.userData.get(n_users_chat).chats.get(n_users_chat).full;
                        data.userData.get(n_users_chat).chats.get(n_users_chat).filter = false;
                        if(data.userData.get(n_users_chat).chats.get(n_users_chat).full){
                            System.out.println("DD = 1");
                        }
                    }

//                    try {
//                        execute(new SendPoll().setChatId(message.getChatId().toString()).setQuestion("SS").setOptions(options).setAllowMultipleAnswers(true));
//                    }catch (TelegramApiException e){
//                        e.printStackTrace();
//                    }
                    break;
                case "/filter":
                    if((flag == 1)) {
                        data.userData.get(n_users_chat).chats.get(n_users_chat).filter = !data.userData.get(n_users_chat).chats.get(n_users_chat).filter;
                        data.userData.get(n_users_chat).chats.get(n_users_chat).full = false;
                    }
                    break;
                case "/start":
                    if ((flag == 1)){
                        if(message.getFrom().getId().toString().equals(message.getChatId().toString())){
                            try {
                                execute(sendMessage.setText("Здесь этого сделать нельзя"));
                            } catch (TelegramApiException e) {
                                e.printStackTrace();
                            }
                        }else {
                            if(chat_flag == 0) {
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
                    }else{
                        try {
                            execute(sendMessage.setText("Нужен доступ к Google-Drive\nПредоставить доступ - /reg"));
                        } catch (TelegramApiException e) {
                            e.printStackTrace();
                        }
                    }

                    break;
                case "/reg":
                    try {
                        execute(sendMessage.setText("Недоступно"));
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                    break;
                default:
                    if(message.getFrom().getId().toString().equals(message.getChatId().toString())){
                        try {
                            execute(sendMessage.setText("Привет, если меня добавить в чат и написать в чате /start, то я начну загружать файлы отправленные пользователми к вам на гугл диск. Если вас интересует мой функционал напишите /command "));
                        } catch (TelegramApiException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
            }
        }

//        if(update.hasCallbackQuery()){
//            if(update.getCallbackQuery().getData().equals("Push1")){
//                System.out.println("ssss");
//            }
//        }



        if (message.hasDocument() ) {
            for (UserData userData : data.userData) {
                for (ChatData chatData : userData.chats) {
                    filter = chatData.filter;
                    full = chatData.full;
                    if (chat_id.equals(chatData.chat_id)) {
                        if (full) {
                            int full_flag = 0;
                            for( Users users : chatData.users) {
                                if (users.user_id_s.equals(user_id)){
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
                        if(filter) {
                            int n = 0;
                            for(Users users: chatData.users){
                                n++;
                                if (users.user_id_s.equals(user_id)){
                                    for (Integer i : chatData.user_choose){
                                        if(n == i) {
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
                        if(!filter&&!full){
                            int n = 0;
                            int check = 0;
                            for(Users users: chatData.users){
                                n++;
                                if (users.user_id_s.equals(user_id)){
                                    for (Integer i : chatData.user_choose){
                                        if(n == i) {
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
                            if (check == 0){
                                    down_up(message, chatData.chat_folder_id);
                            }
                        }
                    }
                }
            }
        }
    }



//
//


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
            down_file = new URL("https://api.telegram.org/file/bot" + "1398352830:AAHEHFlLE8XomFRAGHOV6TcDJmSOZgVJk5c" + "/" + file_path_id).openStream();
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

