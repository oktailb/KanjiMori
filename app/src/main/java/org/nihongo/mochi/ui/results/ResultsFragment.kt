package org.nihongo.mochi.ui.results

import android.app.Activity
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.google.android.gms.games.AchievementsClient
import com.google.android.gms.games.GamesSignInClient
import com.google.android.gms.games.PlayGames
import com.google.android.gms.games.SnapshotsClient
import com.google.android.gms.games.snapshot.SnapshotMetadata
import com.google.android.gms.games.snapshot.SnapshotMetadataChange
import org.nihongo.mochi.MochiApplication
import org.nihongo.mochi.R
import org.nihongo.mochi.data.ScoreManager
import org.nihongo.mochi.databinding.FragmentResultsBinding
import org.nihongo.mochi.domain.statistics.LevelProgress
import org.nihongo.mochi.domain.statistics.StatisticsEngine
import java.io.IOException

class ResultsFragment : Fragment() {

    private var _binding: FragmentResultsBinding? = null
    private val binding get() = _binding!!

    private lateinit var gamesSignInClient: GamesSignInClient
    private lateinit var achievementsClient: AchievementsClient
    private lateinit var snapshotsClient: SnapshotsClient
    
    private lateinit var statisticsEngine: StatisticsEngine

    private val RC_SAVED_GAMES = 9009
    private var mCurrentSaveName = "NihongoMochiSnapshot"

    private val achievementsLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            Log.d("ResultsFragment", "Achievements activity returned OK.")
        } else {
            Log.d("ResultsFragment", "Achievements activity returned with code: ${result.resultCode}")
        }
    }

    private val savedGamesLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val intent = result.data!!
            if (intent.hasExtra(SnapshotsClient.EXTRA_SNAPSHOT_METADATA)) {
                // Load a snapshot.
                val snapshotMetadata = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra(SnapshotsClient.EXTRA_SNAPSHOT_METADATA, SnapshotMetadata::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    intent.getParcelableExtra(SnapshotsClient.EXTRA_SNAPSHOT_METADATA)
                }
                
                if (snapshotMetadata != null) {
                    mCurrentSaveName = snapshotMetadata.uniqueName
                    loadSnapshot()
                }
            } else if (intent.hasExtra(SnapshotsClient.EXTRA_SNAPSHOT_NEW)) {
                // Create a new snapshot named with a unique string
                val unique = java.math.BigInteger(281, java.util.Random()).toString(13)
                mCurrentSaveName = "NihongoMochiSnapshot-$unique"
                saveSnapshot()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentResultsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        gamesSignInClient = PlayGames.getGamesSignInClient(requireActivity())
        achievementsClient = PlayGames.getAchievementsClient(requireActivity())
        snapshotsClient = PlayGames.getSnapshotsClient(requireActivity())
        
        statisticsEngine = StatisticsEngine(
            MochiApplication.kanaRepository,
            MochiApplication.kanjiRepository,
            MochiApplication.wordRepository
        )

        setupCollapsibleSections()
        updateAllPercentages()

        binding.buttonSignIn.setOnClickListener { signInManually() }
        binding.buttonAchievements.setOnClickListener { showAchievements() }
        binding.buttonBackup.setOnClickListener { showSavedGamesUI() }
        binding.buttonRestore.setOnClickListener { showSavedGamesUI() }
    }

    override fun onResume() {
        super.onResume()
        checkSignInStatus()
    }

    private fun checkSignInStatus() {
        gamesSignInClient.isAuthenticated.addOnCompleteListener { isAuthenticatedTask ->
            val isAuthenticated = isAuthenticatedTask.isSuccessful && isAuthenticatedTask.result.isAuthenticated
            updateSignInUI(isAuthenticated)
        }
    }

    private fun updateSignInUI(isSignedIn: Boolean) {
        if (isSignedIn) {
            binding.buttonSignIn.visibility = View.GONE
            binding.buttonAchievements.visibility = View.VISIBLE
            binding.buttonBackup.visibility = View.VISIBLE
            binding.buttonRestore.visibility = View.VISIBLE
        } else {
            binding.buttonSignIn.visibility = View.VISIBLE
            binding.buttonAchievements.visibility = View.GONE
            binding.buttonBackup.visibility = View.GONE
            binding.buttonRestore.visibility = View.GONE
        }
    }

    private fun signInManually() {
        gamesSignInClient.signIn().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val result = task.result
                if (result.isAuthenticated) {
                    Log.d("ResultsFragment", "Manual sign-in successful")
                    updateSignInUI(true)
                } else {
                    Log.d("ResultsFragment", "Manual sign-in returned, but not authenticated")
                    Toast.makeText(requireContext(), "Connexion annulée ou non aboutie", Toast.LENGTH_SHORT).show()
                    updateSignInUI(false)
                }
            } else {
                Log.e("ResultsFragment", "Manual sign-in failed", task.exception)
                Toast.makeText(requireContext(), "Erreur de connexion : ${task.exception?.localizedMessage}", Toast.LENGTH_LONG).show()
                updateSignInUI(false)
            }
        }
    }

    private fun showAchievements() {
        achievementsClient.achievementsIntent.addOnSuccessListener { intent ->
            achievementsLauncher.launch(intent)
        }
    }

    private fun showSavedGamesUI() {
        val maxNumberOfSavedGamesToShow = 5
        snapshotsClient.getSelectSnapshotIntent("Sauvegardes", true, true, maxNumberOfSavedGamesToShow)
            .addOnSuccessListener { intent ->
                savedGamesLauncher.launch(intent)
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Impossible d'ouvrir l'interface de sauvegarde: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveSnapshot() {
        val data = ScoreManager.getAllDataJson().toByteArray()
        val desc = "Backup " + java.text.SimpleDateFormat.getDateTimeInstance().format(java.util.Date())

        snapshotsClient.open(mCurrentSaveName, true, SnapshotsClient.RESOLUTION_POLICY_MOST_RECENTLY_MODIFIED)
            .addOnFailureListener { e ->
                 Log.e("ResultsFragment", "Error opening snapshot", e)
                 Toast.makeText(requireContext(), "Erreur lors de l'ouverture de la sauvegarde", Toast.LENGTH_SHORT).show()
            }
            .continueWithTask { task ->
                val snapshot = task.result.data!!
                snapshot.snapshotContents.writeBytes(data)

                val metadataChange = SnapshotMetadataChange.Builder()
                    .setDescription(desc)
                    .build()
                
                snapshotsClient.commitAndClose(snapshot, metadataChange)
            }
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(requireContext(), "Sauvegarde effectuée avec succès", Toast.LENGTH_SHORT).show()
                } else {
                    Log.e("ResultsFragment", "Error saving snapshot", task.exception)
                    Toast.makeText(requireContext(), "Erreur lors de la sauvegarde", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun loadSnapshot() {
         snapshotsClient.open(mCurrentSaveName, true, SnapshotsClient.RESOLUTION_POLICY_MOST_RECENTLY_MODIFIED)
            .addOnFailureListener { e ->
                Log.e("ResultsFragment", "Error opening snapshot", e)
                Toast.makeText(requireContext(), "Erreur lors de l'ouverture de la sauvegarde", Toast.LENGTH_SHORT).show()
            }
            .continueWith { task ->
                val snapshot = task.result.data!!
                try {
                    val data = snapshot.snapshotContents.readFully()
                    if (data != null) {
                        val jsonString = String(data)
                        ScoreManager.restoreDataFromJson(jsonString)
                    }
                } catch (e: IOException) {
                    Log.e("ResultsFragment", "Error reading snapshot", e)
                }
            }
            .addOnCompleteListener { task ->
                 if (task.isSuccessful) {
                     updateAllPercentages()
                     Toast.makeText(requireContext(), "Données restaurées avec succès", Toast.LENGTH_SHORT).show()
                 } else {
                     Log.e("ResultsFragment", "Error loading snapshot", task.exception)
                     Toast.makeText(requireContext(), "Erreur lors de la restauration", Toast.LENGTH_SHORT).show()
                 }
            }
    }

    private fun setupCollapsibleSections() {
        // Main Sections
        setupCollapsibleSection(binding.headerRecognitionMain, binding.arrowRecognitionMain, binding.containerRecognitionMain, "recognition_main_expanded")
        setupCollapsibleSection(binding.headerReadingMain, binding.arrowReadingMain, binding.containerReadingMain, "reading_main_expanded")
        setupCollapsibleSection(binding.headerWritingMain, binding.arrowWritingMain, binding.containerWritingMain, "writing_main_expanded")

        // Recognition Sub-sections
        setupCollapsibleSection(binding.headerRecognitionKanas, binding.arrowRecognitionKanas, binding.containerRecognitionKanas, "recognition_kanas_expanded")
        setupCollapsibleSection(binding.headerRecognitionJlpt, binding.arrowRecognitionJlpt, binding.containerRecognitionJlpt, "recognition_jlpt_expanded")
        setupCollapsibleSection(binding.headerRecognitionSchool, binding.arrowRecognitionSchool, binding.containerRecognitionSchool, "recognition_school_expanded")

        // Reading Sub-sections
        setupCollapsibleSection(binding.headerReadingJlpt, binding.arrowReadingJlpt, binding.containerReadingJlpt, "reading_jlpt_expanded")
        setupCollapsibleSection(binding.headerReadingFreq, binding.arrowReadingFreq, binding.containerReadingFreq, "reading_freq_expanded")

        // Writing Sub-sections
        setupCollapsibleSection(binding.headerWritingJlpt, binding.arrowWritingJlpt, binding.containerWritingJlpt, "writing_jlpt_expanded")
        setupCollapsibleSection(binding.headerWritingSchool, binding.arrowWritingSchool, binding.containerWritingSchool, "writing_school_expanded")
    }

    private fun setupCollapsibleSection(header: View, arrow: ImageView, container: ViewGroup, preferenceKey: String) {
        val sharedPreferences = requireActivity().getPreferences(Context.MODE_PRIVATE)
        val isExpanded = sharedPreferences.getBoolean(preferenceKey, true)

        container.visibility = if (isExpanded) View.VISIBLE else View.GONE
        arrow.rotation = if (isExpanded) 0f else -90f

        header.setOnClickListener {
            val newExpandedState = container.visibility == View.GONE
            container.visibility = if (newExpandedState) View.VISIBLE else View.GONE
            arrow.animate().rotation(if (newExpandedState) 0f else -90f).start()

            with(sharedPreferences.edit()) {
                putBoolean(preferenceKey, newExpandedState)
                apply()
            }
        }
    }

    private fun updateAllPercentages() {
        val allStats = statisticsEngine.getAllStatistics()
        
        for (stat in allStats) {
            updateCategoryUI(stat)
        }
    }
    
    private fun updateCategoryUI(info: LevelProgress) {
        val percentageInt = info.percentage
        when (info.title) {
            "Hiragana" -> {
                binding.progressRecognitionHiragana.progress = percentageInt
                binding.titleRecognitionHiragana.text = getString(R.string.results_hiragana, percentageInt)
            }
            "Katakana" -> {
                binding.progressRecognitionKatakana.progress = percentageInt
                binding.titleRecognitionKatakana.text = getString(R.string.results_katakana, percentageInt)
            }
            "JLPT N5" -> {
                binding.progressRecognitionN5.progress = percentageInt
                binding.titleRecognitionN5.text = getString(R.string.results_jlpt_n5, percentageInt)
            }
            "JLPT N4" -> {
                binding.progressRecognitionN4.progress = percentageInt
                binding.titleRecognitionN4.text = getString(R.string.results_jlpt_n4, percentageInt)
            }
            "JLPT N3" -> {
                binding.progressRecognitionN3.progress = percentageInt
                binding.titleRecognitionN3.text = getString(R.string.results_jlpt_n3, percentageInt)
            }
            "JLPT N2" -> {
                binding.progressRecognitionN2.progress = percentageInt
                binding.titleRecognitionN2.text = getString(R.string.results_jlpt_n2, percentageInt)
            }
            "JLPT N1" -> {
                binding.progressRecognitionN1.progress = percentageInt
                binding.titleRecognitionN1.text = getString(R.string.results_jlpt_n1, percentageInt)
            }
            "Grade 1" -> {
                binding.progressRecognitionGrade1.progress = percentageInt
                binding.titleRecognitionGrade1.text = getString(R.string.results_grade_1, percentageInt)
            }
            "Grade 2" -> {
                binding.progressRecognitionGrade2.progress = percentageInt
                binding.titleRecognitionGrade2.text = getString(R.string.results_grade_2, percentageInt)
            }
            "Grade 3" -> {
                binding.progressRecognitionGrade3.progress = percentageInt
                binding.titleRecognitionGrade3.text = getString(R.string.results_grade_3, percentageInt)
            }
            "Grade 4" -> {
                binding.progressRecognitionGrade4.progress = percentageInt
                binding.titleRecognitionGrade4.text = getString(R.string.results_grade_4, percentageInt)
            }
            "Grade 5" -> {
                binding.progressRecognitionGrade5.progress = percentageInt
                binding.titleRecognitionGrade5.text = getString(R.string.results_grade_5, percentageInt)
            }
            "Grade 6" -> {
                binding.progressRecognitionGrade6.progress = percentageInt
                binding.titleRecognitionGrade6.text = getString(R.string.results_grade_6, percentageInt)
            }
            "Collège" -> {
                binding.progressRecognitionCollege.progress = percentageInt
                binding.titleRecognitionCollege.text = getString(R.string.results_college, percentageInt)
            }
            "Lycée" -> {
                binding.progressRecognitionLycee.progress = percentageInt
                binding.titleRecognitionLycee.text = getString(R.string.results_high_school, percentageInt)
            }
            "Reading User" -> {
                binding.progressReadingUser.progress = percentageInt
                binding.titleReadingUser.text = getString(R.string.reading_user_list) + " - $percentageInt%"
            }
            "Reading N5" -> {
                binding.progressReadingN5.progress = percentageInt
                binding.titleReadingN5.text = getString(R.string.results_jlpt_n5, percentageInt)
            }
            "Reading N4" -> {
                binding.progressReadingN4.progress = percentageInt
                binding.titleReadingN4.text = getString(R.string.results_jlpt_n4, percentageInt)
            }
            "Reading N3" -> {
                binding.progressReadingN3.progress = percentageInt
                binding.titleReadingN3.text = getString(R.string.results_jlpt_n3, percentageInt)
            }
            "Reading N2" -> {
                binding.progressReadingN2.progress = percentageInt
                binding.titleReadingN2.text = getString(R.string.results_jlpt_n2, percentageInt)
            }
            "Reading N1" -> {
                binding.progressReadingN1.progress = percentageInt
                binding.titleReadingN1.text = getString(R.string.results_jlpt_n1, percentageInt)
            }
            "Reading 1000" -> {
                binding.progressReading1000.progress = percentageInt
                binding.titleReading1000.text = "1000 mots les plus fréquents - $percentageInt%"
            }
            "Reading 2000" -> {
                binding.progressReading2000.progress = percentageInt
                binding.titleReading2000.text = "2000 mots les plus fréquents - $percentageInt%"
            }
            "Reading 3000" -> {
                binding.progressReading3000.progress = percentageInt
                binding.titleReading3000.text = "3000 mots les plus fréquents - $percentageInt%"
            }
            "Reading 4000" -> {
                binding.progressReading4000.progress = percentageInt
                binding.titleReading4000.text = "4000 mots les plus fréquents - $percentageInt%"
            }
            "Reading 5000" -> {
                binding.progressReading5000.progress = percentageInt
                binding.titleReading5000.text = "5000 mots les plus fréquents - $percentageInt%"
            }
            "Reading 6000" -> {
                binding.progressReading6000.progress = percentageInt
                binding.titleReading6000.text = "6000 mots les plus fréquents - $percentageInt%"
            }
            "Reading 7000" -> {
                binding.progressReading7000.progress = percentageInt
                binding.titleReading7000.text = "7000 mots les plus fréquents - $percentageInt%"
            }
            "Reading 8000" -> {
                binding.progressReading8000.progress = percentageInt
                binding.titleReading8000.text = "8000 mots les plus fréquents - $percentageInt%"
            }
            "Writing User" -> {
                binding.progressWritingUser.progress = percentageInt
                binding.titleWritingUser.text = getString(R.string.writing_user_lists) + " - $percentageInt%"
            }
            "Writing JLPT N5" -> {
                binding.progressWritingN5.progress = percentageInt
                binding.titleWritingN5.text = getString(R.string.results_jlpt_n5, percentageInt)
            }
            "Writing JLPT N4" -> {
                binding.progressWritingN4.progress = percentageInt
                binding.titleWritingN4.text = getString(R.string.results_jlpt_n4, percentageInt)
            }
            "Writing JLPT N3" -> {
                binding.progressWritingN3.progress = percentageInt
                binding.titleWritingN3.text = getString(R.string.results_jlpt_n3, percentageInt)
            }
            "Writing JLPT N2" -> {
                binding.progressWritingN2.progress = percentageInt
                binding.titleWritingN2.text = getString(R.string.results_jlpt_n2, percentageInt)
            }
            "Writing JLPT N1" -> {
                binding.progressWritingN1.progress = percentageInt
                binding.titleWritingN1.text = getString(R.string.results_jlpt_n1, percentageInt)
            }
            "Writing Grade 1" -> {
                binding.progressWritingGrade1.progress = percentageInt
                binding.titleWritingGrade1.text = getString(R.string.results_grade_1, percentageInt)
            }
            "Writing Grade 2" -> {
                binding.progressWritingGrade2.progress = percentageInt
                binding.titleWritingGrade2.text = getString(R.string.results_grade_2, percentageInt)
            }
            "Writing Grade 3" -> {
                binding.progressWritingGrade3.progress = percentageInt
                binding.titleWritingGrade3.text = getString(R.string.results_grade_3, percentageInt)
            }
            "Writing Grade 4" -> {
                binding.progressWritingGrade4.progress = percentageInt
                binding.titleWritingGrade4.text = getString(R.string.results_grade_4, percentageInt)
            }
            "Writing Grade 5" -> {
                binding.progressWritingGrade5.progress = percentageInt
                binding.titleWritingGrade5.text = getString(R.string.results_grade_5, percentageInt)
            }
            "Writing Grade 6" -> {
                binding.progressWritingGrade6.progress = percentageInt
                binding.titleWritingGrade6.text = getString(R.string.results_grade_6, percentageInt)
            }
            "Writing Collège" -> {
                binding.progressWritingCollege.progress = percentageInt
                binding.titleWritingCollege.text = getString(R.string.results_college, percentageInt)
            }
            "Writing Lycée" -> {
                binding.progressWritingLycee.progress = percentageInt
                binding.titleWritingLycee.text = getString(R.string.results_high_school, percentageInt)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
