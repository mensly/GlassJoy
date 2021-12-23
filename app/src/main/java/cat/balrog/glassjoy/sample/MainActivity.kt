package cat.balrog.glassjoy.sample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import cat.balrog.glassjoy.InputGesture
import cat.balrog.glassjoy.sample.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.input.inputs.asLiveData().observe(this) {
            if (it == null) return@observe
            binding.text.text = listOf(it, binding.text.text).joinToString("\n")
        }
    }
}