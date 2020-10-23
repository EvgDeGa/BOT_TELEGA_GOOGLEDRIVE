import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;

public class CreateFolder {

    public static final File createGoogleFolder(String folderIdParent, String folderName) throws IOException {

        File fileMetadata = new File();

        fileMetadata.setName(folderName);
        fileMetadata.setMimeType("application/vnd.google-apps.folder");

        if (folderIdParent != null) {
            List<String> parents = Arrays.asList(folderIdParent);

            fileMetadata.setParents(parents);
        }
        Drive driveService = GoogleDriveUtils.getDriveService();

        File file = driveService.files().create(fileMetadata).setFields("id, name").execute();
        return file;
    }

    //Создание папки TELEGA-DRIVE
    //Функция возвращает id созданной папки
    public static String Create_TG() throws IOException {
        File folder = createGoogleFolder(null, "TELEGA-DRIVE");
        return folder.getId();
    }

    public static void main(String ... args) throws IOException {

    }

}