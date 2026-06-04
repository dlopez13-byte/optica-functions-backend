package com.example.optica;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.example.optica.data.ApiService;
import com.example.optica.data.RetrofitClient;
import com.example.optica.model.User;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import java.util.HashMap;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {
    
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private ProgressBar progressBar;
    private final ApiService apiService = RetrofitClient.getClient().create(ApiService.class);

    private final ActivityResultLauncher<Intent> googleSignInLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Intent data = result.getData();
                    try {
                        GoogleSignInAccount account = GoogleSignIn.getSignedInAccountFromIntent(data).getResult(ApiException.class);
                        if (account != null) {
                            firebaseAuthWithGoogle(account.getIdToken());
                        }
                    } catch (ApiException e) {
                        Log.e("QA_LOGIN", "Google Error: " + e.getStatusCode());
                        setLoading(false);
                    }
                } else {
                    setLoading(false);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        progressBar = findViewById(R.id.loginProgress);

        String clientId = getString(R.string.default_web_client_id);
        
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(clientId)
                .requestEmail()
                .build();
        
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        findViewById(R.id.btnGoogleLogin).setOnClickListener(v -> {
            setLoading(true);
            googleSignInLauncher.launch(mGoogleSignInClient.getSignInIntent());
        });
    }

    private void firebaseAuthWithGoogle(String googleIdToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(googleIdToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful() && mAuth.getCurrentUser() != null) {
                        mAuth.getCurrentUser().getIdToken(true).addOnCompleteListener(tokenTask -> {
                            if (tokenTask.isSuccessful() && tokenTask.getResult().getToken() != null) {
                                syncUserWithBackend(tokenTask.getResult().getToken());
                            } else {
                                setLoading(false);
                            }
                        });
                    } else {
                        setLoading(false);
                    }
                });
    }

    private void syncUserWithBackend(String firebaseToken) {
        Map<String, String> body = new HashMap<>();
        body.put("idToken", firebaseToken);

        apiService.login(body).enqueue(new Callback<User>() {
            @Override
            public void onResponse(@NonNull Call<User> call, @NonNull Response<User> response) {
                if (isFinishing()) return;
                setLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    User user = response.body();
                    saveUserRole(user.getRole());
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish();
                } else {
                    String errorMsg = "Error " + response.code();
                    try {
                        if (response.errorBody() != null) {
                            JSONObject jObjError = new JSONObject(response.errorBody().string());
                            errorMsg = jObjError.getString("message");
                        }
                    } catch (Exception e) {
                        errorMsg = "Acceso denegado: Fallo de llave de servidor";
                    }
                    Toast.makeText(LoginActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                    Log.e("QA_LOGIN", "401 Error: " + errorMsg);
                }
            }

            @Override
            public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {
                if (isFinishing()) return;
                setLoading(false);
                Toast.makeText(LoginActivity.this, "Servidor despertando...", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setLoading(boolean loading) {
        if (progressBar != null) progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        findViewById(R.id.btnGoogleLogin).setEnabled(!loading);
    }

    private void saveUserRole(String role) {
        SharedPreferences prefs = getSharedPreferences("OpticaPrefs", Context.MODE_PRIVATE);
        prefs.edit().putString("user_role", role).apply();
    }
}
