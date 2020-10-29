import java.util.ArrayList;
import java.util.List;

public class UserData {
    public String self_user_id;// id авт. пользователя
    public String user_name;// имя пользователя
    public String folder_id = "1";//id папки
    public String folders;
    public int n_user; // номер авт. пользователя в чате
    public int n_users_chat;// номер чата откуда пришёл запрос
    public int U_S;// режим для натсройки
    public String poll_ch_chat = "";//для сравнения для какого чата был запрос
    public String poll_us_chat = "a";//для сравнения для какого чата был запрос
    public String mode = "";// режим для настройки full filter

    public ArrayList<ChatData> chats  = new ArrayList<>();// список чатов откуда качать файлы

    UserData(String _self_user_id,String _user_name, String _folder_id, String _folders,  ChatData _chats){

        self_user_id = _self_user_id;
        user_name = _user_name;
        folder_id = _folder_id;
        folders =  _folders;


        chats.add(_chats);
    }
//
//    String Get_self_user_id( ){
//        return self_user_id;
//    }
//
//    String Get_folder_id( ){
//        return folder_id;
//    }
//
//    String Get_folders( ){
//        return folders;
//    }
//
//    Boolean Get_filter( ){
//        return filter;
//    }
//    Boolean Get_chat_id( ){
//        return full;
//    }
//
//    ArrayList<ChatData> Get_user_id( ){
//        return chats;
//    }
//
//    void Set_self_user_id(String _self_user_id){
//        self_user_id = _self_user_id;
//    }
//    void Set_folder_id(String _folder_id){
//        folder_id = _folder_id;
//    }
//    void Set_folders(String _folders){
//        folders = _folders;
//    }
//    void Set_filter(Boolean _filter){
//        filter = _filter;
//    }
//    void Set_full(Boolean _full){
//        full = _full;
//    }
    void Set_chats(ChatData _chats){
        chats.add(_chats);
    }




}
