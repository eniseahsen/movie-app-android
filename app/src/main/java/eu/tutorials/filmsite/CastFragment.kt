package eu.tutorials.filmsite

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import eu.tutorials.filmsite.model.CastMember

class CastFragment : DialogFragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_cast, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //arguments: fragmenta gelen verileri içeren Bundle
         val castMember = arguments?.getSerializable("cast") as? CastMember

        val imageView = view.findViewById<ImageView>(R.id.castImageView2)
        val nameView = view.findViewById<TextView>(R.id.castName2)
        val textView = view.findViewById<TextView>(R.id.castCharacter2)


        castMember?.let {
            nameView.text = it.name
            textView.text=it.character

            Glide.with(this)
                .load("https://image.tmdb.org/t/p/w500${it.profile_path}")
                .into(imageView)
        }



    }



}