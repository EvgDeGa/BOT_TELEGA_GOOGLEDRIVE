import org.apache.commons.io.FileUtils;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.polls.SendPoll;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;


public class Bot extends TelegramLongPollingBot {
    ArrayList<String> Create_Folder;

    public String Get_Create_Folder(int i) {
        return Create_Folder.get(i);
    }

    public void Set_Create_Folder(String Create_Folder_Buf) {
        Create_Folder.add(Create_Folder_Buf);
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
        ApiContextInitializer.init();
        TelegramBotsApi telegramApi = new TelegramBotsApi();
        try{
            telegramApi.registerBot(new Bot());
        } catch (TelegramApiException e){
            e.printStackTrace();
        }
    }

    public void download_file(String file_id) throws TelegramApiException, IOException {
        GetFile get_file = new GetFile().setFileId(file_id);
        String file_path_id = execute(get_file).getFilePath();
        File local_file = new File(file_path_id);
        InputStream down_file =  new URL("https://api.telegram.org/file/bot" + "1398352830:AAHEHFlLE8XomFRAGHOV6TcDJmSOZgVJk5c" + "/" + file_path_id).openStream();
        FileUtils.copyInputStreamToFile(down_file, local_file);
    }

    //Создание опроса с целью узнать для каких участников создать отдельные папки
    public SendPoll poll(String setChatId) throws TelegramApiException, IOException {
        SendPoll sendPoll = new SendPoll();
        sendPoll.setChatId(setChatId);
        sendPoll.setQuestion("Каво");
        ArrayList<String> options = new ArrayList<>();
        options.add("ghhsf");
        options.add("ghhsf");
        sendPoll.setOptions(options);
        return sendPoll;
    }

    public void onUpdateReceived(Update update) {
        SendMessage sendMessage = new SendMessage();
        Message message = update.getMessage();
        sendMessage.setChatId(message.getChatId().toString());

        String folder_id = Get_Create_Folder();
        //Команды
        if (message.hasText()){
            switch (message.getText()){
                case "/help":
                    try {
                        DriveQuickstart.main();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (GeneralSecurityException e) {
                        e.printStackTrace();
                    }
                    break;
                case "/command":
                    //проверка авторизации
                    try {
                        DriveQuickstart.main();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (GeneralSecurityException e) {
                        e.printStackTrace();
                    }

                    try {
                        Create_Folder = Check();
                    } catch (IOException e) {
                        e.printStackTrace();
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
                    ArrayList<String> options = new ArrayList<>();
                    options.add("ghhsf");
                    options.add("ghhsf");
                    try {
                       execute(new SendPoll().setChatId(message.getChatId().toString()).setQuestion("SS").setOptions(options).setAllowMultipleAnswers(true));
                    }catch (TelegramApiException e){
                        e.printStackTrace();
                    }
                    break;
                case "/full":
//                    try {
//                        execute(new SendPoll().setChatId(message.getChatId().toString()).setQuestion("SS").setOptions(options).setAllowMultipleAnswers(true));
//                    }catch (TelegramApiException e){
//                        e.printStackTrace();
//                    }
//                    break;
                case "/start":
                    try {
                        CreateFolder.main();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                case "/reg":
                   // AuthorizationCodeFlow.loadCredential();
                    break;
                default:
                    try {
                        execute(sendMessage.setText("Привет, если меня добавить в чат, то я начну загружать файлы отправленные пользователми к вам на гугл диск. Если вас интересует мой функционал напишите /command "));
                    }catch (TelegramApiException e){
                        e.printStackTrace();
                    }
                    break;
            }
        }

//        if(update.hasCallbackQuery()){
//            if(update.getCallbackQuery().getData().equals("Push1")){
//                System.out.println("ssss");
//            }
//        }

        if (message.hasDocument() ){
            String file_id = message.getDocument().getFileId();
            GetFile file = new GetFile().setFileId(file_id);
            String file_path_id = "";
            try {
                file_path_id = execute(file).getFilePath();
            }catch (TelegramApiException e){
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
//        if (message.hasDocument()){
//            var file_id = update.message.document.fileId;
//            download_file(file_id);
//        }
    }


    public String getBotUsername() {
        return "Dorokhov_Google_Drive_bot";
    }

    public String getBotToken() {
        return "1398352830:AAHEHFlLE8XomFRAGHOV6TcDJmSOZgVJk5c";
    }
}
