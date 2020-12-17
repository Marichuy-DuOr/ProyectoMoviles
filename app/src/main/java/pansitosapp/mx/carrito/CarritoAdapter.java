package pansitosapp.mx.carrito;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import pansitosapp.mx.R;

public class CarritoAdapter extends RecyclerView.Adapter<CarritoAdapter.CarritoViewHolder> {
    ArrayList<Carrito> listaCarrito; // los panes
    CarritoInterface onClick; // la interfaz de panes

    public CarritoAdapter( ArrayList<Carrito> listaCarrito) {
        this.listaCarrito = listaCarrito;
    }

    @Override
    public CarritoViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
        // poner el view que se va a reciclar
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.item_card_carrito,parent,false);
        return new CarritoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CarritoViewHolder holder, final int position) {
        // modifica por objeto del array
        holder.txtNombre.setText(listaCarrito.get(position).getNombre());
        holder.txtDescripcion.setText(listaCarrito.get(position).getDescripcion());
        holder.txtPrecio.setText("$" + listaCarrito.get(position).getPrecio().toString());
        // para cargar la imagen con la url
        Picasso.with(holder.itemView.getContext()).load(listaCarrito.get(position).getImagen()).into(holder.imagen);
        // agrega el click listener a cada uno de los objetos
        holder.opcion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // aqui adentro irian cambios en el view como cuando sombreas notificaciones ya vistas
                onClick.onPanClick(position); // lo manda al onClick del fragmento padre que es el de Productos
            }
        });
    }

    @Override
    public int getItemCount() {
        return listaCarrito.size();
    }

    public class CarritoViewHolder extends RecyclerView.ViewHolder {
        // para modificarlos segun las variables del pan
        TextView txtNombre,txtDescripcion, txtPrecio;
        ImageView imagen;

        CardView opcion; // tiene el id del card para el onClick

        public CarritoViewHolder(View itemView) {
            super(itemView);
            txtNombre = (TextView) itemView.findViewById(R.id.name);
            txtDescripcion = (TextView) itemView.findViewById(R.id.desc);
            txtPrecio = (TextView) itemView.findViewById(R.id.precio);
            imagen = (ImageView) itemView.findViewById(R.id.img);
            opcion = (CardView) itemView.findViewById(R.id.opcion);

        }
    }

    public void setOnClick(CarritoInterface onClick) {this.onClick = onClick; } // agregar para el onClick
}
