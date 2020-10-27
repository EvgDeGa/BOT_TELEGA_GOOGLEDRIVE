import java.util.ArrayList;
import java.util.List;

public class UserData {
    public String self_user_id;
    public String user_name;
    public String folder_id = "1";
    public String folders;

    public ArrayList<ChatData> chats  = new ArrayList<>();;

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
