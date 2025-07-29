package eu.tutorials.filmsite

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class MainKayitOl : AppCompatActivity() { //Activity'nin geliştirilmiş versiyonu olan base class

    private lateinit var auth: FirebaseAuth //firebaseauth nesnesi
    private lateinit var googleSignInClient: GoogleSignInClient  //google ile giriş ekranını başlatmak  için
    private lateinit var signInLauncher: ActivityResultLauncher<Intent> //giriş ekranını başlatmak için
    private lateinit var progressBar : ProgressBar //yüklenme işareti

    override fun onCreate(savedInstanceState: Bundle?) { //Bundle verilerin paketlenip taşınması vs için
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()


        val currentUser = FirebaseAuth.getInstance().currentUser
        val isFromLogout = intent.getBooleanExtra("isLogout",false)
        if(currentUser != null && !isFromLogout){
            startActivity(Intent(this, MoviesActivity::class.java))
            finish()
        }




        setContentView(R.layout.activity_main) //sayfaya ait xml dosyası tanımlanır
        progressBar = findViewById(R.id.progressBar)

        // Sistem barları için padding ayarı
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Firebase Auth initialize et
        auth = FirebaseAuth.getInstance()








        // View'ları bul
        val emailEditText = findViewById<EditText>(R.id.girisKullaniciAdi)
        val passwordEditText = findViewById<EditText>(R.id.girisParola)
        val btnGiris = findViewById<Button>(R.id.btnSignIn)
        val btnGoogle = findViewById<Button>(R.id.btnGoogleSignIn)
        val btnSignUp = findViewById<Button>(R.id.btnSignUp)



        // E-posta ve şifre ile giriş
        btnGiris.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Email ve şifre boş olamaz!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener //setOnClickListener'dan çık
            }

            progressBar.visibility = View.VISIBLE

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    progressBar.visibility = View.GONE
                    if (task.isSuccessful) {

                        Toast.makeText(this, "Sign-in Successful!", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, MoviesActivity::class.java) //Başarılıysa MoviesActivity sayfasına geçilir
                        intent.putExtra("email",email)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this, "Sign-in unsuccessful.: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }


        // Google Sign-In ayarları
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Google sign in başlatıcısı
        signInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            Log.d("GOOGLE_RESULT", "resultCode: ${result.resultCode}")
            if (result.resultCode == RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                if (task.isSuccessful) {
                    val account = task.result
                    Log.d("GOOGLE_RESULT", "Google Sign-in successful, account: ${account.email}")
                    firebaseAuthWithGoogle(account)


                } else {
                    Log.e("GOOGLE_SIGNIN", "Exception: ${task.exception?.message}", task.exception)
                    Log.e("GOOGLE_SIGNIN", "Google Sign-In başarısız: ", task.exception)
                    Toast.makeText(this, "Google Sign-In başarısız! ", Toast.LENGTH_SHORT).show()
                }
            } else {
                Log.d("GOOGLE_RESULT", "Google Sign-In iptal edildi veya başarısız oldu.")
                Toast.makeText(this, "Google Sign-In iptal edildi!", Toast.LENGTH_SHORT).show()
            }
        }

        // Google ile giriş başarılı ise burası çalışır
        btnGoogle.setOnClickListener {
            googleSignInClient.signOut().addOnCompleteListener {
                val signInIntent = googleSignInClient.signInIntent
                signInLauncher.launch(signInIntent)
            }
        }

        // Kayıt ol
        btnSignUp.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Email ve şifre boş olamaz!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener //setOnClickListener'dan çık
            }



            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task -> //this Activity
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Login unsuccessful!: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                        Log.e("SIGN_UP_ERROR", "Hata: ", task.exception)
                    }
                }
        }



        //şifremi unuttum
        val btnForgotPassword = findViewById<Button>(R.id.btnForgotPassword)

        btnForgotPassword.setOnClickListener{
            val email = emailEditText.text.toString().trim()

            if (email.isEmpty()){
                Toast.makeText(this, "Please enter your email address!",Toast.LENGTH_SHORT).show()
            }
            else{
                auth.sendPasswordResetEmail(email).addOnCompleteListener{
                        task ->
                    if(task.isSuccessful){
                        Toast.makeText(this,"The password reset link has been sent to your email address.",Toast.LENGTH_LONG).show()
                    }
                    else{
                        Toast.makeText(this,"Error: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                        Log.e("PASSWORD_RESET", "ERROR: ", task.exception)
                    }
                }

            }

        }
    }



    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount?) {// act: GoogleSignInAccount türünde dönen bir nesne
        progressBar.visibility = View.VISIBLE
        val credential = GoogleAuthProvider.getCredential(acct?.idToken, null)
        Log.d("GOOGLE_AUTH", "Google ID Token: ${acct?.idToken}")

        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                progressBar.visibility = View.GONE
                if (task.isSuccessful) {
                    Toast.makeText(this, "Google Sign-in successful!!", Toast.LENGTH_SHORT).show()
                    val accountEmail = acct?.email ?: "" //google hesabından email al

                    val intent = Intent(this, MoviesActivity::class.java)
                    intent.putExtra("email",accountEmail)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "Google ile giriş başarısız: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    Log.e("FIREBASE_AUTH", "Hata: ", task.exception)
                }
            }
    }


}
