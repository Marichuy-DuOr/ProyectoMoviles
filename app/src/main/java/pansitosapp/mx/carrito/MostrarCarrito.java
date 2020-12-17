package pansitosapp.mx.carrito;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

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

public class MostrarCarrito extends Fragment implements CarritoInterface, View.OnClickListener {

    private NavController navController; // control para navegar entre componentes
    ProgressDialog progress;

    private RecyclerView recyclerPanes;
    CarritoAdapter carritoAdapter;

    Float total = new Float(0);
    TextView t;

    Button comprar, regresar;

    ArrayList<Carrito> listaCarrito; // lista con todos los panes

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.carrito, container, false); // Cambiar el layout que corresponda
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true); // flecha para regresar al menu
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
        return root;


    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);

        t = view.findViewById(R.id.textViewTotal);
        comprar = view.findViewById(R.id.btnContinuar);
        comprar.setOnClickListener(this);

        regresar = view.findViewById(R.id.btnRegresar);
        regresar.setOnClickListener(this);

        getAllCarrito(); // llena el array de panes

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnContinuar:
                Bundle bundle = new Bundle();
                bundle.putSerializable("elCarrito", listaCarrito );
                navController.navigate(R.id.nav_to_mapa, bundle);
                break;
            case R.id.btnRegresar:
                navController.navigate(R.id.nav_to_client_menu);
                break;
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) { //agregar cuando pongas opciones en el menu
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) { // para que el boton de la flecha funcione
        int id = item.getItemId();
        if (id == android.R.id.home) {
            ((AppCompatActivity)getActivity()).onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }



    private void getAllCarrito(){
        Client userRest = Node.getClient().create(Client.class); // declara la clase de las peticiones al servidor

        progress = ProgressDialog.show(getContext(), "Cargando","Espera", true);

        // cargar el token en una variable para poderlo mandar en una peticion como header
        SharedPreferences preferences = getActivity().getSharedPreferences("Usuario", Context.MODE_PRIVATE);
        String token = preferences.getString("token","No existe la informacion");

        // hace la peticion al servidor con el token y el body
        final Call<JsonObject> call = userRest.getAllCarrito(token);
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

                JsonObject json = response.body(); // recibe el body
                // el servidior manda la respuesta en una variable llamada "array" en el body que tambien es un array
                JsonArray jsonArray = json.getAsJsonArray("array"); // recibe el arra en un tipo de variable especial para esto
                listaCarrito =  new ArrayList<>();
                Float mul = new Float(0);
                for (int i=0; i < jsonArray.size(); i++){ //recorre el array para sacar el contenido
                    JsonObject object = jsonArray.get(i).getAsJsonObject(); // saca el objeto del array en otro objeto json
                    // saca cada variable del objeto en variables normales de java
                    // si la variable es int o float utiliza Integer o Float para que no haya errores
                    Integer id = object.getAsJsonPrimitive("id").getAsInt();
                    Integer id_producto = object.getAsJsonPrimitive("id_producto").getAsInt();
                    String nombre = object.getAsJsonPrimitive("nombre").getAsString();
                    Float precio = object.getAsJsonPrimitive("precio").getAsFloat();
                    String imagen = object.getAsJsonPrimitive("imagen").getAsString();
                    String descripcion = object.getAsJsonPrimitive("descripcion").getAsString();
                    Integer cantidad = object.getAsJsonPrimitive("cantidad").getAsInt();

                    Carrito cart = new Carrito(id, id_producto, nombre, precio, imagen, descripcion, cantidad); // crea un objeto
                    listaCarrito.add(cart); // lo agrega a la lista
                    mul = precio * cantidad;
                    total += mul;
                }

                recyclerPanes = getView().findViewById(R.id.recyclerid); //obtiene el id de xml
                recyclerPanes.setLayoutManager(new LinearLayoutManager(getContext()));
                // declara el adapter que es el que maneja el otro xml que se va a reciclar y le manda el array de panes
                carritoAdapter =  new CarritoAdapter(listaCarrito);
                t.setText(total.toString());

                recyclerPanes.setAdapter(carritoAdapter); // le asignas el adapter al recyclerView
                carritoAdapter.setOnClick(MostrarCarrito.this); // Agrega click listener
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                progress.dismiss(); // aqui tambien se cierra
                call.cancel();
            }
        });

    }

    @Override
    public void onPanClick(int pos) { // lo que vaya a hacer el producto cuando hagan click
        // lo dirijo al fragmento para editar panes con el pan como parametro para modificarlo
        Bundle bundle = new Bundle();
        bundle.putSerializable("elCarrito", listaCarrito.get(pos) );
        navController.navigate(R.id.nav_to_editarcarrito, bundle);
    }
}
