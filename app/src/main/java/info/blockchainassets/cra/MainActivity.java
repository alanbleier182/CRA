package info.blockchainassets.cra;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.navigation.NavigationView;

import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.Web3ClientVersion;
import org.web3j.protocol.http.HttpService;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;

    private Web3j web3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mDrawerLayout = findViewById(R.id.drawer);
        mToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.open, R.string.close);
        mDrawerLayout.addDrawerListener(mToggle);
        NavigationView nvDrawer = findViewById(R.id.nv);
        mToggle.syncState();
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        setupDrawerContent(nvDrawer);
    }

    public void connectToEthNetwork(View v) {
        toastAsync("Connecting to Ethereum network...");
        web3 = Web3j.build(new HttpService("https://rinkeby.infura.io/v3/9e4fadd65d4e4bbab2032b259b0979c3"));
        try {
            Web3ClientVersion clientVersion = web3.web3ClientVersion().sendAsync().get();
            if(!clientVersion.hasError()){
                toastAsync("Connected!");
            }
            else {
                toastAsync(clientVersion.getError().getMessage());
            }
        } catch (Exception e) {
            toastAsync(e.getMessage());
        }
    }

    public void toastAsync(String message) {
        runOnUiThread(() -> {
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(mToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void selectItemDrawer(MenuItem menuItem) {
        Fragment myFragment = null;
        Class fragmentClass;
        switch (menuItem.getItemId()) {
            case R.id.home:
                fragmentClass = Home.class;
                break;
            case R.id.portfolio:
                fragmentClass = Portfolio.class;
                break;
            case R.id.risk_management:
                fragmentClass = RiskManagement.class;
                break;
            case R.id.notifications:
                fragmentClass = Notifications.class;
                break;
            case R.id.settings:
                fragmentClass = Settings.class;
                break;
            default:
                fragmentClass = Home.class;
        }
        try {
            myFragment = (Fragment) fragmentClass.newInstance();
        } catch(Exception e) {
            e.printStackTrace();
        }
        FragmentManager fragmentManager;
        fragmentManager = getSupportFragmentManager();
        assert myFragment != null;
        fragmentManager.beginTransaction().replace(R.id.flcontent,myFragment).commit();
        menuItem.setChecked(true);
        setTitle(menuItem.getTitle());
        mDrawerLayout.closeDrawers();
    }

    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                selectItemDrawer(menuItem);
                return true;
            }
        });
    }
}
