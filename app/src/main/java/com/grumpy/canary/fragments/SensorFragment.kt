package com.grumpy.canary.fragments

import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.telephony.SmsManager
import android.text.TextUtils
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import com.grumpy.canary.ContactsApplication
import com.grumpy.canary.R
import com.grumpy.canary.database.Contacts
import com.grumpy.canary.viewmodels.ContactsViewModel
import com.grumpy.canary.viewmodels.ContactsViewModelFactory
import java.util.*
import kotlin.math.sqrt


class SensorFragment : Fragment() {

    //declare variables needed to detect shake event
    private var sensorManager : SensorManager ?= null
    private var acceleration = 10f
    private var currentAcceleration = 0f
    private var lastAcceleration = 0f

    private val SEND_SMS_PERMISSION_CODE = 1

    //reference to viewModel
    private val viewModel: ContactsViewModel by activityViewModels {
        ContactsViewModelFactory(
            (activity?.application as ContactsApplication).database.contactsDao()
        )
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_sensor, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sensorManager = activity?.getSystemService(Context.SENSOR_SERVICE) as SensorManager

        currentAcceleration = SensorManager.GRAVITY_EARTH
        lastAcceleration = SensorManager.GRAVITY_EARTH
        if(checkPermission(android.Manifest.permission.SEND_SMS)){
            Objects.requireNonNull(sensorManager)!!.registerListener(sensorListener, sensorManager!!
                .getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL)
        }else{
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(android.Manifest.permission.SEND_SMS),SEND_SMS_PERMISSION_CODE)
        }

    }

    private val sensorListener: SensorEventListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]
            lastAcceleration = currentAcceleration
            currentAcceleration = sqrt((x * x + y * y + z * z).toDouble()).toFloat()
            val delta: Float = currentAcceleration - lastAcceleration
            acceleration = acceleration * 0.9f + delta
            if (acceleration > 12) {
                Toast.makeText(requireContext(), "Shake event detected", Toast.LENGTH_SHORT).show()
                vibratePhone()
                sendMessage()

            }
        }
        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
    }
    private fun vibratePhone(){
        val v = activity?.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        v.vibrate(VibrationEffect.createOneShot(5000, VibrationEffect.DEFAULT_AMPLITUDE))
    }

    private fun sendMessage() {

        if(checkPermission(android.Manifest.permission.SEND_SMS)){
            myMessage()
        }else{
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(android.Manifest.permission.SEND_SMS),SEND_SMS_PERMISSION_CODE)
        }
    }
    private fun myMessage(){

        //TODO send message using coroutine instead of the UI Thread
        var contactList : List<Contacts>  = listOf()
        var myNumber = ""
        var myMsg = ""

        viewModel.allContacts.observe(this.viewLifecycleOwner){list ->
            contactList = list
        }
        if(contactList.isNotEmpty()){
            for(i in contactList){
              myNumber = i.phoneNumber
                myMsg = i.message
                if(myNumber == "" || myMsg ==""){
                    Toast.makeText(requireContext(),"Cant be empty",Toast.LENGTH_SHORT).show()
                }else{
                    if(TextUtils.isDigitsOnly(myNumber)){
                        val smsManager : SmsManager = SmsManager.getDefault()
                        smsManager.sendTextMessage(myNumber,null,myMsg,null,null)
                        Toast.makeText(requireContext(), "Message Sent",Toast.LENGTH_SHORT).show()
                    }
                    else{
                        Toast.makeText(requireContext(),"Please Enter a Correct Number",Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

    }

    private fun checkPermission(permission : String): Boolean{
        val check = ContextCompat.checkSelfPermission(requireContext(),permission)
        return check == PackageManager.PERMISSION_GRANTED
    }

    override fun onResume() {
        sensorManager?.registerListener(sensorListener, sensorManager!!.getDefaultSensor(
            Sensor .TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL
        )
        super.onResume()
    }

    override fun onPause() {
        sensorManager!!.unregisterListener(sensorListener)
        super.onPause()
    }

}

