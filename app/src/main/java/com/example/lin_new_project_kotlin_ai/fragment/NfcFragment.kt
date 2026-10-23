package com.example.lin_new_project_kotlin_ai.fragment

import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NdefMessage
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.IsoDep
import android.nfc.tech.MifareClassic
import android.nfc.tech.MifareUltralight
import android.nfc.tech.Ndef
import android.nfc.tech.NfcA
import android.nfc.tech.NfcB
import android.nfc.tech.NfcF
import android.nfc.tech.NfcV
import android.os.Bundle
import android.os.Parcelable
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import com.example.lin_new_project_kotlin_ai.MyApplication
import com.example.lin_new_project_kotlin_ai.databinding.FragmentNfcBinding
import com.example.lin_new_project_kotlin_ai.`fun`.MsgEvent
import com.example.lin_new_project_kotlin_ai.`fun`.NFCMethod
import com.example.lin_new_project_kotlin_ai.`fun`.RxBus
import com.example.lin_new_project_kotlin_ai.viewBinding.BaseBindingFragment
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.functions.Consumer
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


class NfcFragment : BaseBindingFragment<FragmentNfcBinding?>() {
    var nfcPendingIntent: PendingIntent? = null
    lateinit var intentFiltersArray: Array<IntentFilter?>


    var nfcAdpt: NfcAdapter? = null
    private val compositeDisposable = CompositeDisposable()
    private val techList = arrayOf(
        arrayOf<String>(
            NfcA::class.java.getName(), NfcB::class.java.getName(), NfcF::class.java.getName(),
            NfcV::class.java.getName(), IsoDep::class.java.getName(),
            MifareClassic::class.java.getName(), MifareUltralight::class.java.getName(),
            Ndef::class.java.getName()
        )
    )

    override fun onCreateViewBinding(layoutInflater: LayoutInflater): FragmentNfcBinding {
        return FragmentNfcBinding.inflate(layoutInflater)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // 獲取默認的NFC控制器
        nfcAdpt = NfcAdapter.getDefaultAdapter(getContext())
        // Check if the smartphone has NFC
        if (nfcAdpt == null) {
            showToast("不支援NFC")
            return
        } else {
            if (!nfcAdpt!!.isEnabled()) {
                showToast("請開啟NFC功能")
                startActivity(Intent(Settings.ACTION_WIRELESS_SETTINGS))
            }
        }
        setIntentFiltersArray()
//        initEventBus()
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun initEventBus() {
        EventBus.getDefault().register(this)
        val disposable: Disposable =
            RxBus.getInstance().toObservable(MsgEvent::class.java).subscribe(
                Consumer { msg: MsgEvent? ->
                    if (msg?.getType() == MyApplication.nfc) {
                        requireActivity().runOnUiThread {
                            val intent = msg.getObj() as Intent
                            val action = intent.getAction()
                            if (NfcAdapter.ACTION_TAG_DISCOVERED == action
                                || NfcAdapter.ACTION_TECH_DISCOVERED == action
                                || NfcAdapter.ACTION_NDEF_DISCOVERED == action
                            ) {
                                val rawMsgs: Array<Parcelable?>? =
                                    intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)
                                val msgs: Array<NdefMessage?>?
                                if (rawMsgs != null) {
                                    msgs = kotlin.arrayOfNulls<NdefMessage>(rawMsgs.size)
                                    for (i in rawMsgs.indices) {
                                        msgs[i] = rawMsgs[i] as NdefMessage?
                                    }
                                } else {
                                    // Unknown tag type
                                    val empty = ByteArray(0)
                                    val id = intent.getByteArrayExtra(NfcAdapter.EXTRA_ID)
                                    val tag: Parcelable? =
                                        intent.getParcelableExtra<Parcelable?>(NfcAdapter.EXTRA_TAG)

                                    //                byte[] payload = dumpTagData(tag).getBytes();
//                NdefRecord record = new NdefRecord(NdefRecord.TNF_UNKNOWN, empty, id, payload);
//                NdefMessage msg = new NdefMessage(new NdefRecord[]{record});
//                msgs = new NdefMessage[]{msg};
//
                                    val nfc_id = dumpTagData(tag)
                                    Log.d("nfc_id", nfc_id)
                                    Log.v("NFC測試", "nfc_id：" + nfc_id)
                                    getBinding()!!.tvNfc.setText(nfc_id)
                                }
                            }
                        }
                    }
                })
        compositeDisposable.add(disposable)
    }

    private fun dumpTagData(p: Parcelable?): String {
        val sb = StringBuilder()
        val tag = p as Tag
        val id = tag.getId()
        sb.append("Tag ID (hex): ").append(NFCMethod.getHex(id)).append("\n")
        sb.append("Tag ID (dec): ").append(NFCMethod.getDec(id)).append("\n")
        sb.append("ID (reversed): ").append(NFCMethod.getReversed(id)).append("\n")
        Log.e(
            "NFC測試",
            "Tag ID (hex):" + NFCMethod.getHex(id) + "\r\nTag ID (dec):" + NFCMethod.getDec(id) + "\r\nID (reversed):" + NFCMethod.getReversed(
                id
            )
        )

        //有必要再顯示
//        alert("刷code", sb.toString());
        val nfcReversed = NFCMethod.getReversed(id).toString()


        return nfcReversed
    }

    private fun setIntentFiltersArray() {
        val filter = IntentFilter()
        filter.addAction(NfcAdapter.ACTION_TAG_DISCOVERED)
        filter.addAction(NfcAdapter.ACTION_NDEF_DISCOVERED)
        filter.addAction(NfcAdapter.ACTION_TECH_DISCOVERED)
        val nfcIntent = Intent(getContext(), javaClass)
        nfcIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        val tagIntentFilter =
            IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED)
        try {
            tagIntentFilter.addDataType("text/plain")
            intentFiltersArray = (arrayOf<IntentFilter>(filter) as Array<IntentFilter?>?)!!
        } catch (t: Throwable) {
            t.printStackTrace()
        }
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

//    override fun onResume() {
//        super.onResume()
//        if (nfcAdpt != null) {
//            nfcAdpt!!.enableForegroundDispatch(
//                getActivity(), nfcPendingIntent,
//                intentFiltersArray, this.techList
//            )
//        }
//    }

    override fun onPause() {
        super.onPause()
        if (nfcAdpt != null) {
            nfcAdpt!!.disableForegroundDispatch(getActivity())
        }
    } //    @Override
    //    public void onNewIntent(Intent intent) {
    //        super.onNewIntent(intent);
    //        Log.v("NFC測試", "進入NFC感應");
    //        String action = intent.getAction();
    //        Log.e("NFC測試", "NFC_action：" + action);
    //        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)
    //                || NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)
    //                || NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
    //
    //            Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
    //            NdefMessage[] msgs;
    //            if (rawMsgs != null) {
    //                msgs = new NdefMessage[rawMsgs.length];
    //                for (int i = 0; i < rawMsgs.length; i++) {
    //                    msgs[i] = (NdefMessage) rawMsgs[i];
    //                }
    //            } else {
    //                // Unknown tag type
    //                byte[] empty = new byte[0];
    //                byte[] id = intent.getByteArrayExtra(NfcAdapter.EXTRA_ID);
    //                Parcelable tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
    //
//    /**/                byte[] payload = dumpTagData(tag).getBytes();
//    * /                NdefRecord record = new NdefRecord(NdefRecord.TNF_UNKNOWN, empty, id, payload);
//    * /                NdefMessage msg = new NdefMessage(new NdefRecord[]
//    { record });
//    * /                msgs = new NdefMessage[]
//    { msg };
//    * / */
    //                String nfc_id = dumpTagData(tag);
    //                Log.d("nfc_id", nfc_id);
    //                Log.v("NFC測試", "nfc_id：" + nfc_id);
    //                tv_nfc.setText(nfc_id);
    //            }
    //        }
    //    }
}