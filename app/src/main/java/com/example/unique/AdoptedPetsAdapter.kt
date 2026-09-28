package com.example.unique

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.unique.Product
import com.example.unique.R
import com.example.unique.ImageHelper

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
        private val petPriceTextView: TextView = itemView.findViewById(R.id.petPriceTextView)
        private val adoptNowButton: Button = itemView.findViewById(R.id.buttonAdoptNow)

        fun bind(pet: Product) {
            petNameTextView.text = pet.name
            petLocationTextView.text = "📍 ${pet.category}"

            // Show adoption fee — price = 0 means free adoption
            if (pet.price > 0) {
                petPriceTextView.text = "Adoption Fee: ₹${pet.price.toInt()}"
            } else {
                petPriceTextView.text = "Free Adoption 🐾"
            }

            // Load image via ImageHelper (Firebase URL or local drawable fallback)
            ImageHelper.loadImage(petImageView, pet.imageUrl, pet.name, "adopt")

            adoptNowButton.setOnClickListener {
                Toast.makeText(
                    itemView.context,
                    "Contact the owner to adopt ${pet.name}!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}
