package com.example.frauddetection.home

import android.Manifest
import android.content.BroadcastReceiver
import android.content.ContentResolver
import android.content.Context
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.frauddetection.MainActivity
import com.example.frauddetection.R
import com.example.frauddetection.databinding.FragmentFirstBinding
import com.example.frauddetection.home.model.MessageModel
import com.example.frauddetection.utils.FraudDetection
import java.lang.Exception

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null

    val REQUEST_READ_MESSAGE: Int = 1

    val messageList: MutableList<MessageModel> = mutableListOf()

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        getAllMessages()
        listener()
        return binding.root

    }

    private fun listener() {
        binding.refreshLayout.setOnRefreshListener(SwipeRefreshLayout.OnRefreshListener {
            getAllMessages()
            binding.refreshLayout.isRefreshing = false
        })

    }

    private fun getAllMessages() {
        if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.READ_SMS, Manifest.permission.RECEIVE_SMS), REQUEST_READ_MESSAGE)
        } else {
            messageList.clear()
            val inboxUri: Uri = Uri.parse("content://sms/inbox")
            val reqCols = arrayOf("_id", "address", "body")

            val cr: ContentResolver = activity?.contentResolver!!

            val c: Cursor? = cr.query(inboxUri, reqCols, null, null, null)
            try {
                while (c?.moveToNext()!!) {
                    val detectionResult = FraudDetection.detectFromSenderAndMessage(c.getString(1), c.getString(2))
                    if (detectionResult != "0% Fraud") {
                        messageList.add(
                            MessageModel(
                                c.getString(0),
                                c.getString(1),
                                c.getString(2),
                                detectionResult
                            )
                        )
                    }
                }
                activity?.getSharedPreferences("messageCount", Context.MODE_PRIVATE)?.edit()?.putInt("count", messageList.size)?.apply()
                val adapter = MessageAdapter(requireActivity(), messageList)
                binding.adapter = adapter
            } catch (e: Exception) {
                Log.d("cursor", e.message!!);
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_READ_MESSAGE) {
            getAllMessages()
        }
    }
}