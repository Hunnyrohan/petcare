import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.unique.databinding.FragmentAdoptBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.*

@Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")
class AdoptFragment : Fragment() {

    private var _binding: FragmentAdoptBinding? = null
    private val binding get() = _binding!!
    private var selectedImageUri: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdoptBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonSelectImage.setOnClickListener {
            selectImage()
        }

        binding.buttonSubmitAdopt.setOnClickListener {
            submitPetDetails()
        }
    }

    private fun selectImage() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(intent, 1001)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1001 && resultCode == Activity.RESULT_OK) {
            selectedImageUri = data?.data
            binding.imageViewPet.setImageURI(selectedImageUri)
        }
    }

    private fun submitPetDetails() {
        val petName = binding.editTextPetName.text.toString().trim()
        val petColor = binding.editTextPetColor.text.toString().trim()
        val petLocation = binding.editTextPetLocation.text.toString().trim()

        if (petName.isNotEmpty() && petColor.isNotEmpty() && petLocation.isNotEmpty() && selectedImageUri != null) {
            uploadPetImage(petName, petColor, petLocation)
        } else {
            Toast.makeText(context, "Please fill out all fields and select an image.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun uploadPetImage(petName: String, petColor: String, petLocation: String) {
        val storageReference = FirebaseStorage.getInstance().reference
        val imageRef = storageReference.child("adopt_pets/${UUID.randomUUID()}.jpg")
        selectedImageUri?.let { uri ->
            imageRef.putFile(uri).addOnSuccessListener {
                imageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                    savePetDetails(petName, petColor, petLocation, downloadUrl.toString())
                }
            }.addOnFailureListener {
                Toast.makeText(context, "Failed to upload image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun savePetDetails(petName: String, petColor: String, petLocation: String, imageUrl: String) {
        val db = FirebaseFirestore.getInstance()
        val petData = hashMapOf(
            "name" to petName,
            "color" to petColor,
            "location" to petLocation,
            "imageUrl" to imageUrl
        )

        db.collection("adoptedPets").add(petData)
            .addOnSuccessListener {
                Toast.makeText(context, "Pet listed successfully!", Toast.LENGTH_SHORT).show()
                clearForm()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to list pet", Toast.LENGTH_SHORT).show()
            }
    }

    private fun clearForm() {
        binding.editTextPetName.text.clear()
        binding.editTextPetColor.text.clear()
        binding.editTextPetLocation.text.clear()
        binding.imageViewPet.setImageResource(0)
        selectedImageUri = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
