package eu.tutorials.filmsite.profile_page
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import com.bumptech.glide.Glide
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import androidx.appcompat.app.ActionBarDrawerToggle
import eu.tutorials.filmsite.favorites_page.FavoritesFragment
import eu.tutorials.filmsite.R
import eu.tutorials.filmsite.login_page.MainKayitOl
import eu.tutorials.filmsite.movies_page.MoviesActivity


class ProfileActivity : AppCompatActivity(), ProfileInfoFragment.OnProfileUpdatedListener {
    private lateinit var profileImage: ImageView //daha sonra başlatılacak, başta null olmasına gerek kalmaz
    private lateinit var profileEmail: TextView
    private lateinit var nameTextView: TextView
    private lateinit var ageTextView: TextView
    private lateinit var phoneTextView: TextView

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var navigationView: NavigationView
    private lateinit var drawerLayout: DrawerLayout

    private var userEmail: String? = null
    private lateinit var toolbar: androidx.appcompat.widget.Toolbar




    override fun onProfileUpdated(){
        updateUserInfoFromFirestore()
    }


    override fun onFragmentClosed() {
        val detailsButton = findViewById<Button>(R.id.btndetails)
        detailsButton.visibility = View.VISIBLE
        val favoritesButton = findViewById<Button>(R.id.btnfavorites)
        favoritesButton.visibility = View.VISIBLE

    }





    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()




        profileImage = findViewById(R.id.profileImage)
        profileEmail = findViewById(R.id.profileEmail)
        nameTextView = findViewById(R.id.name)
        ageTextView = findViewById(R.id.age)
        phoneTextView = findViewById(R.id.phone)


        navigationView = findViewById(R.id.navigation_view)
        drawerLayout = findViewById(R.id.drawer)

        userEmail = intent.getStringExtra("email")



        toolbar = findViewById(R.id.toolbar2)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayShowTitleEnabled(false)
            setDisplayShowCustomEnabled(true)
            setCustomView(R.layout.toolbar_logo_only_2)
        }

        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()



        supportFragmentManager.addOnBackStackChangedListener {
            val detailsButton = findViewById<Button>(R.id.btndetails)
            val favoritesButton = findViewById<Button>(R.id.btnfavorites)

            if (supportFragmentManager.backStackEntryCount == 0) {
                // Fragment stack boşsa yani geri gelinmişse butonları tekrar göster
                detailsButton.visibility = View.VISIBLE
                favoritesButton.visibility = View.VISIBLE
            }
        }



        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_profile -> {
                    val intent = Intent(this, ProfileActivity::class.java)
                    intent.putExtra("email", userEmail)
                    startActivity(intent)
                    drawerLayout.closeDrawers()
                    true
                }
                R.id.menu_favorites -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainer3, FavoritesFragment())
                        .addToBackStack(null)
                        .commit()
                    val detailsButton = findViewById<Button>(R.id.btndetails)
                    detailsButton.visibility = View.GONE
                    val favoritesButton = findViewById<Button>(R.id.btnfavorites)
                    favoritesButton.visibility = View.GONE
                    drawerLayout.closeDrawers()
                    true
                }
                R.id.menu_home -> {
                    val intent = Intent(this, MoviesActivity::class.java)
                    intent.putExtra("email", userEmail)
                    startActivity(intent)
                    drawerLayout.closeDrawers()
                    true
                }
                R.id.menu_logout -> {

                    FirebaseAuth.getInstance().signOut()

                    val intent = Intent(this, MainKayitOl::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    intent.putExtra("isLogout", true)
                    startActivity(intent)

                    drawerLayout.closeDrawers()
                    true
                }

                else -> false


            }}

        val email = intent.getStringExtra("email") //bu activity'ye gelen Intent objesinden email adlı veriyi alır.
        //profileEmail.text = email ?: "Email bilgisi yok"

        val detailsButton = findViewById<Button>(R.id.btndetails)
        val favoritesButton = findViewById<Button>(R.id.btnfavorites)
        detailsButton.setOnClickListener{
            //val email = intent.getStringExtra("email")


            detailsButton.visibility = View.GONE
            favoritesButton.visibility = View.GONE




            val fragment = ProfileInfoFragment()
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .addToBackStack(null)
                .commit()





            /*
                        supportFragmentManager.beginTransaction()
                            //Fragment işlemleri (ekle, sil, değiştir) için bir işlem (transaction) başlatır
                            //.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                            .replace(R.id.fragmentContainer, fragment) //.replace(...): Var olan içeriği silip yerine yeni fragment'ı ekler
                            .addToBackStack(null) //geri tuşuyla geri alınabilir
                            .commit() //yukarıdaki tüm FragmentTransaction işlemlerini gerçekleştirir */



            onBackPressedDispatcher.addCallback(this){
                if (supportFragmentManager.backStackEntryCount > 0){ //eğer bir fragment eklenmişse (ve geri alınabilir durumdaysa), bu blok çalışır
                    supportFragmentManager.popBackStack() //En son eklenmiş fragment geri alınır (çıkartılır)  yani kullanıcı en son eklenmiş fragmenta geri döner
                    detailsButton.visibility = View.VISIBLE
                    favoritesButton.visibility = View.VISIBLE


                }
                else{
                    finish()
                }
            }






        }

        favoritesButton.setOnClickListener {
            detailsButton.visibility = View.GONE
            favoritesButton.visibility = View.GONE

            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer3, FavoritesFragment())
                .addToBackStack(null)
                .commit()

            onBackPressedDispatcher.addCallback(this){
                if (supportFragmentManager.backStackEntryCount > 0){
                    supportFragmentManager.popBackStack()
                    detailsButton.visibility = View.VISIBLE
                    favoritesButton.visibility = View.VISIBLE


                }
                else{
                    finish()
                }
            }
        }



    }



    override fun onResume(){ //Activity veya Fragment ön plana geldiğinde (visible olduğunda) çağrılır
        super.onResume() //orijinal onResume davranışları korunsun
        updateUserInfoFromFirestore()



    }



    private fun updateUserInfoFromFirestore(){
        val currentUser = auth.currentUser
        if (currentUser != null){
            val uid = currentUser.uid

            db.collection("userInfo").document(uid).get()
                .addOnSuccessListener { document ->
                    if(document != null && document.exists()) {
                        val name = document.getString("name") ?: ""
                        val age = document.getString("age") ?: ""
                        val phone = document.getString("phone") ?: ""
                        val profile_image = document.getString("profileImageUrl") ?: ""

                        nameTextView.text = name
                        ageTextView.text = age
                        phoneTextView.text = phone

                        Glide.with(this@ProfileActivity)
                            .load(profile_image)
                            .placeholder(R.drawable.account)
                            .error(R.drawable.logo)
                            .circleCrop()
                            .into(profileImage)



                    }


                }
                .addOnFailureListener{e->
                    Log.e("FIRESTORE","VERİ alma hatası",e)

                }
        }
    }



}
