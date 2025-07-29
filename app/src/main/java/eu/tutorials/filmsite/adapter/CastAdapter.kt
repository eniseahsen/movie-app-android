package eu.tutorials.filmsite.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import eu.tutorials.filmsite.R
import eu.tutorials.filmsite.model.CastMember

//RecyclerView'a oyuncu verilerini bağlamak için kullanılır
class CastAdapter(
    private val castList: List<CastMember>,
    private val onItemClick: (CastMember) -> Unit ) :
    RecyclerView.Adapter<CastAdapter.CastViewHolder>() {

        //CastViewHolder sayesinde her eleman için findViewById işlemi tekrar tekrar yapılmaz
    inner class CastViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val castImage: ImageView = itemView.findViewById(R.id.castImage)
        val castName: TextView = itemView.findViewById(R.id.castName)




            fun bind(castMember: CastMember){
            castName.text = castMember.name




            castImage.setOnClickListener{
                onItemClick(castMember)
            }
        }
    }

    //UI oluşturur veri bağlamaz
    //RecyclerView'ın bir elemanı için arayüzü oluşturur
    //item_cast.xml dosyasını inflate eder
    //yeni bir CastViewHolder nesnesi döner
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CastViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cast, parent, false)
        return CastViewHolder(view)
    }

    //ViewHolder içindeki görünümlere(TextView, ImageView)lara veriyi bağlar
    override fun onBindViewHolder(holder: CastViewHolder, position: Int) {
        val member = castList[position]
        holder.bind(member) //member nesnesi ViewHolder içindeki TextView ve ImageView gibi bileşenlere aktarılır

        val imageUrl = member.profile_path?.let {
            "https://image.tmdb.org/t/p/w500$it" //it: profile_path
        }

        Glide.with(holder.itemView.context)
            .load(imageUrl)
            .circleCrop()
            .placeholder(R.drawable.logo) //yükleme sırasında geçici bir resim
            .into(holder.castImage)
    }

    override fun getItemCount(): Int = castList.size //RecyclerView kaç öge çizecek onu belirler
}
