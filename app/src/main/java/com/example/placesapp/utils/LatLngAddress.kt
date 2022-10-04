package com.example.placesapp.utils

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.AsyncTask
import java.util.*

class LatLngAddress(context: Context, private val latitude: Double, private val longitude: Double) :
    AsyncTask<Void, String, String>() {

    private val geocoder: Geocoder = Geocoder(context, Locale.getDefault())
    private lateinit var listener: AddressListener

    @Deprecated("Deprecated in Java")
    override fun doInBackground(vararg params: Void?): String {
        try {
            val addressList: List<Address>? = geocoder.getFromLocation(latitude, longitude, 1)

            if (addressList != null && addressList.isNotEmpty()) {
                val address: Address = addressList[0]
                val builder = StringBuilder()
                for (i in 0..address.maxAddressLineIndex) {
                    builder.append(address.getAddressLine(i)).append(" ")
                }
                builder.deleteCharAt(builder.length - 1)
                return builder.toString()
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return ""
    }

    override fun onPostExecute(result: String?) {
        if (result == null) {
            listener.errorFound()
        } else {
            listener.addressFound(result)
        }
        super.onPostExecute(result)
    }

    fun setAddressListener(addressListener: AddressListener) {
        listener = addressListener
    }

    fun getAddress() {
        execute()
    }

    interface AddressListener {
        fun addressFound(address: String)
        fun errorFound()
    }
}