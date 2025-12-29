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
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.google.android.gms.games.SnapshotsClient
import com.google.android.gms.games.snapshot.SnapshotMetadata
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.launch
import org.koin.android.ext.android.get
import org.koin.android.ext.android.inject
import org.nihongo.mochi.R
import org.nihongo.mochi.databinding.FragmentResultsBinding
import org.nihongo.mochi.domain.statistics.LevelProgress
import org.nihongo.mochi.domain.statistics.ResultsViewModel
import org.nihongo.mochi.domain.statistics.StatisticsEngine
import org.nihongo.mochi.domain.statistics.StatisticsType
import org.nihongo.mochi.domain.util.LevelContentProvider
import org.nihongo.mochi.services.AndroidCloudSaveService

class ResultsFragment : Fragment() {

    private var _binding: FragmentResultsBinding? = null
    private val binding get() = _binding!!
    
    private val levelContentProvider: LevelContentProvider by inject()

    private lateinit var statisticsEngine: StatisticsEngine
    private lateinit var androidCloudSaveService: AndroidCloudSaveService

    private val viewModel: ResultsViewModel by viewModels {
        viewModelFactory {
            initializer<ResultsViewModel> {
                // Initialize dependencies for the ViewModel
                val saveService = AndroidCloudSaveService(requireActivity())
                // We use get() here to retrieve LevelContentProvider from Koin context
                val statsEngine = StatisticsEngine(get())
                ResultsViewModel(saveService, statsEngine)
            }
        }
    }

    private val achievementsLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            Log.d("ResultsFragment", "Achievements activity returned OK.")
        }
    }

    private val savedGamesLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val intent = result.data!!
            if (intent.hasExtra(SnapshotsClient.EXTRA_SNAPSHOT_METADATA)) {
                val snapshotMetadata = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra(SnapshotsClient.EXTRA_SNAPSHOT_METADATA, SnapshotMetadata::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    intent.getParcelableExtra(SnapshotsClient.EXTRA_SNAPSHOT_METADATA)
                }
                
                if (snapshotMetadata != null) {
                    viewModel.setCurrentSaveName(snapshotMetadata.uniqueName)
                    lifecycleScope.launch {
                        val data = androidCloudSaveService.loadGame(snapshotMetadata.uniqueName)
                        if (data != null) {
                            viewModel.loadGame(data)
                            updateAllPercentages()
                        }
                    }
                }
            } else if (intent.hasExtra(SnapshotsClient.EXTRA_SNAPSHOT_NEW)) {
                val unique = java.math.BigInteger(281, java.util.Random()).toString(13)
                viewModel.setCurrentSaveName("NihongoMochiSnapshot-$unique")
                viewModel.saveGame()
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
        
        // Init Fragment properties if needed (e.g. for updateAllPercentages called outside ViewModel observation)
        if (!::statisticsEngine.isInitialized) {
             statisticsEngine = StatisticsEngine(levelContentProvider)
        }
        if (!::androidCloudSaveService.isInitialized) {
            androidCloudSaveService = AndroidCloudSaveService(requireActivity())
        }

        updateAllPercentages()

        binding.buttonSignIn.setOnClickListener { viewModel.signIn() }
        binding.buttonAchievements.setOnClickListener { showAchievements() }
        binding.buttonBackup.setOnClickListener { showSavedGamesUI() }
        binding.buttonRestore.setOnClickListener { showSavedGamesUI() }
        
        setupObservers()
    }
    
    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.isAuthenticated.collect { isAuthenticated ->
                        updateSignInUI(isAuthenticated)
                    }
                }
                launch {
                    viewModel.message.collect { msg ->
                        if (msg != null) {
                            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                            viewModel.clearMessage()
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.checkSignInStatus()
    }

    private fun updateSignInUI(isSignedIn: Boolean) {
        val visibility = if (isSignedIn) View.VISIBLE else View.GONE
        val inverseVisibility = if (isSignedIn) View.GONE else View.VISIBLE
        
        binding.buttonSignIn.visibility = inverseVisibility
        binding.buttonAchievements.visibility = visibility
        binding.buttonBackup.visibility = visibility
        binding.buttonRestore.visibility = visibility
    }

    private fun showAchievements() {
        lifecycleScope.launch {
             try {
                 val intent = androidCloudSaveService.getAchievementsIntent()
                 achievementsLauncher.launch(intent)
             } catch (e: Exception) {
                 Toast.makeText(requireContext(), getString(R.string.about_coming_soon) + ": " + e.message, Toast.LENGTH_SHORT).show()
             }
        }
    }

    private fun showSavedGamesUI() {
        lifecycleScope.launch {
            try {
                val intent = androidCloudSaveService.getSavedGamesIntent(
                    "Sauvegardes", 
                    allowAdd = true, 
                    allowDelete = true, 
                    maxSnapshots = 5
                )
                savedGamesLauncher.launch(intent)
            } catch (e: Exception) {
                 Toast.makeText(requireContext(), getString(R.string.about_coming_soon) + ": " + e.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateAllPercentages() {
        // Ensure engine is initialized
        if (!::statisticsEngine.isInitialized) {
             statisticsEngine = StatisticsEngine(levelContentProvider)
        }

        val allStats = statisticsEngine.getAllStatistics()
        
        // Group by StatisticsType (Recognition, Reading, Writing...)
        val groupedStats = allStats.groupBy { it.type }
        
        binding.dynamicContentContainer.removeAllViews()

        // Order of types matters
        val orderedTypes = listOf(StatisticsType.RECOGNITION, StatisticsType.READING, StatisticsType.WRITING)
        // Add any future types that might not be in the ordered list yet
        val otherTypes = groupedStats.keys.filter { it !in orderedTypes }
        
        (orderedTypes + otherTypes).forEach { type ->
            val statsForType = groupedStats[type] ?: return@forEach
            createMainSection(type, statsForType)
        }
    }

    private fun createMainSection(type: StatisticsType, stats: List<LevelProgress>) {
        val context = requireContext()
        val card = MaterialCardView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 16.dpToPx())
            }
            radius = 8.dpToPx().toFloat()
            cardElevation = 4.dpToPx().toFloat()
            setCardBackgroundColor(ContextCompat.getColor(context, R.color.card_background))
        }

        val mainContainer = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16.dpToPx(), 16.dpToPx(), 16.dpToPx(), 16.dpToPx())
        }

        // --- Main Header ---
        val headerLayout = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER_VERTICAL
            isClickable = true
            isFocusable = true
            val outValue = android.util.TypedValue()
            context.theme.resolveAttribute(android.R.attr.selectableItemBackground, outValue, true)
            setBackgroundResource(outValue.resourceId)
        }

        val titleResId = when(type) {
            StatisticsType.RECOGNITION -> R.string.results_recognition_title
            StatisticsType.READING -> R.string.results_reading_title
            StatisticsType.WRITING -> R.string.results_writing_title
            // Handle future types generically or add resources
        }
        
        val titleText = if (titleResId != 0) getString(titleResId) else type.name.lowercase().replaceFirstChar { it.uppercase() }

        val titleView = TextView(context).apply {
            text = titleText
            textSize = 20f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(ContextCompat.getColor(context, R.color.card_text))
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        val arrowView = ImageView(context).apply {
            setImageResource(android.R.drawable.arrow_down_float)
            setColorFilter(resolveThemeAttr(android.R.attr.textColorPrimary))
        }

        headerLayout.addView(titleView)
        headerLayout.addView(arrowView)
        mainContainer.addView(headerLayout)

        // --- Content Container ---
        val contentContainer = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            visibility = View.VISIBLE
        }

        // Group by Category (JLPT, School, etc.)
        val groupedByCategory = stats.groupBy { it.category }
        
        // Define category order if needed, otherwise rely on insertion/alphabetical
        // We can sort categories based on the lowest sortOrder of their items
        val sortedCategories = groupedByCategory.keys.sortedBy { category ->
             groupedByCategory[category]?.minOfOrNull { it.sortOrder } ?: Int.MAX_VALUE
        }

        sortedCategories.forEach { category ->
            val categoryStats = groupedByCategory[category] ?: return@forEach
            // Sort items within category
            val sortedStats = categoryStats.sortedBy { it.sortOrder }

            if (category.isNotEmpty()) {
                createSubSection(context, contentContainer, category, sortedStats)
            } else {
                // Items without category (direct children)
                sortedStats.forEach { stat ->
                    createStatItem(context, contentContainer, stat)
                }
            }
        }
        
        mainContainer.addView(contentContainer)
        card.addView(mainContainer)
        binding.dynamicContentContainer.addView(card)

        // Setup collapse logic
        val prefKey = "expanded_${type.name}"
        setupCollapsibleSection(headerLayout, arrowView, contentContainer, prefKey)
    }

    private fun createSubSection(
        context: Context,
        parent: ViewGroup,
        categoryName: String,
        stats: List<LevelProgress>
    ) {
        // --- Sub Header ---
        val headerLayout = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = 16.dpToPx()
            }
            isClickable = true
            isFocusable = true
            val outValue = android.util.TypedValue()
            context.theme.resolveAttribute(android.R.attr.selectableItemBackground, outValue, true)
            setBackgroundResource(outValue.resourceId)
        }

        // Map category internal names to resources if possible
        val categoryDisplay = when(categoryName) {
            "Kanas" -> getString(R.string.results_section_kanas)
            "JLPT" -> getString(R.string.results_section_jlpt)
            "School" -> getString(R.string.results_section_school)
            "Frequency" -> getString(R.string.results_section_frequency)
            "Challenges" -> "Challenges" // TODO: Add localized string for Challenges if needed
            // Fallback
            else -> categoryName
        }

        val titleView = TextView(context).apply {
            text = categoryDisplay
            textSize = 18f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(ContextCompat.getColor(context, R.color.card_text))
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        val arrowView = ImageView(context).apply {
            setImageResource(android.R.drawable.arrow_down_float)
            setColorFilter(resolveThemeAttr(android.R.attr.textColorPrimary))
        }

        headerLayout.addView(titleView)
        headerLayout.addView(arrowView)
        parent.addView(headerLayout)

        // --- Sub Content ---
        val contentContainer = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            visibility = View.VISIBLE
        }

        stats.forEach { stat ->
            createStatItem(context, contentContainer, stat)
        }

        parent.addView(contentContainer)
        
        // Setup collapse logic
        // Use a unique key based on category and type if possible, but category names are unique enough per main section usually
        val prefKey = "expanded_sub_${categoryName}_${stats.firstOrNull()?.type?.name ?: ""}"
        setupCollapsibleSection(headerLayout, arrowView, contentContainer, prefKey)
    }

    private fun createStatItem(context: Context, parent: ViewGroup, stat: LevelProgress) {
        val titleView = TextView(context).apply {
            // We might need to resolve the dynamic title more nicely
            // For now, we rely on the title from StatisticsEngine, which already has localized-like strings
            // But ideally, we should pass resource IDs or do mapping here if the engine returns keys.
            // The current engine returns display strings like "JLPT N5" or localized strings if we updated it.
            // Since the previous implementation used a huge WHEN to map to string resources, 
            // we should ideally try to map back or have the engine return string keys.
            // For this iteration, we'll try to use the title from the object, 
            // but we can improve localization by using xmlName or a new field.
            
            // Temporary fix to match previous string resources if possible based on xmlName/title pattern
            text = getLocalizedTitle(stat)
            
            textSize = 16f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(ContextCompat.getColor(context, R.color.card_text))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, 
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = 8.dpToPx()
            }
        }

        val progressBar = ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = 4.dpToPx()
            }
            max = 100
            progress = stat.percentage
        }

        parent.addView(titleView)
        parent.addView(progressBar)
    }

    private fun setupCollapsibleSection(header: View, arrow: ImageView, container: ViewGroup, preferenceKey: String) {
        val sharedPreferences = requireActivity().getPreferences(Context.MODE_PRIVATE)
        val isExpanded = sharedPreferences.getBoolean(preferenceKey, true)

        container.visibility = if (isExpanded) View.VISIBLE else View.GONE
        arrow.rotation = if (isExpanded) 0f else -90f

        header.setOnClickListener {
            val newExpandedState = container.isGone
            container.visibility = if (newExpandedState) View.VISIBLE else View.GONE
            arrow.animate().rotation(if (newExpandedState) 0f else -90f).start()

            with(sharedPreferences.edit()) {
                putBoolean(preferenceKey, newExpandedState)
                apply()
            }
        }
    }

    private fun getLocalizedTitle(stat: LevelProgress): String {
        // This is a helper to map static names from Engine to localized strings
        // Ideally Engine should provide resource IDs or KMP equivalent
        return when (stat.xmlName) {
            "Hiragana" -> getString(R.string.results_hiragana, stat.percentage)
            "Katakana" -> getString(R.string.results_katakana, stat.percentage)
            "N5" -> getString(R.string.results_jlpt_n5, stat.percentage)
            "N4" -> getString(R.string.results_jlpt_n4, stat.percentage)
            "N3" -> getString(R.string.results_jlpt_n3, stat.percentage)
            "N2" -> getString(R.string.results_jlpt_n2, stat.percentage)
            "N1" -> getString(R.string.results_jlpt_n1, stat.percentage)
            "Grade 1" -> getString(R.string.results_grade_1, stat.percentage)
            "Grade 2" -> getString(R.string.results_grade_2, stat.percentage)
            "Grade 3" -> getString(R.string.results_grade_3, stat.percentage)
            "Grade 4" -> getString(R.string.results_grade_4, stat.percentage)
            "Grade 5" -> getString(R.string.results_grade_5, stat.percentage)
            "Grade 6" -> getString(R.string.results_grade_6, stat.percentage)
            "Grade 7" -> getString(R.string.results_college, stat.percentage)
            "Grade 8" -> getString(R.string.results_high_school, stat.percentage)
            "user_list" -> getString(R.string.reading_user_list) + " - ${stat.percentage}%"
            "reading_n5" -> getString(R.string.results_jlpt_n5, stat.percentage)
            "reading_n4" -> getString(R.string.results_jlpt_n4, stat.percentage)
            "reading_n3" -> getString(R.string.results_jlpt_n3, stat.percentage)
            "reading_n2" -> getString(R.string.results_jlpt_n2, stat.percentage)
            "reading_n1" -> getString(R.string.results_jlpt_n1, stat.percentage)
            // Frequency lists pattern
            else -> {
                if (stat.xmlName.startsWith("bccwj_wordlist_")) {
                    val number = stat.xmlName.removePrefix("bccwj_wordlist_")
                    getString(R.string.results_frequency_x_words, number.toIntOrNull() ?: 0) + " - ${stat.percentage}%"
                } else {
                    "${stat.title} - ${stat.percentage}%"
                }
            }
        }
    }

    private fun Int.dpToPx(): Int {
        val density = resources.displayMetrics.density
        return (this * density).toInt()
    }

    private fun resolveThemeAttr(attr: Int): Int {
        val typedValue = android.util.TypedValue()
        requireContext().theme.resolveAttribute(attr, typedValue, true)
        return if (typedValue.resourceId != 0) {
            ContextCompat.getColor(requireContext(), typedValue.resourceId)
        } else {
            typedValue.data
        }
    }
}
