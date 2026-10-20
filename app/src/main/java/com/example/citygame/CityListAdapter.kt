package com.example.citygame

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CityListAdapter(
    private val cities: List<City>,
    private val onClick: (City) -> Unit
) : RecyclerView.Adapter<CityListAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageThumb: ImageView = view.findViewById(R.id.imageCityThumb)
        val nameText: TextView = view.findViewById(R.id.textCityName)
        val coordsText: TextView = view.findViewById(R.id.textCityCoords)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_city, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val city = cities[position]
        holder.nameText.text = city.name
        holder.coordsText.text = "Широта: ${city.latitude} | Долгота: ${city.longitude}"

        // Подгружаем фото: если есть пользовательское uri, ставим его, иначе дефолтное
        if (city.imageUri != null) {
            holder.imageThumb.setImageURI(Uri.parse(city.imageUri))
        } else {
            holder.imageThumb.setImageResource(city.imageResId)
        }

        holder.itemView.setOnClickListener { onClick(city) }
    }

    override fun getItemCount() = cities.size
}