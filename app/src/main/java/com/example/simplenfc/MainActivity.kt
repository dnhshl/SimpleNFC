package com.example.simplenfc

import android.content.Intent
import android.nfc.NdefMessage
import android.nfc.NfcAdapter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import splitties.alertdialog.alertDialog
import splitties.alertdialog.cancelButton
import splitties.alertdialog.positiveButton
import splitties.toast.toast

class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivity"

    private val mNfcAdapter: NfcAdapter by lazy { NfcAdapter.getDefaultAdapter(applicationContext) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (mNfcAdapter == null) {
            toast(R.string.nfc_not_available)
            finish()
        }

        val intent = intent
        Log.d(TAG, "onCreate: ${intent.action}")
        handleNfcNdefIntent(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        Log.d(TAG, "onNewIntent : ${intent!!.action}")
        handleNfcNdefIntent(intent)
        super.onNewIntent(intent)
    }

    private fun handleNfcNdefIntent(intent: Intent) {
        if(!intent.hasExtra(NfcAdapter.EXTRA_TAG)){
            return
        }
        toast(getString(R.string.nfc_received, intent.action))

        val rawMessage = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)
        if(rawMessage != null && rawMessage.size > 0) {
            val messages = arrayOfNulls<NdefMessage>(rawMessage.size)
            for(i in 0 until rawMessage.size) {
                messages[i] = rawMessage[i] as NdefMessage
                for(record in messages[i]!!.records){
                    val payloadData = String(record.payload)
                    Log.i(TAG, "handleNfcIntent: $payloadData")
                    toast("payload: $payloadData")
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        if (!mNfcAdapter.isEnabled) {
            alertDialog (title = getString(R.string.nfc_activate_title),
                    message = getString(R.string.nfc_not_activated)){
                positiveButton(R.string.activate) {
                    val intent = Intent(Settings.ACTION_NFC_SETTINGS)
                    startActivity(intent)
                }
                cancelButton {
                    finish()
                }
            }.show()
        }
    }
}