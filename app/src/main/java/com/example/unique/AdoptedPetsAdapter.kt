import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.unique.Product
import com.example.unique.R
import com.bumptech.glide.Glide

class AdoptedPetsAdapter(private var petsList: List<Product>) :
    RecyclerView.Adapter<AdoptedPetsAdapter.PetViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PetViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_adopted_pets, parent, false)
        return PetViewHolder(view)
    }

    override fun onBindViewHolder(holder: PetViewHolder, position: Int) {
        val pet = petsList[position]
        holder.bind(pet)
    }

    override fun getItemCount(): Int {
        return petsList.size
    }

    fun updatePets(newPetsList: List<Product>) {
        petsList = newPetsList
        notifyDataSetChanged()
    }

    inner class PetViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val petImageView: ImageView = itemView.findViewById(R.id.petImageView)
        private val petNameTextView: TextView = itemView.findViewById(R.id.petNameTextView)
        private val petLocationTextView: TextView = itemView.findViewById(R.id.petLocationTextView)

        fun bind(pet: Product) {
            petNameTextView.text = pet.name
            petLocationTextView.text = pet.category // Assuming category stores the location

            // Load the image using Glide
            Glide.with(itemView.context)
                .load(pet.imageUrl)
                .into(petImageView)
        }
    }
}
