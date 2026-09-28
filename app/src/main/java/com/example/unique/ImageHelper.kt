package com.example.unique

import android.widget.ImageView
import com.bumptech.glide.Glide

object ImageHelper {

    fun getDrawableForName(name: String?, category: String? = null): Int {
        val cleanName = name?.lowercase()?.trim() ?: ""
        val cleanCat = category?.lowercase()?.trim() ?: ""

        return when {
            // Dog breeds
            cleanName.contains("labrador") -> R.drawable.labrador_retriever
            cleanName.contains("husky") || cleanName.contains("siberian") -> R.drawable.siberian_husky
            cleanName.contains("golden") -> R.drawable.golden_retriever
            cleanName.contains("beagle") -> R.drawable.beagle
            cleanName.contains("german") || cleanName.contains("shepherd") -> R.drawable.german_shepherd

            // Cat breeds
            cleanName.contains("abyssinian") -> R.drawable.abyssinian
            cleanName.contains("ragdoll") -> R.drawable.ragdoll
            cleanName.contains("russian") || cleanName.contains("blue") -> R.drawable.russian_blue
            cleanName.contains("bengal") -> R.drawable.bengal_cat
            cleanName.contains("persian") -> R.drawable.persian_cat
            cleanName.contains("siamese") -> R.drawable.siamese_cat
            cleanName.contains("maine") || cleanName.contains("coon") -> R.drawable.maine_coon
            cleanName.contains("british") || cleanName.contains("shorthair") -> R.drawable.british_shorthair

            // Fish breeds
            cleanName.contains("goldfish") || cleanName.contains("gold fish") -> R.drawable.goldfish
            cleanName.contains("koi") -> R.drawable.koi_fish
            cleanName.contains("guppy") || cleanName.contains("guppi") -> R.drawable.guppy_fish
            cleanName.contains("oscar") -> R.drawable.oscar_fish

            // Accessories items
            cleanName.contains("collar") -> R.drawable.pet_collar
            cleanName.contains("leash") || cleanName.contains("lead") -> R.drawable.pet_leash
            cleanName.contains("food") || cleanName.contains("treat") || cleanName.contains("kibble") -> R.drawable.pet_food
            cleanName.contains("toy") || cleanName.contains("ball") || cleanName.contains("chew") -> R.drawable.pet_toy
            cleanName.contains("shampoo") || cleanName.contains("wash") || cleanName.contains("groom") -> R.drawable.pet_shampoo
            cleanName.contains("bowl") || cleanName.contains("feeder") || cleanName.contains("water") -> R.drawable.pet_bowl
            cleanName.contains("bed") || cleanName.contains("mat") || cleanName.contains("cushion") || cleanName.contains("blanket") -> R.drawable.pet_bed
            cleanName.contains("cage") || cleanName.contains("crate") || cleanName.contains("kennel") || cleanName.contains("carrier") -> R.drawable.pet_cage

            // Adopt Pet breeds (non-dog, non-cat)
            cleanName.contains("rabbit") || cleanName.contains("bunny") -> R.drawable.rabbit
            cleanName.contains("parrot") || cleanName.contains("bird") || cleanName.contains("cockatiel") || cleanName.contains("macaw") -> R.drawable.parrot
            cleanName.contains("hamster") || cleanName.contains("guinea") || cleanName.contains("gerbil") -> R.drawable.hamster
            cleanName.contains("turtle") || cleanName.contains("tortoise") -> R.drawable.turtle

            // Generic Categories
            cleanName.contains("dog") || cleanCat.contains("dog") -> R.drawable.dogs
            cleanName.contains("cat") || cleanCat.contains("cat") -> R.drawable.cats
            cleanName.contains("fish") || cleanCat.contains("fish") -> R.drawable.fish
            cleanCat.contains("access") -> R.drawable.accessories
            else -> when {
                cleanCat.contains("dog") -> R.drawable.dogs
                cleanCat.contains("cat") -> R.drawable.cats
                cleanCat.contains("fish") -> R.drawable.fish
                cleanCat.contains("access") -> R.drawable.accessories
                cleanCat.contains("adopt") -> R.drawable.adopt_pet
                else -> R.drawable.adopt_pet
            }
        }
    }

    fun loadImage(imageView: ImageView, imageUrl: String?, name: String?, category: String? = null) {
        val fallbackDrawable = getDrawableForName(name, category)
        val validUrl = imageUrl?.trim()

        if (!validUrl.isNullOrEmpty() && (validUrl.startsWith("http://") || validUrl.startsWith("https://") || validUrl.startsWith("content://") || validUrl.startsWith("file://"))) {
            Glide.with(imageView.context)
                .load(validUrl)
                .placeholder(fallbackDrawable)
                .error(fallbackDrawable)
                .into(imageView)
        } else {
            imageView.setImageResource(fallbackDrawable)
        }
    }
}
