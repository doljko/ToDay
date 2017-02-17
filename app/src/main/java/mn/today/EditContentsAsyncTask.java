package mn.today;

import android.content.Context;

import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by Tortuvshin Byambaa on 2/15/2017.
 */


public class EditContentsAsyncTask extends GApiClientAsyncTask<EditContentParams, Void, Boolean> {

    private Context context;
    public EditContentsAsyncTask(Context context) {
        super(context);
        this.context = context;
    }

    @Override
    protected Boolean doInBackgroundConnected(EditContentParams... args) {
        DriveFile file = args[0].getFileToWrite();
        String dataToWrite = args[0].getDataToWrite();

        try {
            DriveApi.DriveContentsResult driveContentsResult = file.open(
                    getGoogleApiClient(), DriveFile.MODE_WRITE_ONLY, null).await();
            if (!driveContentsResult.getStatus().isSuccess()) {
                return false;
            }
            DriveContents driveContents = driveContentsResult.getDriveContents();
            OutputStream outputStream = driveContents.getOutputStream();
            outputStream.write(dataToWrite.getBytes());
            com.google.android.gms.common.api.Status status =
                    driveContents.commit(getGoogleApiClient(), null).await();
            return status.getStatus().isSuccess();
        } catch (IOException e) {
            AppUtils.showMessage(context,"Failed to write data to file.");
        }
        return false;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        if (!result) {
            AppUtils.showMessage(context,"Couldn't write data to file.");
            return;
        }
        AppUtils.showMessage(context,"Data exported :) ");
    }
}
