package mn.today;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.OpenFileActivityBuilder;
import com.kobakei.ratethisapp.RateThisApp;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by Tortuvshin Byambaa on 2/15/2017.
 */


public class TheHubActivity extends AppCompatActivity implements HubRecyclerViewAdapter.onCardClickListener, NavigationView.OnNavigationItemSelectedListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    /* Data Management and Export */
    private GoogleApiClient  mGoogleApiClient;
    private AppDataManager manager;
    // Manages the saving of data and Flow objects to internal storage

    /* Recycler View */
    private RecyclerView recyclerView;
    private HubRecyclerViewAdapter adapter;
    private ArrayList<ToDay> rvContent;

    /* Card Interactions */
    private String menuState;
    private PopupWindow longClickPopup, editingPopup;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_the_hub);

        /* Set up ActionBar */
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_the_hub);
        setSupportActionBar(toolbar);

        /* Set up Navigation Drawer */
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.hub_drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);

        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        generateDrawerGreeting(navigationView);

        /* Set up recycler and Card View */
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        if (savedInstanceState != null && !savedInstanceState.isEmpty()) {
            rvContent = savedInstanceState.getParcelableArrayList(AppConstants.RESTORED_USER_FLOWS);
            manager = savedInstanceState.getParcelable(AppConstants.RESTORED_DATA_MANAGER);
        } else {
            rvContent = new ArrayList<>();
            manager = new AppDataManager(this);
        }

        menuState = AppConstants.MENU_ITEMS_NATIVE;

        // ATTENTION: This "addApi(AppIndex.API)"was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Drive.API)
                .addScope(Drive.SCOPE_FILE)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(AppIndex.API).build();
    }

    private void generateDrawerGreeting(NavigationView view) {
        View header=view.getHeaderView(0);
        TextView greeting = (TextView) header.findViewById(R.id.ndrawer_date_greeting);
        String[] array = this.getResources().getStringArray(R.array.drawer_greeting);

        switch(Calendar.getInstance().get(Calendar.DAY_OF_WEEK)){
            case Calendar.MONDAY:
                greeting.setText(array[0]);
                break;

            case Calendar.TUESDAY:
                greeting.setText(array[1]);
                break;

            case Calendar.WEDNESDAY:
                greeting.setText(array[2]);
                break;
            case Calendar.THURSDAY:
                greeting.setText(array[3]);
                break;
            case Calendar.FRIDAY:
                greeting.setText(array[4]);
                break;
            case Calendar.SATURDAY:
                greeting.setText(array[5]);
                break;

            case Calendar.SUNDAY:
                greeting.setText(array[6]);
                break;

            default:
                greeting.setText(array[7]);
                break;

        }

    }

    @Override
    public boolean onNavigationItemSelected(MenuItem menuItem) {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.hub_drawer_layout);

        switch (menuItem.getItemId()) {

            case R.id.action_tou:
                drawer.closeDrawer(GravityCompat.START);
                goToLicense();
                return false;
            case R.id.action_support_devs:
                Toast.makeText(this,R.string.feature_not_ready,Toast.LENGTH_LONG).show();
                return false;

            case R.id.action_export_data:
                exportDataToDrive();
                return true;

            default:
                return false;
        }

    }


    /* Allows the menu items to appear in the toolbar */
    @Override
    public boolean onPrepareOptionsMenu(final Menu menu) {
        menu.clear();
        getMenuInflater().inflate(R.menu.menu_the_hub, menu);

        MenuItem newF = menu.findItem(R.id.action_new_flow);
        MenuItem deleteAllF = menu.findItem(R.id.action_delete_flows);
        if (menuState.equals(AppConstants.MENU_ITEMS_HIDE)) {
            newF.setVisible(false);
            deleteAllF.setVisible(false);
        }
        return super.onCreateOptionsMenu(menu);
    }

    /* Invokes methods based on the icon picked in the toolbar */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        /* When the user selects one of the app bar items, the system
        calls your activity's onOptionsItemSelected() callback method,
        and passes a MenuItem object to indicate which item was clicked */

        switch (item.getItemId()) {

            case R.id.action_delete_flows:
                deleteFlowsDialog();
                return true;

            case R.id.action_new_flow:
                createNewFlow();
                return true;

            case R.id.action_send_feedback:
                AppUtils.sendFeedback(this);
                return true;
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    private void goToLicense() {
        startActivity(
                new Intent(TheHubActivity.this, LicenseActivity.class)
        );

    }


    @Override
    protected void onResume() {
        super.onResume();
        menuState=AppConstants.MENU_ITEMS_NATIVE;
        invalidateOptionsMenu();
        populateRecycleView();
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(Drive.API)
                    .addScope(Drive.SCOPE_FILE)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
        }
        mGoogleApiClient.connect();
    }


    /** Populates the RecycleView by asking for internal storage to be read,
     *  determining whether the response data is valid, and rebuilding the
     *  RecycleView if possible.
     */
    private void populateRecycleView() {

        boolean savedContentAvailable = manager.hasData();
        // Reads Internal Storage JSON file, receiving return in String Format

        if (savedContentAvailable) {

            new Runnable() {
                @Override
                public void run() {
                    rebuildContent();
                }
            }.run();


        } else {
                /* If no data is avaliable from file, a new Array Adapter will be setup and
                   feed a blank RecycleView
                 */

            adapter = new HubRecyclerViewAdapter(TheHubActivity.this, rvContent);
            // Create new adapter with Recycle View Content
//            adapter.setCardEditingCallback(this);
            recyclerView.setAdapter(adapter);

        }
    }

    /** Attempts to rebuild the RecycleView Content by asking for the AppDataManager to create
     *  an ArrayList of it's available data  from file to recreate the RecViewAdapter.
     *
     */
    private void rebuildContent() {

        rvContent = manager.generateArrayList();

        adapter = new HubRecyclerViewAdapter(TheHubActivity.this, rvContent);
        // Recreate FlowArrayAdapter and set

//        adapter.setCardEditingCallback(this);
        recyclerView.setAdapter(adapter);

    }




    /** Launches the process of creating a new Flow Object by
     *  developing a Custom Dialog Box and determining valid
     *  user input.
     *
     */
    public void createNewFlow() {

        //Create edit text field for name entry
        final EditText nameInputET = new EditText(TheHubActivity.this);
        AlertDialog.Builder customDialog = generateCustomDialog(nameInputET);

        customDialog.setPositiveButton("Lets Roll",
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        if (nameInputET.getText().toString().equals("")) {
                            // Need to optimize this so that the dialog does NOT disappear and just display toast
                            Toast.makeText(TheHubActivity.this, "Every Flow deserves a good name :(", Toast.LENGTH_LONG).show();

                            createNewFlow(); //Recall the dialog
                        } else {

                            ToDay newF = new ToDay(nameInputET.getText().toString(), 0);

                            if (adapter != null) {
                                rvContent.add(newF);
                                // Set the Flow Manager Index and add to List View Content

                                adapter.notifyDataSetChanged();
                            }

                            manager.save(newF.getUuid(),newF);

                        }
                    }
                });

        customDialog.setNegativeButton("Nevermind",
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                    }
                });

        customDialog.show();

    }



    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    private AlertDialog.Builder generateCustomDialog(EditText nameInputET) {
        AlertDialog.Builder newFlowDialog = new AlertDialog.Builder(TheHubActivity.this);

        //Sets up Layout Parameters
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        params.setMarginStart(42);
        params.setMarginEnd(50);


        AppUtils.setNameInputFilters(nameInputET);

        //Adds the ET and params to the layout of the dialog box
        layout.addView(nameInputET, params);

        newFlowDialog.setTitle("Name your new Flow.");

        newFlowDialog.setView(layout);

        return newFlowDialog;
    }


    /**
     * Creates and prompts user for confirmation of deleting all
     * Flow's in the list view from file
     */
    private void deleteFlowsDialog() {
        new AlertDialog.Builder(this)
                .setTitle("ALL Flows will be deleted.")
                .setMessage("This action is PERMANENT")
                .setCancelable(false)
                .setPositiveButton("Understood", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        deleteAllFlowData();
                        Toast.makeText(TheHubActivity.this,
                                "Those poor Flows. \nI hope you're proud of yourself",
                                Toast.LENGTH_LONG)
                                .show();
                    }
                })
                .setNegativeButton("No Don't! It's a Trap!", null)
                .show();
    }

    /** Deletes All Flows visible in the RecView as well as
     *  requests for all Flow data to be deleted from Internal Storage.
     *
     * @return boolean, confirmation of
     */
    private void deleteAllFlowData() {
        rvContent.removeAll(rvContent);
        manager.deleteAllData();
        adapter.notifyDataSetChanged();
    }

    /**
     * Sends the user to the FlowSandbox Activity while passing the Flow that was clicked and it's
     * UUID for use in the next activity
     * @param clickedFlow flow that was clicked
     */
    @Override
    public void onCardClick(ToDay clickedFlow) {
        Intent i = new Intent(TheHubActivity.this, SandBoxActivity.class);

        i.putExtra(AppConstants.EXTRA_PASSING_UUID, clickedFlow.getUuid());
        startActivity(i);
    }

    /**
     * Shows a popupmenu related to editing or deleting the Flow that was LongClicked
     *
     * @param longClickedFlow Flow represented by cardview longclicked
     * @param cardPosition position of cardview in adapter
     * @param cardViewClicked the cardview view object clicked
     * @return boolean representing consumption
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public boolean onCardLongClick(ToDay longClickedFlow, int cardPosition, View cardViewClicked) {
        return  showLongClickPopUpMenu(longClickedFlow,cardPosition, cardViewClicked);
    }

    /**
     * Generates a Popup Menu with Two Actions Edit and Delete.
     *
     * Deleting the Flow removes the single card from the UI and also notifiers the AppDataManager to
     * delete from file
     *
     * Editing launches a renaming process
     *
     * @param longClickedFlow Flow represented by cardview longclicked
     * @param cardPosition position of cardview in adapter
     * @param cardViewClicked the cardview view object clicked
     * @return
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private boolean showLongClickPopUpMenu(final ToDay longClickedFlow, final int cardPosition, final View cardViewClicked) {
        LayoutInflater layoutInflater = (LayoutInflater) this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = layoutInflater.inflate(R.layout.popup_window_longclick, null);

        LinearLayout viewGroup = (LinearLayout)  layout.findViewById(R.id.popup_longclick);

        // Creating the PopupWindow
        final PopupWindow popup = new PopupWindow(layout, RecyclerView.LayoutParams.WRAP_CONTENT,
                RecyclerView.LayoutParams.WRAP_CONTENT);

        int dividerMargin = viewGroup.getDividerPadding(); // Top bottom
        int popupPadding = layout.getPaddingBottom();
        int popupDisplayHeight = -(cardViewClicked.getHeight()-dividerMargin-popupPadding);


        // Prevents border

        popup.setBackgroundDrawable(new ColorDrawable());
        popup.setFocusable(true);

        // Getting a reference to Close button, and close the popup when clicked.
        ImageView delete = (ImageView) layout.findViewById(R.id.popup_delete_item);

        delete.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                /* Deletes current Flow from file and UI */
                rvContent.remove(cardPosition);
                manager.delete(longClickedFlow.getUuid());
                adapter.notifyItemRemoved(cardPosition);
                adapter.notifyItemRangeChanged(cardPosition, adapter.getItemCount());

                popup.dismiss();

                Snackbar bar = Snackbar.make(cardViewClicked, R.string.snackbar_hub_msg, Snackbar.LENGTH_LONG)
                        .setAction("NO!!!", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                rvContent.add(cardPosition, longClickedFlow);
                                manager.save(longClickedFlow.getUuid(), longClickedFlow);
                                adapter.notifyItemInserted(cardPosition);
                            }
                        });


                bar.show();
            }
        });

        ImageView edit = (ImageView) layout.findViewById(R.id.popup_edit_item);

        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popup.dismiss();
                renameFlow(cardPosition, cardViewClicked);

            }
        });

        // Displaying the popup at the specified location, + offsets.
        popup.showAsDropDown(cardViewClicked, cardViewClicked.getMeasuredWidth(),popupDisplayHeight, Gravity.TOP);
        longClickPopup = popup;
        return true;
    }

    /**
     * Hides the Options Menu and uses a ViewSwitcher to quick turn the exisiting TextView with the
     * Flow's name into an EditText for the user to rename.
     *
     * @param cardPosition position of cardview in adapter
     * @param cardViewClicked the cardview view object clicked
     */
    private void renameFlow(final int cardPosition, final View cardViewClicked) {
        menuState = AppConstants.MENU_ITEMS_HIDE;
        invalidateOptionsMenu();
        final ViewSwitcher switcher = (ViewSwitcher) cardViewClicked.findViewById(R.id.hub_rename_switcher);
        final EditText rename = (EditText) switcher.findViewById(R.id.hub_item_flow_rename);

        AppUtils.setNameInputFilters(rename);

        rename.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (rename.hasFocus()) {
                    showEditPopupWindow(rename, cardViewClicked, switcher, cardPosition);
                }
            }
        });

        switcher.showNext();

        rename.requestFocus();
        /* Forces keyboard */


    }

    /**
     * Displays a popup window prompting the user to
     *
     * Confirm the changes to the name, saving the new name to file and updating the UI.
     *
     * Cancel the changes, returning the user back to the original state before editing
     *
     * @param newName new name to be used
     * @param cardPosition position of cardview in adapter
     * @param cardViewClicked the cardview view object clicked
     * @param switcher the viewSwitcher object used to rename
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void showEditPopupWindow(final EditText newName, View cardViewClicked, final ViewSwitcher switcher, final int cardPosition) {
        LayoutInflater layoutInflater = (LayoutInflater) this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = layoutInflater.inflate(R.layout.popup_window_editing, null);

        LinearLayout viewGroup = (LinearLayout)  layout.findViewById(R.id.popup_editing);

        // Creating the PopupWindow
        final PopupWindow popupEditing = new PopupWindow(layout, RecyclerView.LayoutParams.WRAP_CONTENT,
                RecyclerView.LayoutParams.WRAP_CONTENT);

        int dividerMargin = viewGroup.getDividerPadding(); // Top bottom
        int popupPadding = layout.getPaddingBottom();
        int popupDisplayHeight = -(cardViewClicked.getHeight()-dividerMargin-popupPadding);


        // Prevents border from appearing outside popupwindow
        popupEditing.setBackgroundDrawable(new ColorDrawable());
        popupEditing.setFocusable(false);

        // Getting a reference to Close button, and close the popup when clicked.
        ImageView confirmEdit = (ImageView) layout.findViewById(R.id.popup_confirm_item_changes);

        confirmEdit.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                ToDay toChange = rvContent.get(cardPosition);
                if (newName.getText().toString().equals("")) {
                    // Need to optimize this so that the dialog does NOT disappear and just display toast
                    Toast.makeText(TheHubActivity.this, "This Flow needs a name!", Toast.LENGTH_LONG).show();
                } else {
                    toChange.setName(newName.getText().toString());
                    manager.overwrite(toChange.getUuid(), toChange);
                    adapter.notifyDataSetChanged();
                    switcher.showNext();
                    menuState=AppConstants.MENU_ITEMS_NATIVE;
                    invalidateOptionsMenu();
                    popupEditing.dismiss();
                    newName.clearFocus();
                }

            }
        });

        ImageView cancelEdit = (ImageView) layout.findViewById(R.id.popup_cancel_item_changes);

        cancelEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switcher.showNext();
                menuState=AppConstants.MENU_ITEMS_NATIVE;
                invalidateOptionsMenu();
                popupEditing.dismiss();
            }
        });

        // Displaying the popup at the specified location, + offsets.
        popupEditing.showAsDropDown(cardViewClicked, cardViewClicked.getMeasuredWidth(),popupDisplayHeight, Gravity.TOP);
        editingPopup = popupEditing;
    }


    /**
     * Popups must be removed before activity becomes invisible
     * Api Client must disconnect.
     */
    @Override
    protected void onPause() {
        dismissPopups();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        dismissPopups();
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.hub_drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
        super.onBackPressed();
    }


    @Override
    protected void onStart() {
        super.onStart();
        // Monitor launch times and interval from installation
        mGoogleApiClient.connect();

        RateThisApp.onStart(this);
        RateThisApp.Config config = new RateThisApp.Config(10, 10);
        // Custom title ,message and buttons names
        config.setTitle(R.string.rate_app_title);
        config.setMessage(R.string.rate_app_message);
        config.setYesButtonText(R.string.rate);
        config.setNoButtonText(R.string.no_rate);
        config.setCancelButtonText(R.string.rate_cancel);
        RateThisApp.init(config);

        // If the criteria is satisfied, "Rate this app" dialog will be shown
        RateThisApp.showRateDialogIfNeeded(this);
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.start(mGoogleApiClient, getIndexApiAction());
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(AppConstants.RESTORED_USER_FLOWS, rvContent);
        outState.putParcelable(AppConstants.RESTORED_DATA_MANAGER, manager);
        super.onSaveInstanceState(outState);
    }

    /**
     * Catches any popups still open to prevent window leaking when issuing new Intents
     */
    private void dismissPopups() {
        if (longClickPopup!=null && longClickPopup.isShowing()) {
            longClickPopup.setFocusable(false);
            longClickPopup.dismiss();
        }

        if (editingPopup!=null && editingPopup.isShowing()) {
            editingPopup.setFocusable(false);
            editingPopup.dismiss();
        }


    }

    private void exportDataToDrive() {
        createNewDriveFile();
    }

    private void createNewDriveFile() {

        final ResultCallback<DriveApi.DriveContentsResult> contentsCallback = new
                ResultCallback<DriveApi.DriveContentsResult>() {
                    @Override
                    public void onResult(DriveApi.DriveContentsResult result) {
                        if (!result.getStatus().isSuccess()) {
                            Toast.makeText(TheHubActivity.this, R.string.export_failed_msg, Toast.LENGTH_LONG).show();
                            return;
                        }

                        MetadataChangeSet metadataChangeSet = new MetadataChangeSet.Builder()
                                .setMimeType("text/html").build();
                        IntentSender intentSender = Drive.DriveApi
                                .newCreateFileActivityBuilder()
                                .setInitialMetadata(metadataChangeSet)
                                .setInitialDriveContents(result.getDriveContents())
                                .build(mGoogleApiClient);
                        try {
                            startIntentSenderForResult(intentSender,
                                    AppConstants.EXPORT_CREATOR_REQUEST_CODE, null, 0, 0, 0);
                        } catch (IntentSender.SendIntentException e) {
                            AppUtils.showMessage(TheHubActivity.this, "Data could not be exported");
                        }
                    }
                };

        Drive.DriveApi.newDriveContents(mGoogleApiClient)
                .setResultCallback(contentsCallback);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case AppConstants.EXPORT_RESOLVE_CONNECTION_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    mGoogleApiClient.connect();
                }
                break;
            case AppConstants.EXPORT_CREATOR_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    DriveId driveFileId = (DriveId) data.getParcelableExtra(
                            OpenFileActivityBuilder.EXTRA_RESPONSE_DRIVE_ID);
                    writeDataExportToFile(driveFileId);
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

    private void writeDataExportToFile(DriveId driveFileId) {
        EditContentParams params = new EditContentParams(
                new ExportDataManager(TheHubActivity.this).readFileByInputStream(),
                driveFileId.asDriveFile()
        );

        new EditContentsAsyncTask(this).execute(params);

    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        /* Callback can be invoked if user has not previously authorized the app. */
        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(this, AppConstants.EXPORT_RESOLVE_CONNECTION_REQUEST_CODE);
            } catch (IntentSender.SendIntentException e) {
                // Unable to resolve, message user appropriately
            }
        } else {
            Toast.makeText(this, R.string.feedback_failed_msg, Toast.LENGTH_LONG).show();
            GooglePlayServicesUtil.getErrorDialog(connectionResult.getErrorCode(), this, 0).show();
        }

    }

    /**
     * Getter for the {@code GoogleApiClient}.
     */
    public GoogleApiClient getGoogleApiClient() {
        return mGoogleApiClient;
    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("TheHub Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(mGoogleApiClient, getIndexApiAction());
        mGoogleApiClient.disconnect();
    }
}
