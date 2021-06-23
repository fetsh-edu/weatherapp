package me.fetsh.geekbrains.weather.ui.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import me.fetsh.geekbrains.weather.R
import me.fetsh.geekbrains.weather.RemoteData
import me.fetsh.geekbrains.weather.databinding.HistoryFragmentBinding
import me.fetsh.geekbrains.weather.model.HistoryViewModel
import me.fetsh.geekbrains.weather.room.HistoryEntity
import me.fetsh.geekbrains.weather.ui.utils.showSnackBar

class HistoryFragment : Fragment() {

    private var _binding: HistoryFragmentBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HistoryViewModel by lazy { ViewModelProvider(this).get(HistoryViewModel::class.java) }
    private val adapter: HistoryAdapter by lazy { HistoryAdapter() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = HistoryFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.historyFragmentRecyclerview.adapter = adapter
        viewModel.historyLiveData.observe(viewLifecycleOwner, { renderData(it) })
        viewModel.getAllHistory()
    }

    private fun renderData(historyList: RemoteData<List<HistoryEntity>, Throwable>) {
        when (historyList) {
            is RemoteData.Failure -> {
                binding.historyFragmentRecyclerview.visibility = View.VISIBLE
                binding.includedLoadingLayout.loadingLayout.visibility = View.GONE
                binding.historyFragmentRecyclerview.showSnackBar(
                    context = requireContext(),
                    text = R.string.error,
                    actionText = R.string.reload,
                    action = { viewModel.getAllHistory() }
                )
            }
            RemoteData.Loading -> {
                binding.historyFragmentRecyclerview.visibility = View.GONE
                binding.includedLoadingLayout.loadingLayout.visibility = View.VISIBLE
            }
            RemoteData.NotAsked -> {}
            is RemoteData.Success -> {
                binding.historyFragmentRecyclerview.visibility = View.VISIBLE
                binding.includedLoadingLayout.loadingLayout.visibility = View.GONE
                adapter.setData(historyList.value)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            HistoryFragment()
    }
}