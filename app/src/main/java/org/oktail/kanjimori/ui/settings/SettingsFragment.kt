package org.oktail.kanjimori.ui.settings

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.fragment.app.Fragment
import org.oktail.kanjimori.R
import org.oktail.kanjimori.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private val languages = listOf(
        LanguageItem("fr_FR", "Français (France)", R.drawable.flag_fr_fr),
        LanguageItem("en_GB", "English (UK)", R.drawable.flag_en_gb),
        LanguageItem("pt_BR", "Português (Brasil)", R.drawable.flag_pt_br)
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = LanguageAdapter(requireContext(), languages)
        binding.spinnerLanguage.adapter = adapter

        val currentLangCode = getCurrentAppLocale()
        val currentLangIndex = languages.indexOfFirst { it.code == currentLangCode }.takeIf { it != -1 } ?: 0
        binding.spinnerLanguage.setSelection(currentLangIndex, false)

        binding.spinnerLanguage.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedLanguage = languages[position]

                if (selectedLanguage.code != getCurrentAppLocale()) {
                    setAppLocale(selectedLanguage.code)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun getCurrentAppLocale(): String {
        val sharedPreferences = requireActivity().getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        return sharedPreferences.getString("AppLocale", "fr_FR")!!
    }

    private fun setAppLocale(localeCode: String) {
        val sharedPreferences = requireActivity().getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        sharedPreferences.edit().putString("AppLocale", localeCode).apply()

        val localeTag = localeCode.replace('_', '-')
        val appLocale = LocaleListCompat.forLanguageTags(localeTag)
        AppCompatDelegate.setApplicationLocales(appLocale)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}