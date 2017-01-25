package mn.pomodoro;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.animation.Interpolator;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    EditText usernameText;
    EditText passwordText;
    CheckBox rememberCheckBox;
    public static final String PREFER_NAME = "UserInfo";
    private SharedPreferences sharedPreferences;

    Editor editor;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        usernameText = (EditText) findViewById(R.id.userNameText);
        passwordText = (EditText) findViewById(R.id.passText);
        rememberCheckBox = (CheckBox) findViewById(R.id.rememberCheckBox);
        Button loginButton = (Button) findViewById(R.id.loginButton);
        TextView signUpTextView = (TextView) findViewById(R.id.signupTextView);


        setSupportActionBar(toolbar);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sharedPreferences = getSharedPreferences(PREFER_NAME, 0);
                final String thisUsername = sharedPreferences.getString("username", "");
                final String thisPassword = sharedPreferences.getString("password", "");
                String name = usernameText.getText().toString();
                String pass = passwordText.getText().toString();

                if (Objects.equals(thisUsername, name) || name.length() > 3) {
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(getBaseContext(), "Алдаа гарлаа", Toast.LENGTH_LONG).show();
                }

                if (Objects.equals(thisPassword, pass) || pass.length() > 5) {
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(getBaseContext(), "Алдаа гарлаа", Toast.LENGTH_LONG).show();
                }

            }
        });

        signUpTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
                startActivity(intent);
            }
        });

        rememberCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               Rememberme();
            }
        });
        SharedPreferences prefs = getSharedPreferences(PREFER_NAME, 0);
        String thisUsername = prefs.getString("username", "");
        String thisPassword = prefs.getString("password", "");
        boolean thisRemember = prefs.getBoolean("remember", false);
        if(thisRemember) {
            usernameText.setText(thisUsername);
            passwordText.setText(thisPassword);
            rememberCheckBox.setChecked(thisRemember);
        }

    }
    private void Rememberme() {
        boolean thisRemember = rememberCheckBox.isChecked();
        sharedPreferences = getSharedPreferences(PREFER_NAME, 0);
        editor = sharedPreferences.edit();
        editor.putBoolean("remember", thisRemember);
        editor.commit();
    }

}
