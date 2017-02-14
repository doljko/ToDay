package mn.today;
import com.google.android.gms.drive.DriveFile;

/**
 * Created by Tortuvshin Byambaa on 2/15/2017.
 */

public class EditContentParams {
    public DriveFile getFileToWrite() {
        return fileToWrite;
    }

    public String getDataToWrite() {
        return dataToWrite;
    }

    DriveFile fileToWrite;
    String dataToWrite;

    public EditContentParams(String dataToWrite, DriveFile fileToWrite) {
        this.dataToWrite = dataToWrite;
        this.fileToWrite = fileToWrite;
    }
}
