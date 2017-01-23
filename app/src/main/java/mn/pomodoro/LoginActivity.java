package mn.pomodoro;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.animation.Interpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;

public class LoginActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        EditText usernameText = (EditText) findViewById(R.id.userNameText);
        EditText passwordText = (EditText) findViewById(R.id.passText);
        Button loginButton = (Button) findViewById(R.id.loginButton);
        TextView signUpTextView = (TextView) findViewById(R.id.signupTextView);

        setSupportActionBar(toolbar);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ProgressDialog progressDialog = new ProgressDialog(LoginActivity.this, R.style.AlertDialog_AppCompat_Dialog);
                progressDialog.setIndeterminate(true);
                progressDialog.setMessage("Уншиж байна...");
                progressDialog.show();
                new android.os.Handler().postDelayed(
                        new Runnable() {
                            @Override
                            public void run() {

                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                progressDialog.dismiss();
                            }
                        }
                , 2000);
            }
        });

        signUpTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
                startActivity(intent);
            }
        });
    }

}
