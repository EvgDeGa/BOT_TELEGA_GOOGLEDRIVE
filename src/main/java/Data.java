import java.util.ArrayList;

public class Data {

    ArrayList<UserData> userData = new ArrayList<>();
    ArrayList<ChatData> chatData = new ArrayList<>();

    Data(UserData _userData,ChatData _chatData){
        chatData.add(_chatData);
        userData.add(_userData);
    }

    void Set_userData(UserData _userData){
        userData.add(_userData);
    }

    void Set_chatData(ChatData _chatData){
        chatData.add(_chatData);
    }
}
