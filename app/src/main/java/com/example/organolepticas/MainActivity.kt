package com.example.organolepticas
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.wear.compose.material.ContentAlpha
import androidx.wear.compose.material.LocalContentAlpha
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.github.mikephil.charting.utils.Utils
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.json.JSONArray
import org.json.JSONObject
import viewmodels.DatosGuardadosViewModel
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Utils.init(this)
        setContent {
            val navController = rememberNavController()
            val isLoggedIn = rememberSaveable { mutableStateOf(false) }
            val coroutineScope = rememberCoroutineScope()
            val viewModel: DatosGuardadosViewModel = viewModel(
                factory = DatosGuardadosViewModelFactory(applicationContext)
            )
            NavHost(navController, startDestination = "startForms/{username}") {
                composable("startForms/{username}",
                    arguments = listOf(
                        navArgument("username") { type = NavType.StringType }
                    )) { backStackEntry ->
                    val username = backStackEntry.arguments?.getString("username") ?: "organolepticas"
                    startForms(
                        navController = navController,
                        viewModel = viewModel,
                        context = applicationContext,
                        username = username
                    )
                }


                composable("loginScreen") {
                    LoginScreen(navController = navController, viewModel = viewModel,context = applicationContext)
                }
                composable("searchBlock/{username}",
                    arguments = listOf(
                        navArgument("username") { type = NavType.StringType }
                    )) {
                        backStackEntry ->
                    val username = backStackEntry.arguments?.getString("username")
                    username?.let { muestreoValue ->
                        searchBlock(
                            navController = navController,
                            viewModel = viewModel,
                            context = applicationContext,
                            username = muestreoValue
                        )
                    }
                }
                composable("startScreen/{username}",
                    arguments = listOf(
                        navArgument("username") { type = NavType.StringType }
                    )) { backStackEntry ->
                    val username = backStackEntry.arguments?.getString("username")
                    username?.let { muestreoValue ->
                        startScreen(navController = navController, viewModel = viewModel,context = applicationContext,username = muestreoValue)
                    }
                }
                composable(
                    "formulario/{muestreo}/{bloque}/{username}",
                    arguments = listOf(
                        navArgument("muestreo") { type = NavType.IntType },
                        navArgument("bloque") { type = NavType.StringType },
                        navArgument("username") { type = NavType.StringType }
                    )
                ) { backStackEntry ->
                    val muestreo = backStackEntry.arguments?.getInt("muestreo")
                    val bloque = backStackEntry.arguments?.getString("bloque")
                    val usuario = backStackEntry.arguments?.getString("username")
                    muestreo?.let { muestreoValue ->
                        bloque?.let { bloqueValue ->
                            usuario?.let { usuarioValue ->
                                formulario(
                                    navController = navController,
                                    bloque = bloqueValue,
                                    viewModel = viewModel,
                                    username = usuarioValue
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

class DatosGuardadosViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DatosGuardadosViewModel::class.java)) {
            return DatosGuardadosViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class LocalUserStore(private val context: Context) {
    private val sharedPreferences = context.getSharedPreferences("local_user_store", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun getUsers(): MutableList<User> {
        val usersJson = sharedPreferences.getString("users", null)
        return if (usersJson != null) {
            val type = object : TypeToken<MutableList<User>>() {}.type
            gson.fromJson(usersJson, type)
        } else {
            mutableListOf()
        }
    }

    fun saveUsers(users: MutableList<User>) {
        val usersJson = gson.toJson(users)
        sharedPreferences.edit().putString("users", usersJson).apply()
    }

    fun saveUser(user: User) {
        val users = getUsers()
        users.removeAll { it.username == user.username }  // Remove any existing user with the same username
        users.add(user)
        saveUsers(users)
    }

    fun getUser(username: String): User? {
        return getUsers().find { it.username == username }
    }

    data class User(val username: String, val password: String)
}

@Composable
fun LoginScreen(navController: NavController, viewModel: DatosGuardadosViewModel, context: Context) {
    var user by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))
        Text(
            text = "Agricola Guapa SAS\n Muestreos Organolépticas\nLogin",
            fontSize = 30.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(40.dp))
        TextField(
            value = user,
            onValueChange = { user = it },
            label = { Text("Ingrese Usuario") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 7.dp)
                .border(1.dp, Color.Gray, shape = RoundedCornerShape(4.dp)),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(16.dp))
        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Ingrese Contraseña") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 7.dp)
                .border(1.dp, Color.Gray, shape = RoundedCornerShape(4.dp)),
            singleLine = true,
            visualTransformation = PasswordVisualTransformation()
        )
        Spacer(modifier = Modifier.height(40.dp))
        Button(
            onClick = {
                coroutineScope.launch {
                    loginUser(context, user, password) { success ->
                        if (success) {
                            navController.navigate("startForms/$user")
                        } else {
                            errorMessage = when {
                                user.isEmpty() && password.isEmpty() -> "Ingrese usuario y contraseña"
                                user.isEmpty() -> "Ingrese usuario"
                                password.isEmpty() -> "Ingrese contraseña"
                                else -> "Usuario o contraseña incorrectos"
                            }
                        }
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text(text = "Ingresar")
        }
        Spacer(modifier = Modifier.height(16.dp))
        if (errorMessage.isNotEmpty()) {
            Text(
                text = errorMessage,
                color = Color.Red,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Image(
            painter = painterResource(id = R.drawable.logi),
            contentDescription = null,
            modifier = Modifier.size(80.dp)
        )
        Text(
            text = "Powered by Guapa\nVersión 2.0",
            textAlign = TextAlign.Center,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}
@Composable
fun startScreen(navController: NavController, viewModel: DatosGuardadosViewModel, context: Context, username: String) {
    var user by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    val datos = viewModel.datosGuardados.filter { it["Origen"] != "Web" }
    var showDialog by remember { mutableStateOf(false) }
    var showDialogNew by remember { mutableStateOf(false) }
    var showDialogMuestras by remember { mutableStateOf(false) }
    if (showDialogMuestras) {
        AlertDialog(
            onDismissRequest = {
                // Dismiss the dialog if the user clicks outside of it or presses the back button
                showDialogMuestras = false
            },
            title = {
                Text(text = "¿Está seguro de eliminar todas las muestras?")
                //TODO
                // "¿Está seguro de eliminar el bloque $bloque con fecha muestra $fecha_muestra y $cantidad de registros?"
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDialogMuestras = false
                        viewModel.borrarTodosLosDatosGuardadosNoWeb()
                    }
                ) {
                    Text(text = "Aceptar")
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        showDialogMuestras = false
                    }
                ) {
                    Text(text = "Cancelar")
                }
            }
        )
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = {
                // Dismiss the dialog if the user clicks outside of it or presses the back button
                showDialog = false
            },
            title = {
                Text(text = "¿Está seguro de subir ${datos.count()} registros?")
                //TODO
                // "¿Está seguro de eliminar el bloque $bloque con fecha muestra $fecha_muestra y $cantidad de registros?"
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDialog = false
                        showDialogNew = true
                        coroutineScope.launch {
                            sendData(viewModel = viewModel, context = context){ success ->
                                showDialogNew = !success
                                println(success)
                                if(!showDialogNew){viewModel.borrarTodosLosDatosGuardadosNoWeb()}
                            }
                        }
                    }
                ) {
                    Text(text = "Aceptar")
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        showDialog = false
                    }
                ) {
                    Text(text = "Cancelar")
                }
            }
        )
    }

    if (showDialogNew) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text(text = "Se están enviando los datos. Por favor, espere...") },
            confirmButton = {}
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))
        Text(
            text = "Agricola Guapa SAS \n Muestreos Organolepticas",
            fontSize = 30.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(40.dp))
        Button(
            onClick = {
                navController.navigate("startFormsVerificacionCosecha/$username")
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text(text = "Verificación Observaciones")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                navController.navigate("startFormsVerificacion/$username")
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text(text = "Verificación Cosecha")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                navController.navigate("startforms/$username")
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text(text = "Enfermedades y Plagas en Fruta")
        }
        var showDialogData by remember { mutableStateOf(false) }
        Button(
            onClick = {
                // Show the dialog when the button is clicked
                showDialogMuestras = true
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            Text("Borrar Todas las Muestras")
        }
        Button(
            onClick = {
                val url = "https://drive.google.com/file/d/1WuoSP4yLLgVgZSTnZzNWayW1xHf-q_Bw/view?usp=drive_link"
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse(url)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Manual de uso aplicativo móvil",
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp))
        {
            Button(
                onClick = {
                    showDialogData = true
                    updateBlocks(viewModel) { success ->
                        showDialogData = !success
                        println(success)
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
                    .padding(top = 16.dp)
            ) { Text("Actualizar Datos") }
            var showDialogData by remember { mutableStateOf(false) }
            if (showDialogData) {
                AlertDialog(
                    onDismissRequest = {},
                    title = { Text(text = "Se están actualizando los datos. Por favor, espere...") },
                    confirmButton = {}
                )
            }

            Button(
                onClick = { showDialog = true },
                modifier = Modifier
                    .weight(1f)
                    .padding(top = 16.dp)
            ) { Text("Enviar Datos") }
        }
        val datos = viewModel.datosGuardados.filter { it["estatus"] == "enviado" }
        val datosVerificación = datos.filter { it["aplicacion"] == "verificacion" }
        val datosCosecha = datos.filter { it["aplicacion"] == "cosecha" }
        val datosEnfermedades = datos.filter { it["aplicacion"] == "enfermedades" }

        // Función para obtener la fecha del primer registro, o un mensaje si no hay datos
        fun obtenerFecha(datos: List<Map<String, Any?>>): String {
            return if (datos.isNotEmpty()) {
                datos.first()["fecha"] as? String ?: "Fecha desconocida"
            } else {
                "No se han enviado datos"
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Cantidad Datos Cargados Aplicativo Móvil",
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        Text(
            text = "Verificación Observaciones: ${datosVerificación.size}, Última Fecha: ${obtenerFecha(datosVerificación)}",
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = "Verificación Cosecha: ${datosCosecha.size}, Última Fecha: ${obtenerFecha(datosCosecha)}",
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = "Enfermedades y Plagas en Fruta: ${datosEnfermedades.size}, Última Fecha: ${obtenerFecha(datosEnfermedades)}",
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))
        Image(
            painter = painterResource(id = R.drawable.logi),
            contentDescription = null,
            modifier = Modifier.size(80.dp)
        )
        Text(
            text = "Powered by Guapa\nVersión 2.0",
            textAlign = TextAlign.Center,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}
@Composable
fun startForms(navController: NavController, viewModel: DatosGuardadosViewModel, context: Context,username: String) {
    var success: Boolean = false
    var selectedOption by remember { mutableStateOf("") }
    var sampleCountText by remember { mutableStateOf("") }
    val progress = viewModel.getProgress()
    val datos = viewModel.datosGuardados.filter { it["Origen"] != "Web" }
    val coroutineScope = rememberCoroutineScope()
    var showDialog by remember { mutableStateOf(false) }
    var showDialogNew by remember { mutableStateOf(false) }
    var showDialogMuestras by remember { mutableStateOf(false) }
    var showDialogData by remember { mutableStateOf(false) }
    if (showDialogData) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text(text = "Se están actualizando los datos. Por favor, espere...") },
            confirmButton = {}
        )
    }

    if (showDialogMuestras) {
        AlertDialog(
            onDismissRequest = {
                // Dismiss the dialog if the user clicks outside of it or presses the back button
                showDialogMuestras = false
            },
            title = {
                Text(text = "¿Está seguro de eliminar todas las muestras?")
                //TODO
                // "¿Está seguro de eliminar el bloque $bloque con fecha muestra $fecha_muestra y $cantidad de registros?"
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDialogMuestras = false
                        viewModel.borrarTodosLosDatosGuardadosNoWeb()
                    }
                ) {
                    Text(text = "Aceptar")
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        showDialogMuestras = false
                    }
                ) {
                    Text(text = "Cancelar")
                }
            }
        )
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = {
                // Dismiss the dialog if the user clicks outside of it or presses the back button
                showDialog = false
            },
            title = {
                Text(text = "¿Está seguro de subir ${datos.count()} registros?")
                //TODO
                // "¿Está seguro de eliminar el bloque $bloque con fecha muestra $fecha_muestra y $cantidad de registros?"
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDialog = false
                        showDialogNew = true
                        coroutineScope.launch {
                            sendData(viewModel = viewModel, context = context){ success ->
                                showDialogNew = !success
                                println(success)
                                if(!showDialogNew){viewModel.borrarTodosLosDatosGuardadosNoWeb()}
                            }
                        }
                    }
                ) {
                    Text(text = "Aceptar")
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        showDialog = false
                    }
                ) {
                    Text(text = "Cancelar")
                }
            }
        )
    }

    if (showDialogNew) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text(text = "Se están enviando los datos. Por favor, espere...") },
            confirmButton = {}
        )
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = {
                // Dismiss the dialog if the user clicks outside of it or presses the back button
                showDialog = false
            },
            title = {
                Text(text = "¿Está seguro de subir ${datos.count()} registros?")
                //TODO
                // "¿Está seguro de eliminar el bloque $bloque con fecha muestra $fecha_muestra y $cantidad de registros?"
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDialog = false
                        showDialogNew = true
                        coroutineScope.launch {
                            sendData(viewModel = viewModel, context = context){ success ->
                                showDialogNew = !success
                                println(success)
                                if(!showDialogNew){viewModel.borrarTodosLosDatosGuardadosNoWeb()}
                            }
                        }
                    }
                ) {
                    Text(text = "Aceptar")
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        showDialog = false
                    }
                ) {
                    Text(text = "Cancelar")
                }
            }
        )
    }

    if (showDialogNew) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text(text = "Se están enviando los datos. Por favor, espere...") },
            confirmButton = {}
        )
    }

    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .verticalScroll(scrollState)
            .fillMaxSize()
    ) {
        Spacer(modifier = Modifier.height(40.dp))
        Text(
            text = "Muestreo Organolépticas",
            fontSize = 30.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            textAlign = TextAlign.Center
        )
        val fecha_actualizacion = viewModel.obtenerDatosGuardados()
            .filter { it["Origen"] == "Web" }
            .mapNotNull { it["Fecha_Cargue"] as? String }
            .firstOrNull()
        val cantidadDatos = viewModel.obtenerDatosGuardados()
            .count { it["Origen"] == "Web" }


        Text(
            text = "Última Actualización",
            fontSize = 20.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            textAlign = TextAlign.Center
        )
        Text(
            text = "${fecha_actualizacion}",
            fontSize = 16.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            textAlign = TextAlign.Center
        )
        val otrafecha = viewModel.obtenerDatosGuardados()
            .filter { it["Origen"] != "Web" }
            .mapNotNull { (it["fecha_muestreo"] as? String)?.takeIf { it.isNotEmpty() } } // Safe casting and filtering out empty strings
            .maxOrNull()



        Text(
            text = "Fecha Última muestra : $otrafecha",
            fontSize = 16.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            textAlign = TextAlign.Center
        )
        Text(
            text = "Usuario: $username",
            fontSize = 16.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            textAlign = TextAlign.Center
        )
        var selectedBlock by remember { mutableStateOf("") }
        val blocks = viewModel.obtenerDatosGuardados()
            .filter { it["Origen"] == "Web" }
            .map { it["bloque"] as? String }
            .filterNotNull()

        TextField(
            value = selectedBlock,
            onValueChange = { selectedBlock = it },
            label = { Text("Ingrese el bloque") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 7.dp)
                .border(1.dp, Color.Gray, shape = RoundedCornerShape(4.dp)),
            singleLine = true
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 7.dp)
                .horizontalScroll(rememberScrollState())
        ) {
            Text("Bloques disponibles:", fontWeight = FontWeight.Bold)
            if (selectedBlock.isNotBlank()) {
                Row {
                    blocks.filter { it.contains(selectedBlock, ignoreCase = true) }.take(4).forEach { block ->
                        ClickableText(
                            text = AnnotatedString(block),
                            onClick = { selectedBlock = block },
                            modifier = Modifier.padding(vertical = 4.dp, horizontal = 4.dp)
                        )
                    }
                }
            }
        }

        Text(
            text = "*En caso de no tener registrado su bloque, por favor registrar dando clic en botón: Registrar Bloque",
            fontSize = 11.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            textAlign = TextAlign.Left
        )

        val i = 1
        Spacer(modifier = Modifier.height(10.dp))

        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)) {
            Button(
                onClick = {
                    val blockExists = blocks.contains(selectedBlock.trim().uppercase())
                    if (blockExists) {
                        navController.navigate("formulario/$i/${selectedBlock.trim().uppercase()}/$username")
                    } else {
                        println("Bloque no registrado. El bloque ingresado no está registrado.")
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            ) { Text("Registrar Afectaciones") }
            Button(
                onClick = {
                    selectedOption = ""
                    sampleCountText = ""
                    navController.navigate("ingresarBloque/$username")
                },
                modifier = Modifier.weight(1f)
            ) { Text("Registrar Bloque") }
        }
        Button(
            onClick = { navController.navigate("searchBlock/$username") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) { Text("Muestra por Bloque") }
        Button(
            onClick = {
                // Show the dialog when the button is clicked
                showDialogMuestras = true
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            Text("Borrar Todas las Muestras")
        }
        Spacer(modifier = Modifier.height(10.dp))
        Button(
            onClick = {
                val url = "https://drive.google.com/file/d/1WuoSP4yLLgVgZSTnZzNWayW1xHf-q_Bw/view?usp=drive_link"
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse(url)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Manual de uso aplicativo móvil",
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
        }
        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp))
        {
            Button(
                onClick = {
                    showDialogData = true
                    updateBlocks(viewModel) { success ->
                        showDialogData = !success
                        println(success)
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
                    .padding(top = 16.dp)
            ) { Text("Actualizar Datos") }

            if (showDialogData) {
                AlertDialog(
                    onDismissRequest = {},
                    title = { Text(text = "Se están actualizando los datos. Por favor, espere...") },
                    confirmButton = {}
                )
            }

            Button(
                onClick = { showDialog = true },
                modifier = Modifier
                    .weight(1f)
                    .padding(top = 16.dp)
            ) { Text("Enviar Datos") }
        }

        Spacer(modifier = Modifier.height(10.dp))
        Button(
            onClick = {
                CoroutineScope(Dispatchers.Main).launch {
                    val file = generateExcelFile(viewModel, context, aplicacion = "enfermedades")
                    file?.let {
                        val uri = FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.fileprovider",
                            file
                        )
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                            putExtra(Intent.EXTRA_STREAM, uri)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(Intent.createChooser(intent, "Enviar archivo Excel").apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        })
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Descargar Datos Guardados en Excel")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Cantidad de Datos Actualizados \n $cantidadDatos",
            fontSize = 14.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            textAlign = TextAlign.Center
        )
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            Image(
                painter = painterResource(id = R.drawable.logi),
                contentDescription = null,
                modifier = Modifier.size(80.dp)
            )
            Text(
                text = "Powered by Guapa \n Versión 2.0",
                modifier = Modifier.align(Alignment.CenterHorizontally),
                textAlign = TextAlign.Center
            )
        }
    }
}
private suspend fun loginUser(context: Context, username: String, password: String, callback: (Boolean) -> Unit) {
    withContext(Dispatchers.IO) {
        if (isNetworkAvailable(context)) {
            val client = OkHttpClient()
            val jsonMediaType = "application/json; charset=utf-8".toMediaTypeOrNull()

            val json = JSONObject().apply {
                put("nombre", username)
                put("contraseña", password)
            }

            val requestBody = json.toString().toRequestBody(jsonMediaType)
            val request = Request.Builder()
                .url("http://controlgestionguapa.ddns.net:8000/consultor/api_get_users")
                .post(requestBody)
                .build()

            try {
                val response = withContext(Dispatchers.IO) {
                    client.newCall(request).execute()
                }
                val responseBody = response.body?.string()
                val jsonResponse = responseBody?.let { JSONObject(it) }
                val message = jsonResponse?.getString("message")
                val success = message == "Aceptado"

                withContext(Dispatchers.Main) {
                    if (success) {
                        // Guardar credenciales en LocalUserStore
                        val localUserStore = LocalUserStore(context)
                        val user = LocalUserStore.User(username, password)
                        localUserStore.saveUser(user)

                        // Print statements for debugging
                        println("Saving user: $username with password: $password")
                        println("Stored users: ${localUserStore.getUsers()}")

                        Toast.makeText(context, "Login successful", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(context, message ?: "Unknown error occurred", Toast.LENGTH_LONG).show()
                    }
                    callback(success)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error connecting to server", Toast.LENGTH_LONG).show()
                    callback(false)
                }
            }
        } else {
            // Manejar autenticación offline
            val localUserStore = LocalUserStore(context)
            val user = localUserStore.getUser(username)
            val success = user?.password == password

            withContext(Dispatchers.Main) {
                if (success) {
                    println("Authenticated offline user: $username")
                    Toast.makeText(context, "Offline login successful", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context, "Usuario o contraseña incorrectos", Toast.LENGTH_LONG).show()
                }
                callback(success)
            }
        }
    }
}
private suspend fun sendData(viewModel: DatosGuardadosViewModel, context: Context, callback: (Boolean) -> Unit) {
    withContext(Dispatchers.IO) {
        val client = OkHttpClient()
        val datosGuardados = viewModel.datosGuardados.filter { it["Origen"] != "Web" }

        var datoEnviado = false // Inicialmente no se ha enviado ningún dato

        var index = 0
        while (index < datosGuardados.size) {
            val dato = datosGuardados[index]
            val fechaCompleta = dato["fecha"] as? String ?: ""
            fecha_ahora()
            val (fechaCargue, horaCargue) = if (fecha_ahora().isNotEmpty()) {
                val partes = fecha_ahora().split(" ")
                if (partes.size == 2) {
                    partes[0] to partes[1]
                } else {
                    "" to ""
                }
            } else {
                "" to ""
            }
            val (fechaSistema, horaSistema) = if (fechaCompleta.isNotEmpty()) {
                val partes = fechaCompleta.split(" ")
                if (partes.size == 2) {
                    partes[0] to partes[1]
                } else {
                    "" to ""
                }
            } else {
                "" to ""
            }
            try {
                val jsonMediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
                val requestBodys = JsonObject().apply {
                    addProperty("bloque", dato["bloque"] as? String ?: "")
                    addProperty("grupo_forza", dato["grupo_forza"] as? String ?: "")
                    addProperty("bin", dato["bin"] as? String ?: "")
                    addProperty("color_bin", dato["color_bin"] as? String ?: "")
                    addProperty("peso", dato["peso"] as? String ?: "")
                    addProperty("categoria", dato["categoria"] as? String ?: "")
                    addProperty("mlsodio", dato["mlsodio"] as? String ?: "")
                    addProperty("dosisprimerpase", dato["dosisprimerpase"] as? String ?: "")
                    addProperty("mejorador", dato["mejorador"] as? String ?: "")
                    addProperty("brix", dato["brix"] as? String ?: "")
                    addProperty("color", dato["color"] as? String ?: "")
                    addProperty("primerpase", dato["primerpase"] as? String ?: "")
                    addProperty("avance", dato["avance"] as? String ?: "")
                    addProperty("afectaciones", dato["afectaciones"] as? String ?: "")
                    addProperty("observaciones", dato["observaciones"] as? String ?: "")
                    addProperty("fecha_sistema", fechaSistema)
                    addProperty("hora_sistema", horaSistema)
                    addProperty("fecha_cargue", fechaCargue)
                    addProperty("hora_cargue", horaCargue)
                    addProperty("fecha_muestreo", dato["fecha_muestreo"] as? String ?: "")
                }
                val requestBody = requestBodys.toString().toRequestBody(jsonMediaType)
                val request = Request.Builder()
                    .url("http://controlgestionguapa.ddns.net:8000/consultor/api_uploadorganolepticas")
                    .post(requestBody)
                    .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()
                val responseString = responseBody?.takeIf { it.isNotEmpty() }

                if (!responseString.isNullOrEmpty()) {
                    datoEnviado = true
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Datos enviados correctamente", Toast.LENGTH_LONG).show()
                    }

                    val nuevoDato = mapOf(
                        "estatus" to "enviado",
                        "fecha" to fecha_ahora() // Asegúrate de tener la función fecha_ahora implementada correctamente
                    )
                    viewModel.agregarDato(nuevoDato)

                    index++
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Error al enviar datos. Contactar con Estrategia de datos", Toast.LENGTH_LONG).show()
                    }
                    break // Salir del bucle si hay un error en el envío
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error al enviar datos. Contactar con Estrategia de datos", Toast.LENGTH_LONG).show()
                }
                break // Salir del bucle si hay una excepción
            }
        }

        // Llamar al callback para indicar si todos los datos se enviaron correctamente
        callback(datoEnviado)
    }
}
fun updateBlocks(viewModel: DatosGuardadosViewModel, callback: (Boolean) -> Unit) {
    viewModel.borrarTodosLosDatosGuardados()
    val client = OkHttpClient.Builder()
        .callTimeout(200, TimeUnit.SECONDS)
        .build()

    val request = Request.Builder()
        .url("http://controlgestionguapa.ddns.net:8000/consultor/api_get_blocks")
        .build()

    var success = false // Booleano para indicar el éxito de la operación

    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            e.printStackTrace()

            // Ocultar la barra de progreso en caso de fallo
            viewModel.setProgressVisible(false)
            callback(false) // Llamamos a la función de devolución de llamada con false
        }

        override fun onResponse(call: Call, response: Response) {
            response.use {
                if (!response.isSuccessful) throw IOException("Unexpected code $response")
                val responseString = response.body?.string()
                val cleanedResponseString = responseString?.replace("NaN", "\" \"") // Reemplazar NaN por cadena vacía
                val jsonArray = cleanedResponseString?.let { JSONArray(it) } ?: JSONArray()
                var i = 0
                var saveSuccessful = true
                while (i < jsonArray.length() && saveSuccessful) {
                    val elemento = jsonArray.getJSONObject(i)
                    saveSuccessful = saveDataWeb(elemento = elemento, viewModel = viewModel)
                    i++
                    println("Holi $i")
                }
                success = saveSuccessful // Establecer success basado en si todos los datos se guardaron correctamente
                callback(success) // Llamamos a la función de devolución de llamada con el valor final de success
            }
        }
    })
}
fun saveDataWeb(elemento: JSONObject, viewModel: DatosGuardadosViewModel): Boolean {
    return try {
        val nuevoDato = mapOf(
            "Fecha_Cargue" to fecha_ahora(),
            "Origen" to "Web",
            "bloque" to elemento.optString("bloque", ""),
            "area" to elemento.optString("area", ""),
            "fecha_siembra" to elemento.optString("fecha_siembra", ""),
            "grupo_forza" to elemento.optString("grupo_forza", ""),
            "poblacion" to elemento.optString("poblacion", ""),
            "total_frutas" to elemento.optString("total_frutas", "")
        )
        viewModel.agregarDato(nuevoDato)
        true // Retorna true si se guardó exitosamente
    } catch (e: Exception) {
        println("Fallo: $e")
        false // Retorna false si ocurrió un fallo al guardar
    }
}
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun formulario(
    bloque: String,
    viewModel: DatosGuardadosViewModel,
    navController: NavController,
    username: String
): Boolean {
    val cal = Calendar.getInstance()
    val añoActual = cal.get(Calendar.YEAR).toString()
    val mesActual = (cal.get(Calendar.MONTH) + 1).toString() // Se suma 1 porque los meses van de 0 a 11
    val diaActual = cal.get(Calendar.DAY_OF_MONTH).toString()
    var nuevoAnio by remember { mutableStateOf(añoActual) }
    var nuevoDia by remember { mutableStateOf(diaActual) }
    var nuevoMes by remember { mutableStateOf(mesActual) }
    val muestreosActuales = viewModel.obtenerDatosGuardados().filter { it["Origen"] != "Web" }
    val muestreoActual = muestreosActuales.count { it["bloque"] == bloque }.toInt() + 1
    var guardadoExitoso = false
    var conteoLeveCochinilla by remember { mutableStateOf(0) }
    var conteoModeradoCochinilla by remember { mutableStateOf(0) }
    var conteoSeveroCochinilla by remember { mutableStateOf(0) }

    var bin by remember { mutableStateOf("") }
    var peso by remember { mutableStateOf("") }
    var nuevaenfermedadSelected by remember { mutableStateOf("") }
    var categoriaSelected by remember { mutableStateOf("") }
    var dropdownCategoriaExpanded = remember { mutableStateOf(false) }
    val CategoriaOptions = listOf("Especial","Industria","Jugos","Premium","")
    var mlsodio by remember { mutableStateOf("") }
    var dosisPrimerPase by remember { mutableStateOf("") }
    var brix by remember { mutableStateOf("") }

    var colorSelected by remember { mutableStateOf("") }
    var dropdownColorExpanded = remember { mutableStateOf(false) }
    val ColorOptions = listOf("<0.5","0.5","1.0","1.5","2.0","2.5",">=3.0","")
    var colorBinSelected by remember { mutableStateOf("") }
    var dropdownColorBinExpanded = remember { mutableStateOf(false) }
    val ColorBinOptions = listOf("Amarillo","Verde","")
    var primerSelected by remember { mutableStateOf("") }
    var dropdownPrimerPaseExpanded = remember { mutableStateOf(false) }
    val PrimerPase = listOf("Dron","Spray Boom","")
    var mejoradorSelected by remember { mutableStateOf("") }
    var dropdownMejoradorExpanded = remember { mutableStateOf(false) }
    val mejorador = listOf("Dron","Spray Boom","")
    var avanceSelected by remember { mutableStateOf("") }
    var dropdownAvanceExpanded = remember { mutableStateOf(false) }
    val Avance = listOf("0%","10%","20%","30%","40%","50%","60%","70%","80%","90%","100%","")
    val afectacionSelected = remember { mutableStateListOf<String>() }
    val dropdownAfectacionesExpanded = remember { mutableStateOf(false) }
    val Afectaciones = listOf(
        // Afectación Externa
        "Base café",
        "Cicatriz (cáscara)",
        "Cochinilla Externa",
        "Cónica",
        "Corona grande",
        "Corona maltratada",
        "Corona multiple",
        "Corona pequeña",
        "Corona quemada",
        "Corona torcida",
        "Corchosis",
        "Cuello",
        "Daño animales",
        "Daño insecto",
        "Daño mecanico",
        "Deforme",
        "Desbracteo",
        "Fruta con golpe",
        "Fruta sucia",
        "Gomosis",
        "Off Color",
        "Pedunculo viejo",
        "Sobremadura",

        // Ninguno
        "Sin defecto",

        // Afectación Interna
        "Cochinilla interna",
        "Enferma erwinia",
        "Enferma phytophthora",
        "Golpe de agua leve",
        "Golpe de agua severo",
        "Gomosis interna",
        "Levadura",
        "Quema de sol leve",
        "Quema de sol severa",
        "Thielaviopsis"
    )


    val grupo = viewModel.obtenerDatosGuardados()
        .filter { it["Origen"] == "Web" } // Filtrar por origen web
        .firstOrNull { it["bloque"] == bloque } // Obtener el primer elemento con el bloque deseado
        ?.get("grupo_forza") as? String
    val poblacion: Double = viewModel.obtenerDatosGuardados()
        .filter { it["Origen"] == "Web" } // Filtrar por origen "Web"
        .firstOrNull { it["bloque"] == bloque } // Obtener el primer elemento con el bloque deseado
        ?.get("poblacion")?.toString()?.toDoubleOrNull() ?: 0.0 // Convertir a String y luego a Double
    val cosechada: Double = viewModel.obtenerDatosGuardados()
        .filter { it["Origen"] == "Web" } // Filtrar por origen "Web"
        .firstOrNull { it["bloque"] == bloque } // Obtener el primer elemento con el bloque deseado
        ?.get("total_frutas")?.toString()?.toDoubleOrNull() ?: 0.0
    val poda = viewModel.obtenerDatosGuardados()
        .filter { it["Origen"] == "Web" } // Filtrar por origen web
        .firstOrNull { it["bloque"] == bloque } // Obtener el primer elemento con el bloque deseado
        ?.get("fecha_siembra") as? String
    val area: Double = viewModel.obtenerDatosGuardados()
        .filter { it["Origen"] == "Web" } // Filtrar por origen "Web"
        .firstOrNull { it["bloque"] == bloque } // Obtener el primer elemento con el bloque deseado
        ?.get("area")?.toString()?.toDoubleOrNull() ?: 0.0
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .background(Color.White)
    ) {
        // Sección de "Registrar Hallazgos"
        Text(
            text = "Registrar Muestra",
            fontSize = 30.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Bloque : $bloque",
            fontSize = 15.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            textAlign = TextAlign.Center
        )
        Text(
            text = "Grupo Forza : $grupo",
            fontSize = 15.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            textAlign = TextAlign.Center
        )
        Text(
            text = "Fecha Siembra/Poda : $poda",
            fontSize = 15.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            textAlign = TextAlign.Center
        )
        Text(
            text = "Área Neta : ${formatNumberWithCustomSeparators(area)} [Ha]",
            fontSize = 15.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            textAlign = TextAlign.Center
        )
        Text(
            text = "Población : ${formatNumberWithCustomSeparators2(poblacion)}",
            fontSize = 15.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            textAlign = TextAlign.Center
        )
        Text(
            text = "Fruta Cosechada : ${formatNumberWithCustomSeparators2(cosechada)}",
            fontSize = 15.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Muestra Actual : $muestreoActual",
            fontSize = 15.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = peso,
            onValueChange = { peso = it },
            label = { Text("Peso [gramos] : ") },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .focusRequester(focusRequester)
            ,
            keyboardActions = KeyboardActions(
                onDone = {
                    focusManager.clearFocus() // Clear focus to hide the keyboard
                }
            )
        )
        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = bin,
            onValueChange = {
                // Check if the input is a valid number
                val number = it.toIntOrNull()
                if (number != null && number in 0..200) {
                    bin = it
                }
            },
            label = { Text("Bin : ") },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .focusRequester(focusRequester),
            keyboardActions = KeyboardActions(
                onDone = {
                    focusManager.clearFocus() // Clear focus to hide the keyboard
                }
            )
        )
        Spacer(modifier = Modifier.height(20.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .background(color = Color.White, shape = RoundedCornerShape(4.dp))
                .border(1.dp, Color.Gray, shape = RoundedCornerShape(4.dp))
                .height(52.dp)
                .clickable {
                    dropdownColorBinExpanded.value = true
                    focusManager.clearFocus()
                }
        ) {
            Text(
                text = "Color bin: $colorBinSelected",
                modifier = Modifier
                    .padding(start = 16.dp, top = 16.dp)
                    .fillMaxWidth()
            )
            DropdownMenu(
                expanded = dropdownColorBinExpanded.value,
                onDismissRequest = { dropdownColorBinExpanded.value = false },
                modifier = Modifier.fillMaxWidth()
            ) {
                ColorBinOptions.forEach { option ->
                    DropdownMenuItem(
                        onClick = {
                            colorBinSelected = option
                            dropdownColorBinExpanded.value = false
                        }
                    ) {
                        Text(text = option, fontSize = 25.sp)
                    }
                }
            }
        }


        Spacer(modifier = Modifier.height(20.dp))
        OutlinedTextField(
            value = mlsodio,
            onValueChange = { mlsodio = it },
            label = { Text("ml Hidroxido de Sodio : ") },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .focusRequester(focusRequester)
            ,
            keyboardActions = KeyboardActions(
                onDone = {
                    focusManager.clearFocus() // Clear focus to hide the keyboard
                }
            )
        )
        Spacer(modifier = Modifier.height(20.dp))
        OutlinedTextField(
            value = brix,
            onValueChange = { brix = it },
            label = { Text("Grados Brix : ") },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .focusRequester(focusRequester)
            ,
            keyboardActions = KeyboardActions(
                onDone = {
                    focusManager.clearFocus() // Clear focus to hide the keyboard
                }
            )
        )
        Spacer(modifier = Modifier.height(20.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .background(color = Color.White, shape = RoundedCornerShape(4.dp))
                .border(1.dp, Color.Gray, shape = RoundedCornerShape(4.dp))
                .height(52.dp)
                .clickable {
                    dropdownCategoriaExpanded.value = true
                    focusManager.clearFocus()
                }
        ) {
            Text(
                text = "Categoria: $categoriaSelected",
                modifier = Modifier
                    .padding(start = 16.dp, top = 16.dp)
                    .fillMaxWidth().clickable(onClick = {
                        dropdownCategoriaExpanded.value = true
                    })
            )
            DropdownMenu(
                expanded = dropdownCategoriaExpanded.value,
                onDismissRequest = { dropdownCategoriaExpanded.value = false },
                modifier = Modifier.fillMaxWidth()
            ) {
                CategoriaOptions.forEach { option ->
                    DropdownMenuItem(
                        onClick = {
                            categoriaSelected = option
                            dropdownCategoriaExpanded.value = false
                        },
                        modifier = Modifier.fillMaxWidth().clickable {
                            categoriaSelected = option
                            dropdownCategoriaExpanded.value = false
                        }
                    ) {
                        Text(
                            text = option,
                            fontSize = 25.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .background(color = Color.White, shape = RoundedCornerShape(4.dp))
                .border(1.dp, Color.Gray, shape = RoundedCornerShape(4.dp))
                .height(52.dp)
                .clickable {
                    dropdownColorExpanded.value = true
                    focusManager.clearFocus()
                }
        ) {
            Text(
                text = "Color Externo: $colorSelected",
                modifier = Modifier
                    .padding(start = 16.dp, top = 16.dp)
                    .fillMaxWidth().clickable(onClick = {
                        dropdownColorExpanded.value = true
                    })
            )
            DropdownMenu(
                expanded = dropdownColorExpanded.value,
                onDismissRequest = { dropdownColorExpanded.value = false },
                modifier = Modifier.fillMaxWidth()
            ) {
                ColorOptions.forEach { option ->
                    DropdownMenuItem(
                        onClick = {
                            colorSelected = option
                            dropdownColorExpanded.value = false
                        },
                        modifier = Modifier.fillMaxWidth().clickable {
                            colorSelected = option
                            dropdownColorExpanded.value = false
                        }
                    ) {
                        Text(
                            text = option,
                            fontSize = 25.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .background(color = Color.White, shape = RoundedCornerShape(4.dp))
                .border(1.dp, Color.Gray, shape = RoundedCornerShape(4.dp))
                .height(52.dp)
                .clickable {
                    dropdownPrimerPaseExpanded.value = true
                    focusManager.clearFocus()
                }
        ) {
            Text(
                text = "Primer pase: $primerSelected",
                modifier = Modifier
                    .padding(start = 16.dp, top = 16.dp)
                    .fillMaxWidth().clickable(onClick = {
                        dropdownPrimerPaseExpanded.value = true
                    })
            )
            DropdownMenu(
                expanded = dropdownPrimerPaseExpanded.value,
                onDismissRequest = { dropdownPrimerPaseExpanded.value = false },
                modifier = Modifier.fillMaxWidth()
            ) {
                PrimerPase.forEach { option ->
                    DropdownMenuItem(
                        onClick = {
                            primerSelected = option
                            dropdownPrimerPaseExpanded.value = false
                        },
                        modifier = Modifier.fillMaxWidth().clickable {
                            primerSelected = option
                            dropdownPrimerPaseExpanded.value = false
                        }
                    ) {
                        Text(
                            text = option,
                            fontSize = 25.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        OutlinedTextField(
            value = dosisPrimerPase,
            onValueChange = { dosisPrimerPase = it },
            label = { Text("Dosis primer pase : ") },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .focusRequester(focusRequester)
            ,
            keyboardActions = KeyboardActions(
                onDone = {
                    focusManager.clearFocus() // Clear focus to hide the keyboard
                }
            )
        )
        Spacer(modifier = Modifier.height(20.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .background(color = Color.White, shape = RoundedCornerShape(4.dp))
                .border(1.dp, Color.Gray, shape = RoundedCornerShape(4.dp))
                .height(52.dp)
                .clickable {
                    dropdownMejoradorExpanded.value = true
                    focusManager.clearFocus()
                }
        ) {
            Text(
                text = "Mejorador: $mejoradorSelected",
                modifier = Modifier
                    .padding(start = 16.dp, top = 16.dp)
                    .fillMaxWidth().clickable(onClick = {
                        dropdownMejoradorExpanded.value = true
                    })
            )
            DropdownMenu(
                expanded = dropdownMejoradorExpanded.value,
                onDismissRequest = { dropdownMejoradorExpanded.value = false },
                modifier = Modifier.fillMaxWidth()
            ) {
                mejorador.forEach { option ->
                    DropdownMenuItem(
                        onClick = {
                            mejoradorSelected = option
                            dropdownMejoradorExpanded.value = false
                        },
                        modifier = Modifier.fillMaxWidth().clickable {
                            mejoradorSelected = option
                            dropdownMejoradorExpanded.value = false
                        }
                    ) {
                        Text(
                            text = option,
                            fontSize = 25.sp
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .background(color = Color.White, shape = RoundedCornerShape(4.dp))
                .border(1.dp, Color.Gray, shape = RoundedCornerShape(4.dp))
                .height(52.dp)
                .clickable {
                    dropdownAvanceExpanded.value = true
                    focusManager.clearFocus()
                }
        ) {
            Text(
                text = "Porcentaje Avance Translucidez: $avanceSelected",
                modifier = Modifier
                    .padding(start = 16.dp, top = 16.dp)
                    .fillMaxWidth().clickable(onClick = {
                        dropdownAvanceExpanded.value = true
                    })
            )
            DropdownMenu(
                expanded = dropdownAvanceExpanded.value,
                onDismissRequest = { dropdownAvanceExpanded.value = false },
                modifier = Modifier.fillMaxWidth()
            ) {
                Avance.forEach { option ->
                    DropdownMenuItem(
                        onClick = {
                            avanceSelected = option
                            dropdownAvanceExpanded.value = false
                        },
                        modifier = Modifier.fillMaxWidth().clickable {
                            avanceSelected = option
                            dropdownAvanceExpanded.value = false
                        }
                    ) {
                        Text(
                            text = option,
                            fontSize = 25.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .background(color = Color.White, shape = RoundedCornerShape(4.dp))
                .border(1.dp, Color.Gray, shape = RoundedCornerShape(4.dp))
                .height(52.dp)
                .clickable {
                    dropdownAfectacionesExpanded.value = true
                    focusManager.clearFocus()
                }
        ) {
            Text(
                text = "Afectaciones: ${afectacionSelected.joinToString(", ")}",
                modifier = Modifier
                    .padding(start = 16.dp, top = 16.dp)
                    .fillMaxWidth()
                    .clickable(onClick = {
                        dropdownAfectacionesExpanded.value = true
                    })
            )
            DropdownMenu(
                expanded = dropdownAfectacionesExpanded.value,
                onDismissRequest = { dropdownAfectacionesExpanded.value = false },
                modifier = Modifier.fillMaxWidth()
            ) {
                Afectaciones.forEach { option ->
                    val textColor = when (option) {
                        "Base café", "Cicatriz (cáscara)", "Cochinilla Externa", "Cónica", "Corona grande",
                        "Corona maltratada", "Corona pequeña", "Corona quemada", "Corona torcida",
                        "Daño animales", "Daño insecto", "Deforme", "Desbracteo", "Fruta con golpe",
                        "Gomosis", "Sobremadura", "Corona multiple", "Off Color",
                        "Corchosis", "Cuello", "Daño mecanico", "Fruta sucia", "Pedunculo viejo" -> Color.Red

                        "Golpe de agua leve", "Golpe de agua severo", "Gomosis interna", "Levadura",
                        "Enferma erwinia", "Quema de sol severa", "Quema de sol leve", "Cochinilla interna",
                        "Enferma phytophthora", "Thielaviopsis" -> Color.Blue

                        "Ninguno", "Sin defecto" -> Color.Green

                        else -> Color.Black
                    }
                    DropdownMenuItem(
                        onClick = {
                            if (afectacionSelected.contains(option)) {
                                afectacionSelected.remove(option)
                            } else {
                                afectacionSelected.add(option)
                            }
                            dropdownAfectacionesExpanded.value = false // Cierra el menú después de la selección
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = option,
                            fontSize = 25.sp,
                            color = textColor
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "Fecha Muestra Actual:",
            fontSize = 15.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Campo para el año (4 dígitos)
            OutlinedTextField(
                value = nuevoAnio,
                onValueChange = { nuevoAnio = it.take(4) }, // Limitar a 4 dígitos
                label = { Text("Año") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
            )

            // Campo para el mes (2 dígitos)
            OutlinedTextField(
                value = nuevoMes,
                onValueChange = {
                    val nuevoValor = it.take(2).toIntOrNull() // Convertir a Int
                    nuevoMes = when {
                        nuevoValor == null -> "" // Si la conversión falla, establecer el valor como vacío
                        nuevoValor < 1 -> "01" // Si es menor que 1, establecer como "01"
                        nuevoValor > 12 -> "12" // Si es mayor que 12, establecer como "12"
                        else -> "%02d".format(nuevoValor) // Formatear como dos dígitos con ceros a la izquierda
                    }
                },
                label = { Text("Mes") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
            )


            // Campo para el día (2 dígitos)
            OutlinedTextField(
                value = nuevoDia,
                onValueChange = {
                    val nuevoValor = it.take(2).toIntOrNull() // Convertir a Int
                    nuevoDia = when {
                        nuevoValor == null -> "" // Si la conversión falla, establecer el valor como vacío
                        nuevoValor < 1 -> "01" // Si es menor que 1, establecer como "01"
                        nuevoValor > 31 -> "31" // Si es mayor que 31, establecer como "31"
                        else -> "%02d".format(nuevoValor) // Formatear como dos dígitos con ceros a la izquierda
                    }
                },
                label = { Text("Día") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
            )

        }


        var nuevaFecha = "$nuevoAnio-$nuevoMes-$nuevoDia"

        Spacer(modifier = Modifier.height(20.dp))
        OutlinedTextField(
            value = nuevaenfermedadSelected,
            onValueChange = { nuevaenfermedadSelected = it }, // Actualización de la variable
            label = { Text("Otras novedades y observaciones adicionales") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Fecha Sistema",
            fontSize = 20.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            textAlign = TextAlign.Center
        )
        Text(
            text = "${fecha_ahora()}",
            fontSize = 16.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        // Botones de guardar y volver
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ) {
            val grupo = viewModel.obtenerDatosGuardados()
                .filter { it["Origen"] == "Web" } // Filtrar por origen web
                .firstOrNull { it["bloque"] == bloque } // Obtener el primer elemento con el bloque deseado
                ?.get("grupo_forza") as? String ?: ""
            Button(
                onClick = {
                    guardadoExitoso = guardar(
                        bloque = bloque,
                        grupo_forza = grupo,
                        bin = bin,
                        color_bin = colorBinSelected,
                        peso = peso,
                        categoria = categoriaSelected,
                        mlsodio = mlsodio,
                        dosisprimerpase = dosisPrimerPase,
                        mejorador = mejoradorSelected,
                        brix = brix,
                        color = colorSelected,
                        primerPase = primerSelected,
                        avance = avanceSelected,
                        afectaciones = afectacionSelected,
                        fecha = fecha_ahora(),
                        fecha_muestreo = nuevaFecha,
                        viewModel = viewModel,
                        nuevaenfermedad = nuevaenfermedadSelected,
                        usuario = username,
                    )
                    if (guardadoExitoso) {
                        bin = ""
                        colorBinSelected = ""
                        peso = ""
                        categoriaSelected = ""
                        mlsodio = ""
                        dosisPrimerPase = ""
                        mejoradorSelected = ""
                        brix = ""
                        colorSelected = ""
                        primerSelected = ""
                        avanceSelected = ""
                        afectacionSelected.clear() // Limpia la lista de afectaciones seleccionadas
                        nuevaenfermedadSelected = ""

                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                Text(text = "Guardar")
            }
            Button(
                onClick = {
                    navController.navigate("startforms/$username")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                Text("Volver a la pantalla principal")
            }
            Spacer(modifier = Modifier.height(16.dp))
            Image(
                painter = painterResource(id = R.drawable.logi),
                contentDescription = null,
                modifier = Modifier.size(80.dp)
            )
            Text(
                text = "Powered by Guapa \n Versión 1.0",
                modifier = Modifier.align(Alignment.CenterHorizontally),
                textAlign = TextAlign.Center
            )
        }
    }
    return true
}
fun isNetworkAvailable(context: Context): Boolean {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = connectivityManager.activeNetwork ?: return false
    val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
    return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
}
private fun guardar(
    bloque: String,
    grupo_forza: String,
    bin: String,
    color_bin: String,
    peso: String,
    categoria: String,
    mlsodio: String,
    dosisprimerpase: String,
    mejorador: String,
    brix: String,
    color: String,
    primerPase: String,
    avance: String,
    afectaciones: List<String>,  // Cambiado a List<String>
    fecha: String,
    fecha_muestreo: String,
    viewModel: DatosGuardadosViewModel,
    nuevaenfermedad: String,
    usuario: String,
): Boolean {
    return try {
        val nuevoDato = mapOf(
            "bloque" to bloque,
            "grupo_forza" to grupo_forza,
            "bin" to bin,
            "color_bin" to color_bin,
            "peso" to peso,
            "categoria" to categoria,
            "mlsodio" to mlsodio,
            "dosisprimerpase" to dosisprimerpase,
            "mejorador" to mejorador,
            "brix" to brix,
            "color" to color,
            "primerpase" to primerPase,
            "avance" to avance,
            "afectaciones" to afectaciones.joinToString(", "),  // Convertir la lista a una cadena separada por comas
            "fecha" to fecha,
            "observaciones" to nuevaenfermedad,
            "fecha_muestreo" to fecha_muestreo,
            "usuario" to usuario
        )
        viewModel.agregarDato(nuevoDato)
        println("Exitoso")
        println("Valores guardados: $nuevoDato")
        true // Retorna true si se guardó exitosamente
    } catch (e: Exception) {
        println("Fallo: $e")
        false // Retorna false si ocurrió un fallo al guardar
    }
}

suspend fun generateExcelFile(viewModel: DatosGuardadosViewModel, context: Context, aplicacion: String): File? {
    return withContext(Dispatchers.IO) {
        try {
            val workbook = XSSFWorkbook()
            val sheet = workbook.createSheet("Datos Guardados")

            val headers = arrayOf(
                "bloque", "grupo_forza", "bin", "color_bin",
                "peso", "categoria", "mlsodio", "dosisprimerpase",
                "brix", "color", "primerpase","mejorador", "avance",
                "afectaciones", "fecha", "observaciones", "fecha_muestreo"
            )


            val headerRow = sheet.createRow(0)
            headers.forEachIndexed { index, header ->
                headerRow.createCell(index).setCellValue(header)
            }
            val datosGuardados = viewModel.datosGuardados.filter { it["Origen"] != "Web" }


            datosGuardados.forEachIndexed { rowIndex, dato ->
                val row = sheet.createRow(rowIndex + 1)
                headers.forEachIndexed { index, header ->
                    row.createCell(index).setCellValue(dato[header] as? String ?: "")
                }
            }
            val file = File(
                context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
                "DatosGuardados.xlsx"
            )
            FileOutputStream(file).use { outputStream ->
                workbook.write(outputStream)
            }
            workbook.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Error al generar el archivo Excel", Toast.LENGTH_LONG).show()
            }
            null
        }
    }
}
private fun fecha_ahora(): String {
    val currentDateTime = LocalDateTime.now()
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    return currentDateTime.format(formatter)
}
@Composable
fun DropdownMenuItem(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentPadding: PaddingValues = MenuDefaults.DropdownMenuItemContentPadding,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable RowScope.() -> Unit
) {
    Surface(
        modifier = modifier
            .clickable(
                onClick = onClick,
                enabled = enabled,
                interactionSource = interactionSource,
                indication = null
            )
            .then(modifier)
    ) {
        CompositionLocalProvider(LocalContentAlpha provides if (enabled) ContentAlpha.high else ContentAlpha.disabled) {
            Row(
                modifier = Modifier.padding(contentPadding)
            ) {
                ProvideTextStyle(TextStyle.Default) {
                    content()
                }
            }
        }
    }
}


@SuppressLint("UnusedBoxWithConstraintsScope", "RememberReturnType")
@Composable
private fun searchBlock(
    viewModel: DatosGuardadosViewModel,
    navController: NavController,
    context: Context,
    username: String
) {
    val datosGuardados = viewModel.obtenerDatosGuardados()
    val filteredData = datosGuardados.filter { it["Origen"] == "Web" }

    var selectedGrupoForza by remember { mutableStateOf<String?>(null) }
    var selectedBloque by remember { mutableStateOf<String?>(null) }
    var selectedOrigen by remember { mutableStateOf<String?>(null) }

    val gruposForza = filteredData.mapNotNull { it["grupo_forza"] as? String }.distinct().takeIf { it.isNotEmpty() } ?: listOf("")
    val bloques = filteredData.mapNotNull { it["bloque"] as? String }.distinct().takeIf { it.isNotEmpty() } ?: listOf("")
    val origenes = listOf("Web", "Aplicativo", "Ambos")

    // Datos de ejemplo para la gráfica de barras
    val afectaciones = listOf(
        "Base café", "Cicatriz (cáscara)", "Cochinilla Externa", "Cónica",
        "Base café", "Corona grande", "Corona maltratada", "Cicatriz (cáscara)",
        "Base café", "Corona pequeña", "Cochinilla Externa", "Corona quemada",
        "Base café", "Cicatriz (cáscara)", "Cochinilla Externa", "Corona torcida"
    )

    val topAfectaciones = afectaciones.groupingBy { it }.eachCount()
        .entries.sortedByDescending { it.value }
        .take(5)
// Filter the data based on the given conditions
// Filter the data based on "Origen" and "motivo"

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .verticalScroll(scrollState)
                .fillMaxSize()
        ) {
            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(fontSize = 30.sp, fontWeight = FontWeight.Bold)) {
                        append("Análisis Organolépticas\n")
                    }
                    withStyle(style = SpanStyle(fontSize = 16.sp)) { // Tamaño menor para los detalles
                        append("Bloque: ")
                        append(selectedBloque ?: "No seleccionado")
                        append("\n")
                        append("Grupo Forza: ")
                        append(selectedGrupoForza ?: "No seleccionado")
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                textAlign = TextAlign.Center
            )

            Button(
                onClick = {
                    navController.navigate("startForms/$username")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                Text("Volver a la pantalla principal")
            }

            Spacer(modifier = Modifier.height(16.dp))


            // Row to place filters side by side
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Autocomplete filter for grupo_forza
                AutocompleteFilter(
                    label = "Grupo Forza",
                    options = gruposForza,
                    selectedOption = selectedGrupoForza,
                    onOptionSelected = { option -> selectedGrupoForza = option },
                    modifier = Modifier.weight(1f)
                )

                // Autocomplete filter for bloque
                AutocompleteFilter(
                    label = "Bloque",
                    options = bloques,
                    selectedOption = selectedBloque,
                    onOptionSelected = { option -> selectedBloque = option },
                    modifier = Modifier.weight(1f)
                )

            }

            val filteredDataAnalysis = datosGuardados.filter {
                it["Origen"] != "Web"
            }


            val colors = filteredDataAnalysis.mapNotNull {
                val color = it["color"]
                if (color is String) color else null
            }

// Compute the most repeated color
            val mostRepeatedColor = colors.groupingBy { it }.eachCount().maxByOrNull { it.value }?.key ?: "No Color Found"
// Extraer las afectaciones de filteredDataAnalysis
            val afectaciones = filteredDataAnalysis.mapNotNull {
                val afectacion = it["afectaciones"]
                if (afectacion is String) afectacion else null
            }.flatMap { it.split(",").map { it.trim() } }



// Calcular la afectación más repetida
            val mostRepeatedAfectacion = afectaciones.groupingBy { it }.eachCount().maxByOrNull { it.value }?.key ?: "No Afectación Found"

// Calculate the total number of colors
            val totalColors = colors.size

// Calculate the count of the most repeated color
            val mostRepeatedColorCount = colors.count { it == mostRepeatedColor }

// Calculate the percentage of the most repeated color
            val percentColor = if (totalColors > 0) {
                (mostRepeatedColorCount.toDouble() / totalColors) * 100
            } else {
                0.0
            }

// Process translucidez values
            val translucidezValues = filteredDataAnalysis.mapNotNull {
                val translucidez = it["avance"] as? String
                translucidez?.replace("%", "")?.toDoubleOrNull()
            }

            val averageTranslucidez = if (translucidezValues.isNotEmpty()) {
                translucidezValues.average()
            } else {
                0.0
            }
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .align(Alignment.CenterHorizontally),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Cantidad de Registros",
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${filteredDataAnalysis.size}",
                            textAlign = TextAlign.Center,
                            fontSize = 20.sp, // Tamaño de letra para el valor
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .align(Alignment.CenterHorizontally),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Color más repetido",
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = mostRepeatedColor,
                            textAlign = TextAlign.Center,
                            fontSize = 20.sp, // Tamaño de letra para el valor
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "("+String.format("%.2f", percentColor)+"%)", // Ajusta el porcentaje según tus datos
                            textAlign = TextAlign.Center,
                            fontSize = 20.sp, // Tamaño de letra para el porcentaje
                            fontWeight = FontWeight.Normal
                        )
                    }
                }
            }

// Tarjeta para la afectación más repetida
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .align(Alignment.CenterHorizontally),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Afectación más repetida",
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = mostRepeatedAfectacion ?: "No hay datos",
                            textAlign = TextAlign.Center,
                            fontSize = 20.sp, // Tamaño de letra para el valor
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "(25%)", // Ajusta el porcentaje según tus datos
                            textAlign = TextAlign.Center,
                            fontSize = 20.sp, // Tamaño de letra para el porcentaje
                            fontWeight = FontWeight.Normal
                        )
                    }
                }
            }
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .align(Alignment.CenterHorizontally),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Promedio de Afectaciones",
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${afectaciones.size/filteredDataAnalysis.size}" ?: "No hay datos",
                            textAlign = TextAlign.Center,
                            fontSize = 20.sp, // Tamaño de letra para el valor
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "(25%)", // Ajusta el porcentaje según tus datos
                            textAlign = TextAlign.Center,
                            fontSize = 20.sp, // Tamaño de letra para el porcentaje
                            fontWeight = FontWeight.Normal
                        )
                    }
                }
            }

// Tarjeta para el promedio de porcentaje translucidez
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .align(Alignment.CenterHorizontally),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Promedio de porcentaje de translucidez",
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = String.format("%.0f", averageTranslucidez)+"%",
                        textAlign = TextAlign.Center,
                        fontSize = 20.sp, // Tamaño de letra para el valor
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            val promediosPorFecha = filteredDataAnalysis
                .mapNotNull {
                    val fechaMuestra = (it["fecha_hora"] as? String)?.let { dateToMillis(it) } ?: return@mapNotNull null
                    val gradosBrix = (it["avg_brix"] as? String)?.toFloatOrNull() ?: return@mapNotNull null
                    Pair(fechaMuestra, gradosBrix)
                }
                .groupBy { it.first }
                .mapValues { (_, valores) ->
                    val promedio = valores.map { it.second }.average()
                    String.format("%.2f", promedio) // Formatear a 2 decimales
                }

            val exampleData: List<Map<String, Any>> = if (promediosPorFecha.isNotEmpty()) {
                promediosPorFecha.map { (fecha, promedio) ->
                    mapOf("fecha_muestra" to fecha, "promedio_grados_brix" to promedio)
                }
            } else {
                // Genera datos predeterminados si no hay resultados después del filtrado
                listOf(mapOf("fecha_muestra" to 0L, "promedio_grados_brix" to "0.00"))
            }

// Ahora `exampleData` siempre tendrá datos, ya sea los calculados o los predeterminados
            println(exampleData)



            val brixEntries = remember {
                exampleData.mapNotNull { data ->
                    val fechaMuestra = data["fecha_muestra"] as? Long ?: return@mapNotNull null
                    val gradosBrix = data["grados_brix"] as? Float ?: return@mapNotNull null
                    Entry(fechaMuestra.toFloat(), gradosBrix)
                }
            }


            val lineDataSet = LineDataSet(brixEntries, "Grados Brix")
            val lineData = LineData(lineDataSet)

            // Agregar la gráfica temporal
            AndroidView(
                factory = { context ->
                    LineChart(context).apply {
                        data = lineData
                        description.text = "Grados Brix vs Fecha Muestra"
                        xAxis.valueFormatter = object : ValueFormatter() {
                            override fun getFormattedValue(value: Float): String {
                                val date = Date(value.toLong())
                                val format = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                                return format.format(date)
                            }
                        }
                        axisRight.isEnabled = false
                        animateXY(1000, 1000)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .padding(top = 16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))
            val blockColorPercentages = filteredDataAnalysis
                .groupBy { it["bloque"] as? String ?: "" }
                .mapValues { (_, entries) ->
                    val totalEntries = entries.size
                    entries.groupingBy { it["color"] as? String ?: "" }
                        .eachCount()
                        .mapValues { (_, count) -> count.toFloat() / totalEntries * 100 }
                }

            // Crear las entradas para el gráfico de barras
            val barEntries = mutableListOf<BarEntry>()
            val blockNames = blockColorPercentages.keys.toList()
            var index = 0

            blockColorPercentages.forEach { (block, colorPercentages) ->
                val stackEntries = colorPercentages.entries.mapIndexed { colorIndex, (color, percentage) ->
                    BarEntry(index.toFloat(), percentage).apply {
                        // Asignamos una etiqueta personalizada para el color
                        data = color
                    }
                }
                barEntries.addAll(stackEntries)
                index++
            }
            val barDataSet = BarDataSet(barEntries, "Distribución de color por bloque").apply {
                // Set colors using setColors method
                setColors(ColorTemplate.COLORFUL_COLORS.toList().toIntArray(), 0)

                valueTextColor = android.graphics.Color.BLACK
                valueTextSize = 12f
                setDrawValues(true)
                setDrawIcons(false)

                // Disable highlighting
                isHighlightEnabled = false
            }


// Create the bar chart
            AndroidView(
                factory = { context ->
                    BarChart(context).apply {
                        data = BarData(barDataSet)
                        description.text = "Porcentaje de color por bloque"

                        // Configure the XAxis
                        xAxis.valueFormatter = object : ValueFormatter() {
                            override fun getFormattedValue(value: Float): String {
                                val index = value.toInt()
                                return if (index in blockNames.indices) blockNames[index] else ""
                            }
                        }
                        xAxis.position = XAxis.XAxisPosition.BOTTOM
                        xAxis.granularity = 1f
                        xAxis.labelRotationAngle = 45f
                        xAxis.labelCount = blockNames.size

                        // Configure the YAxis
                        axisRight.isEnabled = false
                        axisLeft.textColor = android.graphics.Color.BLACK
                        axisLeft.textSize = 12f

                        // Configure the legend
                        legend.isEnabled = true
                        legend.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
                        legend.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
                        legend.orientation = Legend.LegendOrientation.HORIZONTAL
                        legend.setDrawInside(false)

                        // Set to draw value above the bar
                        setDrawValueAboveBar(true)
                        animateXY(1000, 1000)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp) // Adjust the height of the bar chart
                    .padding(top = 16.dp)
            )

        }
    }
}
fun dateToMillis(dateString: String): Long {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return dateFormat.parse(dateString)?.time ?: 0L
}
@Composable
fun AutocompleteFilter(
    label: String,
    options: List<String>,
    selectedOption: String?,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var text by remember { mutableStateOf(selectedOption ?: "") }
    var expanded by remember { mutableStateOf(false) }
    val filteredOptions = options.filter { it.contains(text, ignoreCase = true) }.take(5)
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        modifier = modifier
            .background(Color.White)
            .border(1.dp, Color.Gray)
            .padding(4.dp)
    ) {
        TextField(
            value = text,
            onValueChange = {
                text = it
                expanded = it.isNotEmpty() // Show dropdown if text is not empty
                keyboardController?.show() // Show the keyboard when typing
            },
            label = { Text(label) },
            modifier = Modifier
                .fillMaxWidth()
                .onGloballyPositioned { coordinates ->
                    // Handle position updates if needed
                },
            singleLine = true
        )

        // Display dropdown only if there are filtered options
        if (expanded && filteredOptions.isNotEmpty()) {
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = {
                    expanded = false
                    keyboardController?.hide() // Hide keyboard when dropdown is dismissed
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .border(1.dp, Color.Gray)
            ) {
                filteredOptions.forEach { option ->
                    DropdownMenuItem(onClick = {
                        text = option
                        onOptionSelected(option)
                        expanded = false
                        keyboardController?.hide() // Hide keyboard after selection
                    }) {
                        Text(text = option)
                    }
                }
            }
        }
    }
}
fun formatNumberWithCustomSeparators(number: Double): String {
    // Configurar los símbolos para usar punto como separador de miles y coma como separador de decimales
    val symbols = DecimalFormatSymbols(Locale.getDefault()).apply {
        groupingSeparator = '.'
        decimalSeparator = ','
    }

    // Crear un formato con dos decimales
    val decimalFormat = DecimalFormat("#,##0.000", symbols)

    // Formatear el número usando el formato configurado
    return decimalFormat.format(number)
}

fun formatNumberWithCustomSeparators2(number: Double): String {
    // Configurar los símbolos para usar punto como separador de miles y coma como separador de decimales
    val symbols = DecimalFormatSymbols(Locale.getDefault()).apply {
        groupingSeparator = '.'
        decimalSeparator = ','
    }

    // Crear un formato con dos decimales
    val decimalFormat = DecimalFormat("#,##0", symbols)

    // Formatear el número usando el formato configurado
    return decimalFormat.format(number)
}





