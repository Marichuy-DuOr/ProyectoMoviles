package pansitosapp.mx.carrito;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;


import com.google.gson.JsonObject;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import pansitosapp.mx.R;
import pansitosapp.mx.http.Client;
import pansitosapp.mx.http.Node;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditarCarrito extends Fragment implements View.OnClickListener {

    private NavController navController;
    ProgressDialog progress;

    TextView nombre, descripcion, precio;
    ImageView imagen;
    EditText cantidad;
    Button editar, eliminar;
    Integer ctd;

    Carrito carrito;
    String token; // uso el token muchas veces, mejor lo declaro desde el inicio

    Boolean band;

    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.editar_carrito, container, false);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
        // revisa si se recibieron argumentos
        if (getArguments() != null){ // en el caso de que si, se pone la opcion para agregar el pan
            carrito = (Carrito) getArguments().getSerializable("elCarrito");
            band = false;
        } else { // en el caso de que no, se va a crear un pan nuevo
            band = true;
        }
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);

        // guardar el token
        SharedPreferences preferences = getActivity().getSharedPreferences("Usuario", Context.MODE_PRIVATE);
        token = preferences.getString("token","No existe la informacion");

        editar = view.findViewById(R.id.btnEditar);
        editar.setOnClickListener(this);

        eliminar = view.findViewById(R.id.btnEliminar);
        eliminar.setOnClickListener(this);

        nombre = view.findViewById(R.id.name);
        imagen = view.findViewById(R.id.img);
        descripcion = view.findViewById(R.id.desc);
        precio = view.findViewById(R.id.txtPrecio);
        cantidad = view.findViewById(R.id.cant);


        if(!band){
            nombre.setText(carrito.getNombre());
            precio.setText(carrito.getPrecio().toString());
            descripcion.setText(carrito.getDescripcion());
            Picasso.with(getContext()).load(carrito.getImagen()).into(imagen);
        } else { // quita el boton de eliminar para que no este cuando se crea un pan
            Toast toast = Toast.makeText(getActivity().getApplicationContext(), "No se puede cargar el producto, intentalo más tarde" , Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER_VERTICAL,0,0);
            toast.show();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnEditar:
                editarCarrito();
                break;
            case R.id.btnEliminar:
                eliminarCarrito();
                break;
        }
    }

    private void eliminarCarrito() {
        Client userRest = Node.getClient().create(Client.class); // declara la clase de peticiones al servidor

        // progress dialog para cuando se elimina algo
        progress = ProgressDialog.show(getContext(), "Eliminando","Espera", true);

        // manda la peticion al servidor, le manda el token y el id del pan
        final Call<JsonObject> call = userRest.deleteCarrito(token, carrito.getId());
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                progress.dismiss(); // cierra el progress dialog
                if (response.code() != 200) {
                    JSONObject jObjError = null;
                    try {
                        jObjError = new JSONObject(response.errorBody().string());
                        Log.i("mainActivity", jObjError.toString());
                        System.out.println(jObjError.toString());
                        return;
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                // cuando se borra el producto lo regresa al fragmento de todos los productos
                JsonObject json = response.body();
                navController.navigate(R.id.nav_to_carrito);
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                progress.dismiss(); // cierralo aqui tambien
                call.cancel();
            }
        });
    }

    private void editarCarrito() {
        String can = cantidad.getText().toString();

        if( !can.equals("") ) {
            Client userRest = Node.getClient().create(Client.class); // declara la clase de las peticiones al servidor

            progress = ProgressDialog.show(getContext(), "Cargando","Espera", true);

            JsonObject data = new JsonObject(); // declara objeto json para el body
            data.addProperty("id", carrito.getId()); // pone el id del pan que ya tenemos previamente guardado
            data.addProperty("cantidad", can);

            // peticion al servidor, manda el token y el body
            final Call<JsonObject> call = userRest.updateCarrito(token, data);
            call.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    progress.dismiss(); // cierra el progress dialog
                    if (response.code() != 200) {
                        JSONObject jObjError = null;
                        try {
                            jObjError = new JSONObject(response.errorBody().string());
                            Log.i("mainActivity", jObjError.toString());
                            System.out.println(jObjError.toString());
                            return;
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    // recibe la respuesta del servidor y redirige a Productos
                    JsonObject json = response.body();
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    progress.dismiss(); // cierra esta cosa donde sea necesario
                    call.cancel();
                }
            });
        }else {
            Toast toast = Toast.makeText(getActivity().getApplicationContext(), "Completa el campo CANTIDAD" , Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER_VERTICAL,0,0);
            toast.show();
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) { // poner para que el menu de la flecha para regresar funcione
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) { // para la flecha de regresar
        int id = item.getItemId();
        if (id == android.R.id.home) {
            ((AppCompatActivity)getActivity()).onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}