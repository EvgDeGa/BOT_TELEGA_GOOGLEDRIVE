import java.util.ArrayList;
import java.util.List;

public class ChatData {
    public String chat_id; // id чата
    ArrayList<Integer> user_choose = new ArrayList<>(); // номера пользователей для которых нужно создать папки
    public String chat_folder_id;// id папки чата
    public String chat_name;// имя чата
    public Boolean filter;// настройка filter
    public Boolean full;// настройка full
    public ArrayList<Users> users  = new ArrayList<>();// список пользователей чата

    ChatData(String _chat_id,int _user_choose, String _chat_folder_id,String _chat_name,Boolean _filter, Boolean _full, Users _users){
        chat_id = _chat_id;
        chat_folder_id = _chat_folder_id;
        chat_name = _chat_name;
        filter = _filter;
        full = _full;
        user_choose.add(_user_choose);
        users.add(_users);
    }

    //
//    String Get_chat_id( ){
//        return chat_id;
//    }
//
//    ArrayList<String> Get_user_id( ){
//        return user_id;
//    }
//
//    void Set_chat_id(String _chat_id){
//        chat_id = _chat_id;
//    }
//
    void Set_user_choose(int _user_choose){ user_choose.add(_user_choose);}
    void Set_user_id(Users _users){
        users.add(_users);
    }
}
