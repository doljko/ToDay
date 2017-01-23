package mn.pomodoro;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RelativeLayout;

import java.util.Random;

public class MainActivity extends AppCompatActivity
        implements ShakeSensor.ShakeListener{
    View view;
    ShakeSensor shakeSensor;
    public RelativeLayout main;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        shakeSensor = new ShakeSensor();
        shakeSensor.setListener(this);
        shakeSensor.init(this);

        main = (RelativeLayout)findViewById(R.id.content_main);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onShake() {
        Random random = new Random();
        int r = random.nextInt(256);
        int g = random.nextInt(256);
        int b = random.nextInt(256);
        main = (RelativeLayout)findViewById(R.id.content_main);
        main.setBackgroundColor(Color.rgb(r,g,b));
//        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
//        builder.setTitle("Цээжлэх үгийн жагсаалт");
//        builder.setIcon(R.drawable.common_google_signin_btn_icon_dark);
//        builder.setMessage("XAXA");
//        builder.setPositiveButton("Хаах", new DialogInterface.OnClickListener() {
//            public void onClick(DialogInterface dialog, int id) {
//                onResume();
//                // Do something
//            }
//        });
//        builder.show();
//        onPause();
    }
    @Override
    protected void onResume() {
        super.onResume();
        shakeSensor.register();
    }
    @Override
    protected void onPause() {
        super.onPause();
        shakeSensor.deregister();
    }
}
