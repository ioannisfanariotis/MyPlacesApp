package com.example.placesapp.adapters

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.placesapp.activities.AddActivity
import com.example.placesapp.activities.MainActivity
import com.example.placesapp.database.DatabaseHandler
import com.example.placesapp.databinding.RecyclerViewBinding
import com.example.placesapp.models.PlaceModel

open class PlaceAdapter(private val context: Context, private val items: ArrayList<PlaceModel>): RecyclerView.Adapter<PlaceAdapter.ViewHolder>() {

    private var onClickListener: OnClickListener? = null

    fun setOnClickListener(onClickListener: OnClickListener){
        this.onClickListener = onClickListener
    }

    class ViewHolder(binding: RecyclerViewBinding): RecyclerView.ViewHolder(binding.root){
        val image = binding.myPlace
        val title = binding.title
        val description = binding.description
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(RecyclerViewBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.image.setImageURI(Uri.parse(item.image))
        holder.title.text = item.title
        holder.description.text = item.description

        holder.itemView.setOnClickListener{
            if (onClickListener != null){
                onClickListener!!.onClick(position, item)
            }
        }
    }

    fun notifyEditItem(activity: Activity, position: Int, requestCode: Int){
        val intent = Intent(context, AddActivity::class.java)
        intent.putExtra(MainActivity.EXTRA_DETAILS, items[position])
        activity.startActivityForResult(intent, requestCode)
        notifyItemChanged(position)
    }

    fun removeItem(position: Int){
        val dbHandler = DatabaseHandler(context)
        val isDeleted = dbHandler.deletePlace(items[position])
        if (isDeleted > 0){
            items.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    interface OnClickListener{
        fun onClick(position: Int, model: PlaceModel)
    }
}