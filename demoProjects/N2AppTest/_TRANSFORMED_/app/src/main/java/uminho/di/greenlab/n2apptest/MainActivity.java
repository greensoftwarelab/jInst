package uminho.di.greenlab.n2apptest;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
//import uminho.di.greenlab.trepnlibrary.TrepnLib;
import com.hunter.library.debug.*;
import android.content.Context;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    @HunterDebug
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //TrepnLib.traceMethod("uminho.di.greenlab.n2apptest.MainActivity->onCreate|-1377881982");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG).setAction("Action", null).show();
            }
        });
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @HunterDebug
    @Override
    public void onBackPressed() {
        // TrepnLib.traceMethod("uminho.di.greenlab.n2apptest.MainActivity->onBackPressed|0");
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @HunterDebug
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // TrepnLib.traceMethod("uminho.di.greenlab.n2apptest.MainActivity->onCreateOptionsMenu|3347807");
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @HunterDebug
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TrepnLib.traceMethod("uminho.di.greenlab.n2apptest.MainActivity->onOptionsItemSelected|-603141902");
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @HunterDebug
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // TrepnLib.traceMethod("uminho.di.greenlab.n2apptest.MainActivity->onNavigationItemSelected|-603141902");
        int id = item.getItemId();
        if (id == R.id.nav_camera) {
            sum(5,5);
        } else if (id == R.id.nav_gallery) {
            sum(5,5);
        } else if (id == R.id.nav_slideshow) {
            sum(5,5);
        } else if (id == R.id.nav_manage) {
            sum(10,10);
        } else if (id == R.id.nav_share) {
            sum(29,29);
        } else if (id == R.id.nav_send) {
            sum(50,50);
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @HunterDebug
    public int sum(int x, int y){
        return x+y;
    }
}
