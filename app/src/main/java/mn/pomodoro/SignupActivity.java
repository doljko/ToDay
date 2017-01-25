package mn.pomodoro;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;

import org.w3c.dom.Text;

import java.util.Objects;

import static mn.pomodoro.R.*;

public class SignupActivity extends AppCompatActivity {
    EditText usernameText;
    EditText emailAddress;
    EditText passOneTxt;
    EditText passReenter;
    Button signUpButton;
    TextView loginButton;

    public SharedPreferences sharedPreferences;
    Editor editor;
    Animation shakeAnimation;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layout.activity_signup);
        Toolbar toolbar = (Toolbar) findViewById(id.toolbar);
        setSupportActionBar(toolbar);
        usernameText = (EditText) findViewById(id.signUpName);
        emailAddress = (EditText) findViewById(id.emailAddress);
        passOneTxt = (EditText) findViewById(id.passOneTxt);
        passReenter = (EditText) findViewById(id.passReenter);
        signUpButton = (Button) findViewById(id.signUpButton);
        loginButton = (TextView) findViewById(id.signUpLoginButton);
        shakeAnimation = AnimationUtils.loadAnimation(getApplicationContext(), anim.shake);
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signup();
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    public void signup() {
        if (!validate()) {
//            onSignupFailed();
            Toast.makeText(getBaseContext(), "Алдаа гарлаа", Toast.LENGTH_LONG).show();
            return;
        }

        final String name = usernameText.getText().toString();
        String email = emailAddress.getText().toString();
        final String pass = passOneTxt.getText().toString();
        String passConfirm = passReenter.getText().toString();

        // TODO: Implement your own signup logic here.
        new Handler().postDelayed(
                new Runnable() {
                    public void run() {
//                        onSignupSuccess();
                        sharedPreferences = getSharedPreferences(LoginActivity.PREFER_NAME, 0);
                        editor = sharedPreferences.edit();
                        editor.putString("username", name);
                        editor.putString("password", pass);
                        editor.commit();

                        Toast.makeText(getBaseContext(), "Амжилттай бүртгэгдлээ", Toast.LENGTH_LONG).show();
                    }
                }, 1000);
    }

    public boolean validate() {
        boolean valid = true;

        String name = usernameText.getText().toString();
        String email = emailAddress.getText().toString();
        String pass = passOneTxt.getText().toString();
        String passConfirm = passReenter.getText().toString();

        if (name.isEmpty() || name.length() < 3) {
            usernameText.setError("Нэр оруулна уу");
            usernameText.setAnimation(shakeAnimation);
            valid = false;
        } else {
            usernameText.setError(null);
        }

        if (email.isEmpty() || email.length() < 3) {
            emailAddress.setError(getString(string.mail_error));
            emailAddress.setAnimation(shakeAnimation);
            valid = false;
        } else {
            emailAddress.setError(null);
        }


        if (pass.isEmpty() || pass.length() < 3) {
            passOneTxt.setError("Нууц үгээ оруулна уу");
            passOneTxt.setAnimation(shakeAnimation);
            valid = false;
        } else {
            usernameText.setError(null);
        }


        if (passConfirm.isEmpty() || passConfirm.length() < 3) {
            passReenter.setError("Нууц үгээ дахин оруулна уу");
            passReenter.setAnimation(shakeAnimation);
            valid = false;
        } else {
            passReenter.setError(null);
        }

        if (Objects.equals(pass, passConfirm)) {
            passReenter.setError("Тэнцүү биш байна");
            passReenter.setAnimation(shakeAnimation);
            valid = false;
        } else {
            passReenter.setError(null);
        }
        return valid;

    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("Signup Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        client.disconnect();
    }
}
