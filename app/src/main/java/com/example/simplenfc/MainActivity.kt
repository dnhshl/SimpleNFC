package com.example.simplenfc

import android.content.Intent
import android.graphics.Color.rgb
import android.net.Uri
import android.nfc.NdefMessage
import android.nfc.NfcAdapter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import org.ndeftools.Message
import org.ndeftools.Record
import org.ndeftools.externaltype.AndroidApplicationRecord
import org.ndeftools.wellknown.TextRecord
import org.ndeftools.wellknown.UriRecord
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
                try {
                    val records : List<Record> = Message(rawMessage[i] as NdefMessage)
                    Log.i(TAG, "Message $i mit ${records.size} Records")

                    for(k in 0 until records.size) {
                        Log.i(TAG, "Record #$k ist eine ${records.get(k).javaClass.simpleName}")

                        val record = records.get(k)
                        if(record is TextRecord){
                            val textRecord = record as TextRecord
                            Log.i(TAG, "TextRecord is ${textRecord.text}")

                            val string = textRecord.text
                            val parts = string.split("-")
                            val color = rgb(parts[1].toInt(), parts[2].toInt(), parts[3].toInt())
                            window.decorView.setBackgroundColor(color)

                        }else if(record is UriRecord){
                            val uri = record as UriRecord
                            Log.i(TAG, "UriRecord is ${uri.uri}")

                            val implicitIntent = Intent(Intent.ACTION_VIEW)
                            implicitIntent.data = Uri.parse(uri.uri.toString())
                            startActivity(implicitIntent)

                        }else if (record is AndroidApplicationRecord) {
                            val aar = record as AndroidApplicationRecord
                            Log.i(TAG, "Package is ${aar.packageName}")
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Problem parsing message : ${e.localizedMessage}")
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