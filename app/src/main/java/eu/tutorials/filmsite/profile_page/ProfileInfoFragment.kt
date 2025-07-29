package eu.tutorials.filmsite.profile_page

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import eu.tutorials.filmsite.R
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException



class ProfileInfoFragment : Fragment(R.layout.fragment_profile_info) {

    companion object { //statik üyeleri tanımlamak için companion
        private const val arg_email = "email"
        //fragmenta veri geçirmek için oluşturuldu
        fun newInstance(email: String?): ProfileInfoFragment { //dışarıdan bir email alıyor
            val fragment = ProfileInfoFragment()
            val args = Bundle() //Bundle nesnesi oluşturulur. Fragment’lara veri taşımak için kullanılır.
            args.putString(arg_email, email) //Bundle içine "email" anahtarıyla email değeri yerleştirilir
            fragment.arguments = args //Oluşturulan Bundle, fragment’ın arguments özelliğine atanır
            return fragment
        }
    }

    private var email: String? = null

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var listener: OnProfileUpdatedListener? = null //fragment ile Activity arasında iletişim kurmak için kullanılan interface


    //profil fotoğrafı için
    private lateinit var profileImageView: ImageView
    private var selectedImageUri: Uri? = null //Uri: dosya yolu
    private lateinit var imagePickLauncher: ActivityResultLauncher<Intent> //Android'de kullanıcıdan bir işlem başlatıp (örneğin: galeri açıp resim seçtirme) sonucunu almanı sağlayan yeni nesil API olan Activity Result API’nin bir parçasıdır.


    //Activity, OnProfileUpdatedListener arayüzünü uyguluyorsa, listener değişkenine o activity atanır
    //fragment kendi activitysine erişir ve interface uyguluyor mu diye kontrol eder
    //eğer uyguluyorsa (yani: context is OnProfileUpdatedListener) → interface üzerinden listener değişkenine activity atanır
    //böylece fragment, activity’ye onProfileUpdated() gibi mesajlar gönderebilir
    //fragment ve activity iletişimi için bir köprü
    override fun onAttach(context: Context){ //fragment bir activitye bağlandığında çalışır
        super.onAttach(context)
        if (context is OnProfileUpdatedListener){  //interface
            listener = context //context=Activity
        }

    }




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        email = arguments?.getString(arg_email)

    }

    override fun onCreateView( //fragment_profile_info.xml dosyası, LayoutInflater yardımıyla View objesine çevrilir
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile_info, container, false) // inflate işlemi sırasında XML dosyasının hemen container'a eklenmesini isteyip istemediğini belirtir. sadece view oluşturur

        val editName = view.findViewById<EditText>(R.id.editName)
        val editAge = view.findViewById<EditText>(R.id.editAge)
        val editPhone = view.findViewById<EditText>(R.id.editPhone)
        val btnSave = view.findViewById<Button>(R.id.btnSaveInfo)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()



        btnSave.setOnClickListener {
            val name = editName.text.toString().trim()
            val age = editAge.text.toString().trim()
            val phone = editPhone.text.toString().trim()



            if (name.isEmpty() || age.isEmpty() || phone.isEmpty()) {
                Toast.makeText(requireContext(), "Lütfen tüm alanları doldurun", Toast.LENGTH_SHORT).show()
            }
            else if(selectedImageUri == null){
                Toast.makeText(requireContext(),"Please add a profile image",Toast.LENGTH_SHORT).show()
            }
            else {
                uploadImageToCloudinary(selectedImageUri!!,
                    onSuccess = {imageUrl ->
                        saveUserInfoToFirestore(name, age, phone, imageUrl)
                    },
                    onError = { errorMsg ->
                        Toast.makeText(requireContext(),errorMsg,Toast.LENGTH_SHORT).show()

                    })


            }
        }

        //profil footoğrafı için
        profileImageView = view.findViewById(R.id.profileImage2)

        imagePickLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {result ->
            if(result.resultCode == AppCompatActivity.RESULT_OK){
                selectedImageUri = result.data?.data
                profileImageView.setImageURI(selectedImageUri)
            }
        }

        profileImageView.setOnClickListener{
            openGallery()
        }




        return view
    }
    fun saveUserInfoToFirestore(name: String,age: String, phone:String, imageUrl: String){
        val currentUser = auth.currentUser
        if(currentUser != null) {
            val userInfo = hashMapOf(
                "name" to name,
                "age"  to age,
                "phone" to phone,
                "email" to currentUser.email,
                "profileImageUrl" to imageUrl
            )

            db.collection("userInfo").document(currentUser.uid)
                .set(userInfo)
                .addOnSuccessListener { Log.d("Firestore","Kullanıcı bilgileri kaydedildi")
                    listener?.onProfileUpdated() //listener null değilse
                    listener?.onFragmentClosed()
                    parentFragmentManager.popBackStack() //fragment geri yığınına dönülür yani kapanır
                }
                .addOnFailureListener{ e ->
                    Log.e("Firestore", "Kullanıcı bilgileri kaydedilirken hata",e)

                }
        }
    }

    private fun openGallery(){
        val intent = Intent(Intent.ACTION_PICK) //ACTION_PICK: Galeri gibi bir içerik sağlayıcıdan içerik seçmek için kullanılır
        intent.type = "image/*"
        imagePickLauncher.launch(intent)
    }

    private fun uploadImageToCloudinary(imageUri: Uri, onSuccess: (String) -> Unit, onError: (String) -> Unit){
        val cloudName = "dzwuedrfg"
        val uploadPreset = "film_site"
        val inputStream = requireContext().contentResolver.openInputStream(imageUri)
        val imageBytes = inputStream?.readBytes()

        //imageBytes, HTTP POST içinde kullanılacak RequestBody'ye dönüştürülüyor
        val requestBody = imageBytes?.let {
            okhttp3.RequestBody.create("image/*".toMediaTypeOrNull(), it)
        }

        val multipartBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("file","profile.jpg",requestBody!!)
            .addFormDataPart("upload_preset", uploadPreset)
            .build()

        //bu istek Cloudinary’nin dosya yükleme API’sine yönlendirilir
        val request = Request.Builder()
            .url("https://api.cloudinary.com/v1_1/$cloudName/image/upload")
            .post(multipartBody)
            .build()

        //bu satırda istek gönderilir ve cevap arka planda (asenkron şekilde) beklenir.
        OkHttpClient().newCall(request).enqueue(object: Callback {
            override fun onFailure(call: Call, e: IOException) {
                requireActivity().runOnUiThread{
                    onError("Yükleme başarısız: ${e.message}")
                }
            }

            override fun onResponse(call: Call, response: Response){
                val json = response.body?.string()
                val secureUrl = JSONObject(json).getString("secure_url")
                requireActivity().runOnUiThread{
                    onSuccess(secureUrl)
                }
            }

        })

      /*  fun saveUserInfoToFirestore(name: String, age: String, phone: String, imageUrl: String){
            val currentUser = auth.currentUser
            if (currentUser != null){
                val userInfo = hashMapOf(
                    "name"
                )
            }
        } */

    }

    interface OnProfileUpdatedListener { //ProfileActivity bu arayüzü uyguluyor
        fun onProfileUpdated()
        fun onFragmentClosed()
    }
}
