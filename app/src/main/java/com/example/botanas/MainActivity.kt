package com.example.botanas

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.core.view.GravityCompat
import androidx.appcompat.app.ActionBarDrawerToggle
import android.view.MenuItem
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import android.view.Menu
import android.widget.TextView
import androidx.fragment.app.FragmentTransaction
import com.example.botanas.db.MySqlHelper
import com.example.botanas.ui.login.LoginActivity
import com.google.android.material.snackbar.Snackbar
import org.jetbrains.anko.db.delete


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener,
        StorageFragment.OnFragmentInteractionListener,
        SaleFragment.OnFragmentInteractionListener
{

    override fun onFragmentInteraction(uri: Uri) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    lateinit var storageFragment: StorageFragment
    lateinit var saleFragment: SaleFragment



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

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
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        storageFragment =  StorageFragment.newInstance(this)
        saleFragment = SaleFragment.newInstance()

        val headerView = navView.getHeaderView(0)
        val userName: TextView = headerView.findViewById(R.id.user_name)
        val name = intent.getStringExtra("user_name")
        userName.text = name
        val welcome = "¡Hola, $name!"
        Snackbar.make(findViewById(R.id.containerFragments), welcome, Snackbar.LENGTH_LONG).setAction("Action", null).show()

        navView.getMenu().getItem(0).setChecked(true)
        onNavigationItemSelected(navView.getMenu().getItem(0))
    }

    override fun onBackPressed() {
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
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
            R.id.action_settings -> true
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
                // Begin the transaction
                supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.mainFrame, storageFragment)
                    .addToBackStack(storageFragment.toString())
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .commit()

            }
            R.id.nav_sale -> {
                supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.mainFrame, saleFragment)
                    .addToBackStack(saleFragment.toString())
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .commit()
            }
            R.id.log_out -> {
                logOut()
            }
            else -> {
                supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.mainFrame, saleFragment)
                    .addToBackStack(saleFragment.toString())
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .commit()
            }
        }
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun logOut() {
        val mySqlHelper = MySqlHelper(this)
        mySqlHelper.use {delete("admin")}
        mySqlHelper.use {delete("driver_general_inventory")}
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}
