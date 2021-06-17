package me.fetsh.geekbrains.weather.ui.contacts

import android.Manifest
import android.app.AlertDialog
import android.content.ContentResolver
import android.content.pm.PackageManager
import android.database.Cursor
import android.os.Bundle
import android.provider.ContactsContract
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import me.fetsh.geekbrains.weather.R
import me.fetsh.geekbrains.weather.databinding.HistoryFragmentBinding

class ContactsFragment : Fragment() {

    private var _binding: HistoryFragmentBinding? = null
    private val binding get() = _binding!!
    private val adapter: ContactsAdapter by lazy { ContactsAdapter() }

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
        checkPermission()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun checkPermission() {
        context?.let { context ->
            if(ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) ==
                PackageManager.PERMISSION_GRANTED) {
                getContacts()
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.READ_CONTACTS)) {
                AlertDialog.Builder(context)
                    .setTitle(R.string.contacts_permission_title)
                    .setMessage(R.string.contacts_permission_message)
                    .setPositiveButton(getString(R.string.contacts_permission_request_button)) { _, _ ->
                        requestPermission()
                    }
                    .setNegativeButton(getString(R.string.cancel)) { dialog, _ -> dialog.dismiss() }
                    .create()
                    .show()
            } else {
                requestPermission()
            }
        }
    }

    private val activityResultLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                getContacts()
            } else {
                context?.let { context ->
                    AlertDialog.Builder(context)
                        .setTitle(R.string.contacts_permission_title)
                        .setMessage(R.string.contacts_permission_denied_message)
                        .setNegativeButton(getString(R.string.cancel)) { dialog, _ -> dialog.dismiss() }
                        .create()
                        .show()
                }
            }
        }

    private fun requestPermission() {
        activityResultLauncher.launch(Manifest.permission.READ_CONTACTS)
    }

    private fun getContacts() {
        context?.let { context ->
            val contentResolver: ContentResolver = context.contentResolver
            val cursorWithContacts: Cursor? = contentResolver.query(
                ContactsContract.Contacts.CONTENT_URI,
                null,
                null,
                null,
                ContactsContract.Contacts.DISPLAY_NAME + " ASC"
            )

            cursorWithContacts?.let { cursor ->
                val contactsList : MutableList<String> = mutableListOf()
                for (i in 0..cursor.count) {
                    if (cursor.moveToPosition(i)) {
                        cursor.getString(
                            cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
                        )?.let { name ->
                            contactsList.add(name)
                        }
                    }
                }
                adapter.setData(contactsList)
            }
            cursorWithContacts?.close()
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            ContactsFragment()
    }
}