package org.nihongo.mochi.ui.dictionary

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.google.mlkit.vision.digitalink.Ink
import org.nihongo.mochi.databinding.DialogDrawingBinding

class DrawingDialogFragment : DialogFragment() {

    private var _binding: DialogDrawingBinding? = null
    private val binding get() = _binding!!

    // Callback to pass the result back to the calling fragment
    var onInkDrawn: ((Ink) -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogDrawingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonClear.setOnClickListener {
            binding.drawingView.clear()
        }

        binding.buttonRecognize.setOnClickListener {
            val ink = binding.drawingView.getInk()
            onInkDrawn?.invoke(ink)
            dismiss()
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
