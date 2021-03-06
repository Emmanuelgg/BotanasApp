package com.example.botanas

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.core.view.GravityCompat
import androidx.appcompat.app.ActionBarDrawerToggle
import android.view.MenuItem
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import android.view.Menu
import android.widget.FrameLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.view.isEmpty
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.example.botanas.api.MyFirebaseMessagingService
import com.example.botanas.db.MySqlHelper
import com.example.botanas.db.Permission
import com.example.botanas.services.SendDataService
import com.example.botanas.ui.login.Admin
import com.example.botanas.ui.login.LoginActivity
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.iid.FirebaseInstanceId
import kotlinx.android.synthetic.main.driver_shipload_item.*
import org.jetbrains.anko.db.delete
import java.io.IOException


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener,
    StorageFragment.OnFragmentInteractionListener,
    SellFragment.OnFragmentInteractionListener,
    SalesFragment.OnFragmentInteractionListener,
    LoadUpFragment.OnFragmentInteractionListener,
    DriverShiploadFragment.OnFragmentInteractionListener
{

    override fun onFragmentInteraction(uri: Uri) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private lateinit var storageFragment: StorageFragment
    private lateinit var sellFragment: SellFragment
    private lateinit var salesFragment: SalesFragment
    private lateinit var loadUpFragment: LoadUpFragment
    private lateinit var driverShiploadFragment: DriverShiploadFragment
    private val TAG = "MyFirebaseToken"
    private val messagingService = MyFirebaseMessagingService()
    private lateinit var mySqlHelper: MySqlHelper
    private lateinit var appContext: Context


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val permission = Permission(applicationContext)
        /*
        Impresora bluetooth para escanear dispositivos
        Printooth.init(applicationContext)
        startActivityForResult(Intent(this, ScanningActivity::class.java), ScanningActivity.SCANNING_FOR_PRINTER)
        */

        /*val fab: FloatingActionButton = findViewById(R.id.fab)
        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }*/

        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        navView.setNavigationItemSelectedListener(this)
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        appContext = this
        mySqlHelper = MySqlHelper(appContext)

        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()


        storageFragment =  StorageFragment.newInstance(this)
        sellFragment = SellFragment.newInstance()
        salesFragment = SalesFragment.newInstance(this)
        loadUpFragment = LoadUpFragment.newInstance()
        driverShiploadFragment = DriverShiploadFragment.newInstance(this)

        val headerView = navView.getHeaderView(0)
        val userName: TextView = headerView.findViewById(R.id.user_name)
        userName.text = Admin.name

        val name = intent.getStringExtra("user_name")
        if (name != null)
            Snackbar.make(findViewById(R.id.containerFragments), "¡Hola, $name!", Snackbar.LENGTH_LONG).setAction("Action", null).show()


        /*if (intent.getBooleanExtra("saleSuccessful", false)) {
            Snackbar.make(findViewById(R.id.containerFragments), R.string.sale_successful, Snackbar.LENGTH_LONG).setAction("Action", null).show()
        }*/
        val openSales = intent.getBooleanExtra("openSales", false)
        if (!openSales) {
            navView.menu.getItem(0).isChecked = true
            onNavigationItemSelected(navView.menu.getItem(0))
        } else  {
            salesFragment = SalesFragment.newInstance(this)
            navView.menu.getItem(2).isChecked = true
            onNavigationItemSelected(navView.menu.getItem(2))
        }

        if (permission.serverNotification())
            initView()

        startService(Intent(appContext, SendDataService::class.java))
    }

    private fun initView() {
        //This method will use for fetching Token
        Thread(Runnable {
            try {
                Log.i(TAG, FirebaseInstanceId.getInstance().getToken(getString(R.string.sender_id), "FCM"))
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }).start()

    }


    override fun onBackPressed() {
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }

        val mainFrame = findViewById<FrameLayout>(R.id.mainFrame)
        if  (mainFrame.isEmpty())
            changeFragment(storageFragment, "storage")
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> {
                val selectionActivity = Intent(appContext, SettingsActivity::class.java)
                startActivity(selectionActivity)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            /*R.id.nav_home -> {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            }*/
            R.id.nav_storage -> {
                changeFragment(storageFragment, "storage")
            }
            R.id.nav_sell -> {
                changeFragment(sellFragment, "sell")
            }
            R.id.nav_sales -> {
                changeFragment(salesFragment, "sales")
            }
            R.id.nav_load_up -> {
                changeFragment(loadUpFragment, "load_up")
            }
            R.id.nav_driver_shipload -> {
                changeFragment(driverShiploadFragment, "driver_shipload")
            }
            R.id.log_out -> {
                logOut()
            }
            else -> {
                changeFragment(salesFragment, "sales")
            }
        }
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun changeFragment(fragment: Fragment, tag: String) {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.mainFrame, fragment, tag)
            .addToBackStack(fragment.toString())
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            .commit()
    }

    private fun logOut() {
        val mySqlHelper = MySqlHelper(this)
        mySqlHelper.use {
            delete("settings")
            delete("admin")
            delete("product")
            delete("product_type")
            delete("product_base")
            delete("driver_general_inventory")
            delete("client")
            delete("requisition")
            delete("requisition_description")
        }
        finish()
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)

    }
}
