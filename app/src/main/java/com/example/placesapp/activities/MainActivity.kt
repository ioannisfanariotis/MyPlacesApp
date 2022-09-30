package com.example.placesapp.activities

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.placesapp.adapters.PlaceAdapter
import com.example.placesapp.database.DatabaseHandler
import com.example.placesapp.databinding.ActivityMainBinding
import com.example.placesapp.models.PlaceModel
import pl.kitek.rvswipetodelete.SwipeToDeleteCallback
import pl.kitek.rvswipetodelete.SwipeToEditCallback

class MainActivity : AppCompatActivity() {

    private var binding: ActivityMainBinding? = null

    companion object{
        const val ACTIVITY_REQUEST_CODE = 1
        const val EXTRA_DETAILS = "extra_details"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        binding?.fab?.setOnClickListener {
            val intent = Intent(this, AddActivity::class.java)
            startActivityForResult(intent, ACTIVITY_REQUEST_CODE)
        }
        getListFromDB()
    }

    private fun getListFromDB(){
        val dbHandler = DatabaseHandler(this)
        val list: ArrayList<PlaceModel> = dbHandler.getList()

        if (list.size > 0){
            binding?.placeList?.visibility = View.VISIBLE
            binding?.nothing?.visibility = View.GONE
            setUpRecyclerView(list)
        }else{
            binding?.placeList?.visibility = View.GONE
            binding?.nothing?.visibility = View.VISIBLE
        }
    }

    private fun setUpRecyclerView(list: ArrayList<PlaceModel>){
        binding?.placeList?.layoutManager = LinearLayoutManager(this)
        binding?.placeList?.setHasFixedSize(true)
        val placeAdapter = PlaceAdapter(this, list)
        binding?.placeList?.adapter = placeAdapter

        placeAdapter.setOnClickListener(object: PlaceAdapter.OnClickListener{
            override fun onClick(position: Int, model: PlaceModel) {
                val intent = Intent(this@MainActivity, DetailsActivity::class.java)
                intent.putExtra(EXTRA_DETAILS, model)
                startActivity(intent)
            }
        })

        val swipeEdit = object: SwipeToEditCallback(this){
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val adapter = binding?.placeList?.adapter as PlaceAdapter
                adapter.notifyEditItem(this@MainActivity, viewHolder.adapterPosition, ACTIVITY_REQUEST_CODE)
            }
        }

        val editItem = ItemTouchHelper(swipeEdit)
        editItem.attachToRecyclerView(binding?.placeList)

        val swipeDelete = object: SwipeToDeleteCallback(this){
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val adapter = binding?.placeList?.adapter as PlaceAdapter
                adapter.removeItem(viewHolder.adapterPosition)
                getListFromDB()
            }
        }

        val deleteItem = ItemTouchHelper(swipeDelete)
        deleteItem.attachToRecyclerView(binding?.placeList)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == ACTIVITY_REQUEST_CODE){
            if (resultCode == Activity.RESULT_OK){
                getListFromDB()
            }else{
                Log.e("Activity", "Cancelled")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}